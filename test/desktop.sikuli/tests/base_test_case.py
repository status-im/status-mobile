import org.sikuli.script.SikulixForJython
import pytest
from sikuli import *
from subprocess import check_output


def mac_os_setup():
    check_output(['hdiutil', 'attach', 'nightly.dmg'])
    check_output(['cp', '-rf', '/Volumes/Status/Status.app', '/Applications/'])
    check_output(['hdiutil', 'detach', '/Volumes/Status/'])
    import time
    time.sleep(10)
    openApp('Status.app')


def mac_os_teardown():
    closeApp('Status.app')
    for dir in '/Applications/Status.app', '/Library/Application\ Support/StatusIm', \
               '/Users/yberdnyk/Library/Caches/StatusIm':
        check_output(['rm', '-rf', dir])


def linux_setup():
    check_output(['chmod', '+x', 'nightly.AppImage'])
    openApp('./nightly.AppImage')


def linux_teardown():
    check_output(['killall', 'ubuntu-server'])
    check_output(['rm', '-rf', '~/.local/share/StatusIm/'])
    check_output(['rm', '-rf', '~/.cache/StatusIm/'])


class BaseTestCase:
    Settings.ActionLogs = 0

    def setup_method(self, method):
        if pytest.config.getoption('os') == 'linux':
            linux_setup()
        else:
            mac_os_setup()

    def teardown_method(self, method):
        if pytest.config.getoption('os') == 'linux':
            linux_teardown()
        else:
            mac_os_teardown()
