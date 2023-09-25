import datetime

import pytest
from _pytest.outcomes import Failed
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, run_in_parallel, transl
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.chat_view import ChatView
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_3")
@marks.critical
class TestGroupChatMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.message_before_adding = 'message before adding new user'
        self.message_to_admin = 'Hey, admin!'

        self.homes, self.public_keys, self.usernames, self.chats = {}, {}, {}, {}
        for key in self.drivers:
            sign_in = SignInView(self.drivers[key])
            self.homes[key] = sign_in.create_user(enable_notifications=True)
            SignInView(self.drivers[2]).put_app_to_background_and_back()
            self.public_keys[key], self.usernames[key] = sign_in.get_public_key(True)
            sign_in.home_button.click()
            SignInView(self.drivers[0]).put_app_to_background_and_back()
        self.chat_name = self.homes[0].get_random_chat_name()

        self.homes[0].just_fyi('Admin adds future members to contacts')
        for i in range(1, 3):
            self.homes[0].add_contact(self.public_keys[i])
            self.homes[0].home_button.double_click()

        self.homes[0].just_fyi('Members add admin to contacts to see PNs and put app in background')
        for i in range(1, 3):
            self.homes[i].handle_contact_request(self.usernames[0])
            self.homes[i].home_button.double_click()

        self.homes[0].just_fyi('Admin creates group chat')
        self.chats[0] = self.homes[0].create_group_chat([self.usernames[1]], self.chat_name)
        for i in range(1, 3):
            self.chats[i] = ChatView(self.drivers[i])

        self.chats[0].send_message(self.message_before_adding)

    @marks.testrail_id(3994)
    def test_group_chat_push_system_messages_when_invited(self):
        self.homes[1].just_fyi("Check system messages in PNs")
        self.homes[2].put_app_to_background_and_back()
        self.homes[1].put_app_to_background()
        self.homes[1].open_notification_bar()
        pns = [self.chats[0].pn_invited_to_group_chat(self.usernames[0], self.chat_name),
               self.chats[0].pn_wants_you_to_join_to_group_chat(self.usernames[0], self.chat_name)]
        for pn in pns:
            if not self.homes[1].get_pn(pn):
                self.errors.append('%s is not shown after invite to group chat' % pn)
        if self.homes[1].get_pn(pns[0]):
            group_invite_pn = self.homes[1].get_pn(pns[0])
            group_invite_pn.click()
        else:
            self.homes[1].click_system_back_button(2)
            self.homes[1].get_chat(self.chat_name).click()

        self.homes[1].just_fyi("Check system messages in group chat for admin and member")
        create_system_message = self.chats[0].create_system_message(self.usernames[0], self.chat_name)
        has_added_system_message = self.chats[0].has_added_system_message(self.usernames[0], self.usernames[1])

        create_for_admin_system_message = 'You created the group %s' % self.chat_name
        joined_message = "You've joined %s from invitation by %s" % (self.chat_name, self.usernames[0])

        for message in [create_for_admin_system_message, create_system_message, has_added_system_message]:
            if not self.chats[0].element_by_text(message).is_element_displayed():
                self.errors.append('%s system message is not shown' % message)

        for message in [joined_message, create_system_message, has_added_system_message]:
            if not self.chats[1].element_by_text(message).is_element_displayed():
                self.errors.append('%s system message is not shown' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(700732)
    def test_group_chat_add_new_member(self):
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[0].get_chat(self.chat_name).click()
        self.chats[0].add_members_to_group_chat([self.usernames[2]])

        self.chats[2].just_fyi("Check there will be PN and no unread in AC for a new member")
        if self.homes[2].notifications_unread_badge.is_element_displayed(60):
            self.drivers[2].fail("Group chat appeared in AC!")
        self.homes[2].open_notification_bar()
        if not self.homes[2].element_by_text_part(self.usernames[0]).is_element_displayed():
            self.errors.append("PN about group chat invite is not shown when invited by mutual contact")

        self.homes[2].click_system_back_button()

        self.homes[2].just_fyi("Check new group appeared in chat list for a new member")
        if not self.homes[2].get_chat(self.chat_name).is_element_displayed(60):
            self.drivers[2].fail("New group chat hasn't appeared in chat list")

        self.homes[2].get_chat(self.chat_name).click()

        for message in (self.message_to_admin, self.message_before_adding):
            if self.chats[2].chat_element_by_text(message).is_element_displayed():
                self.errors.append('%s is shown for new user' % message)
        self.errors.verify_no_errors()

    @marks.testrail_id(5756)
    def test_group_chat_highligted(self):
        chat_name = 'for_invited'
        [self.homes[i].home_button.double_click() for i in range(3)]
        self.homes[0].create_group_chat([self.usernames[1]], chat_name)

        self.homes[1].just_fyi("Check that new group chat from contact is highlited")
        chat_2_element = self.homes[1].get_chat(chat_name)
        if chat_2_element.no_message_preview.is_element_differs_from_template('highligted_preview_group.png', 0):
            self.errors.append("Preview message is not hightligted or text is not shown! ")
        chat_2 = self.homes[1].get_chat(chat_name).click()
        chat_2.home_button.click()
        if not chat_2_element.no_message_preview.is_element_differs_from_template('highligted_preview_group.png', 0):
            self.errors.append("Preview message is still hightligted after opening! ")

    @marks.testrail_id(3997)
    def test_group_chat_leave_relogin(self):
        left_system_message = self.chats[1].leave_system_message(self.usernames[0])
        self.drivers[2].quit()
        [self.homes[i].home_button.double_click() for i in range(2)]
        self.homes[0].home_button.double_click()
        self.homes[1].get_chat(self.chat_name).click()

        self.homes[0].just_fyi("Admin deleted chat via long press")
        self.homes[0].leave_chat_long_press(self.chat_name)

        self.homes[1].just_fyi('Check that leave system message is presented after user left the group chat')
        if not self.chats[1].chat_element_by_text(left_system_message).is_element_displayed():
            self.errors.append('System message when user leaves the chat is not shown')

        self.homes[0].just_fyi("Member sends some message, admin relogins and check chat does not reappear")
        self.chats[1].send_message(self.message_to_admin)
        self.homes[0].relogin()
        if self.homes[0].get_chat_from_home_view(self.chat_name).is_element_displayed():
            self.drivers[0].fail('Deleted %s is present after relaunch app' % self.chat_name)


@pytest.mark.xdist_group(name="new_one_3")
@marks.new_ui_critical
class TestGroupChatMultipleDeviceMergedNewUI(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.message_before_adding = 'message before adding new user'
        self.message_to_admin = 'Hey, admin!'
        self.public_keys, self.usernames, self.chats = {}, {}, {}
        self.sign_in_views = [SignInView(self.drivers[key]) for key in self.drivers]
        self.usernames = ('user admin', 'member_1', 'member_2')
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.sign_in_views[0].create_user, {'enable_notifications': True, 'username': self.usernames[0]}),
                    (self.sign_in_views[1].create_user, {'enable_notifications': True, 'username': self.usernames[1]}),
                    (self.sign_in_views[2].create_user, {'enable_notifications': True, 'username': self.usernames[2]})
                )
            )
        )
        self.homes = [sign_in.get_home_view() for sign_in in self.sign_in_views]
        self.public_keys = self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.homes[0].get_public_key,),
                    (self.homes[1].get_public_key,),
                    (self.homes[2].get_public_key,)
                )
            )
        )

        self.homes[0].just_fyi('Admin adds future members to contacts')

        for i in range(3):
            self.homes[i].navigate_back_to_home_view()
            self.homes[i].chats_tab.click()

        for i in range(1, 3):
            self.homes[0].add_contact(self.public_keys[i])

        self.homes[0].just_fyi('Members add admin to contacts to see PNs and put app in background')
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.homes[1].handle_contact_request, {'username': self.usernames[0]}),
                    (self.homes[2].handle_contact_request, {'username': self.usernames[0]})
                )
            )
        )
        for i in range(1, 3):
            self.homes[i].navigate_back_to_home_view()

        self.homes[0].just_fyi('Admin creates group chat')
        self.chat_name = self.homes[0].get_random_chat_name()
        self.homes[0].chats_tab.click()
        self.chats[0] = self.homes[0].create_group_chat(user_names_to_add=[self.usernames[1], self.usernames[2]],
                                                        group_chat_name=self.chat_name)
        for i in range(1, 3):
            self.chats[i] = ChatView(self.drivers[i])

        self.chats[0].send_message(self.message_before_adding)

    @marks.testrail_id(702807)
    def test_group_chat_join_send_text_messages_push(self):
        message_to_admin = self.message_to_admin
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]
        self.homes[1].get_chat(self.chat_name).click()
        self.chats[0].open_notification_bar()

        self.chats[1].send_message(message_to_admin)

        self.chats[0].just_fyi('Check that PN is received and after tap you are redirected to group chat')
        pn = self.homes[0].get_pn(message_to_admin)
        if pn:
            pn.click()
        else:
            self.errors.append("No PN was received on new message for message in group chat")
            self.homes[0].click_system_back_button()
            self.homes[0].get_chat(self.chat_name).click()

        self.chats[1].just_fyi('Check message status and message delivery')
        self.chats[1].chat_element_by_text(message_to_admin).wait_for_status_to_be('Delivered', timeout=120)
        if not self.chats[0].chat_element_by_text(message_to_admin).is_element_displayed(30):
            self.errors.append('Message %s was not received by admin' % message_to_admin)
        self.errors.verify_no_errors()

    @marks.testrail_id(703202)
    def test_group_chat_reactions(self):
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]
        [self.homes[i].get_chat(self.chat_name).click() for i in range(3)]
        message = "This is a test message to check some reactions."
        self.chats[0].just_fyi("Admin sends a message")
        self.chats[0].send_message(message)

        self.chats[1].just_fyi("Member_1 sets 2 reactions on the message: 'thumbs-up' and 'love'")
        self.chats[1].set_reaction(message=message, emoji="thumbs-up")
        self.chats[1].set_reaction(message=message, emoji="love")

        self.chats[2].just_fyi("Member_2 sets 2 reactions on the message: 'thumbs-up' and 'laugh'")
        self.chats[2].add_remove_same_reaction(message=message, emoji="thumbs-up")
        self.chats[2].set_reaction(message=message, emoji="laugh")

        for i in range(3):
            self.chats[i].just_fyi("Checking reactions count for each group member and admin")
            message_element = self.chats[i].chat_element_by_text(message)
            message_element.emojis_below_message(emoji="thumbs-up").wait_for_element_text(2)
            message_element.emojis_below_message(emoji="love").wait_for_element_text(1)
            message_element.emojis_below_message(emoji="laugh").wait_for_element_text(1)

        self.chats[0].just_fyi("Admin checks info about voted users")
        self.chats[0].chat_element_by_text(message).emojis_below_message(
            emoji="thumbs-up").long_press_until_element_is_shown(self.chats[0].authors_for_reaction(emoji="thumbs-up"))
        if not self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or not self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'thumbs-up' reaction.")

        self.chats[0].authors_for_reaction(emoji="love").click()
        if not self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'love' reaction.")

        self.chats[0].authors_for_reaction(emoji="laugh").click()
        if self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or not self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'laugh' reaction.")

        self.chats[0].just_fyi("Admin opens member_2 profile")
        self.chats[0].user_list_element_by_name(self.usernames[2]).click()
        try:
            username_shown = self.chats[0].get_profile_view().default_username_text.text
            if username_shown != self.usernames[2]:
                self.errors.append(
                    "Incorrect profile is opened from the list of reactions, username is %s but expected to be %s" % (
                        username_shown, self.usernames[2])
                )
        except NoSuchElementException:
            self.errors.append("User profile was not opened from the list of reactions")
        self.chats[0].navigate_back_to_chat_view()

        self.chats[1].just_fyi("Member_1 removes 'thumbs-up' reaction and adds 'sad' one")
        self.chats[1].add_remove_same_reaction(message=message, emoji="thumbs-up")
        self.chats[1].set_reaction(message=message, emoji="sad")

        self.chats[2].just_fyi("Member_2 removes 'laugh' reaction and adds 'sad' one")
        self.chats[2].add_remove_same_reaction(message=message, emoji="laugh")
        self.chats[2].add_remove_same_reaction(message=message, emoji="sad")

        for i in range(3):
            self.chats[i].just_fyi("Checking reactions count for each group member and admin after they were changed")
            message_element = self.chats[i].chat_element_by_text(message)
            try:
                message_element.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
                message_element.emojis_below_message(emoji="love").wait_for_element_text(1)
                message_element.emojis_below_message(emoji="sad").wait_for_element_text(2)
            except (Failed, NoSuchElementException):
                self.errors.append("Incorrect reactions count for %s after changing the reactions" % self.usernames[i])

        self.chats[0].just_fyi("Admin relogins")
        self.chats[0].reopen_app()
        self.homes[0].get_chat(self.chat_name).click()

        self.chats[0].just_fyi("Admin checks reactions count after relogin")
        message_element = self.chats[0].chat_element_by_text(message)
        try:
            message_element.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
            message_element.emojis_below_message(emoji="love").wait_for_element_text(1)
            message_element.emojis_below_message(emoji="sad").wait_for_element_text(2)
        except (Failed, NoSuchElementException):
            self.errors.append("Incorrect reactions count after relogin")

        for chat in self.chats[1], self.chats[2]:
            chat.just_fyi("Just making the session not to quit")
            chat.navigate_back_to_home_view()

        self.chats[0].just_fyi("Admin checks info about voted users after relogin")
        message_element.emojis_below_message(
            emoji="thumbs-up").long_press_until_element_is_shown(self.chats[0].authors_for_reaction(emoji="thumbs-up"))
        if self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or not self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'thumbs-up' reaction after relogin.")

        self.chats[0].authors_for_reaction(emoji="love").click()
        if not self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'love' reaction after relogin.")

        self.chats[0].authors_for_reaction(emoji="sad").click()
        if not self.chats[0].user_list_element_by_name(
                self.usernames[1]).is_element_displayed() or not self.chats[0].user_list_element_by_name(
            self.usernames[2]).is_element_displayed():
            self.errors.append("Incorrect users are shown for 'laugh' reaction after relogin.")

        self.errors.verify_no_errors()

    @marks.testrail_id(703297)
    def test_group_chat_send_image_save_and_share(self):
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]
        for i in range(3):
            self.homes[i].get_chat(self.chat_name).click()

        self.chats[1].just_fyi("Member_1 sends an image")
        image_description = "test image"
        self.chats[1].send_images_with_description(description=image_description, indexes=[2])

        self.chats[0].just_fyi("Admin checks image message")
        self.chats[0].chat_element_by_text(image_description).wait_for_visibility_of_element(60)
        if not self.chats[0].chat_element_by_text(
                image_description).image_in_message.is_element_image_similar_to_template('saucelabs_sauce_chat.png'):
            self.errors.append("Not expected image is shown to the admin.")

        self.chats[2].just_fyi("Member_2 checks image message")
        self.chats[2].chat_element_by_text(image_description).wait_for_visibility_of_element(60)
        if not self.chats[2].chat_element_by_text(
                image_description).image_in_message.is_element_image_similar_to_template('saucelabs_sauce_chat.png'):
            self.errors.append("Not expected image is shown to the member_2.")

        self.chats[0].just_fyi("Admin opens the image and shares it")
        self.chats[0].chat_element_by_text(image_description).image_in_message.click()
        self.chats[0].share_image_icon_button.click()
        self.chats[0].element_starts_with_text("Gmail").click()
        try:
            self.chats[0].wait_for_current_package_to_be('com.google.android.gm')
        except TimeoutException:
            self.errors.append("Admin can't share an image via Gmail.")
        self.chats[0].navigate_back_to_chat_view()

        self.chats[1].navigate_back_to_home_view()

        self.chats[2].just_fyi("Member_2 opens the image and saves it")
        self.chats[2].chat_element_by_text(image_description).image_in_message.click()
        self.chats[2].view_image_options_button.click()
        self.chats[2].save_image_icon_button.click()
        toast_element = self.chats[2].toast_content_element
        if toast_element.is_element_displayed():
            toast_element_text = toast_element.text
            if toast_element_text != self.chats[2].get_translation_by_key("photo-saved"):
                self.errors.append(
                    "Shown message '%s' doesn't match expected '%s' after saving an image for member_2." % (
                        toast_element_text, self.chats[2].get_translation_by_key("photo-saved")))
        else:
            self.errors.append("Message about saving a photo is not shown for member_2.")
        self.chats[2].navigate_back_to_chat_view()

        self.chats[2].just_fyi("Member_2 checks that image was saved in gallery")
        self.chats[2].show_images_button.click()
        self.chats[2].allow_button.click_if_shown()
        if not self.chats[2].get_image_by_index(0).is_element_image_similar_to_template("saucelabs_sauce_gallery.png"):
            self.errors.append("Image is not saved to gallery for member_2.")
        self.chats[2].navigate_back_to_chat_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702808)
    def test_group_chat_offline_pn(self):
        for i in range(1, 3):
            self.homes[i].navigate_back_to_home_view()
            self.homes[i].chats_tab.click()
            self.homes[i].groups_tab.click()
            self.homes[i].get_chat(self.chat_name).click()

        message_1, message_2 = 'message from old member', 'message from new member'

        self.homes[0].just_fyi("Put admin device to offline and send messages from members")
        app_package = self.drivers[0].current_package
        self.homes[0].toggle_airplane_mode()
        self.chats[1].send_message(message_1)
        self.chats[2].send_message(message_2)

        self.homes[0].just_fyi("Put admin device to online and check that messages and PNs will be fetched")
        self.homes[0].toggle_airplane_mode()
        self.homes[0].connection_offline_icon.wait_for_invisibility_of_element(60)
        self.homes[0].open_notification_bar()
        for message in (message_1, message_2):
            if self.homes[0].element_by_text(message).is_element_displayed(30):
                break
        else:
            self.errors.append('Messages PN was not fetched from offline')
        self.homes[0].click_system_back_button()
        # workaround for app closed after opening notifications
        if not self.homes[0].chats_tab.is_element_displayed():
            self.drivers[0].activate_app(app_package)
            SignInView(self.drivers[0]).sign_in()
        self.homes[0].chats_tab.click()
        self.homes[0].get_chat(self.chat_name).click()

        self.homes[0].just_fyi("check that messages are shown for every member")
        for i in range(3):
            for message in (message_1, message_2):
                if not self.chats[i].chat_element_by_text(message).is_element_displayed(30):
                    self.errors.append('%s if not shown for device %s' % (message, str(i)))
        self.errors.verify_no_errors()

    @marks.xfail(reason="Pin feature is in development", run=False)
    @marks.testrail_id(702732)
    def test_group_chat_pin_messages(self):
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]
        [self.homes[i].get_chat(self.chat_name).click() for i in range(3)]

        self.message_1, self.message_2, self.message_3, self.message_4 = \
            "Message 1", "Message 2", "Message 3", "Message 4"
        self.chats[0].just_fyi("Enter group chat and pin message there. It's pinned for both members.")

        self.chats[0].send_message(self.message_1)
        self.chats[0].pin_message(self.message_1, "pin-to-chat")
        if not (self.chats[0].chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed(30) and
                self.chats[1].chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed(30)):
            self.errors.append("Message 1 is not pinned in group chat!")

        self.chats[0].just_fyi("Check that non admin user can not unpin messages")
        self.chats[1].chat_element_by_text(self.message_1).long_press_element()
        if self.chats[1].element_by_translation_id("unpin-from-chat").is_element_displayed():
            self.errors.append("Unpin option is available for non-admin user")
        self.chats[1].tap_by_coordinates(500, 100)

        # not implemented yet :

        # self.home_1.just_fyi("Grant another user with admin rights and check he can unpin message now")
        # self.group_chat_1.chat_options.click()
        # group_info = self.group_chat_1.group_info.click()
        # options = group_info.get_username_options(self.default_username_2).click()
        # options.make_admin_button.click()
        # self.group_chat_2.click_system_back_button()
        # self.group_chat_2.pin_message(self.message_1, action="unpin")
        # if (self.group_chat_1.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed() and
        #         self.group_chat_2.chat_element_by_text(self.message_1).pinned_by_label.is_element_displayed()):
        #     self.errors.append("Message failed to be unpinned by user who granted admin permissions!")

        self.chats[0].just_fyi("Send, pin messages and check they are pinned")
        for message in self.message_2, self.message_3:
            # here group_chat_1 should be changed to group_chat_2 after enabling the previous block
            self.chats[0].send_message(message)
            self.chats[0].pin_message(message, 'pin-to-chat')
            if not (self.chats[0].chat_element_by_text(message).pinned_by_label.is_element_displayed(30) and
                    self.chats[1].chat_element_by_text(message).pinned_by_label.is_element_displayed(30)):
                self.errors.append("%s is not pinned in group chat!" % message)

        self.chats[0].just_fyi("Check that a user can not pin more than 3 messages")
        self.chats[0].send_message(self.message_4)
        self.chats[0].pin_message(self.message_4, 'pin-to-chat')
        self.chats[0].view_pinned_messages_button.click_until_presence_of_element(self.chats[0].pinned_messages_list)
        self.chats[0].pinned_messages_list.message_element_by_text(self.message_2).click_inside_element_by_coordinate()
        self.chats[0].element_by_translation_id('unpin-from-chat').click()
        self.chats[0].chat_element_by_text(self.message_4).click()
        self.chats[0].pin_message(self.message_4, 'pin-to-chat')
        if not (self.chats[0].chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30) and
                self.chats[1].chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30)):
            self.errors.append("Message 4 is not pinned in group chat after unpinning previous one")

        self.chats[0].just_fyi("Check pinned messages count and content")
        for chat_number, group_chat in enumerate([self.chats[0], self.chats[1]]):
            count = group_chat.pinned_messages_count.text
            if count != '3':
                self.errors.append(
                    "Pinned messages count %s doesn't match expected 3 for user %s" % (count, chat_number + 1))
            group_chat.pinned_messages_count.click()
            for message in self.message_1, self.message_3, self.message_4:
                pinned_by = group_chat.pinned_messages_list.get_message_pinned_by_text(message)
                if pinned_by.is_element_displayed():
                    text = pinned_by.text.strip()
                    expected_text = "You" if chat_number == 0 else self.usernames[0]
                    if text != expected_text:
                        self.errors.append(
                            "Pinned by '%s' doesn't match expected '%s' for user %s" % (
                                text, expected_text, chat_number + 1)
                        )
                else:
                    self.errors.append(
                        "Message '%s' is missed on Pinned messages list for user %s" % (message, chat_number + 1)
                    )

        self.errors.verify_no_errors()

    @marks.testrail_id(703495)
    def test_group_chat_mute_chat(self):
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]

        self.homes[1].just_fyi("Member 1 mutes the chat for 1 hour")
        self.homes[1].mute_chat_long_press(self.chat_name, "mute-for-1-hour")
        device_time = self.homes[1].driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_time = current_time + datetime.timedelta(minutes=60)
        expected_text = "Muted until %s %s" % (
            expected_time.strftime('%H:%M'), "today" if current_time.hour < 23 else "tomorrow")
        chat = self.homes[1].get_chat(self.chat_name)
        chat.long_press_element()
        if self.homes[1].mute_chat_button.text != transl["unmute-chat"]:
            pytest.fail("Chat is not muted")
        if not self.homes[1].element_by_text(expected_text).is_element_displayed():
            self.errors.append("Text '%s' is not shown for muted chat" % expected_text)
        self.homes[1].click_system_back_button()
        try:
            initial_counter = int(self.homes[1].chats_tab.counter.text)
        except NoSuchElementException:
            initial_counter = 0

        self.homes[0].just_fyi("Admin sends a message")
        muted_message = "Text message in the muted chat"
        self.homes[0].get_chat(self.chat_name).click()
        self.chats[0].send_message(muted_message)

        self.homes[1].just_fyi("Member 1 checks that chat is muted and message is received")
        if chat.new_messages_grey_dot.is_element_displayed(30):
            self.errors.append("New messages grey dot near chat name is shown after mute")
        try:
            after_mute_counter = int(self.homes[1].chats_tab.counter.text)
        except NoSuchElementException:
            after_mute_counter = 0
        if after_mute_counter > initial_counter:
            self.errors.append("New messages counter near chats tab button is %s after mute, but should be %s" % (
                after_mute_counter, initial_counter))
        if not chat.chat_preview.text.startswith("%s: %s" % (self.usernames[0], muted_message)):
            self.errors.append("Message text '%s' is not shown in chat preview after mute" % muted_message)
        chat.click()
        if not self.chats[1].chat_element_by_text(muted_message).is_element_displayed(30):
            self.errors.append(
                "Message '%s' is not shown in chat for %s after mute" % (muted_message, self.usernames[1]))
        self.chats[1].navigate_back_to_home_view()

        self.chats[1].just_fyi("Change device time so chat will be unmuted by timer")
        unmute_time = current_time + datetime.timedelta(minutes=61)
        self.homes[1].driver.execute_script("mobile: shell",
                                            {"command": "su root date %s" % unmute_time.strftime("%m%d%H%M%Y.%S")}
                                            )
        chat.long_press_element()
        if self.homes[1].element_starts_with_text("Muted until").is_element_displayed():
            self.errors.append("Chat is still muted after timeout")
            self.errors.verify_no_errors()
        self.homes[1].click_system_back_button()

        unmuted_message = "Chat is unmuted now"
        self.homes[2].just_fyi("Member 2 sends a message")
        self.homes[2].get_chat(self.chat_name).click()
        try:
            initial_counter = int(self.homes[1].chats_tab.counter.text)
        except NoSuchElementException:
            initial_counter = 0
        self.chats[2].send_message(unmuted_message)
        self.homes[1].just_fyi("Member 1 checks that chat is unmuted and message is received")
        if not chat.new_messages_grey_dot.is_element_displayed(30):
            self.errors.append("New messages counter near chat name is not shown after unmute")
        try:
            after_mute_counter = int(self.homes[1].chats_tab.counter.text)
        except NoSuchElementException:
            after_mute_counter = 0
        if after_mute_counter <= initial_counter:
            self.errors.append("New messages counter near chats tab button is %s after unmute, but should be %s" % (
                after_mute_counter, initial_counter + 1))
        if not chat.chat_preview.text.startswith("%s: %s" % (self.usernames[2], unmuted_message)):
            self.errors.append("Message text '%s' is not shown in chat preview after unmute" % unmuted_message)
        chat.click()
        if not self.chats[1].chat_element_by_text(unmuted_message).is_element_displayed(30):
            self.errors.append(
                "Message '%s' is not shown in chat for %s after unmute" % (self.usernames[1], unmuted_message))

        self.errors.verify_no_errors()
