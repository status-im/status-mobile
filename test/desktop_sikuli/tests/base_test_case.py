try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass


class BaseTestCase:

    try:
        Settings.ActionLogs = 0
        Settings.MinSimilarity = 0.4
    except NameError:
        pass

    def setup_method(self, method):
        openApp('/home/squashfs-root/AppRun')

    def teardown_method(self, method):
        pass
