import pytest
import time
from views.console_view import ConsoleView
from tests.base_test_case import SingleDeviceTestCase
from tests import transaction_users, api_requests, get_current_time
from selenium.common.exceptions import TimeoutException


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
        console_view.create_user()
        console_view.back_button.click()
        home_view = console_view.get_home_view()
        recipient_address = transaction_users[recipient]['address']
        recipient_key = transaction_users[recipient]['public_key']
        transaction_amount = '0.001'
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        api_requests.get_donate(sender_address)
        initial_balance_recipient = api_requests.get_balance(recipient_address)

        home_view.add_contact(recipient_key)
        if test == 'group_chat':
            home_view.back_button.click(times_to_click=3)
            home_view.create_group_chat([transaction_users[recipient]['username']],
                                        'trg_%s' % get_current_time())
            chat_view = home_view.get_chat_view()
        else:
            chat_view = home_view.get_chat_with_user(transaction_users[recipient]['username']).click()
        chat_view.send_command.click()
        if test == 'group_chat':
            chat_view.first_recipient_button.click()
            chat_view.send_as_keyevent(transaction_amount)
        else:
            chat_view.send_as_keyevent(transaction_amount)
        chat_view.send_message_button.click()
        send_transaction_view = chat_view.get_send_transaction_view()
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
                chat_view.find_full_text('Sent', 10)
            except TimeoutException:
                chat_view.find_full_text('Delivered', 10)
            if test == 'group_chat':
                chat_view.find_full_text('to  ' + transaction_users[recipient]['username'], 60)
            api_requests.verify_balance_is_updated(initial_balance_recipient, recipient_address)

    @pytest.mark.transaction
    def test_send_transaction_from_daap(self):
        console = ConsoleView(self.driver)
        console.recover_access(transaction_users['B_USER']['passphrase'],
                               transaction_users['B_USER']['password'],
                               transaction_users['B_USER']['username'])
        home_view = console.get_home_view()
        address = transaction_users['B_USER']['address']
        initial_balance = api_requests.get_balance(address)
        start_new_chat_view = home_view.plus_button.click()
        auction_house = start_new_chat_view.auction_house_button.click()
        auction_house.toggle_navigation_button.click()
        auction_house.new_auction_button.click()
        auction_house.name_to_reserve_input.click()
        auction_name = time.strftime('%Y-%m-%d-%H-%M')
        auction_house.send_as_keyevent(auction_name)
        auction_house.register_name_button.click()
        send_transaction_view = home_view.get_send_transaction_view()
        send_transaction_view.sign_transaction_button.wait_for_element(20)
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.enter_password_input.send_keys(transaction_users['B_USER']['password'])
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.got_it_button.click()
        auction_house.find_full_text('You are the proud owner of the name: ' + auction_name, 120)
        api_requests.verify_balance_is_updated(initial_balance, address)
