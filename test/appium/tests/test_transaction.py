import pytest
import time
from views.console_view import ConsoleView
from tests.base_test_case import SingleDeviceTestCase
from tests import user_flow, transaction_users, api_requests, get_current_time
from selenium.common.exceptions import TimeoutException, NoSuchElementException


@pytest.mark.all
class TestTransactions(SingleDeviceTestCase):

    @pytest.mark.transaction
    @pytest.mark.parametrize("test, recipient", [('group_chat', 'A_USER'),
                                                         ('one_to_one_chat', 'B_USER'),
                                                         ('wrong_password', 'A_USER')],
                             ids=['group_chat',
                                  'one_to_one_chat',
                                  'wrong_password'])
    def test_transaction_send_command(self, test, recipient):
        console_view = ConsoleView(self.driver)
        user_flow.create_user(console_view)
        console_view.back_button.click()
        chats_view = console_view.get_chat_view()
        recipient_address = transaction_users[recipient]['address']
        recipient_key = transaction_users[recipient]['public_key']
        transaction_amount = '0.001'
        sender_address = user_flow.get_address(chats_view)
        chats_view.back_button.click()
        api_requests.get_donate(sender_address)
        initial_balance_recipient = api_requests.get_balance(recipient_address)

        # next 2 lines are bypassing issue #2417
        wallet_view = chats_view.wallet_button.click()
        wallet_view.chats_button.click()

        user_flow.add_contact(chats_view, recipient_key)
        if test == 'group_chat':
            for _ in range(3):
                chats_view.back_button.click()
            user_flow.create_group_chat(chats_view, transaction_users[recipient]['username'],
                                        'trg_%s' % get_current_time())
        else:
            chats_view.element_by_text(transaction_users[recipient]['username'], 'button').click()
        chats_view.send_command.click()
        if test == 'group_chat':
            chats_view.first_recipient_button.click()
            chats_view.send_as_keyevent(transaction_amount)
        else:
            chats_view.send_as_keyevent(transaction_amount)
        chats_view.send_message_button.click()
        send_transaction_view = chats_view.get_send_transaction_view()
        send_transaction_view.sign_transaction_button.wait_for_element(5)
        send_transaction_view.sign_transaction_button.click()
        if test == 'wrong_password':
            send_transaction_view.enter_password_input.send_keys('invalid')
            send_transaction_view.sign_transaction_button.click()
            send_transaction_view.find_full_text('Wrong password', 20)
        else:
            send_transaction_view.enter_password_input.send_keys('qwerty1234')
            send_transaction_view.sign_transaction_button.click()
            send_transaction_view.got_it_button.click()
            send_transaction_view.find_full_text(transaction_amount)
            try:
                chats_view.find_full_text('Sent', 10)
            except TimeoutException:
                chats_view.find_full_text('Delivered', 10)
            if test == 'group_chat':
                chats_view.find_full_text('to  ' + transaction_users[recipient]['username'], 60)
            api_requests.verify_balance_is_updated(initial_balance_recipient, recipient_address)

    @pytest.mark.transaction
    def test_send_transaction_from_daap(self):
        console = ConsoleView(self.driver)
        user_flow.recover_access(console,
                                 transaction_users['B_USER']['passphrase'],
                                 transaction_users['B_USER']['password'],
                                 transaction_users['B_USER']['username'])
        chats_view = console.get_chat_view()
        address = transaction_users['B_USER']['address']
        initial_balance = api_requests.get_balance(address)
        contacts_view = chats_view.contacts_button.click()
        auction_house = contacts_view.auction_house_button.click()
        auction_house.toggle_navigation_button.click()
        auction_house.new_auction_button.click()
        auction_house.name_to_reserve_input.click()
        auction_name = time.strftime('%Y-%m-%d-%H-%M')
        auction_house.send_as_keyevent(auction_name)
        auction_house.register_name_button.click()
        send_transaction_view = chats_view.get_send_transaction_view()
        send_transaction_view.sign_transaction_button.wait_for_element(20)
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.enter_password_input.send_keys(transaction_users['B_USER']['password'])
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.got_it_button.click()
        auction_house.find_full_text('You are the proud owner of the name: ' + auction_name, 120)
        api_requests.verify_balance_is_updated(initial_balance, address)
