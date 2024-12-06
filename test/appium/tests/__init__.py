import asyncio
import functools
import json
import logging
import os
import time
from datetime import datetime

from urllib3.exceptions import MaxRetryError

from support.appium_container import AppiumContainer
from support.test_data import TestSuiteData


async def start_threads(test_name: str, quantity: int, func: type, returns: dict, **kwargs):
    loop = asyncio.get_event_loop()
    #    from tests.conftest import sauce
    #     for _ in range(60):
    #         if 16 - len([job for job in sauce.jobs.get_user_jobs() if job['status'] == 'in progress']) < quantity:
    #             time.sleep(10)
    for i in range(quantity):
        returns[i] = loop.run_in_executor(None, functools.partial(func, **kwargs))
    for k in returns:
        for _ in range(3):
            try:
                returns[k] = await returns[k]
                break
            except MaxRetryError:
                print("MaxRetryError when creating a driver for %s" % test_name)
                time.sleep(10)
    # for k in returns:
    #     for _ in range(30):
    #         try:
    #             k.session_id
    #             break
    #         except AttributeError:
    #             print("AttributeError when creating a driver for %s" % test_name)
    #             time.sleep(3)
    return returns


async def run_in_parallel(funcs):
    loop = asyncio.get_event_loop()
    res = []
    returns = []
    for func in funcs:
        try:
            res.append(loop.run_in_executor(None, functools.partial(func[0], **func[1])))
        except IndexError:
            res.append(loop.run_in_executor(None, func[0]))
    for k in res:
        returns.append(await k)
    return returns


def get_current_time():
    return datetime.now().strftime('%-m%-d%-H%-M%-S')


def debug(text: str):
    logging.debug(text)


appium_root_project_path = os.path.join(os.sep.join(__file__.split(os.sep)[:-1]), '../')

pytest_config_global = dict()
test_suite_data = TestSuiteData()
appium_container = AppiumContainer()

common_password = 'qwerty1234'
unique_password = 'unique' + get_current_time()
pin = '121212'
puk = '000000000000'
pair_code = '000000'
background_service_message = 'Background service for notifications'

test_dapp_web_url = "status-im.github.io/dapp"
test_dapp_url = 'https://simpledapp.status.im/'
test_dapp_name = 'simpledapp.status.im'

emojis = {'thumbs-up': 2, 'thumbs-down': 3, 'love': 1, 'laugh': 4, 'angry': 6, 'sad': 5}

with open(os.sep.join(__file__.split(os.sep)[:-1]) + '/../../../translations/en.json') as json_file:
    transl = json.load(json_file)
