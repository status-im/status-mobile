import time

import pytest

from tests import marks, common_password, used_fleet
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.sign_in_view import SignInView
from tests.users import basic_user, ens_user, ens_user_message_sender, transaction_senders, chat_users


@pytest.mark.xdist_group(name="one_1")
@marks.medium
class TestBrowserProfileOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.create_user()
        self.wiki_texts = ['Español', '日本語', '中文', 'Português']

    @marks.testrail_id(702149)
    def test_browser_can_access_images_by_link(self):
        urls = {
            'https://cdn.dribbble.com/users/45534/screenshots/3142450/logo_dribbble.png':
                'url1.png',
            'https://steemitimages.com/DQmYEjeBuAKVRa3b3ZqwLicSHaPUm7WFtQqohGaZdA9ghjx/images%20(4).jpeg':
                'url3.png'
        }
        dapp = self.home.dapp_tab_button.click()
        from views.web_views.base_web_view import BaseWebView
        base_web = BaseWebView(self.drivers[0])
        for url in urls:
            dapp.open_url(url)
            if dapp.web_page.is_element_differs_from_template(urls[url], 5):
                self.errors.append('Web page does not match expected template %s' % urls[url])
            base_web.browser_previous_page_button.click_until_presence_of_element(dapp.element_by_text_part('Discover'), attempts=2)

        self.errors.verify_no_errors()

    @marks.testrail_id(702150)
    def test_browser_back_forward_navigation_history_kept_after_relogin(self):

        dapp = self.home.dapp_tab_button.click()
        ua_url = 'https://uk.m.wikipedia.org/'
        browsing = dapp.open_url(ua_url)
        browsing.element_by_text_part('Ласкаво просимо').wait_for_element(20)

        browsing.just_fyi("Check next page")
        browsing.just_fyi('Navigate to next page and back')
        browsing.element_by_text_part('може редагувати кожен').scroll_and_click()
        browsing.element_by_text_part('написана спільно її читачами').scroll_to_element()
        browsing.browser_previous_page_button.click()
        browsing.wait_for_element_starts_with_text('Головна сторінка')

        browsing.just_fyi('Relogin and check that tap on "Next" navigates to next page')
        browsing.reopen_app()
        self.home.dapp_tab_button.click()
        browsing.open_tabs_button.click()
        dapp.element_by_text_part(ua_url).click()
        browsing.element_by_text_part('може редагувати кожен').scroll_to_element()
        browsing.browser_next_page_button.click()
        browsing.element_by_text_part('написана спільно її читачами').scroll_to_element()

        self.errors.verify_no_errors()

    @marks.testrail_id(702201)
    def test_browser_resolve_ipns_name(self):
        ipns_url = 'uniswap.eth'

        self.home.just_fyi('Switching to Mainnet')
        profile = self.home.profile_button.click()
        profile.switch_network()

        self.home.just_fyi('Opening url containing ipns name')
        dapp = self.home.dapp_tab_button.click()
        web_page = dapp.open_url(ipns_url)
        element_on_start_page = dapp.element_by_text('Select token')
        if not element_on_start_page.is_element_displayed(60):
            self.home.driver.fail('No start element is shown for dapp, so IPNS name is not resolved')

        # Switching back to ropsten
        web_page.profile_button.click()
        profile.switch_network('Goerli with upstream RPC')

        self.errors.verify_no_errors()

    @marks.testrail_id(702179)
    def test_browser_refresh_page(self):
        dapp = self.home.dapp_tab_button.click()
        url = 'status.im'
        web_page = dapp.open_url(url)

        self.home.just_fyi("Open collapsed menu and check required element is shown")
        web_page.open_right_collapsed_menu()
        element_on_start_page = dapp.element_by_text('Get Involved')
        element_on_start_page.wait_for_visibility_of_element(20)

        self.home.just_fyi("Tap on Refresh and check that popup is closed")
        web_page.browser_refresh_page_button.click()
        time.sleep(2)
        if element_on_start_page.is_element_displayed(10):
            self.errors.append("Page failed to be refreshed")
        self.errors.verify_no_errors()

    @marks.testrail_id(702151)
    def test_browser_open_url_with_non_english_text(self):
        dapp = self.home.dapp_tab_button.click()

        dapp.just_fyi('Check non english text is shown in open url')
        browsing = dapp.open_url('www.wikipedia.org')
        for wiki_text in self.wiki_texts:
            if not browsing.element_by_text_part(wiki_text).is_element_displayed(15):
                self.errors.append("%s is not shown" % wiki_text)

        self.errors.verify_no_errors()

    @marks.testrail_id(702180)
    def test_browser_connect_revoke_wallet(self):

        dapp = self.home.dapp_tab_button.click()
        browsing = dapp.open_url('www.wikipedia.org')

        dapp.just_fyi("Check that can connect wallet and revoke access")
        browsing.options_button.click()
        browsing.connect_account_button.click()
        browsing.allow_button.click()
        browsing.options_button.click()
        if not browsing.connected_account_button.is_element_displayed():
            self.home.driver.fail("Account is not connected")
        browsing.click_system_back_button()
        profile = browsing.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        if not profile.element_by_text('wikipedia.org').is_element_displayed():
            self.errors.append("Permissions are not granted")
        profile.dapp_tab_button.click(desired_element_text=self.wiki_texts[0])
        browsing.options_button.click()
        browsing.connected_account_button.click()
        browsing.element_by_translation_id("revoke-access").click()
        browsing.options_button.click()
        if not browsing.connect_account_button.is_element_displayed():
            self.errors.append("Permission for account is not removed if using 'Revoke access' from dapp view")
        browsing.click_system_back_button()
        browsing.profile_button.click(desired_element_text='DApp permissions')
        if profile.element_by_text('wikipedia.org').is_element_displayed():
            self.errors.append("Permissions are not revoked")
        profile.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702181)
    def test_browser_open_chat_options(self):
        dapp = self.home.dapp_tab_button.click()
        browsing = dapp.open_url('www.wikipedia.org')

        dapp.just_fyi("Check that can open chat view and send some message")
        browsing.options_button.click()
        browsing.open_chat_from_dapp_button.click()
        public_chat = browsing.get_chat_view()
        if not public_chat.element_by_text('#wikipedia-org').is_element_displayed():
            self.home.driver.fail("No redirect to public chat")
        message = public_chat.get_random_message()
        public_chat.send_message(message)
        public_chat.dapp_tab_button.click(desired_element_text=self.wiki_texts[0])
        browsing.options_button.click()
        browsing.open_chat_from_dapp_button.click()
        if not public_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Messages are not shown if open dapp chat from view")
        public_chat.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702182)
    def test_browser_new_tab_open(self):
        dapp = self.home.dapp_tab_button.click()
        browsing = dapp.open_url('www.wikipedia.org')

        dapp.just_fyi("Check that can open new tab using 'New tab' from bottom sheet")
        browsing.options_button.click()
        browsing.new_tab_button.click()
        if not browsing.element_by_translation_id("open-dapp-store").is_element_displayed():
            self.errors.append("Was not redirected to home dapp store view using New tab")

        self.errors.verify_no_errors()

    @marks.testrail_id(702159)
    def test_profile_invite_friends(self):
        chat_key = self.home.get_public_key()
        self.home.home_button.double_click()

        self.home.just_fyi("Check it via 'Invite friends' on home view")
        self.home.invite_friends_button.click()
        self.home.share_via_messenger()
        if not self.home.element_by_text_part('Hey join me on Status: https://join.status.im/u/%s' % chat_key).is_element_displayed(20):
            self.errors.append("No expected message in input field when sharing via 'Invite friend'")
        self.home.navigate_back_to_home_view()

        self.home.just_fyi("Check it via bottom sheet menu")
        self.home.plus_button.click()
        self.home.chats_menu_invite_friends_button.click()
        self.home.share_via_messenger()
        if not self.home.element_by_text_part('Hey join me on Status: https://join.status.im/u/%s' % chat_key).is_element_displayed(20):
            self.errors.append("No expected message in input field when sharing via 'bottom sheet'")
        self.home.navigate_back_to_home_view()
        self.errors.verify_no_errors()

    @marks.testrail_id(702160)
    def test_profile_add_remove_contact_via_contacts_view(self):
        self.home.just_fyi('Check empty contacts view')
        profile = self.home.profile_button.click()
        self.home.profile_button.click()
        profile.contacts_button.click()
        if not profile.add_new_contact_button.is_element_displayed():
            self.home.driver.fail('No expected element on contacts view')

        users = {
            'scanning_ens_with_stateofus_domain_deep_link': {
                'contact_code': 'https://join.status.im/u/%s.stateofus.eth' % ens_user_message_sender['ens'],
                'username': ens_user_message_sender['username']
            },
            'scanning_public_key': {
                'contact_code': transaction_senders['A']['public_key'],
                'username': transaction_senders['A']['username'],
            },
            'pasting_public_key': {
                'contact_code': basic_user['public_key'],
                'username': basic_user['username'],
            },
            'pasting_ens_another_domain': {
                'contact_code': ens_user['ens'],
                'username': '@%s' % ens_user['ens'],
                'nickname': 'my_dear_friend'
            },

        }

        self.home.just_fyi('Add contact  and check that they appear in Contacts view')
        chat = self.home.get_chat_view()
        for key in users:
            profile.add_new_contact_button.click()
            self.home.just_fyi('Checking %s case' % key)
            if 'scanning' in key:
                chat.scan_contact_code_button.click()
                if chat.allow_button.is_element_displayed():
                    chat.allow_button.click()
                chat.enter_qr_edit_box.scan_qr(users[key]['contact_code'])
            else:
                chat.public_key_edit_box.click()
                chat.public_key_edit_box.send_keys(users[key]['contact_code'])
                if 'nickname' in users[key]:
                    chat.nickname_input_field.send_keys(users[key]['nickname'])
                chat.confirm_until_presence_of_element(profile.add_new_contact_button)
            if not profile.element_by_text(users[key]['username']).is_element_displayed():
                self.errors.append('In %s case username not found in contact view after scanning' % key)
            if 'nickname' in users[key]:
                if not profile.element_by_text(users[key]['nickname']).is_element_displayed():
                    self.errors.append('In %s case nickname %s not found in contact view after scanning' %
                                       (key, users[key]['nickname']))

        self.home.just_fyi('Remove contact and check that it disappeared')
        user_to_remove = '@%s' % ens_user['ens']
        profile.element_by_text(user_to_remove).click()
        chat.remove_from_contacts.click()
        chat.close_button.click()
        if profile.element_by_text(user_to_remove).is_element_displayed():
            self.errors.append('Removed user is still shown in contact view')

        self.home.just_fyi(
            'Relogin and open profile view of the contact removed from Contact list to ensure there is no crash')
        profile.profile_button.click()
        profile.relogin()
        one_to_one_chat = self.home.add_contact(public_key=ens_user['ens'], add_in_contacts=False)
        one_to_one_chat.chat_options.click()
        profile = one_to_one_chat.view_profile_button.click()
        if profile.remove_from_contacts.is_element_displayed():
            self.errors.append('User still added in contact after relogin')

        self.errors.verify_no_errors()

    @marks.testrail_id(702166)
    def test_profile_add_custom_network(self):
        self.home.get_back_to_home_view()
        rpc, name, id, symbol = 'https://polygon-rpc.com/', 'Polygon', '137', 'MATIC'
        profile = self.home.profile_button.click()
        profile.add_custom_network(rpc_url=rpc, netwrok_id=id, symbol=symbol, name=name)
        self.sign_in.sign_in()
        wallet = self.home.wallet_button.click()
        if not wallet.element_by_text_part(symbol).is_element_displayed():
            self.errors.append("No %s currency is shown when switching to custom network" % symbol)
        self.home.profile_button.click()
        profile.advanced_button.click()
        profile.network_settings_button.scroll_to_element(10, 'up')
        if not profile.element_by_text_part(name).is_element_displayed():
            self.driver.fail("Custom network %s was not added!" % name)
        profile.get_back_to_home_view()
        # Switching back to Goerli for the next cases
        profile.switch_network('Goerli with upstream RPC')
        self.errors.verify_no_errors()

    @marks.testrail_id(702164)
    def test_profile_backup_of_contacts(self):
        self.home.get_back_to_home_view()
        self.home.just_fyi('Add user to contacts')
        chat = self.home.add_contact(basic_user['public_key'])

        self.home.just_fyi('Add nickname to contact')
        nickname = 'test user'
        chat.chat_options.click()
        chat.view_profile_button.click()
        chat.set_nickname(nickname)
        self.home.back_button.click()

        self.home.just_fyi('Create community chats')
        community_name = 'test community'
        community_description, community_pic = "test community description", 'sauce_logo.png'
        self.home.create_community_e2e(community_name, community_description, set_image=True, file_name=community_pic)
        self.home.home_button.double_click()

        self.home.just_fyi('Add ENS-user to contacts')
        user_ens = 'ensmessenger'
        self.home.add_contact(user_ens)
        self.home.back_button.click()

        self.home.just_fyi('Block user')
        self.home.add_contact(chat_users['A']['public_key'], add_in_contacts=False)
        chat.chat_options.click()
        chat.view_profile_button.click()
        chat.block_contact()

        self.home.just_fyi('Add nickname to non-contact user')
        nickname1 = 'non-contact user'
        self.home.add_contact(chat_users['B']['public_key'], add_in_contacts=False)
        chat.chat_options.click()
        chat.view_profile_button.click()
        chat.set_nickname(nickname1)

        self.home.just_fyi('Perform backup')
        profile = self.home.profile_button.click()
        profile.sync_settings_button.click()
        profile.backup_settings_button.click()
        profile.perform_backup_button.click()

        profile.just_fyi('Backup seed phrase')
        profile.get_back_to_home_view()
        profile.privacy_and_security_button.click()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()
        self.drivers[0].reset()

        profile.just_fyi('Recover account from seed phrase')
        self.sign_in.recover_access(' '.join(recovery_phrase.values()))

        self.sign_in.just_fyi('Check backup of community')
        if not self.home.element_by_text(community_name).is_element_displayed():
            self.errors.append('Community was not backed up')

        self.sign_in.just_fyi('Check backup of contact with nickname')
        profile.profile_button.click()
        profile.contacts_button.click()
        profile.wait_for_element_starts_with_text('Blocked users')
        if not profile.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname of contact was not backed up')

        self.sign_in.just_fyi('Check backup of ENS contact')
        if not profile.element_by_text('@%s' % user_ens).is_element_displayed():
            self.errors.append('ENS contact was not backed up')

        self.sign_in.just_fyi('Check backup of blocked user')
        profile.blocked_users_button.click()
        if not profile.element_by_text(chat_users['A']['username']).is_element_displayed():
            self.errors.append('Blocked user was not backed up')
        profile.get_back_to_home_view()

        self.sign_in.just_fyi('Check backup of nickname for non-contact user')
        self.home.home_button.double_click()
        self.home.add_contact(chat_users['B']['public_key'], add_in_contacts=False)
        if not chat.element_by_text(nickname1).is_element_displayed():
            self.errors.append("Nickname of non-contact user was not backed up")

        self.errors.verify_no_errors()

    @marks.testrail_id(702165)
    def test_profile_change_log_level(self):
        self.home.get_back_to_home_view()

        profile = self.home.profile_button.click()
        profile.advanced_button.click()
        default_log_level = 'DEBUG'
        if not profile.element_by_text(default_log_level).is_element_displayed():
            self.errors.append('%s is not selected by default' % default_log_level)
        if self.home.find_values_in_geth('lvl=trce'):
            self.errors.append('"%s" is set, but found another entries!' % default_log_level)

        self.home.just_fyi('Set another loglevel and check that changes are applied')
        profile.log_level_setting_button.click()
        changed_log_level = 'TRACE'
        profile.element_by_text(changed_log_level).click_until_presence_of_element(profile.confirm_button)
        profile.confirm_button.click()
        self.sign_in.sign_in()
        self.home.profile_button.click()
        profile.advanced_button.click()
        if not profile.element_by_text(changed_log_level).is_element_displayed():
            self.errors.append('"%s" is not selected after change' % changed_log_level)
        if not self.home.find_values_in_geth('lvl=trc'):
            self.errors.append('"%s" is set, but no entries are found!' % changed_log_level)

        profile.get_back_to_home_view()

        self.errors.verify_no_errors()

    @marks.testrail_id(702178)
    def test_profile_change_fleet(self):

        profile = self.home.profile_button.click()
        profile.advanced_button.click()

        if not profile.element_by_text(used_fleet).is_element_displayed():
            self.errors.append('%s is not selected by default' % used_fleet)

        self.home.just_fyi('Set another fleet and check that changes are applied')
        profile.fleet_setting_button.click()
        changed_fleet = 'wakuv2.prod'
        profile.element_by_text(changed_fleet).click_until_presence_of_element(profile.confirm_button)
        profile.confirm_button.click()
        self.sign_in.sign_in()
        self.home.profile_button.click()
        profile.advanced_button.click()
        if not profile.element_by_text(changed_fleet).is_element_displayed():
            self.errors.append('"%s" fleet is not selected after change' % changed_fleet)
        if not self.home.find_values_in_geth(changed_fleet):
            self.errors.append('"%s" is set, but no entry is found!' % changed_fleet)

        profile.get_back_to_home_view()

        self.errors.verify_no_errors()

    # This case should always be the last in group as it changes common password
    @marks.testrail_id(702161)
    def test_profile_can_reset_password(self):
        self.home.get_back_to_home_view()

        new_password = basic_user['special_chars_password']
        profile = self.home.profile_button.click()
        profile.privacy_and_security_button.click()

        profile.just_fyi("Check that can not reset password when entering wrong current password")
        profile.reset_password_button.click()
        profile.current_password_edit_box.send_keys(common_password + '1')
        profile.new_password_edit_box.send_keys(new_password)
        profile.confirm_new_password_edit_box.send_keys(new_password)
        profile.next_button.click()
        if not profile.current_password_wrong_text.is_element_displayed():
            self.errors.append("Validation error for wrong current password is not shown")

        profile.just_fyi("Check that can not procced if did not confirm new password")
        profile.current_password_edit_box.clear()
        profile.current_password_edit_box.send_keys(common_password)
        profile.new_password_edit_box.send_keys(new_password)
        profile.confirm_new_password_edit_box.send_keys(new_password + '1')
        profile.next_button.click()

        profile.just_fyi("Delete last symbol and check that can reset password")
        profile.confirm_new_password_edit_box.delete_last_symbols(1)
        profile.next_button.click()
        profile.element_by_translation_id("password-reset-success").wait_for_element(30)
        profile.element_by_translation_id("okay").click()

        profile.just_fyi("Login with new password")
        self.sign_in.sign_in(password=new_password)
        if not self.sign_in.home_button.is_element_displayed():
            self.errors.append("Could not sign in with new password after reset")

        self.errors.verify_no_errors()
