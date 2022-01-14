from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.chat_view import CommunityView


class TestCommunitiesMultipleDevices(MultipleDeviceTestCase):

    @marks.testrail_id(695842)
    @marks.medium
    @marks.flaky
    def test_creating_community_accept_membership(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        community_name, pub_chat_name, channel_name = "some name", home_1.get_random_chat_name(), "first_channel"
        community_description, community_pic = "something in community", 'sauce_logo.png'
        message, message_member = "message", "from member"
        userkey_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        home_2.home_button.click()
        community_1 = home_1.create_community(community_name, community_description, set_image=True,
                                              file_name=community_pic)
        channel_1 = community_1.add_channel(channel_name)
        channel_1.send_message(message)
        home_1.home_button.double_click()

        home_1.just_fyi("Sending community link to public chat")
        community_1 = home_1.get_chat(community_name, community=True).click()
        community_link_text = community_1.copy_community_link()
        pub_1 = home_1.join_public_chat(pub_chat_name)
        pub_1.chat_message_input.paste_text_from_clipboard()
        pub_1.send_message_button.click()
        pub_1.get_back_to_home_view()

        home_2.just_fyi("Tapping on community link and request membership")
        pub_2 = home_2.join_public_chat(pub_chat_name)
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

        home_1.just_fyi("Checking pending membership")
        community_1 = home_1.get_chat(community_name, community=True).click()
        community_1.community_options_button.click()
        community_1.community_info_button.click()
        community_1.community_membership_request_value.wait_for_element(60)
        if community_1.community_membership_request_value.text != '1':
            self.drivers[0].fail(
                "Membership request value '%s' is not equal expected" % community_1.community_membership_request_value)

        home_1.just_fyi("Approve membership")
        community_1.handle_membership_request(username_2, approve=True)
        if not community_1.element_by_text(username_2).is_element_displayed():
            self.errors.append("New member %s is not shown as added to community on info page!" % username_2)
        if not community_2.community_info_picture.is_element_image_similar_to_template(community_pic):
            self.errors.append("Community image is different!")
        channel_2 = community_2.get_chat(channel_name).click()
        if not channel_2.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append("Message was not received in community channel!")
        channel_2.send_message(message_member)
        community_1.home_button.double_click()
        home_1.get_chat(community_name, community=True).click()
        chat_element_1 = community_1.get_chat(channel_name)
        if not chat_element_1.new_messages_public_chat.is_element_displayed():
            self.errors.append("Unread messages counter is not shown for community channel!")
        if not community_1.element_by_text(message_member).is_element_displayed():
            self.errors.append("Message from member is not shown for community channel!")

        self.errors.verify_no_errors()

    @marks.testrail_id(695845)
    @marks.medium
    def test_notification_in_activity_center_for_mention_in_community_and_group_chat(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        community_name, gr_chat_name, channel_name = "some name", home_1.get_random_chat_name(), "first_channel"
        community_description = "something in community"
        message, message_member = "message", "from member"
        userkey_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        userkey_1, username_1 = home_1.get_public_key_and_username(return_username=True)
        home_1.home_button.click()
        one_to_one_1 = home_1.add_contact(userkey_2)
        one_to_one_1.home_button.click()
        home_2.home_button.click()
        home_1.just_fyi("Create community on Device_1")

        community_1 = home_1.create_community(community_name, community_description, set_image=True)
        channel_1 = community_1.add_channel(channel_name)
        channel_1.send_message(message)
        home_1.home_button.double_click()

        home_1.just_fyi("Joining Group chat, receiving community link in there")
        one_to_one_2 = home_2.add_contact(userkey_1)
        one_to_one_2.home_button.click()
        community_1 = home_1.get_chat(community_name, community=True).click()
        community_link_text = community_1.copy_community_link()
        pub_1 = home_1.create_group_chat(user_names_to_add=[username_2], group_chat_name=gr_chat_name)

        pub_2 = home_2.get_chat(gr_chat_name).click()
        pub_2.join_chat_button.click()
        pub_1.chat_message_input.paste_text_from_clipboard()
        pub_1.send_message_button.click()
        pub_1.get_back_to_home_view()

        home_2.just_fyi("Tapping on community link and request membership")
        pub_2.element_by_text(community_name).wait_for_element(60)
        community_message_2 = pub_2.get_community_link_preview_by_text(community_link_text)
        community_2 = community_message_2.view()
        community_2.request_access_button.click()
        if not community_2.membership_request_pending_text.is_element_displayed():
            self.errors.append("Membership request is not pending")

        home_1.just_fyi("Checking pending membership")
        community_1 = home_1.get_chat(community_name, community=True).click()
        community_1.community_options_button.click()
        community_1.community_info_button.click()
        community_1.community_membership_request_value.wait_for_element(60)

        home_1.just_fyi("Approve membership")
        community_1.handle_membership_request(username_2, approve=True)
        channel_2 = community_2.get_chat(channel_name).click()
        channel_2.select_mention_from_suggestion_list(username_1, username_1[:2])
        channel_2.send_as_keyevent("community")
        channel_mesage = username_1 + " community"
        channel_2.send_message_button.click()
        community_1.home_button.double_click()
        channel_2.home_button.click()
        home_2.get_chat_from_home_view(gr_chat_name).click()
        pub_2.select_mention_from_suggestion_list(username_1, username_1[:2])
        pub_2.send_as_keyevent("group")
        group_chat_message = username_1 + " group"
        pub_2.send_message_button.click()

        if not home_1.notifications_unread_badge.is_element_displayed():
            self.errors.append("Unread badge is NOT shown after receiving mentions from Group and Community")
        home_1.notifications_unread_badge.wait_and_click(30)

        home_1.just_fyi("Check there are two notifications from two chats are present in Activity Center")
        if home_1.get_chat_from_activity_center_view(username_2).chat_message_preview == group_chat_message:
            home_1.just_fyi("Open group chat where user mentioned and return to Activity Center")
            home_1.get_chat_from_activity_center_view(username_2).click()
            home_1.home_button.click()
            home_1.notifications_button.click()
        else:
            self.errors.append("No mention in Activity Center for Group Chat")

        if home_1.get_chat_from_activity_center_view(username_2).chat_message_preview == channel_mesage:
            home_1.just_fyi("Open community chat where user mentioned and return to Activity Center")
            home_1.get_chat_from_activity_center_view(username_2).click()
            home_1.home_button.click()
        else:
            self.errors.append("No mention in Activity Center for community chat")

        home_1.just_fyi("Check there are no unread messages counters on chats after message read")
        if (home_1.notifications_unread_badge.is_element_present() or
                home_1.get_chat_from_home_view(gr_chat_name).new_messages_counter.text == "1" or
                home_1.get_chat_from_home_view(community_name).new_messages_counter.text == "1"):
            self.errors.append("Unread message indicator is kept after all messages read in chats")

        home_1.just_fyi("Check there is an empty view on Activity Center")
        home_1.notifications_button.click()
        if not home_1.element_by_translation_id('empty-activity-center').is_element_present():
            self.errors.append("It appears Activity Center still has some chats after user opened all of them")

        self.errors.verify_no_errors()
