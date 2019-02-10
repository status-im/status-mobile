import pytest

try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass


class BaseTestCase:
    errors = list()

    try:
        Settings.ActionLogs = 0
        Settings.MinSimilarity = 0.4
    except NameError:
        pass

    def setup_method(self, method):
        openApp('/home/squashfs-root/AppRun')

    def teardown_method(self, method):
        pass

    def verify_no_errors(self):
        if self.errors:
            pytest.fail('. '.join([self.errors.pop(0) for _ in range(len(self.errors))]))
