#!/bin/bash

###############################################
# AMP SDK PR Review Script
# Performs automated code review and generates comments
###############################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Configuration
PR_NUMBER="${PR_NUMBER:-0}"
PR_TITLE="${PR_TITLE:-}"
PR_BODY="${PR_BODY:-}"
CHANGED_FILES="${CHANGED_FILES:-[]}"
JAVA_FILES_COUNT="${JAVA_FILES_COUNT:-0}"
TEST_FILES_COUNT="${TEST_FILES_COUNT:-0}"

log_info "Starting AMP PR Review for PR #$PR_NUMBER"

# Create output directory
mkdir -p .github

# Initialize file comments array
echo "[]" > .github/file-comments.json

# Review categories
declare -A REVIEW_ISSUES
REVIEW_ISSUES[security]=0
REVIEW_ISSUES[testing]=0
REVIEW_ISSUES[code_quality]=0
REVIEW_ISSUES[patterns]=0
REVIEW_ISSUES[logic]=0

# Add a file-specific comment
add_file_comment() {
    local file="$1"
    local line="$2"
    local body="$3"
    local category="$4"
    
    # Increment issue counter
    REVIEW_ISSUES[$category]=$((${REVIEW_ISSUES[$category]} + 1))
    
    # Read existing comments
    local comments=$(cat .github/file-comments.json)
    
    # Add new comment
    local new_comment=$(cat << EOF
{
  "path": "$file",
  "line": $line,
  "body": "$body",
  "category": "$category"
}
EOF
)
    
    # Append to array
    echo "$comments" | jq ". + [$new_comment]" > .github/file-comments.json
}

