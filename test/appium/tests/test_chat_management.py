import time

import pytest

from tests import transaction_users
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
@pytest.mark.chat_management
class TestChatManagement(MultipleDeviceTestCase):

    @pytest.mark.testrail_case_id(3412)
    def test_delete_1_1_chat(self):
        self.senders['g_user'] = transaction_users['G_USER']
        self.senders['h_user'] = transaction_users['H_USER']

        self.create_drivers(2)
        device_1 = self.drivers[0]
        device_2 = self.drivers[1]

        device_1_sign_in_view = SignInView(device_1)
        device_1_sign_in_view.recover_access(self.senders['g_user']['passphrase'], self.senders['g_user']['password'])
        device_2_sign_in_view = SignInView(device_2)
        device_2_sign_in_view.recover_access(self.senders['h_user']['passphrase'], self.senders['h_user']['password'])

        device_1_home_view = device_1_sign_in_view.get_home_view()
        device_2_home_view = device_2_sign_in_view.get_home_view()

        # Device 1: Start new 1-1 chat
        device_1_home_view.add_contact(self.senders['h_user']['public_key'])
        device_1_chat_view = device_1_home_view.get_chat_view()
        chat_with_g_user = device_2_home_view.get_chat_with_user(self.senders['g_user']['username'])
        chat_with_g_user.wait_for_element(30)
        device_2_chat_view = chat_with_g_user.click()

        # Devices: Request and send transactions
        transaction_amount = '0.00001'
        device_1_chat_view.request_transaction_in_1_1_chat(transaction_amount)
        device_1_chat_view.send_transaction_in_1_1_chat(transaction_amount, self.senders['g_user']['password'])
        device_2_chat_view.request_transaction_in_1_1_chat(transaction_amount)
        device_2_chat_view.send_transaction_in_1_1_chat(transaction_amount, self.senders['h_user']['password'])

        # Device 1: Send message to device 2
        device_1_message = 'message from user 1'
        device_1_chat_view.chat_message_input.send_keys(device_1_message)
        device_1_chat_view.send_message_button.click()

        # Device 2: Send message to device 1
        device_2_message = 'message from user 2'
        device_2_chat_view = device_2_home_view.get_chat_view()
        device_2_chat_view.chat_message_input.send_keys(device_2_message)
        device_2_chat_view.send_message_button.click()

        # Device 1: See the message from device 2
        device_1_chat_view.wait_for_message_in_one_to_one_chat(device_2_message, self.errors)

        # Stop device 2, it's not needed anymore
        device_2.quit()

        # Device 1: Delete chat and make sure it does not reappear after logging in again
        device_1_chat_view.delete_chat(self.senders['h_user']['username'], self.errors)
        device_1_profile_view = device_1_sign_in_view.profile_button.click()
        device_1_sign_in_view = device_1_profile_view.logout()
        time.sleep(5) # Prevent stale element exception for first_account_button
        device_1_sign_in_view.account_button.click()
        device_1_sign_in_view.sign_in(self.senders['g_user']['password'])
        assert not device_1_home_view.get_chat_with_user(self.senders['h_user']['username']).is_element_present(20)

        # Device 1: Start 1-1 chat with device 2
        device_1_chat_view = device_1_home_view.start_1_1_chat(self.senders['h_user']['username'])
        assert device_1_chat_view.no_messages_in_chat.is_element_present()

        self.verify_no_errors()
