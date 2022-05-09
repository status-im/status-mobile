import random
from time import sleep
import emoji
import pytest

from tests import marks
from tests.users import ens_user
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
            chat_2.element_by_text_part(statuses['*formatted text*']).scroll_to_element()
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
        public_chat_2.element_by_text(tag_status).scroll_and_click('up')
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

    @marks.testrail_id(702258)
    def test_chat_group_chat_set_nickname_and_ens_via_group_info_mention(self):
        self.drivers[1].reset()

        self.home_2 = SignInView(self.drivers[1]).recover_access(ens_user['passphrase'])
        self.home_1.home_button.double_click()
        self.profile_2 = self.home_2.profile_button.click()
        ens, full_ens, username_2 = ens_user['ens'], '@%s' % ens_user['ens'], ens_user['username']
        self.profile_2.connect_existing_ens(ens)
        [home.home_button.click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi('Set nickname, using emojis, special chars and cyrrilic chars without adding to contact')
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        special_char, cyrrilic = '"£¢€¥~`•|√π¶∆×°™®©%$@', 'стат'
        nickname_to_set = emoji.emojize(emoji_message) + special_char + cyrrilic
        nickname_expected = emoji_unicode + special_char + cyrrilic
        chat_1 = self.home_1.add_contact(ens, add_in_contacts=False, nickname=nickname_to_set)
        if chat_1.user_name_text.text != nickname_expected:
            self.errors.append('Expected special char nickname %s does not match actual %s' % (nickname_expected, chat_1.user_name_text.text))

        self.home_1.just_fyi('Can remove nickname without adding to contact')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.profile_nickname_button.click()
        chat_1.nickname_input_field.clear()
        chat_1.element_by_text('Done').click()
        chat_1.close_button.click()
        if chat_1.user_name_text.text != full_ens:
            self.errors.append(
                'Nickname was not removed! real chat name is %s instead of %s' % (chat_1.user_name_text.text, full_ens))

        self.home_1.just_fyi('Adding ENS user to contacts and start group chat with him')
        group_name = 'ens_group'
        chat_1.add_to_contacts.click()
        chat_2 = self.home_2.add_contact(self.public_key_1)
        chat_2.send_message("first")
        chat_2.home_button.click()
        chat_1.home_button.click()
        chat_1 = self.home_1.create_group_chat([full_ens], group_name)
        chat_2 = self.home_2.get_chat(group_name).click()
        chat_2.join_chat_button.click()

        self.home_1.just_fyi('Check ENS and in group chat and suggestions list')
        chat_1.element_by_text_part(full_ens).wait_for_visibility_of_element(60)
        chat_1.select_mention_from_suggestion_list(ens, typed_search_pattern=ens[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        additional_text = 'and more'
        chat_1.send_as_keyevent(additional_text)
        chat_1.send_message_button.click()
        message_text = '%s %s' % (full_ens, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name is not resolved on sent message")

        self.home_1.just_fyi('Set nickname via group info and check that can mention by nickname /username in group chat')
        nickname = 'funny_bunny'
        device_2_options = chat_1.get_user_options(full_ens)
        device_2_options.view_profile_button.click()
        chat_1.set_nickname(nickname, close_profile=False)
        if not chat_1.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname is not shown in profile view after setting from group info')
        chat_1.close_button.click()
        chat_1.element_by_text(nickname).scroll_to_element()
        chat_1.close_button.click()
        message_text = '%s %s' % (nickname, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name was not replaced with nickname on sent message")
        chat_1.chat_message_input.send_keys('@')
        if not chat_1.element_by_text('%s %s' % (nickname, full_ens)).is_element_displayed():
            self.errors.append("ENS name with nickname is not shown in mention input after set")
        if not chat_1.element_by_text(username_2).is_element_displayed():
            self.errors.append("3-random name is not shown in mention input after set from group info")
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' % (nickname, full_ens), typed_search_pattern=username_2[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS is not resolved in chat input after setting nickname in mention suggestions list (search by 3-random name)!')
        chat_1.chat_message_input.clear()
        chat_1.select_mention_from_suggestion_list('%s %s' % (nickname, full_ens), typed_search_pattern=nickname[:2])
        if chat_1.chat_message_input.text != '@' + ens + ' ':
            self.errors.append(
                'ENS is not resolved in chat input after setting nickname in mention suggestions list (search by nickname)!')
        chat_1.chat_message_input.clear()

        self.home_1.just_fyi('Can delete nickname via group info and recheck received messages')
        device_2_options = chat_1.get_user_options(full_ens)
        device_2_options.view_profile_button.click()
        chat_1.profile_nickname_button.click()
        chat_1.nickname_input_field.clear()
        chat_1.element_by_text('Done').click()
        chat_1.close_button.click()
        chat_1.close_button.click()
        message_text = '%s %s' % (full_ens, additional_text)
        if not chat_1.chat_element_by_text(message_text).is_element_displayed():
            self.errors.append("ENS name is not resolved on sent message after removing nickname")
        chat_1.chat_message_input.send_keys('@')
        if chat_1.element_by_text_part(nickname).is_element_displayed():
            self.errors.append("Nickname is shown in group chat after removing!")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="group_chat_medium_3")
@marks.medium
class TestGroupChatMultipleDevice(MultipleSharedDeviceTestCase):
    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(3)
        cls.sign_ins, cls.homes, cls.public_keys, cls.usernames, cls.chats = {}, {}, {}, {}, {}
        for key in cls.drivers:
            cls.sign_ins[key] = SignInView(cls.drivers[key])
            cls.homes[key] = cls.sign_ins[key].create_user()
            SignInView(cls.drivers[2]).put_app_to_background_and_back()
            cls.public_keys[key], cls.usernames[key] = cls.sign_ins[key].get_public_key_and_username(True)
            cls.sign_ins[key].home_button.click()
            SignInView(cls.drivers[0]).put_app_to_background_and_back()

        for member in (cls.public_keys[1], cls.public_keys[2]):
            cls.homes[0].add_contact(member)
            cls.homes[0].home_button.click()
        cls.chat_name = cls.homes[0].get_random_chat_name()
        cls.invite_chat_name = '%s_invite' % cls.homes[0].get_random_chat_name()
        cls.chats[0] = cls.homes[0].create_group_chat([], cls.invite_chat_name)
        [SignInView(cls.drivers[i]).put_app_to_background_and_back() for i in range(1, 3)]
        cls.link = cls.chats[0].get_group_invite_via_group_info()
        cls.chats[0].home_button.double_click()

        cls.chats[0] = cls.homes[0].create_group_chat([cls.usernames[1], cls.usernames[2]], cls.chat_name)
        for i in range(1, 3):
            cls.chats[i] = cls.homes[i].get_chat(cls.chat_name).click()
            cls.chats[i].join_chat_button.click()

    @marks.testrail_id(702259)
    def test_group_chat_remove_member(self):
        self.chats[0].just_fyi("Admin: get options for device 2 in group chat and remove him")
        removed_user = self.usernames[2]
        options = self.chats[0].get_user_options(removed_user)
        options.remove_user_button.click()
        left_message = self.chats[0].leave_system_message(removed_user)
        for key in self.chats:
            if not self.chats[key].chat_element_by_text(left_message).is_element_displayed():
                self.errors.append("Message with text '%s' was not received" % left_message)

        self.chats[0].just_fyi("Check that input field is not available after removing")
        if self.chats[2].chat_message_input.is_element_displayed():
            self.errors.append("Message input is still available for removed user")
        self.chats[0].just_fyi("Send message and check that it is available only for remaining users")
        message = 'after removing member'
        self.chats[0].send_message(message)
        for chat in (self.chats[0], self.chats[1]):
            if not chat.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append("Message '%s' was not received after removing member" % message)
        if self.chats[2].chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message '%s' was received by removed member" % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(702260)
    def test_group_chat_make_admin(self):
        self.homes[0].just_fyi('Check group info view and options of users')
        self.chats[0].chat_options.click()
        group_info_1 = self.chats[0].group_info.click()
        if not group_info_1.user_admin(self.usernames[0]).is_element_displayed():
            self.errors.append("Admin user is not marked as admin")
        group_info_1.get_user_from_group_info(self.usernames[0]).click()
        if self.chats[0].profile_block_contact.is_element_displayed():
            self.errors.append("Admin is redirected to own profile on tapping own username from group info")

        self.chats[0].just_fyi('Made admin another user and check system message')
        options = group_info_1.get_username_options(self.usernames[1]).click()
        options.make_admin_button.click()
        admin_system_message = self.chats[0].has_been_made_admin_system_message(self.usernames[0], self.usernames[1])
        for chat in (self.chats[0], self.chats[1]):
            if not chat.chat_element_by_text(admin_system_message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % admin_system_message)

        self.chats[1].just_fyi('Check Admin in group info and that "add members" is available')
        self.chats[1].chat_options.click()
        group_info_1 = self.chats[1].group_info.click()
        for username in (self.usernames[0], self.usernames[1]):
            if not group_info_1.user_admin(username).is_element_displayed():
                self.errors.append("Admin user is not marked as admin")
        if not group_info_1.add_members.is_element_displayed():
            self.errors.append("Add member button is not available for new admin")
        self.errors.verify_no_errors()

    @marks.testrail_id(702261)
    def test_group_chat_accept_decline_invite(self):
        [driver.close_app() for driver in (self.drivers[1], self.drivers[2])]
        self.homes[0].home_button.double_click()
        self.chats[0].just_fyi('Member_1, member_2: both users send requests to join group chat')
        [sign_in.open_weblink_and_login(self.link) for sign_in in (self.sign_ins[1], self.sign_ins[2])]
        introduction_messages = ['message for retrying']
        for i in range(1, 3):
            self.homes[i].element_by_text_part(self.invite_chat_name).click()
            introduction_messages.append('Please add me, member_%s to your gorgeous group chat' % str(i))
            self.chats[i].request_membership_for_group_chat(introduction_messages[i])

        self.chats[0].just_fyi('Admin: accept request for Member_1 and decline for Member_2')
        self.homes[0].get_chat(self.invite_chat_name).click()
        self.chats[0].group_membership_request_button.wait_and_click()
        self.chats[0].element_by_text(self.usernames[1]).click()
        if not self.chats[0].element_by_text_part(introduction_messages[1]).is_element_displayed():
            self.errors.append('Introduction message is not shown!')
        self.chats[0].accept_group_invitation_button.wait_and_click()
        self.chats[0].accept_membership_for_group_chat_via_chat_view(self.usernames[2], accept=False)
        self.chats[0].click_system_back_button()

        self.chats[2].just_fyi('Member_2: retry request')
        self.chats[2].retry_group_invite_button.wait_and_click()
        self.chats[2].request_membership_for_group_chat(introduction_messages[0])

        self.chats[2].just_fyi('Admin: decline request for Member_2')
        self.chats[0].group_membership_request_button.wait_and_click()
        self.chats[0].element_by_text(self.usernames[2]).click()
        if not self.chats[0].element_by_text_part(introduction_messages[0]).is_element_displayed():
            self.errors.append('Introduction message that was set after retrying attempt is not shown for admin!')
        self.chats[0].decline_group_invitation_button.wait_and_click()
        self.chats[0].click_system_back_button()

        self.chats[2].just_fyi('Member_2: remove chat')
        self.chats[2].remove_group_invite_button.wait_and_click()

        self.chats[2].just_fyi('Double check after relogin')
        if self.chats[0].group_membership_request_button.is_element_displayed():
            self.errors.append('Group membership request is still shown when there are no pending requests anymore')
        [self.homes[i].reopen_app() for i in range(0, 3)]
        if self.homes[2].element_by_text_part(self.invite_chat_name).is_element_displayed():
            self.errors.append('Group chat was not removed when removing after declining group invite')
        [home.get_chat(self.invite_chat_name).click() for home in (self.homes[0], self.homes[1])]
        if self.chats[0].group_membership_request_button.is_element_displayed():
            self.errors.append(
                'Group membership request is shown after relogin when there are no pending requests anymore')
        join_system_message = self.chats[0].join_system_message(self.usernames[1])
        for chat in (self.chats[1], self.chats[0]):
            if not chat.chat_element_by_text(join_system_message).is_element_displayed():
                self.errors.append('%s is not shown after joining to group chat via invite' % join_system_message)
        self.errors.verify_no_errors()
