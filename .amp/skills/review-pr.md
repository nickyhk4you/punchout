# PR Review Skill

This skill enables reviewing GitHub Pull Requests directly from the Amp prompt.

## Usage

Type one of the following commands:
- `review-pr <PR-URL>` - Review a PR by its full URL
- `review-pr #<number>` - Review a PR by number in the current repo
- `review-pr <JIRA-ID>` - Review a PR associated with a JIRA ticket (searches branches)

## Examples

```
review-pr https://github.com/nickyhk4you/punchout/pull/17
review-pr #17
review-pr JIRA-23232
```

## Instructions for Amp

When the user requests a PR review, follow these steps:

### Step 1: Parse the Input

Determine the PR identifier from the user's input:
- If it's a URL like `https://github.com/{owner}/{repo}/pull/{number}`, extract owner, repo, and PR number
- If it's `#{number}`, use the current repo (nickyhk4you/punchout) and the number
- If it's a JIRA ID pattern (e.g., `JIRA-12345`, `PROJ-123`), search for branches containing that ID

### Step 2: Fetch PR Information

Use the GitHub CLI or API to get PR details:

```bash
# Get PR details
gh pr view <number> --json title,body,files,additions,deletions,changedFiles,headRefName,baseRefName,state,author

# Get the diff
gh pr diff <number>

# List changed files
gh pr view <number> --json files --jq '.files[].path'
```

### Step 3: Review the Changed Files

For each changed file:
1. Read the current version of the file using the Read tool
2. Analyze the changes for:
   - **Security Issues**: SQL injection, XSS, hardcoded secrets, authentication bypasses
   - **Bugs**: Logic errors, null pointer risks, race conditions, resource leaks
   - **Code Quality**: Code smells, duplicate code, overly complex methods
   - **Best Practices**: Spring Boot conventions, Java patterns, error handling
   - **Performance**: N+1 queries, inefficient loops, memory issues
   - **Missing Tests**: New code without corresponding tests

### Step 4: Generate Review Comments

For each issue found, create a comment with:
- The file path and line number
- Severity: 🔴 Critical, ⚠️ Warning, 💡 Suggestion, ✅ Good Practice
- Clear description of the issue
- Recommended fix

### Step 5: Post Review to GitHub

Use the GitHub CLI to post the review:

```bash
# Post a review comment
gh pr review <number> --comment --body "Review summary..."

# Or post inline comments (for specific files)
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  -X POST \
  -f body="Review summary" \
  -f event="COMMENT" \
  -f comments[][path]="file.java" \
  -f comments[][line]=42 \
  -f comments[][body]="Comment text"
```

### Step 6: Summarize the Review

Provide a summary to the user including:
- PR title and author
- Number of files changed
- Issues found by severity
- Overall assessment (Approve / Request Changes / Comment)

## Review Checklist

### Security
- [ ] No hardcoded credentials or secrets
- [ ] Input validation on user data
- [ ] Proper authentication/authorization checks
- [ ] No SQL injection vulnerabilities
- [ ] No XSS vulnerabilities
- [ ] Secure password handling

### Code Quality
- [ ] Methods are not too long (< 50 lines ideally)
- [ ] Classes follow single responsibility principle
- [ ] Proper error handling with meaningful messages
- [ ] No code duplication
- [ ] Clear naming conventions
- [ ] Appropriate logging

### Spring Boot Specific
- [ ] Proper use of annotations (@Service, @Repository, etc.)
- [ ] Transaction boundaries correctly defined
- [ ] REST endpoints follow conventions
- [ ] Proper exception handling with @ControllerAdvice
- [ ] Configuration externalized appropriately

### Testing
- [ ] Unit tests for new functionality
- [ ] Edge cases covered
- [ ] Mocks used appropriately
- [ ] Test names are descriptive

## Output Format

```markdown
## 🔍 PR Review: #{number} - {title}

**Author**: @{author}
**Branch**: {head} → {base}
**Files Changed**: {count}

### 📊 Summary

| Category | Count |
|----------|-------|
| 🔴 Critical | X |
| ⚠️ Warnings | X |
| 💡 Suggestions | X |
| ✅ Good | X |

### 🔴 Critical Issues

#### {file}:{line}
{description}
**Fix**: {recommendation}

### ⚠️ Warnings

...

### 💡 Suggestions

...

### ✅ What's Good

...

### 📋 Recommendation

**{APPROVE / REQUEST_CHANGES / COMMENT}**

{Final summary and next steps}
```