# Analyze Java file for common issues
analyze_java_file() {
    local file="$1"
    local patch="$2"
    
    log_info "Analyzing: $file"
    
    # Skip if file doesn't exist or is a deletion
    if [ ! -f "$file" ]; then
        return
    fi
    
    local content=$(cat "$file" 2>/dev/null || echo "")
    local line_number=1
    
    # Check for common security issues
    if echo "$content" | grep -n "password" | grep -v "Password" | grep -v "ENC(" | grep -v "@" | head -1 | grep -q "."; then
        local line=$(echo "$content" | grep -n "password" | grep -v "Password" | grep -v "ENC(" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "🔒 **Security**: Potential hardcoded password detected. Consider using encrypted configuration." "security"
    fi
    
    # Check for System.out.println
    if echo "$content" | grep -qn "System\.out\.print"; then
        local line=$(echo "$content" | grep -n "System\.out\.print" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "📝 **Code Quality**: Use proper logging (e.g., \`log.info()\`) instead of \`System.out.println\`" "code_quality"
    fi
    
    # Check for TODO/FIXME comments
    if echo "$content" | grep -qnE "TODO|FIXME"; then
        local line=$(echo "$content" | grep -nE "TODO|FIXME" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "📌 **Technical Debt**: TODO/FIXME found. Consider addressing or creating a ticket." "code_quality"
    fi
    
    # Check for empty catch blocks
    if echo "$content" | grep -qE "catch.*\{[[:space:]]*\}"; then
        add_file_comment "$file" 1 "⚠️ **Code Quality**: Empty catch block detected. Consider logging the exception or handling it properly." "code_quality"
    fi
    
    # Check for @SuppressWarnings
    if echo "$content" | grep -q "@SuppressWarnings"; then
        local line=$(echo "$content" | grep -n "@SuppressWarnings" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "🔍 **Code Quality**: \`@SuppressWarnings\` used. Ensure this is intentional and documented." "code_quality"
    fi
    
    # Check for missing @Transactional in service methods that modify data
    if echo "$file" | grep -q "Service\.java"; then
        if echo "$content" | grep -qE "(save|delete|update)" && ! echo "$content" | grep -q "@Transactional"; then
            add_file_comment "$file" 1 "💡 **Pattern**: Service class with data modification methods may need \`@Transactional\` annotation." "patterns"
        fi
    fi
    
    # Check for proper exception handling in controllers
    if echo "$file" | grep -q "Controller\.java"; then
        if ! echo "$content" | grep -qE "@ExceptionHandler|@ControllerAdvice"; then
            # Only warn if there are methods that could throw
            if echo "$content" | grep -qE "throws.*Exception"; then
                add_file_comment "$file" 1 "💡 **Pattern**: Controller has methods that throw exceptions. Consider adding proper exception handling." "patterns"
            fi
        fi
    fi
    
    # Check for magic numbers
    if echo "$content" | grep -qE "return [0-9]{3,}|== [0-9]{3,}|\+ [0-9]{3,}"; then
        add_file_comment "$file" 1 "📝 **Code Quality**: Magic numbers detected. Consider using named constants." "code_quality"
    fi
    
    # Check for long methods (rough heuristic)
    local method_count=$(echo "$content" | grep -cE "public|private|protected" || echo "0")
    local line_count=$(echo "$content" | wc -l)
    if [ "$line_count" -gt 500 ] && [ "$method_count" -lt 5 ]; then
        add_file_comment "$file" 1 "📏 **Code Quality**: File is large with few methods. Consider breaking into smaller, focused methods." "code_quality"
    fi
}

# Check for missing tests
check_test_coverage() {
    log_info "Checking test coverage requirements..."
    
    local java_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith(".java")) | select(.filename | contains("Test") | not) | .filename')
    local test_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | contains("Test.java")) | .filename')
    
    for java_file in $java_files; do
        # Skip if it's in test directory
        if echo "$java_file" | grep -q "/test/"; then
            continue
        fi
        
        # Get the class name
        local class_name=$(basename "$java_file" .java)
        
        # Check if corresponding test exists
        local expected_test="${class_name}Test.java"
        if ! echo "$test_files" | grep -q "$expected_test"; then
            # Check if test file exists in the repo
            if ! find . -name "$expected_test" -type f 2>/dev/null | grep -q .; then
                add_file_comment "$java_file" 1 "🧪 **Testing**: No corresponding test file found (\`$expected_test\`). Consider adding unit tests." "testing"
            fi
        fi
    done
}

# Analyze dependency changes
check_dependency_changes() {
    log_info "Checking for dependency changes..."
    
    local pom_changes=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith("pom.xml")) | .filename')
    
    for pom in $pom_changes; do
        if [ -f "$pom" ]; then
            add_file_comment "$pom" 1 "📦 **Dependencies**: POM file modified. Ensure new dependencies are approved and don't introduce vulnerabilities." "security"
        fi
    done
}

# Generate review summary
generate_summary() {
    log_info "Generating review summary..."
    
    local total_issues=0
    for key in "${!REVIEW_ISSUES[@]}"; do
        total_issues=$((total_issues + ${REVIEW_ISSUES[$key]}))
    done
    
    cat > .github/review-summary.md << EOF
### 📊 Review Summary

| Category | Issues Found |
|----------|-------------|
| 🔒 Security | ${REVIEW_ISSUES[security]} |
| 🧪 Testing | ${REVIEW_ISSUES[testing]} |
| 📝 Code Quality | ${REVIEW_ISSUES[code_quality]} |
| 📐 Patterns | ${REVIEW_ISSUES[patterns]} |
| 🔍 Logic | ${REVIEW_ISSUES[logic]} |
| **Total** | **$total_issues** |

### 📁 Files Analyzed

- **Java Files**: $JAVA_FILES_COUNT
- **Test Files**: $TEST_FILES_COUNT

### 🎯 Recommendations

EOF

    if [ "${REVIEW_ISSUES[security]}" -gt 0 ]; then
        echo "- ⚠️ **Security issues detected** - Please address before merging" >> .github/review-summary.md
    fi
    
    if [ "${REVIEW_ISSUES[testing]}" -gt 0 ]; then
        echo "- 🧪 **Missing tests detected** - Consider adding test coverage" >> .github/review-summary.md
    fi
    
    if [ "$total_issues" -eq 0 ]; then
        echo "- ✅ No major issues detected. Ready for human review." >> .github/review-summary.md
    fi
    
    cat >> .github/review-summary.md << EOF

### ℹ️ Review Notes

- This is an automated review. Human review is still required.
- File-specific comments have been added where applicable.
- Please address any security or testing concerns before merging.

---
*Automated review by AMP SDK - $(date -u +"%Y-%m-%d %H:%M:%S UTC")*
EOF

    log_success "Review summary generated"
}

# Use Sourcegraph AMP SDK for intelligent review (if available)
amp_intelligent_review() {
    # Check if review was already generated by the workflow
    if [ -f "review-output.md" ]; then
        log_info "Using pre-generated AMP review"
        cat >> .github/review-summary.md << EOF

### 🤖 AI-Powered Analysis

$(cat review-output.md)
EOF
        return 0
    fi
    
    # Try to generate using Node.js if AMP SDK is installed
    if [ -n "$AMP_API_KEY" ] && command -v node &> /dev/null; then
        log_info "Generating review with Sourcegraph AMP SDK..."
        
        # Get changed file list
        local files_to_review=""
        for file in $(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith(".java")) | .filename' 2>/dev/null | head -5); do
            if [ -f "$file" ]; then
                files_to_review="$files_to_review $file"
            fi
        done
        
        if [ -z "$files_to_review" ]; then
            log_warn "No Java files to review"
            return 1
        fi
        
        cat > /tmp/amp-review.mjs << 'AMPEOF'
import { execute } from '@sourcegraph/amp-sdk';
import { readFileSync, existsSync } from 'fs';

const files = process.env.FILES_TO_REVIEW?.trim().split(' ').filter(Boolean) || [];

let fileContents = '';
for (const file of files) {
  if (existsSync(file)) {
    try {
      const content = readFileSync(file, 'utf8');
      fileContents += `\n--- ${file} ---\n${content.substring(0, 2000)}`;
    } catch (e) {}
  }
}

const prompt = `
Review this Java code for:
1. Bugs or logic errors
2. Security vulnerabilities
3. Performance issues
4. Best practices violations
5. Missing error handling

Files: ${files.join(', ')}

${fileContents}

Provide specific, actionable feedback in Markdown.
`;

let result = '';
for await (const message of execute({
  prompt,
  options: { dangerouslyAllowAll: true, cwd: process.cwd() }
})) {
  if (message.type === 'result' && !message.is_error) {
    result = message.result;
  }
}

console.log(result);
AMPEOF
        
        FILES_TO_REVIEW="$files_to_review" node /tmp/amp-review.mjs > /tmp/amp-review-result.md 2>/dev/null || {
            log_warn "AMP SDK intelligent review failed"
            return 1
        }
        
        if [ -f /tmp/amp-review-result.md ] && [ -s /tmp/amp-review-result.md ]; then
            cat >> .github/review-summary.md << EOF

### 🤖 AI-Powered Analysis

$(cat /tmp/amp-review-result.md)
EOF
            return 0
        fi
    fi
    
    return 1
}

# Main execution
main() {
    log_info "PR #$PR_NUMBER: $PR_TITLE"
    
    # Parse changed files and analyze each
    local java_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith(".java")) | .filename')
    
    for file in $java_files; do
        if [ -f "$file" ]; then
            local patch=$(echo "$CHANGED_FILES" | jq -r ".[] | select(.filename == \"$file\") | .patch")
            analyze_java_file "$file" "$patch"
        fi
    done
    
    # Additional checks
    check_test_coverage
    check_dependency_changes
    
    # Try AMP intelligent review
    amp_intelligent_review || true
    
    # Generate summary
    generate_summary
    
    # Log the review action
    cat >> amp-review-log.txt << EOF
=== AMP PR Review Log ===
Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
PR Number: $PR_NUMBER
PR Title: $PR_TITLE
Java Files Reviewed: $JAVA_FILES_COUNT
Test Files: $TEST_FILES_COUNT
Security Issues: ${REVIEW_ISSUES[security]}
Testing Issues: ${REVIEW_ISSUES[testing]}
Code Quality Issues: ${REVIEW_ISSUES[code_quality]}
Pattern Issues: ${REVIEW_ISSUES[patterns]}
Logic Issues: ${REVIEW_ISSUES[logic]}
EOF
    
    log_success "PR review complete"
    
    # Output summary to console
    echo ""
    echo "========================================="
    echo "         AMP PR Review Complete          "
    echo "========================================="
    cat .github/review-summary.md
}

# Run main function
main "$@"
