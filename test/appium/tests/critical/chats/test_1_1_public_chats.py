import random

import emoji
import pytest
from _pytest.outcomes import Failed
from appium.webdriver.connectiontype import ConnectionType
from selenium.common.exceptions import TimeoutException, NoSuchElementException

from tests import marks, run_in_parallel, transl
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_one_2")
@marks.new_ui_critical
class TestOneToOneChatMultipleSharedDevicesNewUi(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        self.username_1, self.username_2 = 'sender', 'receiver'
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_2}))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.homes = (self.home_1, self.home_2)
        self.profile_1, self.profile_2 = (home.get_profile_view() for home in self.homes)
        self.public_key_2 = self.home_2.get_public_key()

        self.profile_1.just_fyi("Sending contact request via Profile > Contacts")
        for home in (self.home_1, self.home_2):
            home.navigate_back_to_home_view()
            home.chats_tab.click()
        self.home_1.send_contact_request_via_bottom_sheet(self.public_key_2)

        self.home_2.just_fyi("Accepting contact request from activity centre")
        self.home_2.handle_contact_request(self.username_1)

        self.profile_1.just_fyi("Sending message to contact via Messages > Recent")
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.home_2.navigate_back_to_home_view()
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.message_1, self.message_2, self.message_3, self.message_4 = \
            "Message 1", "Message 2", "Message 3", "Message 4"

    @marks.testrail_id(702730)
    def test_1_1_chat_message_reaction(self):
        message_from_sender = "Message sender"
        self.device_1.just_fyi("Sender start 1-1 chat, set 'thumbs-up' emoji and check counter")
        self.chat_1.send_message(message_from_sender)
        self.chat_1.chat_element_by_text(message_from_sender).wait_for_sent_state(120)
        self.chat_1.set_reaction(message_from_sender)

        message_sender = self.chat_1.chat_element_by_text(message_from_sender)
        message_sender.emojis_below_message().wait_for_element_text(1)

        self.device_2.just_fyi(
            "Receiver also sets 'thumbs-up' emoji and verifies counter on received message in 1-1 chat")
        message_receiver = self.chat_2.chat_element_by_text(message_from_sender)
        message_receiver.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1, 90)
        self.chat_2.add_remove_same_reaction(message_from_sender)
        message_receiver.emojis_below_message(emoji="thumbs-up").wait_for_element_text(2)
        message_sender.emojis_below_message(emoji="thumbs-up").wait_for_element_text(2, 90)

        self.device_2.just_fyi(
            "Receiver removes 'thumbs-up' emoji and verify that counter will decrease for both users")
        self.chat_2.add_remove_same_reaction(message_from_sender)
        message_receiver.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
        message_sender.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1, 90)

        self.device_2.just_fyi("Receiver sets another reaction ('love'). Check it's shown for both sender and receiver")
        self.chat_2.set_reaction(message_from_sender, emoji="love")
        message_receiver.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
        message_sender.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1, 90)
        message_receiver.emojis_below_message(emoji="love").wait_for_element_text(1)
        message_sender.emojis_below_message(emoji="love").wait_for_element_text(1, 90)

        self.device_1.just_fyi("Sender votes for 'love' reaction. Check reactions counters")
        self.chat_1.add_remove_same_reaction(message_from_sender, emoji="love")
        message_receiver.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
        message_sender.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
        message_receiver.emojis_below_message(emoji="love").wait_for_element_text(2, 90)
        message_sender.emojis_below_message(emoji="love").wait_for_element_text(2)

        self.device_1.just_fyi("Check emojis info")
        message_sender.emojis_below_message(emoji="love").long_press_until_element_is_shown(
            self.chat_1.authors_for_reaction(emoji="love"))
        if not self.chat_1.user_list_element_by_name(
                self.username_1).is_element_displayed() or not self.chat_1.user_list_element_by_name(
            self.username_2).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'love' reaction.")

        self.chat_1.authors_for_reaction(emoji="thumbs-up").click()
        if not self.chat_1.user_list_element_by_name(
                self.username_1).is_element_displayed() or self.chat_1.user_list_element_by_name(
            self.username_2).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'thumbs-up' reaction.")
        self.chat_1.driver.press_keycode(4)

        self.errors.verify_no_errors()

    @marks.testrail_id(702782)
    def test_1_1_chat_emoji_send_reply_and_open_link(self):
        self.chat_1.navigate_back_to_chat_view()
        self.chat_2.navigate_back_to_chat_view()
        self.home_1.just_fyi("Check that can send emoji in 1-1 chat")
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        self.chat_1.send_message(emoji.emojize(emoji_name))
        for chat in self.chat_1, self.chat_2:
            if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
                self.errors.append('Message with emoji was not sent or received in 1-1 chat')
        self.chat_1.quote_message(emoji_unicode)
        actual_text = self.chat_1.quote_username_in_message_input.text
        if actual_text != "You":
            self.errors.append(
                "'You' is not displayed in reply quote snippet replying to own message, '%s' instead" % actual_text)

        self.chat_1.just_fyi("Clear quote and check there is not snippet anymore")
        self.chat_1.cancel_reply_button.click()
        if self.chat_1.cancel_reply_button.is_element_displayed():
            self.errors.append("Message quote kept in public chat input after it was cancelled")

        self.chat_1.just_fyi("Send reply")
        self.chat_1.quote_message(emoji_unicode)
        reply_to_message_from_sender = "hey, reply"
        self.chat_1.send_message(reply_to_message_from_sender)

        self.chat_1.just_fyi("Receiver verifies received reply...")
        if self.chat_2.chat_element_by_text(reply_to_message_from_sender).replied_message_text != emoji_unicode:
            self.errors.append("No reply received in 1-1 chat")
        else:
            self.chat_2.just_fyi("Device 2 sets a reaction on the message reply. Device 1 checks the reaction")
            self.chat_1.set_reaction(reply_to_message_from_sender)
            try:
                self.chat_1.chat_element_by_text(
                    reply_to_message_from_sender).emojis_below_message().wait_for_element_text(1)
            except Failed:
                self.errors.append("Reply message reaction is not shown for the sender")

        self.home_1.just_fyi("Check that link can be opened and replied from 1-1 chat")
        reply = 'reply to link'
        url_message = 'Test with link: https://status.im/ here should be nothing unusual.'
        self.chat_1.send_message(url_message)
        self.chat_2.chat_element_by_text(url_message).wait_for_element(20)
        self.chat_2.quote_message(url_message)
        self.chat_2.send_message(reply)
        replied_message = self.chat_1.chat_element_by_text(reply)
        if replied_message.replied_message_text != url_message:
            self.errors.append("Reply for '%s' not present in message received in public chat" % url_message)

        self.chat_2.just_fyi("Device 2 sets a reaction on the message with a link. Device 1 checks the reaction")
        self.chat_2.set_reaction(url_message)
        try:
            self.chat_1.chat_element_by_text(url_message).emojis_below_message().wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Link message reaction is not shown for the sender")

        self.home_2.just_fyi("Check 'Open in Status' option")
        # url_to_open = 'http://status.app'
        url_to_open = 'https://www.ethereum.org/en/run-a-node/'
        self.chat_1.send_message(url_to_open)
        chat_element = self.chat_2.chat_element_by_text(url_to_open)
        if chat_element.is_element_displayed(120):
            chat_element.click_on_link_inside_message_body()
            web_view = self.chat_2.open_in_status_button.click()
            if not web_view.element_by_text("Take full control. Run your own node.").is_element_displayed(60):
                self.errors.append('URL was not opened from 1-1 chat')
        else:
            self.errors.append("Message with URL was not received")

        self.errors.verify_no_errors()

    @marks.xfail(reason="Pin feature is in development", run=False)
    @marks.testrail_id(702731)
    def test_1_1_chat_pin_messages(self):
        self.home_1.just_fyi("Check that Device1 can pin own message in 1-1 chat")
        self.chat_2.jump_to_card_by_text(self.username_1)
        self.chat_1.send_message(self.message_1)
        self.chat_1.send_message(self.message_2)
        self.chat_1.chat_element_by_text(self.message_1).wait_for_status_to_be("Delivered")
        self.chat_1.pin_message(self.message_1, 'pin-to-chat')
        if not self.chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed():
            self.drivers[0].fail("Message is not pinned!")

        self.home_1.just_fyi("Check that Device2 can pin Device1 message in 1-1 chat and two pinned "
                             "messages are in Device1 profile")
        self.chat_2.pin_message(self.message_2, 'pin-to-chat')
        for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
            chat.pinned_messages_count.wait_for_element_text("2",
                                                             message="Pinned messages count is not 2 as expected!")

            chat.just_fyi("Check pinned message are visible in Pinned panel for user %s" % (chat_number + 1))
            chat.pinned_messages_count.click()
            for message in self.message_1, self.message_2:
                pinned_by = chat.pinned_messages_list.get_message_pinned_by_text(message)
                if pinned_by.is_element_displayed():
                    text = pinned_by.text.strip()
                    if chat_number == 0:
                        expected_text = "You" if message == self.message_1 else self.username_2
                    else:
                        expected_text = "You" if message == self.message_2 else self.username_1
                    if text != expected_text:
                        self.errors.append(
                            "Pinned by '%s' doesn't match expected '%s' for user %s" % (
                                text, expected_text, chat_number + 1)
                        )
                else:
                    self.errors.append(
                        "Message '%s' is missed on Pinned messages list for user %s" % (message, chat_number + 1)
                    )
            chat.click_system_back_button()

        self.home_1.just_fyi("Check that Device1 can not pin more than 3 messages and 'Unpin' dialog appears")
        for message in (self.message_3, self.message_4):
            self.chat_1.send_message(message)
            self.chat_1.chat_element_by_text(message).wait_for_status_to_be("Delivered")
            self.chat_1.pin_message(message, 'pin-to-chat')
        # if self.chat_1.pin_limit_popover.is_element_displayed(30):
        self.chat_1.view_pinned_messages_button.click_until_presence_of_element(self.chat_1.pinned_messages_list)
        # temp solution instead of getting pin_limit_popover:
        if self.chat_1.pinned_messages_list.get_pinned_messages_number() > 3 \
                or self.chat_1.pinned_messages_list.message_element_by_text(self.message_4).is_element_displayed():
            self.errors.append("Can pin more than 3 messages in chat")
        else:
            self.chat_1.pinned_messages_list.message_element_by_text(
                self.message_2).click_inside_element_by_coordinate()
            self.home_1.just_fyi("Unpin one message so that another could be pinned")
            self.chat_1.element_by_translation_id('unpin-from-chat').click()
            self.chat_1.chat_element_by_text(self.message_4).click()
            self.chat_1.pin_message(self.message_4, 'pin-to-chat')
            if not (self.chat_1.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30) and
                    self.chat_2.chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30)):
                self.errors.append("Message 4 is not pinned in chat after unpinning previous one")

        self.home_1.just_fyi("Check pinned messages are visible in Pinned panel for both users")
        for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
            count = chat.pinned_messages_count.text
            if count != '3':
                self.errors.append("Pinned messages count is %s but should be 3 for user %s" % (count, chat_number + 1))

        self.home_1.just_fyi("Unpin one message and check it's unpinned for another user")
        self.chat_2.tap_by_coordinates(500, 100)

        self.chat_1.view_pinned_messages_button.click_until_presence_of_element(self.chat_1.pinned_messages_list)
        pinned_message = self.chat_1.pinned_messages_list.message_element_by_text(self.message_4)

        element = self.chat_1.element_by_translation_id("unpin-from-chat")
        pinned_message.long_press_until_element_is_shown(element)
        element.click_until_absense_of_element(element)
        try:
            self.chat_2.chat_element_by_text(self.message_4).pinned_by_label.wait_for_invisibility_of_element()
        except TimeoutException:
            self.errors.append("Message_4 is not unpinned!")

        for chat_number, chat in enumerate([self.chat_1, self.chat_2]):
            count = chat.pinned_messages_count.text
            if count != '2':
                self.errors.append(
                    "Pinned messages count is %s but should be 2 after unpinning the last pinned message for user %s" %
                    (count, chat_number + 1)
                )
        self.errors.verify_no_errors()

    @marks.testrail_id(702745)
    def test_1_1_chat_non_latin_messages_stack_update_profile_photo(self):
        self.home_1.jump_to_messages_home()
        self.home_1.profile_button.click()
        self.profile_1.edit_profile_picture(image_index=2)
        self.profile_1.navigate_back_to_home_view()
        self.profile_1.chats_tab.click()

        self.chat_2.just_fyi("Send messages with non-latin symbols")
        self.home_1.jump_to_card_by_text(self.username_2)
        self.chat_1.send_message("just a text")  # Sending a message here so the next ones will be in a separate line

        self.home_2.navigate_back_to_home_view()
        self.home_2.jump_to_card_by_text(self.username_1)
        messages = ['hello', '¿Cómo estás tu año?', 'ё, доброго вечерочка', '®	æ ç ♥']
        for message in messages:
            self.chat_2.send_message(message)
            if not self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append("Message with text '%s' was not received" % message)

        self.chat_2.just_fyi("Checking updated member photo, timestamp and username on message")
        self.chat_2.hide_keyboard_if_shown()
        try:
            timestamp = self.chat_2.chat_element_by_text(messages[0]).timestamp
            sent_time_variants = self.chat_2.convert_device_time_to_chat_timestamp()
            if timestamp not in sent_time_variants:
                self.errors.append(
                    'Timestamp on message %s does not correspond expected %s' % (timestamp, sent_time_variants))
        except NoSuchElementException:
            self.errors.append("No timestamp on message %s" % messages[0])
        for message in [messages[1], messages[2]]:
            if self.chat_2.chat_element_by_text(message).member_photo.is_element_displayed():
                self.errors.append('%s is not stack to 1st(they are sent in less than 5 minutes)!' % message)

        self.chat_1.just_fyi("Sending message")
        message = 'profile_photo'
        self.chat_1.send_message(message)
        self.chat_2.chat_element_by_text(message).wait_for_visibility_of_element(30)

        self.chat_1.just_fyi("Go back to chat view and checking that profile photo is updated")
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.home_2.get_chat(self.username_1).click()
        if self.chat_2.chat_element_by_text(message).member_photo.is_element_differs_from_template("member3.png",
                                                                                                   diff=7):
            self.errors.append("Image of user in 1-1 chat is too different from template!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702813)
    def test_1_1_chat_push_emoji(self):
        message_no_pn, message = 'No PN', 'Text push notification'

        self.home_1.navigate_back_to_home_view()
        self.home_2.navigate_back_to_home_view()
        self.home_2.profile_button.click()
        self.home_1.chats_tab.click()

        self.device_2.just_fyi("Device 2 puts app on background being on Profile view to receive PN with text")
        app_package = self.device_2.driver.current_package
        self.device_2.put_app_to_background()
        self.device_2.open_notification_bar()
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message(message)

        self.device_1.just_fyi("Device 1 puts app on background to receive emoji push notification")
        self.device_1.navigate_back_to_home_view()
        self.device_1.profile_button.click()

        self.device_2.just_fyi("Check text push notification and tap it")
        if not self.home_2.get_pn(message):
            self.device_2.click_system_back_button()
            self.device_2.driver.activate_app(app_package)
            self.device_2.driver.fail("Push notification with text was not received")
        chat_2 = self.device_2.click_upon_push_notification_by_text(message)

        self.device_2.just_fyi("Send emoji message to Device 1 while it's on background")
        self.device_1.put_app_to_background()
        self.device_1.open_notification_bar()
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        chat_2.send_message(emoji.emojize(emoji_message))

        self.device_1.just_fyi("Device 1 checks PN with emoji")
        if not self.device_1.element_by_text_part(emoji_unicode).is_element_displayed(120):
            self.device_1.click_system_back_button()
            self.device_1.driver.activate_app(app_package)
            self.device_1.driver.fail("Push notification with emoji was not received")
        chat_1 = self.device_1.click_upon_push_notification_by_text(emoji_unicode)

        self.device_1.just_fyi("Check Device 1 is actually on chat")
        if not (chat_1.element_by_text_part(message).is_element_displayed(15)
                and chat_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            self.device_1.driver.fail("Failed to open chat view after tap on PN")

        self.device_1.just_fyi("Checks there are no PN after message was seen")
        [home.open_notification_bar() for home in self.homes]
        if (self.device_2.element_by_text_part(message).is_element_displayed()
                or self.device_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            self.errors.append("PN are keep staying after message was seen by user")
        self.errors.verify_no_errors()

    @marks.testrail_id(702855)
    def test_1_1_chat_edit_message(self):
        self.home_1.navigate_back_to_home_view()
        self.home_2.navigate_back_to_home_view()
        self.chat_2.jump_to_card_by_text(self.username_1)
        self.chat_1.jump_to_card_by_text(self.username_2)

        self.device_2.just_fyi(
            "Device 2 sends text message and edits it in 1-1 chat. Device 2 checks edited message is shown")
        message_before_edit_1_1, message_after_edit_1_1 = "Message before edit 1-1", "AFTER"
        self.chat_2.send_message(message_before_edit_1_1)
        self.chat_2.chat_element_by_text(message_before_edit_1_1).wait_for_status_to_be("Delivered")
        self.chat_2.edit_message_in_chat(message_before_edit_1_1, message_after_edit_1_1)
        message_text_after_edit = message_after_edit_1_1 + ' (Edited)'
        chat_element = self.chat_1.chat_element_by_text(message_text_after_edit)
        if not chat_element.is_element_displayed(30):
            self.errors.append('No edited message in 1-1 chat displayed')
        else:
            self.device_1.just_fyi("Device 1 sets a reaction on the edited message. Device 2 checks the reaction")
            self.chat_1.set_reaction(message_text_after_edit)
            try:
                self.chat_1.chat_element_by_text(
                    message_text_after_edit).emojis_below_message().wait_for_element_text(1)
            except Failed:
                self.errors.append("Message reaction is not shown for the sender")
        self.errors.verify_no_errors()

    @marks.testrail_id(703391)
    def test_1_1_chat_send_image_save_and_share(self):
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.chat_2.jump_to_card_by_text(self.username_1)
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.chat_1.jump_to_card_by_text(self.username_2)

        self.chat_1.just_fyi("Device 1 sends an image")
        image_description = "test image"
        self.chat_1.send_images_with_description(description=image_description, indexes=[2])

        self.chat_2.just_fyi("Device 2 checks image message")
        if not self.chat_2.chat_element_by_text(image_description).is_element_displayed(30):
            self.chat_2.hide_keyboard_if_shown()
        self.chat_2.chat_element_by_text(image_description).wait_for_visibility_of_element(30)
        if not self.chat_2.chat_element_by_text(
                image_description).image_in_message.is_element_image_similar_to_template('saucelabs_sauce_chat.png'):
            self.errors.append("Not expected image is shown to the receiver.")

        for chat in self.chat_1, self.chat_2:
            chat.just_fyi("Open the image and share it")
            if not chat.chat_element_by_text(image_description).image_in_message.is_element_displayed():
                chat.hide_keyboard_if_shown()
            chat.chat_element_by_text(image_description).image_in_message.click()
            chat.share_image_icon_button.click()
            chat.element_starts_with_text("Drive").click()
            try:
                chat.wait_for_current_package_to_be('com.google.android.apps.docs')
            except TimeoutException:
                self.errors.append(
                    "%s can't share an image via Gmail." % ("Sender" if chat is self.chat_1 else "Receiver"))
            chat.navigate_back_to_chat_view()

        for chat in self.chat_1, self.chat_2:
            chat.just_fyi("Open the image and save it")
            device_name = "sender" if chat is self.chat_1 else "receiver"
            chat.chat_element_by_text(image_description).image_in_message.click()
            chat.view_image_options_button.click()
            chat.save_image_icon_button.click()
            toast_element = chat.toast_content_element
            if toast_element.is_element_displayed():
                toast_element_text = toast_element.text
                if toast_element_text != chat.get_translation_by_key("photo-saved"):
                    self.errors.append(
                        "Shown message '%s' doesn't match expected '%s' after saving an image for %s." % (
                            toast_element_text, chat.get_translation_by_key("photo-saved"), device_name))
            else:
                self.errors.append("Message about saving a photo is not shown for %s." % device_name)
            chat.navigate_back_to_chat_view()

        for chat in self.chat_1, self.chat_2:
            chat.just_fyi("Check that image is saved in gallery")
            chat.show_images_button.click()
            chat.allow_all_button.click_if_shown()
            if not chat.get_image_by_index(0).is_element_image_similar_to_template("saucelabs_sauce_gallery.png"):
                self.errors.append(
                    "Image is not saved to gallery for %s." % ("sender" if chat is self.chat_1 else "receiver"))
            chat.click_system_back_button()

        self.errors.verify_no_errors()

    @marks.testrail_id(702733)
    def test_1_1_chat_text_message_delete_push_disappear(self):
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.chat_2.jump_to_card_by_text(self.username_1)
        if not self.chat_1.chat_message_input.is_element_displayed():
            self.chat_1.jump_to_card_by_text(self.username_2)
        app_package = self.chat_1.driver.current_package

        self.device_2.just_fyi("Verify Device1 can not edit and delete received message from Device2")
        message_after_edit_1_1 = 'smth I should edit'
        message_to_delete_for_me = 'message to delete for me'
        self.chat_2.send_message(message_after_edit_1_1)
        self.chat_2.chat_element_by_text(message_after_edit_1_1).wait_for_status_to_be("Delivered")
        chat_1_element = self.chat_1.chat_element_by_text(message_after_edit_1_1)
        chat_1_element.long_press_element()
        for action in ("edit", "delete-for-everyone"):
            if self.chat_1.element_by_translation_id(action).is_element_displayed():
                self.errors.append('Option to %s someone else message available!' % action)
        self.home_1.tap_by_coordinates(500, 100)

        self.device_2.just_fyi("Delete message for me and check it is only deleted for the author")
        self.chat_2.send_message(message_to_delete_for_me)
        try:
            timeout = 60
            self.chat_2.chat_element_by_text(message_to_delete_for_me).wait_for_status_to_be("Delivered", timeout)
            self.chat_2.delete_message_in_chat(message_to_delete_for_me, everyone=False)
        except TimeoutException:
            self.errors.append("Message status was not changed to 'Delivered' after %s s" % timeout)
        else:
            if not self.chat_2.chat_element_by_text(message_to_delete_for_me).is_element_disappeared(20):
                self.errors.append("Deleted for me message is shown in chat for the author of message")
            if not self.chat_2.element_by_translation_id('message-deleted-for-you').is_element_displayed(20):
                self.errors.append("System message about deletion for you is not displayed")
            if not self.chat_1.chat_element_by_text(message_to_delete_for_me).is_element_displayed(20):
                self.errors.append("Deleted for me message is deleted for both users")

        self.device_2.just_fyi("Delete message for everyone and check it is not shown in chat preview on home")
        self.chat_2.delete_message_in_chat(message_after_edit_1_1)
        for chat in (self.chat_2, self.chat_1):
            if chat.chat_element_by_text(message_after_edit_1_1).is_element_displayed(30):
                self.errors.append("Deleted message is shown in chat view for 1-1 chat")
        self.chat_1.navigate_back_to_home_view()
        if self.home_1.element_by_text(message_after_edit_1_1).is_element_displayed(30):
            self.errors.append("Deleted message is shown on chat element on home screen")

        self.device_2.just_fyi("Send one more message and check that PN will be deleted with message deletion")
        message_to_delete = 'DELETE ME'
        self.home_1.put_app_to_background()
        self.home_1.open_notification_bar()
        self.chat_2.send_message(message_to_delete)
        self.chat_2.chat_element_by_text(message_to_delete).wait_for_sent_state()
        if not self.home_1.get_pn(message_to_delete):
            self.home_1.click_system_back_button()
            self.device_2.driver.activate_app(app_package)
            self.errors.append("Push notification doesn't appear")
        self.chat_2.delete_message_in_chat(message_to_delete)
        pn_to_disappear = self.home_1.get_pn(message_to_delete)
        if pn_to_disappear:
            if not pn_to_disappear.is_element_disappeared(90):
                self.errors.append("Push notification was not removed after initial message deletion")
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_six_2")
@marks.new_ui_critical
class TestOneToOneChatMultipleSharedDevicesNewUiTwo(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        self.username_1, self.username_2 = 'sender', 'receiver'
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_2}))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.homes = (self.home_1, self.home_2)
        self.profile_1, self.profile_2 = (home.get_profile_view() for home in self.homes)
        self.public_key_2 = self.home_2.get_public_key()

        self.profile_1.just_fyi("Sending contact request via Profile > Contacts")
        for home in (self.home_1, self.home_2):
            home.navigate_back_to_home_view()
            home.chats_tab.click()
        self.home_1.send_contact_request_via_bottom_sheet(self.public_key_2)

        self.home_2.just_fyi("Accepting contact request from activity centre")
        self.home_2.handle_contact_request(self.username_1)

        self.profile_1.just_fyi("Sending message to contact via Messages > Recent")
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.home_2.navigate_back_to_home_view()
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.message_1, self.message_2, self.message_3, self.message_4 = \
            "Message 1", "Message 2", "Message 3", "Message 4"

    @marks.skip  # ToDo: can't be implemented with current SauceLabs emulators screen resolution
    def test_1_1_chat_send_image_with_camera(self):
        self.chat_1.just_fyi("Device 1 sends a camera image")
        image_description = "camera test"
        self.chat_1.send_image_with_camera(description=image_description)
        for chat in self.chat_1, self.chat_2:
            chat_name = "sender" if chat.driver.number == 0 else "receiver"
            chat.just_fyi("%s checks image message" % chat_name.capitalize())
            chat_element = chat.chat_element_by_text(image_description)
            if chat_element.is_element_displayed(30):
                if not chat_element.image_in_message.is_element_image_similar_to_template('saucelabs_camera_image.png'):
                    self.errors.append("Not expected image is shown to the %s." % chat_name)
            else:
                self.errors.append("Message with camera image is not shown in chat for %s" % chat_name)
        self.errors.verify_no_errors()

    @marks.testrail_id(702783)
    @marks.xfail(reason="Data delivery issue")
    def test_1_1_chat_is_shown_message_sent_delivered_from_offline(self):
        # self.chat_2.jump_to_card_by_text(self.username_1)
        # self.chat_1.jump_to_card_by_text(self.username_2)
        self.home_1.just_fyi('Turn on airplane mode and check that offline status is shown on home view')
        for home in self.homes:
            home.driver.set_network_connection(ConnectionType.AIRPLANE_MODE)

        # Not implemented yet
        # self.home_1.connection_offline_icon.wait_and_click(20)
        # for element in self.home_1.not_connected_to_node_text, self.home_1.not_connected_to_peers_text:
        #     if not element.is_element_displayed():
        #         self.errors.append(
        #             'Element "%s" is not shown in Connection status screen if device is offline' % element.locator)
        # self.home_1.click_system_back_button()

        message_1 = 'test message'

        self.home_2.just_fyi('Device2 checks "Sending" status when sending message from offline')
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.home_2.chats_tab.click()
            self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(message_1)
        status = self.chat_2.chat_element_by_text(message_1).status
        if not (status == 'Sending' or status == 'Sent'):
            self.errors.append('Message status is not "Sending", it is "%s"!' % status)

        self.home_2.just_fyi('Device2 goes back online and checks that status of the message is changed to "delivered"')
        for home in self.homes:
            home.driver.set_network_connection(ConnectionType.ALL_NETWORK_ON)

        self.home_1.just_fyi('Device1 goes back online and checks that 1-1 chat will be fetched')
        if not self.chat_1.chat_element_by_text(message_1).is_element_displayed(120):
            self.errors.append("Message was not delivered after resending from offline")

        self.home_2.just_fyi('Device1 goes back online and checks that 1-1 chat will be fetched')
        try:
            self.chat_2.chat_element_by_text(message_1).wait_for_status_to_be(expected_status='Delivered', timeout=120)
        except TimeoutException as e:
            self.errors.append('%s after back up online!' % e.msg)
        self.errors.verify_no_errors()

    @marks.testrail_id(703496)
    def test_1_1_chat_mute_chat(self):
        self.home_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()
        self.home_1.just_fyi("Mute chat")
        self.home_1.mute_chat_long_press(self.username_2)

        muted_message = "should be muted"
        self.chat_2.send_message(muted_message)
        chat = self.home_1.get_chat(self.username_2)
        if chat.new_messages_counter.is_element_displayed(30) or self.home_1.chats_tab.counter.is_element_displayed(10):
            self.errors.append("New messages counter is shown after mute")
        if not chat.chat_preview.text.startswith(muted_message):
            self.errors.append("Message text '%s' is not shown in chat preview after mute" % muted_message)
        chat.click()
        if not self.chat_1.chat_element_by_text(muted_message).is_element_displayed(30):
            self.errors.append("Message '%s' is not shown in chat for receiver after mute" % muted_message)

        self.chat_1.just_fyi("Unmute chat")
        self.chat_1.navigate_back_to_home_view()
        chat.long_press_element()
        if self.home_1.mute_chat_button.text != transl["unmute-chat"]:
            self.errors.append("Chat is not muted")
        expected_text = "Muted until you turn it back on"
        if not self.home_1.element_by_text(expected_text).is_element_displayed():
            self.errors.append("Text '%s' is not shown for muted chat" % expected_text)
        self.home_1.mute_chat_button.click()

        unmuted_message = "after unmute"
        self.chat_2.send_message(unmuted_message)
        if not chat.new_messages_counter.is_element_displayed(
                30) or not self.home_1.chats_tab.counter.is_element_displayed(10):
            self.errors.append("New messages counter is not shown after unmute")
        if not chat.chat_preview.text.startswith(unmuted_message):
            self.errors.append("Message text '%s' is not shown in chat preview after unmute" % unmuted_message)
        chat.click()
        if not self.chat_1.chat_element_by_text(unmuted_message).is_element_displayed(30):
            self.errors.append("Message '%s' is not shown in chat for receiver after unmute" % unmuted_message)

        self.errors.verify_no_errors()

    @marks.testrail_id(702784)
    def test_1_1_chat_delete_via_long_press_relogin(self):
        self.home_2.navigate_back_to_home_view()
        self.home_2.chats_tab.click()

        self.home_2.just_fyi("Deleting chat via delete button and check it will not reappear after relaunching app")
        self.home_2.delete_chat_long_press(username=self.username_1)
        if self.home_2.get_chat_from_home_view(self.username_1).is_element_displayed():
            self.errors.append("Deleted '%s' chat is shown, but the chat has been deleted" % self.username_1)
        self.home_2.reopen_app()
        if self.home_2.get_chat_from_home_view(self.username_1).is_element_displayed(15):
            self.errors.append(
                "Deleted chat '%s' is shown after re-login, but the chat has been deleted" % self.username_1)
        self.errors.verify_no_errors()
