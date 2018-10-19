import pytest
from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from tests.users import basic_user
from views.sign_in_view import SignInView


@pytest.mark.all
class TestDApps(SingleDeviceTestCase):

    @marks.testrail_id(5353)
    @marks.critical
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

    @marks.testrail_id(5397)
    @marks.high
    def test_request_public_key_status_test_daap(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(passphrase=user['passphrase'])
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

    @marks.testrail_id(5654)
    @marks.low
    def test_can_proceed_dapp_usage_after_transacting_it(self):
        user = basic_user
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(passphrase=user['passphrase'])
        chat = home_view.join_public_chat(home_view.get_public_chat_name())
        chat.back_button.click()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        wallet_view = send_transaction_view.get_wallet_view()
        wallet_view.done_button.click()
        wallet_view.yes_button.click()
        send_transaction_view.advanced_button.click()
        send_transaction_view.transaction_fee_button.click()
        send_transaction_view.done_button.click()
        send_transaction_view.sign_transaction()
        if not status_test_dapp.assets_button.is_element_displayed():
            self.driver.fail('Oops! Cannot proceed to use Status Test Dapp.')
