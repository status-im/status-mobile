import pytest
from tests import api_requests, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestWallet(SingleDeviceTestCase):

    @pytest.mark.wallet
    def test_wallet_error_messages(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        wallet_view = sign_in_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.send_keys('asd')
        send_transaction.find_full_text('Amount is not a valid number')
        send_transaction.amount_edit_box.send_keys('0,1')
        send_transaction.find_full_text('Insufficient funds')

    @pytest.mark.wallet
    def test_eth_and_currency_balance(self):
        errors = list()
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=transaction_users_wallet['A_USER']['passphrase'],
                                    password=transaction_users_wallet['A_USER']['password'])
        wallet = sign_in_view.wallet_button.click()
        address = transaction_users_wallet['A_USER']['address']
        balance = api_requests.get_balance(address) / 1000000000000000000
        eth_rate = api_requests.get_ethereum_price_in_usd()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_currency_balance(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))
