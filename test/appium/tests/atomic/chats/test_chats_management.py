import time
import random
import emoji

from tests import marks
from tests.users import basic_user, dummy_user, ens_user_ropsten, ens_user, ens_user_message_sender
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView
from views.chat_view import ChatView


class TestChatManagement(SingleDeviceTestCase):
    @marks.testrail_id(5319)
    @marks.critical
    def test_long_press_to_delete_chat(self):
        home = SignInView(self.driver).create_user()
        messages = [home.get_random_message() for _ in range(3)]

        home.just_fyi("Creating 3 types of chats")
        chat = home.add_contact(basic_user['public_key'])
        one_to_one, public, group = basic_user['username'], '#public-delete-long-press', 'group'
        chat.get_back_to_home_view()
        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()
        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()

        home.just_fyi("Deleting all types of chats and check that they will not reappear after relogin")
        i = 0
        for chat_name in one_to_one, public, group:
            chat = home.get_chat(chat_name).click()
            chat.send_message(messages[i])
            chat.get_back_to_home_view()
            home.leave_chat_long_press(chat_name) if chat_name == group else home.delete_chat_long_press(chat_name)
            i+=1
        home.relogin()
        for chat_name in one_to_one, public, group:
            if home.get_chat_from_home_view(chat_name).is_element_displayed():
                self.driver.fail('Deleted %s is present after relaunch app' % chat_name)

    @marks.testrail_id(5387)
    @marks.high
    def test_delete_chats_via_delete_button_rejoin(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        messages = [home.get_random_message() for _ in range(3)]

        home.just_fyi("Creating 3 types of chats")
        chat = home.add_contact(basic_user['public_key'])
        one_to_one, public, group = basic_user['username'], '#public-delete-long-press', 'group'
        chat.get_back_to_home_view()
        home.create_group_chat([basic_user['username']], group)
        chat.get_back_to_home_view()
        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()
        home.join_public_chat(public[1:])
        chat.get_back_to_home_view()


        home.just_fyi("Deleting 3 chats via delete button and check they will not reappear after relaunching app")
        i = 0
        for chat_name in one_to_one, public, group:
            message = messages[i]
            chat = home.get_chat(chat_name).click()
            chat.send_message(message)
            chat.leave_chat() if chat_name == group else chat.delete_chat()
            i+=1
            chat.get_back_to_home_view()
        for chat_name in one_to_one, public, group:
            if home.get_chat_from_home_view(chat_name).is_element_displayed():
                self.errors.append('Deleted %s chat is shown, but the chat has been deleted' % chat_name)
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.sign_in()
        for chat_name in one_to_one, public, group:
            if home.get_chat_from_home_view(chat_name).is_element_displayed():
                self.errors.append('Deleted %s is shown after re-login, but the chat has been deleted' % chat_name)

        # TODO: blocked due to #11683 - enable after fix
        # sign_in.just_fyi('Rejoin public chat and check that messages are fetched again')
        # public_chat = home.join_public_chat(public[1:])
        # if not public_chat.chat_element_by_text(messages[1]).is_element_displayed(20):
        #     self.errors.append('Messages are not fetched when rejoining public chat after deleting')

        self.errors.verify_no_errors()

    @marks.testrail_id(5304)
    @marks.high
    def test_open_chat_by_pasting_chat_key_check_invalid_chat_key_cases(self):
        home = SignInView(self.driver).create_user()
        public_key = basic_user['public_key']
        home.plus_button.click()
        chat = home.start_new_chat_button.click()

        home.just_fyi("Check that invalid public key and ENS can not be resolved")
        for invalid_chat_key in (basic_user['public_key'][:-1], ens_user_ropsten['ens'][:-2]):
            chat.public_key_edit_box.clear()
            chat.public_key_edit_box.set_value(invalid_chat_key)
            chat.confirm()
            if not home.element_by_translation_id("profile-not-found").is_element_displayed():
                self.errors.append('Error is not shown for invalid public key')

        home.just_fyi("Check that valid ENS is resolved")
        chat.public_key_edit_box.clear()
        chat.public_key_edit_box.set_value(ens_user_ropsten['ens'])
        resolved_ens = '%s.stateofus.eth' % ens_user_ropsten['ens']
        if not chat.element_by_text(resolved_ens).is_element_displayed(10):
            self.errors.append('ENS name is not resolved after pasting chat key')
        home.close_button.click()

        home.just_fyi("Check that can paste public key from keyboard and start chat")
        home.join_public_chat(home.get_random_chat_name())
        chat.send_message(public_key)
        chat.copy_message_text(public_key)
        chat.back_button.click()
        home.plus_button.click()
        home.start_new_chat_button.click()
        chat.public_key_edit_box.paste_text_from_clipboard()
        if chat.public_key_edit_box.text != public_key:
            self.errors.append('Public key is not pasted from clipboard')
        if not chat.element_by_text(basic_user['username']).is_element_displayed():
            self.errors.append('3 random-name is not resolved after pasting chat key')
        chat.public_key_edit_box.click()
        chat.confirm_until_presence_of_element(chat.chat_message_input)
        chat.get_back_to_home_view()
        if not home.get_chat(basic_user['username']).is_element_present():
            self.errors.append("No chat open in home view")

        self.errors.verify_no_errors()

    @marks.testrail_id(5426)
    @marks.medium
    def test_public_clear_history_via_options_and_long_press(self):
        home = SignInView(self.driver).create_user()

        home.just_fyi("Creating 3 types of chats")
        public_options, public_long_press = '#public-clear-options', '#public-long-options'
        message = 'test message'
        for pub_chat in [public_options[1:], public_long_press[1:]]:
            chat = home.join_public_chat(pub_chat)
            [chat.send_message(message) for _ in range(2)]
            home.home_button.double_click()

        home.just_fyi('Clearing history via long press')
        home.clear_chat_long_press(public_long_press)

        home.just_fyi('Clearing history via options')
        chat = home.get_chat(public_options).click()
        chat.clear_history()
        if chat.element_by_text(message).is_element_displayed():
            self.errors.append('Messages in %s chat are still shown after clearing history via options' % public_options)

        home.just_fyi("Recheck that history won't reappear after relogin")
        home.relogin()
        for chat_name in public_options, public_long_press:
            chat = home.get_chat(chat_name).click()
            if chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('Messages in %s chat are shown after clearing history and relaunch' % chat_name)
            chat.home_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(6320)
    @marks.medium
    def test_can_start_chat_from_suggestions_using_search_chat(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        profile.switch_network()
        chat_view = ChatView(self.driver)
        ens_name_status, ens_name_another_domain, public_chat_name = ens_user_ropsten['ens'], \
                                                                     ens_user['ens_another_domain'], 'some-pub-chat'
        search_list = {
            ens_name_status: {
                'home': {
                    'Start a new private chat',
                    'Join a public chat',
                    '%s.stateofus.eth' % ens_name_status,
                    '#%s' % ens_name_status
                },
                'navigate_to': '%s.stateofus.eth' % ens_name_status,
                'chat_view': {
                    chat_view.add_to_contacts,
                    chat_view.element_by_text('@%s' % ens_name_status),
                    chat_view.chat_message_input
                }
            },
            ens_name_another_domain: {
                'home': {
                    'Start a new private chat',
                },
                'home_not_shown': 'Join a public chat',
                'navigate_to': 'Start a new private chat',
                'chat_view': {
                    chat_view.add_to_contacts,
                    chat_view.element_by_text('@%s' % ens_name_another_domain),
                    chat_view.chat_message_input
                },
            },
            public_chat_name: {
                'home': {
                    'Join a public chat',
                },
                'home_not_shown': 'Start a new private chat',
                'navigate_to': '#%s' % public_chat_name,
                'chat_view': {
                    chat_view.element_by_text('#%s' % public_chat_name),
                    chat_view.chat_message_input
                },
            },

        }

        home.just_fyi('Join public chat to have search input on home view')
        chat_name = home.get_random_chat_name()
        public_chat = home.join_public_chat(chat_name)
        public_chat.get_back_to_home_view()
        home.swipe_down()

        for keyword in search_list:
            home.just_fyi('Can start chat from searching for %s' % keyword)
            home.search_by_keyword(keyword)
            if not home.element_by_text_part('No search results. Do you mean').is_element_displayed():
                self.errors.append('"No search results" is not shown')
            if 'home_not_shown' in search_list[keyword]:
                if home.element_by_text(search_list[keyword]['home_not_shown']).is_element_displayed():
                    self.errors.append('%s is shown on home view while searching for %s' % (
                    search_list[keyword]['home_not_shown'], keyword))
            for text in search_list[keyword]['home']:
                if not home.element_by_text(text).is_element_displayed():
                    self.errors.append('%s is not shown on home view while searching for %s' % (text, keyword))
            home.element_by_text(search_list[keyword]['navigate_to']).click()
            for element in search_list[keyword]['chat_view']:
                if not element.is_element_displayed():
                    self.errors.append(
                        'Requested %s element is not shown on chat view after navigating from suggestion '
                        'for %s' % (element.name, keyword))
            home.back_button.click()

        home.just_fyi('No suggestion at attempt to search for invalid data')
        invalid_data = ['   ', 'ab;', '.6', '@ana']
        for text in invalid_data:
            home.search_by_keyword(text)
            if home.element_by_text_part('No search results. Do you mean').is_element_displayed():
                self.errors.append('"No search results" is shown when searching for invalid value %s' % text)
            home.cancel_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(6319)
    @marks.medium
    def test_deny_access_camera_and_gallery(self):
        home = SignInView(self.driver).create_user()
        general_camera_error = home.element_by_translation_id("camera-access-error")

        home.just_fyi("Denying access to camera in universal qr code scanner")
        home.plus_button.click()
        home.universal_qr_scanner_button.click()
        home.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        home.ok_button.click()
        home.get_back_to_home_view()

        home.just_fyi("Denying access to camera in scan chat key view")
        home.plus_button.click()
        chat = home.start_new_chat_button.click()
        chat.scan_contact_code_button.click()
        chat.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        chat.ok_button.click()
        home.get_back_to_home_view()

        home.just_fyi("Denying access to gallery at attempt to send image")
        home.add_contact(basic_user['public_key'])
        chat.show_images_button.click()
        chat.deny_button.click()
        chat.element_by_translation_id("external-storage-denied").wait_for_visibility_of_element(3)
        chat.ok_button.click()

        home.just_fyi("Denying access to audio at attempt to record audio")
        chat.audio_message_button.click()
        chat.deny_button.click()
        chat.element_by_translation_id("audio-recorder-permissions-error").wait_for_visibility_of_element(3)
        chat.ok_button.click()
        home.get_back_to_home_view()

        home.just_fyi("Denying access to camera in wallet view")
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.scan_qr_button.click()
        wallet.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        wallet.ok_button.click()

        home.just_fyi("Denying access to camera in send transaction > scan address view")
        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.chose_recipient_button.scroll_and_click()
        send_transaction.scan_qr_code_button.click()
        send_transaction.deny_button.click()
        general_camera_error.wait_for_visibility_of_element(3)
        send_transaction.ok_button.click()
        wallet.close_button.click()
        wallet.close_send_transaction_view_button.click()

        home.just_fyi("Allow access to camera in universal qr code scanner and check it in other views")
        wallet.home_button.click()
        home.plus_button.click()
        home.universal_qr_scanner_button.click()
        home.allow_button.click()
        if not home.element_by_text('Scan QR code').is_element_displayed():
            self.errors.append('Scan QR code is not opened after denying and allowing permission to the camera')
        home.cancel_button.click()
        wallet = home.wallet_button.click()
        wallet.scan_qr_button.click()
        if not home.element_by_text('Scan QR code').is_element_displayed():
            self.errors.append(
                'Scan QR code is not opened after allowing permission to the camera from univesal QR code'
                ' scanner view')
        wallet.cancel_button.click()
        wallet.home_button.click()
        home.get_chat(basic_user['username']).click()
        chat.show_images_button.click()
        chat.allow_button.click()
        if not chat.first_image_from_gallery.is_element_displayed():
            self.errors.append('Image previews are not shown after denying and allowing access to gallery')
        self.errors.verify_no_errors()

    @marks.testrail_id(5757)
    @marks.medium
    def test_search_chat_on_home(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.just_fyi('Join public chat, start 1-1 with username and with ENS')
        chat_name = home.get_random_chat_name()
        public_chat = home.join_public_chat(chat_name)
        public_chat.get_back_to_home_view()
        for public_key in (basic_user['public_key'], ens_user_ropsten['ens'], dummy_user['public_key']):
            chat = home.add_contact(public_key)
            chat.get_back_to_home_view()
        profile = home.profile_button.click()
        profile.open_contact_from_profile(dummy_user['username'])
        nickname = 'dummy_user'
        public_chat.set_nickname(nickname)
        public_chat.get_back_to_home_view()
        public_chat.home_button.click()

        search_list = {
            basic_user['username']: basic_user['username'],
            ens_user_ropsten['username']: ens_user_ropsten['ens'],
            chat_name: chat_name,
            nickname: nickname,
            dummy_user['username']: nickname,
            ens_user_ropsten['ens']: ens_user_ropsten['ens']
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
        home.search_input.click()
        home.search_input.send_keys(chat_name)
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
        home = SignInView(self.driver).create_user()
        chat_view = home.add_contact(basic_user["public_key"], add_in_contacts=False)

        chat_view.just_fyi('Block user not added as contact from chat view')
        chat_view.chat_options.click()
        chat_view.view_profile_button.click()
        chat_view.block_contact()
        chat_view.get_back_to_home_view()

        chat_view.just_fyi('Unblock user not added as contact from chat view')
        profile = home.profile_button.click()
        profile.contacts_button.click()
        profile.blocked_users_button.click()
        profile.element_by_text(basic_user["username"]).click()
        chat_view.unblock_contact_button.click()

        profile.just_fyi('Navigating to contact list and check that user is not in list')
        profile.close_button.click()
        profile.back_button.click()
        if profile.element_by_text(basic_user["username"]).is_element_displayed():
            self.driver.fail("Unblocked user not added previously in contact list added in contacts!")

    @marks.testrail_id(5498)
    @marks.medium
    def test_share_user_profile_url_public_chat(self):
        home = SignInView(self.driver).create_user()

        home.just_fyi('Join to one-to-one chat and share link to other user profile via messenger')
        chat_view = home.add_contact(dummy_user["public_key"])
        chat_view.chat_options.click()
        chat_view.view_profile_button.click_until_presence_of_element(chat_view.remove_from_contacts)
        chat_view.profile_details.click()
        chat_view.share_button.click()
        chat_view.share_via_messenger()
        if not chat_view.element_by_text_part(
                'https://join.status.im/u/%s' % dummy_user["public_key"]).is_element_present():
            self.errors.append("Can't share public key of contact")
        for _ in range(2):
            chat_view.click_system_back_button()

        home.just_fyi('Join to public chat and share link to it via messenger')
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

        home.just_fyi('Open URL and share link to it via messenger')
        daap_view = home.dapp_tab_button.click()
        browsing_view = daap_view.open_url('dap.ps')
        browsing_view.options_button.click()
        browsing_view.share_url_button.click()
        browsing_view.share_via_messenger()
        expeceted_text_1 = 'https://join.status.im/b/https://dap.ps'
        expeceted_text_2 = 'https://join.status.im/b/http://dap.ps'

        if not (chat_view.element_by_text_part(expeceted_text_1).is_element_present() or
                chat_view.element_by_text_part(expeceted_text_2).is_element_present()):
            self.errors.append("Can't share link to URL")

        self.errors.verify_no_errors()


class TestChatManagementMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5332)
    @marks.critical
    def test_add_and_remove_mention_contact_with_nickname_from_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = device_1.create_user(), device_2.create_user()
        public_key_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        home_2.home_button.click()
        chat_name = 'testaddcontact'

        device_1.just_fyi('join same public chat')
        chat_1, chat_2 = home_1.join_public_chat(chat_name), home_2.join_public_chat(chat_name)
        message = 'test message' + str(round(time.time()))
        chat_2.send_message(message)

        home_2.just_fyi('check that can mention user with 3-random name in public chat')
        chat_1.select_mention_from_suggestion_list(username_2, typed_search_pattern=username_2[0:4])
        if chat_1.chat_message_input.text != '@' + username_2 + ' ':
            self.errors.append(
                '3-random username is not resolved in chat input after selecting it in mention suggestions list!')
        chat_1.send_message_button.click()
        chat_1.chat_element_by_text(username_2).click()
        chat_1.profile_send_message.wait_for_visibility_of_element(20)
        chat_1.close_button.click()
        chat_2.driver.quit()

        device_1.just_fyi('Tap on userpic and check redirect to user profile')
        chat_element = chat_1.chat_element_by_text(message)
        chat_element.find_element()
        chat_element.member_photo.click()
        for element in [chat_1.contact_profile_picture,
                        chat_1.element_by_text(username_2, 'text'),
                        chat_1.profile_add_to_contacts,
                        chat_1.profile_send_message,
                        chat_1.profile_nickname]:
            if not element.scroll_to_element():
                self.errors.append('%s (locator is %s ) is not visible' % (element.name, element.locator))
        if chat_1.profile_nickname.text != 'None':
            self.errors.append('Default nickname is %s instead on "None"' % chat_1.profile_nickname.text)

        device_1.just_fyi('Set nickname for user without adding him to contacts, check it in public chat')
        nickname = 'Name1'
        chat_1.set_nickname(nickname)
        chat_1.close_button.click()
        expected_username = '%s %s' % (nickname, username_2)
        if chat_element.username.text != expected_username:
            self.errors.append('Username %s in public chat does not match expected %s' % (
            chat_element.username.text, expected_username))

        device_1.just_fyi('Add user to contacts, mention it by nickname check contact list in Profile')
        chat_element.member_photo.click()
        chat_1.profile_add_to_contacts.click()
        if not chat_1.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts'")
        chat_1.close_button.click()

        home_2.just_fyi('check that can mention user with nickname in public chat')
        chat_1.select_mention_from_suggestion_list(username_in_list=nickname + ' ' + username_2,
                                                   typed_search_pattern=nickname[0:2])
        if chat_1.chat_message_input.text != '@' + username_2 + ' ':
            self.errors.append('3-random username is not resolved in chat input after selecting it in mention '
                               'suggestions list by nickname!')
        additional_text = 'and more'
        chat_1.send_as_keyevent(additional_text)
        chat_1.send_message_button.click()
        if not chat_1.chat_element_by_text('%s %s' % (nickname, additional_text)).is_element_displayed():
            self.errors.append("Nickname is not resolved on send message")

        device_1.just_fyi('check contact list in Profile after setting nickname')
        profile_1 = chat_1.profile_button.click()
        userprofile = profile_1.open_contact_from_profile(nickname)
        if not userprofile.remove_from_contacts.is_element_displayed():
            self.errors.append("'Add to contacts' is not changed to 'Remove from contacts' in profile contacts")
        profile_1.close_button.click()
        profile_1.home_button.double_click()

        device_1.just_fyi(
            'Check that user is added to contacts below "Start new chat" and you redirected to 1-1 on tap')
        home_1.plus_button.click()
        home_1.start_new_chat_button.click()
        if not home_1.element_by_text(nickname).is_element_displayed():
            home_1.driver.fail('List of contacts below "Start new chat" does not contain added user')
        home_1.element_by_text(nickname).click()
        if not chat_1.chat_message_input.is_element_displayed():
            home_1.driver.fail('No redirect to 1-1 chat if tap on Contact below "Start new chat"')
        for element in (chat_1.chat_message_input, chat_1.element_by_text(nickname)):
            if not element.is_element_displayed():
                self.errors.append('Expected element is not found in 1-1 after adding user to contacts from profile')
        if chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is shown in 1-1 after adding user to contacts from profile')

        device_1.just_fyi('Remove user from contacts')
        chat_1.profile_button.click()
        userprofile = profile_1.open_contact_from_profile(nickname)
        userprofile.remove_from_contacts.click()
        if userprofile.remove_from_contacts.is_element_displayed():
            self.errors.append("'Remove from contacts' is not changed to 'Add to contacts'")
        if chat_1.profile_nickname.text != nickname:
            self.errors.append("Nickname is changed after removing user from contacts")

        device_1.just_fyi('Check that user is removed from contact list in profile')
        userprofile.close_button.click()
        if profile_1.element_by_text(nickname).is_element_displayed():
            self.errors.append('List of contacts in profile contains removed user')
        profile_1.home_button.click(desired_view='chat')
        if not chat_1.add_to_contacts.is_element_displayed():
            self.errors.append('"Add to contacts" button is not shown in 1-1 after removing user from contacts')
        home_1.get_back_to_home_view()
        home_1.plus_button.click()
        home_1.start_new_chat_button.click()
        if home_1.get_username_below_start_new_chat_button(nickname).is_element_displayed():
            self.errors.append('List of contacts below "Start new chat" contains removed user')
        self.errors.verify_no_errors()

    @marks.testrail_id(5786)
    @marks.critical
    def test_block_user_from_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
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
        # device_1.accept_agreements()
        device_1.sign_in()
        home_1.join_public_chat(chat_name)
        for message in message_before_block_2, message_after_block_2:
            if chat_public_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    "'%s' from blocked user %s are shown in public chat" % (message, device_2.driver.number))

    @marks.testrail_id(5763)
    @marks.medium
    def test_block_user_from_one_to_one_header_check_push_notification_service(self):
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
        device_1.element_by_text_part("Background notification service").click()

        if public_chat_after_block_1.chat_element_by_text(message_after_block_2).is_element_displayed():
            self.errors.append("Message from blocked user '%s' is received" % device_2.driver.number)
        public_chat_after_block_1.get_back_to_home_view()
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
        if not device_1.element_by_text_part("Background notification service").is_element_displayed():
            self.errors.append("Background notification service is not started after relogin")

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

    @marks.testrail_id(6315)
    @marks.critical
    def test_reactions_to_message_in_chats(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('Both devices join to 1-1 chat')
        device_2_public_key = home_2.get_public_key_and_username()
        device_1_profile = home_1.profile_button.click()
        device_1_username = device_1_profile.default_username_text.text
        home_1.home_button.click()

        device_1.just_fyi("Sender start 1-1 chat, set emoji and check counter")
        device_1_chat = home_1.add_contact(device_2_public_key)
        device_1_chat.send_message(message_from_sender)
        device_1_chat.set_reaction(message_from_sender)
        message_sender = device_1_chat.chat_element_by_text(message_from_sender)
        if message_sender.emojis_below_message() != 1:
            self.errors.append("Counter of reaction is not updated on your own message!")

        device_2.just_fyi("Receiver  set own emoji and verifies counter on received message in 1-1 chat")
        home_2.home_button.click()
        device_2_chat_item = home_2.get_chat(device_1_username)
        device_2_chat_item.wait_for_visibility_of_element(20)
        device_2_chat = device_2_chat_item.click()
        message_receiver = device_2_chat.chat_element_by_text(message_from_sender)
        if message_receiver.emojis_below_message(own=False) != 1:
            self.errors.append("Counter of reaction is not updated on received message!")
        device_2_chat.set_reaction(message_from_sender)
        for counter in message_sender.emojis_below_message(), message_receiver.emojis_below_message():
            if counter != 2:
                self.errors.append('Counter is not updated after setting emoji from receiver!')

        device_2.just_fyi("Receiver pick the same emoji and verify that counter will decrease for both users")
        device_2_chat.set_reaction(message_from_sender)
        for counter in message_sender.emojis_below_message(), message_receiver.emojis_below_message(own=False):
            if counter != 1:
                self.errors.append('Counter is not decreased after re-tapping  emoji from receiver!')
        [chat.get_back_to_home_view() for chat in (device_2_chat, device_1_chat)]

        device_1.just_fyi('Both devices joining the same public chat, send messages and check counters')
        chat_name = device_1.get_random_chat_name()
        [home.join_public_chat(chat_name) for home in (home_1, home_2)]
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        chat_public_1.send_message(message_from_sender)

        device_1_chat.just_fyi('Set several emojis as sender and receiver and check counters in public chat')
        message_sender = chat_public_1.chat_element_by_text(message_from_sender)
        emojis_from_sender = ['thumbs-down', 'love', 'laugh']
        [chat_public_1.set_reaction(message_from_sender, emoji) for emoji in emojis_from_sender]
        emojis_from_receiver = ['angry', 'sad']
        [chat_public_2.set_reaction(message_from_sender, emoji) for emoji in emojis_from_receiver]
        message_receiver = chat_public_2.chat_element_by_text(message_from_sender)
        for emoji in emojis_from_sender:
            if message_sender.emojis_below_message(emoji) != 1:
                self.errors.append(
                    'Counter is not updated on own message after tapping %s for sender in pub chat' % emoji)
            if message_receiver.emojis_below_message(emoji, own=False) != 1:
                self.errors.append(
                    'Counter is not updated on received message after tapping %s for receiver in pub chat' % emoji)
        for emoji in emojis_from_receiver:
            if message_sender.emojis_below_message(emoji, own=False) != 1:
                self.errors.append(
                    'Counter is not updated on own message after tapping %s for receiver in pub chat' % emoji)
            if message_receiver.emojis_below_message(emoji) != 1:
                self.errors.append(
                    'Counter is not updated on received message after tapping %s for sender in pub chat' % emoji)

        device_1_chat.just_fyi('Unset emoji and check that it is not shown anymore')
        chat_public_1.set_reaction(message_from_sender, 'love')
        if message_sender.emojis_below_message('love') != 0:
            self.errors.append('Emoji is still shown on message after re-tapping last reaction')

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
        if device_1_chat.view_profile_by_avatar_button.is_element_displayed():
            self.errors.append('Member photo is shown on long tap on sent message from 1-1 chat')
        device_1_chat.click_system_back_button(2)

        device_2.just_fyi("1-1 chat: receiver verifies that can open sender profile on long tap on message")
        home_2.home_button.click()
        device_2_chat_item = home_2.get_chat(device_1_username)
        device_2_chat_item.wait_for_visibility_of_element(20)
        device_2_chat = device_2_chat_item.click()
        device_2_chat.chat_element_by_text(message_from_sender).long_press_element()
        if device_2_chat.view_profile_by_avatar_button.is_element_displayed():
            self.errors.append('1-1 chat: another user profile is opened on long tap on received message')
        device_2_chat.click_system_back_button(2)

        device_1.just_fyi(
            'Public chat: send message and verify that user profile can be opened on long press on message')
        chat_name = device_1.get_random_chat_name()
        for home in home_1, home_2:
            home.join_public_chat(chat_name)
        chat_public_1, chat_public_2 = home_1.get_chat_view(), home_2.get_chat_view()
        chat_public_2.send_message(message_from_receiver)
        chat_public_2.chat_element_by_text(message_from_receiver).long_press_element()
        if chat_public_2.view_profile_by_avatar_button.is_element_displayed():
            self.errors.append('Public chat: "view profile" is shown on long tap on sent message')
        chat_public_1.view_profile_long_press(message_from_receiver)
        if not chat_public_1.remove_from_contacts.is_element_displayed():
            self.errors.append('Public chat: another user profile is not opened on long tap on received message')

        self.errors.verify_no_errors()

    @marks.testrail_id(6326)
    @marks.medium
    def test_mention_users_not_in_chats_if_not_in_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        sender = ens_user_message_sender
        home_1, home_2 = device_1.create_user(), device_2.recover_access(passphrase=sender['passphrase'])

        profile_2 = home_2.profile_button.click()
        profile_2.switch_network()

        home_2.profile_button.click()

        home_2.just_fyi('Set ENS name so its visible in chats')
        dapp_view = profile_2.ens_usernames_button.click()
        dapp_view.element_by_text('Get started').click()
        dapp_view.ens_name_input.set_value(sender['ens'])
        dapp_view.check_ens_name.click_until_presence_of_element(dapp_view.element_by_translation_id("ens-got-it"))
        dapp_view.element_by_translation_id("ens-got-it").click()

        device_1.just_fyi('Both devices joining the same public chat and send messages')
        chat_name = device_1.get_random_chat_name()
        own_default_username = home_1.get_public_key_and_username(return_username=True)
        home_1.home_button.click()
        chat_1 = home_1.join_public_chat(chat_name)
        profile_2.home_button.click()
        chat_2 = home_2.join_public_chat(chat_name)
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
        chat_1.close_button.click()
        ens_nickname_value = nickname + " @" + sender['ens']
        chat_1.wait_ens_name_resolved_in_chat(message=message, username_value=ens_nickname_value)

        device_1.just_fyi('Check there is ENS+Nickname user in separate 1-1 chat')
        chat_1.get_back_to_home_view()
        home_1.add_contact(public_key=basic_user['public_key'])
        chat_1.chat_message_input.send_keys('@')
        if not (chat_1.search_user_in_mention_suggestion_list(ens_nickname_value).is_element_displayed() or
                chat_1.search_user_in_mention_suggestion_list(
                    sender['username']).is_element_displayed()):
            self.errors.append('ENS-owner user is not available in mention suggestion list')

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
            self.errors.append('Blcoked user is available in mention suggestion list')

        self.errors.verify_no_errors()

    @marks.testrail_id(695771)
    @marks.medium
    def test_activity_center_group_chats_trusted_contacts(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        GroupChat1Name = "GroupChat1"
        GroupChat2Name = "GroupChat2"
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
        home_2.create_group_chat([username_1], group_chat_name=GroupChat1Name)

        device_1.just_fyi('Device1 check there are no any chats in Activity Center nor Chats view')

        home_1.home_button.click()
        if home_1.element_by_text_part(username_2).is_element_displayed() or home_1.element_by_text_part(GroupChat1Name).is_element_displayed():
            self.errors.append("Chats are present on Chats view despite they created by non-contact")
        home_1.notifications_button.click()
        if home_1.element_by_text_part(username_2).is_element_displayed() or home_1.element_by_text_part(GroupChat1Name).is_element_displayed():
            self.errors.append("Chats are present in Activity Center view despite they created by non-contact")

        device_1.just_fyi('Device1 adds Device2 in Contacts so chat requests should be visible now')
        home_1.home_button.click()
        home_1.add_contact(public_key_user_2)

        device_1.just_fyi('Device2 creates 1-1 chat Group chats once again')
        home_2.home_button.click()
        home_2.get_chat_from_home_view(username_1).click()
        one_to_one_device_2.send_message(message_from_sender)
        one_to_one_device_2.home_button.click()
        home_2.create_group_chat([username_1], group_chat_name=GroupChat2Name)

        device_1.just_fyi('Device1 verifies 1-1 chat Group chats are visible')

        home_1.home_button.click()
        if not home_1.element_by_text_part(username_2).is_element_displayed() or not home_1.element_by_text_part(GroupChat2Name).is_element_displayed():
            self.errors.append("Chats are not present on Chats view while they have to!")

        self.errors.verify_no_errors()

    @marks.testrail_id(695782)
    @marks.medium
    def test_can_accept_or_reject_multiple_chats_from_activity_center(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        message_from_sender = "Message sender"
        GroupChat1Name = "GroupChat1"
        GroupChat2Name = "GroupChat2"
        home_1, home_2 = device_1.create_user(), device_2.create_user()

        device_1.just_fyi('Device1 adds Devices and creates 1-1 and Group chat with it')
        public_key_user_1, username_1 = home_1.get_public_key_and_username(return_username=True)
        public_key_user_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        home_1.home_button.click()
        device_1_one_to_one_chat = home_1.add_contact(public_key_user_2)
        device_1_one_to_one_chat.send_message(message_from_sender)
        device_1_one_to_one_chat.home_button.click()

        home_1.create_group_chat([username_2], group_chat_name=GroupChat1Name)
        home_1.home_button.click()
        home_2.home_button.click()

        device_1.just_fyi('Device2 rejects both chats and verifies they disappeared and not in Chats too')
        home_2.notifications_button.click()
        home_2.notifications_select_button.click()
        home_2.element_by_text_part(username_1[:10]).click()
        home_2.element_by_text_part(GroupChat1Name).click()
        home_2.notifications_reject_and_delete_button.click()

        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is on Activity Center view after action made on it")
        if home_2.element_by_text_part(GroupChat1Name).is_element_displayed(2):
            self.errors.append("Group chat is on Activity Center view after action made on it")
        home_2.home_button.click()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is added on home after rejection")
        if home_2.element_by_text_part(GroupChat1Name).is_element_displayed(2):
            self.errors.append("Group chat is added on home after rejection")

        home_2.just_fyi("Verify there are still no chats after relogin")
        home_2.relogin()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat appears on Chats view after relogin")
        if home_2.element_by_text_part(GroupChat1Name).is_element_displayed(2):
            self.errors.append("Group chat appears on Chats view after relogin")
        home_2.notifications_button.click()
        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request reappears back in Activity Center view after relogin")
        if home_2.element_by_text_part(GroupChat1Name).is_element_displayed(2):
            self.errors.append("Group chat request reappears back in Activity Center view after relogin")
        home_2.home_button.click()

        device_1.just_fyi('Device1 creates 1-1 and Group chat again')
        home_1.get_chat_from_home_view(username_2).click()
        device_1_one_to_one_chat.send_message('Some text here')
        device_1_one_to_one_chat.home_button.click()
        home_1.create_group_chat([username_2], group_chat_name=GroupChat2Name)

        device_1.just_fyi('Device2 accepts both chats (via Select All button) and verifies they disappeared '
                          'from activity center view but present on Chats view')
        home_2.notifications_button.click()
        home_2.notifications_select_button.click()
        home_2.notifications_select_all.click()
        home_2.notifications_accept_and_add_button.click()

        if home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat request stays on Activity Center view after it was accepted")
        if home_2.element_by_text_part(GroupChat2Name).is_element_displayed(2):
            self.errors.append("Group chat request stays on Activity Center view after it was accepted")
        home_2.home_button.click()

        if not home_2.element_by_text_part(username_1[:20]).is_element_displayed(2):
            self.errors.append("1-1 chat is not added on home after accepted from Activity Center")
        if not home_2.element_by_text_part(GroupChat2Name).is_element_displayed(2):
            self.errors.append("Group chat is not added on home after accepted from Activity Center")

        self.errors.verify_no_errors()
