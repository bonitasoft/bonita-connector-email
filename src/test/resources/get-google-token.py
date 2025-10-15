#!/usr/bin/env python3
"""
Google OAuth2 Token retrieval for Gmail SMTP Testing

This script helps you obtain an access token for testing Gmail SMTP with OAuth2.
"""

import json
import urllib.parse
import urllib.request
import webbrowser
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

# Configuration - Fill these from your Google Cloud Console
CLIENT_ID = "****.apps.googleusercontent.com"
CLIENT_SECRET = "***"

# OAuth2 endpoints
AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
SCOPE = "https://mail.google.com/"
REDIRECT_URI = "http://localhost:8080"

class CallbackHandler(BaseHTTPRequestHandler):
    """HTTP handler to capture OAuth2 callback"""

    def do_GET(self):
        """Handle the OAuth2 callback"""
        query = urlparse(self.path).query
        params = parse_qs(query)

        if 'code' in params:
            self.server.auth_code = params['code'][0]
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            self.wfile.write(b"""
                <html>
                <body>
                    <h1>Authorization Successful!</h1>
                    <p>You can close this window and return to the terminal.</p>
                </body>
                </html>
            """)
        else:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"No authorization code received")

    def log_message(self, format, *args):
        """Suppress log messages"""
        pass

def get_authorization_code():
    """Start local server and get authorization code"""
    print("\n=== Step 1: Get Authorization Code ===")

    # Build authorization URL
    params = {
        'client_id': CLIENT_ID,
        'redirect_uri': REDIRECT_URI,
        'scope': SCOPE,
        'response_type': 'code',
        'access_type': 'offline',
        'prompt': 'consent'  # Force to show consent screen to get refresh token
    }

    auth_url = f"{AUTH_ENDPOINT}?{urllib.parse.urlencode(params)}"

    print(f"\nOpening browser to authorize...")
    print(f"\nIf the browser doesn't open, visit this URL:")
    print(f"{auth_url}\n")

    # Open browser
    webbrowser.open(auth_url)

    # Start local server to receive callback
    server = HTTPServer(('localhost', 8080), CallbackHandler)
    server.auth_code = None

    print("Waiting for authorization...")
    while server.auth_code is None:
        server.handle_request()

    return server.auth_code

def exchange_code_for_tokens(auth_code):
    """Exchange authorization code for access and refresh tokens"""
    print("\n=== Step 2: Exchange Code for Tokens ===\n")

    data = urllib.parse.urlencode({
        'code': auth_code,
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'redirect_uri': REDIRECT_URI,
        'grant_type': 'authorization_code'
    }).encode('utf-8')

    req = urllib.request.Request(TOKEN_ENDPOINT, data=data)
    req.add_header('Content-Type', 'application/x-www-form-urlencoded')

    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode('utf-8'))
            return result
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        print(f"Error: {e.code}")
        print(error_body)
        return None

def main():
    """Main function"""
    print("=" * 60)
    print("Google OAuth2 Token Generator for Gmail SMTP")
    print("=" * 60)

    # Check configuration
    if CLIENT_ID == "YOUR_CLIENT_ID.apps.googleusercontent.com":
        print("\n⚠️  ERROR: Please edit this script and set your CLIENT_ID and CLIENT_SECRET")
        print("\nGet them from: https://console.cloud.google.com/apis/credentials")
        print("\n1. Create OAuth 2.0 Client ID (Web application type)")
        print("2. Add http://localhost:8080 to Authorized redirect URIs")
        print("3. Copy Client ID and Client Secret to this script")
        return

    # Get authorization code
    try:
        auth_code = get_authorization_code()
        print(f"✓ Authorization code received")
    except KeyboardInterrupt:
        print("\n\nAborted by user")
        return
    except Exception as e:
        print(f"\n✗ Error getting authorization code: {e}")
        return

    # Exchange for tokens
    tokens = exchange_code_for_tokens(auth_code)

    if tokens:
        print("✓ Tokens received successfully!\n")
        print("=" * 60)
        print("ACCESS TOKEN (expires in ~1 hour):")
        print("=" * 60)
        print(tokens.get('access_token', 'N/A'))
        print()

        if 'refresh_token' in tokens:
            print("=" * 60)
            print("REFRESH TOKEN (save this to get new access tokens):")
            print("=" * 60)
            print(tokens['refresh_token'])
            print()

        print("=" * 60)
        print("EXPORT COMMANDS FOR TESTING:")
        print("=" * 60)
        print(f"export GMAIL_OAUTH2_USER=\"your-email@gmail.com\"")
        print(f"export GMAIL_OAUTH2_TOKEN=\"{tokens.get('access_token')}\"")
        print(f"export GMAIL_OAUTH2_TO=\"recipient@example.com\"")
        print()
        print("Then run the test:")
        print("./mvnw test -Dtest=EmailConnectorXOAuth2IT#testManualXOAuth2WithGmail")
        print()
    else:
        print("✗ Failed to obtain tokens")

if __name__ == "__main__":
    main()
