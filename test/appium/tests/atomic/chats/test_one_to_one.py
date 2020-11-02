import time

import emoji
import random
import string
from selenium.common.exceptions import TimeoutException

from tests import marks
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten
from views.sign_in_view import SignInView
from  views.send_transaction_view import SendTransactionView


class TestMessagesOneToOneChatMultiple(MultipleDeviceTestCase):

    @marks.testrail_id(5305)
    @marks.critical
    def test_text_message_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()
        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_2_chat = device_2_home.get_chat(default_username_1).click()
        device_2_chat.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(6283)
    @marks.high
    def test_push_notification_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user(enable_notifications=True)
        device_2.just_fyi("Device_1 = Enables Notifications from Profile; Device_2 - from onboarding")

        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        profile_1.settings_button.click()
        profile_1.profile_notifications_button.click()
        profile_1.profile_notifications_toggle_button.click()
        device_1_home = profile_1.get_back_to_home_view()
        device_2_public_key = device_2_home.get_public_key_and_username()

        device_2.just_fyi("Device 2 puts app on background being on Profile view to receive PN with text")
        device_2.click_system_home_button()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        message = 'Text push notification'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        device_1.just_fyi("Device 1 puts app on background to receive emoji push notification")
        device_1.profile_button.click()
        device_1.click_system_home_button()

        device_2.just_fyi("Check text push notification and tap it")
        device_2.open_notification_bar()
        if not (device_2.element_by_text_part(message).is_element_displayed()
                and device_2.element_by_text_part(default_username_1).is_element_displayed()):
           device_2.driver.fail("Push notification with text was not received")
        chat_view_device_2 = device_2.click_upon_push_notification_by_text(message)

        device_2.just_fyi("Send emoji message to Device 1 while it's on backround")
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        chat_view_device_2.send_message(emoji.emojize(emoji_message))

        device_1.just_fyi("Device 1 checks PN with emoji")
        device_1.open_notification_bar()
        if not device_1.element_by_text_part(emoji_unicode).is_element_displayed(10):
            device_1.driver.fail("Push notification with emoji was not received")
        chat_view_device_1 = device_1.click_upon_push_notification_by_text(emoji_unicode)
        device_1.just_fyi("Check Device 1 is actually on chat")
        if not (chat_view_device_1.element_by_text_part(message).is_element_displayed()
            and chat_view_device_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            device_1.driver.fail("Failed to open chat view after tap on PN")

        device_1.just_fyi("Checks there are no PN after message was seen")
        [device.click_system_home_button() for device in (device_1, device_2)]
        [device.open_notification_bar() for device in (device_1, device_2)]
        if (device_2.element_by_text_part(message).is_element_displayed()
                or device_1.element_by_text_part(emoji_unicode).is_element_displayed()):
            self.errors.append("PN are keep staying after message was seen by user")
        self.errors.verify_no_errors()

    @marks.testrail_id(5310)
    @marks.critical
    def test_offline_is_shown_messaging_1_1_chat(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        public_key_1 = home_1.get_public_key_and_username()
        home_1.home_button.click()

        home_1.just_fyi('turn on airplane mode and check that offline status is shown on home view')
        home_1.toggle_airplane_mode()
        home_1.connection_status.wait_for_visibility_of_element(15)
        if home_1.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in home screen')
        profile_2 = home_2.profile_button.click()
        username_2 = profile_2.default_username_text.text
        profile_2.get_back_to_home_view()
        chat_2 = home_2.add_contact(public_key_1)
        message_1 = 'test message'
        chat_2.send_message(message_1)

        home_2.just_fyi('turn on airplane mode and check that offline status is shown on chat view')
        chat_2.toggle_airplane_mode()
        chat_2.element_by_text('Offline').wait_for_visibility_of_element(15)
        if chat_2.connection_status.text != 'Offline':
            self.errors.append('Offline status is not shown in 1-1 chat')

        home_1.just_fyi('go back online and check that 1-1 chat will be fetched')
        home_1.toggle_airplane_mode()
        home_1.connection_status.wait_for_invisibility_of_element(30)
        chat_element = home_1.get_chat(username_2)
        chat_element.wait_for_visibility_of_element(30)
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element(2)

        home_1.just_fyi('checking offline fetching for another message')
        chat_2.toggle_airplane_mode()
        home_1.toggle_airplane_mode()
        chat_2.element_by_text('Connecting to peers...').wait_for_invisibility_of_element(60)
        chat_2.connection_status.wait_for_invisibility_of_element(60)
        message_2 = 'one more message'
        chat_2.send_message(message_2)
        home_1.toggle_airplane_mode()
        chat_1 = chat_element.click()
        chat_1.chat_element_by_text(message_2).wait_for_visibility_of_element(180)

    @marks.testrail_id(5338)
    @marks.critical
    def test_messaging_in_different_networks(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        public_key_2 = home_2.get_public_key_and_username()
        profile_2 = home_2.get_profile_view()
        profile_2.switch_network('Mainnet with upstream RPC')

        chat_1 = home_1.add_contact(public_key_2)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()

        chat_2 = home_2.get_chat(default_username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

        public_chat_name = home_1.get_random_chat_name()
        chat_1.get_back_to_home_view()
        home_1.join_public_chat(public_chat_name)
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(public_chat_name)

        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()

    @marks.testrail_id(5315)
    @marks.high
    def test_send_non_english_message_to_newly_added_contact(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()

        # Skip until edit-profile feature returned

        # profile_1 = device_1_home.profile_button.click()
        # profile_1.edit_profile_picture('sauce_logo.png')
        # profile_1.home_button.click()

        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)
        messages = ['hello', '¿Cómo estás tu año?', 'ё, доброго вечерочка', '®	æ ç ♥']
        for message in messages:
            device_1_chat.send_message(message)

        chat_element = device_2_home.get_chat(default_username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        for message in messages:
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append("Message with test '%s' was not received" % message)
        if not device_2_chat.add_to_contacts.is_element_displayed():
            self.errors.append('Add to contacts button is not shown')
        if device_2_chat.user_name_text.text != default_username_1:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % default_username_1)
        device_2_chat.chat_options.click()
        device_2_chat.view_profile_button.click()

        # TODO: skip until edit-profile feature returned

        # if not device_2_chat.contact_profile_picture.is_element_image_equals_template('sauce_logo.png'):
        #     self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(5782)
    @marks.critical
    def test_install_pack_and_send_sticker(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()

        device_1_home.just_fyi('join public chat and check that stickers are not available on Ropsten')
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_1_public_chat = device_1_home.get_chat_view()
        if device_1_public_chat.show_stickers_button.is_element_displayed():
            self.errors.append('Sticker button is shown while on Ropsten')

        device_1_home.just_fyi('switch to mainnet')
        device_1_public_chat.get_back_to_home_view()
        device_1_profile, device_2_profile = device_1_home.profile_button.click(), device_2_home.profile_button.click()
        device_2_public_key = device_2_profile.get_public_key_and_username()
        device_1_public_key, device_1_username = device_1_profile.get_public_key_and_username(return_username=True)

        for device in device_2_profile, device_1_profile:
            device.switch_network('Mainnet with upstream RPC')
        device_1_home.get_chat('#' + chat_name).click()

        device_1_home.just_fyi('install free sticker pack and use it in public chat')
        device_1_public_chat.show_stickers_button.click()
        device_1_public_chat.get_stickers.click()
        device_1_public_chat.install_sticker_pack_by_name('Status Cat')
        device_1_public_chat.back_button.click()
        time.sleep(2)
        device_1_public_chat.swipe_left()
        device_1_public_chat.sticker_icon.click()
        if not device_1_public_chat.sticker_message.is_element_displayed():
            self.errors.append('Sticker was not sent')
        device_1_public_chat.swipe_right()
        if not device_1_public_chat.sticker_icon.is_element_displayed():
            self.errors.append('Sticker is not shown in recently used list')
        device_1_public_chat.get_back_to_home_view()

        device_1_home.just_fyi('send stickers in 1-1 chat from Recent')
        device_1_one_to_one_chat = device_1_home.add_contact(device_2_public_key)
        device_1_one_to_one_chat.show_stickers_button.click()
        device_1_one_to_one_chat.sticker_icon.click()
        if not device_1_one_to_one_chat.chat_item.is_element_displayed():
            self.errors.append('Sticker was not sent from Recent')

        device_2_home.just_fyi('check that can install stickers by tapping on sticker message')
        device2_one_to_one_chat = device_2_home.get_chat(device_1_username).click()
        device2_one_to_one_chat.chat_item.click()
        if not device2_one_to_one_chat.element_by_text_part('Status Cat').is_element_displayed():
            self.errors.append('Stickerpack is not available for installation after tapping on sticker message')
        device2_one_to_one_chat.element_by_text_part('Free').click()
        if device2_one_to_one_chat.element_by_text_part('Free').is_element_displayed():
            self.errors.append('Stickerpack was not installed')

        device_2_home.just_fyi('check that can navigate to another user profile via long tap on sticker message')
        device2_one_to_one_chat.cross_icon.click()
        device2_one_to_one_chat.chat_item.long_press_element()
        device2_one_to_one_chat.element_by_text('View Details').click()
        if not device2_one_to_one_chat.profile_add_to_contacts.is_element_displayed():
            self.errors.append('No navigate to user profile after tapping View Details on sticker message')


        self.errors.verify_no_errors()

    @marks.testrail_id(6305)
    @marks.critical
    def test_image_in_one_to_one_send_save_reply(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()

        device_1_home.just_fyi('start 1-1 chat')
        device_1_profile, device_2_profile = device_1_home.profile_button.click(), device_2_home.profile_button.click()
        device_2_public_key = device_2_profile.get_public_key_and_username()
        device_1_public_key, device_1_username = device_1_profile.get_public_key_and_username(return_username=True)
        image_description = 'description'
        [home.click() for home in [device_1_profile.home_button, device_2_profile.home_button]]
        device_1_chat = device_1_home.add_contact(device_2_public_key)

        device_1_home.just_fyi('send image in 1-1 chat from Gallery, check options for sender')
        device_1_chat.show_images_button.click()
        device_1_chat.allow_button.click()
        device_1_chat.first_image_from_gallery.click()
        if not device_1_chat.cancel_send_image_button.is_element_displayed():
            self.errors.append("Can't cancel sending images, expected image preview is not shown!")
        device_1_chat.chat_message_input.set_value(image_description)
        device_1_chat.send_message_button.click()
        device_1_chat.chat_message_input.click()
        for message in device_1_chat.image_chat_item, device_1_chat.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for sender')
        device_1_chat.show_images_button.click()
        device_1_chat.image_from_gallery_button.click()
        device_1_chat.click_system_back_button()
        device_1_chat.image_chat_item.long_press_element()
        for element in device_1_chat.reply_message_button, device_1_chat.save_image_button:
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on own image messages')
        if device_1_chat.view_profile_button.is_element_displayed():
            self.errors.append('Options are not shown on long-press on image messages')

        device_2_home.just_fyi('check image, description and options for receiver')
        device_2_chat = device_2_home.get_chat(device_1_username).click()
        for message in device_2_chat.image_chat_item, device_2_chat.chat_element_by_text(image_description):
            if not message.is_element_displayed():
                self.errors.append('Image or description is not shown in chat after sending for receiver')
        device_2_chat.image_chat_item.long_press_element()
        for element in device_2_chat.reply_message_button, device_2_chat.save_image_button:
            if not element.is_element_displayed():
                self.errors.append('Save and reply are not available on long-press on received image messages')

        device_1_home.just_fyi('save image')
        device_1_chat.save_image_button.click()
        device_1_chat.show_images_button.click_until_presence_of_element(device_1_chat.image_from_gallery_button)
        device_1_chat.image_from_gallery_button.click()
        device_1_chat.wait_for_element_starts_with_text('Recent')
        if not device_1_chat.recent_image_in_gallery.is_element_displayed():
            self.errors.append('Saved image is not shown in Recent')

        device_2_home.just_fyi('reply to image message')
        device_2_chat.reply_message_button.click()
        if device_2_chat.quote_username_in_message_input.text != "↪ %s" % device_1_username:
            self.errors.append("Username is not displayed in reply quote snippet replying to image message")
        reply_to_message_from_receiver = "image reply"
        device_2_chat.send_message(reply_to_message_from_receiver)
        reply_message = device_2_chat.chat_element_by_text(reply_to_message_from_receiver)
        if not reply_message.image_in_reply.is_element_displayed():
            self.errors.append("Image is not displayed in reply")

        self.errors.verify_no_errors()

    @marks.testrail_id(6316)
    @marks.critical
    def test_send_audio_message_with_push_notification_check(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user(enable_notifications=True)
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        public_key_2 = home_2.get_public_key_and_username()

        home_2.just_fyi("Put app on background (to check Push notification received for audio message)")
        home_2.click_system_home_button()

        home_2.just_fyi("Sending audio message to device who is on background")
        chat_1 = home_1.add_contact(public_key_2)
        chat_1.record_audio_message(message_length_in_seconds=125)
        if not chat_1.element_by_text("Maximum recording time reached").is_element_displayed():
            self.drivers[0].fail("Exceeded 2 mins limit of recording time.")
        else:
            chat_1.ok_button.click()
        if chat_1.audio_message_recorded_time.text != "1:59":
            self.errors.append("Timer exceed 2 minutes")
        chat_1.send_message_button.click()

        device_2.open_notification_bar()
        chat_2 = home_2.click_upon_push_notification_by_text("audio message")

        listen_time = 5

        device_2.home_button.click()
        home_2.get_chat(default_username_1).click()
        chat_2.play_audio_message(listen_time)
        if chat_2.audio_message_in_chat_timer.text not in ("00:05", "00:06", "00:07", "00:08"):
            self.errors.append("Listened 5 seconds but timer shows different listened time in audio message")

        self.errors.verify_no_errors()


    @marks.testrail_id(5316)
    @marks.critical
    def test_add_to_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_1 = device_1_home.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        device_1_home = profile_1.get_back_to_home_view()

        device_2_public_key = device_2_home.get_public_key_and_username()
        profile_2 = device_2_home.get_profile_view()
        # TODO: skip until edit image profile is enabled
        # file_name = 'sauce_logo.png'
        # profile_2.edit_profile_picture(file_name)
        default_username_2 = profile_2.default_username_text.text
        profile_2.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key + ' ')
        message = 'hello'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()

        chat_element = device_2_home.get_chat(default_username_1)
        chat_element.wait_for_visibility_of_element()
        device_2_chat = chat_element.click()
        if not device_2_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message with text '%s' was not received" % message)
        device_2_chat.connection_status.wait_for_invisibility_of_element(60)
        device_2_chat.add_to_contacts.click()

        device_2_chat.get_back_to_home_view()
        device_2_home.plus_button.click()
        device_2_contacts = device_2_home.start_new_chat_button.click()
        if not device_2_contacts.element_by_text(default_username_1).is_element_displayed():
            self.errors.append('%s is not added to contacts' % default_username_1)

        if device_1_chat.user_name_text.text != default_username_2:
            self.errors.append("Default username '%s' is not shown in one-to-one chat" % default_username_2)
        device_1_chat.chat_options.click()
        device_1_chat.view_profile_button.click()
        # TODO: skip until edit image profile is enabled
        # if not device_1_chat.contact_profile_picture.is_element_image_equals_template(file_name):
        #     self.errors.append("Updated profile picture is not shown in one-to-one chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(5373)
    @marks.high
    def test_send_and_open_links(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        default_username_1 = profile_1.default_username_text.text
        home_1 = profile_1.get_back_to_home_view()
        public_key_2 = home_2.get_public_key_and_username()
        home_2.home_button.click()

        chat_1 = home_1.add_contact(public_key_2)
        url_message = 'http://status.im'
        chat_1.chat_message_input.send_keys(url_message)
        chat_1.send_message_button.click()
        chat_1.get_back_to_home_view()
        chat_2 = home_2.get_chat(default_username_1).click()
        chat_2.element_starts_with_text(url_message, 'button').click()
        web_view = chat_2.open_in_status_button.click()
        try:
            web_view.find_full_text('Private, Secure Communication')
        except TimeoutException:
            self.errors.append('Device 2: URL was not opened from 1-1 chat')
        web_view.back_to_home_button.click()
        chat_2.home_button.click()

        chat_name = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        home_1.join_public_chat(chat_name)
        home_2.join_public_chat(chat_name)
        chat_2.chat_message_input.send_keys(url_message)
        chat_2.send_message_button.click()
        chat_1.element_starts_with_text(url_message, 'button').click()
        web_view = chat_1.open_in_status_button.click()
        try:
            web_view.find_full_text('Private, Secure Communication')
        except TimeoutException:
            self.errors.append('Device 1: URL was not opened from 1-1 chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(5362)
    @marks.medium
    def test_unread_messages_counter_1_1_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = device_1.create_user(), device_2.create_user()
        profile_2 = device_2_home.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        device_2_home = profile_2.get_back_to_home_view()
        device_1_public_key = device_1_home.get_public_key_and_username()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)

        message = 'test message'
        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()

        if device_1_home.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')

        chat_element = device_1_home.get_chat(default_username_2)
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')

        chat_element.click()
        device_1_home.get_back_to_home_view()

        if device_1_home.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')

        if chat_element.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element for already seen message')
        self.errors.verify_no_errors()

    @marks.testrail_id(5425)
    @marks.medium
    # TODO: should be completed with quoting after fix 9480
    def test_markdown_support_in_messages(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user()
        profile_2 = device_2_home.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        device_2_home = profile_2.get_back_to_home_view()
        device_1_public_key = device_1_home.get_public_key_and_username()
        device_1_home.home_button.click()

        device_2_chat = device_2_home.add_contact(device_1_public_key)
        device_2_chat.chat_message_input.send_keys('test message')
        device_2_chat.send_message_button.click()
        device_1_chat = device_1_home.get_chat(default_username_2).click()

        markdown = {
            'bold text in asterics' : '**',
            'bold text in underscores' : '__',
            'italic text in asteric': '*',
            'italic text in underscore' : '_',
            'inline code' : '`',
            'code blocks' : '```',
            # 'quote reply (one row)' : '>',
        }

        for message, symbol in markdown.items():
            device_1_home.just_fyi('checking that "%s" is applied (%s) in 1-1 chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            device_2_chat.chat_message_input.send_keys(message_to_send)
            device_2_chat.send_message_button.click()
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the sender \n' % message)

            if not device_1_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in 1-1 chat for the recipient \n' % message)

        device_1_chat.get_back_to_home_view()
        device_2_chat.get_back_to_home_view()
        chat_name = device_1_home.get_random_chat_name()
        device_1_home.join_public_chat(chat_name)
        device_2_home.join_public_chat(chat_name)

        for message, symbol in markdown.items():
            device_1_home.just_fyi('checking that "%s" is applied (%s) in public chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            device_2_chat.chat_message_input.send_keys(message_to_send)
            device_2_chat.send_message_button.click()
            if not device_2_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in public chat for the sender \n' % message)

            if not device_1_chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is not displayed with markdown in public chat for the recipient \n' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(5385)
    @marks.high
    def test_timestamp_in_chats(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home, device_2_home = sign_in_1.create_user(), sign_in_2.create_user()
        device_2_public_key = device_2_home.get_public_key_and_username()
        device_2_home.home_button.click()
        device_1_profile = device_1_home.profile_button.click()
        default_username_1 = device_1_profile.default_username_text.text
        device_1_profile.home_button.click()

        device_1_chat = device_1_home.add_contact(device_2_public_key)

        device_1_chat.just_fyi('check user picture and timestamps in chat for sender and recipient in 1-1 chat')
        message = 'test text'
        device_1_chat.chat_message_input.send_keys(message)
        device_1_chat.send_message_button.click()
        sent_time = device_1_chat.convert_device_time_to_chat_timestamp()

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the sender')
        if device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the sender')

        device_2_chat = device_2_home.get_chat(default_username_1).click()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in 1-1 chat for the recipient')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in 1-1 chat for the recipient')
        # TODO: disabled due to issue 'yesterday' is shown, can't emulate manually
        # for chat in device_1_chat, device_2_chat:
        #     chat.verify_message_is_under_today_text(message, self.errors)

        device_1_chat.just_fyi('check user picture and timestamps in chat for sender and recipient in public chat')
        chat_name = device_1_home.get_random_chat_name()
        for chat in device_1_chat, device_2_chat:
            home_view = chat.get_back_to_home_view()
            home_view.join_public_chat(chat_name)

        device_2_chat.chat_message_input.send_keys(message)
        device_2_chat.send_message_button.click()
        sent_time = device_2_chat.convert_device_time_to_chat_timestamp()
        if not device_2_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the sender')
        if device_2_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is displayed in public chat for the sender')

        if not device_1_chat.chat_element_by_text(message).contains_text(sent_time):
            self.errors.append('Timestamp is not displayed in public chat for the recipient')
        if not device_1_chat.chat_element_by_text(message).member_photo.is_element_displayed():
            self.errors.append('Member photo is not displayed in public chat for the recipient')
        # TODO: disabled due to issue 'yesterday' is shown, can't emulate manually
        # for chat in device_1_chat, device_2_chat:
        #     chat.verify_message_is_under_today_text(message, self.errors)

        self.errors.verify_no_errors()


class TestMessagesOneToOneChatSingle(SingleDeviceTestCase):

    @marks.testrail_id(5317)
    @marks.critical
    def test_copy_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(''.join(random.choice(string.ascii_lowercase) for _ in range(7)))
        chat = sign_in.get_chat_view()
        message_text = {'text_message': 'mmmeowesage_text'}
        formatted_message = {'message_with_link':'https://status.im',
                        'message_with_tag': '#successishere'}
        message_input = chat.chat_message_input
        message_input.send_keys(message_text['text_message'])
        chat.send_message_button.click()

        chat.copy_message_text(message_text['text_message'])

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text['text_message']:
            self.errors.append('Message %s text was not copied in a public chat' % message_text['text_message'])
        message_input.clear()

        #TODO: uncomment after #11168 and 11161 is fixed
        #for message in formatted_message:
        #    message_input.send_keys(formatted_message[message])
        #    chat.send_message_button.click()

        #    message_bubble = chat.chat_element_by_text(formatted_message[message])
        #    message_bubble.timestamp_message.long_press_element()
        #    chat.element_by_text('Copy').click()

        #    message_input.paste_text_from_clipboard()
        #    if message_input.text != formatted_message[message]:
        #        self.errors.append('Message %s text was not copied in a public chat' % formatted_message[message])
        #    message_input.clear()

        self.errors.verify_no_errors()

    @marks.testrail_id(5322)
    @marks.medium
    def test_delete_cut_and_paste_messages(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(transaction_senders['N']['public_key'])

        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)

        message_input.delete_last_symbols(2)
        current_text = message_input.text
        if current_text != message_text[:-2]:
            self.driver.fail("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        message_input.cut_text()

        message_input.paste_text_from_clipboard()
        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2]).wait_for_visibility_of_element(2)

    @marks.testrail_id(5328)
    @marks.critical
    def test_send_emoji(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.join_public_chat(home.get_random_chat_name())
        chat = sign_in.get_chat_view()
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in public chat')

        chat.get_back_to_home_view()
        home.add_contact(transaction_senders['O']['public_key'])
        chat.chat_message_input.send_keys(emoji.emojize(emoji_name))
        chat.send_message_button.click()

        if not chat.chat_element_by_text(emoji_unicode).is_element_displayed():
            self.errors.append('Message with emoji was not sent in 1-1 chat')
        self.errors.verify_no_errors()


    @marks.testrail_id(5783)
    @marks.critical
    def test_can_use_purchased_stickers_on_recovered_account(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(ens_user['passphrase'])

        sign_in_view.just_fyi('switch to Mainnet')
        profile_view = home_view.profile_button.click()
        profile_view.switch_network('Mainnet with upstream RPC')

        sign_in_view.just_fyi('join to public chat, buy and install stickers')
        chat = home_view.join_public_chat(home_view.get_random_chat_name())
        chat.show_stickers_button.click()
        chat.get_stickers.click()
        chat.install_sticker_pack_by_name('Tozemoon')
        chat.back_button.click()

        sign_in_view.just_fyi('check that can use installed pack')
        time.sleep(2)
        chat.swipe_left()
        chat.sticker_icon.click()
        if not chat.chat_item.is_element_displayed():
            self.driver.fail('Sticker was not sent')

    @marks.testrail_id(5403)
    @marks.critical
    def test_start_chat_with_ens_mention_in_one_to_one(self):
        home = SignInView(self.driver).create_user()

        home.just_fyi('Start new chat with public key and check user profile from 1-1 header > options')
        chat = home.add_contact(ens_user_ropsten['public_key'])
        chat.chat_options.click_until_presence_of_element(chat.view_profile_button)
        chat.view_profile_button.click()
        for element in (chat.profile_block_contact, chat.remove_from_contacts, chat.profile_send_message):
            if not element.is_element_displayed():
                self.errors.append('Expected %s is not visible' % element.locator)
        chat.get_back_to_home_view()

        home.just_fyi('Start new chat with ENS and check that ENS is resolved')
        ens = ens_user_ropsten['ens']
        home.add_contact(ens, add_in_contacts=False)
        if not chat.element_by_text("@" + ens).is_element_displayed():
            self.driver.fail('Wrong user is resolved from username when starting 1-1 chat.')

        home.just_fyi('Mention user by ENS in 1-1 chat')
        message = '@%s hey!' % ens
        chat.send_message(message)
        chat.chat_element_by_text(message).click()
        if not chat.profile_block_contact.is_element_displayed():
            self.errors.append('No redirect to user profile after tapping on message with mention (ENS) in 1-1 chat')

        home.just_fyi('Set nickname and mention user by nickname in 1-1 chat')
        russian_nickname = 'МОЙ дорогой ДРУх'
        chat.set_nickname(russian_nickname)
        chat.back_button.click()
        chat.select_mention_from_suggestion_list(russian_nickname + ' @' + ens)
        chat.chat_element_by_text('%s hey!' % russian_nickname).click()
        if not chat.profile_block_contact.is_element_displayed():
            self.errors.append('No redirect to user profile after tapping on message with mention (nickname) in 1-1 chat')
        self.errors.verify_no_errors()


    @marks.testrail_id(6298)
    @marks.medium
    def test_can_scan_qr_with_chat_key_from_home_start_chat(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(basic_user['passphrase'])
        profile = home_view.profile_button.click()
        profile.switch_network()

        url_data = {
            'ens_with_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s.stateofus.eth' % ens_user['ens'],
                'username': ens_user['username']
            },
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user['ens'],
                'username': ens_user['username']
            },
            'ens_another_domain_deep_link': {
                'url': 'status-im://u/%s' % ens_user['ens_another_domain'],
                'username': ens_user['username']
            },
            'own_profile_key_deep_link': {
                'url': 'https://join.status.im/u/%s' % basic_user['public_key'],
                'error': "That's you"
            },
            'other_user_profile_key_deep_link':{
                'url': 'https://join.status.im/u/%s' % ens_user['public_key'],
                'username': ens_user['username']
            },
            'other_user_profile_key_deep_link_invalid':{
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Please enter or scan a valid chat key'
            },
            # TODO: comment until clarification case with scanning QR with ENS names only
            # 'ens_another_domain':{
            #     'url': ens_user['ens_another_domain'],
            #     'username': ens_user['username']
            # },
            'own_profile_key': {
                'url': basic_user['public_key'],
                'error': "That's you"
            },
            # 'ens_without_stateofus_domain': {
            #     'url': ens_user['ens'],
            #     'username': ens_user['username']
            # },
            'other_user_profile_key': {
                'url': ens_user['public_key'],
                'username': ens_user['username']
            },
            'other_user_profile_key_invalid': {
                    'url': '%s123' % ens_user['public_key'],
                    'error': 'Please enter or scan a valid chat key'
            },
        }

        for key in url_data:
            home_view.plus_button.click_until_presence_of_element(home_view.start_new_chat_button)
            contact_view = home_view.start_new_chat_button.click()
            sign_in_view.just_fyi('Checking scanning qr for "%s" case' % key)
            contact_view.scan_contact_code_button.click()
            if contact_view.allow_button.is_element_displayed():
                contact_view.allow_button.click()
            contact_view.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            from views.chat_view import ChatView
            chat_view = ChatView(self.driver)
            if url_data[key].get('error'):
                if not chat_view.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat_view.ok_button.click()
            if url_data[key].get('username'):
                if not chat_view.chat_message_input.is_element_displayed():
                    self.errors.append('In "%s" case chat input is not found after scanning, so no redirect to 1-1' % key)
                if not chat_view.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In "%s" case "%s" not found after scanning' % (key, url_data[key]['username']))
                chat_view.back_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(6322)
    @marks.medium
    def test_can_scan_different_links_with_universal_qr_scanner(self):
        sign_in_view = SignInView(self.driver)
        user = transaction_senders['C']
        home_view = sign_in_view.recover_access(user['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.home_button.click()
        send_transaction_view = SendTransactionView(self.driver)

        url_data = {
            'ens_without_stateofus_domain_deep_link': {
                'url': 'https://join.status.im/u/%s' % ens_user_ropsten['ens'],
                'username': ens_user_ropsten['username']
            },

            'other_user_profile_key_deep_link': {
                'url': 'status-im://u/%s' % basic_user['public_key'],
                'username': basic_user['username']
            },
            'other_user_profile_key_deep_link_invalid': {
                'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
                'error': 'Unable to read this code'
            },
            'own_profile_key': {
                'url': user['public_key'],
                'username': user['username']
            },
            'other_user_profile_key': {
                'url': ens_user['public_key'],
                'username': ens_user['username']
            },
            'validation_wrong_address_transaction': {
                'url': 'ethereum:0x744d70fdbe2ba4cf95131626614a1763df805b9e@3/transfer?address=blablabla&uint256=1e10',
                'error': 'Invalid address',
            },
            'eip_ens_for_receiver': {
                'url': 'ethereum:0xc55cf4b03948d7ebc8b9e8bad92643703811d162@3/transfer?address=nastya.stateofus.eth&uint256=1e-1',
                'data': {
                    'asset': 'STT',
                    'amount': '0.1',
                    'address': '0x58d8…F2ff',
                },
            },
            'eip_payment_link': {
                'url': 'ethereum:pay-0xc55cf4b03948d7ebc8b9e8bad92643703811d162@3/transfer?address=0x3d597789ea16054a084ac84ce87f50df9198f415&uint256=1e1',
                'data': {
                    'amount': '10',
                    'asset': 'STT',
                    'address': '0x3D59…F415',
                },
            },
            'dapp_deep_link': {
                'url': 'https://join.status.im/b/simpledapp.eth',
             },
            'dapp_deep_link_https': {
                'url': 'https://join.status.im/b/https://simpledapp.eth',
            },
            'public_chat_deep_link': {
                'url': 'https://join.status.im/baga-ma-2020',
                'chat_name': 'baga-ma-2020'
            },
        }

        for key in url_data:
            home_view.plus_button.click_until_presence_of_element(home_view.start_new_chat_button)
            sign_in_view.just_fyi('Checking %s case' % key)
            if home_view.universal_qr_scanner_button.is_element_displayed():
                home_view.universal_qr_scanner_button.click()
            if home_view.allow_button.is_element_displayed():
                home_view.allow_button.click()
            home_view.enter_qr_edit_box.scan_qr(url_data[key]['url'])
            from views.chat_view import ChatView
            chat_view = ChatView(self.driver)
            if url_data[key].get('error'):
                if not chat_view.element_by_text_part(url_data[key]['error']).is_element_displayed():
                    self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
                chat_view.ok_button.click()
            if url_data[key].get('username'):
                if key == 'own_profile_key':
                    if chat_view.profile_nickname.is_element_displayed():
                        self.errors.append('In %s case was not redirected to own profile' % key)
                else:
                    if not chat_view.profile_nickname.is_element_displayed():
                        self.errors.append('In %s case block user button is not shown' % key)
                if not chat_view.element_by_text(url_data[key]['username']).is_element_displayed():
                    self.errors.append('In %s case username not shown' % key)
            if url_data[key].get('data'):
                actual_data = send_transaction_view.get_values_from_send_transaction_bottom_sheet()
                difference_in_data = url_data[key]['data'].items() - actual_data.items()
                if difference_in_data:
                    self.errors.append(
                        'In %s case returned value does not match expected in %s' % (key, repr(difference_in_data)))
                send_transaction_view.back_button.click()
            if 'dapp' in key:
                home_view.open_in_status_button.click()
                if not chat_view.allow_button.is_element_displayed():
                    self.errors.append('No allow button is shown in case of navigating to Status dapp!')
                chat_view.cross_icon.click()
            if 'public' in key:
                if not chat_view.chat_message_input.is_element_displayed():
                    self.errors.append('No message input is shown in case of navigating to public chat via deep link!')
                if not chat_view.element_by_text_part(url_data[key]['chat_name']).is_element_displayed():
                    self.errors.append('Chat name is not shown in case of navigating to public chat via deep link!')
            chat_view.get_back_to_home_view()

        self.errors.verify_no_errors()

