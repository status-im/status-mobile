import datetime
import random

import emoji
import pytest
from _pytest.outcomes import Failed
from appium.webdriver.connectiontype import ConnectionType
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, run_in_parallel, pytest_config_global, transl
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.chat_view import CommunityView
from views.dbs.waku_backup import user as waku_user
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_one_1")
@marks.new_ui_critical
class TestCommunityOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.username = 'first user'

        self.home = self.sign_in.create_user(username=self.username)
        self.home.communities_tab.click_until_presence_of_element(self.home.plus_community_button)
        self.community_name = "closed community"
        self.channel_name = "cats"
        self.community = self.home.create_community(community_type="closed")

        self.home.get_chat(self.community_name, community=True).click()
        self.community_view = self.home.get_community_view()
        self.channel = self.community_view.get_channel(self.channel_name).click()

    @marks.testrail_id(703503)
    @marks.xfail(reason="Curated communities not loading, https://github.com/status-im/status-mobile/issues/17852",
                 run=False)
    def test_community_discovery(self):
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.discover_communities_button.click()
        self.home.community_card_item.wait_for_visibility_of_element(30)

        if len(self.home.community_card_item.find_elements()) > 1:
            contributors_test_community_attributes = "Test Community", 'Open for anyone', 'Web3', 'Software dev'
            for text in contributors_test_community_attributes:
                if not self.home.element_by_text(text).is_element_displayed(10):
                    self.errors.append("'%s' text is not in Discovery!" % text)
            self.home.element_by_text(contributors_test_community_attributes[0]).click()
            element_templates = {
                self.community_view.join_button: 'discovery_join_button.png',
                self.community_view.get_channel_avatar(): 'discovery_general_channel.png',
            }
            for element, template in element_templates.items():
                if element.is_element_differs_from_template(template):
                    element.save_new_screenshot_of_element('%s_different.png' % template.split('.')[0])
                    self.errors.append(
                        "Element %s is different from expected template %s!" % (element.locator, template))
            self.community_view.navigate_back_to_home_view()
            self.home.communities_tab.click()
            self.home.discover_communities_button.click()
            self.home.community_card_item.wait_for_visibility_of_element(30)
            self.home.swipe_up()

        status_ccs_community_attributes = '(old) Status CCs', 'Community for Status CCs', 'Ethereum', \
            'Software dev', 'Web3'
        for text in status_ccs_community_attributes:
            if not self.community_view.element_by_text(text).is_element_displayed(10):
                self.errors.append("'%s' text is not shown for (old) Status CCs!" % text)
        self.errors.verify_no_errors()

    @marks.testrail_id(702846)
    def test_community_navigate_to_channel_when_relaunch(self):
        text_message = 'some_text'
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)

        self.channel.send_message(text_message)
        self.channel.chat_element_by_text(text_message).wait_for_visibility_of_element()
        self.channel.reopen_app()
        if not self.channel.chat_element_by_text(text_message).is_element_displayed(30):
            self.drivers[0].fail("Not navigated to channel view after reopening app")

    @marks.testrail_id(702742)
    def test_community_copy_and_paste_message_in_chat_input(self):
        message_texts = ['mmmeowesage_text', 'https://status.im']
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)

        for message in message_texts:
            self.channel.send_message(message)
            self.channel.copy_message_text(message)
            actual_copied_text = self.channel.driver.get_clipboard_text()
            if actual_copied_text != message:
                self.errors.append(
                    'Message %s text was not copied in community channel, text in clipboard %s' % actual_copied_text)

        self.errors.verify_no_errors()

    @marks.testrail_id(702869)
    def test_community_undo_delete_message(self):
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)
        message_to_delete = "message to delete and undo"
        self.channel.send_message(message_to_delete)
        self.channel.delete_message_in_chat(message_to_delete)
        self.channel.element_by_text("Undo").click()
        try:
            self.channel.chat_element_by_text(message_to_delete).wait_for_visibility_of_element()
        except TimeoutException:
            pytest.fail("Message was not restored by clicking 'Undo' button")
        if self.channel.element_starts_with_text("Message deleted").is_element_displayed():
            pytest.fail("Text about deleted message is shown in the chat")

    @marks.testrail_id(703382)
    def test_community_mute_community_and_channel(self):
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.just_fyi("Mute community and check that channels are also muted")
        self.home.mute_chat_long_press(chat_name=self.community_name, mute_period="mute-for-1-hour", community=True)
        device_time = self.home.driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_times = [current_time + datetime.timedelta(minutes=i) for i in range(59, 62)]
        expected_texts = ["Muted until %s %s" % (
            exp_time.strftime('%H:%M'), "today" if current_time.hour < 23 else "tomorrow"
        ) for exp_time in expected_times]

        self.home.get_chat(self.community_name, community=True).long_press_element()
        self.home.unmute_community_button.wait_for_visibility_of_element()
        try:
            current_text = self.home.unmute_community_button.unmute_caption_text
            if current_text not in expected_texts:
                self.errors.append("Text '%s' is not shown for muted community" % expected_texts[1])
        except NoSuchElementException:
            self.errors.append("Caption with text 'Muted until...' is not shown for muted community")
        self.home.click_system_back_button()

        self.home.get_chat(self.community_name, community=True).click()
        self.community_view.get_channel(self.channel_name).long_press_element()
        self.home.mute_channel_button.wait_for_visibility_of_element()
        try:
            current_text = self.home.mute_channel_button.unmute_caption_text
            if current_text not in expected_texts:
                self.errors.append("Text '%s' is not shown for a channel in muted community" % expected_texts[1])
        except NoSuchElementException:
            self.errors.append("Caption with text 'Muted until...' is not shown for a channel in muted community")

        self.home.just_fyi("Unmute channel and check that the community is also unmuted")
        self.home.mute_channel_button.click()
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.get_chat(self.community_name, community=True).long_press_element()
        if not self.home.element_by_text("Mute community").is_element_displayed():
            self.errors.append("Community is not unmuted when channel is unmuted")
        self.home.click_system_back_button()

        self.home.just_fyi("Mute channel and check that community is not muted")
        self.home.get_chat(self.community_name, community=True).click()
        self.home.mute_chat_long_press(chat_name=self.channel_name, mute_period="mute-for-1-week",
                                       community_channel=True)
        device_time = self.home.driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_times = [current_time + datetime.timedelta(days=7, minutes=i) for i in range(-1, 2)]
        expected_texts = ["Muted until %s" % exp_time.strftime('%H:%M %a %-d %b') for exp_time in expected_times]
        self.community_view.get_channel(self.channel_name).long_press_element()
        self.home.mute_channel_button.wait_for_visibility_of_element()
        try:
            current_text = self.home.mute_channel_button.unmute_caption_text
            if current_text not in expected_texts:
                self.errors.append("Text '%s' is not shown for a muted community channel" % expected_texts[1])
        except NoSuchElementException:
            self.errors.append("Caption with text '%s' is not shown for a muted community channel" % expected_texts[1])
        self.home.click_system_back_button()
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.get_chat(self.community_name, community=True).long_press_element()
        if self.home.element_by_text_part("Muted until").is_element_displayed() or \
                self.home.mute_community_button.text != transl["mute-community"]:
            self.errors.append("Community is muted when channel is muted")
        self.home.click_system_back_button()

        self.errors.verify_no_errors()

    @marks.testrail_id(703133)
    def test_restore_multiaccount_with_waku_backup_remove_switch(self):
        self.home.reopen_app(sign_in=False)
        self.home.just_fyi("Restore user with predefined communities and contacts")
        self.sign_in.recover_access(passphrase=waku_user.seed, second_user=True)

        self.home.just_fyi("Check contacts/blocked users")
        self.home.chats_tab.click()
        self.home.contacts_tab.click()
        contacts_number = self.home.get_contact_rows_count()
        # Todo: enable when https://github.com/status-im/status-mobile/issues/18096 is fixed
        # if contacts_number != len(waku_user.contacts):
        #     self.errors.append(
        #         "Incorrect contacts number restored: %s instead of %s" % (contacts_number, len(waku_user.contacts)))
        for contact in waku_user.contacts:
            if not self.home.element_by_text(contact).is_element_displayed(30):
                self.errors.append('%s was not restored as a contact from waku backup!' % contact)
                # Disabled for simple check as sometimes from waku-backup users restored with 3-random names
                # self.home.click_system_back_button_until_element_is_shown()
                # contact_row = self.home.contact_details_row(index=i + 1)
                # shown_name_text = contact_row.username_text.text
                # if shown_name_text in waku_user.contacts:
                #     waku_user.contacts.remove(shown_name_text)
                #     continue
                # else:
                #     contact_row.click()
                #     shown_name_text = profile.default_username_text.text
                #     if shown_name_text in waku_user.contacts:
                #         waku_user.contacts.remove(shown_name_text)
                #         continue
                #     else:
                #         chat = self.home.get_chat_view()
                #         chat.profile_send_message_button.click()
                #         for name in waku_user.contacts:
                #             if chat.element_starts_with_text(name).is_element_displayed(sec=20):
                #                 waku_user.contacts.remove(name)
                #                 continue
        # if waku_user.contacts:
        #     self.errors.append(
        #         "Contact(s) was (were) not restored from backup: %s!" % ", ".join(waku_user.contacts))

        self.home.just_fyi("Check restored communities")
        self.home.communities_tab.click()
        for key in ['admin_open', 'member_open', 'admin_closed', 'member_closed']:
            if not self.home.element_by_text(waku_user.communities[key]).is_element_displayed(30):
                self.errors.append("%s was not restored from waku-backup!!" % key)
        # TODO: there is a bug when pending community sometimes restored as joined; needs investigation
        # self.home.opened_communities_tab.click()
        # if not self.home.element_by_text(waku_user.communities['member_pending']).is_element_displayed(30):
        #     self.errors.append("Pending community %s was not restored from waku-backup!" % waku_user.communities['member_pending'])

        if not pytest_config_global['pr_number']:
            self.home.just_fyi("Perform back up")
            self.home.navigate_back_to_home_view()
            profile = self.home.profile_button.click()
            profile.profile_legacy_button.scroll_and_click()
            profile.sync_settings_button.click()
            profile.backup_settings_button.click()
            profile.perform_backup_button.click()

        self.home.just_fyi("Check that can login with different user")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.wait_and_click()
        self.sign_in.element_by_text(self.username).click()
        self.sign_in.sign_in()
        self.home.communities_tab.click()
        if self.home.element_by_text(waku_user.communities['admin_open']).is_element_displayed(30):
            self.errors.append("Community of previous user is shown!")

        self.home.just_fyi("Check that can remove user from logged out state")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.wait_and_click()
        user_card = self.sign_in.get_user(username=self.username)
        user_card.open_user_options()
        self.sign_in.remove_profile_button.click()
        if not self.sign_in.element_by_translation_id("remove-profile-confirm-message").is_element_displayed(30):
            self.errors.append("Warning is not shown on removing profile!")
        self.sign_in.element_by_translation_id("remove").click()

        self.home.just_fyi("Check that removed user is not shown in the list anymore")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.wait_and_click()
        if self.sign_in.element_by_text(self.username).is_element_displayed():
            self.errors.append("Removed user is re-appeared after relogin!")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_three_2")
