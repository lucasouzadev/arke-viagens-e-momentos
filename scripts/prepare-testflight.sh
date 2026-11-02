#!/usr/bin/env bash
set -euo pipefail
umask 077
for variable in APPLE_CERTIFICATE_BASE64 APPLE_CERTIFICATE_PASSWORD APPLE_PROVISIONING_PROFILE_BASE64 ASC_KEY_ID ASC_ISSUER_ID ASC_PRIVATE_KEY_BASE64; do
  if [ -z "${!variable:-}" ]; then
    echo "::error::Missing environment secret: $variable"
    exit 1
  fi
done
test "$DISTRIBUTION_DIR" = "$RUNNER_TEMP/arke-testflight"
mkdir -p "$DISTRIBUTION_DIR/private_keys"
python3 - <<'PY'
import base64, os, pathlib, re
root = pathlib.Path(os.environ['DISTRIBUTION_DIR'])
key_id = os.environ['ASC_KEY_ID']
assert re.fullmatch(r'[A-Za-z0-9]+', key_id), 'Invalid ASC key identifier'
for variable, name in [
    ('APPLE_CERTIFICATE_BASE64', 'certificate.p12'),
    ('APPLE_PROVISIONING_PROFILE_BASE64', 'profile.mobileprovision'),
    ('ASC_PRIVATE_KEY_BASE64', f'private_keys/AuthKey_{key_id}.p8')]:
    try:
        (root / name).write_bytes(base64.b64decode(''.join(os.environ[variable].split()), validate=True))
    except Exception:
        raise SystemExit(f'Invalid Base64 in {variable}') from None
PY
security cms -D -i "$DISTRIBUTION_DIR/profile.mobileprovision" > "$DISTRIBUTION_DIR/profile.plist"
python3 scripts/validate-testflight-profile.py
keychain_password=$(openssl rand -hex 24)
echo "::add-mask::$keychain_password"
keychain="$DISTRIBUTION_DIR/distribution.keychain-db"
security create-keychain -p "$keychain_password" "$keychain"
security set-keychain-settings -lut 21600 "$keychain"
security unlock-keychain -p "$keychain_password" "$keychain"
security import "$DISTRIBUTION_DIR/certificate.p12" -P "$APPLE_CERTIFICATE_PASSWORD" -A -t cert -f pkcs12 -k "$keychain" > /dev/null
security set-key-partition-list -S apple-tool:,apple:,codesign: -k "$keychain_password" "$keychain" > /dev/null
security list-keychains -d user -s "$keychain" "$HOME/Library/Keychains/login.keychain-db"
security find-identity -v -p codesigning "$keychain" > "$DISTRIBUTION_DIR/identities.txt"
python3 - <<'PY'
import hashlib, os, pathlib, plistlib, re
root = pathlib.Path(os.environ['DISTRIBUTION_DIR'])
profile = plistlib.loads((root / 'profile.plist').read_bytes())
identities = (root / 'identities.txt').read_text()
matches = re.findall(r'([A-Fa-f0-9]{40}) "Apple Distribution:[^"\n]*\(ZRVX9VFFJW\)"', identities)
fingerprints = {hashlib.sha1(cert).hexdigest().upper() for cert in profile['DeveloperCertificates']}
valid = [item.upper() for item in matches if item.upper() in fingerprints]
assert len(valid) == 1, 'Expected one valid Apple Distribution private key matching profile and team'
(root / 'identity').write_text(valid[0])
destination = pathlib.Path.home() / 'Library/MobileDevice/Provisioning Profiles' / (profile['UUID'] + '.mobileprovision')
destination.parent.mkdir(parents=True, exist_ok=True)
(root / 'installed-profile-path').write_text(str(destination))
destination.write_bytes((root / 'profile.mobileprovision').read_bytes())
options = plistlib.loads(pathlib.Path('ios/ExportOptions-TestFlight.plist').read_bytes())
options.update(signingStyle='manual', signingCertificate=valid[0], provisioningProfiles={'com.travel.arke': profile['UUID']}, manageAppVersionAndBuildNumber=False)
(root / 'ExportOptions.plist').write_bytes(plistlib.dumps(options))
print('Distribution certificate and App Store profile validated.')
PY
