import emoji
import random
from dateutil import parser
from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from views.sign_in_view import SignInView
from datetime import datetime, timedelta


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
        preselected_chats = ['#status', '#chitchat', '#defi', '#crypto', '#markets', '#dap-ps']
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
        chat_1.send_message(message)

        sent_time_variants = chat_1.convert_device_time_to_chat_timestamp()
        for chat in chat_1, chat_2:
            chat.verify_message_is_under_today_text(message, self.errors)
            timestamp = chat.chat_element_by_text(message).timestamp_message.text
            if timestamp not in sent_time_variants:
                self.errors.append("Timestamp is not shown, expected '%s', in fact '%s'" % (sent_time_variants.join(','), timestamp))
        if chat_2.chat_element_by_text(message).username.text != default_username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % default_username_1)

        self.errors.verify_no_errors()

    @marks.testrail_id(5360)
    @marks.critical
    def test_unread_messages_counter_public_chat(self):
        self.create_drivers(2)
        driver_2 = self.drivers[1]
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()

        chat_name = home_1.get_random_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        home_1.get_back_to_home_view()
        message, message_2 = 'test message', 'test message2'
        chat_2.send_message(message)

        home_1.just_fyi("Check unread message indicator on home, on chat element and that it is not shown after reading messages")
        if not home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')
        chat_element = home_1.get_chat('#' + chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')
        chat_element.click()
        home_1.home_button.double_click()

        if home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while there are no unread messages')
        [home.get_chat('#' + chat_name).click() for home in (home_1,home_2)]
        chat_1.send_message(message_2)
        chat_2.chat_element_by_text(message_2).wait_for_element(20)

        home_2.just_fyi("Check that unread message indicator is not reappeared after relogin")
        driver_2.close_app()
        driver_2.launch_app()
        SignInView(driver_2).sign_in()
        chat_element = home_2.get_chat('#' + chat_name)
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is shown after relogin')
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

        device_1.dapp_tab_button.click()
        message = 'hello'
        chat_2.send_message(message)
        home_1.home_button.click(desired_view='chat')
        if not chat_1.chat_element_by_text(message).is_element_displayed():
            self.drivers[0].fail("No message if it is received while another tab opened")


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
        chat.element_by_text_part("↓ Fetch more messages").wait_and_click(120)
        chat.element_by_text_part("↓ Fetch more messages").wait_for_visibility_of_element(180)
        for message in (before_yesterday, quiet_time_before_yesterday):
            if not chat.element_by_text_part(message).is_element_displayed():
                self.driver.fail('"%s" is not shown' % message)
        self.errors.verify_no_errors()
