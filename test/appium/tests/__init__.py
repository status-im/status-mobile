import asyncio
import logging
from datetime import datetime

from support.appium_container import AppiumContainer
from support.test_data import TestSuiteData


@asyncio.coroutine
def start_threads(quantity: int, func: type, returns: dict, *args):
    loop = asyncio.get_event_loop()
    for i in range(quantity):
        returns[i] = loop.run_in_executor(None, func, *args)
    for k in returns:
        returns[k] = yield from returns[k]
    return returns


def get_current_time():
    return datetime.now().strftime('%-m%-d%-H%-M%-S')


def debug(text: str):
    logging.debug(text)

pytest_config_global = dict()
test_suite_data = TestSuiteData()
appium_container = AppiumContainer()

common_password = 'qwerty'
unique_password = 'unique' + get_current_time()

bootnode_address = "enode://a8a97f126f5e3a340cb4db28a1187c325290ec08b2c9a6b1f19845ac86c46f9fac2ba13328822590" \
                   "fd3de3acb09cc38b5a05272e583a2365ad1fa67f66c55b34@167.99.210.203:30404"
mailserver_address = "enode://e4fc10c1f65c8aed83ac26bc1bfb21a45cc1a8550a58077c8d2de2a0e0cd18e40fd40f7e6f7d02dc" \
                     "6cd06982b014ce88d6e468725ffe2c138e958788d0002a7f:status-offline-inbox@35.239.193.41:443"
mailserver_central_2 = 'mail-02.gc-us-central1-a.eth.beta'
mailserver_central_3 = 'mail-03.gc-us-central1-a.eth.beta'
mailserver_staging_central_1 = 'mail-01.gc-us-central1-a.eth.staging'
mailserver_staging_ams_1 = 'mail-01.do-ams3.eth.staging'
mailserver_staging_hk = 'mail-01.ac-cn-hongkong-c.eth.staging'
mailserver_ams_01 = 'mail-01.do-ams3.eth.beta'
camera_access_error_text = "To grant the required camera permission, please go to your system settings " \
                           "and make sure that Status > Camera is selected."

photos_access_error_text = "To grant the required photos permission, please go to your system settings " \
                           "and make sure that Status > Photos is selected."

connection_not_secure_text = "Connection is not secure! " \
                             "Do not sign transactions or send personal data on this site."
connection_is_secure_text = "Connection is secure. Make sure you really trust this site " \
                            "before signing transactions or entering personal data."

test_dapp_url = 'status-im.github.io/dapp'
test_dapp_name = 'status-im.github.io'
