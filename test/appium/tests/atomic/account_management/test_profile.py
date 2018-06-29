import pytest
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, group_chat_users
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.account
class TestProfileSingleDevice(SingleDeviceTestCase):

    @marks.testrail_id(760)
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
    def test_share_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        profile_view.share_button.click()
        try:
            profile_view.element_by_text('Gmail').is_element_displayed()
            profile_view.element_by_text('Gmail', 'button').click()
            profile_view.element_by_text('Welcome to Gmail').wait_for_visibility_of_element()
        except (NoSuchElementException, TimeoutException):
            self.errors.append('Can\'t share contact code via email')
        profile_view.click_system_back_button()
        profile_view.cross_icon.click()
        wallet = profile_view.wallet_button.click()
        wallet.set_up_wallet()
        request = wallet.receive_transaction_button.click()
        request.share_button.click()
        try:
            profile_view.element_by_text('Gmail').is_element_displayed()
            profile_view.element_by_text('Gmail', 'button').click()
            profile_view.element_by_text('Welcome to Gmail').wait_for_visibility_of_element()
        except (NoSuchElementException, TimeoutException):
            self.errors.append('Can\'t share wallet address via email')
        self.verify_no_errors()

    @marks.skip
    @marks.testrail_id(3704)
    def test_copy_contact_code_and_wallet_address(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        profile_view.share_my_contact_key_button.click()
        public_key = profile_view.public_key_text.text
        profile_view.public_key_text.long_press_element()
        self.driver.press_keycode(278)
        profile_view.cross_icon.click()
        home = profile_view.home_button.click()
        chat = home.add_contact(group_chat_users['A_USER']['public_key'])
        chat.chat_message_input.click()
        self.driver.press_keycode(279)
        if chat.chat_message_input.text != public_key:
            self.errors.append('Public key was not copied')
        chat.chat_message_input.clear()
        chat.get_back_to_home_view()

        wallet = home.wallet_button.click()
        wallet.set_up_wallet()
        wallet.receive_transaction_button.click()
        address = wallet.address_text.text
        wallet.address_text.long_press_element()
        self.driver.press_keycode(278)
        wallet.get_back_to_home_view()
        wallet.home_button.click()
        home.get_chat_with_user(group_chat_users['A_USER']['username']).click()
        chat.chat_message_input.click()
        self.driver.press_keycode(279)
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

    @marks.skip
    @marks.testrail_id(2374)
    def test_backup_seed_phrase(self):
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
        profile_view.backup_seed_phrase()
        if sign_in_view.profile_button.counter.is_element_displayed():
            self.errors.append('Profile button counter is shown after seed phrase backup')
        self.verify_no_errors()
