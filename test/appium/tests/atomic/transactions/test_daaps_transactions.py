import pytest

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
        home_view = sign_in_view.recover_access(sender['passphrase'])
        address = sender['address']
        initial_balance = self.network_api.get_balance(address)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction()
        self.network_api.verify_balance_is_updated(initial_balance, address)

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

    @marks.testrail_id(5333)
    @marks.critical
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
        send_transaction_view.sign_transaction()
        for text in 'Contract deployed at: ', 'Call contract get function', \
                    'Call contract set function', 'Call function 2 times in a row':
            if not status_test_dapp.element_by_text(text).is_element_displayed(120):
                self.driver.fail('Contract was not created')

    @marks.testrail_id(5784)
    @marks.critical
    def test_sign_typed_message(self):
        sender = transaction_senders['W']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'])
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
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
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.send_two_tx_in_batch_button.click()
        send_transaction_view.sign_transaction()

        # Check that second 'Send transaction' screen appears
        if not send_transaction_view.element_by_text('Sign with password').is_element_displayed(10):
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

        # Check that second 'Send transaction' screen appears
        if not send_transaction_view.element_by_text('Sign with password').is_element_displayed(20):
            self.driver.fail('Second send transaction screen did not appear!')

        send_transaction_view.sign_transaction()

    @marks.logcat
    @marks.testrail_id(5418)
    @marks.critical
    def test_logcat_send_transaction_from_daap(self):
        sender = transaction_senders['M']
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.recover_access(sender['passphrase'], unique_password)
        wallet_view = home_view.wallet_button.click()
        wallet_view.set_up_wallet()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(unique_password)
        values_in_logcat = send_transaction_view.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)


    @marks.logcat
    @marks.testrail_id(5420)
    @marks.critical
    def test_logcat_sign_message_from_daap(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user(password=unique_password)
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys(unique_password)
        send_transaction_view.sign_button.click()
        values_in_logcat = send_transaction_view.find_values_in_logcat(password=unique_password)
        if values_in_logcat:
            self.driver.fail(values_in_logcat)

    @marks.testrail_id(5372)
    @marks.high
    @marks.skip
    # skipped because it is part of other tests
    def test_request_eth_in_status_test_dapp(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        status_test_dapp.request_eth_button.click()
        status_test_dapp.element_by_text_part('Faucet request').wait_for_visibility_of_element()
        status_test_dapp.ok_button.click()
        status_test_dapp.cross_icon.click()
        wallet_view = sign_in_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_is_equal_expected_amount()

    @marks.testrail_id(5355)
    @marks.medium
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

        if not send_transaction_view.sign_with_password.is_element_displayed():
            self.driver.fail('It seems transaction sign screen is not shown.')

        send_transaction_view.sign_transaction()

        if not status_test_dapp.assets_button.is_element_displayed():
            self.driver.fail('It seems users was not redirected to Status DAPP screen.')

    @marks.testrail_id(5685)
    @marks.medium
    def test_not_enough_eth_for_gas_validation_from_dapp(self):
        singin_view = SignInView(self.driver)
        home_view = singin_view.create_user()
        wallet = home_view.wallet_button.click()
        wallet.set_up_wallet()
        wallet_address = wallet.get_wallet_address()
        home_view = wallet.get_back_to_home_view()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.deploy_contract_button.click()

        warning = 'No "Not enough ETH for gas" warning appeared while {}'
        sign_button_warning = 'Signin transaction button is enabled while {}'

        # Check whether deploying simple contract with an empty ETH balance triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('deploying a contract with an empty ETH balance'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_with_password.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('deploying a contract with an empty ETH balance'))

        # Requesting test ETH and waiting till the balance updates
        send_transaction_view.cancel_button.click()
        self.network_api.get_donate(wallet_address[2:])
        self.network_api.verify_balance_is_updated(initial_balance=0, recipient_address=wallet_address[2:])

        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.send_one_tx_in_batch_button.click()
        send_transaction_view.network_fee_button.click()
        gas_limit = '100000'
        send_transaction_view.gas_limit_input.clear()
        send_transaction_view.gas_limit_input.set_value(gas_limit)
        gas_price = '999.900000001'
        send_transaction_view.gas_price_input.clear()
        send_transaction_view.gas_price_input.set_value(gas_price)
        # send_transaction_view.total_fee_input.click()
        send_transaction_view.update_fee_button.click()

        # Check whether sending a tx in batch with big gas limit and price triggers the warning and sign button is still
        # disabled (no funds to pay gas)
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending one transaction in batch with big gas '
                                              'limit and price (no funds to pay gas)'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_with_password.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.
                               format('sending one transaction in batch with big gas '
                                      'limit and price (no funds to pay gas)'))

        send_transaction_view.network_fee_button.click()
        gas_price = '999.9'
        send_transaction_view.gas_price_input.clear()
        send_transaction_view.gas_price_input.set_value(gas_price)
        send_transaction_view.update_fee_button.click()
        # send_transaction_view.done_button.click()

        # Check whether sending a tx in batch with normal gas limit and price does not trigger the warning
        # so the transaction can be signed
        if send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('"Not enough ETH for gas" warning appeared while sending '
                                              'one transaction in batch with normal gas limit and price'))

        send_transaction_view.sign_transaction()
        if not status_test_dapp.assets_button.is_element_displayed():
            self.errors.append('Could not sing the transaction!')

        self.errors.verify_no_errors()

    @marks.testrail_id(5686)
    @marks.medium
    @marks.skip
    # TODO: e2e blocker: 8567 (should be enabled after fix)
    def test_not_enough_eth_for_gas_validation_from_wallet(self):
        singin_view = SignInView(self.driver)
        home_view = singin_view.create_user()
        wallet = home_view.wallet_button.click()
        wallet.set_up_wallet()
        wallet.select_asset("STT")
        wallet_address = wallet.get_wallet_address()
        recipient = '0x' + basic_user['address']

        wallet.send_transaction(asset_name='ETHro', amount=0, recipient=recipient, sign_transaction=False)
        send_transaction_view = SendTransactionView(self.driver)

        warning = 'No "Not enough ETH for gas" warning appeared while {}'
        sign_button_warning = 'Sign transaction button is enabled {}'

        # Check whether sending 0 ETH with an empty ETH balance triggers the warning
        send_transaction_view.sign_transaction_button.click()
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending 0 ETH with an empty ETH balance'))

        # Check whether sign transaction button is disabled
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('sending 0 ETH with an empty ETH balance'))

        send_transaction_view.cancel_button.click()
        asset_button = send_transaction_view.asset_by_name('STT')
        send_transaction_view.select_asset_button.click_until_presence_of_element(asset_button)
        asset_button.click()
        send_transaction_view.amount_edit_box.set_value('0')
        send_transaction_view.confirm()
        send_transaction_view.sign_transaction_button.click()

        # Check whether sending 0 STT with an empty ETH balance triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending 0 STT with an empty ETH balance'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_with_password.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('sending 0 STT with an empty ETH balance'))

        send_transaction_view.cancel_button.click()
        home_view = send_transaction_view.get_back_to_home_view()
        # Requesting test ETH and waiting till the balance updates
        self.network_api.faucet(wallet_address[2:])
        self.network_api.verify_balance_is_updated(initial_balance=0, recipient_address=wallet_address[2:])

        wallet = home_view.wallet_button.click()
        wallet.accounts_status_account.click()
        wallet.send_transaction(asset_name='ETHro', amount=0.1, recipient=recipient, sign_transaction=False)
        send_transaction_view.sign_transaction_button.click()

        # Check whether sending all available ETH triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending all available ETH (no funds to pay gas)'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_with_password.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(warning.format('sending all available ETH (no funds to pay gas)'))

        # Because tx gas price may change we calculate eth value according to current gas fee value
        transaction_fee_total = send_transaction_view.get_transaction_fee_total()
        eth_available_for_tx = str(Decimal('0.1') - Decimal(transaction_fee_total))
        wei = '0.000000000000000001'
        eth_value_plus_one_wei = ''.join([eth_available_for_tx, wei[len(eth_available_for_tx):]])
        send_transaction_view.amount_edit_box.clear()
        send_transaction_view.amount_edit_box.set_value(eth_value_plus_one_wei)
        send_transaction_view.confirm()

        # Check whether sending big amount of ETH triggers the warning (no funds to pay gas)
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending big amount of ETH (no funds to pay gas)'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_transaction_button.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(warning.format('sending big amount of ETH (no funds to pay gas)'))

        send_transaction_view.cancel_button.click()
        send_transaction_view.amount_edit_box.clear()
        send_transaction_view.amount_edit_box.set_value(eth_available_for_tx)
        send_transaction_view.confirm()
        send_transaction_view.sign_transaction_button.click()

        # Check whether sending normal amount of ETH does not trigger the warning
        if send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append('"Not enough ETH for gas" warning appeared while sending normal amount of ETH')

        send_transaction_view.sign_transaction()
        if not wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Could not sign the transaction!')

        self.errors.verify_no_errors()

    @marks.testrail_id(5687)
    @marks.medium
    @marks.skip
    # TODO: e2e blocker: 8601 (should be enabled after fix)
    def test_not_enough_eth_for_gas_validation_from_chat(self):
        signin_view = SignInView(self.driver)
        home_view = signin_view.create_user()
        recipient_public_key = basic_user['public_key']
        wallet = home_view.wallet_button.click()
        wallet.set_up_wallet()
        wallet_address = wallet.get_wallet_address()
        home_view = wallet.get_back_to_home_view()

        chat = home_view.add_contact(recipient_public_key)
        chat.send_transaction_in_1_1_chat(asset='ETHro', amount='0', wallet_set_up=False, sign_transaction=False)
        send_transaction_view = SendTransactionView(self.driver)

        warning = 'No "Not enough ETH for gas" warning appeared while {}'
        sign_button_warning = 'Signin transaction button is enabled {}'

        # Check whether sending 0 ETH with an empty ETH balance triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending 0 ETH with an empty ETH balance'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_transaction_button.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('sending 0 ETH with an empty ETH balance'))

        send_transaction_view.cross_icon.click()
        chat.send_transaction_in_1_1_chat(asset='STT', amount='0', wallet_set_up=False, sign_transaction=False)

        # Check whether sending 0 STT with an empty ETH balance triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending 0 STT with an empty ETH balance'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_transaction_button.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('sending 0 STT with an empty ETH balance'))

        send_transaction_view.cross_icon.click()
        # Requesting test ETH and waiting till the balance updates
        self.network_api.faucet(wallet_address[2:])
        self.network_api.verify_balance_is_updated(initial_balance=0, recipient_address=wallet_address[2:])
        chat.send_transaction_in_1_1_chat(asset='ETHro', amount='0.1', wallet_set_up=False, sign_transaction=False)

        # Check whether sending all available ETH triggers the warning
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append(warning.format('sending all available ETH'))

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_transaction_button.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append(sign_button_warning.format('sending all available ETH'))

        chat.send_transaction_in_1_1_chat(asset='ETHro', amount='0.099979000000000001',
                                          wallet_set_up=False, sign_transaction=False)

        # Check whether sending big amount of ETH triggers the warning (no funds to pay gas)
        if not send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append('sending big amount of ETH (no funds to pay gas)')

        # Check whether sign transaction button is disabled
        send_transaction_view.sign_transaction_button.click()
        if send_transaction_view.enter_password_input.is_element_displayed():
            self.errors.append('sending big amount of ETH (no funds to pay gas)')

        chat.send_transaction_in_1_1_chat(asset='ETHro', amount='0.099979', wallet_set_up=False, sign_transaction=False)
        # Check whether sending normal amount of ETH does not trigger the warning
        if send_transaction_view.validation_warnings.not_enough_eth_for_gas.is_element_displayed():
            self.errors.append('"Not enough ETH for gas" warning appeared while sending normal amount of ETH')

        send_transaction_view.sign_transaction()
        if not wallet.send_transaction_button.is_element_displayed():
            self.errors.append('Could not sing the transaction!')

        self.errors.verify_no_errors()
