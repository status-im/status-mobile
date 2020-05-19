
from tests import marks, unique_password, common_password
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders, transaction_recipients, basic_user
from views.send_transaction_view import SendTransactionView
from views.sign_in_view import SignInView
from decimal import Decimal


class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(5309)
    @marks.critical
    def test_send_transaction_from_daap(self):
        sender = transaction_senders['K']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], unique_password)
        address = sender['address']
        initial_balance = self.network_api.get_balance(address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        initial_amount_STT = wallet_view.get_asset_amount_by_name('STT')
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(unique_password)
        self.network_api.verify_balance_is_updated(initial_balance, address)
        status_test_dapp.wallet_button.click()

        send_transaction_view.just_fyi('Verify that wallet balance is updated')
        wallet_view.wait_balance_is_changed('STT', initial_amount_STT)

        send_transaction_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction_view.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5342)
    @marks.critical
    def test_sign_message_from_daap(self):
        password = 'password_for_daap'
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user(password=password)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.find_full_text('Test message')
        send_transaction_view.enter_password_input.send_keys(password)
        send_transaction_view.sign_button.click()
        if not status_test_dapp.element_by_text_part('Signed message').is_element_displayed():
            self.driver.fail('Message was not signed')

        send_transaction_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction_view.find_values_in_logcat(password=password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5333)
    @marks.low
    def test_deploy_contract_from_daap(self):
        sender = transaction_senders['L']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.deploy_contract_button.click()
        send_transaction_view.sign_transaction(default_gas_price=False)
        for text in 'Contract deployed at: ', 'Call contract get function', \
                    'Call contract set function', 'Call function 2 times in a row':
            if not status_test_dapp.element_by_text(text).is_element_displayed(180):
                self.driver.fail('Contract was not created')

    @marks.testrail_id(5784)
    @marks.medium
    def test_sign_typed_message(self):
        sender = transaction_senders['W']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.sign_typed_message_button)
        send_transaction_view = status_test_dapp.sign_typed_message_button.click()
        send_transaction_view.enter_password_input.send_keys(common_password)
        send_transaction_view.sign_button.click_until_presence_of_element(send_transaction_view.ok_button)
        status_test_dapp.find_text_part('0xde3048417e5881acc9ca8466ab0b3e2f9f965a70acabbda2d140e95a28b13d2d'
                                        '2d38eba6c0a5bfdc50e5d59e0ed3226c749732fd4a9374b57f34121eaff2a5081c')

    @marks.testrail_id(5743)
    @marks.high
    def test_send_two_transactions_in_batch_in_dapp(self):
        sender = transaction_senders['W']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.send_two_tx_in_batch_button)
        send_transaction_view = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction_view.sign_transaction()

        wallet_view.just_fyi('Check that second "Send transaction" screen appears')
        if not send_transaction_view.sign_with_password.is_element_displayed(10):
            self.driver.fail('Second send transaction screen did not appear!')

        send_transaction_view.sign_transaction()

    @marks.testrail_id(5744)
    @marks.critical
    def test_send_two_transactions_one_after_another_in_dapp(self):
        sender = transaction_senders['Z']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_transaction_view.sign_transaction()

        send_transaction_view.just_fyi('Check that second "Send transaction" screen appears')
        if not send_transaction_view.sign_with_password.is_element_displayed(20):
            self.driver.fail('Second send transaction screen did not appear!')

        send_transaction_view.sign_transaction()

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

