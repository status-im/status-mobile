
import emoji
import random
from dateutil import parser
from selenium.common.exceptions import TimeoutException
from support.utilities import generate_timestamp
from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView
from datetime import datetime, timedelta


@marks.chat
class TestPublicChatMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5313)
    @marks.critical
    def test_public_chat_messaging(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        profile_1.home_button.click()
        public_key_2 = home_2.get_public_key_and_username()
        home_2.home_button.click()

        home_1.add_contact(public_key_2)
        home_1.get_back_to_home_view()

        home_1.plus_button.click_until_presence_of_element(home_1.join_public_chat_button)
        home_1.join_public_chat_button.click()
        preselected_chats = ['#status', '#introductions', '#chitchat', '#crypto', '#tech', '#music', '#movies', '#support']
        for chat in preselected_chats:
            if not home_1.element_by_text(chat).is_element_displayed():
                self.errors.append("'%s' text is not in the list of preselected chats" % chat)
        home_1.element_by_text('#status').click()
        status_chat = home_1.get_chat_view()
        if not status_chat.chat_message_input.is_element_displayed():
            self.errors.append('No redirect to chat if tap on #status chat')
        status_chat.get_back_to_home_view()


        public_chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(public_chat_name), home_2.join_public_chat(public_chat_name)

        message = 'hello'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        if chat_2.chat_element_by_text(message).username.text != default_username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % default_username_1)

        self.errors.verify_no_errors()

    @marks.testrail_id(5386)
    @marks.high
    def test_public_chat_clear_history(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        chat_name = device_1.get_random_chat_name()
        for sign_in in device_1, device_2:
            home = sign_in.create_user()
            home.join_public_chat(chat_name)
        chat_1, chat_2 = device_1.get_chat_view(), device_2.get_chat_view()
        message_1, message_2, message_3 = 'm1', 'm2', 'm3'
        chat_1.chat_message_input.send_keys(message_1)
        chat_1.send_message_button.click()
        chat_2.element_by_text(message_1).is_element_present()

        chat_2.chat_message_input.send_keys(message_2)
        chat_2.send_message_button.click()
        chat_1.element_by_text(message_2).is_element_present()
        chat_1.chat_options.click()
        chat_1.clear_history_button.click()
        chat_1.clear_button.click()
        chat_2.chat_message_input.send_keys(message_3)
        chat_2.send_message_button.click()
        chat_1.element_by_text(message_3).is_element_present()
        for message in message_1, message_2:
            if chat_1.element_starts_with_text(message).is_element_present():
                chat_1.driver.fail("Message '%s' is shown, but public chat history has been cleared" % message)
        home_1 = chat_1.get_back_to_home_view()
        home_1.relogin()
        home_1.element_by_text('#' + chat_name).click()
        for message in message_1, message_2:
            if chat_1.element_starts_with_text(message).is_element_present():
                chat_1.driver.fail(
                    "Message '%s' is shown after re-login, but public chat history has been cleared" % message)

    @marks.testrail_id(5360)
    @marks.critical
    def test_unread_messages_counter_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        home_1.get_back_to_home_view()

        message = 'test message'
        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()

        if not home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')

        chat_element = home_1.get_chat('#' + chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')

        chat_element.click()
        home_1.get_back_to_home_view()

        if home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while while there are no unread messages')

        self.errors.verify_no_errors()

    @marks.testrail_id(6270)
    @marks.medium
    def test_mark_all_messages_as_read_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        home_1.get_back_to_home_view()
        message = 'test message'
        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()

        if not home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')
        chat_element = home_1.get_chat('#' + chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')

        chat_element.long_press_element()
        home_1.mark_all_messages_as_read_button.click()
        home_1.get_back_to_home_view()

        if home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while while there are no unread messages')

        self.errors.verify_no_errors()

    @marks.testrail_id(6202)
    @marks.low
    def test_emoji_messages_long_press(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        chat_1.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat_1.send_message_button.click()

        chat_1.element_by_text_part(emoji_unicode).long_press_element()
        chat_1.element_by_text('Copy').click()
        chat_1.chat_message_input.paste_text_from_clipboard()
        if chat_1.chat_message_input.text != emoji_unicode:
            self.errors.append('Emoji message was not copied')

        chat_element_2 = chat_2.element_by_text_part(emoji_unicode)
        if not chat_element_2.is_element_displayed(sec=10):
            self.errors.append('Message with emoji was not received in public chat by the recipient')

        chat_2.quote_message(emoji_unicode)
        message_text = 'test message'
        chat_2.chat_message_input.send_keys(message_text)
        chat_2.send_message_button.click()

        chat_element_1 = chat_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=10) or chat_element_1.replied_message_text != emoji_unicode:
            self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(6275)
    @marks.medium
    def test_public_chat_messages_received_while_different_tab_opened(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        public_chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(public_chat_name), home_2.join_public_chat(public_chat_name)

        browser = device_1.dapp_tab_button.click()
        message = 'hello'
        chat_2.send_message(message)

        if home_1.home_button.public_unread_messages.is_element_displayed():
            device_1.home_button.click_until_absense_of_element(browser.enter_url_editbox)
        if not chat_1.chat_element_by_text(message).is_element_displayed():
            self.driver.fail("No message if it received while another tab opened")


@marks.chat
class TestPublicChatSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5675)
    @marks.high
    def test_redirect_to_public_chat_tapping_tag_message(self):
        signin = SignInView(self.driver)
        home_view = signin.create_user()
        chat = home_view.join_public_chat('montagne-angerufen')
        tag_message = '#spectentur'
        chat.send_message(tag_message)
        chat.element_starts_with_text(tag_message).click()
        chat.element_by_text_part('montagne-angerufen').wait_for_invisibility_of_element()
        if not chat.user_name_text.text == tag_message:
            self.driver.fail('Could not redirect a user to a public chat tapping the tag message.')
        home_view = chat.get_back_to_home_view()
        if not home_view.element_by_text(tag_message).is_element_displayed():
            self.driver.fail('Could not find the public chat in user chat list.')
        #if not home_view.chat_name_text.text == tag_message:


    @marks.testrail_id(6205)
    @marks.high
    def test_fetch_more_history_in_empty_chat(self):
        signin = SignInView(self.driver)
        device_time = parser.parse(signin.driver.device_time)
        yesterday = (device_time - timedelta(days=1)).strftime("%b %-d, %Y")
        before_yesterday = (device_time - timedelta(days=2)).strftime("%b %-d, %Y")
        quiet_time_yesterday, quiet_time_before_yesterday = '24 hours', '2 days'
        home_view = signin.create_user()
        chat = home_view.join_public_chat('montagne-angerufen-two')
        for message in (yesterday, quiet_time_yesterday):
            if not chat.element_by_text_part(message).is_element_displayed():
                self.driver.fail('"%s" is not shown' % message)
        chat.element_starts_with_text("↓ Fetch more messages").click()
        chat.wait_for_element_starts_with_text("↓ Fetch more messages", 30)
        for message in (before_yesterday, quiet_time_before_yesterday):
            if not chat.element_by_text_part(message).is_element_displayed():
                self.driver.fail('"%s" is not shown' % message)
        self.errors.verify_no_errors()
