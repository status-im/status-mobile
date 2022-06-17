import pytest

from tests import marks
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="four_2")
@marks.medium
class TestActivityCenterMultipleDeviceMedium(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1, self.home_2 = self.device_1.create_user(enable_notifications=True), self.device_2.create_user()
        self.public_key_user_1, self.username_1 = self.home_1.get_public_key_and_username(return_username=True)
        self.public_key_user_2, self.username_2 = self.home_2.get_public_key_and_username(return_username=True)
        [self.group_chat_name_1, self.group_chat_name_2, self.group_chat_name_3, self.group_chat_name_4, \
         self.group_chat_name_5] = "GroupChat1", "GroupChat2", "GroupChat3", "GroupChat4", "GroupChat5"

        self.message_from_sender = "Message sender"
        self.home_2.home_button.double_click()
        self.device_2_one_to_one_chat = self.home_2.add_contact(self.public_key_user_1)

    @marks.testrail_id(702183)
    def test_activity_center_reject_chats_no_pn(self):
        self.device_2.just_fyi('Device2 sends a message in 1-1 chat to Device1')
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)

        self.device_1.just_fyi("Device 2: check there is no PN when receiving new message to activity centre")
        self.device_1.put_app_to_background()
        self.device_1.open_notification_bar()
        if self.home_1.element_by_text(self.message_from_sender).is_element_displayed():
            self.errors.append("Push notification with text was received for new message in activity centre")
        self.device_1.click_system_back_button(2)

        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.device_2.just_fyi('Device2 creates Group chat 1 with Device1')
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_1)
        self.home_2.home_button.double_click()
        self.home_1.home_button.double_click()

        self.device_1.just_fyi('Device1 rejects both chats and verifies they disappeared and not in Chats too')
        self.home_1.notifications_unread_badge.wait_and_click(20)
        self.home_1.notifications_select_button.click()
        self.home_1.element_by_text_part(self.username_2[:10]).click()
        self.home_1.element_by_text_part(self.group_chat_name_1).click()
        self.home_1.notifications_reject_and_delete_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is on Activity Center view after action made on it")
        if self.home_1.element_by_text_part(self.group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat is on Activity Center view after action made on it")
            self.home_1.home_button.double_click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is added on home after rejection")
        if self.home_1.element_by_text_part(self.group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat is added on home after rejection")

        self.home_1.just_fyi("Verify there are still no chats after relogin")
        self.home_1.reopen_app()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat appears on Chats view after relogin")
        if self.home_1.element_by_text_part(self.group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat appears on Chats view after relogin")
        self.home_1.notifications_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request reappears back in Activity Center view after relogin")
        if self.home_1.element_by_text_part(self.group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat request reappears back in Activity Center view after relogin")

        self.errors.verify_no_errors()

    @marks.testrail_id(702184)
    def test_activity_center_accept_chats(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.device_2.just_fyi('Device2 sends a message in 1-1 and creates Group chat 2')
        self.home_2.get_chat_from_home_view(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_2)

        self.device_1.just_fyi('Device1 accepts both chats (via Select All button) and verifies they disappeared '
                               'from activity center view but present on Chats view')
        self.home_1.notifications_unread_badge.wait_and_click(20)
        self.home_1.notifications_select_button.click()
        self.home_1.notifications_select_all.click()
        self.home_1.notifications_accept_and_add_button.click()
        if self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request stays on Activity Center view after it was accepted")
        if self.home_1.element_by_text_part(self.group_chat_name_2).is_element_displayed(2):
            self.errors.append("Group chat request stays on Activity Center view after it was accepted")
        self.home_1.home_button.double_click()
        if not self.home_1.element_by_text_part(self.username_2[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is not added on home after accepted from Activity Center")
        if not self.home_1.element_by_text_part(self.group_chat_name_2).is_element_displayed(2):
            self.errors.append("Group chat is not added on home after accepted from Activity Center")

        self.errors.verify_no_errors()

    @marks.testrail_id(702187)
    def test_activity_center_accept_chats_only_from_contacts(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        if self.home_1.get_chat_from_home_view(self.username_2).is_element_displayed():
            self.home_1.delete_chat_long_press(self.username_2)

        self.device_1.just_fyi('Device1 sets permissions to accept chat requests only from trusted contacts')
        profile_1 = self.home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.accept_new_chats_from.click()
        profile_1.accept_new_chats_from_contacts_only.click()
        profile_1.profile_button.click()

        self.device_1.just_fyi('Device2 creates 1-1 chat Group chats')
        self.home_2.home_button.double_click()
        self.home_2.get_chat(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_4)

        self.device_1.just_fyi('Device1 check there are no any chats in Activity Center nor Chats view')
        self.home_1.home_button.double_click()
        if self.home_1.element_by_text_part(self.username_2).is_element_displayed() or self.home_1.element_by_text_part(
                self.group_chat_name_4).is_element_displayed():
            self.errors.append("Chats are present on Chats view despite they created by non-contact")
        self.home_1.notifications_button.click()
        if self.home_1.element_by_text_part(self.username_2).is_element_displayed() or self.home_1.element_by_text_part(
                self.group_chat_name_4).is_element_displayed():
            self.errors.append("Chats are present in Activity Center view despite they created by non-contact")

        self.device_1.just_fyi('Device1 adds Device2 in Contacts so chat requests should be visible now')
        self.home_1.home_button.double_click()
        self.home_1.add_contact(self.public_key_user_2)

        self.device_1.just_fyi('Device2 creates 1-1 chat Group chats once again')
        self.home_2.home_button.double_click()
        self.home_2.get_chat_from_home_view(self.username_1).click()
        self.device_2_one_to_one_chat.send_message(self.message_from_sender)
        self.device_2_one_to_one_chat.home_button.double_click()
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_5)

        self.device_1.just_fyi('Device1 verifies 1-1 chat Group chats are visible')
        self.home_1.home_button.double_click()
        if not self.home_1.element_by_text_part(
                self.username_2).is_element_displayed() or not self.home_1.element_by_text_part(
            self.group_chat_name_5).is_element_displayed():
            self.errors.append("Chats are not present on Chats view while they have to!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702185)
    def test_activity_center_notifications_on_mentions_in_groups_and_empty_state(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        self.device_2.just_fyi('Device2 creates Group chat 3')
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_3)
        self.home_2.home_button.double_click()

        self.home_1.just_fyi("Device1 joins Group chat 3")
        group_chat_1 = self.home_1.get_chat(self.group_chat_name_3).click()
        group_chat_1.join_chat_button.click()
        group_chat_1.home_button.double_click()

        self.home_2.just_fyi("Device2 mentions Device1 in Group chat 3")
        chat_2 = self.home_2.get_chat_from_home_view(self.group_chat_name_3).click()
        chat_2.select_mention_from_suggestion_list(self.username_1, self.username_1[:2])
        chat_2.send_as_keyevent("group")
        group_chat_message = self.username_1 + " group"
        chat_2.send_message_button.click()

        self.home_1.just_fyi("Device1 checks unread indicator on Activity center bell")
        if not self.home_1.notifications_unread_badge.is_element_displayed():
            self.errors.append("Unread badge is NOT shown after receiving mentions from Group")
        self.home_1.notifications_unread_badge.click_until_absense_of_element(self.home_1.plus_button)

        self.home_1.just_fyi("Check that notification from group is presented in Activity Center")
        if not self.home_1.get_chat_from_activity_center_view(
                self.username_2).chat_message_preview == group_chat_message:
            self.errors.append("No mention in Activity Center for Group Chat")

        self.home_1.just_fyi("Open group chat where user mentioned")
        self.home_1.get_chat_from_activity_center_view(self.username_2).click()
        self.home_1.home_button.double_click()

        self.home_1.just_fyi("Check there are no unread messages counter on chats after message is read")
        if (self.home_1.notifications_unread_badge.is_element_present() or
                self.home_1.get_chat_from_home_view(self.group_chat_name_3).new_messages_counter.text == "1"):
            self.errors.append("Unread message indicator is kept after message is read in chat")

        self.home_1.just_fyi("Check there is an empty view on Activity Center")
        self.home_1.notifications_button.click()
        if not self.home_1.element_by_translation_id('empty-activity-center').is_element_present():
            self.errors.append("Activity Center still has some chats after user opened all of them")

        self.errors.verify_no_errors()
