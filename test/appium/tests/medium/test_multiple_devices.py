import random
import emoji
import time
from tests import background_service_message
from views.chat_view import CommunityView


from tests import bootnode_address, mailserver_address, mailserver_ams,  mailserver_hk, used_fleet, common_password
from tests.users import transaction_senders, basic_user, ens_user
from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.chat_view import ChatView


@marks.medium
class TestChatManagementMultipleDevice(MultipleDeviceTestCase):
    @marks.testrail_id(5763)
    # TODO: check main e2e about block; if the difference is only in place where user is blocked, split it and remove
    # duplicates
    def test_contact_block_user_from_one_to_one_header_check_push_notification_service(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_before_block_1 = "Before block from %s" % device_1.driver.number
        message_before_block_2 = "Before block from %s" % device_2.driver.number
        message_after_block_2 = "After block from %s" % device_2.driver.number
        home_1, home_2 = device_1.create_user(enable_notifications=True), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        device_2_public_key = home_2.get_public_key_and_username()
        home_2.home_button.click()
        default_username_1 = profile_1.default_username_text.text
        profile_1.home_button.click()

        device_1.just_fyi('both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        for chat in chat_public_1, chat_public_2:
            chat.chat_message_input.send_keys("Before block from %s" % chat.driver.number)
            chat.send_message_button.click()

        chat_public_1.get_back_to_home_view()
        chat_public_2.get_back_to_home_view()

        device_1.just_fyi('both devices joining 1-1 chat and exchanging several messages')
        chat_1 = home_1.add_contact(device_2_public_key)
        for _ in range(2):
            chat_1.chat_message_input.send_keys(message_before_block_1)
            chat_1.send_message_button.click()

        chat_2 = home_2.get_chat(default_username_1).click()
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_before_block_2)
            chat_2.send_message_button.click()

        device_1.just_fyi('block user')
        chat_1.chat_options.click()
        chat_1.view_profile_button.click()
        chat_1.block_contact()
        chat_1.get_back_to_home_view()
        chat_1.home_button.click()

        device_1.just_fyi('no 1-1, messages from blocked user are hidden in public chat')
        from views.home_view import ChatElement
        blocked_chat_user = ChatElement(self.drivers[0], basic_user['username'])

        if blocked_chat_user.is_element_displayed():
            home_1.driver.fail("Chat with blocked user '%s' is not deleted" % device_2.driver.number)
        public_chat_after_block_1 = home_1.join_public_chat(chat_name)
        if public_chat_after_block_1.chat_element_by_text(message_before_block_2).is_element_displayed():
            self.errors.append(
                "Messages from blocked user '%s' are not cleared in public chat '%s'" % (device_2.driver.number,
                                                                                         chat_name))
        device_1.click_system_home_button()

        device_2.just_fyi('send messages to 1-1 and public chat')
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_after_block_2)
            chat_2.send_message_button.click()
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(chat_name)
        chat_public_2 = home_2.get_chat_view()
        [chat_public_2.send_message(message_after_block_2) for _ in range(2)]

        device_1.just_fyi("check that new messages and push notifications don't arrive from blocked user")
        device_1.open_notification_bar()
        if device_1.element_by_text_part(message_after_block_2).is_element_displayed():
            self.errors.append("Push notification is received from blocked user")
        device_1.element_by_text_part(background_service_message).click()

        if public_chat_after_block_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received" % device_2.driver.number)
        if home_1.notifications_unread_badge.is_element_displayed():
            device_1.driver.fail("Unread badge is shown after receiving new message from blocked user")
        if blocked_chat_user.is_element_displayed():
            device_2.driver.fail("Chat with blocked user is reappeared after receiving new messages in home view")
        device_1.open_notification_bar()
        home_1.stop_status_service_button.click()

        device_2.just_fyi("send messages when device 1 is offline")
        for _ in range(2):
            chat_public_2.chat_message_input.send_keys(message_after_block_2)
            chat_public_2.send_message_button.click()
        chat_public_2.get_back_to_home_view()
        home_2.get_chat(default_username_1).click()
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_after_block_2)
            chat_2.send_message_button.click()

        device_1.just_fyi("reopen app and check that messages from blocked user are not fetched")
        device_1.click_system_home_button()
        self.drivers[0].launch_app()
        device_1.sign_in()
        public_chat_after_block_1.home_button.double_click()
        if home_1.notifications_unread_badge.is_element_displayed():
            device_1.driver.fail("Unread badge is shown after after fetching new messages from offline")
        if blocked_chat_user.is_element_displayed():
            self.errors.append("Chat with blocked user is reappeared after fetching new messages from offline")
        home_1.join_public_chat(chat_name)
        home_1.get_chat_view()
        if chat_public_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received after fetching new messages from offline"
                               % device_2.driver.number)

        device_1.just_fyi("check that PNs are still enabled in profile after closing 'background notification centre' "
                          "message and relogin")
        device_1.open_notification_bar()
        if not device_1.element_by_text_part(background_service_message).is_element_displayed():
            self.errors.append("Background notification service is not started after relogin")

        self.errors.verify_no_errors()

    @marks.testrail_id(6233)
    # TODO: part about replying to emoji may be duplicated; reply to text should be in critical e2e -
    #  if in fact contains 1-2 mins, may be added to critical group
    def test_chat_reply_to_text_emoji_in_1_1_public(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        message_from_receiver = "Message receiver"
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('Both devices join to 1-1 chat')
        device_2_public_key = home_2.get_public_key_and_username()
        device_1_profile = home_1.profile_button.click()
        device_1_username = device_1_profile.default_username_text.text
        home_1.home_button.click()

        device_1.just_fyi("Sender adds receiver and quotes own message")
        device_1_chat = home_1.add_contact(device_2_public_key)
        device_1_chat.send_message(message_from_sender)
        device_1_chat.quote_message(message_from_sender)
        if device_1_chat.quote_username_in_message_input.text != "↪ You":
            self.errors.append("'You' is not displayed in reply quote snippet replying to own message")

        device_1_chat.just_fyi("Clear quote and check there is not snippet anymore")
        device_1_chat.cancel_reply_button.click()
        if device_1_chat.cancel_reply_button.is_element_displayed():
            self.errors.append("Message quote kept in public chat input after it's cancelation")

        device_1_chat.just_fyi("Send reply")
        device_1_chat.quote_message(message_from_sender)
        reply_to_message_from_sender = message_from_sender + " reply"
        device_1_chat.send_message(reply_to_message_from_sender)

        device_1.just_fyi("Receiver verifies received reply...")
        home_2.home_button.click()
        device_2_chat_item = home_2.get_chat(device_1_username)
        device_2_chat_item.wait_for_visibility_of_element(20)
        device_2_chat = device_2_chat_item.click()
        if device_2_chat.chat_element_by_text(reply_to_message_from_sender).replied_message_text != message_from_sender:
            self.errors.append("No reply received in 1-1 chat")

        device_1_chat.back_button.click()
        device_2_chat.back_button.click()

        device_1.just_fyi('both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        chat_public_1.send_message(message_from_sender)
        chat_public_2.quote_message(message_from_sender)
        if chat_public_2.quote_username_in_message_input.text != ("↪ Replying to " + device_1_username):
            self.errors.append(
                " %s is not displayed in reply quote snippet replying to own message " % device_1_username)

        device_1.just_fyi('Message receiver verifies reply is present in received message')
        chat_public_2.send_message(message_from_receiver)
        public_replied_message = chat_public_1.chat_element_by_text(message_from_receiver)
        if public_replied_message.replied_message_text != message_from_sender:
            self.errors.append("Reply is not present in message received in public chat")

        device_1.just_fyi('Can reply to link')
        link_message, reply = 'Test with link: https://status.im/ here should be nothing unusual.' \
                              ' Just a regular reply.', 'reply to link'
        chat_public_1.send_message(link_message)
        chat_public_2.quote_message(link_message[:10])
        chat_public_2.send_message(reply)
        public_replied_message = chat_public_1.chat_element_by_text(reply)
        if public_replied_message.replied_message_text != link_message:
            self.errors.append("Reply for '%s' not present in message received in public chat" % link_message)

        device_1.just_fyi('Can reply to emoji message')
        reply = 'reply to emoji'
        emoji_message = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_message]
        chat_public_1.send_message(emoji.emojize(emoji_message))
        chat_public_2.quote_message(emoji.emojize(emoji_message))
        chat_public_2.send_message(reply)
        public_replied_message = chat_public_1.chat_element_by_text(reply)
        if public_replied_message.replied_message_text != emoji_unicode:
            self.errors.append("Reply for '%s' emoji not present in message received in public chat" % emoji_unicode)

        self.errors.verify_no_errors()

    @marks.testrail_id(6326)
    # TODO: check if no duplicate checks 702155
    def test_chat_mention_users_if_not_in_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sender = ens_user
        home_1, home_2 = device_1.create_user(), device_2.recover_access(passphrase=sender['passphrase'])
        profile_2 = home_2.profile_button.click()
        profile_2.connect_existing_ens(sender['ens'])
        profile_2.home_button.double_click()

        device_1.just_fyi('Both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        [chat_1, chat_2] = [home.join_public_chat(chat_name) for home in (home_1, home_2)]
        message = 'From ' + sender['ens'] + ' message'
        chat_2.send_message(message)
        username_value = '@' + sender['ens']

        self.drivers[1].close_app()
        self.drivers[1].launch_app()
        device_2.back_button.click()
        device_2.your_keys_more_icon.click()
        device_2.generate_new_key_button.click()
        device_2.create_user(second_user=True)
        home_2.join_public_chat(chat_name)
        newusermessage = 'Newusermessage2'
        chat_2.send_message(newusermessage)
        random_username = chat_1.chat_element_by_text(newusermessage).username.text
        chat_1.wait_ens_name_resolved_in_chat(message=message, username_value=username_value)

        device_1.just_fyi('Set nickname for ENS user')
        chat_1.view_profile_long_press(message)
        nickname = 'nicknamefortestuser'
        chat_1.set_nickname(nickname)
        ens_nickname_value = nickname + " @" + sender['ens']
        chat_1.wait_ens_name_resolved_in_chat(message=message, username_value=ens_nickname_value)

        device_1.just_fyi('Check there is ENS+Nickname user in separate 1-1 chat')
        chat_1.get_back_to_home_view()
        home_1.add_contact(public_key=basic_user['public_key'])
        chat_1.chat_message_input.send_keys('@')
        if (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(
                    sender['username']).is_element_displayed()):
            self.errors.append('ENS-owner user who is not in 1-1 chat is  available in mention suggestion list')

        device_1.just_fyi('Check there is no random user in different public chat')
        chat_1.get_back_to_home_view()
        chat_1 = home_1.join_public_chat(chat_name + "2")
        chat_1.chat_message_input.send_keys('@')
        if chat_1.search_user_in_mention_suggestion_list(random_username).is_element_displayed():
            self.errors.append('Random user from public chat is in mention suggestion list another public chat')

        device_1.just_fyi('Check there is ENS+Nickname user in Group chat and no random user')
        chat_1.get_back_to_home_view()
        home_1.add_contact(sender['public_key'])
        chat_1.get_back_to_home_view()
        home_1.create_group_chat(user_names_to_add=[nickname])
        chat_1.chat_message_input.send_keys('@')
        if chat_1.search_user_in_mention_suggestion_list(random_username).is_element_displayed():
            self.errors.append('Random user from public chat is in mention suggestion list of Group chat')
        if not (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(sender['username']).is_element_displayed()):
            self.errors.append('ENS-owner user is not available in mention suggestion list of Group chat')

        device_1.just_fyi('Check there is no blocked user in mentions Group/Public chat ')
        home_1.home_button.click()
        public_1 = home_1.join_public_chat(chat_name)
        public_1.chat_element_by_text(message).member_photo.click()
        public_1.block_contact()
        public_1.chat_message_input.send_keys('@')
        if (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(sender['username']).is_element_displayed()):
            self.errors.append('Blocked user is available in mention suggestion list')

        self.errors.verify_no_errors()

    @marks.testrail_id(5362)
    # TODO: can be moved to TestOneToOneChatMultipleSharedDevices - should be quick e2e
    def test_1_1_chat_unread_counter_preview_highlited(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_2 = home_2.profile_button.click()
        default_username_2 = profile_2.default_username_text.text
        home_2 = profile_2.home_button.click()
        public_key_1 = home_1.get_public_key_and_username()
        home_1.home_button.click()
        chat_2 = home_2.add_contact(public_key_1)

        message, message_2, message_3 = 'test message', 'test message2', 'test'
        chat_2.send_message(message)
        chat_element = home_1.get_chat(default_username_2)
        home_1.dapp_tab_button.click()
        chat_2.send_message(message_2)

        if home_1.home_button.counter.text != '1':
            self.errors.append('New messages counter is not shown on Home button')
        device_1.home_button.click()
        if chat_element.new_messages_counter.text != '1':
            self.errors.append('New messages counter is not shown on chat element')
        chat_1 = chat_element.click()
        chat_1.add_to_contacts.click()

        home_1.home_button.double_click()

        if home_1.home_button.counter.is_element_displayed():
            self.errors.append('New messages counter is shown on Home button for already seen message')
        if chat_element.new_messages_counter.text == '1':
            self.errors.append('New messages counter is shown on chat element for already seen message')
        home_1.delete_chat_long_press(default_username_2)

        home_1.just_fyi("Checking preview of message and chat highlighting")
        chat_2.send_message(message_3)
        chat_1_element = home_1.get_chat(default_username_2)
        if chat_1_element.chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is not hightligted or text is not shown! ")
        home_1.get_chat(default_username_2).click()
        home_1.home_button.double_click()
        if not home_1.get_chat(default_username_2).chat_preview.is_element_differs_from_template('highligted_preview.png', 0):
            self.errors.append("Preview message is still highlighted after opening ")
        self.errors.verify_no_errors()

    @marks.testrail_id(700727)
    # TODO: merge with any medium group that contains 3 types of chats
    def test_chat_gap_in_public_and_no_gap_in_1_1_and_group(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        message_1 = "testing gap"
        message_2 = "testing no gap"
        pub_chat_name = home_1.get_random_chat_name()
        group_chat_name = home_1.get_random_chat_name()
        public_key_1, username_1 = home_1.get_public_key_and_username(True)
        public_key_2, username_2 = home_2.get_public_key_and_username(True)
        profile_1 = home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.sync_history_for_button.click()
        profile_1.element_by_translation_id("two-minutes").click()
        [home.home_button.click() for home in (home_1, home_2)]

        home_1.just_fyi("Creating 1-1 chat and sending message from device 1")
        one_to_one_chat_1 = home_1.add_contact(public_key_2)
        one_to_one_chat_1.send_message("HI")
        home_1.get_back_to_home_view()

        home_1.just_fyi("Creating group chat and sending message from device 1")
        group_chat_1 = home_1.create_group_chat([username_2], group_chat_name)
        group_chat_1.send_message("HI")
        home_1.get_back_to_home_view()

        home_1.just_fyi("Creating public chat and sending message from device 1")
        pub_chat_1, pub_chat_2 = home_1.join_public_chat(pub_chat_name), home_2.join_public_chat(pub_chat_name)
        pub_chat_1.send_message("HI")
        device_1.toggle_airplane_mode()

        home_2.just_fyi("Joining public chat by device 2 and sending message")
        pub_chat_2.send_message(message_1)
        home_2.get_back_to_home_view()

        home_2.just_fyi("Joining 1-1 chat by device 2 and sending message")
        one_to_one_chat_2 = home_2.add_contact(public_key_1)
        one_to_one_chat_2.send_message(message_2)
        home_2.get_back_to_home_view()

        home_2.just_fyi("Joining Group chat by device 2 and sending message")
        group_chat_2 = home_2.get_chat(group_chat_name).click()
        group_chat_2.join_chat_button.click()
        group_chat_2.send_message(message_2)

        # Waiting for 3 minutes and then going back online
        time.sleep(180)
        device_1.toggle_airplane_mode()

        home_1.just_fyi("Checking gap in public chat and fetching messages")
        if pub_chat_1.chat_element_by_text(message_1).is_element_displayed(10):
            self.errors.append("Test message has been fetched automatically")
        pub_chat_1.element_by_translation_id("fetch-messages").wait_and_click(60)
        if not pub_chat_1.chat_element_by_text(message_1).is_element_displayed(10):
            self.errors.append("Test message has not been fetched")
        home_1.get_back_to_home_view()

        home_1.just_fyi("Checking that there is no gap in 1-1/group chat and messages fetched automatically")
        for chat in [home_1.get_chat(username_2), home_1.get_chat(group_chat_name)]:
            chat_view = chat.click()
            if chat_view.element_by_translation_id("fetch-messages").is_element_displayed(10):
                self.errors.append("Fetch messages button is displayed in {}} chat".format(chat.user_name_text.text))
            if not chat_view.chat_element_by_text(message_2).is_element_displayed(10):
                self.errors.append("Message in {} chat has not been fetched automatically".format(chat.user_name_text.text))
            chat_view.back_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(695842)
    @marks.flaky
    def test_community_creating_accept_membership(self):
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

    @marks.testrail_id(695782)
    # TODO: combine with other tests for activity centre
    def test_activity_center_can_accept_or_reject_multiple_chats_from(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        group_chat_name_1 = "GroupChat1"
        group_chat_name_2 = "GroupChat2"
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('Device1 adds Devices and creates 1-1 and Group chat with it')
        public_key_user_1, username_1 = home_1.get_public_key_and_username(return_username=True)
        public_key_user_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        home_1.home_button.click()
        device_1_one_to_one_chat = home_1.add_contact(public_key_user_2)
        device_1_one_to_one_chat.send_message(message_from_sender)
        device_1_one_to_one_chat.home_button.click()

        home_1.create_group_chat([username_2], group_chat_name=group_chat_name_1)
        home_1.home_button.click()
        home_2.home_button.click()

        device_1.just_fyi('Device2 rejects both chats and verifies they disappeared and not in Chats too')
        home_2.notifications_button.click()
        home_2.notifications_select_button.click()
        home_2.element_by_text_part(username_1[:10]).click()
        home_2.element_by_text_part(group_chat_name_1).click()
        home_2.notifications_reject_and_delete_button.click()

        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is on Activity Center view after action made on it")
        if home_2.element_by_text_part(group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat is on Activity Center view after action made on it")
        home_2.home_button.click()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is added on home after rejection")
        if home_2.element_by_text_part(group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat is added on home after rejection")

        home_2.just_fyi("Verify there are still no chats after relogin")
        home_2.relogin()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat appears on Chats view after relogin")
        if home_2.element_by_text_part(group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat appears on Chats view after relogin")
        home_2.notifications_button.click()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request reappears back in Activity Center view after relogin")
        if home_2.element_by_text_part(group_chat_name_1).is_element_displayed(2):
            self.errors.append("Group chat request reappears back in Activity Center view after relogin")
        home_2.home_button.click()

        device_1.just_fyi('Device1 creates 1-1 and Group chat again')
        home_1.get_chat_from_home_view(username_2).click()
        device_1_one_to_one_chat.send_message('Some text here')
        device_1_one_to_one_chat.home_button.click()
        home_1.create_group_chat([username_2], group_chat_name=group_chat_name_2)

        device_1.just_fyi('Device2 accepts both chats (via Select All button) and verifies they disappeared '
                          'from activity center view but present on Chats view')
        home_2.notifications_button.click()
        home_2.notifications_select_button.click()
        home_2.notifications_select_all.click()
        home_2.notifications_accept_and_add_button.click()

        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request stays on Activity Center view after it was accepted")
        if home_2.element_by_text_part(group_chat_name_2).is_element_displayed(2):
            self.errors.append("Group chat request stays on Activity Center view after it was accepted")
        home_2.home_button.click()

        if not home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is not added on home after accepted from Activity Center")
        if not home_2.element_by_text_part(group_chat_name_2).is_element_displayed(2):
            self.errors.append("Group chat is not added on home after accepted from Activity Center")

        self.errors.verify_no_errors()

    @marks.testrail_id(695845)
    def test_activity_center_notification_in_for_mention_in_community_and_group_chat(self):
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

    @marks.testrail_id(695771)
    # TODO: combine with other tests for activity centre
    def test_activity_center_profile_accept_new_group_from_trusted_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        group_chat_name_1 = "GroupChat1"
        group_chat_name_2 = "GroupChat2"
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('Device1 sets permissions to accept chat requests only from trusted contacts')
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.accept_new_chats_from.click()
        profile_1.accept_new_chats_from_contacts_only.click()
        profile_1.profile_button.click()
        public_key_user_1, username_1 = profile_1.get_public_key_and_username(return_username=True)
        public_key_user_2, username_2 = home_2.get_public_key_and_username(return_username=True)

        device_1.just_fyi('Device2 creates 1-1 chat Group chats')
        home_2.home_button.click()
        one_to_one_device_2 = home_2.add_contact(public_key_user_1)
        one_to_one_device_2.send_message(message_from_sender)
        one_to_one_device_2.home_button.click()
        home_2.create_group_chat([username_1], group_chat_name=group_chat_name_1)

        device_1.just_fyi('Device1 check there are no any chats in Activity Center nor Chats view')

        home_1.home_button.click()
        if home_1.element_by_text_part(username_2).is_element_displayed() or home_1.element_by_text_part(
                group_chat_name_1).is_element_displayed():
            self.errors.append("Chats are present on Chats view despite they created by non-contact")
        home_1.notifications_button.click()
        if home_1.element_by_text_part(username_2).is_element_displayed() or home_1.element_by_text_part(
                group_chat_name_1).is_element_displayed():
            self.errors.append("Chats are present in Activity Center view despite they created by non-contact")

        device_1.just_fyi('Device1 adds Device2 in Contacts so chat requests should be visible now')
        home_1.home_button.click()
        home_1.add_contact(public_key_user_2)

        device_1.just_fyi('Device2 creates 1-1 chat Group chats once again')
        home_2.home_button.click()
        home_2.get_chat_from_home_view(username_1).click()
        one_to_one_device_2.send_message(message_from_sender)
        one_to_one_device_2.home_button.click()
        home_2.create_group_chat([username_1], group_chat_name=group_chat_name_2)

        device_1.just_fyi('Device1 verifies 1-1 chat Group chats are visible')

        home_1.home_button.click()
        if not home_1.element_by_text_part(username_2).is_element_displayed() or not home_1.element_by_text_part(
                group_chat_name_2).is_element_displayed():
            self.errors.append("Chats are not present on Chats view while they have to!")

        self.errors.verify_no_errors()

    @marks.testrail_id(6294)
    # TODO: may be merged with 6295 to group and add more tx tests
    def test_keycard_1_1_chat_command_request_and_send_tx_stt_in_1_1_chat_offline_opened_from_push(self):
        sender = transaction_senders['ETH_STT_1']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])

        device_1.just_fyi('Grab user data for transactions and public chat, set up wallets')
        home_1 = device_1.create_user(keycard=True)
        recipient_public_key, recipient_username = home_1.get_public_key_and_username(return_username=True)
        amount = device_1.get_unique_amount()
        asset_name = 'STT'
        wallet_1 = home_1.wallet_button.click()
        wallet_1.select_asset(asset_name)
        wallet_1.home_button.click()

        home_2 = device_2.recover_access(passphrase=sender['passphrase'], keycard=True, enable_notifications=True)
        wallet_2 = home_2.wallet_button.click()
        initial_amount_stt = wallet_2.get_asset_amount_by_name('STT')
        wallet_2.home_button.click()

        device_2.just_fyi('Add recipient to contact and send 1 message')
        chat_2 = home_2.add_contact(recipient_public_key)
        chat_2.send_message("Hey there!")

        profile_2 = wallet_2.profile_button.click()
        profile_2.airplane_mode_button.click()
        device_2.home_button.double_click()
        chat_element = home_1.get_chat(sender['username'])
        chat_element.wait_for_visibility_of_element(30)
        chat_1 = chat_element.click()

        home_1.just_fyi('Request %s STT in 1-1 chat and check it is visible for sender and receiver' % amount)
        chat_1.commands_button.click()
        request_transaction = chat_1.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        asset_button = request_transaction.asset_by_name(asset_name)
        request_transaction.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        request_transaction.request_transaction_button.click()
        chat_1_request_message = chat_1.get_incoming_transaction()
        if not chat_1_request_message.is_element_displayed():
            self.drivers[0].fail('No incoming transaction in 1-1 chat is shown for recipient after requesting STT')

        home_2.just_fyi('Check that transaction message is fetched from offline and sign transaction')
        profile_2.airplane_mode_button.click()
        transaction_request_pn = 'Request transaction'
        device_2.open_notification_bar()
        if not device_2.element_by_text(transaction_request_pn).is_element_displayed(60):
            self.errors.append("Push notification is not received after going back from offline")
        device_2.element_by_text(transaction_request_pn).click()
        home_2.connection_offline_icon.wait_for_invisibility_of_element(120)
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message = chat_2.get_outgoing_transaction()
        chat_2_sender_message.wait_for_visibility_of_element(60)
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.address_received)
        send_message = chat_2_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction(keycard=True)
        chat_1.toggle_airplane_mode()

        home_2.just_fyi('Check that transaction message is updated with new status after offline')
        [chat.toggle_airplane_mode() for chat in (chat_1, chat_2)]
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, token=True)
        for home in (home_1, home_2):
            home.toggle_airplane_mode()
            home.home_button.double_click()
        home_1.get_chat(sender['username']).click()
        home_2.get_chat(recipient_username).click()
        chat_2_sender_message.transaction_status.wait_for_element_text(chat_2_sender_message.confirmed, wait_time=120)

        home_1.just_fyi('Check that can find tx in history and balance is updated after offline')
        [home.wallet_button.click() for home in (home_1, home_2)]
        wallet_2.wait_balance_is_changed('STT', initial_amount_stt)
        wallet_1.wait_balance_is_changed('STT', scan_tokens=True)
        [wallet.find_transaction_in_history(amount=amount, asset='STT') for wallet in (wallet_1, wallet_2)]
        self.errors.verify_no_errors()

    @marks.testrail_id(6257)
    # TODO: may be removed and couple of checks from it added to TestCommandsMultipleDevicesMerged;
    #  doesn't make a lot of sense as separate e2e
    def test_1_1_chat_command_network_mismatch_for_send_tx_request_in_1_1_chat(self):
        sender = transaction_senders['ETH_1']
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sign_in_1.recover_access(passphrase=sender['passphrase'])
        sign_in_2.create_user()
        home_1, home_2 = sign_in_1.get_home_view(), sign_in_2.get_home_view()
        wallet_1 = home_1.wallet_button.click()
        wallet_1.home_button.click()
        profile_2 = home_2.profile_button.click()
        username_2 = profile_2.default_username_text.text
        profile_2.switch_network()

        chat_2 = home_2.add_contact(sender['public_key'])
        chat_2.send_message("Hey there!")
        amount = chat_2.get_unique_amount()

        chat_2.commands_button.click()
        request_transaction = chat_2.request_command.click()
        request_transaction.amount_edit_box.set_value(amount)
        request_transaction.confirm()
        request_transaction.request_transaction_button.click()
        chat_2_request_message = chat_2.get_incoming_transaction()

        chat_2_request_message.long_press_element()
        if chat_2.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Incoming transaction message!')

        chat_1 = home_1.get_chat(username_2).click()
        chat_1_sender_message = chat_1.get_outgoing_transaction()
        chat_1_sender_message.long_press_element()
        if chat_1.reply_message_button.is_element_displayed():
            self.errors.append('Reply is available on long-tap on Outgoing transaction message!')
        send_message = chat_1_sender_message.sign_and_send.click()
        send_message.next_button.click()
        send_message.sign_transaction()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount)
        chat_1_sender_message.transaction_status.wait_for_element_text(chat_1_sender_message.confirmed)
        wallet_2 = chat_2.wallet_button.click()
        wallet_2.accounts_status_account.click()
        wallet_2.swipe_down()
        wallet_2.home_button.click(desired_view="chat")
        if chat_2_request_message.transaction_status == chat_1_sender_message.confirmed:
            self.errors.append("Transaction is shown as confirmed on mainnet, but was sent on ropsten!")
        self.errors.verify_no_errors()

    @marks.testrail_id(6636)
    def test_profile_show_profile_picture_and_online_indicator_settings(self):
        # TODO: add check by make photo in this test
        # profile_1.just_fyi("Set user Profile image by taking Photo")
        # home_1.profile_button.click()
        # profile_1.edit_profile_picture(file_name='sauce_logo.png', update_by='Make Photo')
        # home_1.home_button.click(desired_view='chat')
        # public_chat_1.chat_message_input.send_keys(message)
        # public_chat_1.send_message_button.click()
        #
        # if public_chat_2.chat_element_by_text(message).member_photo.is_element_image_similar_to_template(
        #         'sauce_logo.png'):
        #     self.drivers[0].fail('Profile picture was not updated in chat after making photo')
        ####
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user(enable_notifications=True)
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        public_key_1, default_username_1 = profile_1.get_public_key_and_username(return_username=True)
        public_key_2, default_username_2 = profile_2.get_public_key_and_username(return_username=True)
        logo_online, logo_default, logo_chats, logo_group = 'logo_new.png', 'sauce_logo.png', 'logo_chats_view.png', 'group_logo.png'

        [profile.home_button.click() for profile in (profile_1, profile_2)]
        home_1.add_contact(public_key_2)
        home_1.profile_button.click()

        profile_1.just_fyi("Set user Profile image from Gallery")
        profile_1.edit_profile_picture(file_name=logo_default)
        home_1.profile_button.click()
        profile_1.swipe_down()

        profile_1.just_fyi('set status in profile')
        device_1_status = 'My new update!'
        timeline = profile_1.status_button.click()
        timeline.set_new_status(device_1_status)
        if not timeline.timeline_own_account_photo.is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in timeline')

        profile_1.just_fyi('Check profile image it is not in mentions because user not in contacts yet')
        one_to_one_chat_2 = home_2.add_contact(public_key_1, add_in_contacts=False)
        one_to_one_chat_2.chat_message_input.set_value('@' + default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if one_to_one_chat_2.user_profile_image_in_mentions_list(
                default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture is updated in 1-1 chat mentions list of contact not in Contacts list')

        profile_1.just_fyi('Check profile image is in mentions because now user was added in contacts')
        one_to_one_chat_2.add_to_contacts.click()
        one_to_one_chat_2.send_message("hey")
        one_to_one_chat_2.chat_message_input.set_value('@' + default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if not one_to_one_chat_2.user_profile_image_in_mentions_list(
                default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in 1-1 chat mentions list')
        home_1.reopen_app()
        one_to_one_chat_2.get_back_to_home_view()

        profile_1.just_fyi('Check profile image is updated in Group chat view')
        profile_2 = one_to_one_chat_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()
        profile_2.online_indicator.wait_for_visibility_of_element(180)
        if not profile_2.profile_picture.is_element_image_similar_to_template('new_profile_online.png'):
            self.errors.append('Profile picture was not updated on user Profile view')
        profile_2.close_button.click()
        home_2.home_button.double_click()
        if not home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_chats):
            self.errors.append('User profile picture was not updated on Chats view')

        profile_1.just_fyi('Check profile image updated in user profile view in Group chat views')
        group_chat_name, group_chat_message = 'new_group_chat', 'Trololo'
        group_chat_2 = home_2.create_group_chat(user_names_to_add=[default_username_1])

        group_chat_2.send_message('Message', wait_chat_input_sec=10)
        group_chat_1 = home_1.get_chat(group_chat_name).click()
        group_chat_1.join_chat_button.click()
        group_chat_1.send_message(group_chat_message)
        if not group_chat_2.chat_element_by_text(group_chat_message).member_photo.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture was not updated in message Group chat view')
        home_2.put_app_to_background()

        profile_1.just_fyi('Check profile image updated in group chat invite')
        home_1.get_back_to_home_view()
        new_group_chat = 'new_gr'
        home_2.click_system_back_button()
        home_2.open_notification_bar()
        home_1.create_group_chat(user_names_to_add=[default_username_2], group_chat_name=new_group_chat)

        invite = group_chat_2.pn_invited_to_group_chat(default_username_1, new_group_chat)
        pn = home_2.get_pn(invite)
        if pn:
            if not pn.group_chat_icon.is_element_image_similar_to_template(logo_group):
                self.errors.append("Group chat invite is not updated with custom logo!")
            pn.click()
        else:
            home_2.click_system_back_button(2)

        profile_1.just_fyi('Check profile image updated in on login view')
        home_1.profile_button.click()
        profile_1.logout()
        sign_in_1 = home_1.get_sign_in_view()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on Multiaccounts list select login view')
        sign_in_1.element_by_text(default_username_1).click()
        if not sign_in_1.get_multiaccount_by_position(1).account_logo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated on account login view')
        sign_in_1.password_input.set_value(common_password)
        sign_in_1.sign_in_button.click()

        profile_1.just_fyi('Remove user from contact and check there is no profile image displayed')
        group_chat_2.profile_button.double_click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()
        one_to_one_chat_2.remove_from_contacts.click()
        # Send message to User 2 so update of profile image picked up
        group_chat_1 = home_1.get_chat('new_group_chat').click()
        group_chat_1.send_message(group_chat_message)
        one_to_one_chat_2.close_button.click()
        one_to_one_chat_2.home_button.double_click()
        if home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture is not default to default after user removed from Contacts')

        profile_2.just_fyi('Enable to see profile image from "Everyone" setting')
        home_2.profile_button.double_click()
        profile_2.privacy_and_security_button.click()
        profile_2.show_profile_pictures_of.scroll_and_click()
        profile_2.element_by_translation_id("everyone").click()
        group_chat_1.send_message(group_chat_message)
        profile_2.home_button.click(desired_view='home')
        if not home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_chats):
            self.errors.append('User profile picture is not returned to default after user removed from Contacts')
        self.errors.verify_no_errors()

    @marks.testrail_id(5432)
    @marks.medium
    # TODO: can be united with other 2-driver profile e2e
    def test_profile_custom_bootnodes(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        public_key = home_2.get_public_key_and_username()

        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        username_1, username_2 = profile_1.default_username_text.text, profile_2.default_username_text.text

        profile_1.just_fyi('Add custom bootnode, enable bootnodes and check validation')
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.add_bootnode_button.click()
        profile_1.specify_name_input.set_value('test')
        # TODO: blocked as validation is missing for bootnodes (rechecked 23.11.21, valid)
        # profile_1.bootnode_address_input.set_value('invalid_bootnode_address')
        # if not profile_1.element_by_text_part('Invalid format').is_element_displayed():
        #      self.errors.append('Validation message about invalid format of bootnode is not shown')
        # profile_1.save_button.click()
        # if profile_1.add_bootnode_button.is_element_displayed():
        #      self.errors.append('User was navigated to another screen when tapped on disabled "Save" button')
        # profile_1.bootnode_address_input.clear()
        profile_1.bootnode_address_input.set_value(bootnode_address)
        profile_1.save_button.click()
        profile_1.enable_bootnodes.click()
        profile_1.home_button.click()

        profile_1.just_fyi('Add contact and send first message')
        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        profile_2.home_button.click()
        chat_2 = home_2.get_chat(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        chat_2.add_to_contacts.click()

        profile_1.just_fyi('Disable custom bootnodes')
        chat_1.profile_button.click()
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.enable_bootnodes.click()
        profile_1.home_button.click()

        profile_1.just_fyi('Send message and check that it is received after disabling bootnodes')
        home_1.get_chat(username_2).click()
        message_1 = 'new message'
        chat_1.chat_message_input.send_keys(message_1)
        chat_1.send_message_button.click()
        for chat in chat_1, chat_2:
            if not chat.chat_element_by_text(message_1).is_element_displayed():
                self.errors.append('Message was not received after enabling bootnodes!')
        self.errors.verify_no_errors()

    @marks.testrail_id(5436)
    @marks.flaky
    # TODO: can be united with other 2-driver profile e2e
    def test_profile_add_switch_delete_custom_history_node(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        public_key = home_2.get_public_key_and_username()
        home_2.home_button.click()

        profile_1 = home_1.profile_button.click()
        username_1 = profile_1.default_username_text.text

        profile_1.just_fyi('disable autoselection')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        mailserver = profile_1.return_mailserver_name(mailserver_hk, used_fleet)
        profile_1.mail_server_auto_selection_button.click()
        profile_1.mail_server_by_name(mailserver).click()
        profile_1.confirm_button.click()

        profile_1.just_fyi('add custom mailserver (check address/name validation) and connect to it')
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.save_button.click()
        if profile_1.element_by_text(mailserver).is_element_displayed():
            self.errors.append('Could add custom mailserver with empty address and name')
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value(mailserver_address[:-3])
        profile_1.save_button.click()
        if not profile_1.element_by_text_part("Invalid format").is_element_displayed():
            self.errors.append('could add custom mailserver with invalid address')
        profile_1.mail_server_address_input.clear()
        profile_1.mail_server_address_input.set_value(mailserver_address)
        profile_1.save_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()
        if profile_1.element_by_text_part("Error connecting").is_element_displayed(40):
            profile_1.retry_to_connect_to_mailserver()
        profile_1.get_back_to_home_view()
        profile_1.home_button.click()

        profile_1.just_fyi('start chat with user2 and check that all messages are delivered')
        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2 = home_2.get_chat(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        message_1 = 'new message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element()

        profile_1.just_fyi('delete custom mailserver')
        chat_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.element_by_text(mailserver).scroll_to_element()
        profile_1.element_by_text(mailserver).click()
        profile_1.confirm_button.click()
        profile_1.element_by_text(server_name).scroll_to_element()
        profile_1.element_by_text(server_name).click()
        profile_1.mail_server_delete_button.scroll_to_element()
        profile_1.mail_server_delete_button.click()
        profile_1.mail_server_confirm_delete_button.click()
        if profile_1.element_by_text(server_name).is_element_displayed():
            self.errors.append('Deleted custom mailserver is shown')
        profile_1.get_back_to_home_view()
        profile_1.relogin()
        chat_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        if profile_1.element_by_text(server_name).is_element_displayed():
            self.errors.append('Deleted custom mailserver is shown after relogin')

        self.errors.verify_no_errors()

    @marks.testrail_id(5767)
    # TODO: can be united with other 2-driver profile e2e
    def test_profile_can_not_connect_to_history_node(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        profile_1 = home_1.profile_button.click()

        profile_1.just_fyi('add non-working mailserver and connect to it')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.mail_server_auto_selection_button.click()
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value('%s%s' % (mailserver_address[:-3], '553'))
        profile_1.save_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.wait_and_click()
        profile_1.confirm_button.wait_and_click()

        profile_1.just_fyi('check that popup "Error connecting" will not reappear if tap on "Cancel"')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        profile_1.cancel_button.click()

        home_2.just_fyi('send several messages to public channel')
        public_chat_name = home_2.get_random_chat_name()
        message = 'test_message'
        public_chat_2 = home_2.join_public_chat(public_chat_name)
        public_chat_2.chat_message_input.send_keys(message)
        public_chat_2.send_message_button.click()
        public_chat_2.back_button.click()

        profile_1.just_fyi('join same public chat and try to reconnect via "Tap to reconnect" and check "Connecting"')
        profile_1.home_button.click()
        public_chat_1 = home_1.join_public_chat(public_chat_name)
        public_chat_1.reopen_app()

        profile_1.just_fyi('check that still connected to custom mailserver after relogin')
        home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        if not profile_1.element_by_text(server_name).is_element_displayed():
            self.drivers[0].fail("Not connected to custom mailserver after re-login")

        profile_1.just_fyi('check that can RETRY to connect')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        public_chat_1.element_by_translation_id('mailserver-retry', uppercase=True).wait_and_click(60)

        profile_1.just_fyi('check that can pick another mailserver and receive messages')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        profile_1.element_by_translation_id('mailserver-pick-another', uppercase=True).wait_and_click(120)
        mailserver = profile_1.return_mailserver_name(mailserver_ams, used_fleet)
        profile_1.element_by_text(mailserver).click()
        profile_1.confirm_button.click()
        profile_1.home_button.click()
        home_1.get_chat('#%s' % public_chat_name).click()
        if not public_chat_1.chat_element_by_text(message).is_element_displayed(60):
            self.errors.append("Chat history wasn't fetched")

        self.errors.verify_no_errors()

    @marks.testrail_id(6332)
    # TODO: can be united with other 2-driver profile e2e
    def test_profile_disable_use_history_node(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        profile_1 = home_1.profile_button.click()

        home_2.just_fyi('send several messages to public channel')
        public_chat_name = home_2.get_random_chat_name()
        message, message_no_history = 'test_message', 'history node is disabled'
        public_chat_2 = home_2.join_public_chat(public_chat_name)
        public_chat_2.send_message(message)

        profile_1.just_fyi(
            'disable use_history_node and check that no history is fetched but you can still send messages')
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.use_history_node_button.click()
        profile_1.home_button.click()
        public_chat_1 = home_1.join_public_chat(public_chat_name)
        if public_chat_1.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append('Chat history was fetched when use_history_node is disabled')
        public_chat_1.send_message(message_no_history)
        if not public_chat_2.chat_element_by_text(message_no_history).is_element_displayed(30):
            self.errors.append('Message sent when use_history_node is disabled was not received')
        public_chat_1.profile_button.click()
        profile_1.relogin()
        home_1.get_chat('#%s' % public_chat_name).click()
        if public_chat_1.chat_element_by_text(message).is_element_displayed(30):
            self.drivers[0].fail('History was fetched after relogin when use_history_node is disabled')

        profile_1.just_fyi('enable use_history_node and check that history is fetched')
        home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.use_history_node_button.click()
        profile_1.home_button.click(desired_view='chat')
        if not public_chat_1.chat_element_by_text(message).is_element_displayed(60):
            self.errors.append('History was not fetched after enabling use_history_node')
        self.errors.verify_no_errors()

    @marks.testrail_id(695856)
    # TODO: can be included to TestPairingMultipleDevicesMerged after #13257
    def test_pair_devices_sync_photo_community_group_chats(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.backup_recovery_phrase_button.click()
        recovery_phrase = profile_1.backup_recovery_phrase()
        profile_1.home_button.double_click()
        name_1, name_2 = 'device_%s' % device_1.driver.number, 'device_%s' % device_2.driver.number
        comm_before_sync_name, channel, message = 'b-%s' % home_1.get_random_chat_name(), 'some-rand-chann', 'comm_message'
        comm_joined_name = 'Status'
        comm_after_sync_name = 'a-public-%s' % home_1.get_random_chat_name()
        group_chat_name = 'group-%s' % home_1.get_random_chat_name()
        channel_after_sync, message_after_sync = 'chann-after-sync', 'sent after sync'

        device_1.just_fyi('join Status community, create community, create group chat, edit user picture')
        # Follow Status community
        # TODO: no predefined community to follow now
        # home_1.element_by_text(comm_joined_name).scroll_and_click()
        # from views.chat_view import CommunityView
        # comm_to_join_1 = CommunityView(self.drivers[0])
        # comm_to_join_1.follow_button.wait_and_click()
        # comm_to_join_1.home_button.double_click()
        # Create community as admin, add channel, send message
        comm_before_1 = home_1.create_community(comm_before_sync_name)
        channel_before_1 = comm_before_1.add_channel(channel)
        channel_before_1.send_message(message)
        home_1.home_button.double_click()
        # Starting group chat
        one_to_one_1 = home_1.add_contact(basic_user['public_key'])
        one_to_one_1.home_button.click()
        group_chat_1 = home_1.create_group_chat([basic_user['username']], group_chat_name)
        group_chat_1.home_button.click()
        # Editing profile picture
        home_1.profile_button.double_click()
        profile_1.edit_profile_picture('sauce_logo.png')

        device_2.just_fyi('go to profile > Devices, set device name, discover device 2 to device 1')
        home_2 = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        profile_2 = home_2.profile_button.click()

        device_2.just_fyi('Pair main and secondary devices')
        profile_2.discover_and_advertise_device(name_2)
        profile_1.discover_and_advertise_device(name_1)
        profile_1.get_toggle_device_by_name(name_2).wait_and_click()
        profile_1.sync_all_button.click()
        profile_1.sync_all_button.wait_for_visibility_of_element(15)
        [device.profile_button.double_click() for device in (profile_1, profile_2)]

        device_2.just_fyi('check that created/joined community and profile details are updated')
        home_2 = profile_2.home_button.click()
        # TODO: no predefined community to follow
        # for community in (comm_before_sync_name, comm_joined_name):
        if not home_2.get_chat(comm_before_sync_name, community=True).is_element_displayed():
            self.errors.append('Community %s was not appeared after initial sync' % comm_before_sync_name)
        comm_before_2 = home_2.get_chat(comm_before_sync_name, community=True).click()
        channel_2 = comm_before_2.get_chat(channel).click()
        if not channel_2.chat_element_by_text(message).is_element_displayed(30):
            self.errors.append("Message sent to community channel before sync is not shown!")

        device_1.just_fyi("Send message, add new channel and check it will be synced")
        home_1.home_button.click()
        home_1.get_chat(comm_before_sync_name, community=True).click()
        channel_1 = comm_before_1.get_chat(channel).click()
        channel_1.send_message(message_after_sync)
        if not channel_2.chat_element_by_text(message_after_sync).is_element_displayed(30):
            self.errors.append("Message sent to community channel after sync is not shown!")
        [channel.back_button.click() for channel in (channel_1, channel_2)]
        [home.get_chat(comm_before_sync_name, community=True).click() for home in (home_1, home_2)]
        comm_before_1.add_channel(channel_after_sync)
        if not comm_before_2.get_chat(channel_after_sync).is_element_displayed(30):
            self.errors.append("New added channel after sync is not shown!")

        device_1.just_fyi("Leave community and check it will be synced")
        [home.home_button.double_click() for home in (home_1, home_2)]
        home_1.get_chat(comm_before_sync_name, community=True).click()
        comm_before_1.leave_community()
        if not home_2.element_by_text_part(comm_before_sync_name).is_element_disappeared(30):
            self.errors.append("Leaving community was not synced!")

        device_1.just_fyi("Adding new community and check it will be synced")
        home_1.create_community(comm_after_sync_name)
        if not home_2.element_by_text(comm_after_sync_name).is_element_displayed(30):
            self.errors.append('Added community was not appeared after initial sync')

        # TODO: skip until #11558 (rechecked 23.11.21, valid)
        # home_2.profile_button.click()
        # if not profile_2.profile_picture.is_element_image_equals_template('sauce_logo_profile.png'):
        #     self.errors.append('Profile picture was not updated after initial sync')
        # profile_2.home_button.click()
        #
        device_1.just_fyi('send message to group chat, check that message in group chat is shown')
        home_1 = profile_1.home_button.click()
        home_1.get_chat(group_chat_name).click()
        group_chat_1.send_message(message_after_sync)
        group_chat_1.back_button.click()
        group_chat_2 = home_2.get_chat(group_chat_name).click()
        if not group_chat_2.chat_element_by_text(message_after_sync).is_element_displayed():
            self.errors.append('"%s" message in group chat is not synced' % message_after_sync)

        self.errors.verify_no_errors()

    @marks.testrail_id(6317)
    # TODO: can be included to TestPairingMultipleDevicesMerged after #13257
    def test_pair_devices_group_chat_different_messages_nicknames(self):
        self.create_drivers(3)
        device_1, device_2, device_3 = SignInView(self.drivers[0]), SignInView(self.drivers[1]), SignInView(
            self.drivers[2])
        home_1 = device_1.create_user()
        profile_1 = home_1.profile_button.click()
        profile_1.privacy_and_security_button.click()
        profile_1.backup_recovery_phrase_button.click()
        profile_1.ok_continue_button.click()
        recovery_phrase = profile_1.get_recovery_phrase()
        profile_1.close_button.click()
        profile_1.home_button.click()
        device_2.put_app_to_background_and_back()
        home_3 = device_3.create_user()
        public_key_3, username_3 = home_3.get_public_key_and_username(return_username=True)
        device_3.home_button.click()
        device_1.put_app_to_background_and_back()
        device_1_name, device_2_name, group_chat_name = 'creator', 'paired', 'some group chat'
        home_2 = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))

        device_1.just_fyi('Add contact, start group chat')
        nickname = 'my_tester'
        home_1.add_contact(public_key_3, nickname=nickname)
        home_1.get_back_to_home_view()
        chat_1 = home_1.create_group_chat([username_3], group_chat_name)
        chat_3 = home_3.get_chat(group_chat_name).click()
        chat_3.join_chat_button.click()

        device_2.just_fyi('Go to profile > Devices, set device name, discover device 2 to device 1')
        profile_2 = home_2.profile_button.click()
        profile_2.discover_and_advertise_device(device_2_name)
        device_1.profile_button.click()
        profile_1.discover_and_advertise_device(device_1_name)
        profile_1.get_toggle_device_by_name(device_2_name).click()
        profile_1.sync_all_button.click()
        profile_1.sync_all_button.wait_for_visibility_of_element(15)
        profile_1.click_system_back_button(2)

        device_1.just_fyi('Send message to group chat and verify it on all devices')
        text_message = 'some text'
        profile_1.home_button.click(desired_view='chat')
        profile_2.home_button.click()
        chat_1.send_message(text_message)
        chat_2 = home_2.get_chat(group_chat_name).click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.chat_element_by_text(text_message).is_element_displayed():
                self.errors.append('Message was sent, but it is not shown')

        device_3.just_fyi('Send message to group chat as member and verify nickname on it')
        message_from_member = 'member1'
        chat_3.send_message(message_from_member)
        chat_1.chat_element_by_text(message_from_member).wait_for_visibility_of_element(20)
        for chat in chat_1, chat_2:
            if not chat.chat_element_by_text(message_from_member).username != '%s %s' % (nickname, username_3):
                self.errors.append('Nickname is not shown in group chat')

        device_1.just_fyi('Send image to group chat and verify it on all devices')
        chat_1.show_images_button.click()
        chat_1.allow_button.click()
        chat_1.first_image_from_gallery.click()
        chat_1.send_message_button.click()
        chat_1.chat_message_input.click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.image_message_in_chat.is_element_displayed(60):
                self.errors.append('Image is not shown in chat after sending for %s' % chat.driver.number)

        device_1.just_fyi('Send audio message to group chat and verify it on all devices')
        chat_1.record_audio_message(message_length_in_seconds=3)
        device_1.send_message_button.click()
        chat_1.chat_message_input.click()
        for chat in chat_1, chat_2, chat_3:
            if not chat.play_pause_audio_message_button.is_element_displayed(30):
                self.errors.append('Audio message is not shown in chat after sending!')

        device_1.just_fyi('Send sticker to group chat and verify it on all devices')
        chat_1.profile_button.click()
        profile_1.switch_network()
        home_1.get_chat(group_chat_name).click()
        chat_1.install_sticker_pack_by_name()
        chat_1.sticker_icon.click()
        if not chat_1.sticker_message.is_element_displayed(30):
            self.errors.append('Sticker was not sent')
        self.errors.verify_no_errors()

    @marks.testrail_id(6330)
    # TODO: can be re-done to 1-driver test (sending between different account)
    def test_wallet_can_send_tx_all_tokens_via_max_option(self):
        sender = transaction_senders['ETH_STT_2']
        receiver = transaction_senders['ETH_1']
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.recover_access(sender['passphrase']), device_2.recover_access(receiver['passphrase'])
        wallet_sender = home_1.wallet_button.click()
        wallet_receiver = home_2.wallet_button.click()

        if wallet_receiver.asset_by_name('STT').is_element_present(10):
            initial_balance = wallet_receiver.get_asset_amount_by_name("STT")
        else:
            initial_balance = '0'

        device_1.just_fyi("Sending token amount to device who will use Set Max option for token")
        amount = '0.012345678912345678'
        wallet_sender.send_transaction(asset_name='STT', amount=amount, recipient=receiver['address'])
        wallet_receiver.wait_balance_is_changed(asset='STT', initial_balance=initial_balance, scan_tokens=True)
        wallet_receiver.accounts_status_account.click()

        device_1.just_fyi("Send all tokens via Set Max option")
        send_transaction = wallet_receiver.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        asset_name = 'STT'
        asset_button = send_transaction.asset_by_name(asset_name)
        send_transaction.select_asset_button.click_until_presence_of_element(
            send_transaction.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_transaction.set_max_button.click()
        send_transaction.set_recipient_address(sender['address'])
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        wallet_receiver.close_button.click()
        initial_balance = float(initial_balance) + float(amount)
        wallet_receiver.wait_balance_is_changed(asset='STT', initial_balance=str(initial_balance), scan_tokens=True)
