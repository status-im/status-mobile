import time
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
        public_key_2 = home_2.get_public_key()
        home_2.home_button.click()

        home_1.add_contact(public_key_2)
        home_1.get_back_to_home_view()

        public_chat_name = home_1.get_public_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(public_chat_name), home_2.join_public_chat(public_chat_name)

        message = 'hello'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2.verify_message_is_under_today_text(message, self.errors)
        # TODO: should be replaced with ens name after https://github.com/status-im/status-react/pull/8487
        # full_username = '%s • %s' % (username_1, default_username_1)
        if chat_2.chat_element_by_text(message).username.text != default_username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % default_username_1)

        # if chat_1.element_by_text_part(username_1).is_element_displayed():
        #     self.errors.append("Username '%s' is shown for the sender" % username_1)

        self.verify_no_errors()

    @marks.testrail_id(5386)
    @marks.high
    def test_public_chat_clear_history(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        chat_name = device_1.get_public_chat_name()
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

        chat_name = home_1.get_public_chat_name()
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        home_1.get_back_to_home_view()

        message = 'test message'
        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()

        if home_1.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')

        chat_element = home_1.get_chat_with_user('#' + chat_name)
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')

        chat_element.click()
        home_1.get_back_to_home_view()

        if home_1.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')

        if chat_element.new_messages_counter.is_element_displayed():
            self.errors.append('New messages counter is shown on chat element for already seen message')
        self.verify_no_errors()


@marks.chat
class TestPublicChatSingleDevice(SingleDeviceTestCase):

    @marks.skip
    @marks.testrail_id(5392)
    @marks.high
    def test_send_korean_characters(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_name = home.get_public_chat_name()
        public_chat = home.join_public_chat(chat_name)
        message = '파란하늘'
        public_chat.chat_message_input.send_keys(message)
        if public_chat.chat_message_input.text != message:
            self.errors.append('Korean characters are not displayed properly in the chat message input')
        public_chat.send_message_button.click()
        if not public_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append('Message with korean characters is not shown')
        self.verify_no_errors()

    @marks.skip
    @marks.testrail_id(5336)
    @marks.medium
    def test_user_can_interact_with_public_chat(self):
        signin = SignInView(self.driver)
        home_view = signin.create_user()
        chat = home_view.join_public_chat('evripidis-middellijn')

        try:
            chat.empty_public_chat_message.wait_for_invisibility_of_element()
        except TimeoutException:
            self.driver.fail('Empty chat: history is not fetched!')

        # just to generate random text to be sent
        text = generate_timestamp()
        chat.send_message(text)

        if not chat.chat_element_by_text(text).is_element_displayed():
            self.errors.append('User sent message but it did not appear in chat!')

        chat.move_to_messages_by_time_marker('Today')
        if not chat.element_by_text('Today').is_element_displayed():
            self.errors.append("'Today' chat marker is not shown")
        if len(chat.chat_item.find_elements()) <= 1:
            self.errors.append('No messages fetched for today!')

        chat.move_to_messages_by_time_marker('Yesterday')
        if not chat.element_by_text('Yesterday').is_element_displayed():
            self.errors.append("'Yesterday' chat marker is not shown")
        if len(chat.chat_item.find_elements()) <= 1:
            self.errors.append('No messages fetched for yesterday!')

        self.verify_no_errors()

    @marks.testrail_id(5675)
    @marks.high
    def test_redirect_to_public_chat_tapping_tag_message(self):
        signin = SignInView(self.driver)
        home_view = signin.create_user()
        chat = home_view.join_public_chat('montagne-angerufen')
        tag_message = '#spectentur'
        chat.send_message(tag_message)
        chat.element_starts_with_text(tag_message).click()
        time.sleep(4)
        if not chat.user_name_text.text == tag_message:
            self.driver.fail('Could not redirect a user to a public chat tapping the tag message.')
        home = chat.get_back_to_home_view()
        if not home.chat_name_text.text == tag_message:
            self.driver.fail('Could not find the public chat in user chat list.')

    @marks.testrail_id(6205)
    @marks.high
    def test_fetch_more_history_in_empty_chat(self):
        signin = SignInView(self.driver)
        yesterday = (datetime.today() - timedelta(days=1)).strftime("%b %-d, %Y")
        before_yesterday = (datetime.today() - timedelta(days=2)).strftime("%b %-d, %Y")
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
        self.verify_no_errors()