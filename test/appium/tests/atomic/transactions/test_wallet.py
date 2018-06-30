import pytest
from selenium.common.exceptions import TimeoutException
from tests import transaction_users, get_current_time, transaction_users_wallet, marks, common_password
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.web_views.base_web_view import BaseWebView


@marks.transaction
class TestTransactionWallet(SingleDeviceTestCase):

    @marks.testrail_id(766)
    def test_send_eth_from_wallet_to_contact(self):
        recipient = transaction_users['F_USER']
        sender = transaction_users['E_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        recent_recipient = send_transaction.element_by_text(recipient['username'])
        send_transaction.recent_recipients_button.click_until_presence_of_element(recent_recipient)
        recent_recipient.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.click()
        send_transaction.send_as_keyevent(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(767)
    def test_send_eth_from_wallet_to_address(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.click()
        send_transaction.send_as_keyevent(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()
        self.network_api.find_transaction_by_unique_amount(sender['address'], transaction_amount)

    @marks.testrail_id(1430)
    def test_send_stt_from_wallet(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.select_asset_button.click_until_presence_of_element(send_transaction.stt_button)
        send_transaction.stt_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()

    @marks.testrail_id(2164)
    def test_transaction_wrong_password_wallet(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.click()
        send_transaction.enter_password_input.send_keys('wrong_password')
        send_transaction.sign_transaction_button.click()
        send_transaction.find_full_text('Wrong password', 20)

    @marks.testrail_id(1452)
    def test_transaction_appears_in_history(self):
        recipient = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        transaction_amount = home_view.get_unique_amount()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        self.network_api.get_donate(sender_address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_changed_on_wallet_screen()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(common_password)
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()
        self.network_api.find_transaction_by_unique_amount(recipient['address'], transaction_amount)
        transactions_view = wallet_view.transaction_history_button.click()
        transactions_view.transactions_table.find_transaction(amount=transaction_amount)

    @marks.testrail_id(2163)
    def test_send_eth_from_wallet_incorrect_address(self):
        recipient = transaction_users['E_USER']
        sender = transaction_users['F_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        send_transaction = wallet_view.send_transaction_button.click()
        send_transaction.amount_edit_box.click()
        transaction_amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(transaction_amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['public_key'])
        send_transaction.done_button.click()
        send_transaction.find_text_part('Invalid address:', 20)
