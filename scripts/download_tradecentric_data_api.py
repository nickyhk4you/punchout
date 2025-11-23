#!/usr/bin/env python3
"""
TradeCentric PunchOut Session Data Downloader (API Version)

This script uses the TradeCentric REST API directly instead of browser automation.
Much faster and more reliable than Selenium/Playwright.

Requirements:
    pip install requests beautifulsoup4
"""

import os
import json
import re
from datetime import datetime
from urllib.parse import urlencode, parse_qs, urlparse
import requests
from bs4 import BeautifulSoup

# Configuration
BASE_URL = "https://portal.tradecentric.com"
REALM = "waters"
LOGIN_URL = f"{BASE_URL}/{REALM}/console/login"
SESSION_API_URL = f"{BASE_URL}/api/rest/public/1.0/punchout_session/"
USERNAME = os.getenv("TRADECENTRIC_USER", "nick.hu")
PASSWORD = os.getenv("TRADECENTRIC_PASSWORD", "TradeCentric123!")
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "../data/tradecentric_import")

class TradeCentricAPIClient:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
            'Accept': 'application/json, text/javascript, */*; q=0.01',
            'Accept-Language': 'en-US,en;q=0.9',
        })
        self.sessions_data = []
        self.csrf_token = None
        
    def login(self):
        """Login to TradeCentric portal"""
        print(f"🔐 Logging in as {USERNAME}...")
        
        try:
            # First, get the login page to get any CSRF tokens
            login_page = self.session.get(LOGIN_URL)
            
            # Parse login page to find any hidden fields or tokens
            soup = BeautifulSoup(login_page.text, 'html.parser')
            login_form = soup.find('form')
            
            # Build login data with all form fields
            login_data = {
                'username': USERNAME,
                'password': PASSWORD,
            }
            
            # Add any hidden input fields
            if login_form:
                for hidden in login_form.find_all('input', type='hidden'):
                    field_name = hidden.get('name')
                    field_value = hidden.get('value', '')
                    if field_name:
                        login_data[field_name] = field_value
                        print(f"   Found hidden field: {field_name} = {field_value}")
                
                # Check form action
                form_action = login_form.get('action')
                if form_action:
                    print(f"   Form action: {form_action}")
                    # If form action is relative, make it absolute
                    if form_action and not form_action.startswith('http'):
                        # Remove leading slash and /console prefix if present
                        clean_action = form_action.lstrip('/')
                        if clean_action.startswith('console/'):
                            clean_action = clean_action[8:]  # Remove 'console/'
                        submit_url = f"{BASE_URL}/{REALM}/console/{clean_action}"
                    else:
                        submit_url = form_action or LOGIN_URL
                else:
                    submit_url = LOGIN_URL
                
                # Check for submit button
                submit_btn = login_form.find('input', type='submit') or login_form.find('button', type='submit')
                if submit_btn and submit_btn.get('name'):
                    login_data[submit_btn.get('name')] = submit_btn.get('value', '')
                    print(f"   Found submit button: {submit_btn.get('name')}")
            else:
                submit_url = LOGIN_URL
            
            print(f"   Submitting login to: {submit_url}")
            print(f"   Login data keys: {list(login_data.keys())}")
            
            # Submit login
            response = self.session.post(submit_url, data=login_data, allow_redirects=True)
            
            print(f"   Login response status: {response.status_code}")
            print(f"   Login response URL: {response.url}")
            print(f"   Cookies after login: {list(self.session.cookies.keys())}")
            
            # Check if login succeeded - should redirect away from /login page
            if response.status_code == 200 and 'console' in response.url and '/login' not in response.url:
                print(f"✅ Logged in successfully")
                
                # Visit the sessions page to establish context
                sessions_page_url = f"{BASE_URL}/{REALM}/console/manage/punchout_session"
                print(f"📋 Visiting sessions page to establish session context...")
                page_response = self.session.get(sessions_page_url)
                print(f"   Sessions page status: {page_response.status_code}")
                
                # Extract CSRF token if present
                soup = BeautifulSoup(page_response.text, 'html.parser')
                csrf_input = soup.find('input', {'name': 'csrf_token'}) or soup.find('meta', {'name': 'csrf-token'})
                if csrf_input:
                    self.csrf_token = csrf_input.get('value') or csrf_input.get('content')
                    print(f"   🔑 CSRF token found: {self.csrf_token[:20]}...")
                else:
                    print(f"   ⚠️  No CSRF token found")
                
                # Print cookies for debugging
                print(f"   🍪 Cookies after visiting page: {list(self.session.cookies.keys())}")
                for cookie_name, cookie_value in self.session.cookies.items():
                    print(f"      - {cookie_name}: {cookie_value[:50] if len(cookie_value) > 50 else cookie_value}...")
                
                return True
            else:
                print(f"❌ Login failed")
                print(f"   Response URL indicates login failure: {response.url}")
                # Print part of response to see error message
                if 'login' in response.url:
                    soup = BeautifulSoup(response.text, 'html.parser')
                    # Look for error messages
                    error_div = soup.find('div', class_=re.compile('error|alert|danger|warning', re.I))
                    if error_div:
                        print(f"   Error message: {error_div.get_text(strip=True)}")
                    
                    # Also check for any message with "invalid" or "incorrect"
                    for elem in soup.find_all(['div', 'span', 'p']):
                        text = elem.get_text(strip=True).lower()
                        if any(word in text for word in ['invalid', 'incorrect', 'failed', 'error', 'wrong']):
                            print(f"   Found message: {elem.get_text(strip=True)}")
                            break
                return False
                
        except Exception as e:
            print(f"❌ Login failed: {e}")
            return False
    
    def get_session_list(self, limit=100):
        """Get list of PunchOut sessions via API"""
        print("🔍 Fetching session list from API...")
        
        try:
            # Build API parameters (simplified from the long URL you provided)
            params = {
                'realm': REALM,
                'draw': 1,
                'start': 0,
                'length': limit,
                'search[value]': '',
                'search[regex]': 'false',
                '_': int(datetime.now().timestamp() * 1000)
            }
            
            # Add column definitions
            columns = ['session_key', 'punchin', 'operation', 'contact', 'catalog', 
                      'environment', 'doc_flag', 'datemark', 'actions']
            for i, col in enumerate(columns):
                params[f'columns[{i}][data]'] = col
                params[f'columns[{i}][name]'] = ''
                params[f'columns[{i}][searchable]'] = 'true'
                params[f'columns[{i}][orderable]'] = 'true' if col not in ['contact', 'doc_flag', 'actions'] else 'false'
                params[f'columns[{i}][search][value]'] = ''
                params[f'columns[{i}][search][regex]'] = 'false'
            
            # Add X-Requested-With header for AJAX requests
            headers = {
                'X-Requested-With': 'XMLHttpRequest',
                'Referer': f'{BASE_URL}/{REALM}/console/manage/punchout_session'
            }
            
            print(f"   Calling API: {SESSION_API_URL}")
            response = self.session.get(SESSION_API_URL, params=params, headers=headers)
            
            if response.status_code == 200:
                data = response.json()
                print(f"   API response keys: {list(data.keys())}")
                print(f"   recordsTotal: {data.get('recordsTotal', 'N/A')}")
                print(f"   recordsFiltered: {data.get('recordsFiltered', 'N/A')}")
                # Try both 'items' and 'data' keys
                sessions = data.get('items', data.get('data', []))
                print(f"✅ Found {len(sessions)} sessions")
                if len(sessions) > 0:
                    print(f"   First session keys: {list(sessions[0].keys())}")
                return sessions
            else:
                print(f"❌ Failed to get sessions: {response.status_code}")
                print(f"   Response: {response.text[:500]}")
                return []
                
        except Exception as e:
            print(f"❌ Error fetching sessions: {e}")
            import traceback
            traceback.print_exc()
            return []
    
    def get_session_details(self, session_id):
        """Get detailed information about a session including network requests"""
        print(f"   📋 Fetching details for session {session_id}...")
        
        try:
            url = f"{BASE_URL}/{REALM}/console/manage/punchout_session/open/id/{session_id}"
            response = self.session.get(url)
            
            if response.status_code != 200:
                print(f"   ❌ Failed to get session details: {response.status_code}")
                return []
            
            # Parse HTML to extract request IDs
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # Find all request links
            requests_list = []
            for link in soup.find_all('a', href=re.compile(r'http_request/open/id/')):
                href = link.get('href')
                # Extract the JavaScript call: Basic.BSModal.open("/waters/console/manage/http_request/open/id/722385157...")
                match = re.search(r'http_request/open/id/(\d+)', href)
                if match:
                    request_id = match.group(1)
                    # Find the URI in the same row
                    row = link.find_parent('tr')
                    if row:
                        uri_cell = row.find_all('td')[2] if len(row.find_all('td')) > 2 else None
                        uri = uri_cell.get_text(strip=True) if uri_cell else ''
                        requests_list.append({
                            'id': request_id,
                            'uri': uri
                        })
            
            print(f"   ✅ Found {len(requests_list)} network requests")
            return requests_list
            
        except Exception as e:
            print(f"   ❌ Error getting session details: {e}")
            return []
    
    def get_request_data(self, request_id, parent_session_id):
        """Get the request/response data for a specific HTTP request"""
        try:
            url = f"{BASE_URL}/{REALM}/console/manage/http_request/open/id/{request_id}"
            params = {
                'parent': 'punchout_session',
                'parent_id': parent_session_id,
                '_': int(datetime.now().timestamp() * 1000)
            }
            
            response = self.session.get(url, params=params)
            
            if response.status_code != 200:
                return None, None
            
            # Parse HTML to find data-data_body attributes
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # Find all divs with data-data_body attribute
            request_body = None
            response_body = None
            
            data_body_divs = soup.find_all('div', {'data-data_body': True})
            for div in data_body_divs:
                data = div.get('data-data_body', '')
                # Check if it's in a request or response section
                parent_text = div.find_parent('li')
                if parent_text:
                    section_text = parent_text.get_text().lower()
                    if 'request' in section_text and not request_body:
                        request_body = data
                    elif 'response' in section_text and not response_body:
                        response_body = data
                elif not request_body:
                    # If we can't determine, assume first is request
                    request_body = data
            
            # Alternative: look in <pre><code> tags
            if not request_body and not response_body:
                code_blocks = soup.find_all('code')
                for i, code in enumerate(code_blocks):
                    text = code.get_text(strip=True)
                    if i == 0 and not request_body:
                        request_body = text
                    elif i == 1 and not response_body:
                        response_body = text
            
            return request_body, response_body
            
        except Exception as e:
            print(f"      ⚠️  Error getting request data: {e}")
            return None, None
    
    def extract_session_data(self, session, index):
        """Extract data from a single session"""
        print(f"\n📦 Processing session {index + 1}...")
        
        try:
            # Get the id field
            session_id = session.get('id') or session.get('session_id') or session.get('DT_RowId', '').replace('row_', '')
            session_key = session.get('session_key', f'session_{index+1}')
            environment = session.get('environment', 'Unknown')
            
            print(f"   Session ID: {session_id}")
            print(f"   Session Key: {session_key}")
            print(f"   Environment: {environment}")
            
            # Get network requests for this session
            requests_list = self.get_session_details(session_id)
            
            cxml_data = ""
            json_data = ""
            
            # Look for specific requests
            for req in requests_list:
                uri = req['uri']
                request_id = req['id']
                
                # Check if this is the catalog request (cXML)
                if 'gateway/punchout/request' in uri or 'punchout/supplier' in uri or 'punchout/start' in uri:
                    print(f"   📂 Found catalog request: {uri}")
                    req_body, resp_body = self.get_request_data(request_id, session_id)
                    # Check both request and response for cXML
                    if req_body and ('cXML' in req_body or '<?xml' in req_body):
                        cxml_data = req_body
                        print(f"   📄 Extracted cXML from request ({len(cxml_data)} chars)")
                    elif resp_body and ('cXML' in resp_body or '<?xml' in resp_body):
                        cxml_data = resp_body
                        print(f"   📄 Extracted cXML from response ({len(cxml_data)} chars)")
                
                # Check if this is the Waters API request (JSON)
                if 'ext-waters-punchout' in uri or 'api.waters.com' in uri or 'punchout/setup' in uri:
                    print(f"   📂 Found Waters API request: {uri}")
                    req_body, resp_body = self.get_request_data(request_id, session_id)
                    if resp_body:
                        try:
                            # Try to parse as JSON
                            json.loads(resp_body)
                            json_data = resp_body
                            print(f"   📄 Extracted JSON ({len(json_data)} chars)")
                        except:
                            # If it's in request, use that
                            if req_body:
                                try:
                                    json.loads(req_body)
                                    json_data = req_body
                                    print(f"   📄 Extracted JSON from request ({len(json_data)} chars)")
                                except:
                                    pass
            
            # Save session data
            if cxml_data or json_data:
                session_data = {
                    'routeName': session.get('catalog', 'Unknown'),
                    'environment': environment,
                    'sessionId': session_key,
                    'sessionKey': session_key,
                    'cxml': cxml_data,
                    'json': json_data,
                    'extractedAt': datetime.now().isoformat()
                }
                
                self.sessions_data.append(session_data)
                print(f"   ✅ Session data saved (cXML: {len(cxml_data)} chars, JSON: {len(json_data)} chars)")
            else:
                print(f"   ⚠️  No data extracted for this session")
                
        except Exception as e:
            print(f"   ❌ Error processing session: {e}")
    
    def save_data(self):
        """Save extracted data to files"""
        print(f"\n💾 Saving data...")
        
        # Create output directory
        os.makedirs(OUTPUT_DIR, exist_ok=True)
        
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        
        # Save as JSON (all sessions in one file)
        output_file = os.path.join(OUTPUT_DIR, f"tradecentric_data_{timestamp}.json")
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(self.sessions_data, f, indent=2, ensure_ascii=False)
        
        print(f"✅ Saved {len(self.sessions_data)} sessions to: {output_file}")
        
        # Save individual cXML and JSON files with naming pattern
        for i, session in enumerate(self.sessions_data):
            # Extract environment and customer name from routeName
            route_name = session['routeName']
            environment = session.get('environment', 'Unknown')
            session_id = session['sessionId']
            
            # Parse routeName like "[Prod] J&J" -> "Prod" and "J&J"
            if '[' in route_name and ']' in route_name:
                env_part = route_name[route_name.find('[')+1:route_name.find(']')]
                customer_part = route_name[route_name.find(']')+1:].strip()
            else:
                env_part = environment
                customer_part = route_name
            
            # Sanitize customer name for filesystem (remove special characters)
            customer_safe = re.sub(r'[^\w\s-]', '', customer_part).strip().replace(' ', '_')
            
            # Build prefix: session_Prod_JJ_{sessionId}
            session_prefix = f"session_{env_part}_{customer_safe}_{session_id}"
            
            # Save cXML
            if session['cxml']:
                cxml_file = os.path.join(OUTPUT_DIR, f"{session_prefix}_input.cxml")
                with open(cxml_file, 'w', encoding='utf-8') as f:
                    f.write(session['cxml'])
            
            # Save JSON
            if session['json']:
                json_file = os.path.join(OUTPUT_DIR, f"{session_prefix}_output.json")
                with open(json_file, 'w', encoding='utf-8') as f:
                    f.write(session['json'])
            
            # Save metadata
            metadata_file = os.path.join(OUTPUT_DIR, f"{session_prefix}_metadata.json")
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
    
    def run(self, limit=25):
        """Main execution flow"""
        try:
            if not self.login():
                return
            
            sessions = self.get_session_list(limit=limit)
            
            if not sessions:
                print("⚠️  No sessions found")
                return
            
            print(f"\n🔄 Processing {len(sessions)} sessions...")
            
            for i, session in enumerate(sessions):
                self.extract_session_data(session, i)
                
                # Optional: Limit for testing
                # if i >= 4:
                #     print(f"\n⚠️  Stopping at {i+1} sessions (test mode)")
                #     break
            
            self.save_data()
            self.print_summary()
            
        except Exception as e:
            print(f"\n❌ Error during execution: {e}")
            import traceback
            traceback.print_exc()


def main():
    import sys
    
    # Parse command-line arguments
    limit = 20  # Default
    if len(sys.argv) > 1:
        try:
            limit = int(sys.argv[1])
        except ValueError:
            print(f"❌ Invalid limit: {sys.argv[1]}")
            print("Usage: python download_tradecentric_data_api.py [limit]")
            print("Example: python download_tradecentric_data_api.py 100")
            return
    
    print("="*60)
    print("🚀 TradeCentric Data Downloader (API Version)")
    print("="*60)
    print(f"Username: {USERNAME}")
    print(f"Password: {'*' * len(PASSWORD)}")
    print(f"Output: {OUTPUT_DIR}")
    print(f"Limit: {limit} sessions")
    print("="*60)
    print()
    
    client = TradeCentricAPIClient()
    client.run(limit=limit)


if __name__ == "__main__":
    main()
