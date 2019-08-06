import pytest

from tests import marks, bootnode_address, mailserver_address, camera_access_error_text, \
    photos_access_error_text
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from tests.users import transaction_senders, basic_user, ens_user
from views.sign_in_view import SignInView
from views.dapps_view import DappsView


@marks.all
@marks.account
class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(5302)
    @marks.high
    def test_set_profile_picture(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.edit_profile_picture(file_name='sauce_logo.png')
        profile_view.home_button.click()
        sign_in_view.profile_button.click()
        profile_view.swipe_down()
        if not profile_view.profile_picture.is_element_image_equals_template():
            pytest.fail('Profile picture was not updated')

    @marks.testrail_id(5454)
    @marks.critical
    def test_user_can_remove_profile_picture(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        profile_view = home_view.profile_button.click()
        profile_view.edit_profile_picture('sauce_logo.png')
        profile_view.swipe_down()
        if not profile_view.profile_picture.is_element_image_equals_template():
            self.driver.fail('Profile picture was not updated')

        profile_view.remove_profile_picture()
        profile_view.swipe_down()
        if profile_view.profile_picture.is_element_image_equals_template():
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
        profile_view.cross_icon.click()
        wallet = profile_view.wallet_button.click()
        wallet.set_up_wallet()
        wallet.accounts_status_account.click()
        request = wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        request.share_button.click()
        wallet.share_via_messenger()
        if not wallet.element_by_text_part(address).is_element_present():
            self.errors.append("Can't share address")
        self.verify_no_errors()

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
        profile_view.cross_icon.click()
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
        share_view.element_by_text('Copy to clipboard').click()
        wallet.get_back_to_home_view()
        wallet.home_button.click()
        home.get_chat_with_user(transaction_senders['M']['username']).click()
        chat.chat_message_input.click()
        chat.paste_text()
        if chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.verify_no_errors()

    @marks.testrail_id(5502)
    @marks.critical
    def test_can_add_existing_ens(self):
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(ens_user['passphrase'])
        profile = home.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        home.profile_button.click()
        profile.element_by_text('ENS usernames').click()
        dapp_view = DappsView(self.driver)

        # check if your name can be added via "ENS usernames" dapp in Profile
        dapp_view.element_by_text('Get started').click()
        dapp_view.ens_name.set_value(ens_user['ens'])
        if not dapp_view.element_by_text_part('is owned by you').is_element_displayed():
            self.errors.append('Owned username is not shown in ENS Dapp.')
        dapp_view.check_ens_name.click()
        dapp_view.check_ens_name.click()
        if not dapp_view.element_by_text_part('Username added').is_element_displayed():
            self.errors.append('No message "Username added" after resolving own username')
        dapp_view.element_by_text('Ok, got it').click()

        # check that after adding username is shown in "ENS usernames" and profile
        if not dapp_view.element_by_text(ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own "ENS usernames" after adding')
        dapp_view.back_button.click()
        if not dapp_view.element_by_text('@%s' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        if not dapp_view.element_by_text('%s.stateofus.eth' % ens_user['ens']).is_element_displayed():
            self.errors.append('No ENS name is shown in own profile after adding')
        self.verify_no_errors()

    @marks.testrail_id(5475)
    @marks.low
    def test_change_profile_picture_several_times(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        for file_name in ['sauce_logo.png', 'sauce_logo_red.png', 'saucelabs_sauce.png']:
            profile_view.edit_profile_picture(file_name=file_name)
            profile_view.swipe_down()
            if not profile_view.profile_picture.is_element_image_equals_template():
                pytest.fail('Profile picture was not updated')

    @marks.testrail_id(5329)
    @marks.critical
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
        profile_view.backup_recovery_phrase()
        if sign_in_view.profile_button.counter.is_element_displayed(60):
            self.errors.append('Profile button counter is shown after recovery phrase backup')
        self.verify_no_errors()

    @marks.testrail_id(5433)
    @marks.medium
    def test_invite_friends(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        home.plus_button.click()
        home.invite_friends_button.click()
        home.share_via_messenger()
        home.find_text_part("Get Status at http://status.im")

    @marks.testrail_id(5431)
    @marks.medium
    def test_add_custom_network(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.add_custom_network()
        sign_in_view.sign_in()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.network_settings_button.scroll_to_element(10, 'up')
        profile_view.find_text_part('custom_ropsten')

    @marks.logcat
    @marks.testrail_id(5419)
    def test_logcat_backup_recovery_phrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
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
        profile_view.check_no_values_in_logcat(passphrase1=recovery_phrase[word_number],
                                               passphrase2=recovery_phrase[word_number_1])

    @marks.testrail_id(5391)
    @marks.high
    def test_need_help_section(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.help_button.click()
        base_web_view = profile_view.faq_button.click()
        base_web_view.open_in_webview()
        base_web_view.find_full_text('Frequently Asked Questions')
        base_web_view.click_system_back_button()
        profile_view.submit_bug_button.click()
        profile_view.find_full_text('#status')

    @marks.testrail_id(5382)
    @marks.high
    @marks.battery_consumption
    def test_contact_profile_view(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(basic_user['public_key'])
        chat_view = home_view.get_chat_view()
        chat_view.chat_options.click_until_presence_of_element(chat_view.view_profile_button)
        chat_view.view_profile_button.click()
        for text in basic_user['username'], 'In contacts', 'Send message', 'Contact code':
            if not chat_view.element_by_text(text).scroll_to_element():
                self.errors.append('%s is not visible' % text)
        self.verify_no_errors()

    @marks.testrail_id(5468)
    @marks.medium
    def test_deny_camera_access_changing_profile_photo(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.edit_button.click()
        profile.edit_picture_button.click()
        profile.capture_button.click()
        for _ in range(2):
            profile.deny_button.click()
        profile.element_by_text(camera_access_error_text).wait_for_visibility_of_element(3)
        profile.ok_button.click()
        profile.edit_picture_button.click()
        profile.capture_button.click()
        profile.deny_button.wait_for_visibility_of_element(2)

    @marks.testrail_id(5469)
    @marks.medium
    def test_deny_device_storage_access_changing_profile_photo(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        profile = sign_in.profile_button.click()
        profile.edit_button.click()
        profile.edit_picture_button.click()
        profile.select_from_gallery_button.click()
        profile.deny_button.click()
        profile.element_by_text(photos_access_error_text, element_type='text').wait_for_visibility_of_element(3)
        profile.ok_button.click()
        profile.edit_picture_button.click()
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
        about_view = profile.about_button.click()
        about_view.privacy_policy_button.click()

        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append('{} Profile about view!'.format(no_link_open_error_msg))

        base_web_view.click_system_back_button()
        if about_view.privacy_policy_button.is_element_displayed():
            base_web_view.click_system_back_button()
        profile.logout()
        if signin_view.ok_button.is_element_displayed():
            signin_view.ok_button.click()
        signin_view.back_button.click()
        signin_view.generate_new_key_button.click()

        if not signin_view.privacy_policy_link.is_element_displayed():
            self.driver.fail('{} Sign in view!'.format(no_link_found_error_msg))

        signin_view.privacy_policy_link.click()
        if not base_web_view.policy_summary.is_element_displayed():
            self.errors.append('{} Sign in view!'.format(no_link_open_error_msg))

        self.verify_no_errors()

    @marks.testrail_id(5738)
    @marks.medium
    def test_dapps_permissions(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        home_view.open_status_test_dapp()
        home_view.back_button.click()
        profile_view = home_view.profile_button.click()
        profile_view.dapp_permissions_button.click()
        profile_view.element_by_text('status-im.github.io').click()
        if not profile_view.element_by_text('Wallet').is_element_displayed():
            self.errors.append('Wallet permission was not granted')
        if not profile_view.element_by_text('Contact code').is_element_displayed():
            self.errors.append('Contact code permission was not granted')
        profile_view.revoke_access_button.click()
        profile_view.back_button.click()
        dapp_view = profile_view.dapp_tab_button.click()
        dapp_view.open_url('status-im.github.io/dapp')
        if not dapp_view.element_by_text_part('connect to your wallet').is_element_displayed():
            self.errors.append('Wallet permission is not asked')
        if dapp_view.allow_button.is_element_displayed():
            dapp_view.allow_button.click(times_to_click=1)
        if not dapp_view.element_by_text_part('to your profile').is_element_displayed():
            self.errors.append('Profile permission is not asked')
        self.verify_no_errors()


@marks.all
@marks.account
class TestProfileMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(5432)
    @marks.medium
    def test_custom_bootnodes(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'
        home_1, home_2 = sign_in_1.create_user(username=username_1), sign_in_2.create_user(username=username_2)
        public_key = home_2.get_public_key()
        home_2.home_button.click()

        profile_1 = home_1.profile_button.click()
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.plus_button.click()
        profile_1.specify_name_input.set_value('test')
        profile_1.bootnode_address_input.set_value(bootnode_address)
        profile_1.save_button.click()
        profile_1.enable_bootnodes.click()
        sign_in_1.sign_in()

        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        chat_2.add_to_contacts.click()

        chat_1.get_back_to_home_view()
        home_1.profile_button.click()
        profile_1.advanced_button.click()
        profile_1.bootnodes_button.click()
        profile_1.enable_bootnodes.click()
        sign_in_1.sign_in()

        home_1.get_chat_with_user(username_2).click()
        message_1 = 'new message'
        chat_1.chat_message_input.send_keys(message_1)
        chat_1.send_message_button.click()
        chat_2.chat_element_by_text(message_1).wait_for_visibility_of_element()

    @marks.testrail_id(5436)
    @marks.medium
    def test_switch_mailserver(self):
        self.create_drivers(2)
        sign_in_1, sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_1, username_2 = 'user_1', 'user_2'
        home_1, home_2 = sign_in_1.create_user(username=username_1), sign_in_2.create_user(username=username_2)
        public_key = home_2.get_public_key()
        home_2.home_button.click()

        profile_1 = home_1.profile_button.click()
        profile_1.advanced_button.click()
        profile_1.mail_server_button.click()
        profile_1.plus_button.click()
        server_name = 'test'
        profile_1.specify_name_input.set_value(server_name)
        profile_1.mail_server_address_input.set_value(mailserver_address)
        profile_1.save_button.click()
        profile_1.mail_server_auto_selection_button.click()
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()
        profile_1.get_back_to_home_view()
        profile_1.home_button.click()

        chat_1 = home_1.add_contact(public_key)
        message = 'test message'
        chat_1.chat_message_input.send_keys(message)
        chat_1.send_message_button.click()
        chat_2 = home_2.get_chat_with_user(username_1).click()
        chat_2.chat_element_by_text(message).wait_for_visibility_of_element()
        message_1 = 'new message'
        chat_2.chat_message_input.send_keys(message_1)
        chat_2.send_message_button.click()
        chat_1.chat_element_by_text(message_1).wait_for_visibility_of_element()


@marks.all
@marks.account
class TestProfileMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(6835)
    @marks.high
    def test_pair_devices_sync_one_to_one_contacts(self):

        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        device_1_home = device_1.create_user()
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.backup_recovery_phrase_button.click()
        device_1_profile.ok_continue_button.click()
        recovery_phrase = device_1_profile.get_recovery_phrase()
        device_1_profile.back_button.click()
        device_1_profile.get_back_to_home_view()
        device_1_name = 'device_%s' % device_1.driver.number
        device_2_name = 'device_%s' % device_2.driver.number
        message_before_sync = 'sent before sync'
        message_after_sync = 'sent after sync'

        # device 1: add contact, start 1-1 chat with basic user
        device_1_chat = device_1_home.add_contact(basic_user['public_key'])
        device_1_chat.chat_message_input.send_keys(message_before_sync)
        device_1_chat.send_message_button.click()

        # device 2: go to profile > Devices, set device name, discover device 2 to device 1
        device_2_home = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        device_2_profile = device_2_home.get_profile_view()
        device_2_profile.discover_and_advertise_device(device_2_name)
        device_1_profile.discover_and_advertise_device(device_1_name)
        device_1_profile.get_toggle_device_by_name(device_2_name).click()
        device_1_profile.sync_all_button.click()
        device_1_profile.sync_all_button.wait_for_visibility_of_element(15)

        # device 2: check that contact is appeared in Contact list
        device_2_profile.back_button.click()
        device_2_profile.contacts_button.scroll_to_element(9, 'up')
        device_2_profile.contacts_button.click()
        if not device_2_profile.element_by_text(basic_user['username']).is_element_displayed():
            self.errors.append('"%s" is not found in Contacts after initial sync' % basic_user['username'])

        # device 1: send message to 1-1 chat with basic user and add another contact
        device_1_chat.get_back_to_home_view()
        device_1_chat.chat_message_input.send_keys(message_after_sync)
        device_1_chat.send_message_button.click()
        device_1_chat.back_button.click()
        device_1_home.add_contact(transaction_senders['A']['public_key'])

        # device 2: check that messages appeared in 1-1 chat and new contacts are synced
        if not device_2_profile.element_by_text(transaction_senders['A']['username']):
            self.errors.append(
                '"%s" is not found in Contacts after adding when devices are paired' % transaction_senders['A'][
                    'username'])
        device_2_profile.get_back_to_home_view()
        chat = device_2_home.get_chat_with_user(basic_user['username']).click()
        if chat.chat_element_by_text(message_before_sync).is_element_displayed():
            self.errors.append('"%s" message sent before pairing is synced' % message_before_sync)
        if not chat.chat_element_by_text(message_after_sync).is_element_displayed():
            self.errors.append('"%s" message in 1-1 is not synced' % message_after_sync)

        self.verify_no_errors()

    @marks.testrail_id(5680)
    @marks.high
    def test_pair_devices_sync_name_photo_public_group_chats(self):

        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        username_before_sync = 'username_before_sync'
        device_1_home = device_1.create_user(username_before_sync)
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.backup_recovery_phrase_button.click()
        device_1_profile.ok_continue_button.click()
        recovery_phrase = device_1_profile.get_recovery_phrase()
        device_1_profile.back_button.click()
        device_1_profile.get_back_to_home_view()
        device_1_name = 'device_%s' % device_1.driver.number
        device_2_name = 'device_%s' % device_2.driver.number
        public_chat_before_sync_name = 'b-public-%s' % device_1_home.get_public_chat_name()
        public_chat_after_sync_name = 'a-public-%s' % device_1_home.get_public_chat_name()
        group_chat_name = 'group-%s' % device_1_home.get_public_chat_name()
        profile_picture_before_sync = 'sauce_logo.png'
        profile_picture_after_sync = 'sauce_logo_red.png'
        # username_after_sync = 'username_after_sync'
        message_after_sync = 'sent after sync'

        # device 1: join public chat, create group chat, edit user picture
        device_1_public_chat = device_1_home.join_public_chat(public_chat_before_sync_name)
        device_1_public_chat.back_button.click()
        device_1_one_to_one = device_1_home.add_contact(basic_user['public_key'])
        device_1_one_to_one.back_button.click()
        device_1_group_chat = device_1_home.create_group_chat([basic_user['username']], group_chat_name)
        device_1_group_chat.back_button.click()
        device_1_home.profile_button.click()
        device_1_profile = device_1_home.get_profile_view()
        device_1_profile.edit_profile_picture(profile_picture_before_sync)

        # device 2: go to profile > Devices, set device name, discover device 2 to device 1
        device_2_home = device_2.recover_access(passphrase=' '.join(recovery_phrase.values()))
        device_2_profile = device_2_home.get_profile_view()
        device_2_profile.discover_and_advertise_device(device_2_name)

        # device 1: enable pairing of `device 2` and sync
        device_1_profile.discover_and_advertise_device(device_1_name)
        device_1_profile.get_toggle_device_by_name(device_2_name).click()
        device_1_profile.sync_all_button.click()
        device_1_profile.sync_all_button.wait_for_visibility_of_element(15)

        # device 2: check that public chat and profile details are updated
        device_2_home = device_2_profile.get_back_to_home_view()
        if not device_2_home.element_by_text('#%s' % public_chat_before_sync_name).is_element_displayed():
            pytest.fail('Public chat "%s" doesn\'t appear after initial sync' % public_chat_before_sync_name)
        device_2_home.profile_button.click()
        device_2_profile.contacts_button.scroll_to_element(9, 'up')
        # if not device_2_profile.element_by_text(username_before_sync).is_element_displayed():
        #     pytest.fail('Profile username was not updated after initial sync')
        device_2_profile.swipe_down()
        if not device_2_profile.profile_picture.is_element_image_equals_template(profile_picture_before_sync):
            pytest.fail('Profile picture was not updated after initial sync')

        # device 1: send message to group chat, edit profile details and join to new public chat
        device_1_home = device_1_profile.get_back_to_home_view()
        device_1_public_chat = device_1_home.join_public_chat(public_chat_after_sync_name)
        device_1_public_chat.back_button.click()
        device_1_home.element_by_text(group_chat_name).click()
        device_1_group_chat.chat_message_input.send_keys(message_after_sync)
        device_1_group_chat.send_message_button.click()
        device_1_group_chat.back_button.click()
        device_1_profile = device_1_home.profile_button.click()
        device_1_profile.edit_profile_picture(profile_picture_after_sync)
        # device_1_profile.edit_profile_username(username_after_sync)

        # device 2: check that message in group chat is shown, profile details and public chats are synced
        # TODO:disabled because editing custom name is not a feature anymore
        # if not device_2_profile.element_by_text(username_after_sync).is_element_displayed():
        #     pytest.fail('Profile username was not updated after changing when devices are paired')
        device_2_profile.swipe_down()
        if not device_2_profile.profile_picture.is_element_image_equals_template(profile_picture_after_sync):
            pytest.fail('Profile picture was not updated after changing when devices are paired')

        device_2_profile.get_back_to_home_view()
        if not device_2_home.element_by_text('#%s' % public_chat_after_sync_name).is_element_displayed():
            pytest.fail(
                'Public chat "%s" doesn\'t appear on other device when devices are paired' % public_chat_before_sync_name)

        device_2_home.element_by_text(group_chat_name).click()
        device_2_group_chat = device_2_home.get_chat_view()

        if not device_2_group_chat.chat_element_by_text(message_after_sync).is_element_displayed():
            pytest.fail('"%s" message in group chat is not synced' % message_after_sync)

        self.verify_no_errors()
