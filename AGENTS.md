# Amp Agent Configuration

## Project Overview

This is a Java/Spring Boot monorepo for the Punchout Platform with the following modules:
- `punchout-gateway` - Main gateway service (port 9090)
- `punchout-ui-backend` - UI backend API (port 8080)
- `punchout-order` - Order processing module
- `punchout-invoice` - Invoice processing module
- `punchout-common` - Shared utilities and models
- `punchout-mock-service` - Mock service for testing

Frontend: Located at `/Users/nickhu/github/punchout-ui-frontend` (Next.js, port 3000)

## Build Commands

```bash
# Build all modules
mvn clean install -DskipTests

# Build with tests
mvn clean install

# Run specific module tests
mvn test -pl punchout-ui-backend

# Type check (compile)
mvn compile
```

## GitHub Repository

- **Owner**: nickyhk4you
- **Repo**: punchout
- **URL**: https://github.com/nickyhk4you/punchout

## PR Commands

### Create PR

When the user types any of the following, create a GitHub PR:

- `create-pr` - Create PR for the current branch
- `create-pr <JIRA-ID>` - Create PR for branch containing JIRA ID (e.g., `create-pr JIRA-829283`)
- `create-pr <branch-name>` - Create PR for a specific branch

**How to Create a PR:**

1. **Find the branch**:
   ```bash
   # Current branch
   git branch --show-current
   
   # Or find by JIRA ID
   git branch -a | grep -i "JIRA-829283"
   ```

2. **Ensure branch is pushed**:
   ```bash
   git push -u origin <branch-name>
   ```

3. **Get changed files**:
   ```bash
   git diff --name-only origin/develop...<branch-name>
   ```

4. **Create the PR**:
   ```bash
   gh pr create \
     --title "[JIRA-ID] type: description" \
     --body "PR description with changes..." \
     --base develop \
     --head <branch-name> \
     --label "needs-review"
   ```

5. **Return PR URL** to user.

### Review PR

When the user types any of the following, perform a GitHub PR review:

- `review-pr <URL>` - Review PR by full GitHub URL
- `review-pr #<number>` - Review PR by number (e.g., `review-pr #17`)
- `review-pr <JIRA-ID>` - Search for PR by JIRA ticket ID in branch name

**How to Review a PR:**

1. **Get PR information** using GitHub CLI:
   ```bash
   gh pr view <number> --json title,body,files,additions,deletions,headRefName,baseRefName,author
   gh pr diff <number>
   ```

2. **Read changed files** and analyze for:
   - Security vulnerabilities
   - Bugs and logic errors
   - Code quality issues
   - Missing tests
   - Spring Boot best practices

3. **Post review to GitHub**:
   ```bash
   # Simple comment
   gh pr review <number> --comment --body "Review content..."
   
   # With inline comments via API
   gh api repos/nickyhk4you/punchout/pulls/<number>/reviews \
     -X POST \
     -f body="Summary" \
     -f event="COMMENT"
   ```

4. **Summarize** the review for the user with issues found and recommendations.

## Code Style

- Java 11
- Spring Boot 2.7.x
- Lombok for boilerplate reduction
- MongoDB for data storage
- Follow existing patterns in codebase

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClassName

# Run with coverage
mvn test jacoco:report
```
