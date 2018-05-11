import random
import emoji
import pytest
import time
from tests.base_test_case import SingleDeviceTestCase
from tests import basic_user, marks
from views.sign_in_view import SignInView


@marks.all
class TestProfileView(SingleDeviceTestCase):

    @marks.testrail_case_id(3395)
    def test_qr_code_and_its_value(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        key_value = profile_view.public_key_text.text
        time.sleep(5)
        key_value_from_qr = profile_view.get_text_from_qr()
        if key_value != key_value_from_qr:
            self.errors.append("QR code value '%s' doesn't match public key '%s'" % (key_value_from_qr, key_value))
        profile_view.cross_icon.click()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.request_button.click()
        wallet_view.qr_code_image.wait_for_element()
        key_value = wallet_view.address_text.text
        key_value_from_qr = wallet_view.get_text_from_qr()
        if key_value not in key_value_from_qr:
            self.errors.append(
                "Wallet QR code value '%s' doesn't match wallet address '%s'" % (key_value_from_qr, key_value))
        self.verify_no_errors()

    @marks.pr
    @pytest.mark.testrail_case_id(3396)
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
        chat_view.profile_send_message.click()
        chat_view.chat_message_input.wait_for_visibility_of_element()
        chat_view.chat_options.click_until_presence_of_element(chat_view.view_profile_button)
        chat_view.view_profile_button.click()
        chat_view.profile_send_transaction.click()
        assert chat_view.chat_message_input.text.strip() == '/send'

    @marks.pr
    @marks.testrail_case_id(3397)
    def test_network_switch(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        sign_in_view = profile_view.switch_network('Rinkeby with upstream RPC')
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.set_value('qwerty1234')
        sign_in_view.sign_in_button.click()
        sign_in_view.profile_button.click_until_presence_of_element(profile_view.advanced_button)
        profile_view.advanced_button.click()
        desired_network = profile_view.element_by_text('RINKEBY WITH UPSTREAM RPC', 'text')
        desired_network.scroll_to_element()
        assert desired_network.is_element_displayed()

    @pytest.mark.testrail_case_id(3398)
    def test_profile_picture(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.edit_profile_picture(file_name='sauce_logo.png')
        profile_view.relogin()
        sign_in_view.profile_button.click()
        if not profile_view.profile_picture.is_element_image_equals_template():
            pytest.fail('Profile picture was not updated')

    @pytest.mark.testrail_case_id(3399)
    def test_backup_seed_phrase_and_recover_account(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user(password='qwerty1234')
        home_view = sign_in_view.get_home_view()
        public_key = home_view.get_public_key()
        profile_view = home_view.get_profile_view()
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
        profile_view.ok_got_it_button.click()
        profile_view.logout_button.click()
        profile_view.confirm_logout_button.click()
        recover_access_view = sign_in_view.add_existing_account_button.click()
        recover_access_view.passphrase_input.set_value(' '.join(seed_phrase[key] for key in sorted(seed_phrase)))
        recover_access_view.password_input.set_value('qwerty1234')
        recover_access_view.sign_in_button.click()
        sign_in_view.do_not_share.click()
        public_key_1 = home_view.get_public_key()
        assert public_key == public_key_1

    @pytest.mark.testrail_case_id(3411)
    def test_debug_on_of(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.debug_mode_toggle.click()
        home_view = profile_view.home_button.click()
        chat_view = home_view.get_chat_with_user('Status Console').click()
        chat_view.commands_button.click()
        chat_view.debug_command.click()
        chat_view.debug_on_command.click()
        chat_view.send_message_button.click()
        chat_view.wait_for_message_in_one_to_one_chat('Debug server has been launched! You can now execute '
                                                      'status-dev-cli scan to find the server from your computer '
                                                      'on the same network.', self.errors)
        chat_view.wait_for_message_in_one_to_one_chat('Debug mode: On', self.errors)
        chat_view.commands_button.click()
        chat_view.debug_command.click()
        chat_view.debug_off_command.click()
        chat_view.send_message_button.click()
        chat_view.wait_for_message_in_one_to_one_chat('Debug mode: Off', self.errors)
        self.verify_no_errors()

    @pytest.mark.testrail_case_id(3421)
    def test_switch_users(self):
        sign_in_view = SignInView(self.driver)
        for _ in range(3):
            sign_in_view.create_user(password='qwerty1234')
            home_view = sign_in_view.get_home_view()
            profile_view = home_view.profile_button.click()
            profile_view.logout_button.click()
            profile_view.confirm_logout_button.click()
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        sign_in_view.home_button.wait_for_visibility_of_element()

    @pytest.mark.testrail_case_id(3424)
    def test_incorrect_password(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_account_button.click()
        sign_in_view.password_input.set_value('123456')
        sign_in_view.next_button.click()
        sign_in_view.confirm_password_input.set_value('123455')
        sign_in_view.next_button.click()
        sign_in_view.find_full_text("Password confirmation doesn't match password.")
        sign_in_view.confirm_password_input.clear()
        sign_in_view.confirm_password_input.set_value('123456')
        sign_in_view.next_button.click()
        sign_in_view.name_input.wait_for_element(45)
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        username = 'user'
        sign_in_view.name_input.click()
        sign_in_view.name_input.send_keys(emoji.emojize('%s %s' % (username, emoji_name)))
        sign_in_view.next_button.click()
        sign_in_view.do_not_share.wait_for_element(10)
        sign_in_view.do_not_share.click_until_presence_of_element(sign_in_view.home_button)
        profile_view = sign_in_view.profile_button.click()
        assert profile_view.username_text.text == '%s %s' % (username, emoji.EMOJI_UNICODE[emoji_name])
        profile_view.logout_button.click()
        profile_view.confirm_logout_button.click()
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.send_keys('123455')
        sign_in_view.sign_in_button.click()
        sign_in_view.find_full_text('Wrong password')
