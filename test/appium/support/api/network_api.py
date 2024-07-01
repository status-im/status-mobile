import logging
import time
from decimal import Decimal
from json import JSONDecodeError
from os import environ
from typing import List

import pytest
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
        params = {'module': 'account', 'action': 'txlist', 'address': address, 'sort': 'desc'}
        return self.send_etherscan_request(params)

    def is_transaction_successful(self, transaction_hash: str) -> int:
        params = {'module': 'transaction', 'action': 'getstatus', 'txhash': transaction_hash}
        return not int(self.send_etherscan_request(params)['isError'])

    def get_balance(self, address: str):
        params = {'module': 'account', 'action': 'balance', 'address': address, 'tag': 'latest'}
        balance = self.send_etherscan_request(params)
        if balance:
            self.log('Balance is %s Gwei' % balance)
            return int(balance) / 1000000000000000000
        else:
            self.log('Cannot extract balance!')

    def get_latest_block_number(self) -> int:
        params = {'module': 'proxy', 'action': 'eth_blockNumber'}
        return int(self.send_etherscan_request(params), 0)

    def find_transaction_by_hash(self, transaction_hash: str):
        params = {'module': 'transaction', 'action': 'gettxreceiptstatus', 'txhash': transaction_hash}
        result = self.send_etherscan_request(params)

        if result:
            final_status = True
            if result['status'] == '1':
                self.log("TX %s is found and confirmed" % transaction_hash)
            elif result['status'] == '0':
                self.log("TX %s is found and failed: " % transaction_hash)
            else:
                final_status = False
                self.log("TX %s is not found!" % transaction_hash)
            return final_status

    def find_transaction_by_unique_amount(self, address, amount, token=False, decimals=18, wait_time=300):
        additional_info = 'token transactions' if token else 'ETH transactions'
        counter = 0
        while True:
            if counter >= wait_time:
                for entry in range(0, 5):
                    self.log('Transaction #%s, amount is %s' % (
                        entry + 1, float(int(transactions[entry]['value']) / 10 ** decimals)))
                    self.log(str(transactions[entry]))
                pytest.fail(
                    'Transaction with amount %s is not found in list of %s, address is %s during %ss' %
                    (amount, additional_info, address, wait_time))
            else:
                self.log("Finding tx in %s, attempt #%s" % (additional_info, str(int(counter / 30) + 1)))
                try:
                    if token:
                        transactions = self.get_token_transactions(address)
                    else:
                        transactions = self.get_transactions(address)
                    counter += 30
                    time.sleep(30)
                except JSONDecodeError as e:
                    self.log("No valid JSON response from Etherscan: %s " % str(e))
                    continue
                try:
                    for transaction in transactions:
                        if float(int(transaction['value']) / 10 ** decimals) == float(amount):
                            self.log("Tx is found: %s (etherscan API)" % transaction['hash'])
                            return transaction
                except TypeError as e:
                    self.log("Failed iterate transactions(Etherscan unexpected error): " + str(e))
                    continue

    def wait_for_confirmation_of_transaction(self, address, amount, confirmations=6, token=False):
        start_time = time.time()
        if token:
            token_info = "token transaction"
        else:
            token_info = "ETH transaction"
        self.log('Waiting %s %s for %s to have %s confirmations' % (amount, token_info, address, confirmations))
        while round(time.time() - start_time, ndigits=2) < 600:  # should be < idleTimeout capability
            transaction = self.find_transaction_by_unique_amount(address, amount, token)
            self.log(
                'Expected amount of confirmations is %s, in fact %s' % (confirmations, transaction['confirmations']))
            if int(transaction['confirmations']) >= confirmations:
                return
            time.sleep(20)
        pytest.fail('Transaction with amount %s was not confirmed, address is %s, still has %s confirmations' % (
            amount, address, int(transaction['confirmations'])))

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

    def wait_for_balance_to_be(self, address: str, expected_balance: int, less: bool = True):
        for _ in range(5):
            balance = self.get_balance(address)
            if balance == expected_balance:
                return
            time.sleep(10)
        raise TimeoutException(
            'balance is not updated on Etherscan, it is %s but expected to be %s' % (balance, expected_balance))

    # Do not use until web3 update
    # def faucet(self, address):
    #     try:
    #         self.log("Trying to get funds from %s" % self.faucet_url)
    #         return requests.request('GET', '%s/0x%s' % (self.faucet_url, address)).json()
    #     except JSONDecodeError as e:
    #         self.log("No valid JSON response from Etherscan: %s " % str(e))
    #         pass

    # def faucet_backup(self, address):
    #     self.log("Trying to get funds from %s" % self.faucet_backup_address)
    #     address = "0x" + address
    #     w3.donate_testnet_eth(address=address, amount=0.01, inscrease_default_gas_price=10)

    # def get_donate(self, address, external_faucet=False, wait_time=300):
    #     initial_balance = self.get_balance(address)
    #     counter = 0
    #     if initial_balance < 1000000000000000000:
    #         if external_faucet:
    #             self.faucet_backup(address)
    #         else:
    #             self.faucet(address)
    #         while True:
    #             if counter >= wait_time:
    #                 pytest.fail("Donation was not received during %s seconds!" % wait_time)
    #             elif self.get_balance(address) == initial_balance:
    #                 counter += 10
    #                 time.sleep(10)
    #                 self.log('Waiting %s seconds for donation' % counter)
    #             else:
    #                 self.log('Got %s Gwei for %s' % (self.get_balance(address), address))
    #                 return

    def get_rounded_balance(self, fetched_balance, actual_balance):
        fetched_balance, actual_balance = str(fetched_balance), str(actual_balance)
        # get actual number of decimals on account balance
        decimals = abs(Decimal(fetched_balance).as_tuple().exponent)
        rounded_balance = round(float(actual_balance), decimals)
        return rounded_balance

    def get_tx_param_by_hash(self, transaction_hash: str, param: str):
        params = {'module': 'proxy', 'action': 'eth_getTransactionByHash', 'txhash': transaction_hash}
        res = self.send_etherscan_request(params)
        return int(res[param], 16)

    def get_custom_fee_tx_params(self, hash: str):
        return {
            'fee_cap': str(self.get_tx_param_by_hash(hash, 'maxFeePerGas') / 1000000000),
            'tip_cap': str(self.get_tx_param_by_hash(hash, 'maxPriorityFeePerGas') / 1000000000),
            'gas_limit': str(self.get_tx_param_by_hash(hash, 'gas'))
        }
