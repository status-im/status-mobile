import logging
from typing import List

import pytest
import requests
import time
from json import JSONDecodeError
from decimal import Decimal
from os import environ
import tests
import support.api.web3_api as w3

class NetworkApi(object):

    def __init__(self):
        self.network_url = 'http://api-%s.etherscan.io/api?' % tests.pytest_config_global['network']
        self.faucet_url = 'https://faucet-ropsten.status.im/donate'
        self.faucet_backup_address = w3.account_address
        self.headers = {
        'User-Agent':"Mozilla\\5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit\\537.36 (KHTML, like Gecko) Chrome\\7"
                     "7.0.3865.90 Safari\\537.36", }
        self.chat_bot_url = 'http://offsite.chat:8099'
        self.api_key = environ.get('ETHERSCAN_API_KEY')

    def log(self, text: str):
        tests.test_suite_data.current_test.testruns[-1].steps.append(text)
        logging.info(text)

    def get_transactions(self, address: str) -> List[dict]:
        method = self.network_url + 'module=account&action=txlist&address=0x%s&sort=desc&apikey=%s' % (address, self.api_key)
        try:
            transactions_response = requests.request('GET', url=method, headers=self.headers).json()
            if transactions_response:
                return transactions_response['result']
        except TypeError as e:
            self.log("Check response from etherscan API. Returned values do not match expected. %s" % e)
        except JSONDecodeError as e:
            self.log("No valid JSON response from Etherscan: %s " % str(e))
            pass

    def get_token_transactions(self, address: str) -> List[dict]:
        method = self.network_url + 'module=account&action=tokentx&address=0x%s&sort=desc&apikey=%s' % (address, self.api_key)
        try:
            transactions_response = requests.request('GET', url=method, headers=self.headers).json()
            if transactions_response:
                return transactions_response['result']
        except TypeError as e:
            self.log("Check response from etherscan API. Returned values do not match expected. %s" % str(e))
        except JSONDecodeError as e:
            self.log("No valid JSON response from Etherscan: %s " % str(e))
            pass

    def is_transaction_successful(self, transaction_hash: str) -> int:
        method = self.network_url + 'module=transaction&action=getstatus&txhash=%s' % transaction_hash
        return not int(requests.request('GET', url=method, headers=self.headers).json()['result']['isError'])

    def get_balance(self, address):
        address = '0x' + address
        balance = w3.balance_of_address(address)
        self.log('Balance is %s Gwei' % balance)
        return int(balance)

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
        additional_info = 'token transactions' if token else 'ETH transactions'
        counter = 0
        while True:
            if counter >= wait_time:
                for entry in range(0,5):
                    self.log('Transaction #%s, amount is %s' %(entry+1, float(int(transactions[entry]['value']) / 10 ** decimals)))
                    self.log(str(transactions[entry]))
                pytest.fail(
                    'Transaction with amount %s is not found in list of %s, address is %s during %ss' %
                    (amount, additional_info, address, wait_time))
            else:
                counter += 30
                time.sleep(30)
                try:
                    if token:
                        transactions = self.get_token_transactions(address)
                    else:
                        transactions = self.get_transactions(address)
                except JSONDecodeError as e:
                    self.log("No valid JSON response from Etherscan: %s " % str(e))
                    continue
                try:
                    for transaction in transactions:
                        if float(int(transaction['value']) / 10 ** decimals) == float(amount):
                            return transaction
                except TypeError as e:
                    self.log("Failed iterate transactions: " + str(e))
                    pytest.fail("No valid JSON response from Etherscan: %s " % str(e))
                    # continue

    def wait_for_confirmation_of_transaction(self, address, amount, confirmations=12, token=False):
        start_time = time.time()
        if token:
            token_info = "token transaction"
        else:
            token_info = "ETH transaction"
        self.log('Waiting %s %s for %s to have %s confirmations' % (amount, token_info, address, confirmations))
        while round(time.time() - start_time, ndigits=2) < 900:  # should be < idleTimeout capability
            transaction = self.find_transaction_by_unique_amount(address, amount, token)
            self.log(
                'Expected amount of confirmations is %s, in fact %s' % (confirmations, transaction['confirmations']))
            if int(transaction['confirmations']) >= confirmations:
                return
            time.sleep(20)
        pytest.fail('Transaction with amount %s was not confirmed, address is %s, still has %s confirmations' % (amount, address, int(transaction['confirmations'])))

    def verify_balance_is_updated(self, initial_balance, recipient_address, wait_time=360):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail('Balance is not changed during %s seconds' % wait_time)
            elif initial_balance == self.get_balance(recipient_address):
                counter += 10
                time.sleep(10)
                self.log('Waiting %s seconds for for changing account balance from %s' % (counter, initial_balance))
            else:
                self.log('Balance is updated!')
                return

    def verify_balance_is(self, expected_balance: int, recipient_address: str, errors: list):
        balance = self.get_balance(recipient_address)
        if balance / 1000000000000000000 != expected_balance:
            errors.append('Recipients balance is not updated on etherscan')

    def faucet(self, address):
        try:
            self.log("Trying to get funds from %s" % self.faucet_url)
            return requests.request('GET', '%s/0x%s' % (self.faucet_url, address)).json()
        except JSONDecodeError as e:
            self.log("No valid JSON response from Etherscan: %s " % str(e))
            pass

    def faucet_backup(self, address):
            self.log("Trying to get funds from %s" % self.faucet_backup_address)
            address = "0x" + address
            w3.donate_testnet_eth(address=address, amount=0.005, inscrease_default_gas_price=10)

    def get_donate(self, address, external_faucet=False, wait_time=300):
        initial_balance = self.get_balance(address)
        counter = 0
        if initial_balance < 1000000000000000000:
            if external_faucet:
                self.faucet_backup(address)
            else:
                self.faucet(address)
            while True:
                if counter >= wait_time:
                    pytest.fail("Donation was not received during %s seconds!" % wait_time)
                elif self.get_balance(address) == initial_balance:
                    counter += 10
                    time.sleep(10)
                    self.log('Waiting %s seconds for donation' % counter)
                else:
                    self.log('Got %s Gwei for %s' % (self.get_balance(address), address))
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