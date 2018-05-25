from tests import transaction_users_wallet, marks
from selenium.common.exceptions import TimeoutException
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.wallet
class TestWallet(SingleDeviceTestCase):

    @marks.testrail_case_id(3425)
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

    def test_eth_and_currency_balance(self):
        errors = list()
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=transaction_users_wallet['A_USER']['passphrase'],
                                    password=transaction_users_wallet['A_USER']['password'])
        wallet = sign_in_view.wallet_button.click()
        address = transaction_users_wallet['A_USER']['address']
        balance = self.network_api.get_balance(address) / 1000000000000000000
        eth_rate = self.network_api.get_ethereum_price_in_usd()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_currency_balance(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))

    @marks.testrail_case_id(3453)
    def test_set_up_wallet(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        self.network_api.get_donate(sender_address)
        wallet_view = sign_in_view.wallet_button.click()
        sign_in_phrase = wallet_view.set_up_wallet()

        send_transaction = wallet_view.send_button.click()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        recipient_address = transaction_users_wallet['A_USER']['address']
        send_transaction.enter_recipient_address_input.set_value(recipient_address)
        send_transaction.done_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        assert send_transaction.sign_in_phrase_text.text == sign_in_phrase
