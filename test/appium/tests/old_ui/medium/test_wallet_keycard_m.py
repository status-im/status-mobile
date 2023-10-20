import random
import string

import pytest

from tests import common_password, marks, test_dapp_name
from tests.base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests.users import transaction_senders, basic_user, ens_user_message_sender
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name='three_1')
@marks.medium
class TestKeycardMediumMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.user = transaction_senders['ETH_STT_4']
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.recover_access(passphrase=self.user['passphrase'], keycard=True)
        self.profile = self.home.get_profile_view()
        self.wallet = self.home.wallet_button.click()
        self.wallet.wait_balance_is_changed('STT')
        self.home.home_button.click()

    @marks.testrail_id(702317)
    def test_keycard_testdapp_sign_typed_message(self):
        test_dapp = self.home.open_status_test_dapp()
        test_dapp.wait_for_d_aap_to_load()
        test_dapp.transactions_button.click_until_presence_of_element(test_dapp.sign_typed_message_button)
        send_transaction = test_dapp.sign_typed_message_button.click()
        send_transaction.sign_with_keycard_button.click()
        keycard = send_transaction.sign_with_keycard_button.click()
        keycard.enter_default_pin()
        if not keycard.element_by_text_part('0xc053c4').is_element_displayed():
            self.errors.append('Typed message was not signed')
        self.errors.verify_no_errors()

    @marks.testrail_id(702318)
    def test_keycard_testdapp_deploy_simple_contract_send_tx(self):
        test_dapp = self.home.open_status_test_dapp()
        test_dapp.wait_for_d_aap_to_load()
        test_dapp.transactions_button.click_until_presence_of_element(test_dapp.sign_typed_message_button)
        self.home.just_fyi("Checking deploy simple contract")
        send_tx = test_dapp.deploy_contract_button.click()
        send_tx.sign_transaction(keycard=True)
        if not test_dapp.element_by_text('Contract deployed at: ').is_element_displayed(300):
            self.drivers[0].fail('Contract was not created or tx taking too long')
        for text in ['Call contract get function',
                     'Call contract set function', 'Call function 2 times in a row']:
            test_dapp.element_by_text(text).scroll_to_element()

    @marks.testrail_id(702319)
    def test_keycard_send_tx_eth_to_ens(self):
        self.home.home_button.double_click()
        chat = self.home.add_contact(ens_user_message_sender['ens'])
        chat.commands_button.click()
        amount = chat.get_unique_amount()

        send_message = chat.send_command.click()
        send_message.amount_edit_box.send_keys(amount)
        send_message.confirm()
        send_message.next_button.click()

        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.drivers[0])
        send_transaction.sign_transaction(keycard=True)
        chat_sender_message = chat.get_outgoing_transaction()
        self.network_api.wait_for_confirmation_of_transaction(self.user['address'], amount)
        chat_sender_message.transaction_status.wait_for_element_text(chat_sender_message.confirmed, 60)

    @marks.testrail_id(702320)
    def test_keycard_profile_pin_puk_edit(self):
        profile = self.home.profile_button.click()

        self.home.just_fyi("Checking changing PIN")
        profile.keycard_button.scroll_and_click()
        keycard = profile.change_pin_button.click()
        keycard.enter_another_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_default_pin()
        if not keycard.element_by_translation_id("new-pin-description").is_element_displayed():
            self.home.driver.fail("Screen for setting new pin is not shown!")
        [keycard.enter_another_pin() for _ in range(2)]
        if not keycard.element_by_translation_id("pin-changed").is_element_displayed(30):
            self.home.driver.fail("Popup about successful setting new PIN is not shown!")
        keycard.ok_button.click()

        self.home.just_fyi("Checking changing PUK with new PIN")
        profile.change_puk_button.click()
        keycard.enter_another_pin()
        if not keycard.element_by_translation_id("new-puk-description").is_element_displayed():
            self.home.driver.fail("Screen for setting new puk is not shown!")
        [keycard.one_button.click() for _ in range(12)]
        if not keycard.element_by_translation_id("repeat-puk").is_element_displayed():
            self.home.driver.fail("Confirmation screen for setting new puk is not shown!")
        [keycard.one_button.click() for _ in range(12)]
        if not keycard.element_by_translation_id("puk-changed").is_element_displayed(30):
            self.home.driver.fail("Popup about successful setting new PUK is not shown!")
        keycard.ok_button.click()

        self.home.just_fyi("Setting PIN back")
        self.home.profile_button.double_click()
        profile.keycard_button.scroll_and_click()
        keycard = profile.change_pin_button.click()
        keycard.enter_another_pin()
        [keycard.enter_default_pin() for _ in range(2)]
        if not keycard.element_by_translation_id("pin-changed").is_element_displayed(30):
            self.home.driver.fail("Popup about successful setting new PIN is not shown!")
        keycard.ok_button.click()

    @marks.testrail_id(702321)
    def test_keycard_profile_pairing_code_set(self):
        self.home.profile_button.double_click()
        self.home.just_fyi("Checking setting pairing with new PIN")
        profile = self.home.get_profile_view()
        profile.keycard_button.scroll_and_click()
        keycard = profile.change_pairing_code_button.click()
        keycard.enter_default_pin()
        self.sign_in.create_password_input.wait_for_element()
        self.sign_in.create_password_input.send_keys(common_password)
        self.sign_in.confirm_your_password_input.send_keys(common_password + "1")
        if not keycard.element_by_translation_id("pairing-code_error1").is_element_displayed():
            self.errors.append("No error is shown when pairing codes don't match")
        self.sign_in.confirm_your_password_input.delete_last_symbols(1)
        self.sign_in.element_by_translation_id("change-pairing").click()
        if not keycard.element_by_translation_id("pairing-changed").is_element_displayed(30):
            self.home.driver.fail("Popup about successful setting new pairing is not shown!")
        keycard.ok_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702322)
    def test_keycard_profile_backup_card(self):
        self.home.profile_button.double_click()
        profile = self.home.get_profile_view()
        profile.keycard_button.scroll_and_click()

        self.home.just_fyi("Checking backing up keycard")
        profile.create_keycard_backup_button.scroll_to_element()
        keycard = profile.create_keycard_backup_button.click()
        self.sign_in.seedphrase_input.send_keys(self.user['passphrase'])
        self.sign_in.next_button.click()
        keycard.return_card_to_factory_settings_checkbox.enable()
        keycard.begin_setup_button.click()
        keycard.yes_button.wait_and_click()
        [keycard.enter_default_pin() for _ in range(2)]
        keycard.element_by_translation_id("keycard-backup-success-title").wait_for_element(30)
        keycard.ok_button.click()

    @marks.testrail_id(702323)
    def test_keycard_same_seed_added_inside_multiaccount_send_tx_login(self):
        recipient = "0x" + transaction_senders['ETH_1']['address']
        self.profile.profile_button.double_click()
        self.profile.logout()

        self.sign_in.just_fyi('Create new multiaccount')
        self.sign_in.close_button.click()
        self.sign_in.your_keys_more_icon.click()
        self.sign_in.generate_new_key_button.click()
        self.sign_in.next_button.click()
        self.sign_in.next_button.click()
        self.sign_in.create_password_input.send_keys(common_password)
        self.sign_in.next_button.click()
        self.sign_in.confirm_your_password_input.send_keys(common_password)
        self.sign_in.next_button.click()
        self.sign_in.maybe_later_button.click_until_presence_of_element(self.sign_in.start_button)
        self.sign_in.start_button.click()

        self.sign_in.just_fyi('Add to wallet seed phrase for restored multiaccount')
        wallet = self.sign_in.wallet_button.click()
        wallet.add_account_button.click()
        wallet.enter_a_seed_phrase_button.click()
        wallet.enter_your_password_input.send_keys(common_password)
        account_name = 'subacc'
        wallet.account_name_input.send_keys(account_name)
        wallet.enter_seed_phrase_input.send_keys(self.user['passphrase'])
        wallet.add_account_generate_account_button.click()
        wallet.get_account_by_name(account_name).click()

        self.sign_in.just_fyi('Send transaction from added account and log out')
        transaction_amount_added = wallet.get_unique_amount()
        wallet.send_transaction(from_main_wallet=False, amount=transaction_amount_added, recipient=recipient,
                                sign_transaction=True)
        wallet.profile_button.click()
        self.profile.logout()

        self.sign_in.just_fyi('Login to keycard account and send another transaction')
        self.sign_in.navigate_up_button.click()
        self.sign_in.sign_in(position=2, keycard=True)
        self.sign_in.wallet_button.click()
        wallet.wait_balance_is_changed('ETH')
        transaction_amount_keycard = wallet.get_unique_amount()
        wallet.send_transaction(amount=transaction_amount_keycard, recipient=recipient, keycard=True,
                                sign_transaction=True)

        for amount in [transaction_amount_keycard, transaction_amount_added]:
            self.sign_in.just_fyi("Checking '%s' tx" % amount)
            self.network_api.find_transaction_by_unique_amount(self.user['address'], amount)


