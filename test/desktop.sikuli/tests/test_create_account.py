import pytest
from views.sign_in_view import SignInView
from tests.base_test_case import BaseTestCase


class TestCreateAccount(BaseTestCase):

    @pytest.mark.desktop
    def test_create_account(self):
        sign_in = SignInView()
        sign_in.create_account_button.click()
        sign_in.password_input.input_value('123456')
        sign_in.next_button.click()
        sign_in.confirm_password_input.input_value('123456')
        sign_in.next_button.click()
        sign_in.username_input.input_value('test')
        sign_in.next_button.click()
        sign_in.home_button.find_element()
