import time

import pytest

from tests import common_password
from tests import marks
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.chat_view import CommunityView
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name='five_2')
@marks.medium
class TestProfileGapsCommunityMediumMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1, self.home_2 = self.device_1.create_user(enable_notifications=True), self.device_2.create_user(enable_notifications=True)
        self.public_key_1, self.default_username_1 = self.home_1.get_public_key()
        self.public_key_2, self.default_username_2 = self.home_2.get_public_key()
        [home.home_button.click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi("Creating 1-1 chats")
        self.chat_1 = self.home_1.add_contact(self.public_key_2)
        self.first_message = 'first message'
        self.chat_2 = self.home_2.add_contact(self.public_key_1)
        [home.home_button.click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi("Creating group chat")
        self.group_chat_name = "gr_chat_%s" % self.home_1.get_random_chat_name()
        self.group_chat_1 = self.home_1.create_group_chat(user_names_to_add=[self.default_username_2],
                                                          group_chat_name=self.group_chat_name)
        self.group_chat_2 = self.home_2.get_chat(self.group_chat_name).click()
        [home.home_button.click() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi("Creating public chats")
        self.public_chat_name = self.home_1.get_random_chat_name()
        self.public_chat_1, self.public_chat_2 = self.home_1.join_public_chat(
            self.public_chat_name), self.home_2.join_public_chat(self.public_chat_name)

        self.home_1.just_fyi("Close the ENS banner")
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        [home.ens_banner_close_button.click_if_shown() for home in (self.home_1, self.home_2)]

    @marks.testrail_id(702281)
    def test_profile_show_profile_picture_and_online_indicator_settings(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]

        self.chat_2.just_fyi('Removing user 1 from contacts')
        self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.chat_options.click()
        self.chat_2.view_profile_button.click()
        self.chat_2.remove_from_contacts.click_until_absense_of_element(self.chat_2.remove_from_contacts)
        self.chat_2.close_button.click()
        self.chat_2.home_button.double_click()

        logo_online, logo_default, logo_chats, logo_group = 'logo_new.png', 'sauce_logo.png', 'logo_chats_view_2.png', 'group_logo.png'
        profile_1 = self.home_1.profile_button.click()

        profile_1.just_fyi("Set user Profile image from Gallery")
        profile_1.edit_profile_picture(file_name=logo_default)
        self.home_1.profile_button.click()
        profile_1.swipe_down()

        profile_1.just_fyi('Set status in profile')
        device_1_status = 'My new update!'
        timeline = profile_1.status_button.click()
        timeline.set_new_status(device_1_status)
        if not timeline.timeline_own_account_photo.is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in timeline')

        profile_1.just_fyi('Check profile image it is not in mentions because user not in contacts yet')
        one_to_one_chat_2 = self.home_2.get_chat(self.default_username_1).click()
        one_to_one_chat_2.chat_message_input.send_keys('@' + self.default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if one_to_one_chat_2.user_profile_image_in_mentions_list(
                self.default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture is updated in 1-1 chat mentions list of contact not in Contacts list')

        profile_1.just_fyi('Check profile image is in mentions because now user was added in contacts')
        one_to_one_chat_2.add_to_contacts.click()
        one_to_one_chat_2.send_message("hey")
        one_to_one_chat_2.chat_message_input.send_keys('@' + self.default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if not one_to_one_chat_2.user_profile_image_in_mentions_list(
                self.default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in 1-1 chat mentions list')
        self.home_1.reopen_app()
        one_to_one_chat_2.get_back_to_home_view()

        profile_1.just_fyi('Check profile image is updated in chat views')
        profile_2 = one_to_one_chat_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(self.default_username_1).click()
        profile_2.online_indicator.wait_for_visibility_of_element(180)
        if not profile_2.profile_picture.is_element_image_similar_to_template('new_profile_online.png'):
            self.errors.append('Profile picture was not updated on user Profile view')
        profile_2.close_button.click()
        self.home_2.home_button.double_click()
        if not self.home_2.get_chat(self.default_username_1).chat_image.is_element_image_similar_to_template(
                logo_chats):
            self.errors.append('User profile picture was not updated on Chats view')

        profile_1.just_fyi('Check profile image updated in user profile view in Group chat views')
        group_chat_message = 'wuuuut'
        self.home_2.get_chat(self.group_chat_name).click()
        self.group_chat_2.send_message('Message', wait_chat_input_sec=10)
        group_chat_1 = self.home_1.get_chat(self.group_chat_name).click()
        group_chat_1.send_message(group_chat_message)
        self.group_chat_2.chat_element_by_text(group_chat_message).wait_for_element(20)
        if not self.group_chat_2.chat_element_by_text(
                group_chat_message).member_photo.is_element_image_similar_to_template(
            logo_default):
            self.errors.append('User profile picture was not updated in message Group chat view')
        self.home_2.put_app_to_background()

        profile_1.just_fyi('Check profile image updated in group chat invite')
        self.home_1.get_back_to_home_view()
        new_group_chat = 'new_gr'
        self.home_2.click_system_back_button()
        self.home_2.open_notification_bar()
        self.home_1.create_group_chat(user_names_to_add=[self.default_username_2], group_chat_name=new_group_chat)

        invite = self.group_chat_2.pn_invited_to_group_chat(self.default_username_1, new_group_chat)
        pn = self.home_2.get_pn(invite)
        if pn:
            if not pn.group_chat_icon.is_element_image_similar_to_template(logo_group):
                self.errors.append("Group chat invite is not updated with custom logo!")
            pn.click()
        else:
            self.errors.append("No invite PN is arrived!")
            self.home_2.click_system_back_button(2)

        profile_1.just_fyi('Check profile image updated in on login view')
        self.home_1.profile_button.click()
        profile_1.logout()
        sign_in_1 = self.home_1.get_sign_in_view()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on Multiaccounts list select login view')
        sign_in_1.element_by_text(self.default_username_1).click()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on account login view')
        sign_in_1.password_input.send_keys(common_password)
        sign_in_1.sign_in_button.click()

        profile_1.just_fyi('Remove user from contact and check there is no profile image displayed')
        self.home_2.profile_button.double_click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(self.default_username_1).click()
        one_to_one_chat_2.remove_from_contacts.click()
        # Send message to User 2 so update of profile image picked up
        group_chat_message = "woho"
        group_chat_1 = self.home_1.get_chat(self.group_chat_name).click()
        group_chat_1.send_message(group_chat_message)
        one_to_one_chat_2.get_back_to_home_view()
        one_to_one_chat_2.home_button.double_click()
        if self.home_2.get_chat(self.default_username_1).chat_image.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture is not returned to default after user removed from Contacts')

        profile_2.just_fyi('Enable to see profile image from "Everyone" setting')
        self.home_2.profile_button.double_click()
        profile_2.privacy_and_security_button.click()
        profile_2.show_profile_pictures_of.scroll_and_click()
        profile_2.element_by_translation_id("everyone").click()
        group_chat_1.send_message(group_chat_message)
        profile_2.home_button.click(desired_view='home')
        if not self.home_2.get_chat(self.default_username_1).chat_image.is_element_image_similar_to_template(
                logo_chats):
            self.errors.append('User profile picture is not shown after user after enabling see profile image from Everyone')
        self.errors.verify_no_errors()

    @marks.testrail_id(702282)
    def test_profile_chat_two_minutes_gap_in_public_and_no_gap_in_1_1_chat_and_group_chat(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        profile_1 = self.home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.sync_history_for_button.click()
        profile_1.element_by_translation_id("two-minutes").click()
        self.device_1.toggle_airplane_mode()
        message_gap = "OOps, that should not be fetched automatically for pub chat"

        self.home_2.just_fyi("Send message to all chats")
        for chat_name in (self.default_username_1, self.group_chat_name, '#%s' % self.public_chat_name):
            self.home_2.get_chat(chat_name).click()
            self.chat_2.send_message(message_gap)
            self.chat_2.home_button.click()

        # Waiting for 3 minutes and then going back online
        time.sleep(130)
        self.device_1.toggle_airplane_mode()

        self.home_1.just_fyi("Checking gap in public chat and fetching messages")
        profile_1.home_button.click()
        pub_chat_1 = self.home_1.get_chat('#%s' % self.public_chat_name).click()
        if pub_chat_1.chat_element_by_text(message_gap).is_element_displayed(10):
            self.errors.append("Test message has been fetched automatically")
        pub_chat_1.element_by_translation_id("fetch-messages").wait_and_click(60)
        if not pub_chat_1.chat_element_by_text(message_gap).is_element_displayed(10):
            self.errors.append("Test message has not been fetched")
        self.home_1.get_back_to_home_view()

        self.home_1.just_fyi("Checking that there is no gap in 1-1/group chat and messages fetched automatically")
        for chat in [self.home_1.get_chat(self.default_username_2), self.home_1.get_chat(self.group_chat_name)]:
            chat_view = chat.click()
            if chat_view.element_by_translation_id("fetch-messages").is_element_displayed(10):
                self.errors.append("Fetch messages button is displayed in {}} chat".format(chat.user_name_text.text))
            if not chat_view.chat_element_by_text(message_gap).is_element_displayed(10):
                self.errors.append(
                    "Message in {} chat has not been fetched automatically".format(chat.user_name_text.text))
            chat_view.back_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702367)
    def test_chat_push_on_mute_unmute_contact(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        chat_1 = self.home_1.get_chat(self.default_username_2).click()
        chat_2 = self.home_2.get_chat(self.default_username_1).click()

        self.home_1.just_fyi('Mute a contact and verify notifications are not received from muted contact')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.profile_mute_contact.scroll_and_click()
        chat_1.close_button.click()
        chat_1.put_app_to_background()

        self.home_2.just_fyi('Muted contact sends a message')
        message_after_mute = 'message after mute'
        chat_2.send_message(message_after_mute)

        self.device_1.open_notification_bar()
        if self.device_1.element_by_text(message_after_mute).is_element_displayed(15):
            self.errors.append("Push notification is received from muted contact")
        # self.device_1.click_system_back_button()
        self.device_1.get_app_from_background()

        chat_1.just_fyi('Verify that message from muted user is actually received')
        if not chat_1.chat_element_by_text(message_after_mute).is_element_displayed():
            self.errors.append("Message from muted contact hasn't been received")

        self.home_1.just_fyi('Unmute contact and verify that notifications are received after unmute')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.profile_unmute_contact.scroll_and_click()
        chat_1.close_button.click()
        chat_1.put_app_to_background()

        self.home_2.just_fyi('Unmuted contact sends a message')
        message_after_unmute = 'message after unmute'
        chat_2.send_message(message_after_unmute)

        self.device_1.open_notification_bar()
        if not self.device_1.element_by_text(message_after_unmute).is_element_displayed(15):
            self.errors.append("Push notification is not received from unmuted contact")
        # self.device_1.click_system_back_button()
        self.device_1.get_app_from_background()

        chat_1.just_fyi('Verify that message from unmuted user is actually received')
        if not chat_1.chat_element_by_text(message_after_unmute).is_element_displayed():
            self.errors.append("Message from unmuted contact hasn't been received")

        self.errors.verify_no_errors()

    @marks.testrail_id(702368)
    def test_chat_reopen_app_on_last_viewed_chat(self):
        self.home_2.home_button.double_click()
        chat_2 = self.home_2.add_contact(self.public_key_1)
        self.home_2.reopen_app()
        if not chat_2.chat_message_input.is_element_displayed():
            self.errors.append('last viewed chat is not opened after app reopening')
        if not chat_2.element_by_text(self.default_username_1).is_element_displayed():
            self.errors.append('wrong chat is opened after app reopening')

        self.errors.verify_no_errors()

    @marks.testrail_id(702283)
    def test_community_create_approve_membership(self):
        [home.home_button.double_click() for home in (self.home_1, self.home_2)]
        community_name, channel_name = "some name", "first_channel"
        community_description, community_pic = "something in community", 'sauce_logo.png'
        message, message_member = "message", "from member"
        community_1 = self.home_1.create_community_e2e(community_name, community_description, set_image=True,
                                                       file_name=community_pic)
        channel_1 = community_1.add_channel(channel_name)
        channel_1.send_message(message)
        self.home_1.home_button.double_click()

        self.home_1.just_fyi("Sending community link to public chat")
        community_1 = self.home_1.get_chat(community_name, community=True).click()
        community_link_text = community_1.copy_community_link()
        pub_1 = self.home_1.get_chat('#%s' % self.public_chat_name).click()
        pub_1.chat_message_input.paste_text_from_clipboard()
        pub_1.send_message_button.click()
        pub_1.get_back_to_home_view()

        self.home_2.just_fyi("Tapping on community link and request membership")
        pub_2 = self.home_2.get_chat('#%s' % self.public_chat_name).click()
        pub_2.element_by_text(community_name).wait_for_element(100)
        community_message_2 = pub_2.get_community_link_preview_by_text(community_link_text)
        if community_message_2.community_description != community_description:
            self.errors.append(
                "Community description '%s' does not match expected" % community_message_2.community_description)
        if community_message_2.community_members_amount != 1:
            self.errors.append("Members amount in resolved message '%s' does not match expected" % str(
                community_message_2.community_members_amount))
        community_message_2.view()
        community_2 = CommunityView(self.drivers[1])
        community_2.request_access_button.click()
        if not community_2.membership_request_pending_text.is_element_displayed():
            self.errors.append("Membership request is not pending")

        self.home_1.just_fyi("Checking pending membership")
        community_1 = self.home_1.get_chat(community_name, community=True).click()
        community_1.community_options_button.click()
        community_1.community_info_button.click()
        community_1.community_membership_request_value.wait_for_element(60)
        if community_1.community_membership_request_value.text != '1':
            self.drivers[0].fail(
                "Membership request value '%s' is not equal expected" % community_1.community_membership_request_value)

        self.home_1.just_fyi("Approve membership")
        community_1.handle_membership_request(self.default_username_2, approve=True)
        if not community_1.element_by_text(self.default_username_2).is_element_displayed():
            self.errors.append(
                "New member %s is not shown as added to community on info page!" % self.default_username_2)
        if not community_2.community_info_picture.is_element_image_similar_to_template(community_pic):
            self.errors.append("Community image is different!")
        channel_2 = community_2.get_chat(channel_name).click()
        if not channel_2.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append("Message was not received in community channel!")
        channel_2.send_message(message_member)
        community_1.home_button.double_click()
        self.home_1.get_chat(community_name, community=True).click()
        chat_element_1 = community_1.get_chat(channel_name)
        if not chat_element_1.new_messages_grey_dot.is_element_displayed():
            self.errors.append("Unread messages counter is not shown for community channel!")
        if not community_1.element_by_text(message_member).is_element_displayed():
            self.errors.append("Message from member is not shown for community channel!")

        self.errors.verify_no_errors()
