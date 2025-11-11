# How to Upload Documentation to Confluence

## Quick Method (Copy-Paste)

### Step 1: Open Confluence Page
1. Go to: https://waterscorporation.atlassian.net/wiki/spaces/BTADP/pages/21891448937/eProcument
2. Click **"+"** to create a child page
3. Title: **"Waters PunchOut Platform - Architecture"**

### Step 2: Copy Content
1. Open `PUNCHOUT_ARCHITECTURE.md` in a text editor
2. Select all content (Cmd+A)
3. Copy (Cmd+C)

### Step 3: Paste into Confluence
1. In Confluence editor, paste the markdown
2. Confluence will auto-convert most formatting
3. Click **Publish**

---

## Better Method (Using Confluence Import)

### Step 1: Install Confluence Markdown Importer
```bash
npm install -g confluence-markdown-sync
```

### Step 2: Create Config File
Create `confluence-config.json`:
```json
{
  "baseUrl": "https://waterscorporation.atlassian.net/wiki",
  "user": "your.email@waters.com",
  "pass": "ATATT3xFfGF0dMT4...",
  "space": "BTADP",
  "parentId": "21891448937"
}
```

### Step 3: Upload
```bash
confluence-markdown-sync \
  --config confluence-config.json \
  --file PUNCHOUT_ARCHITECTURE.md \
  --title "Waters PunchOut Platform - Architecture"
```

---

## Alternative: Use Pandoc

### Step 1: Install Pandoc
```bash
brew install pandoc
```

### Step 2: Convert to Confluence Wiki Format
```bash
pandoc PUNCHOUT_ARCHITECTURE.md \
  -f markdown \
  -t confluence \
  -o confluence-format.txt
```

### Step 3: Copy & Paste
1. Open `confluence-format.txt`
2. Copy content
3. Paste into Confluence (Wiki Markup editor)
4. Publish

---

## Recommended Documents to Upload

### Priority 1 (Must Have)
1. **PUNCHOUT_ARCHITECTURE.md** - Overall system architecture
2. **PROJECT_EXPLANATION.md** - Complete project explanation
3. **README.md** - Main documentation

### Priority 2 (Important)
4. **FLEXIBLE_CXML_CONVERSION_ARCHITECTURE.md** - Conversion engine
5. **PROCUREMENT_PLATFORM_CONVERTERS.md** - Platform support
6. **JWT_TOKEN_IMPLEMENTATION.md** - Authentication

### Priority 3 (Reference)
7. **DEPLOYMENT_GUIDE.md** - Deployment instructions
8. **TESTING_GUIDE.md** - Testing procedures
9. **CODE_OPTIMIZATION_SUMMARY.md** - Code improvements

---

## Manual Steps (Simplest)

Since the API token doesn't have permissions, here's the manual process:

### 1. Create Parent Page (if needed)
- Go to eProcument page
- Click "+" or "Create"
- Title: "Waters PunchOut Platform"
- Add intro text

### 2. Create Child Page for Architecture
- Under "Waters PunchOut Platform", click "+"
- Title: "System Architecture"
- Switch to "Markdown" or paste directly

### 3. Copy-Paste Content
```bash
# View the file
cat PUNCHOUT_ARCHITECTURE.md

# Copy from terminal or open in VS Code
code PUNCHOUT_ARCHITECTURE.md
```

### 4. Format in Confluence
- Confluence auto-converts:
  - `# Header` → H1
  - `## Header` → H2
  - `**bold**` → Bold
  - `` `code` `` → Inline code
  - \`\`\`code blocks\`\`\` → Code blocks
  - `- bullets` → Bullet lists
  - `| tables |` → Tables

### 5. Add Diagrams (Optional)
- Use Confluence's built-in diagram tool
- Or upload as images
- Or use draw.io integration

---

## Quick Upload Script (If You Get Permissions)

Create `upload-to-confluence.sh`:

```bash
#!/bin/bash

CONFLUENCE_URL="https://waterscorporation.atlassian.net/wiki"
TOKEN="YOUR_TOKEN_HERE"
SPACE="BTADP"
PARENT_ID="21891448937"

# Convert markdown to HTML
pandoc "$1" -f markdown -t html -o /tmp/confluence.html

# Read HTML
CONTENT=$(cat /tmp/confluence.html | jq -Rs .)

# Upload
curl -X POST "$CONFLUENCE_URL/rest/api/content" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"type\": \"page\",
    \"title\": \"$2\",
    \"space\": {\"key\": \"$SPACE\"},
    \"ancestors\": [{\"id\": \"$PARENT_ID\"}],
    \"body\": {
      \"storage\": {
        \"value\": $CONTENT,
        \"representation\": \"storage\"
      }
    }
  }"
```

Usage:
```bash
./upload-to-confluence.sh PUNCHOUT_ARCHITECTURE.md "System Architecture"
```

---

## Summary

**Recommended Approach:**
1. Open Confluence page in browser
2. Create child page: "Waters PunchOut Platform - Architecture"
3. Copy content from `PUNCHOUT_ARCHITECTURE.md`
4. Paste into Confluence editor
5. Review formatting
6. Publish

**Time: 2-3 minutes per document**

All markdown files are in the project root, ready to upload! 📄
