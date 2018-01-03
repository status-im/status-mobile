import pytest
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from tests import api_requests, user_flow, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase
from views.console_view import ConsoleView


@pytest.mark.all
class TestWallet(SingleDeviceTestCase):

    @pytest.mark.wallet
    def test_wallet_error_messages(self):
        console = ConsoleView(self.driver)
        user_flow.create_user(console)
        chats = console.get_chat_view()
        chats.back_button.click()
        wallet_view = chats.wallet_button.click()
        wallet_view.send_button.click()
        send_transaction = wallet_view.get_send_transaction_view()
        send_transaction.amount_edit_box.send_keys('asd')
        send_transaction.find_full_text('Amount is not a valid number')
        send_transaction.amount_edit_box.send_keys('0,1')
        send_transaction.find_full_text('Insufficient funds')

    @pytest.mark.wallet
    def test_request_transaction_from_wallet(self):
        console_view = ConsoleView(self.driver)
        user_flow.recover_access(console_view,
                                 transaction_users_wallet['A_USER']['passphrase'],
                                 transaction_users_wallet['A_USER']['password'],
                                 transaction_users_wallet['A_USER']['username'])
        chats_view = console_view.get_chat_view()
        recipient_key = transaction_users_wallet['B_USER']['public_key']
        user_flow.add_contact(chats_view, recipient_key)
        for _ in range(3):
            try:
                chats_view.back_button.click()
            except(TimeoutException, NoSuchElementException):
                pass
        wallet_view = chats_view.wallet_button.click()
        wallet_view.request_button.click()
        send_transaction_view = wallet_view.get_send_transaction_view()
        send_transaction_view.amount_edit_box.scroll_to_element()
        send_transaction_view.amount_edit_box.send_keys('0.1')
        wallet_view.send_request_button.click()
        user_contact = chats_view.element_by_text(transaction_users_wallet['B_USER']['username'], 'button')
        user_contact.click()
        chats_view.find_text_part('Requesting  0.1 ETH')

    @pytest.mark.parametrize("test, recipient, sender", [('sign_now', 'A_USER', 'B_USER'),
                                                         ('sign_later', 'B_USER', 'A_USER')],
                             ids=['sign_now',
                                  'sign_later'])
    def test_send_transaction_from_wallet(self, test, recipient, sender):
        console_view = ConsoleView(self.driver)
        user_flow.recover_access(console_view,
                                 transaction_users_wallet[sender]['passphrase'],
                                 transaction_users_wallet[sender]['password'],
                                 transaction_users_wallet[sender]['username'])
        chats_view = console_view.get_chat_view()
        recipient_key = transaction_users_wallet[recipient]['public_key']
        recipient_address = transaction_users_wallet[recipient]['address']
        initial_balance_recipient = api_requests.get_balance(recipient_address)
        user_flow.add_contact(chats_view, recipient_key)
        for _ in range(3):
            try:
                chats_view.back_button.click()
            except(TimeoutException, NoSuchElementException):
                pass
        wallet_view = chats_view.wallet_button.click()
        wallet_view.send_button.click()
        send_transaction = wallet_view.get_send_transaction_view()
        send_transaction.amount_edit_box.click()
        amount = send_transaction.get_unique_amount()
        send_transaction.send_as_keyevent(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.deny_button.click()
        send_transaction.chose_from_contacts_button.click()
        user_contact = send_transaction.element_by_text(transaction_users_wallet[recipient]['username'], 'button')
        user_contact.click()
        if test == 'sign_later':
            send_transaction.sign_later_button.click()
            send_transaction.yes_button.click()
            send_transaction.ok_button_apk.click()
            transactions_view = wallet_view.transactions_button.click()
            transactions_view.unsigned_tab.click()
            transactions_view.sign_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(transaction_users_wallet[sender]['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient_address)
        if test == 'sign_later':
            transactions_view.history_tab.click()
        else:
            chats_view.wallet_button.click()
            transactions_view = wallet_view.transactions_button.click()
        transaction = transactions_view.transactions_table.find_transaction(amount=amount)
        details_view = transaction.click()
        transaction_hash = details_view.get_transaction_hash()
        api_requests.find_transaction_on_ropsten(address=transaction_users_wallet[sender]['address'],
                                                 transaction_hash=transaction_hash)

    @pytest.mark.wallet
    def test_eth_and_currency_balance(self):
        errors = list()
        console = ConsoleView(self.driver)
        user_flow.recover_access(console,
                                 passphrase=transaction_users_wallet['A_USER']['passphrase'],
                                 password=transaction_users_wallet['A_USER']['password'],
                                 username=transaction_users_wallet['A_USER']['username'])
        chats_view = console.get_chat_view()
        wallet = chats_view.wallet_button.click()
        address = transaction_users_wallet['A_USER']['address']
        balance = api_requests.get_balance(address) / 1000000000000000000
        eth_rate = api_requests.get_ethereum_price_in_usd()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_currency_balance(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))
