# AMP PR Automation Guide

This document describes the automated Pull Request preparation and review system powered by AMP SDK.

## Overview

The AMP PR Automation system provides:

1. **Automated Pre-PR Checks** - Code quality, security scanning, and validation
2. **Automated PR Preparation** - Generates PR title, description, and metadata
3. **Automated PR Review** - Posts structured review comments on PRs
4. **Guardrail Compliance** - Ensures secure operations and proper logging

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Actions                          │
├─────────────────┬─────────────────┬─────────────────────────┤
│  amp-pr-prepare │  amp-pr-review  │  guardrails-check       │
│    workflow     │    workflow     │    workflow             │
├─────────────────┴─────────────────┴─────────────────────────┤
│                    Shell Scripts                            │
├─────────────────┬─────────────────┬─────────────────────────┤
│ amp-pr-prepare  │ amp-pr-review   │  (static analysis)      │
│      .sh        │      .sh        │                         │
├─────────────────┴─────────────────┴─────────────────────────┤
│                      AMP SDK                                │
│         (Intelligent code analysis & generation)            │
└─────────────────────────────────────────────────────────────┘
```

## Workflows

### 1. PR Preparation Workflow (`amp-pr-prepare.yml`)

**Triggered by:**
- Push to `feature/*`, `bugfix/*`, or `hotfix/*` branches
- Manual workflow dispatch

**Steps:**
1. Validates branch strategy (develop vs main targeting)
2. Runs pre-PR quality checks (build, test, lint)
3. Scans for secrets and restricted files
4. Generates PR title, description, and metadata
5. Creates the Pull Request with proper tags

### 2. PR Review Workflow (`amp-pr-review.yml`)

**Triggered by:**
- Pull request opened, synchronized, or reopened

**Steps:**
1. Analyzes changed files
2. Runs build and static analysis
3. Performs automated code review using AMP SDK
4. Posts overall review summary comment
5. Posts file-specific review comments
6. Checks test coverage
7. Validates guardrail compliance

## Branch Strategy

| Branch Pattern | Target Branch | Requirements |
|---------------|---------------|--------------|
| `feature/*` | develop | Standard review |
| `bugfix/*` | develop | Standard review |
| `hotfix/*` | main | Justification required |

### Hotfix Requirements

Hotfix branches targeting `main` require:
- Justification (e.g., JIRA ticket number)
- Urgency assessment
- Rollback plan

## Setup

### Prerequisites

1. **GitHub Secrets** (Required):
   ```
   AMP_API_KEY - API key for Sourcegraph AMP SDK
   ```

2. **Repository Settings**:
   - Enable GitHub Actions
   - Allow workflow to create PRs
   - Configure branch protection rules

### Installation

1. Ensure the following files exist in your repository:

   ```
   .github/
   ├── workflows/
   │   ├── amp-pr-prepare.yml
   │   └── amp-pr-review.yml
   ├── PULL_REQUEST_TEMPLATE.md
   scripts/
   ├── amp-pr-prepare.sh
   └── amp-pr-review.sh
   ```

2. Make shell scripts executable:
   ```bash
   chmod +x scripts/amp-pr-prepare.sh
   chmod +x scripts/amp-pr-review.sh
   ```

3. Configure GitHub Secrets:
   - Go to Repository → Settings → Secrets and variables → Actions
   - Add `AMP_API_KEY` secret (Sourcegraph AMP API key)

## Usage

### Creating a Feature PR

1. Create a feature branch:
   ```bash
   git checkout -b feature/JIRA-123-add-new-feature
   ```

2. Make your changes and commit:
   ```bash
   git add .
   git commit -m "feat: implement new feature"
   ```

3. Push to trigger automation:
   ```bash
   git push origin feature/JIRA-123-add-new-feature
   ```

4. The system will:
   - Run quality checks
   - Scan for secrets
   - Generate PR with proper template
   - Post automated review comments

### Manual Workflow Trigger

You can manually trigger PR preparation:

1. Go to Actions → AMP PR Preparation
2. Click "Run workflow"
3. Select branch and target
4. Provide justification if targeting main

## Review Categories

The automated review checks for:

| Category | Description |
|----------|-------------|
| 🔒 Security | Hardcoded secrets, credentials, vulnerabilities |
| 🧪 Testing | Missing tests, low coverage |
| 📝 Code Quality | Logging, error handling, magic numbers |
| 📐 Patterns | Design patterns, annotations, conventions |
| 🔍 Logic | Potential bugs, logic issues |

## Guardrails

The system enforces these guardrails:

1. **No Auto-Merge**: Agents cannot merge PRs
2. **Restricted Files**: Extra review for sensitive files
3. **Audit Logging**: All actions are logged
4. **Human Review Required**: Automated review supplements human review

### Restricted File Patterns

The following files trigger additional review:
- `.github/workflows/*`
- `*secrets*`, `*credentials*`
- `application-prod.*`
- `.env` files

## PR Template

The automated system uses the PR template at `.github/PULL_REQUEST_TEMPLATE.md` which includes:

- Summary and ticket reference
- Type of change selection
- Affected modules checklist
- Testing summary
- Risk assessment
- Deployment notes
- Validation checklist

## Logging

All AMP operations are logged:

- **PR Preparation Log**: `amp-pr-prepare-log.txt`
- **PR Review Log**: `amp-review-log.txt`

Logs include:
- Timestamp
- Operation performed
- Files analyzed
- Issues found
- Actions taken

## Customization

### Adding Reviewers

Edit `scripts/amp-pr-prepare.sh` to customize reviewer assignment:

```bash
get_reviewers() {
    # Add your team leads here
    if echo "$CHANGED_FILES" | grep -q "punchout-gateway"; then
        reviewers="$reviewers your-team-lead"
    fi
    echo "$reviewers"
}
```

### Adding Custom Checks

Edit `scripts/amp-pr-review.sh` to add custom code analysis:

```bash
analyze_java_file() {
    # Add your custom checks here
    if echo "$content" | grep -q "your-pattern"; then
        add_file_comment "$file" 1 "Your custom message" "code_quality"
    fi
}
```

## Troubleshooting

### Workflow Not Triggering

1. Check branch naming matches patterns
2. Verify GitHub Actions is enabled
3. Check workflow file syntax

### AMP SDK Not Working

1. Verify `AMP_API_KEY` secret is set (Sourcegraph AMP API key)
2. Check API key has sufficient permissions
3. Review workflow logs for errors
4. Ensure `@sourcegraph/amp-sdk` is installed correctly

### Missing Review Comments

1. Check if file exists in the repository
2. Verify file has `.java` extension
3. Review workflow logs for parsing errors

## Best Practices

1. **Branch Naming**: Follow `type/TICKET-ID-description` format
2. **Commit Messages**: Use conventional commit format
3. **Small PRs**: Keep changes focused for better review
4. **Tests First**: Add tests before implementation
5. **Address Comments**: Resolve automated review issues

## Support

For issues with the AMP PR Automation system:
1. Check this documentation
2. Review workflow logs
3. Contact the platform team

---

*Last Updated: December 2025*
