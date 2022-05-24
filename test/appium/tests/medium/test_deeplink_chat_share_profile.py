import re
import random
import string
import pytest

from tests import marks, mailserver_ams, mailserver_gc, mailserver_hk, used_fleet, common_password, test_dapp_name,\
    test_dapp_url, pair_code, unique_password
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from tests.users import user_mainnet, chat_users, dummy_user, recovery_users, transaction_senders, basic_user,\
    wallet_users, ens_user_ropsten, ens_user
from selenium.common.exceptions import NoSuchElementException

from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.send_transaction_view import SendTransactionView
from views.chat_view import ChatView
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="two_1")
@marks.medium
class TestDeeplinkChatProfileOneDevice(MultipleSharedDeviceTestCase):

    @classmethod
    def setup_class(cls):
        cls.drivers, cls.loop = create_shared_drivers(1)
        cls.sign_in = SignInView(cls.drivers[0])
        cls.home = cls.sign_in.create_user()
        cls.public_key, cls.default_username = cls.home.get_public_key_and_username(return_username=True)
        cls.home.home_button.click()
        cls.public_chat_name = 'pubchat'
        cls.nickname = 'dummy_user'
        cls.search_list_1 = {
            basic_user['username']: basic_user['username'],
            ens_user_ropsten['username']: ens_user_ropsten['ens'],
            cls.public_chat_name: cls.public_chat_name,
            cls.nickname: cls.nickname,
            dummy_user['username']: cls.nickname,
            ens_user_ropsten['ens']: ens_user_ropsten['ens']
        }
        cls.public_chat = cls.home.join_public_chat(cls.public_chat_name)
        cls.public_chat.get_back_to_home_view()

    @marks.testrail_id(702244)
    def test_deep_link_with_invalid_user_public_key_own_profile_key(self):
        self.drivers[0].close_app()

        self.sign_in.just_fyi('Check that no error when opening invalid deep link')
        deep_link = 'status-im://u/%s' % self.public_key[:-10]
        self.sign_in.open_weblink_and_login(deep_link)
        self.home = self.sign_in.get_home_view()
        self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
        if not self.home.start_new_chat_button.is_element_present():
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
        for user_ident in ens_user['ens'], ens_user['ens_another'], ens_user['public_key']:
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
            test_dapp_view.allow_button.is_element_present()
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
                'https://join.status.im/u/%s' % dummy_user["public_key"]).is_element_present():
            self.errors.append("Can't share public key of contact")
        for _ in range(2):
            chat.click_system_back_button()

    @marks.testrail_id(702252)
    def test_share_user_profile_url_public_chat(self):

        self.home.just_fyi('Join to public chat and share link to it via messenger')
        self.home.home_button.click()

        self.home.get_chat('#' + self.public_chat_name).click()
        self.public_chat.chat_options.click()
        self.public_chat.share_chat_button.click()
        self.public_chat.share_via_messenger()
        if not self.public_chat.element_by_text_part('https://join.status.im/%s' % self.public_chat_name).is_element_present():
            self.errors.append("Can't share link to public chat")
        for _ in range(2):
            self.public_chat.click_system_back_button()
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

        if not (browser.element_by_text_part(expeceted_text_1).is_element_present() or
                browser.element_by_text_part(expeceted_text_2).is_element_present()):
            self.errors.append("Can't share link to URL")

        browser.click_system_back_button(1)

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

        ens_name_status, ens_name_another_domain, public_chat_name = ens_user_ropsten['ens'], \
                                                                     ens_user['ens_another'], 'some-pub-chat'
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
            self.errors.append("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

        """self.home.just_fyi('Cutting message text from input field')
        message_input.cut_text()
        message_input.click()
        if current_text != message_text[:-4]:
            self.errors.append("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-4]))"""

        message_input.cut_text()

        self.home.just_fyi('Pasting the cut message back to the input field')
        message_input.paste_text_from_clipboard()
        if current_text != message_text[:-2]:
            self.errors.append("Message input text '%s' doesn't match expected '%s'" % (current_text, message_text[:-2]))

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
