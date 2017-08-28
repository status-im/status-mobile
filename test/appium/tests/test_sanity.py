import pytest
from tests.basetestcase import SingleDeviceTestCase
from views.home import HomeView


@pytest.mark.sanity
class TestSanity(SingleDeviceTestCase):

    @pytest.mark.parametrize("verification", ["short", "mismatch", "valid"])
    def test_password(self, verification):

        verifications = {"short": {"input": "qwe1",
                                   "outcome":
                                   "Password should be not less then 6 symbols."},
                         "mismatch": {"input": "mismatch1234",
                                      "outcome":
                                      "Password confirmation doesn\'t match password."},
                         "valid": {"input": "qwerty1234",
                                   "outcome":
                                   "Tap here to enter your phone number & I\'ll find your friends"}}
        home = HomeView(self.driver)
        home.request_password_icon.click()
        home.type_message_edit_box.send_keys(verifications[verification]["input"])
        home.confirm()
        if 'short' not in verification:
            home.type_message_edit_box.send_keys(verifications["valid"]["input"])
            home.confirm()
        home.find_text(verifications[verification]["outcome"])
