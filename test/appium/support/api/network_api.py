import logging
import time
from datetime import datetime
from json import JSONDecodeError
from os import environ
from typing import List

import pytest
import pytz
import requests
from selenium.common import TimeoutException

import tests


class NetworkApi:
    def __init__(self):
        self.network_url = 'http://api-sepolia.arbiscan.io/api'
        self.api_key = environ.get('ETHERSCAN_API_KEY')

    def log(self, text: str):
        tests.test_suite_data.current_test.testruns[-1].steps.append(text)
        logging.info(text)

    def send_etherscan_request(self, params):
        params['apikey'] = self.api_key
        try:
            response = requests.get(url=self.network_url, params=params).json()
            if response:
                return response['result']
        except TypeError as e:
            self.log("Check response from etherscan API. Returned values do not match expected. %s" % str(e))
        except JSONDecodeError as e:
            self.log("No valid JSON response from Etherscan: %s " % str(e))
            pass

    def get_token_transactions(self, address: str) -> List[dict]:
        params = {'module': 'account', 'action': 'tokentx', 'address': address, 'sort': 'desc'}
        return self.send_etherscan_request(params)

    def get_transactions(self, address: str) -> List[dict]:
        params = {'module': 'account', 'action': 'txlist', 'address': address, 'page': 1, 'offset': 10, 'sort': 'desc'}
        return self.send_etherscan_request(params)

    def get_balance(self, address: str):
        params = {'module': 'account', 'action': 'balance', 'address': address, 'tag': 'latest'}
        balance = self.send_etherscan_request(params)
        if balance:
            self.log('Balance is %s Gwei' % balance)
            return int(balance) / 1000000000000000000
        else:
            self.log('Cannot extract balance!')

    def wait_for_confirmation_of_transaction(self, address, tx_time, confirmations=7, token=False):
        expected_tx_timestamp = datetime.strptime(tx_time, "%Y-%m-%dT%H:%M:%S%z").replace(tzinfo=pytz.UTC)
        start_time = time.time()
        if token:
            token_info = "token transaction"
        else:
            token_info = "ETH transaction"
        self.log('Waiting for %s of %s to have %s confirmations' % (token_info, address, confirmations))
        while round(time.time() - start_time, ndigits=2) < 600:  # should be < idleTimeout capability
            if token:
                transaction = self.get_token_transactions(address)[0]
            else:
                transaction = self.get_transactions(address)[0]
            tx_timestamp = datetime.fromtimestamp(int(transaction['timeStamp'])).replace(tzinfo=pytz.UTC)
            if tx_timestamp > expected_tx_timestamp:
                if int(transaction['confirmations']) >= confirmations:
                    return
            time.sleep(20)
            self.log(
                'Expected amount of confirmations is %s, in fact %s' % (confirmations, transaction['confirmations']))
        pytest.fail('The last transaction was not confirmed, address is %s, still has %s confirmations' % (
            address, int(transaction['confirmations'])))

    def wait_for_balance_to_be(self, address: str, expected_balance: int):
        expected_balance = round(expected_balance, 4)
        for _ in range(5):
            balance = round(self.get_balance(address), 4)
            if balance == expected_balance:
                return
            time.sleep(20)
        raise TimeoutException(
            'balance is not updated on Etherscan, it is %s but expected to be %s' % (balance, expected_balance))
