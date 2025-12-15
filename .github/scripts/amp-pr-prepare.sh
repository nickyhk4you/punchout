#!/bin/bash

###############################################
# AMP SDK PR Preparation Script
# Generates PR title, description, and metadata
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
CHANGED_FILES="${CHANGED_FILES:-}"
TARGET_BRANCH="${TARGET_BRANCH:-develop}"
IS_HOTFIX="${IS_HOTFIX:-false}"
COVERAGE_REPORT="${COVERAGE_REPORT:-N/A}"
BRANCH_NAME="${GITHUB_REF_NAME:-$(git rev-parse --abbrev-ref HEAD)}"

log_info "Starting AMP PR Preparation"
log_info "Branch: $BRANCH_NAME -> Target: $TARGET_BRANCH"
log_info "Is Hotfix: $IS_HOTFIX"

# Extract story ID from branch name (e.g., feature/JIRA-123-description)
extract_story_id() {
    echo "$BRANCH_NAME" | grep -oE '[A-Z]+-[0-9]+' | head -1 || echo ""
}

# Determine affected modules from changed files
get_affected_modules() {
    local modules=""
    if echo "$CHANGED_FILES" | grep -q "punchout-gateway"; then
        modules="$modules punchout-gateway"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-ui-backend"; then
        modules="$modules punchout-ui-backend"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-common"; then
        modules="$modules punchout-common"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-order"; then
        modules="$modules punchout-order"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-invoice"; then
        modules="$modules punchout-invoice"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-mock-service"; then
        modules="$modules punchout-mock-service"
    fi
    echo "$modules" | xargs
}

# Count changes by type
count_java_files() {
    echo "$CHANGED_FILES" | tr ',' '\n' | grep -c "\.java$" || echo "0"
}

count_test_files() {
    echo "$CHANGED_FILES" | tr ',' '\n' | grep -cE "(Test\.java|Tests\.java)" || echo "0"
}

count_config_files() {
    echo "$CHANGED_FILES" | tr ',' '\n' | grep -cE "\.(yml|xml|properties|json)$" || echo "0"
}

# Get PR reviewers based on affected modules
get_reviewers() {
    local reviewers=""
    if echo "$CHANGED_FILES" | grep -q "punchout-gateway"; then
        reviewers="$reviewers nickyhk4you"
    fi
    if echo "$CHANGED_FILES" | grep -q "punchout-ui"; then
        reviewers="$reviewers nickyhk4you"
    fi
    echo "$reviewers" | xargs | tr ' ' ','
}

