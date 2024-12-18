import datetime

import pytest
from _pytest.outcomes import Failed
from appium.webdriver.connectiontype import ConnectionType
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, run_in_parallel, transl
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from views.chat_view import ChatView
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_one_3")
@marks.nightly
class TestGroupChatMultipleDeviceMergedNewUI(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.message_before_adding = 'message before adding new user'
        self.message_to_admin = 'Hey, admin!'
        self.public_keys, self.usernames, self.chats = {}, {}, {}
        self.sign_in_views = [SignInView(self.drivers[key]) for key in self.drivers]
        self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.sign_in_views[0].create_user, {'enable_notifications': True}),
                    (self.sign_in_views[1].create_user, {'enable_notifications': True}),
                    (self.sign_in_views[2].create_user, {'enable_notifications': True})
                )
            )
        )
        self.homes = [sign_in.get_home_view() for sign_in in self.sign_in_views]
        self.usernames = self.loop.run_until_complete(
            run_in_parallel(
                (
                    (self.homes[0].get_username,),
                    (self.homes[1].get_username,),
                    (self.homes[2].get_username,)
                )
            )
        )
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

        def _check_reactions_count(chat_view_index):
            self.chats[chat_view_index].just_fyi("Checking reactions count for each group member and admin")
            chat_element = self.chats[chat_view_index].chat_element_by_text(message)
            chat_element.emojis_below_message(emoji="thumbs-up").wait_for_element_text(2)
            chat_element.emojis_below_message(emoji="love").wait_for_element_text(1)
            chat_element.emojis_below_message(emoji="laugh").wait_for_element_text(1)

        self.loop.run_until_complete(run_in_parallel((
            (_check_reactions_count, {'chat_view_index': 0}),
            (_check_reactions_count, {'chat_view_index': 1}),
            (_check_reactions_count, {'chat_view_index': 2})
        )))

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
            username_shown = self.chats[0].get_profile_view().contact_name_text.text
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

        def _check_reactions_count_after_change(chat_view_index):
            self.chats[chat_view_index].just_fyi(
                "Checking reactions count for each group member and admin after they were changed")
            chat_element = self.chats[chat_view_index].chat_element_by_text(message)
            try:
                chat_element.emojis_below_message(emoji="thumbs-up").wait_for_element_text(1)
                chat_element.emojis_below_message(emoji="love").wait_for_element_text(1)
                chat_element.emojis_below_message(emoji="sad").wait_for_element_text(2)
            except (Failed, NoSuchElementException):
                self.errors.append(
                    "Incorrect reactions count for %s after changing the reactions" % self.usernames[chat_view_index])

        self.loop.run_until_complete(run_in_parallel((
            (_check_reactions_count_after_change, {'chat_view_index': 0}),
            (_check_reactions_count_after_change, {'chat_view_index': 1}),
            (_check_reactions_count_after_change, {'chat_view_index': 2})
        )))

        self.chats[0].just_fyi("Admin relogins")
        self.chats[0].reopen_app(user_name=self.usernames[0])
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
        chat_element = self.chats[0].chat_element_by_text(image_description)
        chat_element.wait_for_visibility_of_element(60)
        if not chat_element.image_in_message.is_element_image_similar_to_template('saucelabs_sauce_group_chat.png'):
            self.errors.append("Not expected image is shown to the admin.")

        self.chats[2].just_fyi("Member_2 checks image message")
        chat_element = self.chats[2].chat_element_by_text(image_description)
        chat_element.wait_for_visibility_of_element(60)
        if not chat_element.image_in_message.is_element_image_similar_to_template('saucelabs_sauce_group_chat.png'):
            self.errors.append("Not expected image is shown to the member_2.")

        self.chats[0].just_fyi("Admin opens the image and shares it")
        self.chats[0].chat_element_by_text(image_description).image_in_message.click()
        self.chats[0].share_image_icon_button.click()
        self.chats[0].element_starts_with_text("Drive").click()
        try:
            self.chats[0].wait_for_current_package_to_be('com.google.android.apps.docs')
        except TimeoutException:
            self.errors.append("Admin can't share an image via Gmail.")
        self.chats[0].navigate_back_to_chat_view()

        self.chats[1].navigate_back_to_home_view()
        app_package = self.drivers[2].current_package

        self.chats[2].just_fyi("Member_2 opens the image and saves it")
        self.chats[2].chat_element_by_text(image_description).image_in_message.click()
        self.chats[2].view_image_options_button.click()
        self.chats[2].save_image_icon_button.click()
        toast_element = self.chats[2].toast_content_element
        try:
            toast_element_text = toast_element.wait_for_visibility_of_element().text
            if toast_element_text != self.chats[2].get_translation_by_key("photo-saved"):
                self.errors.append(
                    "Shown message '%s' doesn't match expected '%s' after saving an image for member_2." % (
                        toast_element_text, self.chats[2].get_translation_by_key("photo-saved")))
        except TimeoutException:
            self.errors.append("Message about saving a photo is not shown for member_2.")
        self.chats[2].navigate_back_to_chat_view()

        # workaround for app closed after navigating back from gallery
        if not self.chats[2].chat_message_input.is_element_displayed():
            self.drivers[2].activate_app(app_package)
            SignInView(self.drivers[2]).sign_in(user_name=self.usernames[2])
            self.homes[2].chats_tab.click()
            self.homes[2].get_chat(self.chat_name).click()

        self.chats[2].just_fyi("Member_2 checks that image was saved in gallery")
        self.chats[2].show_images_button.click()
        self.chats[2].allow_all_button.click_if_shown()
        if not self.chats[2].get_image_by_index(0).is_element_image_similar_to_template("saucelabs_sauce_gallery.png"):
            self.errors.append("Image is not saved to gallery for member_2.")
        self.chats[2].navigate_back_to_home_view()

        # workaround for app closed after navigating back from gallery
        if not self.chats[2].chats_tab.is_element_displayed():
            self.drivers[2].activate_app(app_package)
            SignInView(self.drivers[2]).sign_in(user_name=self.usernames[2])
        self.homes[2].chats_tab.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(702808)
    def test_group_chat_offline_pn(self):
        def _proceed_to_chat(index):
            self.homes[index].navigate_back_to_home_view()
            self.homes[index].chats_tab.click()
            self.homes[index].groups_tab.click()
            self.homes[index].get_chat(self.chat_name).click()

        self.loop.run_until_complete(run_in_parallel((
            (_proceed_to_chat, {'index': 1}),
            (_proceed_to_chat, {'index': 2})
        )))

        message_1, message_2 = 'message from old member', 'message from new member'

        self.homes[0].just_fyi("Put admin device to offline and send messages from members")
        self.homes[0].navigate_back_to_home_view()
        app_package = self.drivers[0].current_package
        self.homes[0].driver.set_network_connection(ConnectionType.AIRPLANE_MODE)
        self.chats[1].send_message(message_1)
        self.chats[2].send_message(message_2)

        self.homes[0].just_fyi("Put admin device to online and check that messages and PNs will be fetched")
        self.homes[0].driver.set_network_connection(ConnectionType.ALL_NETWORK_ON)
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
            SignInView(self.drivers[0]).sign_in(user_name=self.usernames[0])
        self.homes[0].chats_tab.click()
        self.homes[0].get_chat(self.chat_name).click()

        def _check_messages(index):
            self.chats[index].just_fyi("Check that messages are shown for user %s" % self.usernames[index])
            for message_text in (message_1, message_2):
                if not self.chats[index].chat_element_by_text(message_text).is_element_displayed(30):
                    self.errors.append('%s if not shown for device %s' % (message_text, index))

        self.loop.run_until_complete(run_in_parallel((
            (_check_messages, {'index': 0}),
            (_check_messages, {'index': 1}),
            (_check_messages, {'index': 2})
        )))

        self.errors.verify_no_errors()

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
        unpin_element = self.chats[0].element_by_translation_id('unpin-from-chat')
        self.chats[0].pinned_messages_list.message_element_by_text(self.message_2).long_press_element(
            element_to_release_on=unpin_element)
        unpin_element.click_until_absense_of_element(desired_element=unpin_element)
        self.chats[0].chat_element_by_text(self.message_4).click()
        self.chats[0].pin_message(self.message_4, 'pin-to-chat')
        if not (self.chats[0].chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30) and
                self.chats[1].chat_element_by_text(self.message_4).pinned_by_label.is_element_displayed(30)):
            self.errors.append("Message 4 is not pinned in group chat after unpinning previous one")

        def _check_pinned_messages(index):
            self.chats[index].just_fyi("Check pinned messages count and content for user %s" % self.usernames[index])
            count = self.chats[index].pinned_messages_count.text
            if count != '3':
                self.errors.append(
                    "Pinned messages count %s doesn't match expected 3 for user %s" % (count, self.usernames[index]))
            self.chats[index].pinned_messages_count.click()
            for message_text in self.message_1, self.message_3, self.message_4:
                pinned_by = self.chats[index].pinned_messages_list.get_message_pinned_by_text(message_text)
                if pinned_by.is_element_displayed():
                    text = pinned_by.text.strip()
                    expected_text = "You" if index == 0 else self.usernames[0]
                    if text != expected_text:
                        self.errors.append(
                            "Pinned by '%s' doesn't match expected '%s' for user %s" % (
                                text, expected_text, self.usernames[index])
                        )
                else:
                    self.errors.append(
                        "Message '%s' is missed on Pinned messages list for user %s" % (message, self.usernames[index])
                    )

        self.loop.run_until_complete(run_in_parallel((
            (_check_pinned_messages, {'index': 0}),
            (_check_pinned_messages, {'index': 1})
        )))

        self.errors.verify_no_errors()

    @marks.testrail_id(703495)
    def test_group_chat_mute_chat(self):
        [self.homes[i].navigate_back_to_home_view() for i in range(3)]

        self.homes[1].just_fyi("Member 1 mutes the chat for 8 hours")
        self.homes[1].mute_chat_long_press(self.chat_name, "mute-for-8-hours")
        device_time = self.homes[1].driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_times = [current_time + datetime.timedelta(minutes=i) for i in range(479, 482)]
        expected_texts = [
            "Muted until %s %s" % (exp_time.strftime('%H:%M'), "today" if current_time.hour < 23 else "tomorrow") for
            exp_time in expected_times]
        chat = self.homes[1].get_chat(self.chat_name)
        chat.long_press_element()
        if self.homes[1].mute_chat_button.text != transl["unmute-chat"]:
            pytest.fail("Chat is not muted")
        current_text = self.homes[1].mute_chat_button.unmute_caption_text
        if current_text not in expected_texts:
            self.errors.append("Text '%s' is not shown for muted chat" % expected_texts[1])
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

        self.chats[1].just_fyi("Member 1 unmutes the chat")
        chat.long_press_element()
        self.homes[1].mute_chat_button.click()
        chat.long_press_element()
        if self.homes[1].element_starts_with_text("Muted until").is_element_displayed():
            self.errors.append("Chat is still muted after being unmuted")
            self.errors.verify_no_errors()
        if self.homes[1].mute_chat_button.is_element_displayed():
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
        if after_mute_counter != initial_counter:
            self.errors.append("New messages counter near chats tab button is %s after unmute, but should be %s" % (
                after_mute_counter, initial_counter + 1))
        if not chat.chat_preview.text.startswith("%s: %s" % (self.usernames[2], unmuted_message)):
            self.errors.append("Message text '%s' is not shown in chat preview after unmute" % unmuted_message)
        chat.click()
        if not self.chats[1].chat_element_by_text(unmuted_message).is_element_displayed(30):
            self.errors.append(
                "Message '%s' is not shown in chat for %s after unmute" % (self.usernames[1], unmuted_message))

        self.errors.verify_no_errors()
