import pytest

from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests import transaction_users, api_requests, get_current_time, transaction_users_wallet, marks
from selenium.common.exceptions import TimeoutException

import time

import pytest
from selenium.common.exceptions import TimeoutException

from tests import transaction_users, api_requests, get_current_time, transaction_users_wallet
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.web_views.base_web_view import BaseWebView


@marks.all
@marks.transaction
class TestTransaction(SingleDeviceTestCase):

    @marks.pr
    @marks.testrail_case_id(3401)
    def test_transaction_send_command_one_to_one_chat(self):
        recipient = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        transaction_amount = home_view.get_unique_amount()
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        api_requests.get_donate(sender_address)
        home_view.add_contact(recipient['public_key'])
        chat_view = home_view.get_chat_with_user(recipient['username']).click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        chat_view.send_transaction_in_1_1_chat(transaction_amount, 'qwerty1234')
        send_transaction_view = chat_view.get_send_transaction_view()
        send_transaction_view.back_button.click()
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        transactions_view = wallet_view.transactions_button.click()
        transactions_view.transactions_table.find_transaction(amount=transaction_amount)

    @marks.pr
    @marks.testrail_case_id(3402)
    def test_transaction_send_command_wrong_password(self):
        sender = transaction_users['A_USER']
        recipient = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        transaction_amount = '0.001'
        home_view.add_contact(recipient['public_key'])
        chat_view = home_view.get_chat_with_user(recipient['username']).click()
        chat_view.commands_button.click()
        chat_view.send_command.click()
        chat_view.send_as_keyevent(transaction_amount)
        send_transaction_view = chat_view.get_send_transaction_view()
        chat_view.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys('wrong_password')
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.find_full_text('Wrong password', 20)

    @marks.pr
    @marks.testrail_case_id(3403)
    def test_transaction_send_command_group_chat(self):
        recipient = transaction_users['A_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        transaction_amount = '0.001'
        sender_public_key = home_view.get_public_key()
        sender_address = home_view.public_key_to_address(sender_public_key)
        home_view.home_button.click()
        api_requests.get_donate(sender_address)
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        home_view.create_group_chat([recipient['username']], 'trg_%s' % get_current_time())
        chat_view = home_view.get_chat_view()
        initial_recipient_balance = api_requests.get_balance(recipient['address'])
        chat_view.send_transaction_in_group_chat(transaction_amount, 'qwerty1234', recipient)
        api_requests.verify_balance_is_updated(initial_recipient_balance, recipient['address'])

    @marks.pr
    @marks.testrail_case_id(3404)
    def test_send_transaction_from_daap(self):
        sender = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        address = transaction_users['B_USER']['address']
        initial_balance = api_requests.get_balance(address)
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.debug_mode_toggle.click()
        home_view = profile_view.home_button.click()
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.open_d_app_button.click()
        start_new_chat_view.simple_dapp_button.scroll_to_element()
        simple_dapp = start_new_chat_view.simple_dapp_button.click()
        start_new_chat_view.open_button.click()

        simple_dapp.wait_for_d_aap_to_load()
        simple_dapp.assets_button.click()
        simple_dapp.request_stt_button.click()

        send_transaction_view = home_view.get_send_transaction_view()
        send_transaction_view.sign_transaction(sender['password'])

        api_requests.verify_balance_is_updated(initial_balance, address)

    @pytest.mark.transactions
    @pytest.mark.testrail_case_id(3422)
    def test_open_transaction_on_etherscan(self):
        user = transaction_users['A_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(user['passphrase'], user['password'])
        home_view = sign_in_view.get_home_view()
        wallet_view = home_view.wallet_button.click()
        transactions_view = wallet_view.transactions_button.click()
        transaction_details = transactions_view.transactions_table.get_first_transaction().click()
        transaction_hash = transaction_details.get_transaction_hash()
        transaction_details.options_button.click()
        transaction_details.open_transaction_on_etherscan_button.click()
        base_web_view = BaseWebView(self.driver)
        base_web_view.web_view_browser.click()
        base_web_view.always_button.click()
        base_web_view.find_text_part(transaction_hash)

    @pytest.mark.pr
    @pytest.mark.testrail_case_id(3406)
    def test_send_stt_from_wallet_via_enter_recipient_address(self):
        sender = transaction_users_wallet['A_USER']
        recipient = transaction_users_wallet['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
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

    @marks.pr
    @marks.testrail_case_id(3407)
    def test_send_eth_from_wallet_sign_now(self):
        recipient = transaction_users['F_USER']
        sender = transaction_users['E_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
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
        if sender['password'] in str(home_view.logcat):
            pytest.fail('Password in logcat!!!', pytrace=False)

    @marks.testrail_case_id(3452)
    def test_sign_transaction_twice(self):
        recipient = transaction_users['F_USER']
        sender = transaction_users['E_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(recipient['public_key'])
        home_view.get_back_to_home_view()
        wallet_view = home_view.wallet_button.click()
        send_transaction = wallet_view.send_button.click()
        send_transaction.amount_edit_box.click()
        send_transaction.amount_edit_box.set_value(send_transaction.get_unique_amount())
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.recent_recipients_button.click()
        recent_recipient = send_transaction.element_by_text(recipient['username'])
        send_transaction.recent_recipients_button.click_until_presence_of_element(recent_recipient)
        recent_recipient.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.send_keys(sender['password'])
        send_transaction.cancel_button.click()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.wait_for_visibility_of_element()
        send_transaction.sign_transaction_button.click()
        send_transaction.enter_password_input.wait_for_visibility_of_element()


@marks.all
@marks.transaction
class TestTransactions(MultipleDeviceTestCase):

    @marks.pr
    @marks.testrail_case_id(3408)
    def test_send_eth_to_request_in_group_chat(self):
        recipient = transaction_users['E_USER']
        sender = self.senders['f_user'] = transaction_users['F_USER']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'])
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
        device_1_chat.commands_button.click()
        device_1_chat.request_command.click()
        device_1_chat.first_recipient_button.click()
        device_1_chat.send_as_keyevent(amount)
        device_1_chat.send_message_button.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        request_button = device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button')
        device_2_chat.send_eth_to_request(request_button, sender['password'])
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])

    @marks.pr
    @marks.testrail_case_id(3409)
    def test_send_eth_to_request_in_one_to_one_chat(self):
        recipient = transaction_users['C_USER']
        sender = self.senders['d_user'] = transaction_users['D_USER']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'])
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
        try:
            one_to_one_chat_device_2.wait_for_visibility_of_element(120)
        except TimeoutException:
            device_1_chat.chat_message_input.send_keys('ping')
            device_1_chat.send_message_button.click()
        one_to_one_chat_device_2.click()
        device_1_chat.commands_button.click_until_presence_of_element(device_1_chat.request_command)
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

    @marks.pr
    @marks.testrail_case_id(3410)
    def test_send_eth_to_request_from_wallet(self):
        recipient = transaction_users_wallet['D_USER']
        sender = self.senders['c_user'] = transaction_users['C_USER']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for user_details in (recipient, device_1), (sender, device_2):
            user_details[1].recover_access(passphrase=user_details[0]['passphrase'],
                                           password=user_details[0]['password'])
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
        one_to_one_chat_device_2.wait_for_visibility_of_element(120)
        one_to_one_chat_device_2.click()
        initial_balance_recipient = api_requests.get_balance(recipient['address'])
        request_button = device_2_chat.element_by_text_part('Requesting  %s ETH' % amount, 'button')
        device_2_chat.send_eth_to_request(request_button, sender['password'])
        api_requests.verify_balance_is_updated(initial_balance_recipient, recipient['address'])
