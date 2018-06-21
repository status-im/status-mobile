from tests import marks
from tests.base_test_case import MultipleDeviceTestCase
from views.sign_in_view import SignInView


@marks.all
@marks.sign_in
class TestSignIn(MultipleDeviceTestCase):

    @marks.testrail_case_id(3740)
    def test_offline_login(self):
        self.create_drivers(1, offline_mode=True)
        driver = self.drivers[0]
        sign_in = SignInView(driver)
        sign_in.create_user()

        driver.close_app()
        driver.set_network_connection(1)  # airplane mode

        driver.launch_app()
        sign_in.accept_agreements()
        home = sign_in.sign_in()
        home.home_button.wait_for_visibility_of_element()
        assert home.connection_status.text == 'Offline'
