import re

from tests import marks, bootnode_address, mailserver_address, camera_access_error_text, \
    photos_access_error_text, test_dapp_url, test_dapp_name, mailserver_ams, mailserver_gc, \
    mailserver_hk, used_fleet, common_password, delete_alert_text
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests.users import transaction_senders, basic_user, ens_user, ens_user_ropsten
from views.sign_in_view import SignInView
from time import time


class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5302)
    @marks.high
    @marks.skip
    def test_set_profile_picture(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.edit_profile_picture(file_name='sauce_logo.png')
        profile_view.home_button.click()
        sign_in_view.profile_button.click()
        profile_view.swipe_down()
        if not profile_view.profile_picture.is_element_image_equals_template('sauce_logo_profile.png'):
            self.driver.fail('Profile picture was not updated')

    @marks.testrail_id(6318)
    @marks.medium
    def test_can_delete_several_multiaccounts(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.generate_key_button.click()
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
        profile.settings_button.click()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.click()
        for element in (username, delete_alert_text):
            if not profile.element_by_text(delete_alert_text).is_element_displayed():
                self.errors.append('Required %s is not shown when deleting multiaccount' % element)
        profile.delete_profile_button.click()
        if profile.element_by_text('Profile deleted').is_element_displayed():
            self.driver.fail('Profile is deleted without confirmation with password')
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click_until_presence_of_element(profile.element_by_text('Profile deleted'))
        profile.ok_button.click()

        sign_in.just_fyi('Delete last multiaccount')
        sign_in.sign_in()
        sign_in.profile_button.click()
        profile.settings_button.click()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.click()
        profile.delete_my_profile_password_input.set_value(common_password)
        profile.delete_profile_button.click()
        profile.ok_button.click()
        if not sign_in.get_started_button.is_element_displayed():
            self.errors.append('No redirected to carousel view after deleting last multiaccount')
        self.errors.verify_no_errors()



    @marks.testrail_id(5741)
    @marks.high
    def test_mobile_data_usage_popup_continue_syncing(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        sign_in_view.just_fyi("Enable mobile network to see popup and enable syncing")
        sign_in_view.toggle_mobile_data()
        if not sign_in_view.element_by_text_part("Sync using mobile data?").is_element_displayed():
            self.driver.fail('No popup about Mobile data is shown')
        sign_in_view.wait_for_element_starts_with_text('Continue syncing').click()

        sign_in_view.just_fyi("Check that selected option is stored in Profile")
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.sync_settings_button.click()
        profile_view.element_by_text('Mobile data').click()
        if not profile_view.use_mobile_data.attribute_value('checked'):
            self.errors.append("Use mobile data option is enabled after 'Continue syncing' selected")
        if profile_view.ask_me_when_on_mobile_network.attribute_value('checked'):
            self.errors.append("'Ask me when on mobile network' option not enabled even it was checked when 'Continue"
                               "syncing' selected!")

        sign_in_view.just_fyi("Check that can join public chat and send message")
        chat_name = sign_in_view.get_random_chat_name()
        home = profile_view.get_back_to_home_view()
        chat = home.join_public_chat(chat_name)
        message = 'test message'
        chat.chat_message_input.send_keys(message)
        chat.send_message_button.click()
        if not chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Message was not sent!")
        self.errors.verify_no_errors()

    @marks.testrail_id(6228)
    @marks.high
    def test_mobile_data_usage_popup_stop_syncing(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        offline_banner_text = "No Wi-fi, message syncing disabled."

        sign_in_view.just_fyi("Enable mobile network to see popup and stop syncing")
        sign_in_view.toggle_mobile_data()
        sign_in_view.wait_for_element_starts_with_text('Stop syncing').click()
        if not sign_in_view.wait_for_element_starts_with_text(offline_banner_text, 120):
            self.driver.fail('No popup about offline history is shown')
        sign_in_view.element_by_text_part(offline_banner_text).click()
        for item in offline_banner_text, "Start syncing", "Go to settings":
            if not sign_in_view.element_by_text(item).is_element_displayed():
                self.driver.fail("%s is not shown" % item)

        sign_in_view.just_fyi("Start syncing in offline popup")
        sign_in_view.element_by_text("Start syncing").click()
        sign_in_view.element_by_text_part(offline_banner_text).wait_for_invisibility_of_element(10)
        if sign_in_view.element_by_text_part(offline_banner_text).is_element_displayed():
            self.driver.fail("Popup about offline history is shown")

    @marks.testrail_id(6229)
    @marks.high
    def test_mobile_data_usage_settings(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()

        sign_in_view.just_fyi("Check default preferences")
        profile_view.settings_button.click()
        profile_view.sync_settings_button.click()
        profile_view.element_by_text('Mobile data').click()

        if profile_view.use_mobile_data.text != 'OFF':
            self.errors.append("Mobile data is enabled by default")
        if profile_view.ask_me_when_on_mobile_network.text != "ON":
            self.errors.append("'Ask me when on mobile network' is not enabled by default")

        sign_in_view.just_fyi("Disable 'ask me when on mobile network' and check that it is not shown")
        profile_view.ask_me_when_on_mobile_network.click()
        sign_in_view.toggle_mobile_data()
        if sign_in_view.element_by_text("Start syncing").is_element_displayed(20):
            self.errors.append("Popup is shown, but 'ask me when on mobile network' is disabled")

        sign_in_view.just_fyi("Check 'Restore default' setting")
        profile_view.element_by_text('Restore Defaults').click()
        if profile_view.use_mobile_data.attribute_value("checked"):
            self.errors.append("Mobile data is enabled by default")
        if not profile_view.ask_me_when_on_mobile_network.attribute_value("checked"):
            self.errors.append("'Ask me when on mobile network' is not enabled by default")
        self.errors.verify_no_errors()


    @marks.testrail_id(5454)
    @marks.critical
    @marks.skip
    # TODO: waiting for better times - no profile picture for now
    def test_user_can_remove_profile_picture(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        profile_view = home_view.profile_button.click()
        profile_view.edit_profile_picture('sauce_logo.png')
        profile_view.swipe_down()
        if not profile_view.profile_picture.is_element_image_equals_template('sauce_logo_profile.png'):
            self.driver.fail('Profile picture was not updated')
        profile_view.remove_profile_picture()
        profile_view.swipe_down()
        if profile_view.profile_picture.is_element_image_equals_template('default_icon_profile.png'):
            self.driver.fail('Profile picture was not deleted')

    @marks.testrail_id(5323)
    @marks.critical
    def test_share_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_profile_button.click()
        public_key = profile_view.public_key_text.text
        profile_view.share_button.click()
        profile_view.share_via_messenger()
        if not profile_view.element_by_text_part(public_key).is_element_present():
            self.errors.append("Can't share public key")
        for _ in range(2):
            profile_view.click_system_back_button()
        profile_view.close_share_popup()
        wallet = profile_view.wallet_button.click()
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()
        request = wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        request.share_button.click()
        wallet.share_via_messenger()
        if not wallet.element_by_text_part(address).is_element_present():
            self.errors.append("Can't share address")
        self.errors.verify_no_errors()

    @marks.testrail_id(5375)
    @marks.high
    def test_copy_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_profile_button.click()
        public_key = profile_view.public_key_text.text
        profile_view.public_key_text.long_press_element()
        profile_view.copy_text()
        profile_view.close_share_popup()
        home = profile_view.home_button.click()
        chat = home.add_contact(transaction_senders['M']['public_key'])
        chat.chat_message_input.click()
        chat.paste_text()
        input_text = chat.chat_message_input.text
        if input_text not in public_key or len(input_text) < 1:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        chat.get_back_to_home_view()

        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()
        wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        share_view = home.get_send_transaction_view()
        share_view.share_button.click()
        share_view.element_by_text('Copy').click()
        wallet.get_back_to_home_view()
        wallet.home_button.click()
        home.get_chat(transaction_senders['M']['username']).click()
        chat.chat_message_input.click()
        chat.paste_text()
        if chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.errors.verify_no_errors()

    @marks.testrail_id(5502)
    @marks.critical
    def test_can_add_existing_ens(self):
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(ens_user['passphrase'])

        home.just_fyi('switching to Mainnet')
        profile = home.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        home.profile_button.click()
        dapp_view = profile.ens_usernames_button.click()

        dapp_view.just_fyi('check if your name can be added via "ENS usernames" in Profile')
        dapp_view.element_by_text('Get started').click()
        dapp_view.ens_name.set_value(ens_user['ens'])
        if not dapp_view.element_by_text_part('is owned by you').is_element_displayed():
            self.errors.append('Owned username is not shown in ENS Dapp.')
        dapp_view.check_ens_name.click()
        if not dapp_view.element_by_text_part('Username added').is_element_displayed():
            self.errors.append('No message "Username added" after resolving own username')
        dapp_view.element_by_text('Ok, got it').click()

        dapp_view.just_fyi('check that after adding username is shown in "ENS usernames" and profile')
        if not dapp_view.element_by_text(ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own "ENS usernames" after adding')
        dapp_view.back_button.click()
        if not dapp_view.element_by_text('@%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        if not dapp_view.element_by_text('%s.stateofus.eth' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        profile.share_my_profile_button.click()
        if profile.ens_name_in_share_chat_key_text.text != '%s.stateofus.eth' % ens_user['ens']:
            self.errors.append('No ENS name is shown on tapping on share icon in Profile')
        profile.close_share_popup()

        self.errors.verify_no_errors()

    @marks.testrail_id(5475)
    @marks.low
    @marks.skip
    # TODO: skip until profile picture change feature is enabled
    def test_change_profile_picture_several_times(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        for file_name in ['sauce_logo.png', 'sauce_logo_red.png', 'saucelabs_sauce.png']:
            profile_view.edit_profile_picture(file_name=file_name)
            profile_view.swipe_down()
            if not profile_view.profile_picture.is_element_image_equals_template(
                    file_name.replace('.png', '_profile.png')):
                self.driver.fail('Profile picture was not updated')

    @marks.testrail_id(6239)
    @marks.medium
    def test_backup_recovery_phrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        if sign_in_view.profile_button.counter.text != '1':
            self.errors.append('Profile button counter is not shown')
        profile_view = sign_in_view.profile_button.click()
        profile_view.logout()
        sign_in_view.sign_in()
        if sign_in_view.profile_button.counter.text != '1':
            self.errors.append('Profile button counter is not shown after re-login')
        sign_in_view.profile_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.backup_recovery_phrase_button.click()
        recovery_phrase = profile_view.backup_recovery_phrase()
        if sign_in_view.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after recovery phrase backup')
        profile_view.backup_recovery_phrase_button.click()
        if not profile_view.backup_recovery_phrase_button.is_element_displayed():
            self.driver.fail('Back up seed phrase option is available after seed phrase backed up!')
        profile_view.back_button.click()
        profile_view.logout()
        recover_view = sign_in_view.access_key_button.click()
        recover_view.enter_seed_phrase_button.click()
        recover_view.seedphrase_input.click()
        recover_view.seedphrase_input.set_value(' '.join(recovery_phrase.values()))
        recover_view.next_button.click()
        recover_view.element_by_text('UNLOCK').click()
        sign_in_view.password_input.set_value(common_password)
        chats_view = sign_in_view.sign_in_button.click()
        chats_view.plus_button.click()
        if not chats_view.start_new_chat_button.is_element_displayed():
            self.errors.append("Can't proceed using account after it's re-recover twice.")
        self.errors.verify_no_errors()

    @marks.testrail_id(6296)
    @marks.high
    def test_recover_account_from_new_user_seedphrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.backup_recovery_phrase_button.click()
        profile_view.ok_continue_button.click()
        recovery_phrase = " ".join(profile_view.get_recovery_phrase().values())
        profile_view.back_button.click()
        profile_view.back_button.click()
        public_key = profile_view.get_public_key_and_username()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        address = wallet_view.get_wallet_address()
        sign_in_view.profile_button.click()
        profile_view.logout()
        self.driver.reset()
        # sign_in_view.accept_agreements()
        sign_in_view.recover_access(recovery_phrase)
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        if wallet_view.get_wallet_address() != address:
            self.driver.fail("Seed phrase displayed in new accounts for back up does not recover respective address")
        profile_view = wallet_view.profile_button.click()
        if profile_view.get_public_key_and_username() != public_key:
            self.driver.fail("Seed phrase displayed in new accounts for back up does not recover respective public key")

    @marks.testrail_id(5433)
    @marks.medium
    def test_invite_friends(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        home.invite_friends_button.click()
        home.share_via_messenger()
        home.find_text_part("Hey join me on Status: https://join.status.im/u/0x")
        home.click_system_back_button()
        home.plus_button.click()
        home.chats_menu_invite_friends_button.click()
        home.share_via_messenger()
        home.find_text_part("Hey join me on Status: https://join.status.im/u/0x")

    @marks.testrail_id(6312)
    @marks.medium
    def test_add_remove_contact_via_contacts_view(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        home.just_fyi('Check empty contacts view')
        profile = home.profile_button.click()
        profile.switch_network()
        home.profile_button.click()
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
                'contact_code': ens_user['ens_another_domain'],
                'username': '@%s' % ens_user['ens_another_domain'],
                'nickname': 'my_dear_friend'
            },

        }

        home.just_fyi('Add contact  and check that they appear in Contacts view')
        from views.chat_view import ChatView
        chat_view = ChatView(self.driver)
        for key in users:
            profile.plus_button.click()
            sign_in_view.just_fyi('Checking %s case' % key)
            if 'scanning' in key:
                from views.contacts_view import ContactsView
                contact_view = ContactsView(self.driver)
                contact_view.scan_contact_code_button.click()
                if contact_view.allow_button.is_element_displayed():
                    contact_view.allow_button.click()
                contact_view.enter_qr_edit_box.scan_qr(users[key]['contact_code'])
            else:
                contact_view.public_key_edit_box.click()
                contact_view.public_key_edit_box.send_keys(users[key]['contact_code'])
                if 'nickname' in users[key]:
                    chat_view.nickname_input_field.set_value(users[key]['nickname'])
                contact_view.confirm_until_presence_of_element(profile.settings_button)
            if not profile.element_by_text(users[key]['username']).is_element_displayed():
                self.errors.append('In %s case username not found in contact view after scanning' % key)
            if 'nickname' in users[key]:
                if not profile.element_by_text(users[key]['nickname']).is_element_displayed():
                    self.errors.append('In %s case nickname %s not found in contact view after scanning' % (key, users[key]['nickname']))

        home.just_fyi('Remove contact and check that it disappeared')
        user_to_remove = '@%s' % ens_user['ens_another_domain']
        profile.element_by_text(user_to_remove).click()
        chat_view.remove_from_contacts.click()
        chat_view.back_button.click()
        if profile.element_by_text(user_to_remove).is_element_displayed():
            self.errors.append('Removed user is still shown in contact view')
        self.errors.verify_no_errors()


    @marks.testrail_id(5431)
    @marks.medium
    def test_add_custom_network(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.add_custom_network()
        sign_in_view.sign_in()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.network_settings_button.scroll_to_element(10, 'up')
        profile_view.find_text_part('custom_ropsten')

    @marks.critical
    @marks.testrail_id(5419)
    @marks.flaky
    def test_logcat_backup_recovery_phrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.backup_recovery_phrase_button.click()
        profile_view.ok_continue_button.click()
        recovery_phrase = profile_view.get_recovery_phrase()
        profile_view.next_button.click()
        word_number = profile_view.recovery_phrase_word_number.number
        profile_view.recovery_phrase_word_input.set_value(recovery_phrase[word_number])
        profile_view.next_button.click()
        word_number_1 = profile_view.recovery_phrase_word_number.number
        profile_view.recovery_phrase_word_input.set_value(recovery_phrase[word_number_1])
        profile_view.done_button.click()
        profile_view.yes_button.click()
        values_in_logcat = profile_view.find_values_in_logcat(passphrase1=recovery_phrase[word_number],
                                               passphrase2=recovery_phrase[word_number_1])
        if len(values_in_logcat) == 2:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5391)
    @marks.high
    def test_need_help_section(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.help_button.click()
        base_web_view = profile_view.faq_button.click()
        base_web_view.open_in_webview()
        base_web_view.find_full_text('Frequently Asked Questions')
        base_web_view.click_system_back_button()
        profile_view.submit_bug_button.click()
        base_web_view.find_full_text('Welcome to Gmail')
        base_web_view.click_system_back_button()
        profile_view.request_a_feature_button.click()
        profile_view.find_full_text('#status')

    @marks.testrail_id(5368)
    @marks.high
    def test_log_level_and_fleet(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.advanced_button.click()
        for text in 'INFO', used_fleet:
            if not profile_view.element_by_text(text).is_element_displayed():
                self.errors.append('%s is not selected by default' % text)
        self.errors.verify_no_errors()


    @marks.testrail_id(5468)
    @marks.medium
    @marks.skip
    # TODO: skip until profile picture change feature is enabled
    def test_deny_camera_access_changing_profile_photo(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.profile_picture.click()
        profile.capture_button.click()
        for _ in range(2):
            profile.deny_button.click()
        profile.element_by_text(camera_access_error_text).wait_for_visibility_of_element(3)
        profile.ok_button.click()
        profile.profile_picture.click()
        profile.capture_button.click()
        profile.deny_button.wait_for_visibility_of_element(2)

    @marks.testrail_id(5469)
    @marks.medium
    @marks.skip
    # TODO: skip until profile picture change feature is enabled
    def test_deny_device_storage_access_changing_profile_photo(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.profile_picture.click()
        profile.select_from_gallery_button.click()
        profile.deny_button.click()
        profile.element_by_text(photos_access_error_text, element_type='text').wait_for_visibility_of_element(3)
        profile.ok_button.click()
        profile.profile_picture.click()
        profile.select_from_gallery_button.click()
        profile.deny_button.wait_for_visibility_of_element(2)

    @marks.testrail_id(5299)
    @marks.high
    def test_user_can_switch_network(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        network_name = 'Mainnet with upstream RPC'
        profile = home_view.profile_button.click()
        profile.switch_network(network_name)
        profile = home_view.profile_button.click()
        if not profile.current_active_network == network_name:
            self.driver.fail('Oops! Wrong network selected!')

    @marks.testrail_id(5453)
    @marks.medium
    def test_privacy_policy_is_accessible(self):
        signin_view = SignInView(self.driver)
        no_link_found_error_msg = 'Could not find privacy policy link at'
        no_link_open_error_msg = 'Could not open our privacy policy from'

        if not signin_view.privacy_policy_link.is_element_displayed():
            self.driver.fail('{} Sign in view!'.format(no_link_found_error_msg))

        base_web_view = signin_view.privacy_policy_link.click()
        base_web_view.open_in_webview()
        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append('{} Sign in view!'.format(no_link_open_error_msg))

        base_web_view.click_system_back_button()
        home_view = signin_view.create_user()
        profile = home_view.profile_button.click()
        profile.settings_button.click()
        about_view = profile.about_button.click()
        about_view.privacy_policy_button.click()

        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append('{} Profile about view!'.format(no_link_open_error_msg))

        self.errors.verify_no_errors()

    @marks.testrail_id(5738)
    @marks.high
    def test_dapps_permissions(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        account_name = 'Status account'

        home_view.just_fyi('open Status Test Dapp, allow all and check permissions in Profile')
        home_view.open_status_test_dapp()
        home_view.cross_icon.click()
        profile_view = home_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.privacy_and_security_button.click()
        profile_view.dapp_permissions_button.click()
        profile_view.element_by_text(test_dapp_name).click()
        if not profile_view.element_by_text(account_name).is_element_displayed():
            self.errors.append('Wallet permission was not granted')
        if not profile_view.element_by_text('Chat key').is_element_displayed():
            self.errors.append('Contact code permission was not granted')

        profile_view.just_fyi('revoke access and check that they are asked second time')
        profile_view.revoke_access_button.click()
        profile_view.back_button.click()
        dapp_view = profile_view.dapp_tab_button.click()
        dapp_view.open_url(test_dapp_url)
        if not dapp_view.element_by_text_part(account_name).is_element_displayed():
            self.errors.append('Wallet permission is not asked')
        if dapp_view.allow_button.is_element_displayed():
            dapp_view.allow_button.click(times_to_click=1)
        if not dapp_view.element_by_text_part('to your profile').is_element_displayed():
            self.errors.append('Profile permission is not asked')
        self.errors.verify_no_errors()

    @marks.testrail_id(5428)
    @marks.low
    def test_version_format(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        profile_view = home_view.profile_button.click()
        profile_view.settings_button.click()
        profile_view.about_button.click()
        app_version = profile_view.app_version_text.text
        node_version = profile_view.node_version_text.text
        if not re.search(r'\d{1}[.]\d{1,2}[.]\d{1,2}\s[(]\d*[)]', app_version):
            self.errors.append("App version %s didn't match expected format" % app_version)
        if not re.search(r'StatusIM\/v.*\/android-\d{3}\/go\d{1}[.]\d{1,}', node_version):
            self.errors.append("Node version %s didn't match expected format" % node_version)
        profile_view.app_version_text.click()
        profile_view.back_button.click()
        profile_view.home_button.click()
        chat = home_view.join_public_chat(home_view.get_random_chat_name())
        message_input = chat.chat_message_input
        message_input.paste_text_from_clipboard()
        if message_input.text != app_version:
            self.errors.append('Version number was not copied to clipboard')
        self.errors.verify_no_errors()

    @marks.testrail_id(5766)
    @marks.medium
    @marks.flaky
    def test_use_pinned_mailserver(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        profile_view = home_view.profile_button.click()

        profile_view.just_fyi('pin mailserver')
        profile_view.settings_button.click()
        profile_view.sync_settings_button.click()
        mailserver_1 = profile_view.return_mailserver_name(mailserver_gc, used_fleet)
        mailserver_2 = profile_view.return_mailserver_name(mailserver_ams, used_fleet)
        # TODO: temporary to avoid issue 9269 - should be disabled after fix
        mailserver = mailserver_1 if profile_view.element_by_text(mailserver_2).is_element_present() else mailserver_2
        profile_view.mail_server_button.click()
        profile_view.mail_server_auto_selection_button.click()
        profile_view.mail_server_by_name(mailserver).click()
        profile_view.confirm_button.click()

        profile_view.just_fyi('check that mailserver is pinned')
        profile_view.back_button.click()
        if not profile_view.element_by_text(mailserver).is_element_displayed():
            self.errors.append('"%s" mailserver is not pinned' % mailserver)
        profile_view.get_back_to_home_view()

        profile_view.just_fyi('Relogin and check that settings are preserved')
        home_view.relogin()
        home_view.profile_button.click()
        profile_view.sync_settings_button.click()
        if not profile_view.element_by_text(mailserver).is_element_displayed():
            self.errors.append('"%s" mailserver is not pinned' % mailserver)

        self.errors.verify_no_errors()

    @marks.testrail_id(6219)
    @marks.medium
    def test_set_primary_ens_custom_domain(self):
        sign_in_view = SignInView(self.driver)
        ens_not_stateofus = ens_user['ens_another_domain']
        ens_stateofus = ens_user['ens']
        home_view = sign_in_view.recover_access(ens_user['passphrase'])

        home_view.just_fyi('add 2 ENS names in Profile')
        profile_view = home_view.profile_button.click()
        dapp_view = profile_view.connect_existing_status_ens(ens_stateofus)
        profile_view.element_by_text("Add username").click()
        profile_view.element_by_text_part("another domain").click()
        dapp_view.ens_name.set_value(ens_not_stateofus)
        dapp_view.check_ens_name.click_until_presence_of_element(dapp_view.element_by_text('Ok, got it'))
        dapp_view.element_by_text('Ok, got it').click()

        home_view.just_fyi('check that by default %s ENS is set' % ens_stateofus)
        dapp_view.element_by_text('Primary username').click()
        message_to_check = 'Your messages are displayed to others with'
        if not dapp_view.element_by_text('%s\n@%s.stateofus.eth' % (message_to_check, ens_stateofus)).is_element_displayed():
             self.errors.append('%s ENS username is not set as primary by default' % ens_stateofus)

        home_view.just_fyi('check view in chat settings ENS from other domain: %s after set new primary ENS' % ens_not_stateofus)
        dapp_view.set_primary_ens_username(ens_user['ens_another_domain']).click()
        if profile_view.username_in_ens_chat_settings_text.text != '@' + ens_not_stateofus:
            self.errors.append('ENS username %s is not shown in ENS username Chat Settings after enabling' % ens_not_stateofus)

        self.errors.verify_no_errors()


class TestProfileMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5432)
    @marks.medium
    def test_custom_bootnodes(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        public_key = home_2.get_public_key_and_username()

        profile_1, profile_2 = home_1.profile_button.click(), home_2.profile_button.click()
        username_1, username_2 = profile_1.default_username_text.text, profile_2.default_username_text.text

        profile_1.just_fyi('Add custom bootnode, enable bootnodes and check validation')
        profile_1.settings_button.click()
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.add_bootnode_button.click()
        profile_1.specify_name_input.set_value('test')
        profile_1.bootnode_address_input.set_value('invalid_bootnode_address')
        if not profile_1.element_by_text_part('Invalid format').is_element_displayed():
             self.errors.append('Validation message about invalid format of bootnode is not shown')
        profile_1.save_button.click()
        if profile_1.add_bootnode_button.is_element_displayed():
             self.errors.append('User was navigated to another screen when tapped on disabled "Save" button')
        profile_1.bootnode_address_input.clear()
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
        home_2.get_back_to_home_view()

        profile_1 = home_1.profile_button.click()
        username_1 = profile_1.default_username_text.text

        profile_1.just_fyi('disable autoselection')
        profile_1.settings_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        mailserver_1 = profile_1.return_mailserver_name(mailserver_hk, used_fleet)
        mailserver_2 = profile_1.return_mailserver_name(mailserver_ams, used_fleet)
        # TODO: temporary pin mailserver to avoid issue 9269 - should be disabled after fix
        mailserver = mailserver_1 if profile_1.element_by_text(mailserver_2).is_element_present() else mailserver_2
        profile_1.mail_server_auto_selection_button.click()
        profile_1.mail_server_by_name(mailserver).click()
        profile_1.confirm_button.click()
        profile_1.just_fyi('add custom mailserver (check address/name validation) and connect to it')
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.save_button.click()
        if profile_1.element_by_text(mailserver_ams).is_element_displayed():
            self.errors.append('Could add custom mailserver with empty address and name')
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value(mailserver_address[:-3])
        profile_1.save_button.click()
        if profile_1.element_by_text(mailserver_2).is_element_displayed():
            self.errors.append('could add custom mailserver with invalid address')
        profile_1.mail_server_address_input.clear()
        profile_1.mail_server_address_input.set_value(mailserver_address)
        profile_1.save_button.click()
        # TODO: disabled due to 10065
        # profile_1.mail_server_by_name(server_name).click()
        # profile_1.mail_server_connect_button.click()
        # profile_1.confirm_button.click()
        # if profile_1.element_by_text_part("Error connecting").is_element_displayed(40):
        #     profile_1.retry_to_connect_to_mailserver()
        # profile_1.get_back_to_home_view()
        # profile_1.home_button.click()


        # profile_1.just_fyi('start chat with user2 and check that all messages are delivered')
        # chat_1 = home_1.add_contact(public_key)
        # message = 'test message'
        # chat_1.chat_message_input.send_keys(message)
        # chat_1.send_message_button.click()
        # chat_2 = home_2.get_chat(username_1).click()
        # chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        # message_1 = 'new message'
        # chat_2.chat_message_input.send_keys(message_1)
        # chat_2.send_message_button.click()
        # chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element()
        #
        # profile_1.just_fyi('delete custom mailserver')
        # chat_1.profile_button.click()
        # profile_1.sync_settings_button.click()
        # profile_1.mail_server_button.click()
        # profile_1.element_by_text(mailserver).scroll_to_element()
        # profile_1.element_by_text(mailserver).click()
        # profile_1.confirm_button.click()
        # profile_1.element_by_text(server_name).scroll_to_element()
        # profile_1.element_by_text(server_name).click()
        # profile_1.mail_server_delete_button.scroll_to_element()
        # profile_1.mail_server_delete_button.click()
        # profile_1.mail_server_confirm_delete_button.click()
        # if profile_1.element_by_text(server_name).is_element_displayed():
        #     self.errors.append('Deleted custom mailserver is shown')
        # profile_1.get_back_to_home_view()
        # profile_1.relogin()
        # chat_1.profile_button.click()
        # profile_1.sync_settings_button.click()
        # profile_1.mail_server_button.click()
        # if profile_1.element_by_text(server_name).is_element_displayed():
        #     self.errors.append('Deleted custom mailserver is shown after relogin')

        self.errors.verify_no_errors()

    @marks.testrail_id(5767)
    @marks.medium
    @marks.flaky
    def test_can_not_connect_to_mailserver(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        home_1, home_2 = sign_in_1.create_user(), sign_in_2.create_user()
        profile_1 = home_1.profile_button.click()

        profile_1.just_fyi('add non-working mailserver and connect to it')
        profile_1.settings_button.click()
        profile_1.sync_settings_button.click()
        profile_1.mail_server_button.click()
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value(mailserver_address.replace('4','5'))
        profile_1.save_button.click()
        profile_1.mail_server_auto_selection_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()
        if not profile_1.element_by_text_part('Error connecting').is_element_displayed(30):
            sign_in_1.driver.fail("No popup with 'Error connecting' is shown")
        profile_1.element_by_text('CANCEL').click()

        # TODO: blocked by 10065
        # profile_1.home_button.click()
        # home_2.just_fyi('send several messages to public channel')
        # public_chat_name = home_2.get_random_chat_name()
        # message = 'test_message'
        # public_chat_2 = home_2.join_public_chat(public_chat_name)
        # public_chat_2.chat_message_input.send_keys(message)
        # public_chat_2.send_message_button.click()
        # public_chat_2.back_button.click()
        #
        # profile_1.just_fyi('join same public chat and try to reconnect via "Tap to reconnect" and check "Connecting"')
        # profile_1.home_button.click()
        # home_1.join_public_chat(public_chat_name)
        # public_chat_1 = home_1.get_chat_view()
        # chat_state = 'Could not connect to mailserver. Tap to reconnect'
        # public_chat_1.element_by_text(chat_state).click()
        # if not public_chat_1.element_by_text_part('Connecting').is_element_displayed():
        #     self.errors.append("Indicator doesn't show 'Connecting'")

        # profile_1.just_fyi('check that can RETRY to connect')
        # for _ in range(2):
        #     public_chat_1.element_by_text('RETRY').wait_for_element(30)
        #     public_chat_1.element_by_text('RETRY').click()
        #
        # profile_1.just_fyi('check that can pick another mailserver and receive messages')
        # public_chat_1.element_by_text('PICK ANOTHER').is_element_displayed(30)
        # public_chat_1.element_by_text_part('PICK ANOTHER').click()
        # mailserver = profile_1.return_mailserver_name(mailserver_ams, used_fleet)
        # profile_1.element_by_text(mailserver).click()
        # profile_1.confirm_button.click()
        # profile_1.home_button.click()
        # if not public_chat_1.chat_element_by_text(message).is_element_displayed(30):
        #     self.errors.append("Chat history wasn't fetched")

        self.errors.verify_no_errors()

    @marks.testrail_id(5762)
    @marks.high
    def test_pair_devices_sync_one_to_one_contacts_public_chat(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home = device_1.create_user()
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.settings_button.click()
        device_1_profile.privacy_and_security_button.click()
        device_1_profile.backup_recovery_phrase_button.click()
        device_1_profile.ok_continue_button.click()
        recovery_phrase = device_1_profile.get_recovery_phrase()
        device_1_profile.back_button.click()
        device_1_profile.get_back_to_home_view()
        device_1_name = 'device_%s' % device_1.driver.number
        device_2_name = 'device_%s' % device_2.driver.number
        message_before_sync = 'sent before sync'
        message_after_sync = 'sent after sync'
        public_chat_before_sync = 'before-pairing'
        public_chat_after_sync = 'after-pairing'

        device_1.just_fyi('add contact, start 1-1 chat with basic user')
        device_1_chat = device_1_home.add_contact(basic_user['public_key'])
        device_1_chat.chat_message_input.send_keys(message_before_sync)
        device_1_chat.send_message_button.click()

        device_1.just_fyi('join public chat')
        device_1_chat.get_back_to_home_view()
        device_1_public_chat = device_1_home.join_public_chat(public_chat_before_sync)
        device_2_home = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        device_1_profile, device_2_profile = device_1_home.profile_button.click(), device_2_home.profile_button.click()

        device_2.just_fyi('go to profile and set nickname for contact')
        device_1_profile.open_contact_from_profile(basic_user['username'])
        nickname = 'my_basic_user'
        device_1_chat.set_nickname(nickname)
        device_1_profile.back_button.click()

        device_2.just_fyi('go to profile > Devices, set device name, discover device 2 to device 1')
        device_2_profile.discover_and_advertise_device(device_2_name)
        device_1_profile.discover_and_advertise_device(device_1_name)
        device_1_profile.get_toggle_device_by_name(device_2_name).wait_and_click()
        device_1_profile.sync_all_button.click()
        device_1_profile.sync_all_button.wait_for_visibility_of_element(15)

        device_2.just_fyi('check that contact with nickname is appeared in Contact list')
        [device.back_button.click(2) for device in (device_1_profile, device_2_profile)]
        device_2_profile.contacts_button.scroll_to_element(9, 'up')
        device_2_profile.contacts_button.click()
        for name in (basic_user['username'], nickname):
            if not device_2_profile.element_by_text(name).is_element_displayed():
                self.errors.append('"%s" is not found in Contacts after initial sync' % name)

        device_1.just_fyi('send message to 1-1 chat with basic user and add another contact')
        device_1_profile.home_button.click(desired_view='chat')
        device_1_public_chat.back_button.click()
        device_1_home.get_chat(nickname).click()
        device_1_chat.chat_message_input.send_keys(message_after_sync)
        device_1_chat.send_message_button.click()
        device_1_chat.back_button.click()
        device_1_home.add_contact(transaction_senders['A']['public_key'])

        device_2.just_fyi('check that messages appeared in 1-1 chat, public chats and new contacts are synced')
        if not device_2_profile.element_by_text(transaction_senders['A']['username']).is_element_displayed(60):
            self.errors.append(
                '"%s" is not found in Contacts after adding when devices are paired' % transaction_senders['A'][
                    'username'])

        device_1.just_fyi('Set nickname for added contact and check that it will be synced')
        device_1_home.profile_button.click()

        device_1_profile.open_contact_from_profile(transaction_senders['A']['username'])
        nickname_after_sync = 'my_transaction sender'
        device_1_chat.set_nickname(nickname_after_sync)
        device_1_profile.back_button.click()
        device_1.home_button.click(desired_view='chat')
        if not device_2_profile.element_by_text(nickname_after_sync).is_element_displayed(60):
            self.errors.append(
                '"%s" is not updated in Contacts after setting nickname when devices are paired' % nickname_after_sync)

        device_2_profile.home_button.click()
        if not device_2_home.element_by_text_part(public_chat_before_sync).is_element_displayed():
            self.errors.append(
                '"%s" is not found in Home after initial sync when devices are paired' % public_chat_before_sync)
        chat = device_2_home.get_chat(nickname).click()
        if chat.chat_element_by_text(message_before_sync).is_element_displayed():
            self.errors.append('"%s" message sent before pairing is synced' % message_before_sync)
        if not chat.chat_element_by_text(message_after_sync).is_element_displayed(60):
            self.errors.append('"%s" message in 1-1 is not synced' % message_after_sync)

        device_1.just_fyi('add new public chat and check that it will be synced with device2')
        device_1_chat.get_back_to_home_view()
        device_1_home.join_public_chat(public_chat_after_sync)
        device_2_home = chat.get_back_to_home_view()
        if not device_2_home.element_by_text_part(public_chat_after_sync).is_element_displayed(20):
            self.errors.append(
                '"%s" public chat is not synced after adding when devices are paired' % public_chat_after_sync)

        self.errors.verify_no_errors()

    @marks.testrail_id(5680)
    @marks.high
    @marks.skip
    # TODO: skip until edit userpic is enabled back
    def test_pair_devices_sync_name_photo_public_group_chats(self):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home = device_1.create_user()
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.settings_button.click()
        device_1_profile.privacy_and_security_button.click()
        device_1_profile.backup_recovery_phrase_button.click()
        recovery_phrase = device_1_profile.backup_recovery_phrase()
        device_1_profile.back_button.click()
        device_1_profile.get_back_to_home_view()
        device_1_name = 'device_%s' % device_1.driver.number
        device_2_name = 'device_%s' % device_2.driver.number
        public_chat_before_sync_name = 'b-public-%s' % device_1_home.get_random_chat_name()
        public_chat_after_sync_name = 'a-public-%s' % device_1_home.get_random_chat_name()
        group_chat_name = 'group-%s' % device_1_home.get_random_chat_name()
        message_after_sync = 'sent after sync'

        device_1.just_fyi('join public chat, create group chat, edit user picture')
        device_1_public_chat = device_1_home.join_public_chat(public_chat_before_sync_name)
        device_1_public_chat.back_button.click()
        device_1_one_to_one = device_1_home.add_contact(basic_user['public_key'])
        device_1_one_to_one.back_button.click()
        device_1_group_chat = device_1_home.create_group_chat([basic_user['username']], group_chat_name)
        device_1_group_chat.back_button.click()
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.edit_profile_picture('sauce_logo.png')

        device_2.just_fyi('go to profile > Devices, set device name, discover device 2 to device 1')
        device_2_home = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        device_2_profile = device_2_home.get_profile_view()
        device_2_profile.discover_and_advertise_device(device_2_name)

        device_1.just_fyi('enable pairing of `device 2` and sync')
        device_1_profile.discover_and_advertise_device(device_1_name)
        device_1_profile.get_toggle_device_by_name(device_2_name).click()
        device_1_profile.sync_all_button.click()
        device_1_profile.sync_all_button.wait_for_visibility_of_element(15)

        device_2.just_fyi('check that public chat and profile details are updated')
        device_2_home = device_2_profile.get_back_to_home_view()
        if not device_2_home.element_by_text('#%s' % public_chat_before_sync_name).is_element_displayed():
            self.errors.append('Public chat "%s" doesn\'t appear after initial sync'
                               % public_chat_before_sync_name)
        device_2_home.home_button.click()
        device_2_home.profile_button.click()
        if not device_2_profile.profile_picture.is_element_image_equals_template('sauce_logo_profile.png'):
            self.errors.append('Profile picture was not updated after initial sync')
        device_2_profile.home_button.click()

        device_1.just_fyi('send message to group chat, and join to new public chat')
        device_1_home = device_1_profile.get_back_to_home_view()
        device_1_public_chat = device_1_home.join_public_chat(public_chat_after_sync_name)
        device_1_public_chat.back_button.click()
        device_1_home.element_by_text(group_chat_name).click()
        device_1_group_chat.chat_message_input.send_keys(message_after_sync)
        device_1_group_chat.send_message_button.click()
        device_1_group_chat.back_button.click()

        device_2.just_fyi('check that message in group chat is shown, public chats are synced')
        if not device_2_home.element_by_text('#%s' % public_chat_after_sync_name).is_element_displayed():
            self.errors.append('Public chat "%s" doesn\'t appear on other device when devices are paired'
                               % public_chat_before_sync_name)

        device_2_home.element_by_text(group_chat_name).click()
        device_2_group_chat = device_2_home.get_chat_view()

        if not device_2_group_chat.chat_element_by_text(message_after_sync).is_element_displayed():
            self.errors.append('"%s" message in group chat is not synced' % message_after_sync)

        self.errors.verify_no_errors()

    @marks.testrail_id(6226)
    @marks.critical
    def test_ens_mentions_and_nickname_in_public_and_1_1_chats(self):
        self.create_drivers(2)
        device_1, device_2 = self.drivers[0], self.drivers[1]
        sign_in_1, sign_in_2 = SignInView(device_1), SignInView(device_2)
        user_1 = ens_user
        home_1 = sign_in_1.recover_access(user_1['passphrase'])
        home_2 = sign_in_2.create_user()

        home_1.just_fyi('switching to mainnet and add ENS')
        profile_1 = sign_in_1.profile_button.click()
        profile_1.switch_network('Mainnet with upstream RPC')
        home_1.profile_button.click()
        profile_1.settings_button.click()
        dapp_view_1 = profile_1.ens_usernames_button.click()
        dapp_view_1.element_by_text('Get started').click()
        dapp_view_1.ens_name.set_value(ens_user['ens'])
        expected_text = 'This user name is owned by you and connected with your chat key.'
        if not dapp_view_1.element_by_text_part(expected_text).is_element_displayed():
            dapp_view_1.click_system_back_button()
            dapp_view_1.wait_for_element_starts_with_text(expected_text)
        dapp_view_1.check_ens_name.click_until_presence_of_element(dapp_view_1.element_by_text('Ok, got it'))
        dapp_view_1.element_by_text('Ok, got it').click()

        home_1.just_fyi('check ENS name wallet address and public key')
        profile_1.element_by_text(user_1['ens']).click()
        for text in ('10 SNT, deposit unlocked', user_1['address'].lower(), user_1['public_key'] ):
            if not profile_1.element_by_text_part(text).is_element_displayed(40):
                self.errors.append('%s text is not shown' % text)
        dapp_view_1.get_back_to_home_view()
        profile_1.home_button.click()

        home_2.just_fyi('joining same public chat, set ENS name and check it in chat from device2')
        chat_name = home_1.get_random_chat_name()
        chat_2 = home_2.join_public_chat(chat_name)
        chat_1 = home_1.join_public_chat(chat_name)
        chat_1.get_back_to_home_view()
        home_1.profile_button.click()
        ens_name = '@' + user_1['ens']
        profile_1.element_by_text('Your ENS name').click()
        if profile_1.username_in_ens_chat_settings_text.text != ens_name:
            self.errors.append('ENS username is not shown in ENS usernames Chat Settings after enabling')
        profile_1.back_button.click()
        profile_1.home_button.click()
        home_1.get_chat('#' + chat_name).click()
        message_text_2 = 'message test text 1'
        chat_1.send_message(message_text_2)
        if not chat_2.wait_for_element_starts_with_text(ens_name):
            self.errors.append('ENS username is not shown in public chat')

        home_2.just_fyi('check that can mention user with ENS name')
        chat_2.select_mention_from_suggestion_list(user_1['ens'])
        if chat_2.chat_message_input.text != ens_name + ' ':
            self.errors.append('ENS username is not resolved in chat input after selecting it in mention suggestions list!')
        chat_2.send_message_button.click()
        chat_2.element_starts_with_text(ens_name,'button').click()
        for element in (chat_2.element_by_text(user_1['username']), chat_2.profile_add_to_contacts):
            if not element.is_element_displayed():
                self.errors.append('Was not redirected to user profile after tapping on mention!')
        chat_1.element_starts_with_text(user_1['ens'] +'.stateofus.eth','button').click()
        if not profile_1.settings_button.is_element_displayed():
                self.errors.append('Was not redirected to own profile after tapping on mention of myself from another user!')

        home_2.just_fyi('check that ENS name is shown in 1-1 chat without adding user as contact in header, profile, options')
        chat_2_one_to_one = chat_2.profile_send_message.click()
        if chat_2_one_to_one.user_name_text.text != ens_name:
            self.errors.append('ENS username is not shown in 1-1 chat header')
        chat_2_one_to_one.chat_options.click()
        if not chat_2_one_to_one.element_by_text(ens_name).is_element_displayed():
            self.errors.append('ENS username is not shown in 1-1 chat options')
        chat_2_one_to_one.view_profile_button.click()
        if not chat_2_one_to_one.element_by_text(ens_name).is_element_displayed():
            self.errors.append('ENS username is not shown in user profile')

        home_2.just_fyi('add user to contacts and check that ENS name is shown in contact')
        chat_2_one_to_one.profile_add_to_contacts.click()
        chat_2.back_button.click()
        profile_2 = chat_2_one_to_one.profile_button.click()
        profile_2.open_contact_from_profile(ens_name)

        home_2.just_fyi('set nickname and recheck username in 1-1 header, profile, options, contacts')
        nickname = 'test user' + str(round(time()))
        chat_2.set_nickname(nickname)
        profile_2.back_button.click()
        for name in (nickname, ens_name):
            if not profile_2.element_by_text(name).is_element_displayed():
                self.errors.append('%s is not shown in contact list' % name)
        profile_2.home_button.click(desired_view='chat')
        if chat_2_one_to_one.user_name_text.text != nickname:
            self.errors.append('Nickname for user with ENS is not shown in 1-1 chat header')
        chat_2_one_to_one.chat_options.click()
        if not chat_2_one_to_one.element_by_text(nickname).is_element_displayed():
            self.errors.append('Nickname for user with ENS is not shown in 1-1 chat options')

        home_2.just_fyi('check nickname in public chat')
        chat_2.get_back_to_home_view()
        home_2.get_chat('#' + chat_name).click()
        chat_element = chat_2.chat_element_by_text(message_text_2)
        chat_element.find_element()
        if chat_element.username.text != '%s %s' % (nickname, ens_name):
            self.errors.append('Nickname for user with ENS is not shown in public chat')

        self.errors.verify_no_errors()
