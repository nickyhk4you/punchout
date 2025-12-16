# Create PR Skill

This skill enables creating GitHub Pull Requests directly from the Amp prompt.

## Usage

Type one of the following commands:
- `create-pr` - Create PR for the current branch
- `create-pr <JIRA-ID>` - Create PR for branch containing JIRA ID (e.g., `create-pr JIRA-829283`)
- `create-pr <branch-name>` - Create PR for a specific branch

## Examples

```
create-pr
create-pr JIRA-829283
create-pr feature/user-management
```

## Instructions for Amp

When the user requests to create a PR, follow these steps:

### Step 1: Determine the Branch

1. If no argument provided, use current branch:
   ```bash
   git branch --show-current
   ```

2. If JIRA ID provided (pattern like `JIRA-12345`, `PROJ-123`), find matching branch:
   ```bash
   git branch -a | grep -i "<JIRA-ID>"
   ```

3. If branch name provided, verify it exists:
   ```bash
   git branch -a | grep "<branch-name>"
   ```

### Step 2: Check Branch Status

Ensure the branch has commits and is pushed:

```bash
# Check if branch exists remotely
git ls-remote --heads origin <branch-name>

# If not pushed, push it
git push -u origin <branch-name>

# Get commit count ahead of target
git rev-list --count origin/develop..<branch-name>
```

### Step 3: Get Changed Files

```bash
# List changed files compared to develop
git diff --name-only origin/develop...<branch-name>

# Get diff summary
git diff --stat origin/develop...<branch-name>
```

### Step 4: Generate PR Title and Description

Based on the branch name and changes:

**Title Format:**
- If JIRA ID found: `[JIRA-123] feat: description from branch name`
- Otherwise: `feat: description from branch name`

**Determine type from branch prefix:**
- `feature/` → `feat:`
- `bugfix/` or `fix/` → `fix:`
- `hotfix/` → `hotfix:`
- `chore/` → `chore:`
- `refactor/` → `refactor:`

**Description should include:**
- Summary of changes
- List of modified files by module
- Testing notes
- JIRA link if applicable

### Step 5: Determine Target Branch

Based on branch type:
- `hotfix/*` → `main`
- Everything else → `develop`

### Step 6: Create the PR

Use GitHub CLI:

```bash
gh pr create \
  --title "<generated-title>" \
  --body "<generated-body>" \
  --base <target-branch> \
  --head <source-branch>
```

Or with labels and reviewers:

```bash
gh pr create \
  --title "<title>" \
  --body "<body>" \
  --base develop \
  --head <branch> \
  --label "needs-review" \
  --reviewer nickyhk4you
```

### Step 7: Report to User

Show the user:
- PR URL
- Title
- Target branch
- Number of files changed
- Next steps (review, CI status)

## PR Description Template

```markdown
## Summary

Brief description of what this PR does.

## JIRA Ticket

[JIRA-XXXXX](https://jira.example.com/browse/JIRA-XXXXX)

## Changes

### Modified Modules
- `punchout-gateway`: Description of changes
- `punchout-ui-backend`: Description of changes

### Files Changed
- `path/to/file1.java` - Added new endpoint
- `path/to/file2.java` - Fixed bug in validation

## Type of Change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to change)
- [ ] Documentation update

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated if needed
- [ ] No secrets or credentials committed

---
*PR created by Amp*
```

## Example Workflow

User: `create-pr JIRA-829283`

Amp will:
1. Find branch: `feature/JIRA-829283-user-management`
2. Check it's pushed to origin
3. Get changed files (e.g., 5 files in punchout-ui-backend)
4. Generate title: `[JIRA-829283] feat: user management`
5. Generate description with file list and JIRA link
6. Create PR targeting `develop`
7. Return PR URL to user

## Error Handling

- **Branch not found**: "No branch found matching 'JIRA-829283'. Available branches: ..."
- **Not pushed**: "Branch exists locally but not on remote. Push with: git push -u origin <branch>"
- **No changes**: "Branch has no changes compared to develop."
- **PR exists**: "PR already exists for this branch: <URL>"
