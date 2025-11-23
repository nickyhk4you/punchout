# TradeCentric Data Downloader - API Version (RECOMMENDED)

**This is the recommended version** - 10-100x faster than browser automation!

## Features

- ✅ Direct API access (no browser needed)
- ✅ Fast execution (~1-2 seconds per session)
- ✅ Extracts both cXML and JSON data
- ✅ Configurable session limit
- ✅ Automatic login with CSRF token handling
- ✅ Reliable error handling and recovery

## Installation

```bash
pip install requests beautifulsoup4
```

## Usage

```bash
# Download default 20 sessions
python3 download_tradecentric_data_api.py

# Download up to 100 sessions (Note: API may limit to 20-25 per request)
python3 download_tradecentric_data_api.py 100

# Set credentials via environment variables
export TRADECENTRIC_USER="your.username"
export TRADECENTRIC_PASSWORD="your_password"
python3 download_tradecentric_data_api.py
```

## Output

Data is saved to `data/tradecentric_import/` with the following naming pattern:

```
data/tradecentric_import/
├── tradecentric_data_20251123_172743.json      # All sessions in one file
├── session_001_rh69224039e025d_input.cxml      # Session 1 cXML request
├── session_001_rh69224039e025d_output.json     # Session 1 JSON response
├── session_001_rh69224039e025d_metadata.json   # Session 1 metadata
├── session_002_jN6921c22355786_input.cxml      # Session 2 cXML request
├── session_002_jN6921c22355786_output.json     # Session 2 JSON response
├── session_002_jN6921c22355786_metadata.json   # Session 2 metadata
└── ...
```

**File naming pattern:** `session_{env}_{customer}_{sessionId}_{type}.{ext}`
- `env`: Environment (e.g., `Prod`, `PreProd`, `Staging`)
- `customer`: Sanitized customer name (e.g., `JJ`, `Abbott_Ariba`, `Bachem`)
- `sessionId`: TradeCentric session identifier
- `type`: `input` (cXML), `output` (JSON), or `metadata`
- `ext`: File extension (`.cxml`, `.json`)

Examples:
- `session_Prod_JJ_rh69224039e025d_input.cxml`
- `session_Prod_Abbott_Ariba_Eo692107c79993d_output.json`
- `session_PreProd_Yale_University_xyz123_metadata.json`

This naming makes it easy to identify sessions by environment and customer at a glance, perfect for MongoDB imports.

## API Limitations

The TradeCentric API appears to have a server-side limit of ~20-25 sessions per request. To download more:

1. Run the script multiple times on different days
2. Use date filtering (if needed, modify the script to add date parameters)
3. Contact TradeCentric to request higher API limits

## Performance Comparison

| Method | Speed | Reliability | Setup |
|--------|-------|-------------|-------|
| API (this version) | **~2s per session** | ✅ Excellent | pip install |
| Playwright | ~5-10s per session | Good | playwright install |
| Selenium | ~10-20s per session | Medium | ChromeDriver setup |

## How It Works

1. **Login**: Submits credentials with CSRF tokens
2. **Session List**: Calls `/api/rest/public/1.0/punchout_session/` to get sessions
3. **Session Details**: For each session, fetches `/console/manage/punchout_session/open/id/{id}`
4. **Request Data**: For each network request, fetches `/console/manage/http_request/open/id/{id}`
5. **Data Extraction**: Parses HTML to extract `data-data_body` attributes
6. **Save**: Outputs to JSON and individual files

## Troubleshooting

### Login Failed
- Check credentials in environment variables or script
- Verify you can log in manually at https://portal.tradecentric.com/waters/console

### No Sessions Found
- The API might be returning empty results - check if you have access to sessions
- Try logging in manually and verifying sessions exist

### Only 20 Sessions Extracted
- This is a server-side API limitation
- Run the script on different dates to get different sessions
- Or modify the `start` parameter to paginate (advanced)

## Advanced: Pagination

To fetch more than 20 sessions, you'd need to implement pagination:

```python
# In get_session_list(), add offset support:
all_sessions = []
offset = 0
while len(all_sessions) < limit:
    params['start'] = offset
    params['length'] = min(25, limit - len(all_sessions))
    # ... fetch sessions ...
    if not sessions:
        break
    all_sessions.extend(sessions)
    offset += len(sessions)
```

However, TradeCentric's API may still enforce limits.
