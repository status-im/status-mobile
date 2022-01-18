import random
from datetime import timedelta
from time import sleep

import emoji
import pytest
from dateutil import parser

from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase, create_shared_drivers, \
    MultipleSharedDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="public_chat")
class TestPublicChatMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        device_1, device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.home_1, cls.home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = cls.home_1.profile_button.click()
        cls.username_1 = profile_1.default_username_text.text
        profile_1.home_button.click()
        cls.home_2.home_button.click()
        cls.public_chat_name = cls.home_1.get_random_chat_name()
        cls.chat_1, cls.chat_2 = cls.home_1.join_public_chat(cls.public_chat_name), cls.home_2.join_public_chat(cls.public_chat_name)

    @marks.testrail_id(5313)
    @marks.critical
    def test_public_chat_text_timestamps_while_on_different_tab(self):
        message = 'hello'
        self.chat_2.dapp_tab_button.click()
        self.chat_1.send_message(message)
        sent_time_variants = self.chat_1.convert_device_time_to_chat_timestamp()
        timestamp = self.chat_1.chat_element_by_text(message).timestamp_on_tap
        if timestamp not in sent_time_variants:
            self.errors.append("Timestamp is not shown, expected: '%s', in fact: '%s'" % (sent_time_variants.join(','), timestamp))
        self.chat_2.home_button.click(desired_view='chat')
        for chat in self.chat_1, self.chat_2:
            chat.verify_message_is_under_today_text(message, self.errors)
        if self.chat_2.chat_element_by_text(message).username.text != self.username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % self.username_1)
        self.errors.verify_no_errors()

    @marks.testrail_id(700719)
    @marks.critical
    def test_public_emoji(self):
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        emoji_message = emoji.emojize(emoji_name)
        self.chat_1.send_message(emoji_message)
        for chat in self.chat_1, self.chat_2:
            if not chat.chat_element_by_text(emoji_unicode).is_element_displayed(30):
                self.errors.append('Message with emoji was not sent or received in public chat')

        self.chat_1.just_fyi("Can copy and paste emojis")
        self.chat_1.element_by_text_part(emoji_unicode).long_press_element()
        self.chat_1.element_by_text('Copy').click()
        self.chat_1.chat_message_input.paste_text_from_clipboard()
        if self.chat_1.chat_message_input.text != emoji_unicode:
            self.errors.append('Emoji message was not copied')

        self.chat_1.just_fyi("Can reply to emojis")
        self.chat_2.quote_message(emoji_unicode)
        message_text = 'test message'
        self.chat_2.chat_message_input.send_keys(message_text)
        self.chat_2.send_message_button.click()
        chat_element_1 = self.chat_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=10) or chat_element_1.replied_message_text != emoji_unicode:
            self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(5360)
    @marks.critical
    def test_public_unread_messages_counter(self):
        self.chat_1.send_message('пиу')
        home_1 = self.chat_1.home_button.click()
        message = 'test message'
        self.chat_2.send_message(message)
        if not self.chat_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')
        chat_element = home_1.get_chat('#' + self.public_chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(700718)
    @marks.critical
    def test_public_unread_messages_counter_for_mentions_relogin(self):
        message = 'test message2'
        [chat.home_button.double_click() for chat in (self.chat_1, self.chat_2)]
        chat_element = self.home_1.get_chat('#' + self.public_chat_name)
        self.home_2.get_chat('#' + self.public_chat_name).click()
        self.chat_2.select_mention_from_suggestion_list(self.username_1, self.username_1[:2])
        self.chat_2.send_message_button.click()
        chat_element.new_messages_counter.wait_for_element(30)
        chat_element.new_messages_counter.wait_for_element_text("1", 60)
        chat_element.click()
        self.home_1.home_button.double_click()
        if self.home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while there are no unread messages')
        [home.get_chat('#' + self.public_chat_name).click() for home in (self.home_1, self.home_2)]
        self.chat_1.send_message(message)
        self.chat_2.chat_element_by_text(message).wait_for_element(20)

        self.chat_2.just_fyi("Check that unread messages counter doesn't reappear after relogin")
        driver_2 = self.drivers[1]
        driver_2.close_app()
        driver_2.launch_app()
        SignInView(driver_2).sign_in()
        chat_element = self.home_2.get_chat('#' + self.public_chat_name)
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.drivers[0].fail('New messages counter is shown after relogin')
        self.errors.verify_no_errors()


class TestPublicChatMultipleDevice(MultipleDeviceTestCase):

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
        chat_2.send_message(message)

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

    @marks.testrail_id(6342)
    @marks.medium
    def test_different_status_in_timeline(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        public_key_1, username_1 = profile_1.get_public_key_and_username(return_username=True)
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]

        home_1.just_fyi('Set status in profile')
        statuses = {
            '*formatted text*': 'formatted text',
            'https://www.youtube.com/watch?v=JjPWmEh2KhA': 'Status Town Hall',
            emoji.emojize(emoji_message): emoji_unicode,

        }
        timeline_1 = device_1.status_button.click()
        for status in statuses.keys():
            timeline_1.set_new_status(status)
            sleep(60)

        timeline_1.element_by_translation_id("enable").wait_and_click()
        timeline_1.element_by_translation_id("enable-all").wait_and_click()
        timeline_1.close_modal_view_from_chat_button.click()
        for status in statuses:
            expected_value = statuses[status]
            if not timeline_1.element_by_text_part(expected_value).is_element_displayed():
                self.errors.append("Expected value %s is not shown" % expected_value)
        text_status = 'some text'
        timeline_1.set_new_status(status=text_status)
        for timestamp in ('Now', '1M', '2M'):
            if not timeline_1.element_by_text(timestamp).is_element_displayed():
                self.errors.append("Expected timestamp %s is not shown in timeline_1" % timestamp)

        home_2.just_fyi('Check that can see user status without adding him as contact')
        profile_2.home_button.click()
        chat_2 = home_2.add_contact(public_key_1, add_in_contacts=False)
        chat_2.chat_options.click()
        chat_2.view_profile_button.click()
        chat_2.chat_element_by_text(text_status).wait_for_element(30)
        chat_2.element_by_translation_id("enable").scroll_and_click()
        chat_2.element_by_translation_id("enable-all").wait_and_click()
        chat_2.close_modal_view_from_chat_button.click()
        for status in statuses:
            chat_2.element_by_text_part(statuses['*formatted text*']).scroll_to_element()
            expected_value = statuses[status]
            if not chat_2.element_by_text_part(expected_value).is_element_displayed():
                self.errors.append(
                    "Expected value %s is not shown in other user profile without adding to contacts" % expected_value)

        home_2.just_fyi('Add device1 to contacts and check that status will be shown in timeline_1')
        chat_2.close_button.scroll_and_click(direction='up')
        chat_2.add_to_contacts.click()
        timeline_2 = chat_2.status_button.click()
        for status in statuses:
            expected_value = statuses[status]
            if not timeline_2.element_by_text_part(expected_value).is_element_displayed():
                self.errors.append(
                    "Expected value %s is not shown in timeline_1 after adding user to contacts" % expected_value)

        profile_1.just_fyi('Checking message tag and reactions on statuses')
        tag_status = '#public-chat-to-redirect-long-name'
        timeline_1.set_new_status(tag_status)
        public_chat_2 = home_2.get_chat_view()

        public_chat_2.element_by_text(tag_status).wait_and_click()
        public_chat_2.user_name_text.wait_for_element(30)
        if not public_chat_2.user_name_text.text == tag_status:
            self.errors.append('Could not redirect a user to a public chat tapping the tag message from timeline_1')
        public_chat_2.back_button.click()

        timeline_1.set_reaction(text_status)
        status_with_reaction_1 = timeline_1.chat_element_by_text(text_status)
        if status_with_reaction_1.emojis_below_message() != 1:
            self.errors.append("Counter of reaction is not updated on your own status in timeline_1!")
        device_2.home_button.double_click()
        home_2.get_chat(username_1).click()
        chat_2.chat_options.click()
        chat_2.view_profile_button.click()
        status_with_reaction_2 = chat_2.chat_element_by_text(text_status)
        if status_with_reaction_2.emojis_below_message(own=False) != 1:
            self.errors.append("Counter of reaction is not updated on status of another user in profile!")
        profile_1.just_fyi("Remove reaction and check it is updated for both users")
        timeline_1.set_reaction(text_status)
        status_with_reaction_1 = timeline_1.chat_element_by_text(text_status)
        if status_with_reaction_1.emojis_below_message() != 0:
            self.errors.append(
                "Counter of reaction is not updated after removing reaction on your own status in timeline_1!")
        status_with_reaction_2 = chat_2.chat_element_by_text(text_status)
        if status_with_reaction_2.emojis_below_message(own=False) != 0:
            self.errors.append(
                "Counter of reaction is not updated after removing on status of another user in profile!")

        profile_1.just_fyi("Remove user from contacts and check there is no his status in timeline_1 anymore")
        chat_2.remove_from_contacts.click()
        chat_2.close_button.click()
        chat_2.status_button.click()
        if public_chat_2.chat_element_by_text(text_status).is_element_displayed(10):
            self.errors.append("Statuses of removed user are still shown in profile")

        self.errors.verify_no_errors()


class TestPublicChatSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5675)
    @marks.high
    def test_redirect_to_public_chat_tapping_tag_message_fetch_more_history(self):
        signin = SignInView(self.driver)
        home_view = signin.create_user()
        chat_name = 'montagne-angerufen'
        chat = home_view.join_public_chat(chat_name)
        tag_message = '#spectentur'

        signin.just_fyi("Check that will be redirected to chat view on tap on tag message")
        chat.send_message(tag_message)
        chat.element_starts_with_text(tag_message).click()
        chat.element_by_text_part(chat_name).wait_for_invisibility_of_element()
        if not chat.user_name_text.text == tag_message:
            self.errors.append('Could not redirect a user to a public chat tapping the tag message.')

        signin.just_fyi("Check that can fetch previous history")
        device_time = parser.parse(signin.driver.device_time)
        yesterday = (device_time - timedelta(days=1)).strftime("%b %-d, %Y")
        before_yesterday = (device_time - timedelta(days=2)).strftime("%b %-d, %Y")
        quiet_time_yesterday, quiet_time_before_yesterday = '24 hours', '2 days'
        fetch_more = signin.get_translation_by_key("load-more-messages")
        for message in (yesterday, quiet_time_yesterday):
            if not chat.element_by_text_part(message).is_element_displayed(120):
                self.driver.fail('"%s" is not shown' % message)
        chat.element_by_text_part(fetch_more).wait_and_click(120)
        chat.element_by_text_part(fetch_more).wait_for_visibility_of_element(180)
        for message in (before_yesterday, quiet_time_before_yesterday):
            if not chat.element_by_text_part(message).is_element_displayed():
                self.driver.fail('"%s" is not shown' % message)

        signin.just_fyi("Check that chat is added to home view")
        home_view = chat.get_back_to_home_view()
        if not home_view.element_by_text(tag_message).is_element_displayed():
            self.errors.append('Could not find the public chat in user chat list.')
        times = {
            "three-days": '5 days',
            "one-week": '12 days',
            "one-month": ['43 days', '42 days', '41 days', '40 days'],
        }

        signin.just_fyi("Check that can fetch more history")
        home_view.element_by_text(tag_message).click()
        profile = home_view.profile_button.click()
        profile.sync_settings_button.click()
        profile.sync_history_for_button.click()
        for period in times:
            profile.just_fyi("Checking %s period" % period)
            profile.element_by_translation_id(period).click()
            profile.home_button.click(desired_view='chat')
            chat.element_by_text_part(fetch_more).wait_and_click(120)
            if period != "one-month":
                if not profile.element_by_text_part(times[period]).is_element_displayed(30):
                    self.errors.append("'Quiet here for %s' is not shown after fetching more history" % times[period])
            else:
                variants = times[period]
                chat.element_by_text_part(fetch_more).wait_for_invisibility_of_element(120)
                res = any(profile.element_by_text_part(variant).is_element_displayed(30) for variant in variants)
                if not res:
                    self.errors.append("History is not fetched for one month!")
            home_view.profile_button.click(desired_element_text=profile.get_translation_by_key("default-sync-period"))

        self.errors.verify_no_errors()
