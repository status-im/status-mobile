import pytest

from tests import base_user
from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestProfile(BaseTestCase):

    def test_copy_contact_code(self):
        sign_in = SignInView()
        sign_in.recover_access(base_user['passphrase'])
        profile = sign_in.profile_button.click()
        profile.share_my_code_button.click()
        profile.copy_code_button.click()
        assert profile.get_clipboard() == base_user['public_key']

    @pytest.mark.skip('Test cases is not ready yet')
    def test_change_mail_server(self):
        sign_in = SignInView()
        sign_in.create_account()
        profile = sign_in.profile_button.click()
        profile.element_by_text('Advanced settings').click()
        s_names = profile.get_mail_servers_list()
        profile.get_mail_server(s_names[0]).is_active()

