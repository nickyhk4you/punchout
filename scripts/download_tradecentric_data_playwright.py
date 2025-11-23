#!/usr/bin/env python3
"""
TradeCentric PunchOut Session Data Downloader (Playwright Version)

This script logs into TradeCentric portal, extracts PunchOut session data,
and prepares it for import into MongoDB.

Requirements:
    pip install playwright
    playwright install chromium
"""

import os
import json
from datetime import datetime
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

# Configuration
TRADECENTRIC_URL = "https://portal.tradecentric.com/waters/console"
SESSIONS_URL = "https://portal.tradecentric.com/waters/console/manage/punchout_session"
USERNAME = os.getenv("TRADECENTRIC_USER", "nick.hu")
PASSWORD = os.getenv("TRADECENTRIC_PASSWORD", "TradeCentric123!")
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "../data/tradecentric_import")

class TradeCentricScraper:
    def __init__(self):
        self.playwright = None
        self.browser = None
        self.context = None
        self.page = None
        self.sessions_data = []
        
    def setup_browser(self):
        """Initialize Playwright browser"""
        print("🔧 Setting up Playwright browser...")
        
        try:
            self.playwright = sync_playwright().start()
            
            # Launch browser (headless=False to see the browser, True for headless)
            self.browser = self.playwright.chromium.launch(
                headless=False,  # Set to True for headless mode
                args=[
                    '--disable-blink-features=AutomationControlled',
                ]
            )
            
            # Create context with custom user agent
            self.context = self.browser.new_context(
                viewport={'width': 1920, 'height': 1080},
                user_agent='Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
            )
            
            # Create page
            self.page = self.context.new_page()
            
            # Set default timeout
            self.page.set_default_timeout(30000)  # 30 seconds
            
            print("✅ Browser ready")
            print(f"   Playwright version: {self.playwright}")
            
        except Exception as e:
            print(f"❌ Browser setup failed: {e}")
            raise
    
    def login(self):
        """Login to TradeCentric portal"""
        print(f"🔐 Logging in to {TRADECENTRIC_URL}...")
        
        try:
            # Navigate to login page
            self.page.goto(TRADECENTRIC_URL)
            
            # Fill in login form
            self.page.fill('input[name="username"]', USERNAME)
            self.page.fill('input[name="password"]', PASSWORD)
            
            # Click login button and wait for navigation
            self.page.click('button[type="submit"], input[type="submit"]')
            self.page.wait_for_load_state('networkidle')
            
            print(f"✅ Logged in successfully")
            print(f"   Current URL: {self.page.url}")
            
        except Exception as e:
            print(f"❌ Login failed: {e}")
            raise
    
    def navigate_to_sessions(self):
        """Navigate to PunchOut sessions page"""
        print(f"📋 Navigating to sessions page...")
        
        try:
            self.page.goto(SESSIONS_URL)
            
            # Wait for table to load
            self.page.wait_for_selector('table, .session-list, [role="grid"]', timeout=10000)
            
            print(f"✅ At sessions page: {self.page.url}")
            
        except Exception as e:
            print(f"❌ Navigation failed: {e}")
            raise
    
    def get_session_list(self):
        """Get list of all PunchOut sessions (waits for stable row count)"""
        print("🔍 Finding PunchOut sessions...")
        
        try:
            # Wait for table to load
            self.page.wait_for_selector('table, .session-list, [role="grid"]', timeout=20000)
            
            # Wait for rows to stabilize
            selector = 'table tbody tr, .session-row'
            last_count = -1
            stable_checks = 0
            
            for _ in range(30):  # Max 9 seconds (30 * 300ms)
                elements = self.page.query_selector_all(selector)
                count = len(elements)
                
                if count == last_count and count > 0:
                    stable_checks += 1
                    if stable_checks >= 2:
                        break
                else:
                    stable_checks = 0
                
                last_count = count
                self.page.wait_for_timeout(300)
            
            session_rows = self.page.query_selector_all(selector)
            
            print(f"✅ Found {len(session_rows)} sessions")
            return session_rows
            
        except Exception as e:
            print(f"❌ Error finding sessions: {e}")
            return []
    
    def extract_session_data(self, session_index, total):
        """Extract data from a single session"""
        print(f"\n📦 Processing session {session_index + 1}/{total}...")
        
        cxml_data = ""
        json_data = ""
        
        try:
            # Re-get the session row (to avoid stale references)
            session_rows = self.page.query_selector_all('table tbody tr, .session-row')
            if session_index >= len(session_rows):
                print(f"   ⚠️  Session index {session_index} out of range")
                return
            
            session_row = session_rows[session_index]
            
            # Extract Route Name
            route_name = session_row.query_selector('td:nth-child(1), .route-name').inner_text()
            print(f"   Route Name: {route_name}")
            
            # Extract Environment
            environment = "Unknown"
            if '[' in route_name and ']' in route_name:
                environment = route_name[route_name.find('[')+1:route_name.find(']')]
            print(f"   Environment: {environment}")
            
            # Extract session ID
            try:
                session_id = session_row.query_selector('td:nth-child(2), .session-id').inner_text()
            except:
                session_id = f"session_{session_index+1}"
            print(f"   Session ID: {session_id if session_id else f'session_{session_index+1}'}")
            
            # Click to open session details
            detail_link = session_row.query_selector('a, button')
            detail_link.click()
            
            # Wait for and click "Requests" button
            print(f"   🔍 Looking for Requests button...")
            requests_button = self.page.wait_for_selector("//button[contains(text(), 'Request')]", timeout=10000)
            requests_button.click()
            
            # Wait for requests to load
            self.page.wait_for_selector("//tr[contains(., 'gateway')]", timeout=10000)
            
            # Step 1: Extract cXML from catalog request
            print(f"   🔍 Looking for catalog request...")
            try:
                # Find catalog row
                catalog_row = self.page.query_selector("//tr[contains(., 'gateway/punchout/request/catalog')]")
                
                if catalog_row:
                    # Find and click the 'open' link
                    open_link = catalog_row.query_selector(".//td[@class='open']//a[contains(text(), 'open')]")
                    
                    print(f"   📂 Clicking 'open' for catalog request...")
                    open_link.click()
                    
                    # Extract cXML
                    cxml_data = self.extract_data_body()
                    print(f"   📄 Extracted cXML ({len(cxml_data)} chars)")
                    
                    # Close popup
                    self.close_popup()
                    
            except Exception as e:
                print(f"   ⚠️  Could not extract cXML: {e}")
            
            # Step 2: Extract JSON from Waters API
            print(f"   🔍 Looking for ext-waters-punchout-exp-api...")
            try:
                # Try multiple patterns
                api_row = None
                for pattern in ['ext-waters-punchout-exp-api', 'api.waters.com:443/p2/ext-waters', 'punchout/setup']:
                    try:
                        api_row = self.page.query_selector(f"//tr[contains(., '{pattern}')]")
                        if api_row:
                            print(f"   ✅ Found row with pattern: {pattern}")
                            break
                    except:
                        continue
                
                if api_row:
                    # Find and click the 'open' link
                    open_link = api_row.query_selector(".//td[@class='open']//a[contains(text(), 'open')]")
                    
                    print(f"   📂 Clicking 'open' for Waters API request...")
                    open_link.click()
                    
                    # Extract JSON
                    json_data = self.extract_data_body()
                    print(f"   📄 Extracted JSON ({len(json_data)} chars)")
                    
                    # Close popup
                    self.close_popup()
                else:
                    raise Exception("Could not find Waters API row")
                    
            except Exception as e:
                print(f"   ⚠️  Could not extract JSON: {e}")
            
            # Save session data
            if cxml_data or json_data:
                session_data = {
                    'routeName': route_name,
                    'environment': environment,
                    'sessionId': session_id if session_id else f"session_{session_index+1}",
                    'cxml': cxml_data,
                    'json': json_data,
                    'extractedAt': datetime.now().isoformat()
                }
                
                self.sessions_data.append(session_data)
                print(f"   ✅ Session data saved (cXML: {len(cxml_data)} chars, JSON: {len(json_data)} chars)")
            else:
                print(f"   ⚠️  No data extracted for this session")
            
            # Navigate back to sessions list
            self.page.goto(SESSIONS_URL)
            self.page.wait_for_selector('table tbody tr, .session-row', timeout=10000)
            
        except Exception as e:
            print(f"   ❌ Error processing session: {e}")
            
            # Try to recover
            try:
                print(f"   🔄 Recovering - navigating back to sessions list...")
                self.page.goto(SESSIONS_URL)
                self.page.wait_for_selector('table tbody tr, .session-row', timeout=10000)
                print(f"   ✅ Recovered, continuing with next session")
            except Exception as recover_error:
                print(f"   ❌ Could not recover: {recover_error}")
                raise
    
    def extract_data_body(self, body_type='request'):
        """Extract Data body content from popup modal
        
        Args:
            body_type: 'request' for request body (li[10]) or 'response' for response body (li[11])
        """
        try:
            # Wait for modal to appear
            self.page.wait_for_selector("//div[contains(@class, 'modal')]", timeout=10000)
            
            # Use the exact XPath
            if body_type == 'request':
                xpath = "/html/body/div[5]/div/div/div[2]/div/div/div[1]/form/ul/li[10]/div/div[4]/div/div/pre/code"
            else:
                xpath = "/html/body/div[5]/div/div/div[2]/div/div/div[1]/form/ul/li[11]/div/div[4]/div/div/pre/code"
            
            try:
                element = self.page.wait_for_selector(xpath, timeout=10000)
                data = element.inner_text() or element.text_content() or ""
                
                if data and len(data) > 10:
                    return data
            except:
                # Fallback selectors
                fallback_selectors = [
                    "//li[10]//pre//code" if body_type == 'request' else "//li[11]//pre//code",
                    "//div[contains(text(), 'Request')]//following::pre[1]//code",
                    "pre code",
                ]
                
                for selector in fallback_selectors:
                    try:
                        element = self.page.query_selector(selector)
                        if element:
                            data = element.inner_text() or element.text_content() or ""
                            if data and len(data) > 10:
                                return data
                    except:
                        continue
            
            return ""
            
        except Exception as e:
            print(f"      ⚠️  Could not extract {body_type} body: {e}")
            return ""
    
    def close_popup(self):
        """Close the current popup/modal"""
        close_selectors = [
            "button.close, .modal-close, [aria-label='Close']",
            "//button[contains(text(), 'Close')]",
            "//button[contains(text(), '×')]",
            ".modal-header button",
        ]
        
        for selector in close_selectors:
            try:
                element = self.page.query_selector(selector)
                if element:
                    element.click()
                    return
            except:
                continue
        
        # If no close button found, press ESC
        try:
            self.page.keyboard.press('Escape')
        except:
            pass
    
    def save_data(self):
        """Save extracted data to files"""
        print(f"\n💾 Saving data...")
        
        # Create output directory
        os.makedirs(OUTPUT_DIR, exist_ok=True)
        
        # Save as JSON
        output_file = os.path.join(OUTPUT_DIR, f"tradecentric_data_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json")
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(self.sessions_data, f, indent=2, ensure_ascii=False)
        
        print(f"✅ Saved {len(self.sessions_data)} sessions to: {output_file}")
        
        # Save individual cXML and JSON files
        for i, session in enumerate(self.sessions_data):
            session_dir = os.path.join(OUTPUT_DIR, f"session_{i+1}_{session['sessionId']}")
            os.makedirs(session_dir, exist_ok=True)
            
            # Save cXML
            if session['cxml']:
                cxml_file = os.path.join(session_dir, "input.cxml")
                with open(cxml_file, 'w', encoding='utf-8') as f:
                    f.write(session['cxml'])
            
            # Save JSON
            if session['json']:
                json_file = os.path.join(session_dir, "output.json")
                with open(json_file, 'w', encoding='utf-8') as f:
                    f.write(session['json'])
            
            # Save metadata
            metadata_file = os.path.join(session_dir, "metadata.json")
            with open(metadata_file, 'w', encoding='utf-8') as f:
                json.dump({
                    'routeName': session['routeName'],
                    'environment': session.get('environment', 'Unknown'),
                    'sessionId': session['sessionId'],
                    'extractedAt': session['extractedAt']
                }, f, indent=2)
        
        print(f"✅ Saved individual files to: {OUTPUT_DIR}")
    
    def print_summary(self):
        """Print summary of extracted data"""
        print("\n" + "="*60)
        print("📊 EXTRACTION SUMMARY")
        print("="*60)
        
        for i, session in enumerate(self.sessions_data):
            print(f"\n{i+1}. Route: {session['routeName']}")
            print(f"   Session ID: {session['sessionId']}")
            print(f"   cXML: {len(session['cxml'])} characters")
            print(f"   JSON: {len(session['json'])} characters")
            
            # Try to pretty-print JSON if valid
            if session['json']:
                try:
                    parsed_json = json.loads(session['json'])
                    print(f"   JSON Structure:")
                    for key in list(parsed_json.keys())[:10]:  # First 10 keys
                        print(f"      - {key}")
                except:
                    print(f"   JSON: (not parseable)")
        
        print(f"\n📁 Total sessions extracted: {len(self.sessions_data)}")
        print("="*60)
    
    def run(self):
        """Main execution flow"""
        try:
            self.setup_browser()
            self.login()
            self.navigate_to_sessions()
            
            session_rows = self.get_session_list()
            
            if not session_rows:
                print("⚠️  No sessions found. Check selectors.")
                return
            
            total = len(session_rows)
            print(f"\n🔄 Processing {total} sessions...")
            
            for i in range(total):
                # Retry fetching until we have enough rows
                attempts = 0
                while attempts < 20:
                    current_rows = self.get_session_list()
                    if i < len(current_rows):
                        break
                    self.page.wait_for_timeout(500)
                    attempts += 1
                
                current_rows = self.get_session_list()
                if i >= len(current_rows):
                    print(f"\n⚠️  Rows not fully loaded for index {i}. Skipping.")
                    continue
                
                self.extract_session_data(i, total)
                
                # Optional: Limit for testing (uncomment to limit)
                # if i >= 2:
                #     print(f"\n⚠️  Stopping at {i+1} sessions (test mode)")
                #     break
            
            self.save_data()
            self.print_summary()
            
        except Exception as e:
            print(f"\n❌ Error during execution: {e}")
        
        finally:
            if self.context:
                print("\n🔒 Closing browser...")
                self.context.close()
            if self.browser:
                self.browser.close()
            if self.playwright:
                self.playwright.stop()
            print("✅ Done")


def main():
    print("="*60)
    print("🚀 TradeCentric Data Downloader (Playwright)")
    print("="*60)
    print(f"Username: {USERNAME}")
    print(f"Password: {'*' * len(PASSWORD)}")
    print(f"Output: {OUTPUT_DIR}")
    print("="*60)
    print()
    
    scraper = TradeCentricScraper()
    scraper.run()


if __name__ == "__main__":
    main()
