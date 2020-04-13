import logging
from typing import List

import pytest
import requests
import time
from json import JSONDecodeError
from decimal import Decimal
from os import environ
import tests


class NetworkApi(object):

    def __init__(self):
        self.network_url = 'http://api-%s.etherscan.io/api?' % tests.pytest_config_global['network']
        self.faucet_url = 'https://faucet-ropsten.status.im/donate'
        self.faucet_backup_url = 'https://faucet.ropsten.be/donate'
        self.headers = {
        'User-Agent':"Mozilla\/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit\
        /537.36 (KHTML, like Gecko) Chrome\/77.0.3865.90 Safari\/537.36", }
        self.chat_bot_url = 'http://offsite.chat:8099'
        self.api_key = environ.get('ETHERSCAN_API_KEY')

    def log(self, text: str):

        tests.test_suite_data.current_test.testruns[-1].steps.append(text)
        logging.info(text)

    def get_transactions(self, address: str) -> List[dict]:
        method = self.network_url + 'module=account&action=txlist&address=0x%s&sort=desc&apikey=%s' % (address, self.api_key)
        try:
            return requests.request('GET', url=method, headers=self.headers).json()['result']
        except TypeError as e:
            self.log("Check response from etherscan API. Returned values do not match expected. %s" % e)

    def get_token_transactions(self, address: str) -> List[dict]:
        method = self.network_url + 'module=account&action=tokentx&address=0x%s&sort=desc&apikey=%s' % (address, self.api_key)
        try:
            return requests.request('GET', url=method, headers=self.headers).json()['result']
        except TypeError as e:
            self.log("Check response from etherscan API. Returned values do not match expected. %s" % e)

    def is_transaction_successful(self, transaction_hash: str) -> int:
        method = self.network_url + 'module=transaction&action=getstatus&txhash=%s' % transaction_hash
        return not int(requests.request('GET', url=method, headers=self.headers).json()['result']['isError'])

    def get_balance(self, address):
        method = self.network_url + 'module=account&action=balance&address=0x%s&tag=latest&apikey=%s' % (address , self.api_key)
        for i in range(5):
            try:
                return int(requests.request('GET', method, headers=self.headers).json()["result"])
            except ValueError:
                pass

    def get_latest_block_number(self) -> int:
        method = self.network_url + 'module=proxy&action=eth_blockNumber'
        return int(requests.request('GET', url=method).json()['result'], 0)

    def find_transaction_by_hash(self, address: str, transaction_hash: str):
        transactions = self.get_transactions(address=address)
        for transaction in transactions:
            if transaction['hash'] == transaction_hash:
                logging.info('Transaction is found in Ropsten network')
                return
        pytest.fail('Transaction is not found in Ropsten network')

    def find_transaction_by_unique_amount(self, address, amount, token=False, decimals=18, wait_time=600):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail(
                    'Transaction with amount %s is not found in list of transactions, address is %s' %
                    (amount, address))
            else:
                counter += 10
                time.sleep(10)
                try:
                    if token:
                        transactions = self.get_token_transactions(address)
                    else:
                        transactions = self.get_transactions(address)
                except JSONDecodeError as e:
                    self.log(str(e))
                    continue
                self.log('Looking for a transaction with unique amount %s in list of transactions, address is %s' %
                             (amount, address))
                try:
                    for transaction in transactions:
                        if float(int(transaction['value']) / 10 ** decimals) == float(amount):
                            self.log(
                                'Transaction with unique amount %s is found in list of transactions, address is %s' %
                                (amount, address))
                            return transaction
                except TypeError as e:
                    self.log("Failed iterate transactions " + str(e))
                    continue

    def wait_for_confirmation_of_transaction(self, address, amount, confirmations=12, token=False):
        start_time = time.time()
        while round(time.time() - start_time, ndigits=2) < 900:  # should be < idleTimeout capability
            transaction = self.find_transaction_by_unique_amount(address, amount, token)
            if int(transaction['confirmations']) >= confirmations:
                return
            time.sleep(10)
        pytest.fail('Transaction with amount %s was not confirmed, address is %s' % (amount, address))

    def verify_balance_is_updated(self, initial_balance, recipient_address, wait_time=360):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail('Balance is not changed during %s seconds, funds were not received!' % wait_time)
            elif initial_balance == self.get_balance(recipient_address):
                counter += 10
                time.sleep(10)
                self.log('Waiting %s seconds for funds' % counter)
            else:
                self.log('Transaction is received')
                return

    def verify_balance_is(self, expected_balance: int, recipient_address: str, errors: list):
        balance = self.get_balance(recipient_address)
        if balance / 1000000000000000000 != expected_balance:
            errors.append('Recipients balance is not updated on etherscan')

    def faucet(self, address):
        return requests.request('GET', '%s/0x%s' % (self.faucet_url, address)).json()

    def faucet_backup(self, address):
        return requests.request('GET', '%s/0x%s' % (self.faucet_backup_url, address)).json()

    def get_donate(self, address, external_faucet=True, wait_time=300):
        initial_balance = self.get_balance(address)
        counter = 0
        if initial_balance < 1000000000000000000:
            if external_faucet:
                self.faucet_backup(address)
            response = self.faucet(address)
            while True:
                if counter >= wait_time:
                    pytest.fail("Donation was not received during %s seconds!" % wait_time)
                elif self.get_balance(address) == initial_balance:
                    counter += 10
                    time.sleep(10)
                    self.log('Waiting %s seconds for donation' % counter)
                else:
                    self.log('Got %s for %s' % (response["amount_eth"], address))
                    return

    def start_chat_bot(self, chat_name: str, messages_number: int, interval: int = 1) -> list:
        url = '%s/ping/%s?count=%s&interval=%s' % (self.chat_bot_url, chat_name, messages_number, interval)
        text = requests.request('GET', url).text
        return [i.split(maxsplit=5)[-1].strip('*') for i in text.splitlines()]

    def get_rounded_balance(self, fetched_balance, actual_balance):
        fetched_balance, actual_balance = str(fetched_balance), str(actual_balance)
        # get actual number of decimals on account balance
        decimals = abs(Decimal(fetched_balance).as_tuple().exponent)
        rounded_balance = round(float(actual_balance), decimals)
        return rounded_balance