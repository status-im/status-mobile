import pytest
from selenium.common import TimeoutException

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from support.helpers import generate_wallet_address
from tests import marks, run_in_parallel, transl
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="one_3")
@marks.nightly
class TestFallbackMultipleDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(3)
        self.sign_in_1, self.sign_in_2, self.sign_in_3 = SignInView(self.drivers[0]), SignInView(
            self.drivers[1]), SignInView(self.drivers[2])
        self.home_1, self.home_2, self.home_3 = self.sign_in_1.get_home_view(), self.sign_in_2.get_home_view(), \
            self.sign_in_3.get_home_view()
        self.sign_in_1.just_fyi("Device 1: create a new user")
        self.sign_in_3.just_fyi("Device 3: create a new user")
        self.loop.run_until_complete(
            run_in_parallel(((self.sign_in_1.create_user,),
                             (self.sign_in_3.create_user,))))
        self.user_name_1, self.user_name_3 = self.home_1.get_username(), self.home_3.get_username()
        self.profile_1 = self.home_1.profile_button.click()
        self.profile_2 = self.home_2.get_profile_view()
        self.sign_in_3.just_fyi("Device 3: get public key")
        self.recovery_phrase, self.public_key_3 = self.loop.run_until_complete(
            run_in_parallel(((self.profile_1.backup_recovery_phrase, {}),
                             (self.home_3.get_public_key, {}))))
        self.home_2.driver.get_clipboard_text()  # just pinging 2nd device to save the connection
        self.profile_1.click_system_back_button()
        self.home_1.chats_tab.click()
        self.home_1.just_fyi("Device 1: add the 3rd user as a contact")
        self.home_1.add_contact(self.public_key_3)
        self.home_3.just_fyi("Device 3: accepting contact request from the 1st user")
        self.home_3.handle_contact_request(self.user_name_1)
        self.profile_1.just_fyi("Device 1: get sync code")
        self.home_1.profile_button.click()
        self.sync_code = self.profile_1.get_sync_code()

    @marks.testrail_id(740220)
    def test_fallback_sync_with_error(self):
        self.sign_in_2.just_fyi("Device 2: try syncing profile")
        self.sign_in_2.sync_profile(sync_code=self.sync_code)
        self.sign_in_2.progress_screen_title.wait_for_element()
        assert self.sign_in_2.progress_screen_title.text == "Oops, something’s wrong!"
        self.home_3.chats_tab.is_element_displayed()  # just pinging 3rd device to save the connection

    @marks.testrail_id(740221)
    def test_fallback_with_correct_seed_phrase(self):
        self.sign_in_2.just_fyi("Device 2: recover a profile with backed up seed phrase")
        self.sign_in_2.try_seed_phrase_button.click()
        self.sign_in_2.recover_access(passphrase=self.recovery_phrase, after_sync_code=True)
        self.profile_1.click_system_back_button()

        self.home_2.just_fyi("Getting device 2 name")
        self.home_2.profile_button.click()
        self.profile_2.syncing_button.scroll_and_click()
        self.profile_2.paired_devices_button.click()
        device_2_name = self.profile_2.get_current_device_name()
        self.profile_2.click_system_back_button(times=3)

        self.home_3.chats_tab.is_element_displayed()  # just pinging 3rd device to save the connection

        self.profile_1.just_fyi("Check device 2 is shown in not paired devices list in profile 1")
        device_1_name = self.profile_1.get_current_device_name()
        device_element = self.profile_1.get_paired_device_by_name(device_2_name)
        if device_element.is_element_displayed():
            if not device_element.get_pair_button.is_element_displayed():
                self.errors.append(
                    self.profile_1,
                    "Pair button is absent for the device 2 inside Paired devices list of profile 1 before pairing")
        else:
            self.errors.append(self.profile_1,
                               "Device 2 is not shown in Paired devices list for device 1 before pairing")
        self.profile_1.click_system_back_button(times=3)

        for home in self.home_1, self.home_2:
            home.notifications_unread_badge.wait_for_visibility_of_element(30)
            home.open_activity_center_button.click_until_presence_of_element(home.close_activity_centre)

        self.home_1.just_fyi("Checking pairing request on device 1")
        a_c_element = self.home_1.get_activity_center_element_by_text(transl['review-pairing-request'])
        if a_c_element.title.text != transl['new-device-detected']:
            self.errors.append(self.home_1,
                               "Notification with title '%s' is not shown in the activity center for the device 1" %
                               transl[
                                   'new-device-detected'])
        a_c_element.review_pairing_request_button.click()
        device_id_1 = self.home_1.get_new_device_installation_id()

        self.home_3.chats_tab.is_element_displayed()  # just pinging 3rd device to save the connection

        self.home_2.just_fyi("Checking sync profile on device 2")
        a_c_element = self.home_2.get_activity_center_element_by_text(transl['more-details'])
        if a_c_element.title.text != transl['sync-your-profile']:
            self.errors.append(self.home_2,
                               "Notification with title '%s' is not shown in the activity center for the device 2" %
                               transl[
                                   'sync-your-profile'])
        a_c_element.more_details_button.click()
        device_id_2 = self.home_2.get_new_device_installation_id()

        if device_id_1 != device_id_2:
            self.errors.append(self.home_2, "Device ids don't match on the activity center notifications")

        self.home_1.just_fyi("Confirm pairing request on device 1")
        self.home_1.element_by_translation_id('pair-and-sync').click()
        self.home_2.element_by_translation_id('close').click()

        self.home_1.close_activity_centre.click()
        self.home_2.close_activity_centre.click()

        self.home_3.chats_tab.is_element_displayed()  # just pinging 3rd device to save the connection

        self.home_1.just_fyi("Device 1: Check that the device 2 is shown in paired devices list")
        self.home_1.profile_button.click()
        self.profile_1.syncing_button.scroll_and_click()
        self.profile_1.paired_devices_button.click()
        device_element = self.profile_1.get_paired_device_by_name(device_2_name)
        if device_element.is_element_displayed():
            if not device_element.get_unpair_button.is_element_displayed():
                self.errors.append(
                    self.profile_1,
                    "Unpair button is absent for the device 2 inside Paired devices list of profile 1 after pairing")
        else:
            self.errors.append(self.profile_1,
                               "Device 2 is not shown in Paired devices list for device 1 after pairing")

        self.home_2.just_fyi("Device 2: Check that the device 1 is shown paired devices list")
        self.home_2.profile_button.click()
        self.profile_2.syncing_button.scroll_and_click()
        self.profile_2.paired_devices_button.click()
        device_element = self.profile_2.get_paired_device_by_name(device_1_name)
        if device_element.is_element_displayed():
            if not device_element.get_unpair_button.is_element_displayed():
                self.errors.append(
                    self.profile_2,
                    "Unpair button is absent for the device 1 inside Paired devices list of profile 2 after pairing")
        else:
            self.errors.append(self.profile_2,
                               "Device 1 is not shown in Paired devices list for device 2 after pairing")

        self.home_3.just_fyi("Device 3: send a message to user 1")
        self.home_3.chats_tab.click()
        chat_3 = self.home_3.get_chat(self.user_name_1).click()
        message = "Test message"
        chat_3.send_message(message)

        def _check_message(home_view, index):
            home_view.just_fyi("Device %s: check the message from the user 3 is received" % index)
            home_view.click_system_back_button(times=3)
            home_view.chats_tab.click()
            try:
                chat_element = home_view.get_chat(self.user_name_3)
                chat_element.wait_for_visibility_of_element(60)
                chat_view = chat_element.click()
                chat_view.chat_element_by_text(message).wait_for_visibility_of_element(60)
            except TimeoutException:
                self.errors.append(home_view, "Message is not received by the user %s" % index)

        self.loop.run_until_complete(
            run_in_parallel(((_check_message, {'home_view': self.home_1, 'index': 1}),
                             (_check_message, {'home_view': self.home_2, 'index': 2}))))

        self.errors.verify_no_errors()

    @marks.testrail_id(741054)
    def test_fallback_add_key_pair(self):
        expected_addresses = generate_wallet_address(passphrase=self.recovery_phrase, number=4)
        account_to_add = transaction_senders['ETH_1']
        self.home_1.navigate_back_to_home_view()
        self.home_2.navigate_back_to_home_view()
        wallet_1 = self.home_1.wallet_tab.click()
        wallet_2 = self.home_2.wallet_tab.click()

        wallet_1.just_fyi("Device 1: add a new regular account")
        regular_account_name = "New regular account"
        regular_derivation_path = wallet_1.add_regular_account(account_name=regular_account_name)
        regular_account_address = wallet_1.get_account_address().split(':')[-1]
        account_element = wallet_1.get_account_element(account_name=regular_account_name)
        wallet_1.close_account_button.click_until_presence_of_element(account_element)

        if regular_account_address != expected_addresses[1]:
            self.errors.append(wallet_1, "Newly added regular account address %s doesn't match expected %s" % (
                regular_account_address, expected_addresses[1]))
        if regular_account_address not in expected_addresses:
            self.errors.append(wallet_1,
                               "Newly added regular account address %s is not in the list of expected addresses %s" % (
                                   regular_account_address, expected_addresses))

        wallet_1.just_fyi("Device 1: add a new key pair account by importing recovery phrase")
        account_element.swipe_left_on_element()
        imported_key_pair_account_name = "Imported account"
        imported_key_pair_name = "Imported key pair"
        imported_key_pair_account_address = '0x' + account_to_add['address'].lower()
        imported_key_pair_derivation_path = wallet_1.import_key_pair_using_recovery_phrase(
            account_name=imported_key_pair_account_name,
            passphrase=account_to_add['passphrase'],
            key_pair_name=imported_key_pair_name)

        wallet_1.close_account_button.click_until_presence_of_element(account_element)

        wallet_1.just_fyi("Device 1: verify default and added key pairs details")
        account_element.swipe_left_on_element()
        wallet_1.get_account_element(account_name=imported_key_pair_account_name).swipe_left_on_element()
        if not wallet_1.add_account_button.is_element_displayed():
            wallet_1.get_account_element(account_name=imported_key_pair_account_name).swipe_left_on_element()
        wallet_1.add_account_button.click()
        wallet_1.create_account_button.click()
        wallet_1.edit_key_pair_button.click()
        expected_texts_regular = [
            regular_account_name,
            "%s...%s" % (regular_account_address[:5], regular_account_address[-3:])
        ]
        for text in expected_texts_regular:
            if not wallet_1.default_key_pair_container.get_child_element_by_text_part(text).is_element_displayed():
                self.errors.append(wallet_1, "Newly added regular account is not shown in default key pair list")
                break
        expected_texts_key_pair = [
            imported_key_pair_account_name, imported_key_pair_name,
            "%s...%s" % (imported_key_pair_account_address[:5], imported_key_pair_account_address[-3:])
        ]
        for text in expected_texts_key_pair:
            if not wallet_1.added_key_pair_container.get_child_element_by_text_part(text).is_element_displayed():
                self.errors.append(wallet_1, "Newly added regular account is not shown in default key pair list")
                break

        wallet_1.just_fyi("Device 1: add a new key pair account by generating a new key pair")
        generated_key_pair_account_name = "Generated account"
        generated_key_pair_name = "Generated key pair"
        generated_key_pair_derivation_path, generated_passphrase = wallet_1.generate_new_key_pair(
            account_name=generated_key_pair_account_name,
            key_pair_name=generated_key_pair_name)
        generated_key_pair_account_address = wallet_1.get_account_address().split(':')[-1]
        wallet_1.close_account_button.click_until_presence_of_element(account_element)

        expected_addresses = generate_wallet_address(passphrase=generated_passphrase, number=4)
        if generated_key_pair_account_address != expected_addresses[0]:
            self.errors.append(wallet_1, "Generated key pair account address %s doesn't match expected %s" % (
                generated_key_pair_account_address, expected_addresses[0]))
        if generated_key_pair_account_address not in expected_addresses:
            self.errors.append(wallet_1,
                               "Generated key pair account address %s is not in the list of expected addresses %s" % (
                                   generated_key_pair_account_address, expected_addresses))

        self.home_2.just_fyi("Device 2: check imported accounts are shown before importing key pair")
        self.home_2.profile_button.click()
        self.profile_2.profile_wallet_button.click()
        self.profile_2.key_pairs_and_accounts_button.click()
        if not self.profile_2.get_missing_key_pair_by_name(key_pair_name=imported_key_pair_name).is_element_displayed():
            self.errors.append(self.profile_2,
                               "New imported key pair is not shown in profile as missing before importing")
        if not self.profile_2.get_missing_key_pair_by_name(
                key_pair_name=generated_key_pair_name).is_element_displayed():
            self.errors.append(self.profile_2, "Generated key pair is not shown in profile as missing before importing")
        if not self.profile_2.get_key_pair_account_by_name(account_name=regular_account_name).is_element_displayed():
            self.errors.append(
                self.profile_2,
                "Newly added regular account is not shown in profile as on device before importing key pair")
        self.profile_2.options_button.click()
        if not self.profile_2.import_by_entering_recovery_phrase_button.is_element_displayed():
            self.errors.append(self.profile_2, "Can not import key pair account from profile")
        self.profile_2.click_system_back_button(times=4)

        wallet_2.just_fyi("Device 2: import key pair")
        wallet_2.get_account_element(account_name=regular_account_name).swipe_left_on_element()
        wallet_2.get_account_element(account_name=imported_key_pair_account_name).click()
        wallet_2.element_by_translation_id("import-key-pair").click()
        self.sign_in_2.passphrase_edit_box.send_keys(account_to_add['passphrase'])
        wallet_2.slide_and_confirm_with_password()

        self.home_2.just_fyi("Device 2: check imported accounts are shown in profile as added after importing key pair")
        self.home_2.profile_button.click()
        self.profile_2.profile_wallet_button.click()
        self.profile_2.key_pairs_and_accounts_button.click()
        if self.profile_2.get_key_pair_account_by_name(account_name=regular_account_name).is_element_displayed():
            address_text = self.profile_2.get_key_pair_account_by_name(account_name=regular_account_name).address.text
            if address_text != '...'.join((regular_account_address[:5], regular_account_address[-3:])):
                self.errors.append(
                    self.profile_2,
                    "Incorrect wallet address if shown for regular account after importing: " + address_text)
        else:
            self.errors.append(self.profile_2,
                               "Newly added regular account is not shown in profile after importing key pair")

        account_element = self.profile_2.get_key_pair_account_by_name(
            account_name=imported_key_pair_account_name)
        if account_element.is_element_displayed():
            address_text = account_element.address.text
            if address_text != '...'.join(
                    (imported_key_pair_account_address[:5], imported_key_pair_account_address[-3:])):
                self.errors.append(
                    self.profile_2,
                    "Incorrect wallet address if shown for imported key pair account after importing: " + address_text)
        else:
            self.errors.append(
                self.profile_2,
                "Imported key pair account is not shown in profile as on device after importing key pair")

        if not self.profile_2.get_missing_key_pair_by_name(
                key_pair_name=generated_key_pair_name).is_element_displayed():
            self.errors.append(
                self.profile_2,
                "Generated key pair account is not shown in profile as missing after importing the first key pair")
        self.profile_2.click_system_back_button(times=3)

        # ToDo: Arbiscan API is down, looking for analogue
        # wallet_2.just_fyi("Device 2: check wallet balance")
        # wallet_2.set_network_in_wallet(network_name='Arbitrum')
        # expected_balance = self.network_api.get_balance(key_pair_account_address)
        # shown_balance = wallet_2.get_asset(asset_name='Ether').get_amount()
        # if shown_balance != round(expected_balance, 5):
        #     self.errors.append("Device 2: ETH balance %s doesn't match expected %s" % (shown_balance, expected_balance))

        wallet_2.just_fyi("Device 2: check derivation paths of the regular and key pair accounts")
        account_element = wallet_2.get_account_element(account_name=regular_account_name)
        account_element.click()
        wallet_2.about_tab.click()
        der_path = wallet_2.account_about_derivation_path_text.text
        if der_path != regular_derivation_path:
            self.errors.append(wallet_2, "Incorrect derivation path %s is shown for the regular account" % der_path)
        wallet_2.close_account_button.click_until_presence_of_element(account_element)
        account_element.swipe_left_on_element()
        wallet_2.get_account_element(account_name=imported_key_pair_account_name).click()
        wallet_2.about_tab.click()
        der_path = wallet_2.account_about_derivation_path_text.text
        if der_path != imported_key_pair_derivation_path:
            self.errors.append(wallet_2,
                               "Incorrect derivation path %s is shown for the imported key pair account" % der_path)
        wallet_2.close_account_button.click_until_presence_of_element(account_element)
        account_element.swipe_left_on_element()
        wallet_2.get_account_element(account_name=generated_key_pair_account_name).click()
        wallet_2.element_by_translation_id("import-key-pair").click()
        self.sign_in_2.passphrase_edit_box.send_keys(generated_passphrase)
        wallet_2.slide_and_confirm_with_password()
        wallet_2.get_account_element(account_name=generated_key_pair_account_name).click()
        wallet_2.about_tab.click()
        der_path = wallet_2.account_about_derivation_path_text.text
        if der_path != generated_key_pair_derivation_path:
            self.errors.append(wallet_2,
                               "Incorrect derivation path %s is shown for the generated key pair account" % der_path)
        if not wallet_2.element_by_text_part(generated_key_pair_account_address).is_element_displayed():
            self.errors.append(
                "Generated key pair address %s is absent in About tab" % generated_key_pair_account_address)
        self.errors.verify_no_errors()

    @marks.testrail_id(740222)
    def test_fallback_validate_seed_phrase(self):
        self.sign_in_2.reopen_app(sign_in=False)

        self.sign_in_2.just_fyi("Device 2: try syncing profile")
        self.sign_in_2.explore_new_status_button.click_if_shown()
        self.sign_in_2.sync_profile(sync_code=self.sync_code, first_user=False)
        self.sign_in_2.progress_screen_title.wait_for_element()
        assert self.sign_in_2.progress_screen_title.text == "Oops, something’s wrong!"

        self.sign_in_2.just_fyi("Device 2: try invalid passphrase")
        self.sign_in_2.try_seed_phrase_button.click()
        self.sign_in_2.passphrase_edit_box.send_keys(' '.join(['asset'] * 12))
        self.sign_in_2.continue_button.click()
        if not self.sign_in_2.element_by_translation_id('seed-phrase-invalid').is_element_displayed():
            self.errors.append(self.sign_in_2, "Error message is not displayed for invalid recovery phrase")

        self.sign_in_2.just_fyi("Device 2: try creating an account with another valid passphrase")
        self.sign_in_2.passphrase_edit_box.clear()
        self.sign_in_2.passphrase_edit_box.send_keys(transaction_senders['A']['passphrase'])
        self.sign_in_2.continue_button.click()
        if not self.sign_in_2.password_input.is_element_displayed():
            self.errors.append(self.sign_in_2, "Can't recover an access with a valid passphrase")
        self.sign_in_2.click_system_back_button(times=2)

        self.sign_in_2.just_fyi("Device 2: try recovering an account which is already synced")
        self.sign_in_2.passphrase_edit_box.clear()
        self.sign_in_2.passphrase_edit_box.send_keys(self.recovery_phrase)
        self.sign_in_2.continue_button.click()
        if not self.sign_in_2.element_by_translation_id('account-already-exist-error').is_element_displayed():
            self.errors.append(self.sign_in_2, "Error is not shown for already synced account")

        self.errors.verify_no_errors()
