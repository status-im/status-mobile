import pytest
import time
from tests.base_test_case import SingleDeviceTestCase
from tests import basic_user
from views.sign_in_view import SignInView


@pytest.mark.all
class TestProfileView(SingleDeviceTestCase):

    def test_qr_code_and_its_value(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.share_my_contact_key_button.click()
        key_value = profile_view.public_key_text.text
        time.sleep(5)
        key_value_from_qr = profile_view.get_text_from_qr()
        assert key_value == key_value_from_qr

    @pytest.mark.pr
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

    @pytest.mark.pr
    def test_network_switch(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        sign_in_view = profile_view.switch_network('Rinkeby with upstream RPC')
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.set_value('qwerty1234')
        sign_in_view.sign_in_button.click()
        desired_network = sign_in_view.element_by_text('RINKEBY WITH UPSTREAM RPC')
        sign_in_view.profile_button.click_until_presence_of_element(desired_network)
