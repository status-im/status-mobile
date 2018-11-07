import pytest

from tests import marks, unique_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders, transaction_recipients
from views.sign_in_view import SignInView


class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(5309)
    @marks.critical
    def test_send_transaction_from_daap(self):
        sender = transaction_senders['K']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        address = sender['address']
        initial_balance = self.network_api.get_balance(address)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        wallet_view = send_transaction_view.get_wallet_view()
        wallet_view.done_button.click()
        wallet_view.yes_button.click()
        send_transaction_view.sign_transaction()
        self.network_api.verify_balance_is_updated(initial_balance, address)

    @marks.testrail_id(5342)
    @marks.critical
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

    @marks.testrail_id(5333)
    @marks.critical
    def test_deploy_contract_from_daap(self):
        sender = transaction_senders['L']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'])
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.deploy_contract_button.click()
        send_transaction_view.sign_transaction()
        for text in 'Contract deployed at: ', 'Call contract get function', \
                    'Call contract set function', 'Call function 2 times in a row':
            if not status_test_dapp.element_by_text(text).is_element_displayed(120):
                pytest.fail('Contract was not created')

    @marks.logcat
    @marks.testrail_id(5418)
    @marks.critical
    def test_logcat_send_transaction_from_daap(self):
        sender = transaction_senders['M']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], unique_password)
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(unique_password)
        send_transaction_view.check_no_values_in_logcat(password=unique_password)

    @marks.logcat
    @marks.testrail_id(5420)
    @marks.critical
    def test_logcat_sign_message_from_daap(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user(password=unique_password)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys(unique_password)
        send_transaction_view.sign_transaction_button.click()
        send_transaction_view.check_no_values_in_logcat(password=unique_password)

    @marks.testrail_id(5372)
    @marks.high
    def test_request_eth_in_status_test_dapp(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        status_test_dapp.request_eth_button.click()
        status_test_dapp.element_by_text('Faucet request recieved').wait_for_visibility_of_element()
        status_test_dapp.ok_button.click()
        status_test_dapp.cross_icon.click()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_changed_on_wallet_screen()

    @marks.testrail_id(5355)
    @marks.critical
    def test_onboarding_screen_when_requesting_tokens_for_new_account(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        if not send_transaction_view.onboarding_message.is_element_displayed():
            self.driver.fail('It seems onboarding screen is not shown.')

    @marks.testrail_id(5677)
    @marks.high
    def test_onboarding_screen_when_requesting_tokens_for_recovered_account(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=transaction_senders['U']['passphrase'])
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        if not send_transaction_view.onboarding_message.is_element_displayed():
            self.driver.fail('It seems onboarding screen is not shown.')

    @marks.testrail_id(5380)
    @marks.high
    def test_user_can_complete_tx_to_dapp_when_onboarding_via_dapp_completed(self):
        user = transaction_recipients['G']
        signin_view = SignInView(self.driver)
        home_view = signin_view.recover_access(passphrase=user['passphrase'])
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()

        send_transaction_view = status_test_dapp.request_stt_button.click()
        if not send_transaction_view.onboarding_message.is_element_displayed():
            self.driver.fail('It seems onborading screen is not shown.')
        send_transaction_view.complete_onboarding()

        if not send_transaction_view.sign_transaction_button.is_element_displayed():
            self.driver.fail('It seems transaction sign screen is not shown.')

        send_transaction_view.sign_transaction()

        if not status_test_dapp.assets_button.is_element_displayed():
            self.driver.fail('It seems users was not redirected to Status DAPP screen.')
