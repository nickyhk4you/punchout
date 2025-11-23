#!/usr/bin/env python3
"""
Import TradeCentric Session Data to MongoDB

This script imports extracted TradeCentric session data into MongoDB,
organizing by environment (Prod → production, Dev → development, etc.)

Requirements:
    pip install pymongo
"""

import os
import sys
import json
import glob
from datetime import datetime
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError

# MongoDB configuration
MONGO_HOST = os.getenv("MONGO_HOST", "localhost")
MONGO_PORT = int(os.getenv("MONGO_PORT", "27017"))
MONGO_DB = os.getenv("MONGO_DB", "punchout")
DATA_DIR = os.path.join(os.path.dirname(__file__), "../data/tradecentric_import")

# Environment mapping: filename prefix → MongoDB environment
# Note: UI uses 'prod', 'dev', 'stage', 's4-dev' so we map to those values
ENV_MAPPING = {
    'Prod': 'prod',
    'Production': 'prod',
    'PreProd': 'preprod',
    'Pre-Prod': 'preprod',
    'Staging': 'stage',
    'Stage': 'stage',
    'Dev': 'dev',
    'Development': 'dev',
    's4-dev': 's4-dev',
    'S4-Dev': 's4-dev',
}

class TradeCentricImporter:
    def __init__(self):
        self.client = None
        self.db = None
        self.stats = {
            'total_sessions': 0,
            'imported': 0,
            'skipped': 0,
            'errors': 0,
            'by_environment': {}
        }
        
    def connect(self):
        """Connect to MongoDB"""
        print(f"🔌 Connecting to MongoDB at {MONGO_HOST}:{MONGO_PORT}...")
        try:
            self.client = MongoClient(MONGO_HOST, MONGO_PORT)
            self.db = self.client[MONGO_DB]
            # Test connection
            self.client.server_info()
            print(f"✅ Connected to MongoDB database: {MONGO_DB}")
            return True
        except Exception as e:
            print(f"❌ Failed to connect to MongoDB: {e}")
            return False
    
    def parse_session_files(self):
        """Parse all session files and group by session"""
        print(f"\n📂 Scanning for session files in {DATA_DIR}...")
        
        # Find all metadata files
        metadata_pattern = os.path.join(DATA_DIR, "session_*_metadata.json")
        metadata_files = glob.glob(metadata_pattern)
        
        if not metadata_files:
            print(f"⚠️  No session metadata files found in {DATA_DIR}")
            return []
        
        print(f"✅ Found {len(metadata_files)} session metadata files")
        
        sessions = []
        for metadata_file in metadata_files:
            try:
                # Parse filename: session_Prod_JJ_rh69224039e025d_metadata.json
                basename = os.path.basename(metadata_file)
                parts = basename.replace('_metadata.json', '').split('_')
                
                if len(parts) < 4:
                    print(f"⚠️  Skipping malformed filename: {basename}")
                    continue
                
                # Extract parts: session, Prod, JJ, sessionId (or more parts for customer name)
                env_part = parts[1]  # e.g., "Prod"
                session_id = parts[-1]  # Last part is session ID
                customer_parts = parts[2:-1]  # Everything between env and session ID
                customer_name = '_'.join(customer_parts)
                
                # Read metadata
                with open(metadata_file, 'r') as f:
                    metadata = json.load(f)
                
                # Find corresponding input and output files
                prefix = basename.replace('_metadata.json', '')
                input_file = os.path.join(DATA_DIR, f"{prefix}_input.cxml")
                output_file = os.path.join(DATA_DIR, f"{prefix}_output.json")
                
                # Read cXML input
                cxml_data = ""
                if os.path.exists(input_file):
                    with open(input_file, 'r') as f:
                        cxml_data = f.read()
                
                # Read JSON output
                json_data = ""
                if os.path.exists(output_file):
                    with open(output_file, 'r') as f:
                        json_data = f.read()
                
                # Map environment
                mongo_env = ENV_MAPPING.get(env_part, env_part.lower())
                
                # Clean customer name for display
                customer_display = customer_name.replace('_', ' ')
                
                # Create onboarding document that matches customer_onboarding structure
                session = {
                    '_id': f"tradecentric_{mongo_env}_{session_id}",
                    'customerName': customer_display,
                    'customerType': 'CUSTOM',  # Default type
                    'network': f'{customer_name.lower()}.tradecentric.com',
                    'environment': mongo_env,
                    'sampleCxml': cxml_data,
                    'targetJson': json_data,
                    'fieldMappings': {},  # Empty for now
                    'notes': f"Imported from TradeCentric: {metadata.get('routeName', '')}. Extracted at: {metadata.get('extractedAt', '')}",
                    'converterClass': f"{customer_display.replace(' ', '')}CUSTOMConverter",
                    'status': 'DEPLOYED',
                    'deployed': True,
                    'deployedAt': metadata.get('extractedAt', datetime.utcnow().isoformat()),
                    'createdAt': datetime.utcnow().isoformat(),
                    'updatedAt': datetime.utcnow().isoformat(),
                    'createdBy': 'tradecentric_import',
                    'updatedBy': 'tradecentric_import',
                    'source': 'tradecentric_import'
                }
                
                sessions.append(session)
                
            except Exception as e:
                print(f"❌ Error processing {metadata_file}: {e}")
                self.stats['errors'] += 1
        
        return sessions
    
    def import_sessions(self, sessions):
        """Import sessions into MongoDB"""
        if not sessions:
            print("\n⚠️  No sessions to import")
            return
        
        print(f"\n📥 Importing {len(sessions)} sessions to MongoDB...")
        
        # Get collection - import to customer_onboarding so it shows up in UI
        collection = self.db['customer_onboarding']
        
        for session in sessions:
            try:
                env = session['environment']
                doc_id = session['_id']
                customer_name = session['customerName']
                
                # Check if session already exists
                existing = collection.find_one({'_id': doc_id})
                
                if existing:
                    print(f"   ⏭️  Skipping {env}/{customer_name} (already exists)")
                    self.stats['skipped'] += 1
                else:
                    # Insert new session
                    collection.insert_one(session)
                    print(f"   ✅ Imported {env}/{customer_name}")
                    self.stats['imported'] += 1
                    
                    # Track by environment
                    if env not in self.stats['by_environment']:
                        self.stats['by_environment'][env] = 0
                    self.stats['by_environment'][env] += 1
                
                self.stats['total_sessions'] += 1
                
            except DuplicateKeyError:
                print(f"   ⏭️  Skipping {doc_id} (duplicate)")
                self.stats['skipped'] += 1
            except Exception as e:
                print(f"   ❌ Error importing {doc_id}: {e}")
                self.stats['errors'] += 1
    
    def print_summary(self):
        """Print import summary"""
        print("\n" + "="*60)
        print("📊 IMPORT SUMMARY")
        print("="*60)
        print(f"Total sessions processed: {self.stats['total_sessions']}")
        print(f"✅ Successfully imported: {self.stats['imported']}")
        print(f"⏭️  Skipped (duplicates): {self.stats['skipped']}")
        print(f"❌ Errors: {self.stats['errors']}")
        print()
        print("By Environment:")
        for env, count in sorted(self.stats['by_environment'].items()):
            print(f"  - {env}: {count} sessions")
        print("="*60)
    
    def close(self):
        """Close MongoDB connection"""
        if self.client:
            self.client.close()
            print("\n🔒 MongoDB connection closed")
    
    def run(self):
        """Main execution flow"""
        try:
            if not self.connect():
                return False
            
            sessions = self.parse_session_files()
            self.import_sessions(sessions)
            self.print_summary()
            
            return self.stats['errors'] == 0
            
        except Exception as e:
            print(f"\n❌ Error during import: {e}")
            import traceback
            traceback.print_exc()
            return False
        finally:
            self.close()


def main():
    print("="*60)
    print("🚀 TradeCentric Session Data Import to MongoDB")
    print("="*60)
    print(f"MongoDB Host: {MONGO_HOST}")
    print(f"MongoDB Port: {MONGO_PORT}")
    print(f"Database: {MONGO_DB}")
    print(f"Collection: customer_onboarding (for UI visibility)")
    print(f"Data Directory: {DATA_DIR}")
    print("="*60)
    print()
    
    # Check if data directory exists
    if not os.path.exists(DATA_DIR):
        print(f"❌ Data directory not found: {DATA_DIR}")
        print("   Please run download_tradecentric_data_api.py first")
        sys.exit(1)
    
    importer = TradeCentricImporter()
    success = importer.run()
    
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
