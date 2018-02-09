import pytest
import time
from views.console_view import ConsoleView
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests import transaction_users, api_requests, get_current_time, transaction_users_wallet
from selenium.common.exceptions import TimeoutException


@pytest.mark.all
class TestTransaction(SingleDeviceTestCase):

    @pytest.mark.pr
    def test_transaction_send_command_one_to_one_chat(self):
        recipient = transaction_users['B_USER']
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        home_view = console_view.get_home_view()
        transaction_amount = home_view.get_unique_amount()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        api_requests.get_donate(sender_address)
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        home_view.add_contact(recipient['public_key'])
        chat_view = home_view.get_chat_with_user(recipient['username']).click()
        chat_view.send_command.click()
        chat_view.send_as_keyevent(transaction_amount)
        send_transaction_view = chat_view.get_send_transaction_view()
        chat_view.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction('qwerty1234')
        send_transaction_view.find_full_text(transaction_amount)
        try:
            chat_view.find_full_text('Sent', 10)
        except TimeoutException:
            chat_view.find_full_text('Delivered', 10)
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        send_transaction_view.back_button.click()
        wallet_view = home_view.wallet_button.click()
        transactions_view = wallet_view.transactions_button.click()
        transactions_view.transactions_table.find_transaction(amount=transaction_amount)

    @pytest.mark.pr
    def test_transaction_send_command_wrong_password(self):
        sender = transaction_users['A_USER']
        recipient = transaction_users['B_USER']
        console_view = ConsoleView(self.driver)
        console_view.recover_access(sender['passphrase'], sender['password'], sender['username'])
        home_view = console_view.get_home_view()
        transaction_amount = '0.001'
        home_view.add_contact(recipient['public_key'])
        chat_view = home_view.get_chat_with_user(recipient['username']).click()
        chat_view.send_command.click()
        chat_view.send_as_keyevent(transaction_amount)
        send_transaction_view = chat_view.get_send_transaction_view()
        chat_view.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys('wrong_password')
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.find_full_text('Wrong password', 20)

    @pytest.mark.pr
    def test_transaction_send_command_group_chat(self):
        recipient = transaction_users['A_USER']
        console_view = ConsoleView(self.driver)
        console_view.create_user()
        console_view.back_button.click()
        home_view = console_view.get_home_view()
        transaction_amount = '0.001'
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        api_requests.get_donate(sender_address)
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        home_view.create_group_chat([recipient['username']], 'trg_%s' % get_current_time())
        chat_view = home_view.get_chat_view()
        chat_view.send_command.click()
        chat_view.first_recipient_button.click()
        chat_view.send_as_keyevent(transaction_amount)
        send_transaction_view = chat_view.get_send_transaction_view()
        chat_view.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction('qwerty1234')
        send_transaction_view.find_full_text(transaction_amount)
        chat_view.find_full_text('to  ' + recipient['username'], 10)
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])

    @pytest.mark.pr
    def test_send_transaction_from_daap(self):
        console = ConsoleView(self.driver)
        sender = transaction_users['B_USER']
        console.recover_access(sender['passphrase'],
                               sender['password'],
                               sender['username'])
        home_view = console.get_home_view()
        address = transaction_users['B_USER']['address']
        initial_balance = api_requests.get_balance(address)
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.open_d_app_button.click()
        auction_house = start_new_chat_view.auction_house_button.click()
        start_new_chat_view.open_button.click()
        auction_house.wait_for_d_aap_to_load()
        auction_house.toggle_navigation_button.click()
        auction_house.new_auction_button.click()
        auction_house.name_to_reserve_input.click()
        auction_name = time.strftime('%Y-%m-%d-%H-%M')
        auction_house.send_as_keyevent(auction_name)
        auction_house.register_name_button.click()
        send_transaction_view = home_view.get_send_transaction_view()
        send_transaction_view.sign_transaction(sender['password'])
        auction_house.find_full_text('You are the proud owner of the name: ' + auction_name, 120)
        api_requests.verify_balance_is_updated(initial_balance, address)

    @pytest.mark.pr
    def test_send_eth_from_wallet_sign_later(self):
        sender = transaction_users_wallet['B_USER']
        recipient = transaction_users_wallet['A_USER']
        console_view = ConsoleView(self.driver)
        console_view.recover_access(sender['passphrase'],
                                    sender['password'],
                                    sender['username'])
        home_view = console_view.get_home_view()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.click()
        amount = send_transaction.get_unique_amount()
        send_transaction.amount_edit_box.set_value(amount)
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_contact_code_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_later_button.click()
        send_transaction.yes_button.click()
        send_transaction.ok_button_apk.click()
        transactions_view = wallet_view.transactions_button.click()
        transactions_view.unsigned_tab.click()
        transactions_view.sign_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        transactions_view.history_tab.click()
        transaction = transactions_view.transactions_table.find_transaction(amount=amount)
        details_view = transaction.click()
        details_view.get_transaction_hash()

    @pytest.mark.pr
    def test_send_stt_from_wallet_via_enter_contact_code(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        console_view = ConsoleView(self.driver)
        console_view.recover_access(sender['passphrase'],
                                    sender['password'],
                                    sender['username'])
        home_view = console_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        wallet_view.options_button.click_until_presence_of_element(wallet_view.manage_assets_button)
        wallet_view.manage_assets_button.click()
        wallet_view.stt_check_box.click()
        wallet_view.done_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.select_asset_button.click_until_presence_of_element(send_transaction.stt_button)
        send_transaction.stt_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_contact_code_button.click()
        send_transaction.enter_recipient_address_input.set_value(recipient['address'])
        send_transaction.done_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()

    @pytest.mark.pr
    def test_send_eth_from_wallet_sign_now(self):
        sender = transaction_users_wallet['A_USER']
        console_view = ConsoleView(self.driver)
        console_view.recover_access(sender['passphrase'],
                                    sender['password'],
                                    sender['username'])
        home_view = console_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        recent_recipient = send_transaction.element_by_text('Jarrad')
        send_transaction.recent_recipients_button.click_until_presence_of_element(recent_recipient)
        recent_recipient.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.sign_transaction_button.click()
        send_transaction.got_it_button.click()


@pytest.mark.all
class TestTransactions(MultipleDeviceTestCase):

    @pytest.mark.pr
    def test_send_eth_to_request_in_group_chat(self):
        recipient = transaction_users['A_USER']
        sender = transaction_users['B_USER']
        self.create_drivers(2)
        device_1, device_2 = \
            ConsoleView(self.drivers[0]), ConsoleView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'],
                                           username=user_details[0]['username'])
        device_2_home = device_2.get_home_view()
        device_1_home = device_1.get_home_view()
        device_1_home.add_contact(sender['public_key'])
        device_1_home.get_back_to_home_view()
        group_chat_name = 'gtr_%s' % get_current_time()
        device_1_home.create_group_chat([sender['username']], group_chat_name)
        device_2_home.element_by_text(group_chat_name, 'button').click()
        device_1_chat = device_1_home.get_chat_view()
        device_2_chat = device_2_home.get_chat_view()
        amount = device_1_chat.get_unique_amount()
        device_1_chat.request_command.click()
        device_1_chat.first_recipient_button.click()
        device_1_chat.send_as_keyevent(amount)
        device_1_chat.send_message_button.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        request_button = device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button')
        device_2_chat.send_eth_to_request(request_button, sender['password'])
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])

    @pytest.mark.pr
    def test_send_eth_to_request_in_one_to_one_chat(self):
        recipient = transaction_users['B_USER']
        sender = transaction_users['A_USER']
        self.create_drivers(2)
        device_1, device_2 = \
            ConsoleView(self.drivers[0]), ConsoleView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'],
                                           username=user_details[0]['username'])
        device_2_home = device_2.get_home_view()
        device_1_home = device_1.get_home_view()
        device_1_home.add_contact(sender['public_key'])
        device_1_home.get_back_to_home_view()
        one_to_one_chat_device_1 = device_1_home.element_by_text_part(sender['username'][:25], 'button')
        one_to_one_chat_device_1.scroll_to_element()
        one_to_one_chat_device_1.click()
        device_1_chat = device_1_home.get_chat_view()
        device_2_chat = device_2_home.get_chat_view()
        amount = device_1_chat.get_unique_amount()
        one_to_one_chat_device_2 = device_2_chat.element_by_text_part(recipient['username'][:25], 'button')
        one_to_one_chat_device_2.click()
        device_1_chat.request_command.click()
        device_1_chat.send_as_keyevent(amount)
        device_1_chat.send_message_button.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        request_button = device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button')
        device_2_chat.send_eth_to_request(request_button, sender['password'])
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        device_2_chat.back_button.click()
        device_2_wallet = device_2_home.wallet_button.click()
        transactions_view = device_2_wallet.transactions_button.click()
        transactions_view.transactions_table.find_transaction(amount=amount)

    @pytest.mark.pr
    def test_send_eth_to_request_from_wallet(self):
        recipient = transaction_users_wallet['B_USER']
        sender = transaction_users_wallet['A_USER']
        self.create_drivers(2)
        device_1, device_2 = \
            ConsoleView(self.drivers[0]), ConsoleView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'],
                                           username=user_details[0]['username'])
        device_2_home = device_2.get_home_view()
        device_1_home = device_1.get_home_view()
        device_1_home.add_contact(sender['public_key'])
        device_1_home.get_back_to_home_view()
        wallet_view_device_1 = device_1_home.wallet_button.click()
        send_transaction_device_1 = wallet_view_device_1.request_button.click_until_presence_of_element(
            wallet_view_device_1.send_transaction_request)
        wallet_view_device_1.send_transaction_request.click()
        send_transaction_device_1.amount_edit_box.scroll_to_element()
        amount = device_1_home.get_unique_amount()
        send_transaction_device_1.amount_edit_box.set_value(amount)
        send_transaction_device_1.confirm()
        send_transaction_device_1.chose_recipient_button.click()
        sender_button = send_transaction_device_1.element_by_text(sender['username'])
        send_transaction_device_1.recent_recipients_button.click_until_presence_of_element(sender_button)
        sender_button.click()
        wallet_view_device_1.send_request_button.click()
        device_2_chat = device_2_home.get_chat_view()
        one_to_one_chat_device_2 = device_2_chat.element_by_text_part(recipient['username'][:25], 'button')
        one_to_one_chat_device_2.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        request_button = device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button')
        device_2_chat.send_eth_to_request(request_button, sender['password'])
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
