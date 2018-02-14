import pytest
from tests import api_requests, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from views.console_view import ConsoleView


@pytest.mark.all
class TestWallet(SingleDeviceTestCase):

    @pytest.mark.wallet
    def test_wallet_error_messages(self):
        console = ConsoleView(self.driver)
        console.create_user()
        console.back_button.click()
        wallet_view = console.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.send_keys('asd')
        send_transaction.find_full_text('Amount is not a valid number')
        send_transaction.amount_edit_box.send_keys('0,1')
        send_transaction.find_full_text('Insufficient funds')

    @pytest.mark.wallet
    def test_eth_and_currency_balance(self):
        errors = list()
        console = ConsoleView(self.driver)
        console.recover_access(passphrase=transaction_users_wallet['A_USER']['passphrase'],
                               password=transaction_users_wallet['A_USER']['password'],
                               username=transaction_users_wallet['A_USER']['username'])
        home_view = console.get_home_view()
        wallet = home_view.wallet_button.click()
        address = transaction_users_wallet['A_USER']['address']
        balance = api_requests.get_balance(address) / 1000000000000000000
        eth_rate = api_requests.get_ethereum_price_in_usd()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_currency_balance(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))
