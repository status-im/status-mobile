import pytest
from selenium.common.exceptions import TimeoutException
from tests import api_requests, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestWallet(SingleDeviceTestCase):

    @pytest.mark.wallet
    @pytest.mark.testrail_case_id(3425)
    def test_wallet_error_messages(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()

        # Check valid amount
        invalid_amount = 'asd'
        error_message = 'Amount is not a valid number'
        send_transaction.amount_edit_box.send_keys(invalid_amount)
        try:
            send_transaction.find_full_text(error_message)
        except TimeoutException:
            self.errors.append(error_message + ' error did not appear')
        send_transaction.amount_edit_box.clear()

        # Check insufficient funds
        send_transaction.amount_edit_box.send_keys('100000')
        send_transaction.find_full_text('Insufficient funds')
        send_transaction.amount_edit_box.clear()

        # Check invalid address
        incorrect_public_key = '5261ceba31e3a7204b498b2dd20220a6057738d'
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(incorrect_public_key)
        send_transaction.done_button.click()
        error_message = 'Invalid address'
        try:
            send_transaction.error_dialog.wait_for_error_message(error_message)
        except TimeoutException:
            self.errors.append(error_message + ' error did not appear')
        send_transaction.error_dialog.ok_button.click()

        # Check wrong password
        incorrect_password = 'wrongpasswd'
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value('0.00001')
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(incorrect_password)
        send_transaction.sign_transaction_button.click()
        error_message = 'Wrong password'
        try:
            send_transaction.find_full_text(error_message)
        except TimeoutException:
            self.errors.append(error_message + ' error did not appear')

        self.verify_no_errors()

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
