"""Fail closed when altool reports errors despite returning exit status zero."""
import pathlib
import re
import sys

operation, filename = sys.argv[1:]
output = pathlib.Path(filename).read_text(encoding='utf-8', errors='replace')
success_patterns = {
    'validate': r'\b(?:VERIFY|VALIDATION) SUCCEEDED\b|\bNo errors validating\b',
    'upload': r'\bUPLOAD (?:SUCCEEDED|SUCCESSFUL)\b|\bNo errors uploading\b|\bsuccessfully uploaded\b',
}
assert operation in success_patterns, 'Unknown Apple operation'
errors = r'\bERROR\s*:|\b(?:VERIFY|VALIDATION|UPLOAD) FAILED\b|\bFailed to (?:upload|validate)\b'
if re.search(errors, output, re.IGNORECASE) or not re.search(success_patterns[operation], output, re.IGNORECASE):
    raise SystemExit(f'Apple {operation} was not confirmed successful. Review altool diagnostics; upload is not accepted.')
print(f'Apple {operation} explicitly confirmed successful.')
