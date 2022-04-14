import random
from time import sleep
import emoji
import pytest

from tests import marks
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="public_chat_medium_2")
@marks.medium
class TestPublicChatMultipleDeviceMergedMedium(MultipleSharedDeviceTestCase):
    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        device_1, device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.home_1, cls.home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = cls.home_1.profile_button.click()
        cls.public_key_1, cls.username_1 = profile_1.get_public_key_and_username(return_username=True)
        profile_1.home_button.click()
        cls.text_message = 'hello'
        [home.home_button.click() for home in (cls.home_1, cls.home_2)]
        cls.public_chat_name = cls.home_1.get_random_chat_name()
        cls.chat_1, cls.chat_2 = cls.home_1.join_public_chat(cls.public_chat_name), cls.home_2.join_public_chat(
            cls.public_chat_name)
        cls.chat_1.send_message(cls.text_message)

    @marks.testrail_id(6342)
    def test_public_chat_timeline_different_statuses_reaction(self):
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]

        self.home_1.just_fyi('Set status in profile')
        statuses = {
            '*formatted text*': 'formatted text',
            'https://www.youtube.com/watch?v=JjPWmEh2KhA': 'Status Town Hall',
            emoji.emojize(emoji_message): emoji_unicode,

        }
        timeline_1 = self.home_1.status_button.click()
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
        timeline_1.set_new_status(status=text_status, image=True)
        for timestamp in ('Now', '1M', '2M'):
            if not timeline_1.element_by_text(timestamp).is_element_displayed():
                self.errors.append("Expected timestamp %s is not shown in timeline_1" % timestamp)
        if not timeline_1.image_message_in_chat.is_element_displayed():
            self.errors.append("Timeline image is not shown in timeline_1")

        self.home_2.just_fyi('Check that can see user status without adding him as contact')
        self.home_2.home_button.click()
        chat_2 = self.home_2.add_contact(self.public_key_1, add_in_contacts=False)
        chat_2.chat_options.click()
        timeline_2 = chat_2.view_profile_button.click()
        if not timeline_2.image_message_in_chat.is_element_displayed(40):
            self.errors.append(
                'Timeline image of another user is not shown when open another user profile before adding to contacts')
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

        self.home_2.just_fyi('Add device1 to contacts and check that status will be shown in timeline_1')
        chat_2.close_button.scroll_and_click(direction='up')
        chat_2.add_to_contacts.click()
        timeline_2 = chat_2.status_button.click()
        for status in statuses:
            expected_value = statuses[status]
            if not timeline_2.element_by_text_part(expected_value).is_element_displayed():
                self.errors.append(
                    "Expected value %s is not shown in timeline_1 after adding user to contacts" % expected_value)
        if not timeline_2.image_message_in_chat.is_element_displayed(40):
            self.errors.append(
                'Timeline image of another user is not shown when open another user profile after adding to contacts')

        self.home_2.just_fyi('Checking message tag and reactions on statuses')
        tag_status = '#public-chat-to-redirect-long-name'
        timeline_1.set_new_status(tag_status)
        public_chat_2 = self.home_2.get_chat_view()

        public_chat_2.element_by_text(tag_status).wait_and_click()
        public_chat_2.user_name_text.wait_for_element(30)
        if not public_chat_2.user_name_text.text == tag_status:
            self.errors.append('Could not redirect a user to a public chat tapping the tag message from timeline_1')
        public_chat_2.back_button.click()

        timeline_1.set_reaction(text_status)
        status_with_reaction_1 = timeline_1.chat_element_by_text(text_status)
        if status_with_reaction_1.emojis_below_message() != 1:
            self.errors.append("Counter of reaction is not updated on your own status in timeline_1!")
        self.home_2.home_button.double_click()
        self.home_2.get_chat(self.username_1).click()
        chat_2.chat_options.click()
        chat_2.view_profile_button.click()
        status_with_reaction_2 = chat_2.chat_element_by_text(text_status)
        if status_with_reaction_2.emojis_below_message(own=False) != 1:
            self.errors.append("Counter of reaction is not updated on status of another user in profile!")
        self.home_1.just_fyi("Remove reaction and check it is updated for both users")
        timeline_1.set_reaction(text_status)
        status_with_reaction_1 = timeline_1.chat_element_by_text(text_status)
        if status_with_reaction_1.emojis_below_message() != 0:
            self.errors.append(
                "Counter of reaction is not updated after removing reaction on your own status in timeline_1!")
        status_with_reaction_2 = chat_2.chat_element_by_text(text_status)
        if status_with_reaction_2.emojis_below_message(own=False) != 0:
            self.errors.append(
                "Counter of reaction is not updated after removing on status of another user in profile!")

        self.home_1.just_fyi("Remove user from contacts and check there is no his status in timeline_1 anymore")
        chat_2.remove_from_contacts.click()
        chat_2.close_button.click()
        chat_2.status_button.click()
        if public_chat_2.chat_element_by_text(text_status).is_element_displayed(10):
            self.errors.append("Statuses of removed user are still shown in profile")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="chat_medium_2")