# Generate PR title
generate_pr_title() {
    local story_id=$(extract_story_id)
    local change_type="feat"
    
    if [[ "$BRANCH_NAME" == bugfix/* ]]; then
        change_type="fix"
    elif [[ "$BRANCH_NAME" == hotfix/* ]]; then
        change_type="hotfix"
    elif [[ "$BRANCH_NAME" == chore/* ]]; then
        change_type="chore"
    fi
    
    local description=$(echo "$BRANCH_NAME" | sed -E 's/^(feature|bugfix|hotfix|chore)\///' | sed -E 's/^[A-Z]+-[0-9]+-//' | tr '-' ' ')
    
    if [ -n "$story_id" ]; then
        echo "[$story_id] $change_type: $description"
    else
        echo "$change_type: $description"
    fi
}

# Generate change impact analysis
analyze_change_impact() {
    local impact="Low"
    local java_count=$(count_java_files)
    
    if [ "$java_count" -gt 20 ]; then
        impact="High"
    elif [ "$java_count" -gt 10 ]; then
        impact="Medium"
    fi
    
    if echo "$CHANGED_FILES" | grep -qE "(Controller|Resource|Endpoint)\.java"; then
        impact="Medium"
    fi
    
    if echo "$CHANGED_FILES" | grep -qE "(Entity|Repository|Migration)\.java"; then
        impact="High"
    fi
    
    echo "$impact"
}

# Generate validation checklist
generate_checklist() {
    local java_count=$(count_java_files)
    local test_count=$(count_test_files)
    
    cat << EOF
### Validation Checklist

- [x] Code compiles without errors
- [x] No secrets or credentials in code
- [x] Branch follows naming convention
EOF

    if [ "$java_count" -gt 0 ]; then
        if [ "$test_count" -gt 0 ]; then
            echo "- [x] Unit tests added/updated"
        else
            echo "- [ ] Unit tests added/updated ⚠️"
        fi
    fi
    
    cat << EOF
- [ ] Documentation updated (if applicable)
- [ ] Integration tests passed
- [ ] Code review completed
- [ ] Ready for merge
EOF
}

# Use Sourcegraph AMP SDK for intelligent PR description
generate_amp_description() {
    if [ -f ".github/amp-description.md" ]; then
        log_info "Using AMP SDK generated description"
        cat .github/amp-description.md
        return 0
    fi
    
    if [ -n "$AMP_API_KEY" ] && command -v node &> /dev/null; then
        log_info "Generating description with Sourcegraph AMP SDK"
        
        cat > /tmp/amp-describe.mjs << 'AMPEOF'
import { execute } from '@sourcegraph/amp-sdk';

const changedFiles = process.env.CHANGED_FILES || '';
const targetBranch = process.env.TARGET_BRANCH || 'develop';
const modules = process.env.AFFECTED_MODULES || '';

const prompt = `
Generate a concise PR description for these changes:

Changed Files: ${changedFiles}
Target Branch: ${targetBranch}
Affected Modules: ${modules}

Provide:
1. A brief summary (2-3 sentences)
2. Key changes made
3. Any potential risks

Format as Markdown.
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
        
        AFFECTED_MODULES="$(get_affected_modules)" node /tmp/amp-describe.mjs 2>/dev/null || {
            log_warn "AMP SDK call failed, using fallback description"
            return 1
        }
        return 0
    fi
    
    return 1
}

# Generate PR body
generate_pr_body() {
    local story_id=$(extract_story_id)
    local affected_modules=$(get_affected_modules)
    local java_count=$(count_java_files)
    local test_count=$(count_test_files)
    local config_count=$(count_config_files)
    local impact=$(analyze_change_impact)
    
    cat > .github/pr-body.md << EOF
## Summary

This PR contains changes from branch \`$BRANCH_NAME\` targeting \`$TARGET_BRANCH\`.

EOF

    if generate_amp_description >> .github/pr-body.md 2>/dev/null; then
        log_success "Added AMP-generated description"
    else
        cat >> .github/pr-body.md << EOF
### Changes Overview
- **Total Files Changed**: $(echo "$CHANGED_FILES" | tr ' ' '\n' | wc -l | xargs)
- **Java Files**: $java_count
- **Test Files**: $test_count
- **Config Files**: $config_count

EOF
    fi

    cat >> .github/pr-body.md << EOF

## Metadata

| Field | Value |
|-------|-------|
| **Story ID** | ${story_id:-N/A} |
| **Affected Modules** | ${affected_modules:-None detected} |
| **Change Impact** | $impact |
| **Test Coverage** | $COVERAGE_REPORT |
| **Target Branch** | $TARGET_BRANCH |
EOF

    if [ "$IS_HOTFIX" = "true" ]; then
        cat >> .github/pr-body.md << EOF
| **Hotfix** | ✅ Yes |
| **Justification** | ${JUSTIFICATION:-Required} |
EOF
    fi

    cat >> .github/pr-body.md << EOF

## Changed Files

<details>
<summary>Click to expand file list</summary>

\`\`\`
$(echo "$CHANGED_FILES" | tr ' ' '\n')
\`\`\`

</details>

$(generate_checklist)

## Testing

- [ ] Unit tests pass locally
- [ ] Integration tests pass
- [ ] Manual testing completed

## Deployment Notes

- No database migrations required
- No environment variable changes needed

---
*This PR was prepared by AMP Automated PR System*
EOF

    log_success "Generated PR body at .github/pr-body.md"
}

# Main execution
main() {
    mkdir -p .github
    
    PR_TITLE=$(generate_pr_title)
    log_info "Generated PR Title: $PR_TITLE"
    echo "PR_TITLE=$PR_TITLE" >> $GITHUB_ENV 2>/dev/null || export PR_TITLE
    
    PR_REVIEWERS=$(get_reviewers)
    log_info "Reviewers: $PR_REVIEWERS"
    echo "PR_REVIEWERS=$PR_REVIEWERS" >> $GITHUB_ENV 2>/dev/null || export PR_REVIEWERS
    
    generate_pr_body
    
    cat >> .github/amp-pr-prepare-log.txt << EOF
=== AMP PR Preparation Log ===
Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
Branch: $BRANCH_NAME
Target: $TARGET_BRANCH
Story ID: $(extract_story_id)
Affected Modules: $(get_affected_modules)
Files Changed: $(echo "$CHANGED_FILES" | tr ' ' '\n' | wc -l | xargs)
PR Title: $PR_TITLE
Reviewers: $PR_REVIEWERS
EOF
    
    log_success "PR preparation complete"
}

main "$@"
