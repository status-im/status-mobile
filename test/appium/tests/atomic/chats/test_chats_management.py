import time

from tests import marks, camera_access_error_text
from tests.users import basic_user, dummy_user, ens_user_ropsten
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestChatManagement(SingleDeviceTestCase):

    @marks.testrail_id(5426)
    @marks.medium
    def test_clear_history_via_options(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(basic_user['public_key'])

        one_to_one, public, group = basic_user['username'], '#public-clear-options', 'group'
        message = 'test message'
        chat.get_back_to_home_view()

        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()

        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            chat = home.get_chat(chat_name).click()
            chat.just_fyi('Sending messages to %s chat' % chat_name)
            for _ in range(2):
                chat.chat_message_input.send_keys(message)
                chat.send_message_button.click()
            chat.just_fyi('Clear history for %s chat' % chat_name)
            chat.clear_history()
            if chat.element_by_text(message).is_element_displayed():
                self.errors.append('Messages in %s chat are still shown after clearing history' % chat_name)
            chat.get_back_to_home_view()
        home.relogin()
        for chat_name in one_to_one, public, group:
            if home.element_by_text(message).is_element_displayed():
                self.errors.append(
                    'Messages in %s chat are still shown in Preview after clearing history and relaunch' % chat_name)
            chat = home.get_chat(chat_name).click()
            if chat.element_by_text(message).is_element_displayed():
                self.errors.append(
                    'Messages in %s chat are shown after clearing history and relauch' % chat_name)
            chat.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(5319)
    @marks.medium
    def test_long_press_to_clear_chat_history(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(basic_user['public_key'])

        one_to_one, public, group = basic_user['username'], '#public-clear-long-press', 'group'
        message = 'test message'
        chat.get_back_to_home_view()

        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()

        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            chat = home.get_chat(chat_name).click()
            chat.just_fyi('Sending message to %s chat' % chat_name)
            chat.chat_message_input.send_keys(message)
            chat.send_message_button.click()
            if chat.element_by_text(message).is_element_displayed():
                self.errors.append('Messages in %s chat are still shown after clearing history' % chat_name)
            home = chat.get_back_to_home_view()
            home.just_fyi('Clear history for %s chat' % chat_name)
            home.clear_chat_long_press(chat_name)
        home.relogin()
        for chat_name in one_to_one, public, group:
            if home.element_by_text(message).is_element_displayed():
                self.errors.append(
                    'Messages in %s chat are still shown in Preview after clearing history and relaunch' % chat_name)
            chat = home.get_chat(chat_name).click()
            if chat.element_by_text(message).is_element_displayed():
                self.errors.append(
                    'Messages in %s chat are shown after clearing history and relauch' % chat_name)
            chat.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(5319)
    @marks.critical
    def test_long_press_to_delete_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(basic_user['public_key'])

        one_to_one, public, group = basic_user['username'], '#public-delete-long-press', 'group'
        chat.get_back_to_home_view()

        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()

        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            chat = home.get_chat(chat_name).click()
            chat.just_fyi('Sending message to %s chat' % chat_name)
            chat.chat_message_input.send_keys('test message')
            chat.send_message_button.click()
            chat.get_back_to_home_view()
            chat.just_fyi('Deleting %s chat' % chat_name)
            home.leave_chat_long_press(chat_name) if chat_name == group else home.delete_chat_long_press(chat_name)
        home.relogin()
        for chat_name in one_to_one, public, group:
            if home.get_chat(chat_name).is_element_displayed():
                self.driver.fail('Deleted %s is present after relaunch app' % chat_name)

    @marks.testrail_id(5304)
    @marks.high
    def test_open_chat_by_pasting_public_key(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        public_key = basic_user['public_key']

        chat = home.join_public_chat(home.get_random_chat_name())
        chat.chat_message_input.send_keys(public_key)
        chat.send_message_button.click()
        chat.chat_element_by_text(public_key).long_press_element()
        chat.element_by_text('Copy').click()
        chat.get_back_to_home_view()

        home.plus_button.click()
        contacts_view = home.start_new_chat_button.click()
        contacts_view.public_key_edit_box.paste_text_from_clipboard()
        if contacts_view.public_key_edit_box.text != public_key:
            self.driver.fail('Public key is not pasted from clipboard')
        contacts_view.public_key_edit_box.click()
        contacts_view.confirm_until_presence_of_element(chat.chat_message_input)
        contacts_view.get_back_to_home_view()
        if not home.get_chat(basic_user['username']).is_element_present():
            self.driver.fail("No chat open in home view")

    @marks.testrail_id(5387)
    @marks.high
    def test_delete_chats_via_delete_button(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat = home.add_contact(basic_user['public_key'])

        one_to_one, public, group = basic_user['username'], '#public-delete-options', 'group'
        chat.get_back_to_home_view()

        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()

        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            chat = home.get_chat(chat_name).click()
            chat.just_fyi('Sending message to %s chat' % chat_name)
            chat.chat_message_input.send_keys('test message')
            chat.send_message_button.click()
            chat.just_fyi('Deleting %s chat' % chat_name)
            chat.leave_chat() if chat_name == group else chat.delete_chat()
            chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            if home.get_chat(chat_name).is_element_displayed():
                self.errors.append('Deleted %s chat is shown, but the chat has been deleted' % chat_name)
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.sign_in()
        for chat_name in one_to_one, public, group:
            if home.get_chat(chat_name).is_element_displayed():
                self.errors.append('Deleted %s is shown after re-login, but the chat has been deleted' % chat_name)
        self.errors.verify_no_errors()

    @marks.testrail_id(5464)
    @marks.medium
    def test_incorrect_contact_code_start_new_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        home.plus_button.click()
        contacts_view = home.start_new_chat_button.click()
        contacts_view.public_key_edit_box.set_value(basic_user['public_key'][:-1])
        contacts_view.confirm()
        warning_text = contacts_view.element_by_text('User not found')
        if not warning_text.is_element_displayed():
            self.driver.fail('Error is not shown for invalid public key')

    @marks.testrail_id(5466)
    @marks.medium
    def test_deny_camera_access_scanning_contact_code(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        home.plus_button.click()
        contacts_view = home.start_new_chat_button.click()
        contacts_view.scan_contact_code_button.click()
        contacts_view.deny_button.click()
        contacts_view.element_by_text(camera_access_error_text).wait_for_visibility_of_element(3)
        contacts_view.ok_button.click()
        contacts_view.scan_contact_code_button.click()
        contacts_view.deny_button.wait_for_visibility_of_element(2)

    @marks.testrail_id(5757)
    @marks.medium
    def test_search_chat_on_home(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.just_fyi('Join public chat, start 1-1 with username and with ENS')
        chat_name = home.get_random_chat_name()
        public_chat = home.join_public_chat(chat_name)
        public_chat.get_back_to_home_view()
        for public_key in (basic_user['public_key'], ens_user_ropsten['ens']):
            chat = home.add_contact(public_key)
            chat.get_back_to_home_view()

        search_list = {
            basic_user['username']: basic_user['username'],
            ens_user_ropsten['username']: ens_user_ropsten['ens'],
            chat_name: chat_name
        }

        home.just_fyi('Can search for public chat name, ens name, username')
        home.swipe_down()
        for keyword in search_list:
            home.search_by_keyword(keyword)
            search_results = home.chat_name_text.find_elements()
            if not search_results:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            for element in search_results:
                if search_list[keyword] not in element.text:
                    self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                            (element.text, keyword))
            home.cancel_button.click()

        home.just_fyi('Can search for public chat while offline')
        home.toggle_airplane_mode()
        home.search_chat_input.click()
        home.search_chat_input.send_keys(chat_name)
        search_results = home.chat_name_text.find_elements()
        if not search_results:
            self.errors.append('No search results after searching by %s keyword' % chat_name)
        for element in search_results:
            if search_list[chat_name] not in element.text:
                self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                   (element.text, chat_name))

        self.errors.verify_no_errors()

    @marks.testrail_id(6221)
    @marks.medium
    def test_app_on_background_by_back_button(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        home.click_system_back_button()
        home.driver.press_keycode(187)
        if self.driver.current_activity != '.NexusLauncherActivity':
            self.driver.fail('App is not in background! Current activity is: %s' % self.driver.current_activity)
        home.status_in_background_button.click()
        if not home.plus_button.is_element_displayed():
            self.driver.fail('Chats view was not opened')

    @marks.testrail_id(6213)
    @marks.medium
    def test_unblocked_user_is_not_added_in_contacts(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_view = home.add_contact(basic_user["public_key"], add_in_contacts=False)

        chat_view.just_fyi('Block user not added as contact from chat view')
        chat_view.chat_options.click()
        chat_view.view_profile_button.click()
        chat_view.block_contact()

        chat_view.just_fyi('Unblock user not added as contact from chat view')
        profile = sign_in.profile_button.click()
        profile.contacts_button.click()
        profile.blocked_users_button.click()
        profile.element_by_text(basic_user["username"]).click()
        chat_view.unblock_contact_button.click()

        profile.just_fyi('Navigating to contact list and check that user is not in list')
        profile.back_button.click(2)
        if profile.element_by_text(basic_user["username"]).is_element_displayed():
            self.driver.fail("Unblocked user not added previously in contact list added in contacts!")

    @marks.testrail_id(5496)
    @marks.low
    def test_can_remove_quote_snippet_from_inputs(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        chat_view = home.add_contact(dummy_user["public_key"], add_in_contacts=False)
        message_to_quote_1_to_1 = "This is a message to quote in 1-1"
        message_to_quote_public = "This is a message to quote in public"

        chat_view.just_fyi("Send and quote message in 1-1 chat")
        chat_view.send_message(message_to_quote_1_to_1)
        chat_view.quote_message(message_to_quote_1_to_1)
        chat_view.get_back_to_home_view(times_to_click_on_back_btn=1)

        chat_view.just_fyi("Send and quote message in public chat")
        public_chat_name = home.get_random_chat_name()
        home.join_public_chat(public_chat_name)
        chat_view.send_message(message_to_quote_public)
        chat_view.quote_message(message_to_quote_public)

        chat_view.just_fyi("Clear quotes from both chats")
        chat_view.cancel_reply_button.click()

        if chat_view.cancel_reply_button.is_element_displayed():
            self.errors.append("Message quote kept in public chat input after it's cancelation")
        chat_view.get_back_to_home_view(times_to_click_on_back_btn=1)
        home.get_chat(dummy_user["username"]).click()
        chat_view.cancel_reply_button.click()
        if chat_view.cancel_reply_button.is_element_displayed():
            self.errors.append("Message quote kept in 1-1 chat input after it's cancelation")

        self.errors.verify_no_errors()

    @marks.testrail_id(5498)
    @marks.medium
    def test_share_user_profile_url_public_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        sign_in.just_fyi('Join to one-to-one chat and share link to other user profile via messenger')
        chat_view = home.add_contact(dummy_user["public_key"])
        chat_view.chat_options.click()
        chat_view.view_profile_button.click_until_presence_of_element(chat_view.remove_from_contacts)
        chat_view.profile_details.click()
        chat_view.share_button.click()
        chat_view.share_via_messenger()
        if not chat_view.element_by_text_part('https://join.status.im/u/%s' % dummy_user["public_key"]).is_element_present():
             self.errors.append("Can't share public key of contact")
        for _ in range(2):
             chat_view.click_system_back_button()

        sign_in.just_fyi('Join to public chat and share link to it via messenger')
        chat_view.get_back_to_home_view()
        public_chat_name = 'pubchat'
        public_chat = home.join_public_chat(public_chat_name)
        public_chat.chat_options.click()
        public_chat.share_chat_button.click()
        public_chat.share_via_messenger()
        if not chat_view.element_by_text_part('https://join.status.im/%s' % public_chat_name).is_element_present():
             self.errors.append("Can't share link to public chat")
        for _ in range(2):
             chat_view.click_system_back_button()
        chat_view.get_back_to_home_view()

        sign_in.just_fyi('Open URL and share link to it via messenger')
        daap_view = home.dapp_tab_button.click()
        browsing_view = daap_view.open_url('dap.ps')
        browsing_view.share_url_button.click()
        browsing_view.share_via_messenger()
        expeceted_text_1 = 'https://join.status.im/b/https://dap.ps'
        expeceted_text_2 = 'https://join.status.im/b/http://dap.ps'

        if not (chat_view.element_by_text_part(expeceted_text_1).is_element_present() or
                chat_view.element_by_text_part(expeceted_text_2).is_element_present()):
            self.errors.append("Can't share link to URL")

        self.errors.verify_no_errors()


@marks.chat
class TestChatManagementMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5332)
    @marks.critical
    def test_add_and_remove_contact_from_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        chat_name = 'testaddcontact'

        device_1.just_fyi('join same public chat')
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        message = 'test message' + str(round(time.time()))

        chat_2.chat_message_input.send_keys(message)
        chat_2.send_message_button.click()
        chat_2.driver.quit()

        device_1.just_fyi('Tap on userpic and check redirect to user profile')
        chat_element = chat_1.chat_element_by_text(message)
        chat_element.find_element()
        username = chat_element.username.text
        chat_element.member_photo.click()
        for element in [chat_1.contact_profile_picture,
                        chat_1.element_by_text(username, 'text'),
                        chat_1.add_to_contacts,
                        chat_1.profile_send_message,
                        chat_1.profile_address_text]:
            if not element.scroll_to_element():
                self.errors.append('%s is not visible' % element.name)

        device_1.just_fyi('Add user to contacts, check contact list in Profile')
        chat_1.add_to_contacts.click()
        if not chat_1.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts'")
        chat_1.get_back_to_home_view()
        profile_1 = chat_1.profile_button.click()
        userprofile = profile_1.open_contact_from_profile(username)
        if not userprofile.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts'")
        profile_1.get_back_to_home_view()

        device_1.just_fyi('Check that user is added to contacts below "Start new chat" and you redirected to 1-1 on tap')
        home_1.plus_button.click()
        home_1.start_new_chat_button.click()
        if not home_1.element_by_text(username).is_element_displayed():
            home_1.driver.fail('List of contacts below "Start new chat" does not contain added user')
        home_1.element_by_text(username).click()
        if not chat_1.chat_message_input.is_element_displayed():
            home_1.driver.fail('No redirect to 1-1 chat if tap on Contact below "Start new chat"')
        if chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is shown in 1-1 after adding user to contacts from profile')

        device_1.just_fyi('Remove user from contacts')
        chat_1.profile_button.click()
        userprofile = profile_1.open_contact_from_profile(username)
        userprofile.remove_from_contacts.click()
        if userprofile.remove_from_contacts.is_element_displayed():
            self.errors.append("'Remove from contacts' is not changed to 'Add to contacts'")

        device_1.just_fyi('Check that user is removed from contact list in profile')
        userprofile.back_button.click()
        if profile_1.element_by_text(username).is_element_displayed():
            self.errors.append('List of contacts in profile contains removed user')
        profile_1.home_button.click()
        if not chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is not shown in 1-1 after removing user from contacts')
        home_1.get_back_to_home_view()
        home_1.plus_button.click()
        home_1.start_new_chat_button.click()
        if home_1.get_username_below_start_new_chat_button(username).is_element_displayed():
            self.errors.append('List of contacts below "Start new chat" contains removed user')
        self.errors.verify_no_errors()

    @marks.testrail_id(5786)
    @marks.critical
    def test_block_user_from_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_before_block_1 = "Before block from %s" % device_1.driver.number
        message_before_block_2 = "Before block from %s" % device_2.driver.number
        message_after_block_2 = "After block from %s" % device_2.driver.number
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        for chat in chat_public_1, chat_public_2:
            chat.chat_message_input.send_keys("Before block from %s" % chat.driver.number)
            chat.send_message_button.click()

        device_1.just_fyi('block user')
        chat_element = chat_public_1.chat_element_by_text(message_before_block_2)
        chat_element.find_element()
        chat_element.member_photo.click()
        chat_public_1.block_contact()

        device_1.just_fyi('messages from blocked user are hidden in public chat and close app')
        if chat_public_1.chat_element_by_text(message_before_block_2).is_element_displayed():
            self.errors.append(
                "Messages from blocked user %s are not cleared in public chat '%s'" % (
                    device_2.driver.number, chat_name))
        self.drivers[0].close_app()

        device_2.just_fyi('send message to public chat while device 1 is offline')
        chat_public_2.chat_message_input.send_keys(message_after_block_2)
        chat_public_2.send_message_button.click()

        device_1.just_fyi('check that new messages from blocked user are not delivered')
        self.drivers[0].launch_app()
        device_1.accept_agreements()
        device_1.sign_in()
        home_1.join_public_chat(chat_name)
        for message in message_before_block_2, message_after_block_2:
            if chat_public_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    "'%s' from blocked user %s are shown in public chat" % (message, device_2.driver.number))

    @marks.testrail_id(5763)
    @marks.medium
    def test_block_user_from_one_to_one_header(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_before_block_1 = "Before block from %s" % device_1.driver.number
        message_before_block_2 = "Before block from %s" % device_2.driver.number
        message_after_block_2 = "After block from %s" % device_2.driver.number
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = home_1.profile_button.click()
        device_2_public_key = home_2.get_public_key_and_username()
        home_2.get_back_to_home_view()
        default_username_1 = profile_1.default_username_text.text
        profile_1.get_back_to_home_view()

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

        device_1.just_fyi('no 1-1, messages from blocked user are hidden in public chat')
        if home_1.get_chat(basic_user['username']).is_element_displayed():
            home_1.driver.fail("Chat with blocked user '%s' is not deleted" % device_2.driver.number)
        public_chat_after_block = home_1.join_public_chat(chat_name)
        if public_chat_after_block.chat_element_by_text(message_before_block_2).is_element_displayed():
            self.errors.append(
                "Messages from blocked user '%s' are not cleared in public chat '%s'" % (device_2.driver.number,
                                                                                         chat_name))

        device_2.just_fyi('send messages to 1-1 and public chat')
        for _ in range(2):
            chat_2.chat_message_input.send_keys(message_after_block_2)
            chat_2.send_message_button.click()
        chat_2.get_back_to_home_view()
        home_2.join_public_chat(chat_name)
        chat_public_2 = home_2.get_chat_view()

        for _ in range(2):
            chat_public_2.chat_message_input.send_keys(message_after_block_2)
            chat_public_2.send_message_button.click()

        device_1.just_fyi("check that new messages didn't arrived from blocked user")
        if public_chat_after_block.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received" % device_2.driver.number)
        public_chat_after_block.get_back_to_home_view()
        if home_1.get_chat(basic_user['username']).is_element_displayed():
            device_2.driver.fail("Chat with blocked user is reappeared after receiving new messages")
        self.drivers[0].close_app()

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
        self.drivers[0].launch_app()
        device_1.accept_agreements()
        device_1.sign_in()
        if home_1.get_chat(basic_user['username']).is_element_displayed():
            self.errors.append("Chat with blocked user is reappeared after fetching new messages from offline")
        home_1.join_public_chat(chat_name)
        home_1.get_chat_view()
        if chat_public_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append(
                "Message from blocked user '%s' is received after fetching new messages from offline"
                % device_2.driver.number)

        self.errors.verify_no_errors()

    @marks.testrail_id(6233)
    @marks.medium
    def test_reply_to_message_in_chats(self):
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

        device_1.just_fyi("Sender adds receiver and quotes own message and sends")
        device_1_chat = home_1.add_contact(device_2_public_key)
        device_1_chat.send_message(message_from_sender)
        device_1_chat.quote_message(message_from_sender)
        if device_1_chat.quote_username_in_message_input.text != "↪ You":
            self.errors.append("'You' is not displayed in reply quote snippet replying to own message")
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
        if chat_public_2.quote_username_in_message_input.text != ("↪ " + device_1_username):
            self.errors.append(" %s is not displayed in reply quote snippet replying to own message " % device_1_username)

        device_1.just_fyi('Message receiver verifies reply is present in received message')
        chat_public_2.send_message(message_from_receiver)
        public_replied_message = chat_public_1.chat_element_by_text(message_from_receiver)
        if public_replied_message.replied_message_text != message_from_sender:
            self.errors.append("Reply is not present in message received in public chat")

        self.errors.verify_no_errors()

    @marks.testrail_id(6267)
    @marks.medium
    def test_open_user_profile_long_press_on_message(self):
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

        device_1.just_fyi("1-1 chat: sender adds receiver and send a message")
        device_1_chat = home_1.add_contact(device_2_public_key)
        device_1_chat.send_message(message_from_sender)
        device_1_chat.chat_element_by_text(message_from_sender).long_press_element()
        if device_1_chat.view_profile_button.is_element_displayed():
            self.errors.append('1-1 chat: "view profile" is shown on long tap on sent message')
        device_1_chat.get_back_to_home_view()

        device_2.just_fyi("1-1 chat: receiver verifies that can open sender profile on long tap on message")
        home_2.home_button.click()
        device_2_chat_item = home_2.get_chat(device_1_username)
        device_2_chat_item.wait_for_visibility_of_element(20)
        device_2_chat = device_2_chat_item.click()
        device_2_chat.view_profile_long_press(message_from_sender)
        if not device_2_chat.profile_add_to_contacts.is_element_displayed():
            self.errors.append('1-1 chat: another user profile is not opened on long tap on received message')
        device_2_chat.get_back_to_home_view()

        device_1.just_fyi('Public chat: send message and verify that user profile can be opened on long press on message')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        chat_public_2.send_message(message_from_receiver)
        chat_public_2.chat_element_by_text(message_from_receiver).long_press_element()
        if chat_public_2.view_profile_button.is_element_displayed():
            self.errors.append('Public chat: "view profile" is shown on long tap on sent message')
        chat_public_1.view_profile_long_press(message_from_receiver)
        if not chat_public_1.remove_from_contacts.is_element_displayed():
            self.errors.append('Public chat: another user profile is not opened on long tap on received message')

        self.errors.verify_no_errors()