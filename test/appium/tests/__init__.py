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
pin = '121212'
puk = '000000000000'
pair_code= '000000'

bootnode_address = "enode://a8a97f126f5e3a340cb4db28a1187c325290ec08b2c9a6b1f19845ac86c46f9fac2ba13328822590" \
                   "fd3de3acb09cc38b5a05272e583a2365ad1fa67f66c55b34@167.99.210.203:30404"
# referred to https://github.com/status-im/status-react/blob/1ea49a80fc915aa3174ecfd9649c3bab6480d30d/src/status_im/constants.cljs#L40
mailserver_address = "enode://ee2b53b0ace9692167a410514bca3024695dbf0e1a68e1dff9716da620efb195f04a4b9e873fb9b74ac84de80" \
                     "1106c465b8e2b6c4f0d93b8749d1578bfcaf03e:status-offline-inbox@104.197.238.144:443"
staging_fleet = 'eth.staging'
prod_fleet = 'eth.prod'
mailserver_ams = 'mail-01.do-ams3'
mailserver_hk = 'mail-02.ac-cn-hongkong-c'
mailserver_gc = 'mail-01.gc-us-central1-a'
mailserver_ams_01 = 'mail-01.do-ams3.eth.prod'
camera_access_error_text = "To grant the required camera permission, please go to your system settings " \
                           "and make sure that Status > Camera is selected."

photos_access_error_text = "To grant the required photos permission, please go to your system settings " \
                           "and make sure that Status > Photos is selected."

connection_not_secure_text = "Connection is not secure! " \
                             "Do not sign transactions or send personal data on this site."
connection_is_secure_text = "Connection is secure. Make sure you really trust this site " \
                            "before signing transactions or entering personal data."

test_dapp_url = 'simpledapp.eth'
test_dapp_name = 'simpledapp.eth'
