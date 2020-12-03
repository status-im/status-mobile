import asyncio
import logging
from datetime import datetime
import os
import json
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
# referred to https://github.com/status-im/status-react/blob/develop/resources/config/fleets.json
mailserver_address = "enode://69f72baa7f1722d111a8c9c68c39a31430e9d567695f6108f31ccb6cd8f0adff4991e7fdca8fa770e75bc8a511" \
                     "a87d24690cbc80e008175f40c157d6f6788d48:status-offline-inbox@206.189.240.16:443"
staging_fleet = 'eth.staging'
prod_fleet = 'eth.prod'
# This fleet is used in the tests
used_fleet = staging_fleet
geth_log_emulator_path = '/storage/emulated/0/Android/data/im.status.ethereum/files/Download/geth.log'

mailserver_ams = 'mail-01.do-ams3'
mailserver_hk = 'mail-01.ac-cn-hongkong-c'
mailserver_gc = 'mail-01.gc-us-central1-a'
mailserver_ams_01 = 'mail-01.do-ams3.{}'.format(used_fleet)
camera_access_error_text = "To grant the required camera permission, please go to your system settings " \
                           "and make sure that Status > Camera is selected."

photos_access_error_text = "Access to external storage is denied"
delete_alert_text = "Warning: If you don’t have your seed phrase written down, you will lose access to your funds after you delete your profile"

connection_not_secure_text = "Connection is not secure! " \
                             "Do not sign transactions or send personal data on this site."
connection_is_secure_text = "Connection is secure. Make sure you really trust this site " \
                            "before signing transactions or entering personal data."
recorded_error = "You have to give permission to send audio messages"

test_dapp_web_url = "status-im.github.io/dapp"
test_dapp_url = 'simpledapp.eth'
test_dapp_name = 'simpledapp.eth'

emojis = {'thumbs-up': 2, 'thumbs-down': 3, 'love': 1, 'laugh': 4, 'angry': 6, 'sad': 5}


with open(os.sep.join(__file__.split(os.sep)[:-1]) + '/../../../translations/en.json') as json_file:
    transl = json.load(json_file)
