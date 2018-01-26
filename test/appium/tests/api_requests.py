import pytest
import requests
import time
from tests import info


def get_transactions(address: str) -> dict:
    url = 'http://ropsten.etherscan.io/api?module=account&action=txlist&address=0x%s&sort=desc' % address
    return requests.request('GET', url=url).json()['result']


def is_transaction_successful(transaction_hash: str) -> int:
    url = "https://ropsten.etherscan.io/api?module=transaction&action=getstatus&txhash=%s" % transaction_hash
    return not int(requests.request('GET', url=url).json()['result']['isError'])


def get_balance(address):
    url = 'http://ropsten.etherscan.io/api?module=account&action=balance&address=0x%s&tag=latest' % address
    for i in range(5):
        try:
            return int(requests.request('GET', url).json()["result"])
        except ValueError:
            pass


def find_transaction_on_ropsten(address: str, transaction_hash: str):
    transactions = get_transactions(address=address)
    for transaction in transactions:
        if transaction['hash'] == transaction_hash:
            info('Transaction is found in Ropsten network')
            return
    pytest.fail('Transaction is not found in Ropsten network')


def verify_balance_is_updated(initial_balance, recipient_address, wait_time=240):
    counter = 0
    while True:
        if counter >= wait_time:
            pytest.fail('Balance is not changed during %s seconds, funds were not received!' % wait_time)
        elif initial_balance == get_balance(recipient_address):
            counter += 10
            time.sleep(10)
            info('Waiting %s seconds for funds' % counter)
        else:
            info('Transaction is received')
            return


def get_donate(address, wait_time=300):
    initial_balance = get_balance(address)
    counter = 0
    if initial_balance < 1000000000000000000:
        response = requests.request('GET', 'http://51.15.45.169:3001/donate/0x%s' % address).json()
        while True:
            if counter >= wait_time:
                pytest.fail("Donation was not received during %s seconds!" % wait_time)
            elif get_balance(address) == initial_balance:
                counter += 10
                time.sleep(10)
                info('Waiting %s seconds for donation' % counter)
            else:
                info('Got %s for %s' % (response["amount_eth"], address))
                return


def get_ethereum_price_in_usd() -> float:
    url = 'https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD'
    return float(requests.request('GET', url).json()['USD'])
