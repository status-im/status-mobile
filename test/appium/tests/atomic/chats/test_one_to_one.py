import random
import string

import emoji
import pytest
from selenium.common.exceptions import TimeoutException

from tests import marks, get_current_time, group_chat_users
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.chat
class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(764)
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

    @marks.testrail_id(772)
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_2 = 'user_2'
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user(username=username_2)
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
    @marks.testrail_id(3701)
    def test_resend_message_offline(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_1 = 'user_%s' % get_current_time()
        home_1, home_2 = sign_in_1.create_user(username_1), sign_in_2.create_user()
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        device_1.set_network_connection(1)  # airplane mode on primary device

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        progress_time = chat_1.chat_element_by_text(message).progress_bar.measure_time_while_element_is_shown()
        if not 5 < progress_time < 30:
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
    @marks.testrail_id(3710)
    def test_messaging_in_different_networks(self):
        self.create_drivers(2)
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

    @marks.testrail_id(1386)
    def test_send_message_to_newly_added_contact(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()

        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user()

        profile_1 = device_1_home.profile_button.click()
        file_name = 'sauce_logo.png'
        profile_1.edit_profile_picture(file_name)
        profile_1.home_button.click()

        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat_with_user(username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.erros.append("Message with test '%s' was not received" % message)
        if not device_2_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts button is not shown')
        if device_2_chat.user_name_text.text != username_1:
            self.errors.append("Real username '%s' is not shown in one-to-one chat" % username_1)
        device_2_chat.chat_options.click()
        device_2_chat.view_profile_button.click()
        if not device_2_chat.contact_profile_picture.is_element_image_equals_template(file_name):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.verify_no_errors()

    @marks.testrail_id(1387)
    def test_add_to_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'

        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user(
            username=username_2)

        device_2_public_key = device_2_home.get_public_key()
        profile_2 = device_2_home.get_profile_view()
        file_name = 'sauce_logo.png'
        profile_2.edit_profile_picture(file_name)
        profile_2.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat_with_user(username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.erros.append("Message with test '%s' was not received" % message)
        device_2_chat.add_to_contacts.click()

        device_2_chat.get_back_to_home_view()
        start_new_chat = device_2_home.plus_button.click()
        start_new_chat.start_new_chat_button.click()
        if not start_new_chat.element_by_text(username_1).is_element_displayed():
            self.errors.append('%s is not added to contacts' % username_1)

        if device_1_chat.user_name_text.text != username_2:
            self.errors.append("Real username '%s' is not shown in one-to-one chat" % username_2)
        device_1_chat.chat_options.click()
        device_1_chat.view_profile_button.click()
        if not device_1_chat.contact_profile_picture.is_element_image_equals_template(file_name):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.verify_no_errors()

    @marks.testrail_id(1413)
    def test_send_and_open_links(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'

        home_1, home_2 = device_1.create_user(username=username_1), device_2.create_user(username=username_2)
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        chat_1 = home_1.add_contact(public_key_2)
        url_message = 'status.im'
        chat_1.chat_message_input.send_keys(url_message)
        chat_1.send_message_button.click()
        chat_1.get_back_to_home_view()
        home_2.connection_status.wait_for_invisibility_of_element(30)
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = chat_2.open_in_browser_button.click()
        try:
            web_view.find_full_text('Status, the Ethereum discovery tool.')
        except TimeoutException:
            self.errors.append('URL was not opened from 1-1 chat')
        web_view.back_to_home_button.click()
        chat_2.get_back_to_home_view()

        chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        home_1.join_public_chat(chat_name)
        home_2.join_public_chat(chat_name)
        chat_2.chat_message_input.send_keys(url_message)
        chat_2.send_message_button.click()
        chat_1.element_starts_with_text(url_message, 'button').click()
        web_view = chat_1.open_in_browser_button.click()
        try:
            web_view.find_full_text('Status, the Ethereum discovery tool.')
        except TimeoutException:
            self.errors.append('URL was not opened from 1-1 chat')
        self.verify_no_errors()

    @marks.testrail_id(1431)
    def test_offline_status(self):
        self.create_drivers(1, offline_mode=True)
        driver = self.drivers[0]
        sign_in = SignInView(driver)
        home = sign_in.create_user()

        driver.set_network_connection(1)  # airplane mode

        if home.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in home screen')

        chat = home.add_contact(group_chat_users['C_USER']['public_key'])
        if chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in 1-1 chat')
        chat.get_back_to_home_view()

        public_chat = home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        if public_chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in a public chat')
        self.verify_no_errors()


@marks.all
@marks.chat
class TestMessagesOneToOneChatSingle(SingleDeviceTestCase):

    @marks.testrail_id(1390)
    def test_copy_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        chat = sign_in.get_chat_view()
        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text).long_press_element()
        chat.element_by_text('Copy to clipboard').click()

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text:
            self.errors.append('Message text was not copied in a public chat')

        chat.get_back_to_home_view()
        home.add_contact(group_chat_users['A_USER']['public_key'])
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text).long_press_element()
        chat.element_by_text('Copy to clipboard').click()

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text:
            self.errors.append('Message text was not copied in 1-1 chat')
        self.verify_no_errors()

    @marks.testrail_id(1398)
    def test_delete_cut_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(group_chat_users['B_USER']['public_key'])

        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)

        message_input.delete_last_symbols(2)
        assert message_input.text == message_text[:-2]

        message_input.cut_text()

        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2] + ' ').wait_for_visibility_of_element(2)

    @marks.testrail_id(2106)
    def test_send_emoji(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        chat = sign_in.get_chat_view()
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in public chat')

        chat.get_back_to_home_view()
        home.add_contact(group_chat_users['C_USER']['public_key'])
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in 1-1 chat')
        self.verify_no_errors()