@marks.medium
class TestChatMultipleDevice(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(2)
        cls.device_1, cls.device_2 = SignInView(cls.drivers[0]), SignInView(cls.drivers[1])
        cls.home_1, cls.home_2 = cls.device_1.create_user(enable_notifications=True), cls.device_2.create_user()
        cls.public_key_1, cls.default_username_1 = cls.home_1.get_public_key_and_username(return_username=True)
        cls.public_key_2, cls.default_username_2 = cls.home_2.get_public_key_and_username(return_username=True)
        profile_2 = cls.home_2.profile_button.click()
        profile_2.switch_network()
        [home.home_button.click() for home in (cls.home_1, cls.home_2)]

        cls.home_1.just_fyi("Creating 1-1 chats")
        cls.chat_1 = cls.home_1.add_contact(cls.public_key_2)
        cls.chat_2 = cls.home_2.add_contact(cls.public_key_1)
        cls.home_2.just_fyi('Install free sticker pack and use it in 1-1 chat')
        cls.chat_2.install_sticker_pack_by_name()
        [home.home_button.click() for home in (cls.home_1, cls.home_2)]

        cls.home_1.just_fyi("Creating group chats")
        cls.initial_group_chat_name = "GroupChat before rename"
        cls.new_group_chat_name = "GroupChat after rename"
        cls.group_chat_1 = cls.home_1.create_group_chat(user_names_to_add=[cls.default_username_2], group_chat_name=cls.initial_group_chat_name)
        cls.group_chat_2 = cls.home_2.get_chat(cls.initial_group_chat_name).click()
        cls.group_chat_2.join_chat_button.click()
        [home.home_button.click() for home in (cls.home_1, cls.home_2)]

        cls.home_1.just_fyi("Creating public chats")
        cls.public_chat_name = cls.home_1.get_random_chat_name()
        cls.public_chat_1, cls.public_chat_2 = cls.home_1.join_public_chat(cls.public_chat_name), cls.home_2.join_public_chat(cls.public_chat_name)
        [home.home_button.click() for home in (cls.home_1, cls.home_2)]

        cls.home_1.get_chat(cls.default_username_2).click()
        cls.home_2.get_chat(cls.default_username_1).click()

        cls.message_1, cls.message_2, cls.message_3, cls.message_4 = "Message1", "Message2", "Message3", "Message4"

    @marks.testrail_id(702066)
    def test_chat_1_1_push_and_reaction_for_messages_sticker_audio_image(self):

        # methods with steps to use later in loop
        def navigate_to_start_state_of_both_devices():
            self.chat_1.put_app_to_background()
            self.device_1.open_notification_bar()
            self.chat_2.get_back_to_home_view(2)
            self.home_2.get_chat_from_home_view(self.default_username_1).click()

        def device_2_sends_sticker():
            self.chat_2.just_fyi("Sending Sticker in chat")
            self.chat_2.show_stickers_button.click()
            self.chat_2.sticker_icon.click()

        def device_2_sends_image():
            self.chat_2.just_fyi("Sending Image in chat")
            self.chat_2.show_images_button.click()
            self.chat_2.allow_button.click()
            self.chat_2.first_image_from_gallery.click()
            self.chat_2.send_message_button.click()

        def device_2_sends_audio():
            self.chat_2.just_fyi("Sending Audio in chat")
            self.chat_2.record_audio_message(message_length_in_seconds=3)
            self.chat_2.send_message_button.click()

        sending_list = {
            "sticker": device_2_sends_sticker,
            "image": device_2_sends_image,
            "audio": device_2_sends_audio,
        }

        for key, value in sending_list.items():
            navigate_to_start_state_of_both_devices()
            sending_list[key]()
            if not self.device_1.element_by_text_part(key.capitalize()).is_element_displayed(10):
                self.errors.append("%s not appeared in Push Notification" % key.capitalize())
                self.device_1.click_system_back_button()
                self.device_1.get_app_from_background()
            else:
                self.device_1.element_by_text_part(key.capitalize()).click()
            message = self.chat_2.chat_element_by_text(key)
            self.chat_1.set_reaction(key)
            if message.emojis_below_message(own=False) != 1:
                self.errors.append("Counter of reaction is not set on %s for message receiver!" % key)
            self.chat_1.set_reaction(key)
            if message.emojis_below_message(own=False) == 1:
                self.errors.append("Counter of reaction is not re-set on %s for message receiver!" % key)

        self.chat_2.just_fyi("Sending Emoji/Tag/Links in chat")
        # TODO: add link and tag messages after #11168 is fixed(rechecked 23.11.21, valid)
        navigate_to_start_state_of_both_devices()

        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]

        self.chat_2.just_fyi("Sending Emoji in chat")
        self.chat_2.chat_message_input.send_keys(emoji.emojize(emoji_name))
        self.chat_2.send_message_button.click()

        if not self.device_1.element_by_text_part(emoji_unicode).is_element_displayed(10):
            self.errors.append("Emoji not appeared in Push Notification")
            self.device_1.click_system_back_button()
            self.device_1.get_app_from_background()
        else:
            self.device_1.element_by_text_part(emoji_unicode).click()

        emoji_message = self.chat_2.chat_element_by_text(emoji_unicode)
        self.chat_1.set_reaction(emoji_unicode, emoji_message=True)
        if emoji_message.emojis_below_message(own=False) != 1:
            self.errors.append("Counter of reaction is not set on Emoji for message receiver!")
        self.chat_1.set_reaction(emoji_unicode, emoji_message=True)
        if emoji_message.emojis_below_message(own=False) == 1:
            self.errors.append("Counter of reaction is not re-set on Emoji for message receiver!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702069)
    def test_chat_1_1_pin_messages(self):
        self.home_1.just_fyi("Check that Device1 can pin own message in 1-1 chat")
        self.chat_1.send_message(self.message_1)
        self.chat_1.send_message(self.message_2)
        self.chat_1.pin_message(self.message_1)
        if not self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present():
            self.drivers[0].fail("Message is not pinned!")

        self.home_1.just_fyi("Check that Device2 can pin Device1 message in 1-1 chat and two pinned "
                             "messages are in Device1 profile")
        self.chat_2.pin_message(self.message_2)
        self.chat_2.chat_options.click()
        self.chat_2.view_profile_button.click()
        if not self.chat_2.pinned_messages_button.count == "2":
            self.drivers[0].fail("Pinned message count is not 2 as expected!")

        self.home_1.just_fyi("Check pinned message are visible in Pinned panel for both users")
        self.chat_1.chat_options.click()
        self.chat_1.view_profile_button.click()
        self.chat_1.pinned_messages_button.click()
        if not (self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present() and
                self.chat_1.chat_element_by_text(self.message_2).pinned_by_label.is_element_present() and
                self.chat_1.chat_element_by_text(self.message_1).is_element_present() and
                self.chat_1.chat_element_by_text(self.message_2).is_element_present()):
            self.drivers[0].fail("Something missed on Pinned messaged on Device 1!")
        self.chat_2.pinned_messages_button.click()
        if not (self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present() and
                self.chat_2.chat_element_by_text(self.message_2).pinned_by_label.is_element_present() and
                self.chat_2.chat_element_by_text(self.message_1).is_element_present() and
                self.chat_2.chat_element_by_text(self.message_2).is_element_present()):
            self.drivers[0].fail("Something missed on Pinned messaged on Device 2!")
        self.chat_1.close_button.click()

        self.home_1.just_fyi("Check that Device1 can not pin more than 3 messages and 'Unpin' dialog appears"
                             "messages are in Device1 profile")
        self.chat_1.send_message(self.message_3)
        self.chat_1.send_message(self.message_4)
        self.chat_1.pin_message(self.message_3)
        self.chat_1.pin_message(self.message_4)
        if not self.chat_1.unpin_message_popup.is_element_present():
            self.drivers[0].fail("No 'Unpin' dialog appears when pining 4th message")

        self.home_1.just_fyi("Unpin one message so that another could be pinned")
        self.chat_1.unpin_message_popup.message_text(self.message_1).click()
        self.chat_1.unpin_message_popup.click_unpin_message_button()

        if self.chat_1.unpin_message_popup.is_element_present():
            self.drivers[0].fail("Unpin message pop up keep staying after Unpin button pressed")
        if self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present():
            self.drivers[0].fail("Message is not unpinned!")
        if not self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_present():
            self.drivers[0].fail("Message is not pinned!")

        self.home_1.just_fyi("Unpin another message and check it's unpinned for another user")
        self.chat_2.close_button.click()
        self.chat_2.pin_message(self.message_4, action="unpin")
        self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.wait_for_invisibility_of_element()
        if self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_present():
            self.drivers[0].fail("Message_4 is not unpinned!")

    @marks.testrail_id(702065)
    def test_chat_public_markdown_support(self):
        markdown = {
            'bold text in asterics': '**',
            'bold text in underscores': '__',
            'italic text in asteric': '*',
            'italic text in underscore': '_',
            'inline code': '`',
            'code blocks': '```',
            'quote reply (one row)': '>',
        }

        for message, symbol in markdown.items():
            self.home_1.just_fyi('checking that "%s" is applied (%s) in 1-1 chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.chat_2.send_message(message_to_send)
            if not self.chat_2.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the sender \n' % message)

            if not self.chat_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the recipient \n' % message)

        [chat.home_button.double_click() for chat in (self.chat_1, self.chat_2)]
        [home.get_chat('#' + self.public_chat_name).click() for home in (self.home_1, self.home_2)]

        for message, symbol in markdown.items():
            self.home_1.just_fyi('checking that "%s" is applied (%s) in public chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.public_chat_1.send_message(message_to_send)
            if not self.public_chat_2.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append('%s is not displayed with markdown in public chat for the sender \n' % message)

            if not self.public_chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append('%s is not displayed with markdown in public chat for the recipient \n' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(702098)
    def test_chat_group_chat_rename(self):

        [chat.home_button.double_click() for chat in [self.chat_1, self.chat_2]]

        self.home_2.just_fyi('Rename chat and check system messages')
        [home.get_chat(self.initial_group_chat_name).click() for home in (self.home_1, self.home_2)]
        self.group_chat_1.rename_chat_via_group_info(self.new_group_chat_name)
        for chat in (self.group_chat_1, self.group_chat_2):
            if not chat.element_by_text(
                    chat.create_system_message(self.default_username_1, self.initial_group_chat_name)).is_element_displayed():
                self.errors.append('Initial system message about creating chat was changed!')
            if not chat.element_by_text(
                    chat.changed_group_name_system_message(self.default_username_1,
                                                           self.new_group_chat_name)).is_element_displayed():
                self.errors.append('Message about changing chat name is not shown')

        self.home_2.just_fyi('Check that you can see renamed chat')
        self.group_chat_2.back_button.click()
        self.home_2.get_chat(self.new_group_chat_name).wait_for_visibility_of_element(60)

        self.errors.verify_no_errors()

    @marks.testrail_id(702097)
    def test_chat_block_and_unblock_user_from_group_chat_via_group_info(self):

        [chat.home_button.double_click() for chat in [self.chat_1, self.chat_2]]

        self.home_2.just_fyi('Send message and block user via Group Info')
        [home.get_chat(self.new_group_chat_name).click() for home in (self.home_1, self.home_2)]
        message_before_block = 'message from device2'
        self.group_chat_2.send_message(message_before_block)
        options_2 = self.group_chat_1.get_user_options(self.default_username_2)
        options_2.view_profile_button.click()
        options_2.block_contact()
        self.home_1.close_button.click()
        if self.group_chat_1.chat_element_by_text(message_before_block).is_element_displayed(10):
            self.errors.append('User was blocked, but past message are shown')
        message_after_block = 'message from device2 after block'
        self.group_chat_2.send_message(message_after_block)
        if self.group_chat_1.chat_element_by_text(message_after_block).is_element_displayed(10):
            self.errors.append('User was blocked, but new messages still received')

        self.home_1.just_fyi('Unblock user via group info and check that new messages will arrive')
        options_2 = self.group_chat_1.get_user_options(self.default_username_2)
        options_2.view_profile_button.click()
        options_2.unblock_contact_button.click()
        [options_2.close_button.click() for _ in range(2)]
        message_after_unblock = 'message from device2 after unblock'
        self.group_chat_2.send_message(message_after_unblock)
        if not self.group_chat_1.chat_element_by_text(message_after_unblock).is_element_displayed(20):
            self.errors.append('User was unblocked, but new messages are not received')

        self.errors.verify_no_errors()

    @marks.testrail_id(702070)
    def test_chat_pin_messages_in_group_chat(self):

        [chat.home_button.double_click() for chat in [self.chat_1, self.chat_2]]

        self.home_1.just_fyi("Enter group chat and pin message there. It's pinned for both members.")
        [home.get_chat(self.new_group_chat_name).click() for home in (self.home_1, self.home_2)]
        self.group_chat_1.send_message(self.message_1)
        self.group_chat_1.pin_message(self.message_1)
        if not (self.group_chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present(30) and
                self.group_chat_2.chat_element_by_text(self.message_1).pinned_by_label.is_element_present(30)):
            self.errors.append("Message is not pinned in group chat!")

        self.home_1.just_fyi("Check that non admin user can not unpin messages")
        self.group_chat_2.chat_element_by_text(self.message_1).long_press_element()
        if self.group_chat_2.element_by_translation_id("unpin").is_element_present():
            self.errors.append("Unpin option is available for non-admin user")

        self.home_1.just_fyi("Grant another user with admin rights and check he can unpin message now")
        self.group_chat_1.chat_options.click()
        group_info = self.group_chat_1.group_info.click()
        options = group_info.get_username_options(self.default_username_2).click()
        options.make_admin_button.click()
        self.group_chat_2.click_system_back_button()
        self.group_chat_2.pin_message(self.message_1, action="unpin")
        if (self.group_chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_present() and
                self.group_chat_2.chat_element_by_text(self.message_1).pinned_by_label.is_element_present()):
            self.errors.append("Message failed be unpinned by user who granted admin permissions!")

        self.errors.verify_no_errors()