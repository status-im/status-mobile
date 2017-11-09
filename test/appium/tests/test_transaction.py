import pytest
import time

from apis.ropsten_api import verify_balance_is_updated, get_balance
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView
from tests.preconditions import recover_access
from tests import transaction_users
from selenium.common.exceptions import TimeoutException


@pytest.mark.all
class TestTransactions(SingleDeviceTestCase):

    @pytest.mark.transaction
    @pytest.mark.parametrize("test, recipient, sender", [('group_chat', 'A_USER', 'B_USER'),
                                                         ('one_to_one_chat', 'B_USER', 'A_USER'),
                                                         ('wrong_password', 'A_USER', 'B_USER')],
                             ids=['group_chat',
                                  'one_to_one_chat',
                                  'wrong_password'])
    def test_send_transaction(self, test, recipient, sender):
        home = HomeView(self.driver)
        recover_access(home,
                       transaction_users[sender]['passphrase'],
                       transaction_users[sender]['password'],
                       transaction_users[sender]['username'])
        chats = home.get_chats()
        chats.wait_for_syncing_complete()

        sender_address = transaction_users[sender]['address']
        recipient_address = transaction_users[recipient]['address']
        recipient_key = transaction_users[recipient]['public_key']
        initial_balance_recipient = get_balance(recipient_address)

        chats.plus_button.click()
        chats.add_new_contact.click()
        chats.public_key_edit_box.send_keys(recipient_key)
        chats.confirm()
        chats.confirm_public_key_button.click()

        if test == 'group_chat':
            user_name = chats.user_name_text.text
            for _ in range(2):
                chats.back_button.click()
            chats.new_group_chat_button.click()
            user_contact = chats.element_by_text(user_name, 'button')
            user_contact.scroll_to_element()
            user_contact.click()
            chats.next_button.click()
            chats.name_edit_box.send_keys('chat_send_transaction')
            chats.save_button.click()

        chats.send_funds_button.click()
        if test == 'group_chat':
            chats.first_recipient_button.click()
            chats.send_as_keyevent('0,1')
        else:
            chats.send_as_keyevent('0,1')
        chats.send_message_button.click()
        chats.sign_transaction_button.wait_for_element(20)
        chats.sign_transaction_button.click()

        if test == 'wrong_password':
            chats.enter_password_input.send_keys('invalid')
            chats.sign_transaction_button.click()
            chats.find_full_text('Wrong password', 20)

        else:
            chats.enter_password_input.send_keys(transaction_users[sender]['password'])
            chats.sign_transaction_button.click()
            chats.got_it_button.click()
            chats.find_full_text('0.1')
            try:
                chats.find_full_text('Sent', 10)
            except TimeoutException:
                chats.find_full_text('Delivered', 10)
            if test == 'group_chat':
                chats.find_full_text('to  ' + transaction_users[recipient]['username'], 60)
            verify_balance_is_updated(initial_balance_recipient, recipient_address)

    @pytest.mark.transaction
    def test_send_transaction_from_daap(self):
        home = HomeView(self.driver)
        recover_access(home,
                       transaction_users['B_USER']['passphrase'],
                       transaction_users['B_USER']['password'],
                       transaction_users['B_USER']['username'])
        chats = home.get_chats()

        address = transaction_users['B_USER']['address']
        initial_balance = get_balance(address)
        contacts = chats.contacts_button.click()
        auction_house = contacts.auction_house_button.click()

        auction_house.toggle_navigation_button.click()
        auction_house.new_auction_button.click()
        auction_house.name_to_reserve_input.click()
        auction_name = time.strftime('%Y-%m-%d-%H-%M')
        auction_house.send_as_keyevent(auction_name)
        auction_house.register_name_button.click()

        chats.sign_transaction_button.wait_for_element(20)
        chats.sign_transaction_button.click()
        chats.enter_password_input.send_keys(transaction_users['B_USER']['password'])
        chats.sign_transaction_button.click()
        chats.got_it_button.click()
        auction_house.find_full_text('You are the proud owner of the name: ' + auction_name, 120)
        verify_balance_is_updated(initial_balance, address)
