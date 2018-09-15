import org.sikuli.script.SikulixForJython
from sikuli import *


class BaseTestCase:

    Settings.ActionLogs = 0

    def setup_method(self, method):
        openApp('/home/squashfs-root/AppRun')

    def teardown_method(self, method):
        pass
