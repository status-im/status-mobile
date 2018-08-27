import pytest

from tests import marks, group_chat_users, basic_user, bootnode_address, mailserver_address, camera_access_error_text, \
    photos_access_error_text
from tests.base_test_case import SingleDeviceTestCase, MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(760)
    @marks.smoke_1
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

    @marks.testrail_id(1403)
    @marks.smoke_1
    def test_share_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        public_key = profile_view.public_key_text.text
        profile_view.share_button.click()
        profile_view.share_via_messenger()
        if not profile_view.element_by_text_part(public_key).is_element_present():
            self.errors.append("Can't share public key")
        profile_view.click_system_back_button()
        profile_view.cross_icon.click()
        wallet = profile_view.wallet_button.click()
        wallet.set_up_wallet()
        request = wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        request.share_button.click()
        wallet.share_via_messenger()
        if not wallet.element_by_text_part(address).is_element_present():
            self.errors.append("Can't share address")
        self.verify_no_errors()

    @marks.testrail_id(3704)
    @marks.smoke_1
    def test_copy_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        public_key = profile_view.public_key_text.text
        profile_view.public_key_text.long_press_element()
        profile_view.copy_text()
        profile_view.cross_icon.click()
        home = profile_view.home_button.click()
        chat = home.add_contact(group_chat_users['A_USER']['public_key'])
        chat.chat_message_input.click()
        chat.paste_text()
        input_text = chat.chat_message_input.text
        if input_text not in public_key or len(input_text) < 1:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        chat.get_back_to_home_view()

        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        wallet.address_text.long_press_element()
        wallet.copy_text()
        wallet.get_back_to_home_view()
        wallet.home_button.click()
        home.get_chat_with_user(group_chat_users['A_USER']['username']).click()
        chat.chat_message_input.click()
        chat.paste_text()
        if chat.chat_message_input.text != address:
            self.errors.append('Wallet address was not copied')
        self.verify_no_errors()

    @marks.testrail_id(1407)
    def test_change_profile_picture_several_times(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        for file_name in ['sauce_logo.png', 'sauce_logo_red.png', 'saucelabs_sauce.png']:
            profile_view.edit_profile_picture(file_name=file_name)
            profile_view.swipe_down()
            if not profile_view.profile_picture.is_element_image_equals_template():
                pytest.fail('Profile picture was not updated')

    @marks.testrail_id(2374)
    @marks.smoke_1
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
        if sign_in_view.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after seed phrase backup')
        self.verify_no_errors()

    @marks.testrail_id(3721)
    def test_invite_friends(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.invite_friends_button.click()
        start_new_chat.share_via_messenger()
        start_new_chat.find_text_part("Get Status at http://status.im")

    @marks.testrail_id(3450)
    def test_set_currency(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.set_currency('Euro (EUR)')
        profile_view.get_back_to_home_view()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        if 'EUR' != wallet_view.currency_text.text:
            pytest.fail('EUR currency is not displayed')

    @marks.testrail_id(3707)
    def test_add_custom_network(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.add_custom_network()
        sign_in_view.sign_in()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.find_text_part('CUSTOM_ROPSTEN')

    @marks.testrail_id(3774)
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

    @marks.testrail_id(3751)
    def test_need_help_section(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.help_button.click()
        profile_view.request_feature_button.click()
        profile_view.find_full_text('Feature Requests')
        profile_view.click_system_back_button()
        profile_view.submit_bug_button.click()
        profile_view.find_full_text('Report a problem')
        profile_view.click_system_back_button()
        profile_view.discard_button.click()
        base_web_view = profile_view.faq_button.click()
        base_web_view.open_in_webview()
        profile_view.find_text_part('Questions around beta')

    @marks.testrail_id(1416)
    def test_contact_profile_view(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        home_view = sign_in_view.get_home_view()
        home_view.add_contact(basic_user['public_key'])
        chat_view = home_view.get_chat_view()
        chat_view.chat_options.click_until_presence_of_element(chat_view.view_profile_button)
        chat_view.view_profile_button.click()
        for text in basic_user['username'], 'In contacts', 'Send transaction', 'Send message', 'Contact code':
            chat_view.find_full_text(text)

    @marks.testrail_id(2177)
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

    @marks.testrail_id(2178)
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


@marks.all
@marks.account
class TestProfileMultipleDevice(MultipleDeviceTestCase):

    @marks.testrail_id(3708)
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

    @marks.testrail_id(3737)
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
        profile_1.mail_server_by_name(server_name).click()
        profile_1.mail_server_connect_button.click()
        profile_1.confirm_button.click()
        sign_in_1.sign_in()

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
