import random
import string

import pytest

from tests import marks, get_current_time
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.chat
class TestMessagesOneToOneChat(MultipleDeviceTestCase):

    @marks.skip
    @marks.testrail_case_id(764)
    def test_text_message_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for sign_in in device_1, device_2:
            sign_in.create_user()
        device_1_home, device_2_home = device_1.get_home_view(), device_2.get_home_view()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_2_home.element_by_text(message, 'button').click()

        if device_1_chat.chat_element_by_text(message).status.text != 'Seen':
            pytest.fail("'Seen' status is shown under the sent text message")

    @marks.skip
    @marks.testrail_case_id(772)
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        sign_in_1.create_user()
        username_2 = sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        public_key_1 = home_1.get_public_key()
        home_1.home_button.click()

        device_1.set_network_connection(1)  # airplane mode on primary device

        chat_2 = home_2.add_contact(public_key_1)
        message_1 = 'test message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        device_2.set_network_connection(1)  # airplane mode on secondary device

        device_1.set_network_connection(2)  # turning on WiFi connection on primary device

        chat_element = home_1.get_chat_with_user(username_2)
        chat_element.wait_for_visibility_of_element(20)
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(2)

        device_2.set_network_connection(2)  # turning on WiFi connection on secondary device
        device_1.set_network_connection(1)  # airplane mode on primary device

        chat_2.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(60)
        message_2 = 'one more message'
        chat_2.chat_message_input.send_keys(message_2)
        chat_2.send_message_button.click()

        device_1.set_network_connection(2)  # turning on WiFi connection on primary device

        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)

    @marks.testrail_case_id(3741)
    def test_resend_message_offline(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_1 = 'user_%s' % get_current_time()
        sign_in_1.create_user(username_1)
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        device_1.set_network_connection(1)  # airplane mode on primary device

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        progress_time = chat_1.chat_element_by_text(message).progress_bar.measure_time_while_element_is_shown()
        if not 9 < progress_time < 15:
            self.errors.append('Progress indicator is shown during %s seconds' % progress_time)

        device_1.set_network_connection(2)  # turning on WiFi connection

        chat_1.element_by_text('Not sent. Tap for options').click()
        if not chat_1.element_by_text('Delete message').is_element_displayed():
            self.errors.append("'Delete message' button is not shown for not sent message")

        chat_1.connection_status.wait_for_invisibility_of_element(60)
        chat_1.element_by_text('Resend').click()
        if chat_1.chat_element_by_text(message).status.text != 'Sent':
            self.errors.append("Message status is not 'Sent' after resending the message")

        chat_2 = home_2.get_chat_with_user(username_1).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed(10):
            self.errors.append("Message with text '%s' is not received" % message)

        self.verify_no_errors()

    @marks.testrail_case_id(3743)
    def test_messaging_in_different_networks(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_1 = 'user_%s' % get_current_time()
        sign_in_1.create_user(username_1)
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        public_key_2 = home_2.get_public_key()
        profile_2 = home_2.get_profile_view()
        profile_2.switch_network('Mainnet with upstream RPC')
        sign_in_2.click_account_by_position(0)
        sign_in_2.sign_in()

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

        public_chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        chat_1.get_back_to_home_view()
        home_1.join_public_chat(public_chat_name)
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(public_chat_name)

        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
