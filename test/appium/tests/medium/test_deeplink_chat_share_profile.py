import pytest
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, test_dapp_url
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from tests.users import dummy_user, transaction_senders, basic_user, \
    ens_user_message_sender, ens_user
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="two_1")
@marks.medium
class TestDeeplinkChatProfileOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.create_user()
        self.public_key, self.default_username = self.home.get_public_key()
        self.home.home_button.click()
        self.public_chat_name = 'pubchat'
        self.nickname = 'dummy_user'
        self.search_list_1 = {
            basic_user['username']: basic_user['username'],
            ens_user_message_sender['username']: ens_user_message_sender['ens'],
            self.public_chat_name: self.public_chat_name,
            self.nickname: self.nickname,
            dummy_user['username']: self.nickname,
            ens_user_message_sender['ens']: ens_user_message_sender['ens']
        }
        self.public_chat = self.home.join_public_chat(self.public_chat_name)
        self.public_chat.get_back_to_home_view()

        self.home.just_fyi("Close the ENS banner")
        self.home.ens_banner_close_button.click_if_shown()

    @marks.testrail_id(702244)
    def test_deep_link_with_invalid_user_public_key_own_profile_key_old(self):
        self.drivers[0].close_app()

        self.sign_in.just_fyi('Check that no error when opening invalid deep link')
        deep_link = 'status-im://u/%s' % self.public_key[:-10]
        self.sign_in.open_weblink_and_login(deep_link)
        self.home = self.sign_in.get_home_view()
        self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
        if not self.home.start_new_chat_button.is_element_displayed():
            self.errors.append(
                "Can't navigate to start new chat after app opened from deep link with invalid public key")
        self.drivers[0].close_app()

        self.sign_in.just_fyi('Check that no error when opening own valid deep link')
        deep_link = 'status-im://u/%s' % self.public_key
        self.sign_in.open_weblink_and_login(deep_link)
        from views.profile_view import ProfileView
        profile = ProfileView(self.drivers)
        if not profile.default_username_text != self.default_username:
            self.errors.append("Can't navigate to profile from deep link with own public key")
        self.errors.verify_no_errors()

    @marks.testrail_id(702245)
    def test_deep_link_open_user_profile(self):
        for user_ident in ens_user['ens'], ens_user['ens_upgrade'], ens_user['public_key']:
            self.drivers[0].close_app()
            deep_link = 'status-im://u/%s' % user_ident
            self.sign_in.open_weblink_and_login(deep_link)
            chat = self.sign_in.get_chat_view()
            for text in ens_user['username'], self.sign_in.get_translation_by_key("add-to-contacts"):
                if not chat.element_by_text(text).scroll_to_element(10):
                    self.driver.fail("User profile screen is not opened")
            self.errors.verify_no_errors()

    @marks.testrail_id(702246)
    def test_deep_link_open_dapp(self):
        self.drivers[0].close_app()
        dapp_name = test_dapp_url
        dapp_deep_link = 'status-im://b/%s' % dapp_name
        self.sign_in.open_weblink_and_login(dapp_deep_link)
        web_view = self.sign_in.get_chat_view()
        try:
            test_dapp_view = web_view.open_in_status_button.click()
            test_dapp_view.allow_button.is_element_displayed()
        except NoSuchElementException:
            self.drivers[0].fail("DApp '%s' is not opened!" % dapp_name)
        self.home.reopen_app()
        self.errors.verify_no_errors()

    @marks.testrail_id(702247)
    def test_share_user_profile_url_one_to_one_chat(self):
        self.home.home_button.click()

        self.home.just_fyi('Join to one-to-one chat and share link to other user profile via messenger')
        chat = self.home.add_contact(dummy_user["public_key"])
        chat.chat_options.click()
        chat.view_profile_button.click_until_presence_of_element(chat.remove_from_contacts)
        chat.profile_details.click()
        chat.share_button.click()
        chat.share_via_messenger()
        if not chat.element_by_text_part(
                'https://join.status.im/u/%s' % dummy_user["public_key"]).is_element_displayed():
            self.errors.append("Can't share public key of contact")
        for _ in range(2):
            chat.navigate_back_to_home_view()

    @marks.testrail_id(702252)
    def test_share_user_profile_url_public_chat(self):

        self.home.just_fyi('Join to public chat and share link to it via messenger')
        self.home.home_button.click()

        self.home.get_chat('#' + self.public_chat_name).click()
        self.public_chat.chat_options.click()
        self.public_chat.share_chat_button.click()
        self.public_chat.share_via_messenger()
        if not self.public_chat.element_by_text_part(
                'https://join.status.im/%s' % self.public_chat_name).is_element_displayed():
            self.errors.append("Can't share link to public chat")
        for _ in range(2):
            self.public_chat.navigate_back_to_home_view()
        self.public_chat.get_back_to_home_view()

    @marks.testrail_id(702251)
    def test_share_user_profile_url_browser(self):

        self.home.just_fyi('Open URL and share link to it via messenger')
        daap = self.home.dapp_tab_button.click()
        browser = daap.open_url('dap.ps')
        browser.options_button.click()
        browser.share_url_button.click()
        browser.share_via_messenger()
        expeceted_text_1 = 'https://join.status.im/b/https://dap.ps'
        expeceted_text_2 = 'https://join.status.im/b/http://dap.ps'

        if not (browser.element_by_text_part(expeceted_text_1).is_element_displayed() or
                browser.element_by_text_part(expeceted_text_2).is_element_displayed()):
            self.errors.append("Can't share link to URL")

        browser.navigate_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702248)
    def test_chat_can_start_and_find_from_suggestions_using_search(self):
        self.home.home_button.double_click()
        self.home.just_fyi('Start 1-1 with username and with ENS')

        for public_key in (basic_user['public_key'], dummy_user['public_key']):
            chat = self.home.add_contact(public_key)
            chat.get_back_to_home_view()
        profile = self.home.profile_button.click()
        profile.open_contact_from_profile(dummy_user['username'])
        self.public_chat.set_nickname(self.nickname)
        self.public_chat.home_button.click()

        ens_name_status, ens_name_another_domain, public_chat_name = ens_user_message_sender['ens'], \
                                                                     ens_user['ens'], 'some-pub-chat'
        search_list_2 = {
            ens_name_status: {
                'home': {
                    'Start a new private chat',
                    'Join a public chat',
                    '%s.stateofus.eth' % ens_name_status,
                    '#%s' % ens_name_status
                },
                'navigate_to': '%s.stateofus.eth' % ens_name_status,
                'chat': {
                    chat.add_to_contacts,
                    chat.element_by_text('@%s' % ens_name_status),
                    chat.chat_message_input
                }
            },
            ens_name_another_domain: {
                'home': {
                    'Start a new private chat',
                },
                'home_not_shown': 'Join a public chat',
                'navigate_to': 'Start a new private chat',
                'chat': {
                    chat.add_to_contacts,
                    chat.element_by_text('@%s' % ens_name_another_domain),
                    chat.chat_message_input
                },
            },
            public_chat_name: {
                'home': {
                    'Join a public chat',
                },
                'home_not_shown': 'Start a new private chat',
                'navigate_to': '#%s' % public_chat_name,
                'chat': {
                    chat.element_by_text('#%s' % public_chat_name),
                    chat.chat_message_input
                },
            },

        }

        self.home.swipe_down()

        for keyword in search_list_2:
            self.home.just_fyi('Can start chat from searching for %s' % keyword)
            self.home.search_by_keyword(keyword)
            if not self.home.element_by_text_part('No search results. Do you mean').is_element_displayed():
                self.errors.append('"No search results" is not shown')
            if 'home_not_shown' in search_list_2[keyword]:
                if self.home.element_by_text(search_list_2[keyword]['home_not_shown']).is_element_displayed():
                    self.errors.append('%s is shown on home view while searching for %s' % (
                        search_list_2[keyword]['home_not_shown'], keyword))
            for text in search_list_2[keyword]['home']:
                if not self.home.element_by_text(text).is_element_displayed():
                    self.errors.append('%s is not shown on home view while searching for %s' % (text, keyword))
            self.home.element_by_text(search_list_2[keyword]['navigate_to']).click()
            for element in search_list_2[keyword]['chat']:
                if not element.is_element_displayed():
                    self.errors.append(
                        'Requested %s element is not shown on chat view after navigating from suggestion '
                        'for %s' % (element.name, keyword))
            self.home.back_button.click()

        self.home.just_fyi('Can search for public chat name, ens name, username')
        self.home.swipe_down()
        for keyword in self.search_list_1:
            self.home.search_by_keyword(keyword)
            search_results = self.home.chat_name_text.find_elements()
            if not search_results:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            for element in search_results:
                if self.search_list_1[keyword] not in element.text:
                    self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                       (element.text, keyword))
            self.home.cancel_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(702253)
    def test_chat_no_suggestions_invalid_data_search(self):
        self.home.just_fyi('No suggestion at attempt to search for invalid data')
        self.home.home_button.double_click()
        invalid_data = ['   ', 'ab;', '.6', '@ana']
        for text in invalid_data:
            self.home.search_by_keyword(text)
            if self.home.element_by_text_part('No search results. Do you mean').is_element_displayed():
                self.errors.append('"No search results" is shown when searching for invalid value %s' % text)
            self.home.cancel_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(702249)
    def test_chat_input_delete_cut_and_paste_messages(self):
        chat = self.home.add_contact(transaction_senders['N']['public_key'])
        message_text = 'test'
        message_input = chat.chat_message_input
        message_input.send_keys(message_text)

        self.home.just_fyi('Deleting last 2 symbols in initial message')
        message_input.delete_last_symbols(2)
        current_text = message_input.text
        if current_text != message_text[:-2]:
            self.errors.append(
                "Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        """self.home.just_fyi('Cutting message text from input field')
        message_input.cut_text()
        message_input.click()
        if current_text != message_text[:-4]:
            self.errors.append("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-4]))"""

        message_input.cut_text()

        self.home.just_fyi('Pasting the cut message back to the input field')
        message_input.paste_text_from_clipboard()
        if current_text != message_text[:-2]:
            self.errors.append(
                "Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        chat.send_message_button.click()

        chat.chat_element_by_text(message_text[:-2]).wait_for_visibility_of_element(2)

        self.errors.verify_no_errors()

    @marks.testrail_id(702250)
    def test_chat_public_clear_history_via_options_and_long_press(self):
        self.home.home_button.double_click()
        public_long_press = '#public-clear-options'
        options_chat_name = '#' + self.public_chat_name
        message = 'test message'

        long_press_chat = self.home.join_public_chat(public_long_press[1:])
        [long_press_chat.send_message(message) for _ in range(2)]
        self.home.home_button.double_click()

        self.home.element_by_text(options_chat_name).scroll_to_element()
        options_chat = self.home.get_chat(options_chat_name)
        chat_view = options_chat.click()
        [chat_view.send_message(message) for _ in range(2)]
        self.home.home_button.double_click()

        self.home.element_by_text(public_long_press).scroll_to_element(direction='up')
        self.home.just_fyi('Clearing history via long press')
        self.home.clear_chat_long_press(public_long_press)

        self.home.just_fyi('Clearing history via options')
        options_chat.click()
        chat_view.clear_history()
        if chat_view.element_by_text(message).is_element_displayed():
            self.errors.append(
                'Messages in %s chat are still shown after clearing history via options' % options_chat_name)

        self.home.just_fyi("Recheck that history won't reappear after relogin")
        self.home.relogin()
        for chat_name in options_chat_name, public_long_press:
            chat = self.home.get_chat(chat_name).click()
            if chat.chat_element_by_text(message).is_element_displayed():
                self.errors.append('Messages in %s chat are shown after clearing history and relaunch' % chat_name)
            chat.home_button.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(702254)
    # Should be the last in group as it disables network connection
    def test_chat_can_search_while_offline(self):
        self.home.just_fyi('Can search for public chat while offline')
        self.home.get_back_to_home_view()
        self.home.toggle_airplane_mode()
        self.home.search_input.click()
        self.home.search_input.send_keys(self.public_chat_name)
        search_results = self.home.chat_name_text.find_elements()
        if not search_results:
            self.errors.append('No search results after searching by %s keyword' % self.public_chat_name)
        for element in search_results:
            if self.search_list_1[self.public_chat_name] not in element.text:
                self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                   (element.text, self.public_chat_name))

        self.errors.verify_no_errors()

# Skipped before proper testing of ENS names on goerli
# @pytest.mark.xdist_group(name="new_one_1")
# @marks.new_ui_critical
# class TestDeeplinkOneDeviceNewUI(MultipleSharedDeviceTestCase):
#
#     def prepare_devices(self):
#         self.drivers, self.loop = create_shared_drivers(1)
#         self.sign_in = SignInView(self.drivers[0])
#         self.home = self.sign_in.create_user()
#         self.public_key, self.default_username = self.home.get_public_key()
#         self.home.click_system_back_button_until_element_is_shown()
#         self.home.chats_tab.click_until_presence_of_element(self.home.plus_button)
#
#     @marks.testrail_id(702774)
#     def test_deep_link_with_invalid_user_public_key_own_profile_key(self):
#         self.drivers[0].close_app()
#
#         self.sign_in.just_fyi('Check that no error when opening invalid deep link')
#         deep_link = 'status-im://u/%s' % self.public_key[:-10]
#         self.sign_in.open_weblink_and_login(deep_link)
#         self.home = self.sign_in.get_home_view()
#         self.home.chats_tab.click_until_presence_of_element(self.home.plus_button)
#         if not self.home.plus_button.is_element_displayed():
#             self.errors.append(
#                 "Can't navigate to chats tab after app opened from deep link with invalid public key")
#         self.drivers[0].close_app()
#
#         self.sign_in.just_fyi('Check that no error when opening own valid deep link')
#         deep_link = 'status-im://u/%s' % self.public_key
#         self.sign_in.open_weblink_and_login(deep_link)
#         profile = self.home.get_profile_view()
#         if profile.default_username_text.text != self.default_username:
#             self.errors.append("Can't navigate to profile from deep link with own public key")
#         self.errors.verify_no_errors()

    # @marks.testrail_id(702775)
    # @marks.xfail(reason="Profile is often not opened in e2e builds for some reason. Needs to be investigated.")
    # def test_deep_link_open_user_profile(self):
    #     for user_ident in ens_user['ens']:
    #         self.drivers[0].close_app()
    #         deep_link = 'status-im://u/%s' % user_ident
    #         self.sign_in.open_weblink_and_login(deep_link)
    #         chat = self.sign_in.get_chat_view()
    #         chat.wait_for_element_starts_with_text(ens_user['ens'])
    #
    #         for text in ens_user['ens'], self.sign_in.get_translation_by_key("add-to-contacts"):
    #             if not chat.element_by_text(text).scroll_to_element(10):
    #                 self.drivers[0].fail("User profile screen is not opened")

    # @marks.testrail_id(702777)
    # @marks.skip(reason="Skipping until chat names are implemented in new UI")
    # def test_scan_qr_with_scan_contact_code_via_start_chat(self):

    #     url_data = {
    #         'ens_with_stateofus_domain_deep_link': {
    #             'url': 'https://join.status.im/u/%s.stateofus.eth' % ens_user_message_sender['ens'],
    #             'username': '@%s' % ens_user_message_sender['ens']
    #         },
    #         'ens_without_stateofus_domain_deep_link': {
    #             'url': 'https://join.status.im/u/%s' % ens_user_message_sender['ens'],
    #             'username': '@%s' % ens_user_message_sender['ens']
    #         },
    #         'ens_another_domain_deep_link': {
    #             'url': 'status-im://u/%s' % ens_user['ens'],
    #             'username': '@%s' % ens_user['ens']
    #         },
    #         'own_profile_key_deep_link': {
    #             'url': 'https://join.status.im/u/%s' % self.public_key,
    #             'error': "That's you"
    #         },
    #         'other_user_profile_key_deep_link': {
    #             'url': 'https://join.status.im/u/%s' % transaction_senders['M']['public_key'],
    #             'username': transaction_senders['M']['username']
    #         },
    #         'other_user_profile_key_deep_link_invalid': {
    #             'url': 'https://join.status.im/u/%sinvalid' % ens_user['public_key'],
    #             'error': 'Please enter or scan a valid chat key'
    #         },
    #         'own_profile_key': {
    #             'url': self.public_key,
    #             'error': "That's you"
    #         },
    #         # 'ens_without_stateofus_domain': {
    #         #     'url': ens_user['ens'],
    #         #     'username': ens_user['username']
    #         # },
    #         'other_user_profile_key': {
    #             'url': transaction_senders['M']['public_key'],
    #             'username': transaction_senders['M']['username']
    #         },
    #         'other_user_profile_key_invalid': {
    #             'url': '%s123' % ens_user['public_key'],
    #             'error': 'Please enter or scan a valid chat key'
    #         },
    #     }

    #     for key in url_data:
    #         self.home.chats_tab.click_until_presence_of_element(self.home.plus_button)
    #         self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
    #         contacts = self.home.start_new_chat_button.click()
    #         self.home.just_fyi('Checking scanning qr for "%s" case' % key)
    #         contacts.scan_contact_code_button.click()
    #         contacts.allow_button.click_if_shown(3)
    #         contacts.enter_qr_edit_box.scan_qr(url_data[key]['url'])
    #         chat = ChatView(self.drivers[0])
    #         if url_data[key].get('error'):
    #             if not chat.element_by_text_part(url_data[key]['error']).is_element_displayed():
    #                 self.errors.append('Expected error %s is not shown' % url_data[key]['error'])
    #             chat.ok_button.click()
    #         if url_data[key].get('username'):
    #             if not chat.chat_message_input.is_element_displayed():
    #                 self.errors.append(
    #                     'In "%s" case chat input is not found after scanning, so no redirect to 1-1' % key)
    #             if not chat.element_by_text(url_data[key]['username']).is_element_displayed():
    #                 self.errors.append('In "%s" case "%s" not found after scanning' % (key, url_data[key]['username']))
    #             chat.get_back_to_home_view()
    #     self.errors.verify_no_errors()

