import re
import random
from tests import marks, mailserver_ams, mailserver_gc, mailserver_hk, used_fleet, common_password,\
    pair_code, unique_password
from tests.users import user_mainnet, chat_users, recovery_users, transaction_senders, basic_user,\
    wallet_users, ens_user_message_sender, ens_user
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
import support.api.web3_api as w3


@marks.medium
class TestChatManagement(SingleDeviceTestCase):

    @marks.testrail_id(6243)
    def test_keycard_can_recover_keycard_account_offline_and_add_watch_only_acc(self):
        sign_in = SignInView(self.driver)
        sign_in.toggle_airplane_mode()

        sign_in.just_fyi('Recover multiaccount offline')
        sign_in.accept_tos_checkbox.enable()
        sign_in.get_started_button.click_until_presence_of_element(sign_in.access_key_button)
        sign_in.access_key_button.click()
        sign_in.recover_with_keycard_button.click()
        keycard_view = sign_in.begin_recovery_button.click()
        keycard_view.connect_pairing_card_button.click()
        keycard_view.pair_code_input.send_keys(pair_code)
        keycard_view.confirm()
        keycard_view.enter_default_pin()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.start_button)
        sign_in.start_button.click_until_absense_of_element(sign_in.start_button)
        sign_in.home_button.wait_for_visibility_of_element(30)
        wallet_view = sign_in.wallet_button.click()

        sign_in.just_fyi('Relogin offline')
        self.driver.close_app()
        self.driver.launch_app()
        sign_in.sign_in(keycard=True)
        if not sign_in.home_button.is_element_displayed(10):
            self.driver.fail('Keycard user is not logged in')

        sign_in.just_fyi('Turn off airplane mode and turn on cellular network')
        sign_in.toggle_airplane_mode()
        sign_in.toggle_mobile_data()
        sign_in.element_by_text_part('Stop syncing').wait_and_click(60)
        sign_in.wallet_button.click()
        if not wallet_view.element_by_text_part('XEENUS').is_element_displayed():
            self.errors.append('Token balance is not fetched while on cellular network!')

        wallet_view.just_fyi('Add watch-only account when on cellular network')
        wallet_view.add_account_button.click()
        wallet_view.add_watch_only_address_button.click()
        wallet_view.enter_address_input.send_keys(basic_user['address'])
        account_name = 'watch-only'
        wallet_view.account_name_input.send_keys(account_name)
        wallet_view.add_account_generate_account_button.click()
        account_button = wallet_view.get_account_by_name(account_name)
        if not account_button.is_element_displayed():
            self.driver.fail('Account was not added')

        wallet_view.just_fyi('Check that balance is changed after go back to WI-FI')
        sign_in.toggle_mobile_data()
        for asset in ('YEENUS', 'STT'):
            wallet_view.asset_by_name(asset).scroll_to_element()
            wallet_view.wait_balance_is_changed(asset, wait_time=60)

        wallet_view.just_fyi('Delete watch-only account')
        wallet_view.get_account_by_name(account_name).click()
        wallet_view.get_account_options_by_name(account_name).click()
        wallet_view.account_settings_button.click()
        wallet_view.delete_account_button.click()
        wallet_view.yes_button.click()
        if wallet_view.get_account_by_name(account_name).is_element_displayed(20):
            self.errors.append('Account was not deleted')

        self.errors.verify_no_errors()

    @marks.testrail_id(6292)
    def test_keycard_send_funds_between_accounts_set_max_in_multiaccount_instance(self):
        sign_in = SignInView(self.driver).create_user(keycard=True)
        wallet = sign_in.wallet_button.click()
        status_account_address = wallet.get_wallet_address()[2:]
        w3.donate_testnet_eth('0x%s' % status_account_address, 0.05)
        wallet.wallet_button.double_click()
        account_name = 'subaccount'
        wallet.add_account(account_name, keycard=True)
        wallet.get_account_by_name(account_name).click()
        wallet.get_account_options_by_name(account_name).click()
        wallet.account_settings_button.click()
        wallet.swipe_up()

        wallet.just_fyi("Checking that delete account and importing account are not available on keycard")
        if wallet.delete_account_button.is_element_displayed(10):
            self.errors.append('Delete account option is shown on added account "On Status Tree"!')
        wallet.wallet_button.double_click()
        wallet.add_account_button.click()
        if wallet.enter_a_seed_phrase_button.is_element_displayed():
            self.errors.append('Importing account option is available on keycard!')
        wallet.click_system_back_button()

        wallet.just_fyi("Send transaction to new account")
        transaction_amount = '0.006'
        initial_balance = self.network_api.get_balance(status_account_address)
        wallet.send_transaction(account_name=account_name, amount=transaction_amount, keycard=True)
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount)
        self.network_api.verify_balance_is_updated(str(initial_balance), status_account_address)

        wallet.just_fyi("Verifying previously sent transaction in new account")
        wallet.get_account_by_name(account_name).click()
        wallet.send_transaction_button.click()
        wallet.close_send_transaction_view_button.click()
        balance_after_receiving_tx = float(wallet.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(balance_after_receiving_tx, transaction_amount)
        if balance_after_receiving_tx != expected_balance:
            self.driver.fail('New account balance %s does not match expected %s after receiving a transaction' % (
                balance_after_receiving_tx, transaction_amount))

        wallet.just_fyi("Sending eth from new account to main account")
        updated_balance = self.network_api.get_balance(status_account_address)
        transaction_amount_1 = round(float(transaction_amount) * 0.2, 11)
        wallet.wait_balance_is_changed()
        wallet.get_account_by_name(account_name).click()
        send_transaction = wallet.send_transaction(from_main_wallet=False, account_name=wallet.status_account_name,
                                                   amount=transaction_amount_1, keycard=True)
        wallet.close_button.click()
        sub_account_address = wallet.get_wallet_address(account_name)[2:]
        self.network_api.wait_for_confirmation_of_transaction(sub_account_address, transaction_amount_1)
        wallet.find_transaction_in_history(amount=format(float(transaction_amount_1), '.11f').rstrip('0'))

        wallet.just_fyi("Check transactions on subaccount")
        self.network_api.verify_balance_is_updated(updated_balance, status_account_address)

        wallet.just_fyi("Verify total ETH on main wallet view")
        self.network_api.wait_for_confirmation_of_transaction(status_account_address, transaction_amount_1)
        self.network_api.verify_balance_is_updated((updated_balance + transaction_amount_1), status_account_address)
        wallet.close_button.click()
        balance_of_sub_account = float(self.network_api.get_balance(sub_account_address)) / 1000000000000000000
        balance_of_status_account = float(self.network_api.get_balance(status_account_address)) / 1000000000000000000
        wallet.scan_tokens()
        total_eth_from_two_accounts = float(wallet.get_asset_amount_by_name('ETH'))
        expected_balance = self.network_api.get_rounded_balance(total_eth_from_two_accounts,
                                                                (balance_of_status_account + balance_of_sub_account))

        if total_eth_from_two_accounts != expected_balance:
            self.driver.fail('Total wallet balance %s != of Status account (%s) + SubAccount (%s)' % (
                total_eth_from_two_accounts, balance_of_status_account, balance_of_sub_account))

        wallet.just_fyi("Check that can set max and send transaction with max amount from subaccount")
        wallet.get_account_by_name(account_name).click()
        wallet.send_transaction_button.click()
        send_transaction.set_max_button.click()
        set_amount = float(send_transaction.amount_edit_box.text)
        if set_amount == 0.0 or set_amount >= balance_of_sub_account:
            self.driver.fail('Value after setting up max amount is set to %s' % str(set_amount))
        send_transaction.confirm()
        send_transaction.chose_recipient_button.click()
        send_transaction.accounts_button.click()
        send_transaction.element_by_text(wallet.status_account_name).click()
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction(keycard=True)
        wallet.element_by_text('Assets').click()
        wallet.wait_balance_is_equal_expected_amount(asset='ETH', expected_balance=0, main_screen=False)
        wallet.donate_leftovers(keycard=True)

    @marks.testrail_id(5742)
    def test_keycard_onboarding_interruption_creating_flow(self):
        sign_in = SignInView(self.driver)

        sign_in.just_fyi('Cancel on PIN code setup stage')
        sign_in.accept_tos_checkbox.enable()
        sign_in.get_started_button.click()
        sign_in.generate_key_button.click()
        username = sign_in.first_username_on_choose_chat_name.text
        sign_in.next_button.click()
        keycard_flow = sign_in.keycard_storage_button.click()
        keycard_flow.next_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.connect_card_button.wait_and_click()
        keycard_flow.enter_another_pin()
        keycard_flow.cancel_button.click()

        sign_in.just_fyi('Cancel from Confirm seed phrase: initialized + 1 pairing slot is used')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        seed_phrase = keycard_flow.get_seed_phrase()
        keycard_flow.confirm_button.click()
        keycard_flow.yes_button.click()
        keycard_flow.cancel_button.click()
        if not keycard_flow.element_by_text_part('Back up seed phrase').is_element_displayed():
            self.driver.fail('On canceling setup from Confirm seed phrase was not redirected to expected screen')

        sign_in.just_fyi('Cancel from Back Up seed phrase: initialized + 1 pairing slot is used')
        keycard_flow.cancel_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.element_by_translation_id("back-up-seed-phrase").wait_for_element(10)
        new_seed_phrase = keycard_flow.get_seed_phrase()
        if new_seed_phrase != seed_phrase:
            self.errors.append('Another seed phrase is shown after cancelling setup during Back up seed phrase')
        keycard_flow.backup_seed_phrase()
        keycard_flow.enter_default_pin()
        for element in sign_in.maybe_later_button, sign_in.start_button:
            element.wait_for_visibility_of_element(30)
            element.click()
        sign_in.profile_button.wait_for_visibility_of_element(30)

        sign_in.just_fyi('Check username and relogin')
        profile = sign_in.get_profile_view()
        public_key, real_username = profile.get_public_key()
        if real_username != username:
            self.errors.append('Username was changed after interruption of creating account')
        profile.logout()
        home = sign_in.sign_in(keycard=True)
        if not home.wallet_button.is_element_displayed(10):
            self.errors.append("Failed to login to Keycard account")
        self.errors.verify_no_errors()

    @marks.testrail_id(6246)
    def test_keycard_onboarding_interruption_access_key_flow(self):
        sign_in = SignInView(self.driver)
        sign_in.accept_tos_checkbox.enable()
        sign_in.get_started_button.click()

        sign_in.access_key_button.click()
        sign_in.enter_seed_phrase_button.click()
        sign_in.seedphrase_input.click()
        sign_in.seedphrase_input.send_keys(basic_user['passphrase'])
        sign_in.next_button.click()
        sign_in.reencrypt_your_key_button.click()
        keycard_flow = sign_in.keycard_storage_button.click()

        sign_in.just_fyi('Cancel on PIN code setup stage')
        keycard_flow.next_button.click()
        keycard_flow.begin_setup_button.click()
        keycard_flow.connect_card_button.wait_and_click()
        keycard_flow.enter_another_pin()
        keycard_flow.cancel_button.click()

        sign_in.just_fyi('Finish setup and relogin')
        keycard_flow.begin_setup_button.click()
        keycard_flow.enter_default_pin()
        keycard_flow.enter_default_pin()
        for element in sign_in.maybe_later_button, sign_in.start_button:
            element.wait_for_visibility_of_element(30)
            element.click()
        sign_in.profile_button.wait_for_visibility_of_element(30)
        public_key, default_username = sign_in.get_public_key()
        profile_view = sign_in.get_profile_view()
        if public_key != basic_user['public_key']:
            self.errors.append('Public key %s does not match expected' % public_key)
        if default_username != basic_user['username']:
            self.errors.append('Default username %s does not match expected' % default_username)
        profile_view.logout()
        home = sign_in.sign_in(keycard=True)
        if not home.wallet_button.is_element_displayed(10):
            self.errors.append("Failed to login to Keycard account")
        self.errors.verify_no_errors()

    @marks.testrail_id(695851)
    def test_keycard_frozen_card_flows(self):
        sign_in = SignInView(self.driver)
        seed = basic_user['passphrase']
        home = sign_in.recover_access(passphrase=seed, keycard=True)
        profile = home.profile_button.click()
        profile.keycard_button.scroll_and_click()

        home.just_fyi('Set new PUK')
        keycard = profile.change_puk_button.click()
        keycard.enter_default_pin()
        [keycard.enter_default_puk() for _ in range(2)]
        keycard.ok_button.click()

        home.just_fyi("Checking reset with PUK when logged in")
        keycard = profile.change_pin_button.click()
        keycard.enter_another_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_another_pin()
        keycard.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_another_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed(30):
            self.driver.fail("No popup about frozen keycard is shown!")
        home.element_by_translation_id("keycard-is-frozen-reset").click()
        keycard.enter_another_pin()
        home.element_by_text_part('2/2').wait_for_element(20)
        keycard.enter_another_pin()
        home.element_by_translation_id("enter-puk-code").click()
        keycard.enter_default_puk()
        home.element_by_translation_id("keycard-access-reset").wait_for_element(20)
        home.profile_button.double_click()
        profile.logout()

        home.just_fyi("Checking reset with PUK when logged out")
        keycard.enter_default_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_default_pin()
        keycard.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_default_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed():
            self.driver.fail("No popup about frozen keycard is shown!")
        home.element_by_translation_id("keycard-is-frozen-reset").click()
        keycard.enter_another_pin()
        home.element_by_text_part('2/2').wait_for_element(20)
        keycard.enter_another_pin()
        home.element_by_translation_id("enter-puk-code").click()
        keycard.enter_default_puk()
        home.element_by_translation_id("keycard-access-reset").wait_for_element(20)
        home.element_by_translation_id("open").click()

        home.just_fyi("Checking reset with seed when logged in")
        profile = home.profile_button.click()
        profile.keycard_button.scroll_and_click()
        profile.change_pin_button.click()
        keycard.enter_default_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_default_pin()
        keycard.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_default_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed():
            self.driver.fail("No popup about frozen keycard is shown!")
        home.element_by_translation_id("dismiss").click()
        profile.profile_button.double_click()
        profile.keycard_button.scroll_and_click()
        profile.change_pin_button.click()
        keycard.enter_default_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed(30):
            self.driver.fail("No reset card flow is shown for frozen card")
        home.element_by_translation_id("keycard-is-frozen-factory-reset").click()
        sign_in.seedphrase_input.send_keys(transaction_senders['A']['passphrase'])
        sign_in.next_button.click()
        if not home.element_by_translation_id("seed-key-uid-mismatch").is_element_displayed():
            self.driver.fail("No popup about mismatch in seed phrase is shown!")
        home.element_by_translation_id("try-again").click()
        sign_in.seedphrase_input.clear()
        sign_in.seedphrase_input.send_keys(seed)
        sign_in.next_button.click()
        keycard.begin_setup_button.click()
        keycard.yes_button.click()
        keycard.enter_default_pin()
        home.element_by_translation_id("intro-wizard-title5").wait_for_element(20)
        keycard.enter_default_pin()
        home.element_by_translation_id("keycard-access-reset").wait_for_element(30)
        home.ok_button.click()
        profile.profile_button.double_click()
        profile.logout()

        home.just_fyi("Checking reset with seed when logged out")
        keycard.enter_another_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_another_pin()
        keycard.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_another_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed():
            self.driver.fail("No popup about frozen keycard is shown!")

        sign_in.element_by_translation_id("keycard-is-frozen-factory-reset").click()
        sign_in.seedphrase_input.send_keys(seed)
        sign_in.next_button.click()
        keycard.begin_setup_button.click()
        keycard.yes_button.click()
        keycard.enter_default_pin()
        home.element_by_translation_id("intro-wizard-title5").wait_for_element(20)
        keycard.enter_default_pin()
        home.element_by_translation_id("keycard-access-reset").wait_for_element(30)
        home.ok_button.click()
        keycard.enter_default_pin()
        home.home_button.wait_for_element(30)

    @marks.testrail_id(695852)
    def test_keycard_blocked_card_lost_or_frozen_flows(self):
        sign_in = SignInView(self.driver)
        seed = basic_user['passphrase']
        home = sign_in.recover_access(passphrase=seed, keycard=True)
        profile = home.profile_button.click()
        profile.keycard_button.scroll_and_click()

        home.just_fyi("Checking blocked card screen when entering 3 times invalid PIN + 5 times invalid PUK")
        keycard = profile.change_pin_button.click()
        keycard.enter_another_pin()
        keycard.wait_for_element_starts_with_text('2 attempts left', 30)
        keycard.enter_another_pin()
        keycard.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_another_pin()
        if not home.element_by_translation_id("keycard-is-frozen-title").is_element_displayed():
            self.driver.fail("No popup about frozen keycard is shown!")
        home.element_by_translation_id("keycard-is-frozen-reset").click()
        keycard.enter_another_pin()
        home.element_by_text_part('2/2').wait_for_element(20)
        keycard.enter_another_pin()
        home.element_by_translation_id("enter-puk-code").click()

        for i in range(1, 4):
            keycard.enter_default_puk()
            sign_in.wait_for_element_starts_with_text('%s attempts left' % str(5 - i))
            i += 1
        keycard.enter_default_puk()
        sign_in.element_by_text_part('one attempt').wait_for_element(30)
        keycard.enter_default_puk()
        keycard.element_by_translation_id("keycard-is-blocked-title").wait_for_element(30)
        keycard.close_button.click()
        if not keycard.element_by_translation_id("keycard-blocked").is_element_displayed():
            self.errors.append("In keycard settings there is no info that card is blocked")
        keycard.navigate_up_button.click()
        profile.logout()

        home.just_fyi("Check blocked card when user is logged out and use lost or frozen to restore access")
        keycard.enter_another_pin()
        keycard.element_by_translation_id("keycard-is-blocked-title").wait_for_element(30)
        keycard.element_by_translation_id("keycard-recover").click()
        keycard.yes_button.click()
        sign_in.seedphrase_input.send_keys(seed)
        sign_in.next_button.click()
        keycard.begin_setup_button.click()
        keycard.yes_button.click()
        keycard.enter_default_pin()
        home.element_by_translation_id("intro-wizard-title5").wait_for_element(20)
        keycard.enter_default_pin()
        home.element_by_translation_id("keycard-access-reset").wait_for_element(30)
        home.ok_button.click()
        keycard.enter_default_pin()
        home.home_button.wait_for_element(30)

        self.errors.verify_no_errors()

    @marks.testrail_id(6330)
    def test_wallet_send_tx_token_set_max(self):
        sender = transaction_senders['ETH_STT_2']
        receiver = transaction_senders['ETH_1']
        sign_in = SignInView(self.driver)
        home_1 = sign_in.recover_access(sender['passphrase'])
        wallet = home_1.wallet_button.click()
        wallet.wait_balance_is_changed('STT')

        home_1.just_fyi("Sending token amount to account who will use Set Max option for token")
        amount = wallet.get_unique_amount()
        wallet.send_transaction(asset_name='STT', amount=amount, recipient=receiver['address'])
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, token=True)
        wallet.wallet_button.double_click()

        home_1.just_fyi('Add account restored from seed phrase')
        account_name = 'subaccount'
        wallet.add_account_button.click()
        wallet.enter_a_seed_phrase_button.click()
        wallet.enter_your_password_input.send_keys(common_password)
        wallet.enter_seed_phrase_input.send_keys(receiver['passphrase'])
        wallet.account_name_input.send_keys(account_name)
        wallet.add_account_generate_account_button.click()
        account_button = wallet.get_account_by_name(account_name)
        account_button.click()
        wallet.wait_balance_is_changed('STT', navigate_to_home=False)

        home_1.just_fyi("Send all tokens via Set Max option")
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        asset_name = 'STT'
        asset_button = send_transaction.asset_by_name(asset_name)
        send_transaction.select_asset_button.click_until_presence_of_element(
            send_transaction.eth_asset_in_select_asset_bottom_sheet_button)
        asset_button.click()
        send_transaction.set_max_button.click()
        send_transaction.set_recipient_address(sender['address'])
        send_transaction.sign_transaction_button.click()
        send_transaction.sign_transaction()
        wallet.close_button.click()
        wallet.wallet_button.double_click()
        wallet.get_account_by_name(account_name).click()
        wallet.wait_balance_is_equal_expected_amount(asset='STT', expected_balance=0, main_screen=False)

    @marks.testrail_id(5358)
    def test_wallet_backup_recovery_phrase_warning_from_wallet(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        wallet = sign_in.wallet_button.click()
        if wallet.backup_recovery_phrase_warning_text.is_element_displayed():
            self.driver.fail("'Back up your seed phrase' warning is shown on Wallet while no funds are present")
        address = wallet.get_wallet_address()
        self.click = wallet.close_button.click()
        w3.donate_testnet_eth(address, 0.0001)
        wallet.wait_balance_is_changed()
        if not wallet.backup_recovery_phrase_warning_text.is_element_displayed(30):
            self.driver.fail("'Back up your seed phrase' warning is not shown on Wallet with funds")
        profile = wallet.get_profile_view()
        wallet.backup_recovery_phrase_warning_text.click()
        profile.backup_recovery_phrase()

    @marks.testrail_id(5437)
    def test_wallet_validation_amount_errors(self):
        sender = wallet_users['C']
        sign_in = SignInView(self.driver)

        errors = {'send_transaction_screen': {
            'too_precise': 'Amount is too precise. Max number of decimals is 8.',
            'insufficient_funds': 'Insufficient funds'
        },
            'sending_screen': {
                'Amount': 'Insufficient funds',
                'Network fee': 'Not enough ETH for gas'
            },
        }
        warning = 'Warning %s is not shown on %s'

        sign_in.recover_access(sender['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.wait_balance_is_changed('YEENUS')
        wallet.accounts_status_account.click()

        screen = 'send transaction screen from wallet'
        sign_in.just_fyi('Checking %s on %s' % (errors['send_transaction_screen']['too_precise'], screen))
        initial_amount_adi = wallet.get_asset_amount_by_name('YEENUS')
        send_transaction = wallet.send_transaction_button.click()
        adi_button = send_transaction.asset_by_name('YEENUS')
        send_transaction.select_asset_button.click_until_presence_of_element(
            send_transaction.eth_asset_in_select_asset_bottom_sheet_button)
        adi_button.click()
        send_transaction.amount_edit_box.click()
        amount = '0.000%s' % str(random.randint(100000, 999999)) + '1'
        send_transaction.amount_edit_box.send_keys(amount)
        if not send_transaction.element_by_text(
                errors['send_transaction_screen']['too_precise']).is_element_displayed():
            self.errors.append(warning % (errors['send_transaction_screen']['too_precise'], screen))

        sign_in.just_fyi('Checking %s on %s' % (errors['send_transaction_screen']['insufficient_funds'], screen))
        send_transaction.amount_edit_box.clear()
        send_transaction.amount_edit_box.send_keys(str(initial_amount_adi) + '1')
        if not send_transaction.element_by_text(
                errors['send_transaction_screen']['insufficient_funds']).is_element_displayed():
            self.errors.append(warning % (errors['send_transaction_screen']['insufficient_funds'], screen))
        wallet.close_send_transaction_view_button.click()
        wallet.close_button.click()

        screen = 'sending screen from wallet'
        sign_in.just_fyi('Checking %s on %s' % (errors['sending_screen']['Network fee'], screen))
        account_name = 'new'
        wallet.add_account(account_name)
        wallet.get_account_by_name(account_name).click()
        wallet.send_transaction_button.click()
        send_transaction.amount_edit_box.send_keys('0')
        send_transaction.set_recipient_address(ens_user_message_sender['ens'])
        send_transaction.next_button.click()
        wallet.ok_got_it_button.wait_and_click(30)
        if not send_transaction.validation_error_element.is_element_displayed(10):
            self.errors.append('Validation icon is not shown when testing %s on %s' % (errors['sending_screen']['Network fee'], screen))
        if not wallet.element_by_translation_id("tx-fail-description2").is_element_displayed():
            self.errors.append("No warning about failing tx is shown!")
        send_transaction.cancel_button.click()

        screen = 'sending screen from DApp'
        sign_in.just_fyi('Checking %s on %s' % (errors['sending_screen']['Network fee'], screen))
        home = wallet.home_button.click()
        dapp = sign_in.dapp_tab_button.click()
        dapp.select_account_button.click()
        dapp.select_account_by_name(account_name).wait_for_element(30)
        dapp.select_account_by_name(account_name).click()
        status_test_dapp = home.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click_until_presence_of_element(
            status_test_dapp.send_two_tx_in_batch_button)
        status_test_dapp.send_two_tx_in_batch_button.click()
        if not send_transaction.validation_error_element.is_element_displayed(10):
            self.errors.append(warning % (errors['sending_screen']['Network fee'], screen))
        self.errors.verify_no_errors()

    @marks.testrail_id(695855)
    def test_wallet_custom_gas_settings_send_tx(self):
        sender = transaction_senders['ETH_7']
        sign_in = SignInView(self.driver)
        sign_in.recover_access(sender['passphrase'])
        wallet = sign_in.wallet_button.click()
        wallet.scan_tokens()
        wallet.wait_balance_is_changed()
        wallet.accounts_status_account.click()

        send_transaction = wallet.send_transaction_button.click()
        amount = '0.000%s' % str(random.randint(100000, 999999)) + '1'
        self.value = send_transaction.amount_edit_box.send_keys(amount)
        send_transaction.set_recipient_address(ens_user_message_sender['ens'])
        send_transaction.next_button.click()
        wallet.ok_got_it_button.wait_and_click(30)
        send_transaction.network_fee_button.click()
        send_transaction = wallet.get_send_transaction_view()
        fee_fields = (send_transaction.per_gas_tip_limit_input, send_transaction.per_gas_price_limit_input)
        [default_tip, default_price] = [field.text for field in fee_fields]
        default_limit = '21000'

        wallet.just_fyi("Check basic validation")
        values = {
            send_transaction.gas_limit_input:
                {
                    'default': default_limit,
                    'value': '22000',
                    '20999': 'wallet-send-min-units',
                    '@!': 'invalid-number',
                },
            send_transaction.per_gas_tip_limit_input:
                {
                    'default': default_tip,
                    'value': '2.5',
                    'aaaa': 'invalid-number',
                },
            send_transaction.per_gas_price_limit_input:
                {
                    'default': default_price,
                    'value': str(round(float(default_price)+3, 9)),
                    '-2': 'invalid-number',
                }
        }
        for field in values:
            for key in values[field]:
                if key != 'default' and key != 'value':
                    field.clear()
                    field.send_keys(key)
                    if not send_transaction.element_by_translation_id(values[field][key]).is_element_displayed(10):
                        self.errors.append("%s is not shown for %s" % (values[field][key], field.accessibility_id))
                    field.clear()
                    field.send_keys(values[field]['value'])

        wallet.just_fyi("Set custom fee and check that it will be applied")
        send_transaction.save_fee_button.scroll_and_click()
        if wallet.element_by_translation_id("change-tip").is_element_displayed():
            wallet.element_by_translation_id("continue-anyway").click()
        send_transaction.sign_transaction()
        self.network_api.wait_for_confirmation_of_transaction(sender['address'], amount, confirmations=3)
        transaction = wallet.find_transaction_in_history(amount=amount, return_hash=True)
        expected_params = {
            'fee_cap': values[send_transaction.per_gas_price_limit_input]['value'],
            'tip_cap': '2.5',
            'gas_limit': '22000'
        }
        actual_params = self.network_api.get_custom_fee_tx_params(transaction)
        if actual_params != expected_params:
            self.errors.append('Real params %s for tx do not match expected %s' % (str(actual_params), str(expected_params)))

        wallet.just_fyi('Verify custom fee data on tx screen')
        wallet.swipe_up()
        for key in expected_params:
            if key != 'fee_cap':
                if not wallet.element_by_text_part(expected_params[key]).is_element_displayed():
                    self.errors.append("Custom tx param %s is not shown on tx history screen" % key)

        wallet.wallet_button.double_click()

        wallet.just_fyi("Check below fee popup on Goerli")

        wallet.accounts_status_account.click()
        send_transaction = wallet.send_transaction_button.click_until_presence_of_element(send_transaction.amount_edit_box)
        send_transaction.amount_edit_box.send_keys(0)
        send_transaction.set_recipient_address(ens_user_message_sender['ens'])
        send_transaction.next_button.click()
        wallet.element_by_translation_id("network-fee").click()
        send_transaction.gas_limit_input.clear()
        send_transaction.gas_limit_input.send_keys(default_limit)
        send_transaction.per_gas_price_limit_input.clear()
        send_transaction.per_gas_price_limit_input.click()
        send_transaction.per_gas_price_limit_input.send_keys('0.00000000000001')
        if not wallet.element_by_translation_id("below-base-fee").is_element_displayed(10):
            self.errors.append("Fee is below error is not shown")
        send_transaction.save_fee_button.scroll_and_click()
        if not wallet.element_by_translation_id("change-tip").is_element_displayed():
            self.errors.append("Popup about changing fee error is not shown")
        wallet.element_by_translation_id("continue-anyway").click()
        if not send_transaction.element_by_text_part('0.000000 ETH').is_element_displayed():
            self.driver.fail("Custom fee is not applied!")
        self.errors.verify_no_errors()

        wallet.just_fyi("Check can change tip to higher value and sign transaction")
        wallet.element_by_translation_id("network-fee").click()
        send_transaction.gas_limit_input.clear()
        send_transaction.gas_limit_input.send_keys(default_limit)
        send_transaction.per_gas_price_limit_input.clear()
        send_transaction.per_gas_price_limit_input.click()
        send_transaction.per_gas_price_limit_input.send_keys('0.00000000000001')
        send_transaction.save_fee_button.scroll_and_click()
        wallet.element_by_translation_id("change-tip").click()
        send_transaction.per_gas_price_limit_input.clear()
        send_transaction.per_gas_price_limit_input.click()
        send_transaction.per_gas_price_limit_input.send_keys(default_price)
        if wallet.element_by_translation_id("below-base-fee").is_element_displayed(10):
            self.errors.append("Fee is below error is shown after fee correction")
        send_transaction.save_fee_button.scroll_and_click()
        if wallet.element_by_translation_id("change-tip").is_element_displayed():
            self.errors.append("Popup about changing fee error is shown after fee correction")
        send_transaction.sign_transaction()
        self.errors.verify_no_errors()

        wallet.just_fyi('Check gas limit price is calculated in case of signing contract address')
        wallet.send_transaction_button.click_until_presence_of_element(send_transaction.amount_edit_box)
        send_transaction.amount_edit_box.send_keys(0)
        send_transaction.chose_recipient_button.click()
        send_transaction.enter_recipient_address_input.send_keys('0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6')
        send_transaction.element_by_translation_id("warning-sending-to-contract-descr").wait_for_visibility_of_element()
        send_transaction.ok_button.click()
        send_transaction.enter_recipient_address_input.click()
        send_transaction.done_button.click_until_absense_of_element(send_transaction.done_button)
        send_transaction.next_button.click()
        wallet.element_by_translation_id("network-fee").click()
        gas_value = send_transaction.gas_limit_input.text
        if gas_value is default_price:
            self.errors.append('Gas limit price remains default for contract addresses')
        send_transaction.save_fee_button.scroll_and_click()
        send_transaction.cancel_button.click()

        wallet.just_fyi("Check not enough balance to cover trans fee error on Mainnet")
        profile = wallet.profile_button.click()
        profile.switch_network()
        sign_in.wallet_button.click()
        wallet.accounts_status_account.click()

        send_transaction = wallet.send_transaction_button.click_until_presence_of_element(send_transaction.amount_edit_box)
        send_transaction.amount_edit_box.send_keys(0)
        send_transaction.set_recipient_address(ens_user_message_sender['ens'])
        send_transaction.next_button.click()
        wallet.element_by_translation_id("network-fee").click()
        if not wallet.element_by_translation_id("tx-fail-description2").is_element_displayed():
            self.errors.append("Tx is likely to fail is not shown!")
        if send_transaction.network_fee_button.is_element_displayed():
            self.errors.append("Still can set tx fee when balance is not enough")
        self.errors.verify_no_errors()

    @marks.testrail_id(702360)
    def test_collectibles_mainnet_set_as_profile_image(self):
        wallet_users['D'] = dict()
        wallet_users['D'][
            'passphrase'] = "art base select follow harsh capable upper monkey report gun actor rib"
        wallet_users['D']['username'] = "Upbeat Diligent Jaguar"
        wallet_users['D']['address'] = "0xb51fe9F539E611Be5871b40baeBE5c4fe3E33020"
        wallet_users['D'][
            'public_key'] = "0x04c50fe17e2832e0927ea3248afd9056f88af8bac6233bd2b81d123fa" \
                            "5882d729b1341e40ebe60331f8a42386fb2ebd4fa11f592ad8d4cf3b824d8e51a03216185"
        wallet_users['D']['collectibles'] = {
            'CryptoKitties': '1',
            'Status Sticker Pack V2': '1'
        }
        user = wallet_users['D']
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])

        home.just_fyi('Check that collectibles amount is shown on Mainnet')
        profile = home.profile_button.click()
        profile.switch_network('Mainnet with upstream RPC')
        wallet = profile.wallet_button.click()
        wallet.accounts_status_account.click()
        wallet.collectibles_button.click()
        wallet.element_by_translation_id("display-collectibles").scroll_and_click()
        for asset in user['collectibles']:
            wallet.get_collectibles_amount(asset).scroll_to_element()
            if wallet.get_collectibles_amount(asset).text != user['collectibles'][asset]:
                self.errors.append(
                    '%s %s is not shown in Collectibles for Mainnet!' % (user['collectibles'][asset], asset))

        wallet.just_fyi('Check that you can open collectible to view')
        nft, nft_name = 'CryptoKitties', 'Miss Purrfect'
        wallet.get_collectibles_amount().click()
        if not wallet.nft_asset_button.is_element_displayed(60):
            self.driver.fail("No card is not shown for %s after opening it from collectibles!" % nft)
        wallet.nft_asset_button.click()
        wallet.set_collectible_as_profile_photo_button.scroll_and_click()

        wallet.just_fyi('Check that you can set collectible as profile photo')
        web_view = wallet.get_base_web_view()
        wallet.view_collectible_on_opensea_button.click_until_presence_of_element(
            web_view.browser_previous_page_button)
        web_view.wait_for_d_aap_to_load()
        if not web_view.element_by_text(nft_name).is_element_displayed(30):
            self.errors.append("Collectible can't be opened when tapping 'View on OpenSea' via NFT page")
        wallet.wallet_button.click()

        wallet.just_fyi('Check that collectibles are not shown when sending assets from wallet')
        send_transaction = wallet.send_transaction_button.click()
        send_transaction.select_asset_button.click()
        if send_transaction.asset_by_name(nft).is_element_displayed():
            self.errors.append('Collectibles can be sent from wallet')
        wallet.close_send_transaction_view_button.double_click()

        wallet.just_fyi("Check that custom image from collectible is set as profile photo")
        wallet.profile_button.double_click()
        if not profile.profile_picture.is_element_image_similar_to_template('collectible_pic_2.png'):
            self.errors.append("Collectible image is not set as profile image")
        self.errors.verify_no_errors()

    @marks.testrail_id(695890)
    def test_profile_use_another_fleets_balance_bsc_xdai_advanced_set_nonce(self):
        user = user_mainnet
        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user['passphrase'])

        home.just_fyi("Check that can enable all toggles and still login successfully")
        profile = home.profile_button.click()
        profile.advanced_button.click()
        profile.transaction_management_enabled_toggle.click()
        profile.webview_debug_toggle.click()
        profile.waku_bloom_toggle.scroll_and_click()
        sign_in.sign_in()

        home.just_fyi("Check tx management")
        wallet = home.wallet_button.click()
        send_tx = wallet.send_transaction_from_main_screen.click()
        from views.send_transaction_view import SendTransactionView
        send_tx = SendTransactionView(self.driver)
        send_tx.amount_edit_box.send_keys('0')
        send_tx.set_recipient_address(transaction_senders['ETH_7']['address'])
        send_tx.next_button.click()
        send_tx.set_up_wallet_when_sending_tx()
        send_tx.advanced_button.click()
        send_tx.nonce_input.send_keys('0')
        send_tx.nonce_save_button.click()
        error_text = send_tx.sign_transaction(error=True)
        if error_text != 'nonce too low':
            self.errors.append("%s is not expected error when signing tx with custom nonce" % error_text)

        home.just_fyi("Check balance on mainnet")
        profile = home.profile_button.click()
        profile.switch_network()
        wallet = home.wallet_button.click()
        wallet.scan_tokens()
        [wallet.wait_balance_is_equal_expected_amount(asset, value) for asset, value in user['mainnet'].items()]
        home.just_fyi("Check balance on xDai and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('xDai Chain')
        home.wallet_button.click()
        wallet.element_by_text(user['xdai']).wait_for_element(30)

        home.just_fyi("Check balance on BSC and default network fee")
        profile = home.profile_button.click()
        profile.switch_network('BSC Network')
        home.wallet_button.click()
        wallet.element_by_text(user['bsc']).wait_for_element(30)

        self.errors.verify_no_errors()

    @marks.testrail_id(6219)
    def test_profile_set_primary_ens_custom_domain(self):
        home = SignInView(self.driver).recover_access(ens_user['passphrase'])
        ens_second, ens_main = ens_user['ens_upgrade'], ens_user['ens']

        home.just_fyi('add 2 ENS names in Profile')
        profile = home.profile_button.click()
        dapp = profile.connect_existing_ens(ens_main)

        profile.element_by_translation_id("ens-add-username").wait_and_click()
        dapp.ens_name_input.send_keys(ens_second)
        dapp.check_ens_name.click_until_presence_of_element(dapp.element_by_translation_id("ens-got-it"))
        dapp.element_by_translation_id("ens-got-it").wait_and_click()

        home.just_fyi('check that by default %s ENS is set' % ens_main)
        dapp.element_by_translation_id("ens-primary-username").click()
        message_to_check = 'Your messages are displayed to others with'
        if not dapp.element_by_text('%s\n@%s' % (message_to_check, ens_main)).is_element_displayed():
            self.errors.append('%s ENS username is not set as primary by default' % ens_main)

        home.just_fyi('check view in chat settings ENS from other domain: %s after set new primary ENS' % ens_second)
        dapp.set_primary_ens_username(ens_second).click()
        if profile.username_in_ens_chat_settings_text.text != '@' + ens_second:
            self.errors.append('ENS username %s is not shown in ENS username Chat Settings after enabling' % ens_second)
        self.errors.verify_no_errors()

    @marks.testrail_id(5453)
    def test_profile_privacy_policy_terms_of_use_node_version_need_help(self):
        signin = SignInView(self.driver)
        no_link_found_error_msg = 'Could not find privacy policy link at'
        no_link_open_error_msg = 'Could not open our privacy policy from'
        no_link_tos_error_msg = 'Could not open Terms of Use from'

        signin.just_fyi("Checking privacy policy and TOS links")
        if not signin.privacy_policy_link.is_element_displayed():
            self.errors.append('%s Sign in view!' % no_link_found_error_msg)
        if not signin.terms_of_use_link.is_element_displayed():
            self.driver.fail("No Terms of Use link on Sign in view!")

        home = signin.create_user()
        profile = home.profile_button.click()
        profile.about_button.click()
        profile.privacy_policy_button.click()
        from views.web_views.base_web_view import BaseWebView
        web_page = BaseWebView(self.driver)
        if not web_page.policy_summary.is_element_displayed():
            self.errors.append('%s Profile about view!' % no_link_open_error_msg)
        web_page.click_system_back_button()

        profile.terms_of_use_button.click()
        web_page.wait_for_d_aap_to_load()
        web_page.swipe_by_custom_coordinates(0.5, 0.8, 0.5, 0.4)
        if not web_page.terms_of_use_summary.is_element_displayed(30):
            self.errors.append('%s Profile about view!' % no_link_tos_error_msg)
        web_page.click_system_back_button()

        signin.just_fyi("Checking that version match expected format and can be copied")
        app_version = profile.app_version_text.text
        node_version = profile.node_version_text.text
        if not re.search(r'\d[.]\d{1,2}[.]\d{1,2}\s[(]\d*[)]', app_version):
            self.errors.append("App version %s didn't match expected format" % app_version)
        if not re.search(r'StatusIM/v.*/android-\d{3}/go\d[.]\d+', node_version):
            self.errors.append("Node version %s didn't match expected format" % node_version)
        profile.app_version_text.click()
        profile.home_button.double_click()
        chat = home.join_public_chat(home.get_random_chat_name())
        message_input = chat.chat_message_input
        message_input.paste_text_from_clipboard()
        if message_input.text != app_version:
            self.errors.append('Version number was not copied to clipboard')

        signin.just_fyi("Checking Need help section")
        home.profile_button.double_click()
        profile.help_button.click()
        web_page = profile.faq_button.click()
        web_page.open_in_webview()
        web_page.wait_for_d_aap_to_load()
        if not profile.element_by_text_part("F.A.Q").is_element_displayed(30):
            self.errors.append("FAQ is not shown")
        profile.click_system_back_button()
        profile.submit_bug_button.click()

        signin.just_fyi("Checking bug submitting form")
        profile.bug_description_edit_box.send_keys('1234')
        profile.bug_submit_button.click()
        if not profile.element_by_translation_id("bug-report-too-short-description").is_element_displayed():
            self.errors.append("Can submit big with too short description!")
        profile.bug_description_edit_box.clear()
        [field.send_keys("Something wrong happened!!") for field in
         (profile.bug_description_edit_box, profile.bug_steps_edit_box)]
        profile.bug_submit_button.click()
        if not profile.element_by_text_part("Welcome to Gmail").is_element_displayed(30):
            self.errors.append("Mail client is not opened when submitting bug")
        profile.click_system_back_button(2)

        signin.just_fyi("Checking request feature")
        profile.request_a_feature_button.click()
        if not profile.element_by_text("#support").is_element_displayed(30):
            self.errors.append("Support channel is not suggested for requesting a feature")
        self.errors.verify_no_errors()

    @marks.testrail_id(5766)
    def test_profile_use_pinned_history_node_from_list(self):
        home = SignInView(self.driver).create_user()
        profile = home.profile_button.click()
        home.profile_button.click()

        home.just_fyi('pin history node')
        profile.sync_settings_button.click()
        node_gc, node_ams, node_hk = [profile.return_mailserver_name(history_node_name, used_fleet) for
                                      history_node_name in (mailserver_gc, mailserver_ams, mailserver_hk)]
        h_node = node_ams
        profile.mail_server_button.click()
        profile.mail_server_auto_selection_button.click()
        profile.mail_server_by_name(h_node).click()
        profile.confirm_button.click()
        if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
            h_node = node_hk
            profile.element_by_translation_id("mailserver-pick-another", uppercase=True).click()
            profile.mail_server_by_name(h_node).click()
            profile.confirm_button.click()
            if profile.element_by_translation_id("mailserver-error-title").is_element_displayed(10):
                self.driver.fail("Couldn't connect to any history node")

        profile.just_fyi('check that history node is pinned')
        profile.close_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)

        profile.just_fyi('Relogin and check that settings are preserved')
        home.reopen_app()
        home.profile_button.click()
        profile.sync_settings_button.click()
        if not profile.element_by_text(h_node).is_element_displayed():
            self.errors.append('"%s" history node is not pinned' % h_node)

        self.errors.verify_no_errors()

    @marks.testrail_id(6318)
    def test_profile_delete_several_multiaccounts(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        delete_alert_warning = sign_in.get_translation_by_key("delete-profile-warning")
        profile = sign_in.profile_button.click()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.navigate_up_button.click()
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.send_keys(common_password)
        sign_in.next_button.click()
        sign_in.confirm_your_password_input.send_keys(common_password)
        sign_in.next_button.click()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.start_button)
        sign_in.start_button.click()

        sign_in.just_fyi('Delete 2nd multiaccount')
        public_key, username = sign_in.get_public_key()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        for text in (username, delete_alert_warning):
            if not profile.element_by_text(text).is_element_displayed():
                self.errors.append('Required %s is not shown when deleting multiaccount' % text)
        profile.delete_profile_button.click()
        if profile.element_by_translation_id("profile-deleted-title").is_element_displayed():
            self.driver.fail('Profile is deleted without confirmation with password')
        profile.delete_my_profile_password_input.send_keys(common_password)
        profile.delete_profile_button.click_until_presence_of_element(
            profile.element_by_translation_id("profile-deleted-title"))
        profile.ok_button.click()

        sign_in.just_fyi('Delete last multiaccount')
        sign_in.sign_in()
        sign_in.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.delete_my_profile_button.scroll_and_click()
        profile.delete_my_profile_password_input.send_keys(common_password)
        profile.delete_profile_button.click()
        profile.ok_button.click()
        if not sign_in.get_started_button.is_element_displayed(20):
            self.errors.append('No redirected to carousel view after deleting last multiaccount')
        self.errors.verify_no_errors()

    @marks.testrail_id(6213)
    def test_contacts_unblock_user_is_not_added_back_to_contacts(self):
        home = SignInView(self.driver).create_user()
        chat = home.add_contact(basic_user["public_key"], add_in_contacts=False)

        chat.just_fyi('Block user not added as contact from chat view')
        chat.chat_options.click()
        chat.view_profile_button.click()
        chat.block_contact()
        chat.get_back_to_home_view()

        chat.just_fyi('Unblock user not added as contact from chat view')
        profile = home.profile_button.click()
        profile.contacts_button.click()
        profile.blocked_users_button.click()
        profile.element_by_text(basic_user["username"]).click()
        chat.unblock_contact_button.click()

        profile.just_fyi('Navigating to contact list and check that user is not in list')
        profile.close_button.click()
        profile.navigate_up_button.click()
        if profile.element_by_text(basic_user["username"]).is_element_displayed():
            self.driver.fail("Unblocked user not added previously in contact list added in contacts!")

    @marks.testrail_id(5721)
    @marks.xfail(reason="may be failed due to 14013")
    def test_group_chat_cant_add_more_twenty_participants(self):
        user_20_contacts = dict()
        user_20_contacts[
            'passphrase'] = "length depend bottom mom kitchen solar deposit emerge junior horse midnight grunt"

        sign_in = SignInView(self.driver)
        home = sign_in.recover_access(user_20_contacts['passphrase'])

        users = [chat_users['A'],
                 transaction_senders['A'],
                 transaction_senders['ETH_8'],
                 transaction_senders['ETH_1'],
                 transaction_senders['ETH_2'],
                 transaction_senders['ETH_7'],
                 transaction_senders['ETH_STT_3'],
                 transaction_senders['ETH_STT_ADI_1'],
                 transaction_senders['ETH_STT_1'],
                 transaction_senders['C'],
                 transaction_senders['G'],
                 transaction_senders['H'],
                 transaction_senders['I'],
                 transaction_senders['M'],
                 transaction_senders['N'],
                 transaction_senders['Q'],
                 transaction_senders['R'],
                 transaction_senders['S'],
                 transaction_senders['T'],
                 transaction_senders['U']]
        usernames = []

        for user in users:
            usernames.append(user['username'])
        profile = home.profile_button.click()
        profile.element_by_text('20').wait_for_visibility_of_element(120)
        home.home_button.double_click()

        home.just_fyi('Create group chat with max amount of users')
        chat = home.create_group_chat(usernames, 'some_group_chat')

        home.just_fyi('Verify that can not add more users via group info')
        chat.get_back_to_home_view()
        home.get_chat('some_group_chat').click()
        chat.chat_options.click()
        group_info_view = chat.group_info.click()
        if group_info_view.add_members.is_element_displayed():
            self.errors.append('Add members button is displayed when max users are added in chat')
        if not group_info_view.element_by_text_part('20 members').is_element_displayed():
            self.errors.append('Amount of users is not shown on Group info screen')

        self.errors.verify_no_errors()

    @marks.testrail_id(5455)
    def test_restore_multiaccounts_with_certain_seed_phrase(self):
        sign_in = SignInView(self.driver)
        for phrase, account in recovery_users.items():
            home_view = sign_in.recover_access(passphrase=phrase, password=unique_password)
            wallet_view = home_view.wallet_button.click()
            address = wallet_view.get_wallet_address()
            if address != account:
                self.errors.append('Restored wallet address "%s" does not match expected "%s"' % (address, account))
            profile = home_view.profile_button.click()
            profile.privacy_and_security_button.click()
            profile.delete_my_profile_button.scroll_and_click()
            profile.delete_my_profile_password_input.send_keys(unique_password)
            profile.delete_profile_button.click()
            profile.ok_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702167)
    def test_ens_dapp_purchase(self):
        sign_in = SignInView(self.driver)
        self.home = sign_in.create_user()
        self.ens_name = 'purchased%s' % self.home.get_random_chat_name()
        self.wallet = self.home.wallet_button.click()
        self.address = self.wallet.get_wallet_address()
        w3.donate_testnet_eth(self.address, 0.1)
        self.wallet.wait_balance_is_changed()
        self.chat_key = self.home.get_public_key()

        self.wallet.just_fyi("Get required STT")
        self.wallet.get_test_assets(token=True)

        self.wallet.just_fyi("Purchase ENS")
        self.profile = self.home.profile_button.click()
        self.profile.ens_usernames_button.wait_and_click()
        self.dapp = self.home.get_dapp_view()
        self.dapp.get_started_ens.click()
        self.dapp.ens_name_input.send_keys(self.ens_name)
        self.dapp.check_ens_name.click_until_presence_of_element(self.dapp.register_ens_button)
        self.dapp.agree_on_terms_ens.scroll_and_click()
        if not self.dapp.element_by_text(self.chat_key).is_element_displayed():
            self.error.append("No chat key for user is shown when register requested chat key")
        self.dapp.register_ens_button.click()
        self.send_tx = self.home.get_send_transaction_view()
        self.send_tx.sign_transaction()
        if not self.dapp.element_by_text('Nice! You own %s.stateofus.eth once the transaction is complete.' % self.ens_name).is_element_displayed(60):
            self.error.append("ENS name %s is not purchasing" % self.ens_name)
        self.dapp.ens_got_it.click()
        if self.dapp.registration_in_progress.is_element_displayed(10):
            self.dapp.registration_in_progress.wait_for_invisibility_of_element(400)
        self.dapp.element_by_text(self.ens_name).click()
        for text in ("10 SNT, deposit unlocked", self.chat_key, self.address.lower()):
            if not self.dapp.element_by_text(text).is_element_displayed(10):
                self.errors.append("%s is not displayed after ENS purchasing" % text)

        self.wallet.just_fyi("Send leftovers")
        self.wallet.wallet_button.double_click()
        address = self.wallet.get_wallet_address()
        self.wallet.donate_leftovers()

        self.wallet.just_fyi("Verify purchased ENS")
        self.home.home_button.click()
        self.home.plus_button.click_until_presence_of_element(self.home.start_new_chat_button)
        chat = self.home.start_new_chat_button.click()
        chat.public_key_edit_box.click()
        chat.public_key_edit_box.send_keys(self.ens_name)
        if not self.home.element_by_translation_id("can-not-add-yourself").is_element_displayed(20):
            self.errors.append(
                "Public key in not resolved correctly from %s ENS name on stateofus!" % self.ens_name)
        self.home.get_back_to_home_view()
        self.wallet.wallet_button.double_click()
        from views.send_transaction_view import SendTransactionView
        send_transaction = SendTransactionView(self.driver)
        self.wallet.send_transaction_from_main_screen.click_until_presence_of_element(send_transaction.chose_recipient_button)
        send_transaction.chose_recipient_button.scroll_and_click()
        send_transaction.set_recipient_address(self.ens_name)
        if not send_transaction.element_by_text_part(send_transaction.get_formatted_recipient_address(address)).is_element_displayed(5):
            self.errors.append("Wallet address in not resolved correctly from %s ENS name on stateofus!" % self.ens_name)

        self.errors.verify_no_errors()

    @marks.testrail_id(6300)
    @marks.skip
    # TODO: waiting mode (rechecked 04.10.22, valid)
    def test_webview_security(self):
        home_view = SignInView(self.driver).create_user()
        daap_view = home_view.dapp_tab_button.click()

        browsing_view = daap_view.open_url('https://simpledapp.status.im/webviewtest/url-spoof-ssl.html')
        browsing_view.url_edit_box_lock_icon.click()
        if not browsing_view.element_by_translation_id("browser-not-secure").is_element_displayed():
            self.errors.append("Broken certificate displayed as secure connection \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/webviewtest.html')
        browsing_view.element_by_text_part('204').click()
        if browsing_view.element_by_text_part('google.com').is_element_displayed():
            self.errors.append("URL changed on attempt to redirect to no-content page \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/webviewtest.html')
        browsing_view.element_by_text_part('XSS check').click()
        browsing_view.open_in_status_button.click()
        if browsing_view.element_by_text_part('simpledapp.status.im').is_element_displayed():
            self.errors.append("XSS attemp succedded \n")
            browsing_view.ok_button.click()

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/url-blank.html')
        if daap_view.edit_url_editbox.text == '':
            self.errors.append("Blank URL value. Must show the actual URL \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/port-timeout.html')
        # wait up  ~2.5 mins for port time out
        if daap_view.element_by_text_part('example.com').is_element_displayed(150):
            self.errors.append("URL spoof due to port timeout \n")

        self.errors.verify_no_errors()
