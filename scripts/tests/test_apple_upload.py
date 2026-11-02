import pathlib
import subprocess
import sys
import tempfile
import unittest


class AppleUploadResultTests(unittest.TestCase):
    def check_result(self, operation, output, accepted):
        with tempfile.TemporaryDirectory() as directory:
            log = pathlib.Path(directory) / 'altool.log'
            log.write_text(output, encoding='utf-8')
            verifier = pathlib.Path(__file__).resolve().parents[1] / 'verify-apple-upload.py'
            result = subprocess.run([sys.executable, str(verifier), operation, str(log)], capture_output=True)
            self.assertEqual(result.returncode == 0, accepted)

    def test_validation_success(self):
        self.check_result('validate', 'VERIFY SUCCEEDED with no errors', True)

    def test_upload_success(self):
        self.check_result('upload', 'UPLOAD SUCCEEDED with no errors', True)

    def test_server_rejection_with_zero_exit_does_not_pass(self):
        self.check_result('validate', 'ERROR: Validation failed (409) Invalid bundle.\nVERIFY FAILED with 1 error', False)

    def test_failed_upload_with_zero_exit_does_not_pass(self):
        self.check_result('upload', 'Failed to upload package.', False)

    def test_error_overrides_success_marker(self):
        self.check_result('upload', 'UPLOAD SUCCEEDED\nERROR: server rejected package', False)

    def test_unknown_or_empty_output_is_not_success(self):
        self.check_result('upload', '', False)
        self.check_result('validate', 'Running altool...', False)
