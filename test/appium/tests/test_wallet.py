from datetime import datetime

import pytest
from tests.basetestcase import SingleDeviceTestCase
from views.base_view import get_ethereum_price_in_usd, verify_transaction_in_ropsten
from views.home import HomeView
from tests.preconditions import set_password_as_new_user, recover_access
from tests import transaction_users


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
    @pytest.mark.parametrize("test, recipient, sender", [('sign_now', 'A_USER', 'B_USER'),
                                                         ('sign_later', 'B_USER', 'A_USER')],
                             ids=['sign_now', 'sign_later'])
    def test_send_transaction_from_wallet(self, test, recipient, sender):
        home = HomeView(self.driver)
        recover_access(home,
                       transaction_users[sender]['passphrase'],
                       transaction_users[sender]['password'],
                       transaction_users[sender]['username'])
        chats = home.get_chats()
        chats.wait_for_syncing_complete()

        recipient_key = transaction_users[recipient]['public_key']
        recipient_address = transaction_users[recipient]['address']
        initial_balance_recipient = chats.get_balance(recipient_address)

        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        for _ in range(2):
            chats.back_button.click()
        wallet = chats.wallet_button.click()
        wallet.send_button.click()
        wallet.amount_edit_box.click()
        amount = '0,0%s' % datetime.now().strftime('%-m%-d%-H%-M%-S')
        wallet.send_as_keyevent(amount)
        wallet.confirm()
        wallet.chose_recipient_button.click()
        wallet.deny_button.click()
        wallet.chose_from_contacts_button.click()
        user_contact = chats.element_by_text(transaction_users[recipient]['username'], 'button')
        user_contact.click()

        if test == 'sign_later':
            chats.sign_later_button.click()
            wallet.yes_button.click()
            wallet.ok_button_apk.click()
            tr_view = wallet.transactions_button.click()
            tr_view.unsigned_tab.click()
            tr_view.sign_button.click()

        chats.sign_transaction_button.click()
        chats.enter_password_input.send_keys(transaction_users[sender]['password'])
        chats.sign_transaction_button.click()
        chats.got_it_button.click()
        chats.verify_balance_is_updated(initial_balance_recipient, recipient_address)
        if test == 'sign_later':
            tr_view.history_tab.click()
        else:
            chats.wallet_button.click()
            tr_view = wallet.transactions_button.click()
        transaction = tr_view.transactions_table.find_transaction(amount=amount.replace(',', '.'))
        details_view = transaction.click()
        transaction_hash = details_view.get_transaction_hash()
        verify_transaction_in_ropsten(address=transaction_users[sender]['address'], transaction_hash=transaction_hash)

    @pytest.mark.wallet
    def test_balance_and_eth_rate(self):
        errors = list()
        home = HomeView(self.driver)
        recover_access(home,
                       passphrase=transaction_users['A_USER']['passphrase'],
                       password=transaction_users['A_USER']['password'],
                       username=transaction_users['A_USER']['username'])
        chats = home.get_chats()
        address = transaction_users['A_USER']['address']
        balance = chats.get_balance(address) / 1000000000000000000
        eth_rate = get_ethereum_price_in_usd()
        wallet = chats.wallet_button.click()
        wallet_balance = wallet.get_eth_value()
        if wallet_balance != balance:
            errors.append('Balance %s is not equal to the expected %s' % (wallet_balance, balance))
        wallet.verify_eth_rate(eth_rate, errors)
        assert not errors, 'errors occurred:\n{}'.format('\n'.join(errors))
