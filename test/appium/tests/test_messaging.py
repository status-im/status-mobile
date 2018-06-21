import random
import string
import emoji
import time
from tests.base_test_case import MultipleDeviceTestCase
from tests import group_chat_users, get_current_time, marks
from views.sign_in_view import SignInView

unicode_text_message = '%s%s%s%s %s%s%s%s%s%s%s' % (chr(355), chr(275), chr(353), chr(539), chr(1084), chr(949),
                                                    chr(349), chr(353), chr(513), chr(485), chr(283))
unicode_chinese = '%s%s' % (chr(29320), chr(22909))
emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
emoji_name_1 = random.choice(list(emoji.EMOJI_UNICODE))
emoji_unicode_1 = emoji.EMOJI_UNICODE[emoji_name_1]
message_with_new_line = 'message' '\n' 'with new line'


@marks.all
@marks.chat
class TestMessages(MultipleDeviceTestCase):

    @marks.pr
    @marks.testrail_case_id(3390)
    def test_one_to_one_chat_messages(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for sign_in in device_1, device_2:
            sign_in.create_user()
        device_1_home, device_2_home = device_1.get_home_view(), device_2.get_home_view()
        device_2_public_key = device_2_home.get_public_key()
        device_1_home.add_contact(device_2_public_key)
        device_1_chat = device_1_home.get_chat_view()

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        device_2_home.home_button.click()
        device_2_home.element_by_text(message, 'button').click()
        device_2_chat = device_2_home.get_chat_view()

        device_2_chat.chat_message_input.send_keys('~abc~ !@#$%%^&(() *bold*')
        device_2_chat.send_message_button.click()
        device_1_chat.wait_for_message_in_one_to_one_chat('abc !@#$%%^&(() bold', self.errors)

        device_1_chat.chat_message_input.send_keys(unicode_text_message)
        device_1_chat.send_message_button.click()
        device_2_chat.wait_for_message_in_one_to_one_chat(unicode_text_message, self.errors)

        device_2_chat.chat_message_input.send_keys(unicode_chinese)
        device_2_chat.send_message_button.click()
        device_1_chat.wait_for_message_in_one_to_one_chat(unicode_chinese, self.errors)

        device_1_chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        device_1_chat.send_message_button.click()
        device_2_chat.wait_for_message_in_one_to_one_chat(emoji_unicode, self.errors)

        message_with_emoji = 'message with emoji'
        device_2_chat.chat_message_input.send_keys(emoji.emojize('%s %s' % (message_with_emoji, emoji_name_1)))
        device_2_chat.send_message_button.click()
        device_1_chat.wait_for_message_in_one_to_one_chat('%s %s' % (message_with_emoji, emoji_unicode_1), self.errors)

        device_1_chat.chat_message_input.click()
        device_1_chat.send_as_keyevent(message_with_new_line)
        device_1_chat.send_message_button.click()
        device_2_chat.wait_for_message_in_one_to_one_chat(message_with_new_line, self.errors)

        url_message = 'status.im'
        device_2_chat.chat_message_input.send_keys(url_message)
        device_2_chat.send_message_button.click()
        device_1_chat.wait_for_message_in_one_to_one_chat(url_message, self.errors)
        if device_1_chat.element_by_text(url_message, 'button').is_element_present():
            device_1_chat.element_by_text(url_message, 'button').click()
            web_view = device_1_chat.open_in_browser_button.click()
            web_view.find_full_text('Status, the Ethereum discovery tool.')
            device_1_chat.back_button.click()

        self.verify_no_errors()

    @marks.pr
    @marks.testrail_case_id(3391)
    @marks.skip
    def test_group_chat_messages_and_delete_chat(self):
        self.create_drivers(3)

        device_1, device_2, device_3 = \
            SignInView(self.drivers[0]), SignInView(self.drivers[1]), SignInView(self.drivers[2])

        for data in (device_1, group_chat_users['A_USER']), \
                    (device_2, group_chat_users['B_USER']), \
                    (device_3, group_chat_users['C_USER']):
            data[0].recover_access(data[1]['passphrase'], data[1]['password'])

        home_1, home_2, home_3 = device_1.get_home_view(), device_2.get_home_view(), device_3.get_home_view()

        public_key_2, public_key_3 = \
            group_chat_users['B_USER']['public_key'], \
            group_chat_users['C_USER']['public_key']

        username_1, username_2, username_3 = \
            group_chat_users['A_USER']['username'], \
            group_chat_users['B_USER']['username'], \
            group_chat_users['C_USER']['username']

        for public_key in public_key_2, public_key_3:
            home_1.add_contact(public_key)
            home_1.get_back_to_home_view()

        chat_name = 'a_chat_%s' % get_current_time()
        home_1.create_group_chat(sorted([username_2, username_3]), chat_name)
        chat_1 = home_1.get_chat_view()
        text_message = 'This is text message!'
        chat_1.chat_message_input.send_keys(text_message)
        chat_1.send_message_button.click()
        for home in home_2, home_3:
            home.element_by_text(chat_name, 'button').click()

        chat_2, chat_3 = home_2.get_chat_view(), home_3.get_chat_view()
        for chat in chat_2, chat_3:
            chat.wait_for_messages(username_1, text_message, self.errors)

        chat_2.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat_2.send_message_button.click()
        for chat in chat_1, chat_3:
            chat.wait_for_messages(username_2, emoji_unicode, self.errors)

        message_with_emoji = 'message with emoji'
        chat_3.chat_message_input.send_keys(emoji.emojize('%s %s' % (message_with_emoji, emoji_name_1)))
        chat_3.send_message_button.click()
        for chat in chat_1, chat_2:
            chat.wait_for_messages(username_3, '%s %s' % (message_with_emoji, emoji_unicode_1), self.errors)

        chat_1.chat_message_input.send_keys(unicode_text_message)
        chat_1.send_message_button.click()

        for chat in chat_2, chat_3:
            chat.wait_for_messages(username_1, unicode_text_message, self.errors)

        # for chat in chat_1, chat_2, chat_3:
        #     chat.delete_chat(chat_name, self.errors)

        self.verify_no_errors()

    @marks.pr
    @marks.testrail_case_id(3392)
    def test_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        users = []
        chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        for sign_in in device_1, device_2:
            users.append(sign_in.create_user())
            home = sign_in.get_home_view()
            home.join_public_chat(chat_name)
        chat_1, chat_2 = device_1.get_chat_view(), device_2.get_chat_view()

        chat_1.chat_message_input.send_keys('/command')
        chat_1.send_message_button.click()

        message_with_emoji = 'message with emoji'

        chat_2.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat_2.send_message_button.click()

        chat_1.chat_message_input.send_keys('%s %s' % (unicode_text_message, unicode_chinese))
        chat_1.send_message_button.click()

        chat_2.chat_message_input.send_keys('%s %s' % (message_with_emoji, emoji_unicode_1))
        chat_2.send_message_button.click()

        chat_1.chat_message_input.send_keys('This is text message.')
        chat_1.send_message_button.click()

        chat_2.chat_message_input.click()
        chat_2.send_as_keyevent(message_with_new_line)
        chat_2.send_message_button.click()

        messages_from_user_2 = [emoji_unicode, '%s %s' % (message_with_emoji, emoji_unicode_1), message_with_new_line]
        chat_1.wait_for_messages(users[1], messages_from_user_2, self.errors)

        messages_from_user_1 = ['/command', '%s %s' % (unicode_text_message, unicode_chinese), 'This is text message.']
        chat_2.wait_for_messages(users[0], messages_from_user_1, self.errors)

        for chat in chat_1, chat_2:
            chat.delete_chat(chat_name, self.errors)
        self.verify_no_errors()

    @marks.testrail_case_id(3423)
    def test_username_and_profile_picture_in_chats(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = device_1.create_user(), device_2.create_user()
        home_1, home_2 = device_1.get_home_view(), device_2.get_home_view()
        device_2_public_key = home_2.get_public_key()
        profile_2 = home_2.get_profile_view()
        file_name = 'sauce_logo.png'
        profile_2.edit_profile_picture(file_name)
        home_1.add_contact(device_2_public_key)
        chat_1 = home_1.get_chat_view()
        chat_1.chat_message_input.send_keys('ping')
        chat_1.send_message_button.click()
        profile_2.home_button.click()
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.add_to_contacts.click()
        time.sleep(3)
        if chat_1.user_name_text.text != username_2:
            self.errors.append("Real username '%s' is not shown in one-to-one chat" % username_2)
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        if not chat_1.contact_profile_picture.is_element_image_equals_template(file_name):
            self.errors.append("Updated profile picture is not shown in one-to-one chat")

        # home_1.get_back_to_home_view()
        # chat_name = 'a_chat_%s' % get_current_time()
        # home_1.create_group_chat([username_2], chat_name)
        # group_chat_1 = home_1.get_chat_view()
        #
        # home_2.get_back_to_home_view()
        # group_chat_2 = home_2.get_chat_with_user(chat_name).click()
        # message_text = 'test message'
        # group_chat_2.chat_message_input.send_keys(message_text)
        # group_chat_2.send_message_button.click()
        #
        # group_chat_1.wait_for_messages(username_2, message_text, self.errors)
        # group_chat_1.verify_username_is_shown_per_message(username_2, message_text, self.errors)

        self.verify_no_errors()

    @marks.testrail_case_id(3429)
    def test_copy_and_paste_messages(self):
        self.create_drivers(1)
        sign_in = SignInView(self.drivers[0])
        sign_in.create_user()
        home = sign_in.get_home_view()
        home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        chat = sign_in.get_chat_view()
        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)
        chat.send_message_button.click()

        # copy text message
        chat.chat_element_by_text(message_text).long_press_element()
        chat.element_by_text('Copy to clipboard').click()

        # paste text into the input field
        message_input.paste_text_from_clipboard()
        assert message_input.text == message_text

        # delete part of the message
        message_input.delete_last_symbols(2)
        assert message_input.text == message_text[:-2]

        # cut message text
        message_input.cut_text()

        # paste selected text
        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        # verify correct text is sent
        chat.chat_element_by_text(message_text[:-2] + ' ')


@marks.all
@marks.chat
class TestOfflineMessages(MultipleDeviceTestCase):

    @marks.testrail_case_id(3420)
    def test_offline_messaging_1_1_chat(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_1 = sign_in_1.create_user()
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        device_2_public_key = home_2.get_public_key()
        home_1.add_contact(device_2_public_key)
        chat_1 = home_1.get_chat_view()
        chat_1.chat_message_input.send_keys('ping')
        chat_1.send_message_button.click()
        home_2.home_button.click()
        home_2.get_chat_with_user(username_1).is_element_present(20)
        profile_2 = home_2.profile_button.click()
        profile_2.logout()
        device_2.set_network_connection(1)  # airplane mode
        message_text = 'test message'
        chat_1.chat_message_input.send_keys(message_text)
        chat_1.send_message_button.click()
        sign_in_2.click_account_by_position(0)
        sign_in_2.sign_in()
        sign_in_2.home_button.wait_for_visibility_of_element()
        if home_2.connection_status.text != 'Offline':
            self.errors.append('Offline label is not shown on Home view while being offline')
        chat_2 = home_2.get_chat_with_user(username_1).click()
        if chat_2.connection_status.text != 'Offline':
            self.errors.append('Offline label is not shown on Chat view while being offline')
        device_2.set_network_connection(2)  # turning on WiFi connection
        chat_2.wait_for_message_in_one_to_one_chat(message_text, self.errors, wait_time=120)
        self.verify_no_errors()

    @marks.testrail_case_id(3430)
    @marks.skip
    def test_offline_messaging_group_chat(self):
        self.create_drivers(2, offline_mode=True)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        username_1 = sign_in_1.create_user()
        username_2 = sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()

        device_2_public_key = home_2.get_public_key()
        home_1.add_contact(device_2_public_key)
        home_1.get_back_to_home_view()

        home_2.home_button.click()
        chat_view_2 = home_2.get_chat_with_user(username_1).click()
        chat_view_2.add_to_contacts.click()
        chat_view_2.get_back_to_home_view()

        chat_name = 'a_chat_%s' % get_current_time()
        home_1.create_group_chat([username_2], chat_name)
        chat_1 = home_1.get_chat_view()

        chat_element_2 = home_2.get_chat_with_user(chat_name)
        chat_element_2.wait_for_visibility_of_element()
        device_2.set_network_connection(1)  # airplane mode

        if home_2.connection_status.text != 'Offline':
            self.errors.append('Offline label is not shown on Home view while being offline')
        chat_2 = chat_element_2.click()
        if chat_2.connection_status.text != 'Offline':
            self.errors.append('Offline label is not shown on Chat view while being offline')

        message_text = 'test message'
        chat_1.chat_message_input.send_keys(message_text)
        chat_1.send_message_button.click()

        chat_1.get_back_to_home_view()
        profile_1 = home_1.profile_button.click()
        profile_1.logout()

        device_2.set_network_connection(2)  # turning on WiFi connection
        chat_2.wait_for_messages(username_1, message_text, self.errors, wait_time=120)
        self.verify_no_errors()
