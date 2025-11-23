# TradeCentric Data Downloader

## Setup

### 1. Install Python Dependencies
```bash
cd scripts
pip install -r requirements.txt
```

### 2. Run the Script
```bash
python download_tradecentric_data.py
```

## Configuration

### Environment Variables (Optional)
```bash
export TRADECENTRIC_USER="nick.hu"
export TRADECENTRIC_PASSWORD="your-password"
```

Or edit the script directly.

## Output

Data will be saved to:
```
data/tradecentric_import/
├── tradecentric_data_20251116_223000.json  # All data
├── session_1_SESSION123/
│   ├── input.cxml      # cXML request
│   ├── output.json     # JSON response
│   └── metadata.json   # Route name, session ID, etc.
├── session_2_SESSION456/
│   ├── input.cxml
│   ├── output.json
│   └── metadata.json
...
```

## Next Steps

After downloading, use the import script to load into MongoDB:
```bash
python import_to_mongodb.py
```

## Troubleshooting

### If login fails:
- Check credentials
- Check if website structure changed
- Look at screenshots in `data/tradecentric_import/`

### If selectors don't work:
- Inspect the TradeCentric website
- Update CSS selectors in the script
- Use browser dev tools to find correct selectors

### Headless mode:
Uncomment line in `setup_driver()`:
```python
chrome_options.add_argument("--headless")
```
