from tests.base_test_case import BaseTestCase
from views.sign_in_view import SignInView


class TestPrivacyPolicy(BaseTestCase):

    def test_privacy_policy(self):
        sign_in = SignInView()
        sign_in.privacy_policy_button.click()
        sign_in.find_text('Privacy Policy of Status Mobile')
