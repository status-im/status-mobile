from tests import marks, pin, puk, pair_code
from tests.base_test_case import SingleDeviceTestCase
from tests.users import transaction_senders
from views.sign_in_view import SignInView


class TestTransactionDApp(SingleDeviceTestCase):

    @marks.testrail_id(6249)
    @marks.critical
    def test_keycard_send_transaction_from_daap(self):
        sender = transaction_senders['K']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        address = sender['address']
        initial_balance = self.network_api.get_balance(address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        initial_amount_STT = wallet_view.get_asset_amount_by_name('STT')
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(keycard=True)
        self.network_api.verify_balance_is_updated(initial_balance, address)
        status_test_dapp.wallet_button.click()

        send_transaction_view.just_fyi('Verify that wallet balance is updated')
        wallet_view.wait_balance_is_changed('STT', initial_amount_STT)

        send_transaction_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction_view.find_values_in_logcat(mnemonic=sender['passphrase'],
                                                                       pin=pin,
                                                                       puk=puk,
                                                                       password=pair_code)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(6251)
    @marks.critical
    def test_keycard_sign_message_from_daap(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user(keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.find_full_text('Test message')
        keycard_view = send_transaction_view.sign_with_keycard_button.click()
        keycard_view.enter_default_pin()
        if not keycard_view.element_by_text_part('Signed message').is_element_displayed():
            self.driver.fail('Message was not signed')

        keycard_view.just_fyi('Check logcat for sensitive data')
        values_in_logcat = send_transaction_view.find_values_in_logcat(pin=pin,
                                                                       puk=puk,
                                                                       password=pair_code)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5333)
    @marks.medium
    def test_keycard_deploy_contract_from_daap(self):
        sender = transaction_senders['L']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.deploy_contract_button.click()
        send_transaction_view.sign_transaction(keycard=True, default_gas_price=False)
        for text in 'Contract deployed at: ', 'Call contract get function', \
                    'Call contract set function', 'Call function 2 times in a row':
            if not status_test_dapp.element_by_text(text).is_element_displayed(180):
                self.driver.fail('Contract was not created')

    @marks.testrail_id(5784)
    @marks.medium
    def test_keycard_sign_typed_message(self):
        sender = transaction_senders['W']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.sign_typed_message_button)
        send_transaction_view = status_test_dapp.sign_typed_message_button.click()
        send_transaction_view.sign_with_keycard_button.click()
        keycard_view = send_transaction_view.sign_with_keycard_button.click()
        keycard_view.enter_default_pin()
        if not keycard_view.element_by_text('0x123').is_element_displayed():
            self.driver.fail('Typed message was not signed')

    @marks.testrail_id(6287)
    @marks.high
    def test_keycard_send_two_transactions_in_batch_in_dapp(self):
        sender = transaction_senders['W']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(status_test_dapp.send_two_tx_in_batch_button)
        send_transaction_view = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction_view.sign_transaction(keycard=True)

        wallet_view.just_fyi('Check that second "Send transaction" screen appears')
        if not send_transaction_view.sign_with_keycard_button.is_element_displayed(10):
            self.driver.fail('Second send transaction screen did not appear!')

        send_transaction_view.sign_transaction(keycard=True)

    @marks.testrail_id(5744)
    @marks.critical
    def test_keycard_send_two_transactions_one_after_another_in_dapp(self):
        sender = transaction_senders['Z']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], keycard=True)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.send_two_tx_one_by_one_button.click()
        send_transaction_view.sign_transaction(keycard=True)

        wallet_view.just_fyi('Check that second "Send transaction" screen appears')
        if not send_transaction_view.sign_with_keycard_button.is_element_displayed(20):
            self.driver.fail('Second send transaction screen did not appear!')

        send_transaction_view.sign_transaction(keycard=True)


