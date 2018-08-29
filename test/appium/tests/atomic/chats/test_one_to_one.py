import pytest
import random
import string
import emoji
from datetime import datetime
from selenium.common.exceptions import TimeoutException
from tests import marks, get_current_time, group_chat_users
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(764)
    @marks.smoke_1
    def test_text_message_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_2_chat = device_2_home.get_chat_with_user(username_1).click()
        device_2_chat.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(772)
    @marks.smoke_1
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2, offline_mode=True)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_2'
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user(username=username_2)
        public_key_1 = home_1.get_public_key()
        home_1.home_button.click()

        home_1.driver.set_network_connection(1)  # airplane mode on primary device

        chat_2 = home_2.add_contact(public_key_1)
        message_1 = 'test message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_2.driver.set_network_connection(1)  # airplane mode on secondary device

        home_1.driver.set_network_connection(2)  # turning on WiFi connection on primary device

        home_1.connection_status.wait_for_invisibility_of_element(20)
        chat_element = home_1.get_chat_with_user(username_2)
        chat_element.wait_for_visibility_of_element(20)
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(2)

        chat_2.driver.set_network_connection(2)  # turning on WiFi connection on secondary device
        home_1.driver.set_network_connection(1)  # airplane mode on primary device

        chat_2.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(60)
        message_2 = 'one more message'
        chat_2.chat_message_input.send_keys(message_2)
        chat_2.send_message_button.click()

        home_1.driver.set_network_connection(2)  # turning on WiFi connection on primary device

        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)

    @marks.smoke_1
    @marks.testrail_id(3701)
    def test_resend_message_offline(self):
        self.create_drivers(2, offline_mode=True)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        home_1, home_2 = sign_in_1.create_user(username_1), sign_in_2.create_user()
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        home_1.driver.set_network_connection(1)  # airplane mode on primary device

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        progress_time = chat_1.chat_element_by_text(message).progress_bar.measure_time_while_element_is_shown()
        if not 5 < progress_time < 30:
            self.errors.append('Progress indicator is shown during %s seconds' % progress_time)

        home_1.driver.set_network_connection(2)  # turning on WiFi connection
        chat_1.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(30)
        chat_1.element_by_text('Not sent. Tap for options').click()
        if not chat_1.element_by_text('Delete message').is_element_displayed():
            self.errors.append("'Delete message' button is not shown for not sent message")

        chat_element = chat_1.chat_element_by_text(message)
        chat_1.element_by_text('Resend').click()
        chat_element.status.wait_for_visibility_of_element()
        if chat_element.status.text != 'Sent':
            self.errors.append("Message status is not 'Sent' after resending the message")

        chat_2 = home_2.get_chat_with_user(username_1).click()
        if not chat_2.chat_element_by_text(message).is_element_displayed(10):
            self.errors.append("Message with text '%s' is not received" % message)

        self.verify_no_errors()

    @marks.testrail_id(3710)
    @marks.smoke_1
    def test_messaging_in_different_networks(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        home_1, home_2 = sign_in_1.create_user(username_1), sign_in_2.create_user()
        public_key_2 = home_2.get_public_key()
        profile_2 = home_2.get_profile_view()
        profile_2.switch_network('Mainnet with upstream RPC')
        sign_in_2.sign_in()

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

        public_chat_name = home_1.get_public_chat_name()
        chat_1.get_back_to_home_view()
        home_1.join_public_chat(public_chat_name)
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(public_chat_name)

        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(1386)
    @marks.smoke_1
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
            self.errors.append("Message with test '%s' was not received" % message)
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
    @marks.smoke_1
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
            self.errors.append("Message with text '%s' was not received" % message)
        device_2_chat.reconnect()
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
    @marks.smoke_1
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
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = chat_2.open_in_status_button.click()
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
        web_view = chat_1.open_in_status_button.click()
        try:
            web_view.find_full_text('Status, the Ethereum discovery tool.')
        except TimeoutException:
            self.errors.append('URL was not opened from 1-1 chat')
        self.verify_no_errors()

    @marks.testrail_id(1431)
    @marks.smoke_1
    def test_offline_status(self):
        self.create_drivers(1, offline_mode=True)
        sign_in = SignInView(self.drivers[0])
        home_view = sign_in.create_user()

        # Dismiss "Welcome to Status" placeholder.
        # When the placeholder is visible, the offline status bar does not appear
        wallet_view = home_view.wallet_button.click()
        wallet_view.home_button.click()

        sign_in.driver.set_network_connection(1)  # airplane mode

        if home_view.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in home screen')

        chat = home_view.add_contact(group_chat_users['C_USER']['public_key'])
        if chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in 1-1 chat')
        chat.get_back_to_home_view()

        public_chat = home_view.join_public_chat(home_view.get_public_chat_name())
        if public_chat.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in a public chat')
        self.verify_no_errors()

    @marks.testrail_id(3695)
    @marks.smoke_1
    def test_message_marked_as_sent_and_seen_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = device_1.create_user(username=username_1), device_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'test message'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        if device_1_chat.chat_element_by_text(message).status.text != 'Sent':
            self.errors.append("'Sent' status is not shown under the sent text message")

        device_2_chat = device_2_home.get_chat_with_user(username_1).click()
        device_2_chat.chat_element_by_text(message).wait_for_visibility_of_element()

        if device_1_chat.chat_element_by_text(message).status.text != 'Seen':
            self.errors.append("'Seen' status is not shown under the text message which was read by a receiver")
        self.verify_no_errors()

    @marks.testrail_id(3784)
    def test_unread_messages_counter_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user(username=username_2)
        device_1_public_key = device_1_home.get_public_key()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        message = 'test message'
        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()

        if device_1_home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')

        chat_element = device_1_home.get_chat_with_user(username_2)
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')

        chat_element.click()
        device_1_home.get_back_to_home_view()

        if device_1_home.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')

        if chat_element.new_messages_counter.is_element_displayed():
            self.errors.append('New messages counter is shown on chat element for already seen message')
        self.verify_no_errors()

    @marks.testrail_id(1414)
    def test_bold_and_italic_text_in_messages(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_2 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user(username=username_2)
        device_1_public_key = device_1_home.get_public_key()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        bold_text = 'bold text'
        device_2_chat.chat_message_input.send_keys('*%s*' % bold_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in 1-1 chat for the sender')

        device_1_chat = device_1_home.get_chat_with_user(username_2).click()
        if not device_1_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in 1-1 chat for the recipient')

        italic_text = 'italic text'
        device_2_chat.chat_message_input.send_keys('~%s~' % italic_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the sender')

        if not device_1_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the recipient')

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_public_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys('*%s*' % bold_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(bold_text).is_element_displayed():
            self.errors.append('Bold text is not displayed in public chat for the recipient')

        device_2_chat.chat_message_input.send_keys('~%s~' % italic_text)
        device_2_chat.send_message_button.click()
        if not device_2_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(italic_text).is_element_displayed():
            self.errors.append('Italic text is not displayed in 1-1 chat for the recipient')

        self.verify_no_errors()

    @marks.skip
    @marks.testrail_id(2781)
    def test_timestamp_in_chats(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1 = 'user_%s' % get_current_time()
        device_1_home, device_2_home = sign_in_1.create_user(username=username_1), sign_in_2.create_user()
        device_2_public_key = device_2_home.get_public_key()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'test text'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        sent_time = datetime.strptime(device_1_chat.driver.device_time, '%a %b %d %H:%M:%S GMT %Y').strftime("%I:%M %p")
        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the sender')
        if device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the sender')

        device_2_chat = device_2_home.get_chat_with_user(username_1).click()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the recipient')
        if not device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in 1-1 chat for the recipient')

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_public_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()
        sent_time = datetime.strptime(device_2_chat.driver.device_time, '%a %b %d %H:%M:%S GMT %Y').strftime("%I:%M %p")
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the sender')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the recipient')
        if not device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in 1-1 chat for the recipient')

        self.verify_no_errors()


@marks.all
@marks.chat
class TestMessagesOneToOneChatSingle(SingleDeviceTestCase):

    @marks.testrail_id(1390)
    @marks.smoke_1
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
    @marks.smoke_1
    def test_delete_cut_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(group_chat_users['B_USER']['public_key'])

        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)

        message_input.delete_last_symbols(2)
        current_text = message_input.text
        if current_text != message_text[:-2]:
            pytest.fail("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        message_input.cut_text()

        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2] + ' ').wait_for_visibility_of_element(2)

    @marks.testrail_id(2106)
    @marks.smoke_1
    def test_send_emoji(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(home.get_public_chat_name())
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
