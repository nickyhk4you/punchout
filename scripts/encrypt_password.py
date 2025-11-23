#!/usr/bin/env python3
"""
Encrypt passwords using Jasypt-compatible encryption

This script encrypts passwords that can be decrypted by the Java Jasypt library.
Uses PBEWithMD5AndDES algorithm to match the Java configuration.

Requirements:
    pip install pycryptodome
"""

import sys
import base64
import os
from Crypto.Cipher import DES
from Crypto.Hash import MD5
from Crypto.Random import get_random_bytes

def derive_key_and_iv(password, salt, iterations=1000):
    """
    Derive key and IV using MD5 (to match Jasypt's PBEWithMD5AndDES)
    Based on PKCS#5 PBKDF1
    """
    key_iv = password.encode('utf-8') + salt
    
    for i in range(iterations):
        hasher = MD5.new()
        hasher.update(key_iv)
        key_iv = hasher.digest()
    
    # Split into key (8 bytes) and IV (8 bytes)  
    key = key_iv[0:8]
    iv = key_iv[8:16] if len(key_iv) >= 16 else b'\x00' * 8
    
    return key, iv

def encrypt_password(plaintext, encryptor_password="punchout-secret-key"):
    """
    Encrypt a password using Jasypt-compatible PBEWithMD5AndDES
    Must match Jasypt's algorithm exactly
    """
    # Generate random salt (8 bytes)
    salt = get_random_bytes(8)
    
    # Derive key and IV using PBKDF1 with MD5
    key, iv = derive_key_and_iv(encryptor_password, salt, iterations=1000)
    
    # Pad plaintext using PKCS#5 padding
    def pkcs5_pad(data):
        pad_len = 8 - (len(data) % 8)
        return data + bytes([pad_len] * pad_len)
    
    padded_data = pkcs5_pad(plaintext.encode('utf-8'))
    
    # Encrypt using DES in CBC mode with derived IV
    # Note: Even though config says NoIvGenerator, the derived IV is used
    cipher = DES.new(key, DES.MODE_CBC, iv=iv)
    encrypted = cipher.encrypt(padded_data)
    
    # Combine salt + encrypted data (Jasypt format)
    result = salt + encrypted
    
    # Encode as base64
    encoded = base64.b64encode(result).decode('utf-8')
    
    return f"ENC({encoded})"

def main():
    encryptor_password = os.getenv('JASYPT_ENCRYPTOR_PASSWORD', 'punchout-secret-key')
    
    if len(sys.argv) < 2:
        print("Usage: python encrypt_password.py <password>")
        print("\nOptional: Set JASYPT_ENCRYPTOR_PASSWORD environment variable")
        print(f"Current encryptor password: {'***' + encryptor_password[-4:] if len(encryptor_password) > 4 else '***'}")
        print("\nExamples:")
        print("  python encrypt_password.py 'Password1!'")
        print("  JASYPT_ENCRYPTOR_PASSWORD=my-secret python encrypt_password.py 'Password1!'")
        sys.exit(1)
    
    plaintext = sys.argv[1]
    encrypted = encrypt_password(plaintext, encryptor_password)
    
    print("="*60)
    print("🔐 Jasypt Password Encryption")
    print("="*60)
    print(f"Plaintext: {plaintext}")
    print(f"Encrypted: {encrypted}")
    print("="*60)
    print("\nUse this in MongoDB:")
    print(f'  authPassword: "{encrypted}"')
    print("\nOr update directly:")
    print(f"  mongosh punchout --eval \"db.environment_configs.updateOne(")
    print(f"    {{environment: 'prod'}},")
    print(f"    {{$set: {{authPassword: '{encrypted}'}}}})\"" )

if __name__ == "__main__":
    main()
