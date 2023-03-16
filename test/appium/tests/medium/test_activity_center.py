import pytest

from tests import marks, run_in_parallel
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.sign_in_view import SignInView
from views.chat_view import CommunityView
from selenium.common.exceptions import TimeoutException

@pytest.mark.xdist_group(name="four_2")
@marks.medium
class TestActivityCenterMultipleDeviceMedium(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1, self.home_2 = self.device_1.create_user(enable_notifications=True), self.device_2.create_user()
        self.public_key_user_1, self.username_1 = self.home_1.get_public_key_and_username(return_username=True)
        self.public_key_user_2, self.username_2 = self.home_2.get_public_key_and_username(return_username=True)
        [self.group_chat_name_1, self.group_chat_name_2] = "GroupChat1", "GroupChat2"

        self.message_from_sender = "Message sender"
        self.home_2.home_button.double_click()
        self.device_2_one_to_one_chat = self.home_2.add_contact(self.public_key_user_1)

    @marks.testrail_id(702185)
    def test_activity_center_notifications_on_mentions_in_groups_and_empty_state(self):
        [home.home_button.double_click() for home in [self.home_1, self.home_2]]

        if not self.home_1.element_by_text_part(self.username_2).is_element_displayed():
            self.home_1.handle_contact_request(self.username_2)
            self.home_1.home_button.double_click()

        self.device_2.just_fyi('Device2 creates Group chat 3')
        self.home_2.create_group_chat([self.username_1], group_chat_name=self.group_chat_name_1)
        self.home_2.home_button.double_click()

        self.home_1.just_fyi("Device1 joins Group chat 3")
        group_chat_1 = self.home_1.get_chat(self.group_chat_name_1).click()
        group_chat_1.join_chat_button.click_if_shown()
        group_chat_1.home_button.double_click()

        self.home_2.just_fyi("Device2 mentions Device1 in Group chat 3")
        chat_2 = self.home_2.get_chat_from_home_view(self.group_chat_name_1).click()
        chat_2.select_mention_from_suggestion_list(self.username_1, self.username_1[:2])
        chat_2.send_as_keyevent("group")
        group_chat_message = self.username_1 + " group"
        chat_2.send_message_button.click()

        self.home_1.just_fyi("Device1 checks unread indicator on Activity center bell")
        if not self.home_1.notifications_unread_badge.is_element_displayed():
            self.errors.append("Unread badge is NOT shown after receiving mentions from Group")
        self.home_1.notifications_unread_badge.click_until_absense_of_element(self.home_1.plus_button, 6)

        self.home_1.just_fyi("Check that notification from group is presented in Activity Center")
        if not self.home_1.get_element_from_activity_center_view(
                self.username_2).chat_message_preview == group_chat_message:
            self.errors.append("No mention in Activity Center for Group Chat")

        self.home_1.just_fyi("Open group chat where user mentioned")
        self.home_1.get_element_from_activity_center_view(self.username_2).click()
        self.home_1.home_button.double_click()

        self.home_1.just_fyi("Check there are no unread messages counter on chats after message is read")
        if (self.home_1.notifications_unread_badge.is_element_displayed() or
                self.home_1.get_chat_from_home_view(self.group_chat_name_1).new_messages_counter.text == "1"):
            self.errors.append("Unread message indicator is kept after message is read in chat")

        self.home_1.just_fyi("Check there is an empty view on Activity Center")
        self.home_1.notifications_button.click()
        if not self.home_1.element_by_translation_id('empty-activity-center').is_element_displayed():
            self.errors.append("Activity Center still has some chats after user has opened all of them")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_two_2")
@marks.new_ui_critical
class TestActivityCenterContactRequestMultipleDevicePR(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.profile_1, self.profile_2 = self.home_1.get_profile_view(), self.home_2.get_profile_view()
        users = self.loop.run_until_complete(run_in_parallel(
            ((self.home_1.get_public_key_and_username, True),
             (self.home_2.get_public_key_and_username, True))
        ))
        self.public_key_1, self.default_username_1 = users[0]
        self.public_key_2, self.default_username_2 = users[1]

        self.profile_1.just_fyi("Enabling PNs")
        self.profile_1.switch_push_notifications()
        [home.click_system_back_button_until_element_is_shown() for home in [self.home_1, self.home_2]]
        [home.chats_tab.click() for home in [self.home_1, self.home_2]]

    @marks.testrail_id(702871)
    def test_activity_center_contact_request_cancel_outgoing_no_pn(self):
        self.device_1.put_app_to_background()
        self.device_2.just_fyi('Device2 sends a contact request to Device1')
        self.home_2.add_contact(self.public_key_1)

        self.device_1.just_fyi("Device 2: check there is no PN when receiving new message to activity centre")
        self.device_1.open_notification_bar()
        if self.home_1.element_by_text_part("Please add me to your contacts").is_element_displayed():
            self.errors.append("Push notification with text was received for new message in activity centre")
        self.device_1.click_system_back_button(2)

        self.profile_2.just_fyi("Check outgoing contact request and cancel it")
        for indicator in (self.home_2.notifications_unread_badge, self.home_2.contact_new_badge):
            if not indicator.is_element_displayed():
                self.errors.append("Unread indicator on contacts tab or on activity center is not shown for outgoing CR!")
        self.home_2.contacts_tab.click()
        if self.home_2.pending_contact_request_text.text != '1':
            self.errors.append("The amount of contact requests is not shown for outgoing CR!")
        self.home_2.pending_contact_request_button.click()
        outgoing_cr = self.home_2.get_element_from_activity_center_view(self.default_username_1)
        outgoing_cr.cancel_contact_request()
        self.home_2.close_activity_centre.click()

        self.profile_1.just_fyi("Check there is no unread indicator and pending CR")
        self.device_1.chats_tab.click()
        try:
            self.home_1.pending_contact_request_button.wait_for_invisibility_of_element(30)
            self.home_1.notifications_unread_badge.wait_for_invisibility_of_element(30)
        except TimeoutException:
            self.errors.append("Cancelled request is not disappeared!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702850)
    def test_activity_center_contact_request_decline(self):
        [home.chats_tab.click() for home in [self.home_1, self.home_2]]

        self.home_2.just_fyi("Device2 sends pending contact request after cancelling")
        self.home_2.add_contact(self.public_key_1)

        self.device_1.just_fyi('Device1 verifies pending contact request')
        self.home_1.contacts_tab.click()
        for indicator in (self.home_1.notifications_unread_badge, self.home_1.contact_new_badge):
            if not indicator.is_element_displayed():
                self.errors.append("Unread indicator on contacts tab or on activity center is not shown for incoming CR!")
        if self.home_1.pending_contact_request_text.text != '1':
            self.errors.append("The amount of contact requests is not shown for incoming CR!")

        self.device_1.just_fyi('Device1 declines pending contact request')
        self.home_1.handle_contact_request(username=self.default_username_2, action='decline')
        for indicator in (self.home_1.notifications_unread_badge, self.home_1.contact_new_badge, self.home_1.pending_contact_request_text):
            if indicator.is_element_displayed():
                self.errors.append("Unread indicator on contacts tab or on activity center is shown after declining contact request!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702851)
    def test_activity_center_contact_request_accept(self):
        self.device_2.just_fyi('Device2 re-sends a contact request to Device1')
        self.home_2.add_contact(self.public_key_1, remove_from_contacts=True)

        self.device_1.just_fyi('Device1 accepts pending contact request and check contact list')
        self.home_1.chats_tab.click()
        self.home_1.handle_contact_request(username=self.default_username_2)
        self.home_1.contacts_tab.click()
        if not self.home_1.contact_details(username=self.default_username_2).is_element_displayed(20):
            self.errors.append("Contact was not added to contact list after accepting contact request (as receiver)")

        self.device_2.just_fyi('Device1 check that contact appeared in contact list mutually')
        self.home_2.chats_tab.click()
        self.home_2.contacts_tab.click()
        if not self.home_2.contact_details(username=self.default_username_1).is_element_displayed(20):
            self.errors.append("Contact was not added to contact list after accepting contact request (as sender)")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_one_3")
@marks.new_ui_critical
class TestActivityMultipleDevicePR(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.loop.run_until_complete(
            run_in_parallel(((self.device_1.create_user,), (self.device_2.create_user,))))
        self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.profile_1, self.profile_2 = self.home_1.get_profile_view(), self.home_2.get_profile_view()
        users = self.loop.run_until_complete(run_in_parallel(
            ((self.home_1.get_public_key_and_username, True),
             (self.home_2.get_public_key_and_username, True))
        ))
        self.public_key_1, self.default_username_1 = users[0]
        self.public_key_2, self.default_username_2 = users[1]
        [home.click_system_back_button_until_element_is_shown() for home in (self.home_1, self.home_2)]
        [home.chats_tab.click() for home in (self.home_1, self.home_2)]

        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.default_username_1)
        self.text_message = 'hello'
        self.one_to_one_message = 'one-t-one message'

        self.home_2.just_fyi("Send message to contact (need for jump to) test")
        self.chat_1 = self.home_1.get_chat(self.default_username_2).click()
        self.chat_1.send_message(self.one_to_one_message)
        self.chat_2 = self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.send_message(self.text_message)
        [home.click_system_back_button_until_element_is_shown() for home in (self.home_1, self.home_2)]

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = self.home_1.get_random_chat_name()
        self.channel_name = self.home_1.get_random_chat_name()
        self.home_1.create_community(name=self.community_name, description='community to test', require_approval=False)
        self.community_1 = CommunityView(self.drivers[0])
        self.community_1.send_invite_to_community(self.default_username_2)
        self.channel_1 = self.community_1.add_channel(self.channel_name)
        self.chat_2 = self.home_2.get_chat(self.default_username_1).click()
        self.chat_2.element_by_text_part('View').click()
        self.community_2 = CommunityView(self.drivers[1])
        self.community_2.join_button.click()
        self.channel_1.send_message(self.text_message)

        self.home_1.just_fyi("Reopen community view to use new interface")
        for home in (self.home_1, self.home_2):
            home.jump_to_communities_home()
            home.get_chat(self.community_name, community=True).click()
            community_view = home.get_community_view()
            community_view.get_channel(self.channel_name).click()
        self.channel_2 = self.home_2.get_chat_view()

    @marks.testrail_id(702936)
    def test_navigation_jump_to(self):
        self.community_1.just_fyi("Check Jump to screen and redirect on tap")
        self.community_1.jump_to_button.click()
        for card in (self.community_name, self.default_username_2):
            if not self.community_1.element_by_text_part(card).is_element_displayed(20):
                self.errors.append("Card %s is not shown on Jump to screen!" % card)
        self.community_1.element_by_translation_id("community-channel").click()
        if not self.channel_1.chat_element_by_text(self.text_message).is_element_displayed(20):
            self.errors.append("User was not redirected to community channel after tapping on community channel card!")
        self.channel_1.click_system_back_button()
        self.community_1.jump_to_button.click()
        self.community_1.element_by_text_part(self.default_username_2).click()
        if not self.chat_1.chat_element_by_text(self.one_to_one_message).is_element_displayed(20):
            self.errors.append("User was not redirected to 1-1 chat after tapping card!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702947)
    def test_activity_center_reply_read_unread_delete_filter(self):
        message_to_reply, reply_to_message_from_sender = 'something to reply to', 'this is a reply'
        self.home_1.jump_to_communities_home()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.community_1.get_channel(self.channel_name).click()
        self.channel_1.send_message(message_to_reply)

        self.home_1.jump_to_communities_home()
        self.channel_2.chat_element_by_text(message_to_reply).wait_for_visibility_of_element(60)
        self.channel_2.quote_message(message_to_reply)
        self.channel_2.send_message(reply_to_message_from_sender)

        self.home_1.just_fyi("Checking unread indicators")
        self.home_1.notifications_unread_badge.wait_for_visibility_of_element(120)
        community_element_1 = self.home_1.get_chat(self.community_name, community=True)
        for unread_counter in community_element_1.new_messages_counter, self.home_1.communities_tab.counter:
            if not unread_counter.is_element_displayed(60):
                self.errors.append('New message counter badge is not shown!')
            if int(unread_counter.text) != 1:
                self.errors.append('New message counter badge is not 1, it is %s!' % unread_counter.text)

        self.home_1.just_fyi("Checking reply attributes in activity center")
        self.home_1.open_activity_center_button.click()
        reply_element = self.home_1.get_element_from_activity_center_view(reply_to_message_from_sender)
        if reply_element.title.text != 'Reply':
            self.errors.append("Expected title is not shown, '%s' is instead!" % reply_element.title)
        if not reply_element.unread_indicator.is_element_displayed():
            self.errors.append("No unread dot is shown on activity center element!")

        self.home_1.just_fyi("Swiping to 'Replies' on activity center and check unread there")
        self.home_1.mention_activity_tab_button.click()
        if reply_element.is_element_displayed(2):
            self.errors.append("Filter on mentions is not working in Activity centre!")
        self.home_1.reply_activity_tab_button.click()
        if not self.home_1.reply_activity_tab_button.counter.is_element_displayed(2):
            self.errors.append("No unread dot is shown on activity center tab element!")
        if not reply_element.is_element_displayed():
            self.errors.append("Filter on replies tab is not working in Activity centre!")

        self.home_1.just_fyi("Mark it as read and check filter")
        reply_element.swipe_right_on_element()
        self.home_1.activity_notification_swipe_button.click()
        if reply_element.is_element_displayed(2):
            self.errors.append("Message is not marked as read!")
        self.home_1.activity_unread_filter_button.click()
        if not reply_element.is_element_displayed(2):
            self.errors.append("Read filter is not displayed read message!")

        self.home_1.just_fyi("Mark it as unread and check filter via right swipe")
        reply_element.swipe_right_on_element()
        self.home_1.activity_notification_swipe_button.click()
        if not reply_element.unread_indicator.is_element_displayed():
            self.errors.append("No unread dot is shown on activity center element after marking it as unread!")

        self.home_1.just_fyi("Tap on it and check it marked as read")
        reply_element.click()
        if not self.channel_1.chat_element_by_text(reply_to_message_from_sender).is_element_displayed():
            self.errors.append("Was not redirected to chat after tapping on reply!")
        self.home_1.jump_to_communities_home()
        if self.home_1.notifications_unread_badge.is_element_displayed():
            self.errors.append("Notification was not marked as read after opening it in community channel!")

        self.home_1.just_fyi("Delete it from unread via left swipe")
        self.home_1.open_activity_center_button.click()
        reply_element.swipe_left_on_element()
        self.home_1.activity_notification_swipe_button.click()
        if reply_element.is_element_displayed():
            self.errors.append("Reply is still shown after removing from activity centre!")

        self.errors.verify_no_errors()

