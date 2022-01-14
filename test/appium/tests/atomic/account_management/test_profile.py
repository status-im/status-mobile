import re

from tests import marks, bootnode_address, mailserver_address, test_dapp_url, test_dapp_name, mailserver_ams, \
    mailserver_gc, mailserver_hk, used_fleet, common_password
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten, user_mainnet
from views.sign_in_view import SignInView
from time import time


class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(6318)
    @marks.medium
    def test_can_delete_several_multiaccounts(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        delete_alert_warning = sign_in.get_translation_by_key("delete-profile-warning")
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.back_button.click()
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.confirm_your_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.lets_go_button)
        sign_in.lets_go_button.click()

        sign_in.just_fyi('Delete 2nd multiaccount')
        public_key, username = sign_in.get_public_key_and_username(return_username=True)
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        for text in (username, delete_alert_warning):
            if not profile.element_by_text(text).is_element_displayed():
                self.errors.append('Required %s is not shown when deleting multiaccount' % text)
        profile.delete_profile_button.click()
        if profile.element_by_translation_id("profile-deleted-title").is_element_displayed():
            self.driver.fail('Profile is deleted without confirmation with password')
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click_until_presence_of_element(
            profile.element_by_translation_id("profile-deleted-title"))
        profile.ok_button.click()

        sign_in.just_fyi('Delete last multiaccount')
        sign_in.sign_in()
        sign_in.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click()
        profile.ok_button.click()
        if not sign_in.get_started_button.is_element_displayed(20):
            self.errors.append('No redirected to carousel view after deleting last multiaccount')
        self.errors.verify_no_errors()

    @marks.testrail_id(695890)
    @marks.medium
    def test_can_use_another_fleets_and_networks_advanced_set_nonce(self):
        user = user_mainnet
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])

        home.just_fyi("Check that can enable all toggles and still login successfully")
        profile = home.profile_button.click()
        profile.advanced_button.click()
        profile.transaction_management_enabled_toggle.click()
        profile.webview_debug_toggle.click()
        profile.waku_bloom_toggle.click()
        sign_in.sign_in()

        home.just_fyi("Check tx management")
        wallet = home.wallet_button.click()
        send_tx = wallet.send_transaction_from_main_screen.click()
        from views.send_transaction_view import SendTransactionView
        send_tx = SendTransactionView(self.driver)
        send_tx.amount_edit_box.set_value('0')
        send_tx.set_recipient_address(ens_user['address'])
        send_tx.next_button.click()
        send_tx.set_up_wallet_when_sending_tx()
        send_tx.advanced_button.click()
        send_tx.nonce_input.set_value('4')
        send_tx.nonce_save_button.click()
        error_text = send_tx.sign_transaction(error=True)
        if error_text != 'nonce too low':
            self.errors.append("%s is not expected error when signing tx with custom nonce" % error_text)

        home.just_fyi("Check balance on mainnet")
        profile = home.profile_button.click()
        profile.switch_network()
        wallet = home.wallet_button.click()
        wallet.scan_tokens()
        [wallet.wait_balance_is_equal_expected_amount(asset, value) for asset, value in user['mainnet'].items()]

        home.just_fyi("Check balance on xDai and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('xDai Chain')
        home.wallet_button.click()
        wallet.element_by_text(user['xdai']).wait_for_element(30)

        home.just_fyi("Check balance on BSC and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('BSC Network')
        home.wallet_button.click()
        wallet.element_by_text(user['bsc']).wait_for_element(30)

        self.errors.verify_no_errors()

    @marks.testrail_id(5323)
    @marks.critical
    def test_share_copy_contact_code_and_wallet_address(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()

        home.just_fyi("Copying contact code")
        profile.share_my_profile_button.click()
        public_key = profile.public_key_text.text
        profile.public_key_text.long_press_element()
        profile.copy_text()

        home.just_fyi("Sharing contact code via messenger")
        profile.share_button.click()
        profile.share_via_messenger()
        if not profile.element_by_text_part(public_key).is_element_present():
            self.errors.append("Can't share public key")
        [profile.click_system_back_button() for _ in range(2)]
        profile.close_share_popup()

        home.just_fyi("Check that can paste contact code in chat message input")
        home = profile.home_button.click()
        chat = home.add_contact(transaction_senders['M']['public_key'])
        chat.chat_message_input.click()
        chat.paste_text()
        input_text = chat.chat_message_input.text
        if input_text not in public_key or len(input_text) < 1:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        chat.get_back_to_home_view()

        home.just_fyi("Copying wallet address")
        wallet = profile.wallet_button.click()
        wallet.accounts_status_account.click()
        request = wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        request.share_button.click()
        request.element_by_translation_id("sharing-copy-to-clipboard").click()

        home.just_fyi("Sharing wallet address via messenger")
        request.share_button.click()
        wallet.share_via_messenger()
        if not wallet.element_by_text_part(address).is_element_present():
            self.errors.append("Can't share address")
        [wallet.click_system_back_button() for _ in range(2)]
        wallet.close_share_popup()

        home.just_fyi("Check that can paste wallet address in chat message input")
        wallet.home_button.click()
        home.get_chat(transaction_senders['M']['username']).click()
        chat.chat_message_input.click()
        chat.paste_text()
        if chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.errors.verify_no_errors()

    @marks.testrail_id(5502)
    @marks.critical
    def test_can_add_existing_ens_on_mainnet(self):
        home = SignInView(self.driver).recover_access(ens_user['passphrase'])
        profile = home.profile_button.click()

        profile.just_fyi('check if your name can be added via "ENS usernames" in Profile')
        profile.switch_network()
        home.profile_button.click()
        profile.connect_existing_ens(ens_user['ens'])

        profile.just_fyi('check that after adding username is shown in "ENS usernames" and profile')
        if not profile.element_by_text(ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own "ENS usernames" after adding')
        profile.back_button.click()
        if not profile.element_by_text('@%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        if not profile.element_by_text('%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        profile.share_my_profile_button.click()
        if profile.ens_name_in_share_chat_key_text.text != '%s' % ens_user['ens']:
            self.errors.append('No ENS name is shown on tapping on share icon in Profile')
        profile.close_share_popup()

        self.errors.verify_no_errors()

    @marks.testrail_id(695850)
    @marks.medium
    def test_can_reset_password(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        new_password = basic_user['special_chars_password']
        profile = home.profile_button.click()
        profile.privacy_and_security_button.click()

        profile.just_fyi("Check that can not reset password when entering wrong current password")
        profile.reset_password_button.click()
        profile.current_password_edit_box.send_keys(common_password + '1')
        profile.new_password_edit_box.set_value(new_password)
        profile.confirm_new_password_edit_box.set_value(new_password)
        profile.next_button.click()
        if not profile.current_password_wrong_text.is_element_displayed():
            self.errors.append("Validation error for wrong current password is not shown")

        profile.just_fyi("Check that can not procced if did not confirm new password")
        profile.current_password_edit_box.clear()
        profile.current_password_edit_box.set_value(common_password)
        profile.new_password_edit_box.set_value(new_password)
        profile.confirm_new_password_edit_box.set_value(new_password + '1')
        profile.next_button.click()

        profile.just_fyi("Delete last symbol and check that can reset password")
        profile.confirm_new_password_edit_box.delete_last_symbols(1)
        profile.next_button.click()
        profile.element_by_translation_id("password-reset-success").wait_for_element(30)
        profile.element_by_translation_id("okay").click()

        profile.just_fyi("Login with new password")
        sign_in.sign_in(password=new_password)
        if not sign_in.home_button.is_element_displayed():
            self.errors.append("Could not sign in with new password after reset")

        self.errors.verify_no_errors()

    @marks.testrail_id(6296)
    @marks.high
    def test_recover_account_from_new_user_seedphrase(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = " ".join(profile.get_recovery_phrase().values())
        profile.close_button.click()
        profile.back_button.click()
        public_key = profile.get_public_key_and_username()
        wallet = profile.wallet_button.click()
        address = wallet.get_wallet_address()
        home.profile_button.click()
        profile.logout()
        self.driver.reset()
        SignInView(self.driver).recover_access(recovery_phrase)
        wallet = home.wallet_button.click()
        if wallet.get_wallet_address() != address:
            self.driver.fail("Seed phrase displayed in new accounts for back up does not recover respective address")
        profile = wallet.profile_button.click()
        if profile.get_public_key_and_username() != public_key:
            self.driver.fail("Seed phrase displayed in new accounts for back up does not recover respective public key")

    @marks.testrail_id(5433)
    @marks.medium
    def test_invite_friends(self):
        home = SignInView(self.driver).create_user()

        self.driver.info("Check it via 'Invite friends' on home view")
        home.invite_friends_button.click()
        home.share_via_messenger()
        home.element_by_text_part("Hey join me on Status: https://join.status.im/u/0x")
        home.click_system_back_button()

        self.driver.info("Check it via bottom sheet menu")
        home.plus_button.click()
        home.chats_menu_invite_friends_button.click()
        home.share_via_messenger()
        home.element_by_text_part("Hey join me on Status: https://join.status.im/u/0x")

    @marks.testrail_id(6312)
    @marks.medium
    def test_add_remove_contact_via_contacts_view(self):
        home = SignInView(self.driver).create_user()

        home.just_fyi('Check empty contacts view')
        profile = home.profile_button.click()
        profile.contacts_button.click()
        if not profile.add_new_contact_button.is_element_displayed():
            self.driver.fail('No expected element on contacts view')

        users = {
            'scanning_ens_with_stateofus_domain_deep_link': {
                'contact_code': 'https://join.status.im/u/%s.stateofus.eth' % ens_user_ropsten['ens'],
                'username': ens_user_ropsten['username']
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
                'contact_code': ens_user['ens_another'],
                'username': '@%s' % ens_user['ens_another'],
                'nickname': 'my_dear_friend'
            },

        }

        home.just_fyi('Add contact  and check that they appear in Contacts view')
        chat = home.get_chat_view()
        for key in users:
            profile.add_new_contact_button.click()
            home.just_fyi('Checking %s case' % key)
            if 'scanning' in key:
                chat.scan_contact_code_button.click()
                if chat.allow_button.is_element_displayed():
                    chat.allow_button.click()
                chat.enter_qr_edit_box.scan_qr(users[key]['contact_code'])
            else:
                chat.public_key_edit_box.click()
                chat.public_key_edit_box.send_keys(users[key]['contact_code'])
                if 'nickname' in users[key]:
                    chat.nickname_input_field.set_value(users[key]['nickname'])
                chat.confirm_until_presence_of_element(profile.add_new_contact_button)
            if not profile.element_by_text(users[key]['username']).is_element_displayed():
                self.errors.append('In %s case username not found in contact view after scanning' % key)
            if 'nickname' in users[key]:
                if not profile.element_by_text(users[key]['nickname']).is_element_displayed():
                    self.errors.append('In %s case nickname %s not found in contact view after scanning' % (key,
                                                                                                            users[key]['nickname']))

        home.just_fyi('Remove contact and check that it disappeared')
        user_to_remove = '@%s' % ens_user['ens_another']
        profile.element_by_text(user_to_remove).click()
        chat.remove_from_contacts.click()
        chat.close_button.click()
        if profile.element_by_text(user_to_remove).is_element_displayed():
            self.errors.append('Removed user is still shown in contact view')

        home.just_fyi(
            'Relogin and open profile view of the contact removed from Contact list to ensure there is no crash')
        profile.profile_button.click()
        profile.relogin()
        one_to_one_chat = home.add_contact(public_key=ens_user['ens_another'], add_in_contacts=False)
        one_to_one_chat.chat_options.click()
        profile = one_to_one_chat.view_profile_button.click()
        if profile.remove_from_contacts.is_element_displayed():
            self.errors.append('User still added in contact after relogin')

        self.errors.verify_no_errors()

    @marks.testrail_id(5431)
    @marks.medium
    def test_add_custom_network(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.add_custom_network()
        sign_in.sign_in()
        sign_in.profile_button.click()
        profile.advanced_button.click()
        profile.network_settings_button.scroll_to_element(10, 'up')
        if not profile.element_by_text_part('custom_ropsten').is_element_displayed():
            self.driver.fail("Network custom_ropsten was not added!")

    @marks.critical
    @marks.testrail_id(5419)
    @marks.flaky
    def test_logcat_backup_recovery_phrase(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()

        home.just_fyi("Check that badge on profile about back up seed phrase is presented")
        if home.profile_button.counter.text != '1':
            self.errors.append('Profile button counter is not shown')

        home.just_fyi("Back up seed phrase and check logcat")
        profile = home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.backup_recovery_phrase_button.click()
        profile.ok_continue_button.click()
        recovery_phrase = profile.get_recovery_phrase()
        profile.next_button.click()
        word_number = profile.recovery_phrase_word_number.number
        profile.recovery_phrase_word_input.set_value(recovery_phrase[word_number])
        profile.next_button.click()
        word_number_1 = profile.recovery_phrase_word_number.number
        profile.recovery_phrase_word_input.set_value(recovery_phrase[word_number_1])
        profile.done_button.click()
        profile.yes_button.click()
        profile.ok_got_it_button.click()
        if home.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after recovery phrase backup')
        values_in_logcat = profile.find_values_in_logcat(passphrase1=recovery_phrase[word_number],
                                                         passphrase2=recovery_phrase[word_number_1])
        if len(values_in_logcat) == 2:
            self.driver.fail(values_in_logcat)
        profile.profile_button.double_click()

        home.just_fyi(
            "Try to restore same account from seed phrase (should be possible only to unlock existing account)")
        profile.logout()
        sign_in.back_button.click()
        sign_in.access_key_button.click()
        sign_in.enter_seed_phrase_button.click()
        sign_in.seedphrase_input.click()
        sign_in.seedphrase_input.set_value(' '.join(recovery_phrase.values()))
        sign_in.next_button.click()
        sign_in.element_by_translation_id(translation_id="unlock", uppercase=True).click()
        sign_in.password_input.set_value(common_password)
        chat = sign_in.sign_in_button.click()
        chat.plus_button.click()
        if not chat.start_new_chat_button.is_element_displayed():
            self.errors.append("Can't proceed using account after it's re-recover twice.")
        self.errors.verify_no_errors()

    @marks.testrail_id(5453)
    @marks.medium
    def test_privacy_policy_terms_of_use_node_version_need_help_in_profile(self):
        signin = SignInView(self.driver)
        no_link_found_error_msg = 'Could not find privacy policy link at'
        no_link_open_error_msg = 'Could not open our privacy policy from'
        no_link_tos_error_msg = 'Could not open Terms of Use from'

        signin.just_fyi("Checking privacy policy and TOS links")
        if not signin.privacy_policy_link.is_element_present():
            self.errors.append('%s Sign in view!' % no_link_found_error_msg)
        if not signin.terms_of_use_link.is_element_displayed():
            self.driver.fail("No Terms of Use link on Sign in view!")

        home = signin.create_user()
        profile = home.profile_button.click()
        profile.about_button.click()
        profile.privacy_policy_button.click()
        from views.web_views.base_web_view import BaseWebView
        web_page = BaseWebView(self.driver)
        if not web_page.policy_summary.is_element_displayed():
            self.errors.append('%s Profile about view!' % no_link_open_error_msg)
        web_page.click_system_back_button()

        profile.terms_of_use_button.click()
        web_page.wait_for_d_aap_to_load()
        web_page.swipe_by_custom_coordinates(0.5, 0.8, 0.5, 0.4)
        if not web_page.terms_of_use_summary.is_element_displayed(30):
            self.errors.append('%s Profile about view!' % no_link_tos_error_msg)
        web_page.click_system_back_button()

        signin.just_fyi("Checking that version match expected format and can be copied")
        app_version = profile.app_version_text.text
        node_version = profile.node_version_text.text
        if not re.search(r'\d[.]\d{1,2}[.]\d{1,2}\s[(]\d*[)]', app_version):
            self.errors.append("App version %s didn't match expected format" % app_version)
        if not re.search(r'StatusIM/v.*/android-\d{3}/go\d[.]\d+', node_version):
            self.errors.append("Node version %s didn't match expected format" % node_version)
        profile.app_version_text.click()
        profile.back_button.click()
        profile.home_button.click()
        chat = home.join_public_chat(home.get_random_chat_name())
        message_input = chat.chat_message_input
        message_input.paste_text_from_clipboard()
        if message_input.text != app_version:
            self.errors.append('Version number was not copied to clipboard')

        signin.just_fyi("Checking Need help section")
        home.profile_button.double_click()
        profile.help_button.click()
        web_page = profile.faq_button.click()
        web_page.open_in_webview()
        web_page.wait_for_d_aap_to_load()
        if not profile.element_by_text_part("F.A.Q").is_element_displayed(30):
            self.errors.append("FAQ is not shown")
        profile.click_system_back_button()
        profile.submit_bug_button.click()

        signin.just_fyi("Checking bug submitting form")
        profile.bug_description_edit_box.set_value('1234')
        profile.bug_submit_button.click()
        if not profile.element_by_translation_id("bug-report-too-short-description").is_element_displayed():
            self.errors.append("Can submit big with too short description!")
        profile.bug_description_edit_box.clear()
        [field.set_value("Something wrong happened!!") for field in
         (profile.bug_description_edit_box, profile.bug_steps_edit_box)]
        profile.bug_submit_button.click()
        if not profile.element_by_text_part("Welcome to Gmail").is_element_displayed(30):
            self.errors.append("Mail client is not opened when submitting bug")
        profile.click_system_back_button(2)

        signin.just_fyi("Checking request feature")
        profile.request_a_feature_button.click()
        if not profile.element_by_text("#support").is_element_displayed(30):
            self.errors.append("Support channel is not suggested for requesting a feature")
        self.errors.verify_no_errors()

    @marks.testrail_id(5738)
    @marks.high
    def test_dapps_permissions(self):
        home = SignInView(self.driver).create_user()
        account_name = home.status_account_name

        home.just_fyi('open Status Test Dapp, allow all and check permissions in Profile')
        web_view = home.open_status_test_dapp()
        dapp = home.dapp_tab_button.click()
        profile = home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        profile.element_by_text(test_dapp_name).click()
        if not profile.element_by_text(account_name).is_element_displayed():
            self.errors.append('Wallet permission was not granted')
        if not profile.element_by_translation_id("chat-key").is_element_displayed():
            self.errors.append('Contact code permission was not granted')

        profile.just_fyi('revoke access and check that they are asked second time')
        profile.revoke_access_button.click()
        profile.back_button.click()
        profile.dapp_tab_button.click()

        web_view.open_tabs_button.click()
        web_view.empty_tab_button.click()

        dapp.open_url(test_dapp_url)
        if not dapp.element_by_text_part(account_name).is_element_displayed():
            self.errors.append('Wallet permission is not asked')
        if dapp.allow_button.is_element_displayed():
            dapp.allow_button.click(times_to_click=1)
        if not dapp.element_by_translation_id("your-contact-code").is_element_displayed():
            self.errors.append('Profile permission is not asked')
        self.errors.verify_no_errors()

    @marks.testrail_id(5368)
    @marks.medium
    def test_change_log_level_and_fleet(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()
        profile.advanced_button.click()
        default_log_level = 'INFO'
        for text in default_log_level, used_fleet:
            if not profile.element_by_text(text).is_element_displayed():
                self.errors.append('%s is not selected by default' % text)
        if home.find_values_in_geth('lvl=trce', 'lvl=dbug'):
            self.errors.append('"%s" is set, but found another entries!' % default_log_level)
        if not home.find_values_in_geth('lvl=info'):
            self.errors.append('"%s" is set, but no entries are found!' % default_log_level)

        home.just_fyi('Set another loglevel and check that changes are applied')
        profile.log_level_setting_button.click()
        changed_log_level = 'TRACE'
        profile.element_by_text(changed_log_level).click_until_presence_of_element(profile.confirm_button)
        profile.confirm_button.click()
        SignInView(self.driver).sign_in()
        home.profile_button.click()
        profile.advanced_button.click()
        if not profile.element_by_text(changed_log_level).is_element_displayed():
            self.errors.append('"%s" is not selected after change' % changed_log_level)
        if not home.find_values_in_geth('lvl=trc'):
            self.errors.append('"%s" is set, but no entries are found!' % changed_log_level)

        home.just_fyi('Set another fleet and check that changes are applied')
        profile.fleet_setting_button.click()
        changed_fleet = 'wakuv2.prod'
        profile.element_by_text(changed_fleet).click_until_presence_of_element(profile.confirm_button)
        profile.confirm_button.click()
        SignInView(self.driver).sign_in()
        home.profile_button.click()
        profile.advanced_button.click()
        if not profile.element_by_text(changed_fleet).is_element_displayed():
            self.errors.append('"%s" fleet is not selected after change' % changed_fleet)
        if not home.find_values_in_geth(changed_fleet):
            self.errors.append('"%s" is set, but no entry is found!' % changed_fleet)

        self.errors.verify_no_errors()

    @marks.testrail_id(5766)
    @marks.medium
    @marks.flaky
    def test_use_pinned_mailserver(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()

        profile.just_fyi('pin history node')
        profile.sync_settings_button.click()
        node_gc, node_ams, node_hk = [profile.return_mailserver_name(history_node_name, used_fleet) for
                                      history_node_name in (mailserver_gc, mailserver_ams, mailserver_hk)]
        h_node = node_ams
        profile.mail_server_button.click()
        profile.mail_server_auto_selection_button.click()
        profile.mail_server_by_name(h_node).click()
        profile.confirm_button.click()
        if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
            h_node = node_hk
            profile.element_by_translation_id("mailserver-pick-another", uppercase=True).click()
            profile.mail_server_by_name(h_node).click()
            profile.confirm_button.click()
            if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
                self.driver.fail("Couldn't connect to any history node")

        profile.just_fyi('check that history node is pinned')
        profile.close_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)
        profile.home_button.click()

        profile.just_fyi('Relogin and check that settings are preserved')
        home.relogin()
        home.profile_button.click()
        profile.sync_settings_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)

        self.errors.verify_no_errors()

    @marks.testrail_id(6219)
    @marks.medium
    def test_set_primary_ens_custom_domain(self):
        home = SignInView(self.driver).recover_access(ens_user['passphrase'])
        ens_second, ens_main = ens_user['ens_another'], ens_user['ens']

        home.just_fyi('add 2 ENS names in Profile')
        profile = home.profile_button.click()
        dapp = profile.connect_existing_ens(ens_main)
        profile.element_by_translation_id("ens-add-username").wait_and_click()
        profile.element_by_translation_id("ens-want-custom-domain").wait_and_click()
        dapp.ens_name_input.set_value(ens_second)
        dapp.check_ens_name.click_until_presence_of_element(dapp.element_by_translation_id("ens-got-it"))
        dapp.element_by_translation_id("ens-got-it").wait_and_click()

        home.just_fyi('check that by default %s ENS is set' % ens_main)
        dapp.element_by_translation_id("ens-primary-username").click()
        message_to_check = 'Your messages are displayed to others with'
        if not dapp.element_by_text('%s\n@%s' % (message_to_check, ens_main)).is_element_displayed():
            self.errors.append('%s ENS username is not set as primary by default' % ens_main)

        home.just_fyi('check view in chat settings ENS from other domain: %s after set new primary ENS' % ens_second)
        dapp.set_primary_ens_username(ens_second).click()
        if profile.username_in_ens_chat_settings_text.text != '@' + ens_second:
            self.errors.append('ENS username %s is not shown in ENS username Chat Settings after enabling' % ens_second)
        self.errors.verify_no_errors()


class TestProfileMultipleDevice(MultipleDeviceTestCase):
    @marks.testrail_id(6646)
    @marks.high
    def test_set_profile_picture(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user()
        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        public_key_1, public_key_2 = profile_1.get_public_key_and_username(), profile_2.get_public_key_and_username()
        profile_2.home_button.click()

        profile_1.just_fyi("Set user Profile image from Gallery")
        profile_1.edit_profile_picture(file_name='sauce_logo.png')
        home_1.profile_button.click()
        profile_1.swipe_down()

        if not profile_1.profile_picture.is_element_image_similar_to_template('sauce_logo_profile.png'):
            self.drivers[0].fail('Profile picture was not updated')

        profile_1.just_fyi("Add user2 to contacts")
        profile_1.home_button.click()
        home_1.add_contact(public_key_2)
        home_1.home_button.click()

        profile_1.just_fyi("Check user profile updated in chat")
        message = "Text message"
        public_chat_name = home_1.get_random_chat_name()
        home_2.add_contact(public_key=public_key_1)
        home_2.home_button.click()
        public_chat_2 = home_2.join_public_chat(public_chat_name)
        public_chat_1 = home_1.join_public_chat(public_chat_name)
        public_chat_1.chat_message_input.send_keys(message)
        public_chat_1.send_message_button.click()
        if not public_chat_2.chat_element_by_text(message).member_photo.is_element_image_similar_to_template(
                'sauce_logo.png'):
            self.drivers[0].fail('Profile picture was not updated in chat')

        profile_1.just_fyi("Set user Profile image by taking Photo")
        home_1.profile_button.click()
        profile_1.edit_profile_picture(file_name='sauce_logo.png', update_by='Make Photo')
        home_1.home_button.click(desired_view='chat')
        public_chat_1.chat_message_input.send_keys(message)
        public_chat_1.send_message_button.click()

        if public_chat_2.chat_element_by_text(message).member_photo.is_element_image_similar_to_template(
                'sauce_logo.png'):
            self.drivers[0].fail('Profile picture was not updated in chat after making photo')

    @marks.testrail_id(6636)
    @marks.medium
    @marks.flaky
    def test_show_profile_picture_of_setting(self):
        self.create_drivers(2)
        home_1, home_2 = SignInView(self.drivers[0]).create_user(), SignInView(self.drivers[1]).create_user(
            enable_notifications=True)
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
        one_to_one_chat_2.chat_message_input.set_value('@' + default_username_1)
        one_to_one_chat_2.chat_message_input.click()
        if not one_to_one_chat_2.user_profile_image_in_mentions_list(
                default_username_1).is_element_image_similar_to_template(logo_default):
            self.errors.append('Profile picture was not updated in 1-1 chat mentions list')
        one_to_one_chat_2.get_back_to_home_view()

        profile_1.just_fyi('Check profile image is updated in Group chat view')
        profile_2 = one_to_one_chat_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()

        if not profile_2.profile_picture.is_element_image_similar_to_template(logo_online):
            self.errors.append('Profile picture was not updated on user Profile view')
        profile_2.close_button.click()
        [home.home_button.click() for home in (profile_2, home_1)]
        if not home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_chats):
            self.errors.append('User profile picture was not updated on Chats view')

        profile_1.just_fyi('Check profile image updated in user profile view in Group chat views')
        group_chat_name, group_chat_message = 'new_group_chat', 'Trololo'
        group_chat_2 = home_2.create_group_chat(user_names_to_add=[default_username_1])
        group_chat_2.send_message('Message')
        group_chat_1 = home_1.get_chat(group_chat_name).click()
        group_chat_1.join_chat_button.click()
        group_chat_1.send_message(group_chat_message)
        if not group_chat_2.chat_element_by_text(group_chat_message).member_photo.is_element_image_similar_to_template(
                logo_default):
            self.errors.append('User profile picture was not updated in message Group chat view')
        home_2.put_app_to_background()

        profile_1.just_fyi('Check profile image updated in group chat invite')
        home_1.get_back_to_home_view()
        new_group_chat = 'new_gr'
        home_2.click_system_back_button()
        home_2.open_notification_bar()
        home_1.create_group_chat(user_names_to_add=[default_username_2], group_chat_name=new_group_chat)

        invite = group_chat_2.pn_invited_to_group_chat(default_username_1, new_group_chat)
        home_2.get_pn(invite).wait_for_visibility_of_element(30)
        if not home_2.get_pn(invite).group_chat_icon.is_element_image_similar_to_template(logo_group):
            self.errors.append("Group chat invite is not updated with custom logo!")
        home_2.get_pn(invite).click()

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
        group_chat_2.profile_button.click()
        profile_2.contacts_button.click()
        profile_2.element_by_text(default_username_1).click()
        one_to_one_chat_2.remove_from_contacts.click()
        # Send message to User 2 so update of profile image picked up
        group_chat_1 = home_1.get_chat('new_group_chat').click()
        group_chat_1.send_message(group_chat_message)
        one_to_one_chat_2.close_button.click()
        one_to_one_chat_2.home_button.click(desired_view='home')
        if home_2.get_chat(default_username_1).chat_image.is_element_image_similar_to_template(logo_default):
            self.errors.append('User profile picture is not default to default after user removed from Contacts')

        profile_2.just_fyi('Enable to see profile image from "Everyone" setting')
        home_2.profile_button.click()
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
    def test_custom_bootnodes(self):
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
    @marks.medium
    @marks.flaky
    def test_add_switch_delete_custom_mailserver(self):
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
    @marks.medium
    @marks.flaky
    def test_can_not_connect_to_mailserver(self):
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
        profile_1.mail_server_address_input.set_value(mailserver_address.replace('4', '5'))
        profile_1.save_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()

        profile_1.just_fyi('check that popup "Error connecting" will not reappear if tap on "Cancel"')
        profile_1.element_by_translation_id('mailserver-error-title').wait_for_element(120)
        profile_1.cancel_button.click()
        profile_1.home_button.click()

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
        public_chat_1.relogin()

        # TODO: blocked due to 11786 (rechecked 23.11.21, valid)
        # profile_1.just_fyi('check that still connected to custom mailserver after relogin')
        # home_1.profile_button.click()
        # profile_1.sync_settings_button.click()
        # if not profile_1.element_by_text(server_name).is_element_displayed():
        #     self.drivers[0].fail("Not connected to custom mailserver after re-login")
        #
        # profile_1.just_fyi('check that can RETRY to connect')
        # profile_1.element_by_translation_id(id='mailserver-error-title').wait_for_element(60)
        # public_chat_1.element_by_translation_id(id='mailserver-retry', uppercase=True).wait_and_click(60)
        #
        # profile_1.just_fyi('check that can pick another mailserver and receive messages')
        # profile_1.element_by_translation_id(id='mailserver-error-title').wait_for_element(60)
        # profile_1.element_by_translation_id(id='mailserver-pick-another', uppercase=True).wait_and_click(120)
        # mailserver = profile_1.return_mailserver_name(mailserver_ams, used_fleet)
        # profile_1.element_by_text(mailserver).click()
        # profile_1.confirm_button.click()
        # profile_1.home_button.click()
        # home_1.get_chat('#%s' % public_chat_name).click()
        # if not public_chat_1.chat_element_by_text(message).is_element_displayed(60):
        #    self.errors.append("Chat history wasn't fetched")

        self.errors.verify_no_errors()

    @marks.testrail_id(6332)
    @marks.medium
    def test_disable_use_history_node(self):
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

    @marks.testrail_id(6226)
    @marks.critical
    def test_ens_mentions_pn_and_nickname_in_public_and_1_1_chats(self):
        self.create_drivers(2)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        user_1 = ens_user
        home_1 = sign_in_1.recover_access(user_1['passphrase'], enable_notifications=True)
        home_2 = sign_in_2.create_user()
        public_key_2, username_2 = home_2.get_public_key_and_username(return_username=True)
        home_2.home_button.double_click()
        profile_1 = sign_in_1.profile_button.click()
        profile_1.connect_existing_ens(user_1['ens'])

        home_1.just_fyi('check ENS name wallet address and public key')
        profile_1.element_by_text(user_1['ens']).click()
        for text in (user_1['address'].lower(), user_1['public_key']):
            if not profile_1.element_by_text_part(text).is_element_displayed(40):
                self.errors.append('%s text is not shown' % text)
        profile_1.home_button.click()

        home_2.just_fyi('joining same public chat, set ENS name and check it in chat from device2')
        chat_name = home_1.get_random_chat_name()
        [public_1, public_2] = [home.join_public_chat(chat_name) for home in (home_1, home_2)]
        public_1.home_button.double_click()
        home_1.profile_button.double_click()
        ens_name = '@' + user_1['ens']
        profile_1.element_by_translation_id("ens-your-your-name").click()
        if profile_1.username_in_ens_chat_settings_text.text != ens_name:
            self.errors.append('ENS username is not shown in ENS usernames Chat Settings after enabling')
        profile_1.back_button.click()
        profile_1.home_button.click()
        home_1.get_chat('#' + chat_name).click()
        message_text_2 = 'message test text 1'
        public_1.send_message(message_text_2)
        if not public_2.wait_for_element_starts_with_text(ens_name):
            self.errors.append('ENS username is not shown in public chat')
        home_1.put_app_to_background()

        home_2.just_fyi('check that can mention user with ENS name')
        public_2.select_mention_from_suggestion_list(user_1['ens'])
        if public_2.chat_message_input.text != ens_name + ' ':
            self.errors.append(
                'ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        public_2.send_message_button.click()
        public_2.element_starts_with_text(ens_name, 'button').click()
        for element in (public_2.element_by_text(user_1['username']), public_2.profile_add_to_contacts):
            if not element.is_element_displayed():
                self.errors.append('Was not redirected to user profile after tapping on mention!')

        home_1.just_fyi(
            'check that PN is received and after tap you are redirected to public chat, mention is highligted')
        home_1.open_notification_bar()
        home_1.element_by_text_part(username_2).click()
        if home_1.element_starts_with_text(user_1['ens']).is_element_differs_from_template('mentioned.png', 2):
            self.errors.append('Mention is not highlighted!')

        # Close Device1 driver session since it's not needed anymore
        self.drivers[0].quit()

        home_2.just_fyi(
            'check that ENS name is shown in 1-1 chat without adding user as contact in header, profile, options')
        one_to_one_2 = public_2.profile_send_message.click()
        if one_to_one_2.user_name_text.text != ens_name:
            self.errors.append('ENS username is not shown in 1-1 chat header')
        one_to_one_2.chat_options.click()
        if not one_to_one_2.element_by_text(ens_name).is_element_displayed():
            self.errors.append('ENS username is not shown in 1-1 chat options')
        one_to_one_2.view_profile_button.click()
        if not one_to_one_2.element_by_text(ens_name).is_element_displayed():
            self.errors.append('ENS username is not shown in user profile')

        home_2.just_fyi('add user to contacts and check that ENS name is shown in contact')
        one_to_one_2.profile_add_to_contacts.click()
        public_2.close_button.click()
        profile_2 = one_to_one_2.profile_button.click()
        profile_2.open_contact_from_profile(ens_name)

        home_2.just_fyi('set nickname and recheck username in 1-1 header, profile, options, contacts')
        nickname = 'test user' + str(round(time()))
        public_2.set_nickname(nickname)
        for name in (nickname, ens_name):
            if not profile_2.element_by_text(name).is_element_displayed():
                self.errors.append('%s is not shown in contact list' % name)
        profile_2.home_button.click(desired_view='chat')
        if one_to_one_2.user_name_text.text != nickname:
            self.errors.append('Nickname for user with ENS is not shown in 1-1 chat header')
        one_to_one_2.chat_options.click()
        if not one_to_one_2.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname for user with ENS is not shown in 1-1 chat options')

        home_2.just_fyi('check nickname in public chat')
        public_2.get_back_to_home_view()
        home_2.get_chat('#' + chat_name).click()
        chat_element = public_2.chat_element_by_text(message_text_2)
        chat_element.find_element()
        if chat_element.username.text != '%s %s' % (nickname, ens_name):
            self.errors.append('Nickname for user with ENS is not shown in public chat')

        self.errors.verify_no_errors()

    @marks.testrail_id(6228)
    @marks.high
    def test_mobile_data_usage_complex_settings(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1 = device_1.create_user()
        public_chat_name, public_chat_message = 'e2e-started-before', 'message to pub chat'
        public_1 = home_1.join_public_chat(public_chat_name)
        public_1.send_message(public_chat_message)

        home_1.just_fyi('set mobile data to "OFF" and check that peer-to-peer connection is still working')
        home_2 = device_2.create_user()
        home_2.toggle_mobile_data()
        home_2.mobile_connection_off_icon.wait_for_visibility_of_element(20)
        for element in home_2.continue_syncing_button, home_2.stop_syncing_button, home_2.remember_my_choice_checkbox:
            if not element.is_element_displayed(10):
                self.drivers[0].fail('Element %s is not not shown in "Syncing mobile" bottom sheet' % element.locator)
        home_2.stop_syncing_button.click()
        if not home_2.mobile_connection_off_icon.is_element_displayed():
            self.drivers[0].fail('No mobile connection OFF icon is shown')
        home_2.mobile_connection_off_icon.click()
        for element in home_2.connected_to_n_peers_text, home_2.waiting_for_wi_fi:
            if not element.is_element_displayed():
                self.errors.append("Element '%s' is not shown in Connection status bottom sheet" % element.locator)
        home_2.click_system_back_button()
        public_2 = home_2.join_public_chat(public_chat_name)
        if public_2.chat_element_by_text(public_chat_message).is_element_displayed(30):
            self.errors.append("Chat history was fetched with mobile data fetching off")
        public_chat_new_message = 'new message'
        public_1.send_message(public_chat_new_message)
        if not public_2.chat_element_by_text(public_chat_new_message).is_element_displayed(30):
            self.errors.append("Peer-to-peer connection is not working when  mobile data fetching is off")

        home_2.just_fyi('set mobile data to "ON"')
        home_2.home_button.click()
        home_2.mobile_connection_off_icon.click()
        home_2.use_mobile_data_switch.wait_and_click(30)
        if not home_2.connected_to_node_text.is_element_displayed(10):
            self.errors.append("Not connected to history node after enabling fetching on mobile data")
        home_2.click_system_back_button()
        home_2.mobile_connection_on_icon.wait_for_visibility_of_element(10)
        home_2.get_chat('#%s' % public_chat_name).click()
        if not public_2.chat_element_by_text(public_chat_message).is_element_displayed(180):
            self.errors.append("Chat history was not fetched with mobile data fetching ON")

        home_2.just_fyi('check redirect to sync settings by tapping on "Sync" in connection status bottom sheet')
        home_2.home_button.click()
        home_2.mobile_connection_on_icon.click()
        home_2.connection_settings_button.click()
        if not home_2.element_by_translation_id("mobile-network-use-mobile").is_element_displayed():
            self.errors.append("Was not redirected to sync settings after tapping on Settings in connection bottom sheet")

        home_1.just_fyi("Check default preferences in Sync settings")
        profile_1 = home_1.profile_button.click()
        profile_1.sync_settings_button.click()
        if not profile_1.element_by_translation_id("mobile-network-use-wifi").is_element_displayed():
            self.errors.append("Mobile data is enabled by default")
        profile_1.element_by_translation_id("mobile-network-use-wifi").click()
        if profile_1.ask_me_when_on_mobile_network.text != "ON":
            self.errors.append("'Ask me when on mobile network' is not enabled by default")

        profile_1.just_fyi("Disable 'ask me when on mobile network' and check that it is not shown")
        profile_1.ask_me_when_on_mobile_network.click()
        profile_1.toggle_mobile_data()
        if profile_1.element_by_translation_id("mobile-network-start-syncing").is_element_displayed(20):
            self.errors.append("Popup is shown, but 'ask me when on mobile network' is disabled")

        profile_1.just_fyi("Check 'Restore default' setting")
        profile_1.element_by_text('Restore Defaults').click()
        if profile_1.use_mobile_data.attribute_value("checked"):
            self.errors.append("Mobile data is enabled by default")
        if not profile_1.ask_me_when_on_mobile_network.attribute_value("checked"):
            self.errors.append("'Ask me when on mobile network' is not enabled by default")

        self.errors.verify_no_errors()

    @marks.testrail_id(695856)
    @marks.medium
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
        home_1.profile_button.click()
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
        [device.profile_button.click() for device in (profile_1, profile_2)]

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
