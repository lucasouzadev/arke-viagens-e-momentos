"""Validate a decoded private profile without logging its contents."""
import datetime
import os
import pathlib
import plistlib
import re

root = pathlib.Path(os.environ['DISTRIBUTION_DIR'])
profile = plistlib.loads((root / 'profile.plist').read_bytes())
entitlements = profile.get('Entitlements', {})
assert profile.get('TeamIdentifier') == ['ZRVX9VFFJW'], 'Profile team differs from configured team'
assert entitlements.get('application-identifier') == 'ZRVX9VFFJW.com.travel.arke', 'Profile bundle identifier differs'
assert entitlements.get('com.apple.developer.team-identifier') == 'ZRVX9VFFJW', 'Profile entitlement team differs'
assert entitlements.get('get-task-allow') is False, 'Development profile cannot be used'
assert not profile.get('ProvisionedDevices') and not profile.get('ProvisionsAllDevices'), 'Expected App Store Connect profile'
assert profile.get('ExpirationDate', datetime.datetime.min) > datetime.datetime.utcnow(), 'Profile has expired'
assert re.fullmatch(r'[A-Fa-f0-9-]{36}', profile.get('UUID', '')), 'Invalid profile UUID'
assert profile.get('DeveloperCertificates'), 'Profile contains no signing certificate'
(root / 'profile-uuid').write_text(profile['UUID'])
