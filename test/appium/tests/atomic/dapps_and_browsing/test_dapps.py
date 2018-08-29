import pytest
from tests import marks, basic_user
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestDApps(SingleDeviceTestCase):

    @marks.testrail_id(3782)
    @marks.smoke_1
    def test_filters_from_daap(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        status_test_dapp.test_filters_button.click()
        for element in status_test_dapp.element_by_text('eth_uninstallFilter'), status_test_dapp.ok_button:
            if element.is_element_displayed(10):
                pytest.fail("'Test filters' button produced an error")

    @marks.testrail_id(3789)
    def test_request_public_key_status_test_daap(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=user['passphrase'], password=user['password'])
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.status_api_button.click()
        status_test_dapp.request_contact_code_button.click()
        status_test_dapp.deny_button.click()
        if status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            pytest.fail('Public key is returned but access was not allowed')
        status_test_dapp.request_contact_code_button.click()
        status_test_dapp.allow_button.click()
        if not status_test_dapp.element_by_text(user['public_key']).is_element_displayed():
            pytest.fail('Public key is not returned')
