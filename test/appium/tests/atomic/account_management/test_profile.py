import pytest

from tests import marks, group_chat_users
from tests import marks, group_chat_users, basic_user
from tests.base_test_case import SingleDeviceTestCase
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
        sign_in_view.click_account_by_position(0)
        sign_in_view.sign_in()
        if sign_in_view.profile_button.counter.text != '1':
            self.errors.append('Profile button counter is not shown after relogin')
        sign_in_view.profile_button.click()
        profile_view.backup_recovery_phrase()
        if sign_in_view.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after seed phrase backup')
        self.verify_no_errors()

    @marks.testrail_id(3721)
    def test_invite_friends(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.receive_transaction_button.click()
        address = wallet.address_text.text[2:]
        wallet.get_back_to_home_view()
        wallet.home_button.click()
        start_new_chat = home.plus_button.click()
        start_new_chat.invite_friends_button.click()
        start_new_chat.share_via_messenger()
        start_new_chat.find_text_part("Get Status at http://status.im?refCode=%s" % address)

    @marks.testrail_id(3450)
    def test_set_currency(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.set_currency('Euro (EUR)')
        profile_view.get_back_to_home_view()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        assert '€' in wallet_view.total_amount_text.text
        assert 'EUR' == wallet_view.currency_text.text

    @marks.testrail_id(3707)
    def test_add_custom_network(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.add_custom_network()
        sign_in_view.click_account_by_position(0)
        sign_in_view.sign_in()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.find_text_part('CUSTOM_ROPSTEN')

    @marks.logcat
    @marks.testrail_id(3774)
    def test_logcat_backup_seed_phrase(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.backup_seed_phrase_button.click()
        profile_view.ok_continue_button.click()
        seed_phrase = profile_view.get_seed_phrase()
        profile_view.next_button.click()
        word_number = profile_view.seed_phrase_word_number.number
        profile_view.seed_phrase_word_input.set_value(seed_phrase[word_number])
        profile_view.next_button.click()
        word_number_1 = profile_view.seed_phrase_word_number.number
        profile_view.seed_phrase_word_input.set_value(seed_phrase[word_number_1])
        profile_view.done_button.click()
        profile_view.yes_button.click()
        for i in seed_phrase[word_number], seed_phrase[word_number_1]:
            profile_view.check_no_value_in_logcat(i, 'Passphrase')

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
    @marks.smoke_1
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
