from tests import transaction_users, marks, common_password
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(769)
    @marks.smoke_1
    def test_send_transaction_from_daap(self):
        sender = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        address = transaction_users['B_USER']['address']
        initial_balance = self.network_api.get_balance(address)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(sender['password'])
        self.network_api.verify_balance_is_updated(initial_balance, address)

    @marks.testrail_id(3716)
    @marks.smoke_1
    def test_sign_message_from_daap(self):
        password = 'password_for_daap'
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user(password=password)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.find_full_text('Test message')
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys(password)
        send_transaction_view.sign_transaction_button.click()

    @marks.testrail_id(3772)
    def test_logcat_send_transaction_from_daap(self):
        sender = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(sender['password'])
        send_transaction_view.check_no_value_in_logcat(sender['password'])

    @marks.testrail_id(3775)
    def test_logcat_sign_message_from_daap(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys(common_password)
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.check_no_value_in_logcat(common_password)
