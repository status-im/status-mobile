import pytest
import requests
import time
from tests import info


class NetworkApi:

    def __init__(self):
        self.url = 'http://api-ropsten.etherscan.io/api?'

    def get_transactions(self, address: str) -> dict:
        method = self.url + 'module=account&action=txlist&address=0x%s&sort=desc' % address
        return requests.request('GET', url=method).json()['result']

    def is_transaction_successful(self, transaction_hash: str) -> int:
        method = self.url + 'module=transaction&action=getstatus&txhash=%s' % transaction_hash
        return not int(requests.request('GET', url=method).json()['result']['isError'])

    def get_balance(self, address):
        method = self.url + 'module=account&action=balance&address=0x%s&tag=latest' % address
        for i in range(5):
            try:
                return int(requests.request('GET', method).json()["result"])
            except ValueError:
                pass

    def find_transaction_by_hash(self, address: str, transaction_hash: str):
        transactions = self.get_transactions(address=address)
        for transaction in transactions:
            if transaction['hash'] == transaction_hash:
                info('Transaction is found in Ropsten network')
                return
        pytest.fail('Transaction is not found in Ropsten network')

    def find_transaction_by_unique_amount(self, address, amount, wait_time=240):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail(
                    'Transaction with amount %s is not found in list of transactions, address is %s' %
                    (amount, address))
            else:
                counter += 10
                time.sleep(10)
                transactions = self.get_transactions(address=address)
                for transaction in transactions:
                    if int(float(amount) * 10 ** 18) == int(transaction['value']):
                        info('Transaction with unique amount %s is found in list of transactions, address is %s' %
                             (amount, address))
                        return

    def verify_balance_is_updated(self, initial_balance, recipient_address, wait_time=360):
        counter = 0
        while True:
            if counter >= wait_time:
                pytest.fail('Balance is not changed during %s seconds, funds were not received!' % wait_time)
            elif initial_balance == self.get_balance(recipient_address):
                counter += 10
                time.sleep(10)
                info('Waiting %s seconds for funds' % counter)
            else:
                info('Transaction is received')
                return

    def faucet(self, address):
        return requests.request('GET', 'http://51.15.45.169:3001/donate/0x%s' % address).json()

    def get_donate(self, address, wait_time=300):
        initial_balance = self.get_balance(address)
        counter = 0
        if initial_balance < 1000000000000000000:
            response = self.faucet(address)
            while True:
                if counter >= wait_time:
                    pytest.fail("Donation was not received during %s seconds!" % wait_time)
                elif self.get_balance(address) == initial_balance:
                    counter += 10
                    time.sleep(10)
                    info('Waiting %s seconds for donation' % counter)
                else:
                    info('Got %s for %s' % (response["amount_eth"], address))
                    return

    def get_ethereum_price_in_usd(self) -> float:
        url = 'https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD'
        return float(requests.request('GET', url).json()['USD'])