@pytest.mark.xdist_group(name='one_1')
@marks.medium
class TestWalletTestDappMediumMultipleDevicesMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.user = transaction_senders['ETH_5']
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.home = self.sign_in.recover_access(passphrase=self.user['passphrase'])
        self.profile = self.home.get_profile_view()
        self.wallet = self.home.wallet_button.click()
        self.wallet.wait_balance_is_changed()
        self.wallet.just_fyi('create new account in multiaccount')
        self.status_account = self.home.status_account_name
        self.account_name = 'Subaccount'
        self.wallet.add_account(self.account_name)
        self.sub_acc_address = self.wallet.get_wallet_address(self.account_name)
        self.home.wallet_button.double_click()

    @marks.testrail_id(702324)
    def test_testdapp_request_public_key(self):
        self.home.just_fyi("Checking requesting public key from dapp")
        test_dapp = self.home.open_status_test_dapp(allow_all=False)
        test_dapp.status_api_button.click_until_presence_of_element(test_dapp.request_contact_code_button)
        test_dapp.request_contact_code_button.click_until_presence_of_element(test_dapp.deny_button)
        test_dapp.deny_button.click()
        if test_dapp.element_by_text(self.user['public_key']).is_element_displayed():
            self.errors.append('Public key is returned but access was not allowed')
        test_dapp.request_contact_code_button.click_until_presence_of_element(test_dapp.deny_button)
        test_dapp.allow_button.click()
        if not test_dapp.element_by_text(self.user['public_key']).is_element_displayed():
            self.errors.append('Public key is not returned')
        self.errors.verify_no_errors()

    @marks.testrail_id(702325)
    def test_testdapp_sign_typed_message(self):
        self.home.just_fyi("Checking sign typed message")
        test_dapp = self.home.open_status_test_dapp(allow_all=True)
        test_dapp.transactions_button.click_until_presence_of_element(test_dapp.sign_typed_message_button)
        send_transaction = test_dapp.sign_typed_message_button.click()
        send_transaction.enter_password_input.send_keys(common_password)
        send_transaction.sign_button.click_until_absense_of_element(send_transaction.sign_button)
        if not test_dapp.element_by_text_part('0x0876752fe').is_element_displayed(30):
            self.errors.append("Hash of signed typed message is not shown!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702326)
    def test_testdapp_deploy_simple_contract_send_tx(self):
        test_dapp = self.home.open_status_test_dapp()
        test_dapp.transactions_button.click_until_presence_of_element(test_dapp.deploy_contract_button)
        send_transaction = test_dapp.deploy_contract_button.click()
        send_transaction.sign_transaction()
        if not test_dapp.element_by_text('Contract deployed at: ').is_element_displayed(240):
            self.errors.append('Contract was not created')
        for text in ['Call contract get function',
                     'Call contract set function', 'Call function 2 times in a row']:
            test_dapp.element_by_text(text).scroll_to_element()
        self.errors.verify_no_errors()

    @marks.testrail_id(702327)
    def test_wallet_currency_set_search(self):
        self.home.wallet_button.double_click()

        self.home.just_fyi('Searching for currency')
        search_list_currencies = {
            'aF': ['Afghanistan Afghani (AFN)', 'South Africa Rand (ZAR)'],
            'bolívi': ['Bolivia Bolíviano (BOB)']
        }
        self.wallet.multiaccount_more_options.click_until_presence_of_element(self.wallet.set_currency_button)
        self.wallet.set_currency_button.click()
        for keyword in search_list_currencies:
            self.home.search_by_keyword(keyword)
            search_elements = self.wallet.currency_item_text.find_elements()
            if not search_elements:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            search_results = [element.text for element in search_elements]
            if search_results != search_list_currencies[keyword]:
                self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                   (', '.join(search_results), keyword))
            self.home.cancel_button.click()
        user_currency = 'Euro (EUR)'

        self.home.wallet_button.click()
        self.home.just_fyi('Searching for currency')
        self.wallet.set_currency(user_currency)
        if not self.wallet.element_by_text_part('EUR').is_element_displayed(20):
            self.wallet.driver.fail('EUR currency is not displayed')
        self.errors.verify_no_errors()

    @marks.testrail_id(702328)
    def test_wallet_asset_search(self):
        self.home.wallet_button.double_click()
        search_list_assets = {
            'ee': ['XEENUS', 'YEENUS', 'ZEENUS'],
            'ST': ['STT']
        }
        self.home.just_fyi('Searching for asset by name and symbol')
        self.wallet.multiaccount_more_options.click()
        self.wallet.manage_assets_button.click()
        for keyword in search_list_assets:
            self.home.search_by_keyword(keyword)
            if keyword == 'd':
                search_elements = self.wallet.all_assets_full_names.find_elements()
            else:
                search_elements = self.wallet.all_assets_symbols.find_elements()
            if not search_elements:
                self.errors.append('No search results after searching by %s keyword' % keyword)
            search_results = [element.text for element in search_elements]
            if search_results != search_list_assets[keyword]:
                self.errors.append("'%s' is shown on the home screen after searching by '%s' keyword" %
                                   (', '.join(search_results), keyword))
            self.home.cancel_button.click()
        self.wallet.get_back_to_home_view()
        self.errors.verify_no_errors()

    @marks.testrail_id(702329)
    def test_wallet_testdapp_switch_account(self):
        self.home.just_fyi('can see two accounts in DApps')
        dapp = self.home.dapp_tab_button.click()
        dapp.select_account_button.click()
        for text in 'Select the account', self.status_account, self.account_name:
            if not dapp.element_by_text_part(text).is_element_displayed():
                self.wallet.driver.fail("No expected element %s is shown in menu" % text)
        self.home.click_system_back_button()

        self.home.just_fyi('add permission to Status account')
        status_test_dapp = self.home.open_status_test_dapp()

        self.home.just_fyi('check that permissions from previous account was removed once you choose another')
        dapp.select_account_button.click()
        dapp.select_account_by_name(self.account_name).wait_for_element(30)
        dapp.select_account_by_name(self.account_name).click()
        profile = dapp.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        if profile.element_by_text(test_dapp_name).is_element_displayed():
            self.errors.append("Permissions for %s are not removed" % test_dapp_name)

        self.home.just_fyi('check that can change account')
        profile.dapp_tab_button.click()
        if not status_test_dapp.element_by_text_part(self.account_name).is_element_displayed():
            self.errors.append("No expected account %s is shown in authorize web3 popup for wallet" % self.account_name)
        status_test_dapp.allow_button.click()
        dapp.profile_button.click(desired_element_text='DApp permissions')
        profile.element_by_text(test_dapp_name).click()
        for text in 'Chat key', self.account_name:
            if not dapp.element_by_text_part(text).is_element_displayed():
                self.errors.append("Access is not granted to %s" % text)

        self.home.just_fyi('check correct account is shown for transaction if sending from DApp')
        profile.dapp_tab_button.click(desired_element_text='Accounts')
        status_test_dapp.transactions_button.click_until_presence_of_element(
            status_test_dapp.send_one_tx_in_batch_button)
        send_transaction = status_test_dapp.send_one_tx_in_batch_button.click()
        send_transaction.ok_got_it_button.click_if_shown()
        address = send_transaction.get_formatted_recipient_address(self.sub_acc_address)
        if not send_transaction.element_by_text(address).is_element_displayed():
            self.errors.append("Wallet address %s in not shown in 'From' on Send Transaction screen" % address)

        self.home.just_fyi('Relogin and check multiaccount loads fine')
        self.home.reopen_app()
        self.home.wallet_button.click()
        if not self.wallet.element_by_text(self.account_name).is_element_displayed():
            self.errors.append("Subaccount is gone after relogin in Wallet!")
        self.home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        profile.element_by_text(test_dapp_name).click()
        if not profile.element_by_text(self.account_name).is_element_displayed():
            self.errors.append("Subaccount is not selected after relogin in Dapps!")
        self.errors.verify_no_errors()

    @marks.testrail_id(702330)
    def test_wallet_can_change_account_settings(self):
        self.wallet.wallet_button.double_click()
        status_account_address = self.wallet.get_wallet_address()
        self.wallet.get_account_options_by_name().click()

        self.wallet.just_fyi('open Account Settings screen and check that all elements are shown')
        self.wallet.account_settings_button.click()
        for text in 'On Status tree', status_account_address, "m/44'/60'/0'/0/0":
            if not self.wallet.element_by_text(text).is_element_displayed():
                self.errors.append("'%s' text is not shown on Account Settings screen!" % text)

        self.wallet.just_fyi('change account name/color and verified applied changes')
        account_name = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(10))
        self.wallet.account_name_input.clear()
        self.wallet.account_name_input.send_keys(account_name)
        self.wallet.account_color_button.select_color_by_position(1)
        self.wallet.apply_settings_button.click()
        self.wallet.element_by_text('This device').scroll_to_element()
        self.wallet.close_button.click()
        self.wallet.close_button.click()
        account_button = self.wallet.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.wallet.driver.fail('Account name was not changed')
        if not account_button.color_matches('multi_account_color.png'):
            self.wallet.driver.fail('Account color does not match expected')
        self.errors.verify_no_errors()

    @marks.testrail_id(702331)
    def test_wallet_offline_can_login_cant_send_transaction(self):
        self.wallet.wallet_button.double_click()
        self.wallet.toggle_airplane_mode()
        send_transaction = self.wallet.send_transaction_from_main_screen.click()
        send_transaction.set_recipient_address('0x%s' % basic_user['address'])
        send_transaction.amount_edit_box.send_keys("0")
        send_transaction.confirm()
        send_transaction.sign_transaction_button.click()
        if send_transaction.sign_with_password.is_element_displayed():
            self.wallet.driver.fail("Sign transaction button is active in offline mode")
        self.home.reopen_app()
        self.home.connection_offline_icon.wait_for_visibility_of_element(20)
