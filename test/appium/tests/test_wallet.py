import pytest

from apis.ropsten_api import get_balance, verify_balance_is_updated
from apis.third_party_apis import get_ethereum_price_in_usd
from tests.basetestcase import SingleDeviceTestCase
from views.base_view import verify_transaction_in_ropsten
from views.chats import get_unique_amount
from views.home import HomeView
from tests.preconditions import set_password_as_new_user, recover_access
from tests import transaction_users_wallet


@pytest.mark.all
class TestWallet(SingleDeviceTestCase):

    @pytest.mark.wallet
    def test_wallet_error_messages(self):
        home = HomeView(self.driver)
        set_password_as_new_user(home)
        chats = home.get_chats()
        chats.back_button.click()

        wallet_view = chats.wallet_button.click()
        wallet_view.send_button.click()
        wallet_view.amount_edit_box.send_keys('asd')
        wallet_view.find_full_text('Amount is not a valid number')
        wallet_view.amount_edit_box.send_keys('0,1')
        wallet_view.find_full_text('Insufficient funds')

    @pytest.mark.wallet
    def test_request_transaction_from_wallet(self):
        home = HomeView(self.driver)
        recover_access(home,
                       transaction_users_wallet['A_USER']['passphrase'],
                       transaction_users_wallet['A_USER']['password'],
                       transaction_users_wallet['A_USER']['username'])
        chats = home.get_chats()
        chats.wait_for_syncing_complete()

        recipient_key = transaction_users_wallet['B_USER']['public_key']

        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        for _ in range(3):
            chats.back_button.click()
        wallet = chats.wallet_button.click()
        wallet.request_button.click()
        wallet.amount_edit_box.scroll_to_element()
        wallet.amount_edit_box.send_keys('0.1')
        wallet.send_request_button.click()
        user_contact = chats.element_by_text(transaction_users_wallet['B_USER']['username'], 'button')
        user_contact.click()
        chats.find_text_part('Requesting  0.1 ETH')

    @pytest.mark.parametrize("test, recipient, sender", [('sign_now', 'A_USER', 'B_USER'),
                                                         ('sign_later', 'B_USER', 'A_USER')],
                             ids=['sign_now',
                                  'sign_later'])
    def test_send_transaction_from_wallet(self, test, recipient, sender):
        home = HomeView(self.driver)
        recover_access(home,
                       transaction_users_wallet[sender]['passphrase'],
                       transaction_users_wallet[sender]['password'],
                       transaction_users_wallet[sender]['username'])
        chats = home.get_chats()
        chats.wait_for_syncing_complete()

        recipient_key = transaction_users_wallet[recipient]['public_key']
        recipient_address = transaction_users_wallet[recipient]['address']
        initial_balance_recipient = get_balance(recipient_address)

        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        for _ in range(3):
            chats.back_button.click()
        wallet = chats.wallet_button.click()
        wallet.send_button.click()
        wallet.amount_edit_box.click()
        amount = get_unique_amount()
        wallet.send_as_keyevent(amount)
        wallet.confirm()
        wallet.chose_recipient_button.click()
        wallet.deny_button.click()
        wallet.chose_from_contacts_button.click()
        user_contact = chats.element_by_text(transaction_users_wallet[recipient]['username'], 'button')
        user_contact.click()

        if test == 'sign_later':
            chats.sign_later_button.click()
            wallet.yes_button.click()
            wallet.ok_button_apk.click()
            tr_view = wallet.transactions_button.click()
            tr_view.unsigned_tab.click()
            tr_view.sign_button.click()

        chats.sign_transaction_button.click()
        chats.enter_password_input.send_keys(transaction_users_wallet[sender]['password'])
        chats.sign_transaction_button.click()
        chats.got_it_button.click()
        verify_balance_is_updated(initial_balance_recipient, recipient_address)
        if test == 'sign_later':
            tr_view.history_tab.click()
        else:
            chats.wallet_button.click()
            tr_view = wallet.transactions_button.click()
        transaction = tr_view.transactions_table.find_transaction(amount=amount)
        details_view = transaction.click()
        transaction_hash = details_view.get_transaction_hash()
        verify_transaction_in_ropsten(address=transaction_users_wallet[sender]['address'],
                                      transaction_hash=transaction_hash)

    @pytest.mark.wallet
    def test_eth_and_currency_balance(self):
        errors = list()
        home = HomeView(self.driver)
        recover_access(home,
                       passphrase=transaction_users_wallet['A_USER']['passphrase'],
                       password=transaction_users_wallet['A_USER']['password'],
                       username=transaction_users_wallet['A_USER']['username'])
        chats = home.get_chats()
        address = transaction_users_wallet['A_USER']['address']
        balance = get_balance(address) / 1000000000000000000
        wallet = chats.wallet_button.click()
        eth_rate = get_ethereum_price_in_usd()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_currency_balance(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))
