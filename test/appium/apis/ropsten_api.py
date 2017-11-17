import logging

import pytest
import requests
import time


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


def verify_balance_is_updated(initial_balance, recipient_address, wait_time=240):
    counter = 0
    while True:
        if counter == wait_time:
            pytest.fail('Balance is not changed during %s seconds, funds were not received!' % wait_time)
        elif initial_balance == get_balance(recipient_address):
            counter += 10
            time.sleep(10)
            logging.info('Waiting %s seconds for funds' % counter)
        else:
            logging.info('Transaction was received and verified on ropsten.etherscan.io')
            break