@marks.new_ui_critical
class TestCommunityMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.username_1, self.username_2 = "user_1", "user_2"
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'username': self.username_2}))))
        self.homes = self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.public_key_2 = self.home_2.get_public_key()
        self.profile_1 = self.home_1.get_profile_view()
        [home.navigate_back_to_home_view() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)
        self.text_message = 'hello'

        # self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.home_1.get_chat(self.username_2).wait_for_visibility_of_element()
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        # self.chat_2.send_message(self.text_message)
        # [home.click_system_back_button_until_element_is_shown() for home in self.homes]
        self.home_1.navigate_back_to_home_view()

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = "open community"
        self.channel_name = 'general'
        self.home_1.create_community(community_type="open")
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)
        self.channel_1.send_message(self.text_message)

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.share_community(self.community_name, self.username_2)
        self.home_1.get_to_community_channel_from_home(self.community_name)

        self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.chat_2.send_message(self.text_message)
        self.community_2.join_community()
        self.channel_2 = self.community_2.get_channel(self.channel_name).click()
        self.channel_2.chat_message_input.wait_for_visibility_of_element(20)

    @marks.testrail_id(702838)
    def test_community_message_send_check_timestamps_sender_username(self):
        message = self.text_message
        sent_time_variants = self.channel_1.convert_device_time_to_chat_timestamp()
        timestamp = self.channel_1.chat_element_by_text(message).timestamp
        if sent_time_variants and timestamp:
            if timestamp not in sent_time_variants:
                self.errors.append("Timestamp is not shown, expected: '%s', in fact: '%s'" %
                                   (", ".join(sent_time_variants), timestamp))
        self.channel_1.verify_message_is_under_today_text(message, self.errors)
        self.channel_2.send_message("one more message")
        new_message = "new message"
        self.channel_1.send_message(new_message)
        self.channel_2.verify_message_is_under_today_text(new_message, self.errors, 60)
        if self.channel_2.chat_element_by_text(new_message).username.text != self.username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % self.username_1)
        self.errors.verify_no_errors()

    @marks.testrail_id(702843)
    def test_community_message_edit(self):
        message_before_edit, message_after_edit = 'Message BEFORE edit', "Message AFTER edit 2"
        self.channel_1.send_message(message_before_edit)
        self.channel_1.edit_message_in_chat(message_before_edit, message_after_edit)
        for channel in (self.channel_1, self.channel_2):
            if not channel.element_by_text_part(message_after_edit).is_element_displayed(60):
                self.errors.append('Message is not edited')
        message_text_after_edit = message_after_edit + ' (Edited)'
        self.channel_2.set_reaction(message_text_after_edit)
        try:
            self.channel_1.chat_element_by_text(message_text_after_edit).emojis_below_message().wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Message reaction is not shown for the sender")
        self.errors.verify_no_errors()

    @marks.testrail_id(702839)
    def test_community_message_delete(self):
        message_to_delete_everyone = 'delete for everyone'
        message_to_delete_for_me = 'delete for me'
        self.channel_2.send_message(message_to_delete_everyone)
        self.home_2.just_fyi('Delete for message everyone. Checking that message is deleted for all members')
        self.channel_2.delete_message_in_chat(message_to_delete_everyone)
        for channel in (self.channel_1, self.channel_2):
            if not channel.chat_element_by_text(message_to_delete_everyone).is_element_disappeared(30):
                self.errors.append("Deleted message is shown in channel")
        if not self.channel_2.element_by_translation_id('message-deleted-for-everyone').is_element_displayed(30):
            self.errors.append("System message about deletion for everyone is not displayed")

        self.home_2.just_fyi(
            'Deleting message for me. Checking that message is deleted only for the author of the message')
        self.channel_2.send_message(message_to_delete_for_me)
        self.channel_1.chat_element_by_text(message_to_delete_for_me).wait_for_element(120)
        self.channel_2.delete_message_in_chat(message_to_delete_for_me, everyone=False)
        if not self.channel_2.chat_element_by_text(message_to_delete_for_me).is_element_disappeared(30):
            self.errors.append("Deleted for me message is shown in channel for the author of message")
        if not self.channel_2.element_by_translation_id('message-deleted-for-you').is_element_displayed(30):
            self.errors.append("System message about deletion for you is not displayed")
        if not self.channel_1.chat_element_by_text(message_to_delete_for_me).is_element_displayed(30):
            self.errors.append("Deleted for me message is deleted all channel members")
        self.errors.verify_no_errors()

    @marks.testrail_id(703194)
    def test_community_several_images_send_reply(self):
        self.home_1.just_fyi('Send several images in 1-1 chat from Gallery')
        image_description, file_name = 'gallery', 'gallery_1.png'
        self.channel_1.send_images_with_description(image_description, [0, 1])

        self.channel_2.just_fyi("Check gallery on second device")
        self.channel_2.navigate_back_to_home_view()
        self.home_2.get_to_community_channel_from_home(self.community_name)
        chat_element = self.channel_2.chat_element_by_text(image_description)
        try:
            chat_element.wait_for_visibility_of_element(120)
            received = True
            if chat_element.image_container_in_message.is_element_differs_from_template(file_name, 5):
                self.errors.append("Gallery message do not match the template!")
        except TimeoutException:
            self.errors.append("Gallery message was not received")
            received = False

        if received:
            self.channel_2.just_fyi("Checking an ability to save and share an image from gallery")
            chat_element.image_container_in_message.image_by_index(1).click()
            if not self.channel_2.share_image_icon_button.is_element_displayed():
                self.errors.append("Can't share an image from gallery.")
            if self.channel_2.view_image_options_button.is_element_displayed():
                self.channel_2.view_image_options_button.click()
                if not self.channel_2.save_image_icon_button.is_element_displayed():
                    self.errors.append("Can't save an image from gallery.")
            else:
                self.errors.append("Image options button is not shown for an image from gallery.")

            self.channel_2.navigate_back_to_chat_view()

            self.channel_2.just_fyi("Can reply to gallery")
            self.channel_2.quote_message(image_description)
            message_text = 'reply to gallery'
            self.channel_2.chat_message_input.send_keys(message_text)
            self.channel_2.send_message_button.click()
            chat_element_1 = self.channel_1.chat_element_by_text(message_text)
            if not chat_element_1.is_element_displayed(
                    sec=60) or chat_element_1.replied_message_text != image_description:
                self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(702859)
    def test_community_one_image_send_reply(self):
        self.home_1.just_fyi('Send image in 1-1 chat from Gallery')
        image_description = 'description'
        self.channel_1.send_images_with_description(image_description)

        self.home_2.just_fyi('Check image, description and options for receiver')
        if not self.channel_2.chat_element_by_text(image_description).is_element_displayed(60):
            self.channel_2.hide_keyboard_if_shown()
        self.channel_2.chat_element_by_text(image_description).wait_for_visibility_of_element(10)
        if not self.channel_2.chat_element_by_text(
                image_description).image_in_message.is_element_image_similar_to_template('image_sent_in_community.png'):
            self.errors.append("Not expected image is shown to the receiver")

        if not self.channel_1.chat_element_by_text(image_description).is_element_displayed(60):
            self.channel_1.hide_keyboard_if_shown()
        self.channel_1.chat_element_by_text(image_description).image_in_message.click()
        self.home_1.just_fyi('Save image')
        self.channel_1.view_image_options_button.click()
        self.channel_1.save_image_icon_button.click()
        toast_element = self.channel_1.toast_content_element
        if toast_element.is_element_displayed():
            toast_element_text = toast_element.text
            if toast_element_text != self.channel_1.get_translation_by_key("photo-saved"):
                self.errors.append(
                    "Shown message '%s' doesn't match expected '%s' after saving an image." % (
                        toast_element_text, self.channel_1.get_translation_by_key("photo-saved")))
        else:
            self.errors.append("Message about saving a photo is not shown.")
        self.channel_1.navigate_back_to_chat_view()

        self.channel_1.just_fyi("Check that image is saved in gallery")
        self.channel_1.show_images_button.click()
        self.channel_1.allow_all_button.click_if_shown()
        if not self.channel_1.get_image_by_index(0).is_element_image_similar_to_template(
                "sauce_dark_image_gallery.png"):
            self.errors.append('Saved image is not shown in Recent')
        self.channel_1.click_system_back_button()

        self.home_2.just_fyi('Check share option on opened image')
        self.channel_2.chat_element_by_text(image_description).image_in_message.click()
        self.channel_2.share_image_icon_button.click()
        self.channel_2.element_starts_with_text("Drive").click()
        try:
            self.channel_2.wait_for_current_package_to_be('com.google.android.apps.docs')
        except TimeoutException:
            self.errors.append("Can't share image")
        self.channel_2.navigate_back_to_chat_view()

        self.channel_2.just_fyi("Can reply to images")
        self.channel_2.quote_message(image_description)
        message_text = 'reply to image'
        self.channel_2.chat_message_input.send_keys(message_text)
        self.channel_2.send_message_button.click()
        chat_element_1 = self.channel_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=60) or chat_element_1.replied_message_text != image_description:
            self.errors.append('Reply message was not received by the sender')
        self.channel_2.just_fyi("Set a reaction for the image message")
        self.channel_2.set_reaction(message=image_description)
        try:
            self.channel_1.chat_element_by_text(image_description).emojis_below_message().wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Image message reaction is not shown for the sender")
        self.channel_1.just_fyi("Set a reaction for the message reply")
        self.channel_2.set_reaction(message=image_description, emoji="love")
        try:
            self.channel_2.chat_element_by_text(message_text).emojis_below_message(
                emoji="love").wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Reply message reaction is not shown for the reply sender")

        self.errors.verify_no_errors()

    @marks.testrail_id(702840)
    def test_community_emoji_send_copy_paste_reply(self):
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        emoji_message = emoji.emojize(emoji_name)
        self.channel_1.send_message(emoji_message)
        for channel in self.channel_1, self.channel_2:
            if not channel.chat_element_by_text(emoji_unicode).is_element_displayed(30):
                self.errors.append('Message with emoji was not sent or received in community channel')

        self.channel_1.just_fyi("Can copy and paste emojis")
        self.channel_1.copy_message_text(emoji_unicode)
        actual_copied_text = self.channel_1.driver.get_clipboard_text()
        if actual_copied_text != emoji_unicode:
            self.errors.append('Emoji message was not copied, text in clipboard is %s' % actual_copied_text)

        self.channel_1.just_fyi("Can reply to emojis")
        self.channel_2.quote_message(emoji_unicode)
        message_text = 'test message'
        self.channel_2.chat_message_input.send_keys(message_text)
        self.channel_2.send_message_button.click()
        chat_element_1 = self.channel_1.chat_element_by_text(message_text)
        chat_element_1.wait_for_element(60)
        if chat_element_1.replied_message_text != emoji_unicode:
            self.errors.append('Reply message is not reply to original message!')
        self.errors.verify_no_errors()

    @marks.testrail_id(702844)
    def test_community_links_with_previews_github_youtube_twitter_gif_send_enable(self):
        preview_urls = {
            # TODO: disabled because of the bug in 15891
            # 'giphy':{'url': 'https://giphy.com/gifs/this-is-fine-QMHoU66sBXqqLqYvGO',
            #               'title': 'This Is Fine GIF - Find & Share on GIPHY',
            #               'description': 'Discover & share this Meme GIF with everyone you know. GIPHY is how you search, share, discover, and create GIFs.',
            #               'link': 'giphy.com'},
            'github_pr': {'url': 'https://github.com/status-im/status-mobile/pull/11707',
                          'title': 'Update translations by jinhojang6 · Pull Request #11707 · status-im/status-mobile',
                          'description': 'Update translation json files of 19 languages.',
                          'link': 'github.com'},
            'yotube_short': {
                'url': 'https://youtu.be/Je7yErjEVt4',
                'title': 'Status, your gateway to Ethereum',
                'description': 'Learn more at https://status.im. This video aims to provide an explanation '
                               'and brief preview of the utility that will be supported by the Status App - provid...',
                'link': 'youtu.be'},
            'yotube_full': {
                'url': 'https://www.youtube.com/watch?v=XN-SVmuJH2g&list=PLbrz7IuP1hrgNtYe9g6YHwHO6F3OqNMao',
                'title': 'Status & Keycard – Hardware-Enforced Security',
                'description': 'With Status and Keycard, you can enable hardware enforced authorizations to '
                               'your Status account and transactions. Two-factor authentication to access your ac...',
                'link': 'www.youtube.com'},
            'yotube_mobile': {
                'url': 'https://m.youtube.com/watch?v=Je7yErjEVt4',
                'title': 'Status, your gateway to Ethereum',
                'description': 'Learn more at https://status.im. This video aims to provide an explanation '
                               'and brief preview of the utility that will be supported by the Status App - provid...',
                'link': 'm.youtube.com',
            },

            # twitter link is temporary removed from check as current xpath locator in message.preview_title is not applicable for this type of links
            # 'twitter': {
            #     'url': 'https://twitter.com/ethdotorg/status/1445161651771162627?s=20',
            #     'txt': "We've rethought how we translate content, allowing us to translate",
            #     'subtitle': 'Twitter'
            # }
        }
        for home in self.home_1, self.home_2:
            if not home.chat_floating_screen.is_element_displayed():
                home.navigate_back_to_home_view()
                home.get_to_community_channel_from_home(self.community_name)

        for key, data in preview_urls.items():
            self.home_2.just_fyi("Checking %s preview case" % key)
            url = data['url']
            self.channel_2.chat_message_input.send_keys(url)
            self.channel_2.url_preview_composer.wait_for_element(20)
            shown_title = self.channel_2.url_preview_composer_text.text
            if shown_title != data['title']:
                self.errors.append("Preview text is not expected, it is '%s'" % shown_title)
            self.channel_2.send_message_button.click()
            message = self.channel_1.get_preview_message_by_text(url)
            message.wait_for_element(60)
            if not message.preview_image:
                self.errors.append("No preview image is shown for %s" % url)
            shown_title = message.preview_title.text
            if shown_title != data['title']:
                self.errors.append("Title is not equal expected for '%s', actual is '%s'" % (url, shown_title))
            shown_description = message.preview_subtitle.text
            if shown_description != data['description']:
                self.errors.append(
                    "Description is not equal expected for '%s', actual is '%s'" % (url, shown_description))
            shown_link = message.preview_link.text
            if shown_link != data['link']:
                self.errors.append("Link is not equal expected for '%s', actual is '%s'" % (url, shown_link))

        self.channel_1.just_fyi("Set reaction and check it")
        message_with_reaction = list(preview_urls.values())[-1]['url']
        self.channel_1.set_reaction(message=message_with_reaction, emoji="laugh")
        if not self.channel_2.chat_element_by_text(message_with_reaction).emojis_below_message(
                emoji="laugh").is_element_displayed(10):
            self.channel_2.hide_keyboard_if_shown()
        try:
            self.channel_2.chat_element_by_text(message_with_reaction).emojis_below_message(
                emoji="laugh").wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Link message reaction is not shown for the sender")

        self.errors.verify_no_errors()

    @marks.testrail_id(702841)
    def test_community_unread_messages_badge(self):
        self.channel_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        message = 'test message'
        if not self.home_2.chat_floating_screen.is_element_displayed():
            self.home_2.navigate_back_to_home_view()
            self.home_2.communities_tab.click()
            self.home_2.get_to_community_channel_from_home(self.community_name)
        self.channel_2.send_message(message)
        self.home_1.just_fyi('Check new messages badge is shown for community')
        community_element_1 = self.home_1.get_chat(self.community_name, community=True)
        if not community_element_1.new_messages_grey_dot.is_element_displayed(sec=30):
            self.errors.append('New message community badge is not shown')

        community_1 = community_element_1.click()
        channel_1_element = community_1.get_channel(self.channel_name)

        self.home_1.just_fyi('Check new messages badge is shown for community')
        if not community_element_1.new_messages_grey_dot.is_element_displayed():
            self.errors.append('New messages channel badge is not shown on channel')
        channel_1_element.click()
        self.errors.verify_no_errors()

    @marks.xfail(reason="Message can be missed after unblock: https://github.com/status-im/status-mobile/issues/16873")
    @marks.testrail_id(702894)
    def test_community_contact_block_unblock_offline(self):
        for i, channel in enumerate([self.channel_1, self.channel_2]):
            if not channel.chat_message_input.is_element_displayed():
                channel.navigate_back_to_home_view()
                self.homes[i].communities_tab.click()
                self.homes[i].get_to_community_channel_from_home(self.community_name)

        self.channel_1.send_message('message to get avatar of user 2 visible in next message')

        self.channel_2.just_fyi("Sending message before block")
        message_to_disappear = "I should not be in chat"
        self.channel_2.send_message(message_to_disappear)

        self.chat_1.just_fyi('Block user')
        self.channel_1.chat_element_by_text(message_to_disappear).wait_for_visibility_of_element(30)
        chat_element = self.channel_1.chat_element_by_text(message_to_disappear)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.channel_1.block_contact()

        self.chat_1.just_fyi('Check that messages from blocked user are hidden in public chat and close app')
        app_package = self.device_1.driver.current_package
        if not self.chat_1.chat_element_by_text(message_to_disappear).is_element_disappeared(30):
            self.errors.append("Messages from blocked user is not cleared in public chat ")
        self.chat_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()
        if not self.home_1.element_by_translation_id("no-messages").is_element_displayed():
            self.errors.append("1-1 chat from blocked user is not removed and messages home is not empty!")
        self.chat_1.driver.set_network_connection(ConnectionType.AIRPLANE_MODE)

        self.home_2.just_fyi('Send message to public chat while device 1 is offline')
        message_blocked, message_unblocked = "Message from blocked user", "Hurray! unblocked"
        self.channel_2.send_message(message_blocked)

        self.chat_1.just_fyi('Check that new messages from blocked user are not delivered')
        self.chat_1.driver.set_network_connection(ConnectionType.ALL_NETWORK_ON)
        # self.home_1.jump_to_card_by_text('# %s' % self.channel_name)
        self.home_1.communities_tab.click()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.home_1.get_chat(self.channel_name, community_channel=True).click()
        for message in message_to_disappear, message_blocked:
            if self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    "'%s' from blocked user is fetched from offline in community channel" % message)

        self.chat_1.just_fyi('Unblock user and check that can see further messages')
        # TODO: still no blocked users in new UI
        profile_1 = self.home_1.get_profile_view()
        self.home_1.navigate_back_to_home_view()
        self.chat_1.profile_button.click()
        profile_1.logout_button.scroll_to_element()
        profile_1.profile_legacy_button.click()
        profile_1.contacts_button.wait_and_click()
        profile_1.blocked_users_button.wait_and_click()
        profile_1.element_by_text(self.username_2).click()
        self.chat_1.unblock_contact_button.click()
        self.chat_1.close_button.click()
        self.chat_1.navigate_back_to_home_view()

        self.home_2.just_fyi("Check that can send message in community after unblock")
        self.chat_2.send_message(message_unblocked)
        # self.home_1.jump_to_card_by_text('# %s' % self.channel_name)
        self.home_1.communities_tab.click()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.home_1.get_chat(self.channel_name, community_channel=True).click()
        self.chat_1.hide_keyboard_if_shown()
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(120):
            self.errors.append("%s was not received in public chat after user unblock!" % message_unblocked)
            self.errors.verify_no_errors()

        self.home_1.just_fyi("Add blocked user to contacts again after removing(removed automatically when blocked)")
        chat_element = self.channel_1.chat_element_by_text(message_unblocked)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.channel_1.profile_add_to_contacts_button.click()
        self.home_2.just_fyi("Accept contact request after being unblocked")
        self.home_2.navigate_back_to_home_view()
        self.home_2.handle_contact_request(self.username_1)
        self.channel_1.profile_send_message_button.click_until_absense_of_element(
            desired_element=self.channel_1.profile_send_message_button, attempts=20, timeout=3)
        try:
            self.chat_1.send_message("piy")
            unblocked = True
        except TimeoutException:
            unblocked = False
            self.errors.append("Chat with unblocked user was not enabled after 1 minute")

        if unblocked:
            self.home_2.just_fyi("Check message in 1-1 chat after unblock")
            self.home_2.chats_tab.click()
            self.home_2.get_chat(self.username_1).click()
            self.chat_2.send_message(message_unblocked)
            try:
                self.chat_2.chat_element_by_text(message_unblocked).wait_for_status_to_be(expected_status='Delivered',
                                                                                          timeout=120)
                if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(30):
                    self.errors.append("Message was not received in 1-1 chat after user unblock!")
            except TimeoutException:
                self.errors.append('Message was not delivered after back up online.')

        self.errors.verify_no_errors()

    @marks.testrail_id(703086)
    def test_community_mark_all_messages_as_read(self):
        for home in self.home_1, self.home_2:
            home.navigate_back_to_home_view()
            home.communities_tab.click()
        self.home_2.get_chat(self.community_name, community=True).click()
        self.community_2.get_channel(self.channel_name).click()
        self.channel_2.send_message(self.text_message)
        community_1_element = self.community_1.get_chat(self.community_name, community=True)
        if not community_1_element.new_messages_grey_dot.is_element_displayed(90):
            self.errors.append('New messages counter is not shown in home > Community element')
        community_1_element.click()
        channel_1_element = self.community_1.get_chat(self.channel_name, community_channel=True)
        if not channel_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append("New messages counter is not shown in community channel element")
        self.community_1.click_system_back_button()
        mark_as_read_button = self.community_1.mark_all_messages_as_read_button
        self.home_1.community_floating_screen.wait_for_invisibility_of_element()
        community_1_element.long_press_until_element_is_shown(mark_as_read_button)
        mark_as_read_button.click()
        if community_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append(
                'Unread messages badge is shown in community element while there are no unread messages')
        community_1_element.click()
        if channel_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append(
                "New messages badge is shown in community channel element while there are no unread messages")
        self.errors.verify_no_errors()

    @marks.testrail_id(704615)
    def test_community_edit_delete_message_when_offline(self):
        self.channel_2.just_fyi("Sending messages for edit and delete")
        message_to_edit, message_to_delete = "message to edit", "message to delete"
        self.channel_2.send_message(message_to_edit)
        self.channel_2.send_message(message_to_delete)
        self.home_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.community_1.get_channel(self.channel_name).click()
        self.channel_1.just_fyi("Receiver is checking if initial messages were delivered")
        for message in message_to_edit, message_to_delete:
            if not self.channel_1.chat_element_by_text(message).is_element_displayed(30):
                self.channel_1.driver.fail("Message '%s' was not received")

        self.channel_2.just_fyi("Turning on airplane mode and editing/deleting messages")
        self.channel_2.driver.set_network_connection(ConnectionType.AIRPLANE_MODE)
        message_after_edit = "text after edit"
        self.channel_2.edit_message_in_chat(message_to_edit, message_after_edit)
        self.channel_2.delete_message_in_chat(message_to_delete)
        self.channel_2.just_fyi("Turning on network connection")
        self.channel_2.driver.set_network_connection(ConnectionType.ALL_NETWORK_ON)

        self.channel_1.just_fyi("Receiver is checking if messages were updated and deleted")
        if not self.channel_1.chat_element_by_text(message_after_edit).is_element_displayed(30):
            self.errors.append("Updated message '%s' is not delivered to the receiver" % message_after_edit)
        if not self.channel_1.chat_element_by_text(message_to_delete).is_element_disappeared():
            self.errors.append("Message '%s' was not deleted for the receiver" % message_to_delete)
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_five_2")
@marks.new_ui_critical
class TestCommunityMultipleDeviceMergedTwo(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.username_1, self.username_2 = "user_1", "user_2"
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_2}))))
        self.homes = self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.public_key_2 = self.home_2.get_public_key()
        self.profile_1 = self.home_1.get_profile_view()
        [home.navigate_back_to_home_view() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)
        self.text_message = 'hello'

        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.home_1.navigate_back_to_home_view()

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = "open community"
        self.channel_name = 'general'
        self.home_1.create_community(community_type="open")

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.share_community(self.community_name, self.username_2)
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)

        self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.chat_2.send_message(self.text_message)
        self.community_2.join_community()
        self.channel_2 = self.community_2.get_channel(self.channel_name).click()

    @marks.testrail_id(702786)
    def test_community_mentions_push_notification(self):
        self.home_1.navigate_back_to_home_view()
        self.device_1.open_notification_bar()

        self.device_2.just_fyi("Invited member sends a message with a mention")
        self.channel_2.send_message("hi")
        self.channel_2.mention_user(self.username_1)
        self.channel_2.send_message_button.click()

        self.device_1.just_fyi("Admin gets push notification with the mention and tap it")
        message_received = False
        if self.home_1.get_pn(self.username_1):
            message_received = True
            self.device_1.click_upon_push_notification_by_text(self.username_1)
            if not self.channel_1.chat_element_by_text(self.username_1).is_element_displayed():
                if self.channel_1.chat_message_input.is_element_displayed():
                    self.errors.append("Message with the mention is not shown in the chat for the admin")
                else:
                    self.errors.append(
                        "Channel did not open by clicking on a notification with the mention for admin")
        else:
            self.errors.append("Push notification with the mention was not received by admin")

        if not self.channel_1.chat_message_input.is_element_displayed():
            self.channel_1.navigate_back_to_home_view()
            self.home_1.communities_tab.click()
            self.home_1.get_to_community_channel_from_home(self.community_name)

        if message_received:
            self.channel_1.just_fyi("Set reaction for the message with a mention")
            self.channel_1.set_reaction(message=self.username_1, emoji="sad")
            try:
                self.channel_2.chat_element_by_text(self.username_1).emojis_below_message(
                    emoji="sad").wait_for_element_text(1)
            except (Failed, NoSuchElementException):
                self.errors.append("Message reaction is not shown for the sender")

        self.device_2.just_fyi("Sender edits the message with a mention")
        chat_element = self.channel_2.chat_element_by_text(self.username_1)
        chat_element.wait_for_sent_state()
        chat_element.long_press_element_by_coordinate()
        edit_done = False
        expected_message = ""
        try:
            self.channel_2.element_by_translation_id("edit-message").click()
            for i in range(29, 32):
                self.channel_2.driver.press_keycode(i)
            input_text = self.channel_2.chat_message_input.text
            if 'cba' in input_text:  # sometimes method press_keycode adds symbols to the beginning of line
                expected_message = "0 cba (Edited)"
            else:
                expected_message = "0 abc (Edited)"
            self.channel_2.send_message_button.click()
            edit_done = True
            if chat_element.message_body_with_mention.text != expected_message:
                self.errors.append("Edited message is not shown correctly for the sender")
        except NoSuchElementException:
            self.errors.append("Can not edit a message with a mention")
        if edit_done:
            element = self.channel_1.chat_element_by_text(self.username_1).message_body_with_mention
            if not element.is_element_displayed(10) or element.text != expected_message:
                self.errors.append("Edited message is not shown correctly for the (receiver) admin")

        self.home_2.navigate_back_to_home_view()
        if not self.channel_1.chat_message_input.is_element_displayed():
            self.channel_1.navigate_back_to_home_view()
            self.home_1.communities_tab.click()
            self.home_1.get_chat(self.community_name, community=True).click()
            self.community_1.get_channel(self.channel_name).click()

        self.device_1.just_fyi("Admin sends a message with a mention")
        self.channel_1.mention_user(self.username_2)
        self.channel_1.send_message_button.click()
        self.device_2.just_fyi("Invited member gets push notification with the mention and tap it")
        self.device_2.open_notification_bar()
        push_notification_element = self.home_2.get_pn(self.username_2)
        if push_notification_element:
            push_notification_element.click()
            if not self.channel_2.chat_element_by_text(self.username_2).is_element_displayed():
                if self.channel_2.chat_message_input.is_element_displayed():
                    self.errors.append("Message with the mention is not shown in the chat for the invited member")
                else:
                    self.errors.append(
                        "Channel did not open by clicking on a notification with the mention for the invited member")
        else:
            self.errors.append("Push notification with the mention was not received by the invited member")
        self.errors.verify_no_errors()

    @marks.testrail_id(702809)
    def test_community_markdown_support(self):
        markdown = {
            'bold text in asterics': '**',
            'bold text in underscores': '__',
            'italic text in asteric': '*',
            'italic text in underscore': '_',
            'inline code': '`',
            'code blocks': '```',
            'quote reply (one row)': '>',
        }

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.jump_to_communities_home()
            community = home.get_chat(self.community_name, community=True).click()
            community.get_channel(self.channel_name).click()

        for message, symbol in markdown.items():
            self.home_1.just_fyi('Checking that "%s" is applied (%s) in community channel' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.channel_2.send_message(message_to_send)
            if not self.channel_2.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    '%s is not displayed with markdown in community channel for the sender (device 2) \n' % message)

            if not self.channel_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    '%s is not displayed with markdown in community channel for the recipient (device 1) \n' % message)

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.chats_tab.click()
            home.recent_tab.click()

        self.home_1.get_chat(self.username_2).click()
        self.home_2.get_chat(self.username_1).click()

        for message, symbol in markdown.items():
            self.home_1.just_fyi('Checking that "%s" is applied (%s) in 1-1 chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.chat_1.send_message(message_to_send)
            if not self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    '%s is not displayed with markdown in 1-1 chat for the sender (device 1) \n' % message)

            if not self.chat_2.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    '%s is not displayed with markdown in 1-1 chat for the recipient (device 2) \n' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(702845)
    def test_community_leave(self):
        self.home_2.navigate_back_to_home_view()
        self.home_2.jump_to_communities_home()
        community = self.home_2.get_chat(self.community_name, community=True)
        community_to_leave = CommunityView(self.drivers[1])
        community.long_press_until_element_is_shown(community_to_leave.leave_community_button)
        community_to_leave.leave_community_button.click()
        community_to_leave.leave_community_button.click()
        if not community.is_element_disappeared():
            self.errors.append('Community is still shown in the list after leave')
        self.errors.verify_no_errors()

    @marks.testrail_id(702948)
    @marks.xfail(
        reason="Can't navigate to a channel by hashtag link, https://github.com/status-im/status-mobile/issues/18095")
    def test_community_hashtag_links_to_community_channels(self):
        for home in self.homes:
            home.navigate_back_to_home_view()
        self.home_2.jump_to_messages_home()
        self.home_1.jump_to_communities_home()

        self.home_1.just_fyi("Device 1 creates a closed community")
        self.home_1.create_community(community_type="closed")
        community_name = "closed community"
        self.community_1.share_community(community_name, self.username_2)

        self.home_2.just_fyi("Device 2 joins the community")
        self.home_2.get_chat(self.username_1).click()
        control_message_1_1_chat = "it is just a message text"
        self.chat_2.send_message(control_message_1_1_chat)
        self.community_2.join_community(open_community=False)

        self.home_1.just_fyi("Device 1 accepts the community request")
        self.home_1.jump_to_communities_home()
        try:
            self.home_1.notifications_unread_badge.wait_for_visibility_of_element(120)
        except TimeoutException:
            self.errors.append("Unread indicator is not shown in notifications on membership request")
        self.home_1.open_activity_center_button.click()
        reply_element = self.home_1.get_element_from_activity_center_view(self.username_2)
        reply_element.title.swipe_right_on_element(width_percentage=2.5)
        self.home_1.activity_notification_swipe_button.click()
        self.home_1.close_activity_centre.click()

        dogs_channel, cats_channel = "dogs", "cats"
        cats_message = "Where is a cat?"

        self.home_1.just_fyi("Device 1 sends a message in the cats channel")
        self.home_1.get_to_community_channel_from_home(community_name=community_name, channel_name=cats_channel)
        self.channel_1.send_message(cats_message)
        self.channel_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        self.home_1.get_to_community_channel_from_home(community_name=community_name, channel_name=dogs_channel)

        self.home_1.just_fyi("Device 1 sends a message with hashtag in the dogs channel")
        message_with_hashtag = "#cats"
        self.channel_1.send_message(message_with_hashtag)

        self.home_2.just_fyi("Device 2 clicks on the message with hashtag in the community channel")
        self.home_2.navigate_back_to_home_view()
        self.home_2.communities_tab.click()
        self.home_2.get_to_community_channel_from_home(community_name, dogs_channel)
        self.channel_2.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if not self.channel_2.chat_element_by_text(cats_message).is_element_displayed(30):
            self.errors.append("Receiver was not navigated to the cats channel")

        self.home_1.just_fyi("Device 1 clicks on the message with hashtag in the community channel")
        self.channel_1.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if not self.channel_1.chat_element_by_text(cats_message).is_element_displayed(30):
            self.errors.append("Sender was not navigated to the cats channel")

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.chats_tab.click()

        self.home_2.just_fyi("Device 2 sends a message with hashtag in 1-1 chat")
        self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(message_with_hashtag)

        self.home_1.just_fyi("Device 1 clicks on the message with hashtag in 1-1 chat")
        self.home_1.get_chat(self.username_2).click()
        self.chat_1.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if self.chat_1.chat_element_by_text(control_message_1_1_chat).is_element_disappeared():
            self.errors.append("Receiver was navigated out of 1-1 chat")

        self.home_2.just_fyi("Device 2 clicks on the message with hashtag in 1-1 chat")
        self.chat_2.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if self.chat_2.chat_element_by_text(control_message_1_1_chat).is_element_disappeared():
            self.errors.append("Sender was navigated out of 1-1 chat")

        self.errors.verify_no_errors()

    @marks.testrail_id(703629)
    def test_community_join_when_node_owner_offline(self):
        for home in self.homes:
            home.navigate_back_to_home_view()
        self.home_2.jump_to_communities_home()
        if self.home_2.get_chat(self.community_name, community=True).is_element_displayed():
            CommunityView(self.home_2.driver).leave_community(self.community_name)
        self.home_1.jump_to_communities_home()

        self.home_1.just_fyi("Device 1 creates open community")
        self.home_1.create_community(community_type="open")
        community_name = "open community"
        self.community_1.share_community(community_name, self.username_2)

        self.home_1.just_fyi("Device 1 goes offline")
        app_package = self.device_1.driver.current_package
        self.device_1.driver.terminate_app(app_package)

        self.home_2.just_fyi("Device 2 requests to join the community")
        self.home_2.jump_to_messages_home()
        self.home_2.get_chat(self.username_1).click()
        self.community_2.join_community(open_community=False)
        exp_text = "You requested to join “%s”" % community_name
        if self.community_2.toast_content_element.is_element_displayed(10):
            cur_text = self.community_2.toast_content_element.text
            if cur_text != exp_text:
                self.errors.append(
                    "Text \"%s\" in shown toast element doesn't match expected \"%s\"" % (cur_text, exp_text))
        else:
            self.errors.append("Toast element with the text \"%s\" doesn't appear" % exp_text)
        if not self.community_2.community_status_pending.is_element_displayed():
            self.errors.append("Pending status is not displayed")
        self.community_2.toast_content_element.wait_for_invisibility_of_element(30)
        self.community_2.close_community_view_button.click()
        self.home_2.pending_communities_tab.click()
        if self.home_2.get_chat(community_name, community=True).is_element_displayed():
            self.home_2.get_chat(community_name, community=True).click()
        else:
            self.errors.append("%s is not listed inside Pending communities tab" % community_name)

        self.home_1.just_fyi("Device 1 goes back online")
        self.home_1.driver.activate_app(app_package)
        self.device_1.sign_in()

        self.home_2.just_fyi("Device 2 checks that he's joined the community")
        exp_text = "You joined “%s”" % community_name
        if self.community_2.toast_content_element.is_element_displayed(60):
            cur_text = self.community_2.toast_content_element.text
            if cur_text != exp_text:
                self.errors.append(
                    "Text \"%s\" in shown toast element doesn't match expected \"%s\"" % (cur_text, exp_text))
        # else:
        #     self.errors.append("Toast element with the text \"%s\" doesn't appear" % exp_text)
        # ToDo: add verification when toast is fixed
        if not self.community_2.community_status_joined.is_element_displayed():
            self.errors.append("Joined status is not displayed")
        self.community_2.close_community_view_button.click()
        self.home_2.joined_communities_tab.click()
        chat_element = self.home_2.get_chat(community_name, community=True)
        if chat_element.is_element_displayed(30):
            chat_element.click()
        else:
            self.errors.append("%s is not listed inside Joined communities tab" % community_name)

        self.errors.verify_no_errors()
