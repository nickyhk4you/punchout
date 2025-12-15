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

mkdir -p .github
echo "[]" > .github/file-comments.json

# Review categories
declare -A REVIEW_ISSUES
REVIEW_ISSUES[security]=0
REVIEW_ISSUES[testing]=0
REVIEW_ISSUES[code_quality]=0
REVIEW_ISSUES[patterns]=0
REVIEW_ISSUES[logic]=0

add_file_comment() {
    local file="$1"
    local line="$2"
    local body="$3"
    local category="$4"
    
    REVIEW_ISSUES[$category]=$((${REVIEW_ISSUES[$category]} + 1))
    
    local comments=$(cat .github/file-comments.json)
    local new_comment=$(cat << EOF
{
  "path": "$file",
  "line": $line,
  "body": "$body",
  "category": "$category"
}
EOF
)
    
    echo "$comments" | jq ". + [$new_comment]" > .github/file-comments.json
}

analyze_java_file() {
    local file="$1"
    
    log_info "Analyzing: $file"
    
    if [ ! -f "$file" ]; then
        return
    fi
    
    local content=$(cat "$file" 2>/dev/null || echo "")
    
    if echo "$content" | grep -n "password" | grep -v "Password" | grep -v "ENC(" | grep -v "@" | head -1 | grep -q "."; then
        local line=$(echo "$content" | grep -n "password" | grep -v "Password" | grep -v "ENC(" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "🔒 **Security**: Potential hardcoded password detected." "security"
    fi
    
    if echo "$content" | grep -qn "System\.out\.print"; then
        local line=$(echo "$content" | grep -n "System\.out\.print" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "📝 **Code Quality**: Use proper logging instead of System.out.println" "code_quality"
    fi
    
    if echo "$content" | grep -qnE "TODO|FIXME"; then
        local line=$(echo "$content" | grep -nE "TODO|FIXME" | head -1 | cut -d: -f1)
        add_file_comment "$file" "${line:-1}" "📌 **Technical Debt**: TODO/FIXME found." "code_quality"
    fi
    
    if echo "$content" | grep -qE "catch.*\{[[:space:]]*\}"; then
        add_file_comment "$file" 1 "⚠️ **Code Quality**: Empty catch block detected." "code_quality"
    fi
}

check_test_coverage() {
    log_info "Checking test coverage requirements..."
    
    local java_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith(".java")) | select(.filename | contains("Test") | not) | .filename' 2>/dev/null)
    local test_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | contains("Test.java")) | .filename' 2>/dev/null)
    
    for java_file in $java_files; do
        if echo "$java_file" | grep -q "/test/"; then
            continue
        fi
        
        local class_name=$(basename "$java_file" .java)
        local expected_test="${class_name}Test.java"
        
        if ! echo "$test_files" | grep -q "$expected_test"; then
            if ! find . -name "$expected_test" -type f 2>/dev/null | grep -q .; then
                add_file_comment "$java_file" 1 "🧪 **Testing**: No corresponding test file found." "testing"
            fi
        fi
    done
}

check_dependency_changes() {
    log_info "Checking for dependency changes..."
    
    local pom_changes=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith("pom.xml")) | .filename' 2>/dev/null)
    
    for pom in $pom_changes; do
        if [ -f "$pom" ]; then
            add_file_comment "$pom" 1 "📦 **Dependencies**: POM file modified. Review for vulnerabilities." "security"
        fi
    done
}

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

---
*Automated review by Sourcegraph AMP SDK - $(date -u +"%Y-%m-%d %H:%M:%S UTC")*
EOF

    log_success "Review summary generated"
}

amp_intelligent_review() {
    if [ -f "review-output.md" ]; then
        log_info "Using pre-generated AMP review"
        cat >> .github/review-summary.md << EOF

### 🤖 AI-Powered Analysis

$(cat review-output.md)
EOF
        return 0
    fi
    
    if [ -n "$AMP_API_KEY" ] && command -v node &> /dev/null; then
        log_info "Generating review with Sourcegraph AMP SDK..."
        
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

main() {
    log_info "PR #$PR_NUMBER: $PR_TITLE"
    
    local java_files=$(echo "$CHANGED_FILES" | jq -r '.[] | select(.filename | endswith(".java")) | .filename' 2>/dev/null)
    
    for file in $java_files; do
        if [ -f "$file" ]; then
            analyze_java_file "$file"
        fi
    done
    
    check_test_coverage
    check_dependency_changes
    amp_intelligent_review || true
    generate_summary
    
    cat >> .github/amp-review-log.txt << EOF
=== AMP PR Review Log ===
Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
PR Number: $PR_NUMBER
PR Title: $PR_TITLE
Java Files Reviewed: $JAVA_FILES_COUNT
Test Files: $TEST_FILES_COUNT
Security Issues: ${REVIEW_ISSUES[security]}
Testing Issues: ${REVIEW_ISSUES[testing]}
Code Quality Issues: ${REVIEW_ISSUES[code_quality]}
EOF
    
    log_success "PR review complete"
    
    echo ""
    echo "========================================="
    echo "         AMP PR Review Complete          "
    echo "========================================="
    cat .github/review-summary.md
}

main "$@"
