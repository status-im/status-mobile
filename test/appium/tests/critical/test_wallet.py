import time

import pytest
from _pytest.outcomes import Failed
from selenium.common import TimeoutException

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from support.api.network_api import NetworkApi
from tests import marks, run_in_parallel
from users import transaction_recipients
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_four_2")
@marks.new_ui_critical
@marks.secured
class TestWalletMultipleDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(2)
        self.sign_in_1, self.sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender, self.receiver = transaction_recipients['J'], transaction_recipients['K']
        self.sender_username, self.receiver_username = 'sender', 'receiver'
        self.loop.run_until_complete(
            run_in_parallel(((self.sign_in_1.recover_access, {'passphrase': self.sender['passphrase'],
                                                              'username': self.sender_username}),
                             (self.sign_in_2.recover_access, {'passphrase': self.receiver['passphrase'],
                                                              'username': self.receiver_username}))))
        self.home_1, self.home_2 = self.sign_in_1.get_home_view(), self.sign_in_2.get_home_view()
        self.wallet_1, self.wallet_2 = self.sign_in_1.get_wallet_view(), self.sign_in_2.get_wallet_view()

    # ToDo: Add verification of Activity tabs when the feature is ready in the next 2 tests:

    def _get_balances_before_tx(self):
        sender_balance = self.network_api.get_balance(self.sender['address'])
        receiver_balance = self.network_api.get_balance(self.receiver['address'])
        self.wallet_1.just_fyi("Getting ETH amount in the wallet of the sender before transaction")
        self.wallet_1.wallet_tab.click()
        self.wallet_1.get_account_element().click()
        eth_amount_sender = self.wallet_1.get_asset(asset_name='Ether').get_amount()
        self.wallet_2.just_fyi("Getting ETH amount in the wallet of the receiver before transaction")
        self.wallet_2.wallet_tab.click()
        self.wallet_2.get_account_element().click()
        eth_amount_receiver = self.wallet_2.get_asset(asset_name='Ether').get_amount()
        return sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver

    def _check_balances_after_tx(self, amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                 eth_amount_receiver):
        try:
            self.network_api.wait_for_balance_to_be(address=self.sender['address'],
                                                    expected_balance=sender_balance - amount_to_send)
        except TimeoutException:
            self.errors.append("Sender balance was not updated")
        try:
            self.network_api.wait_for_balance_to_be(address=self.receiver['address'],
                                                    expected_balance=receiver_balance + amount_to_send)
        except TimeoutException:
            self.errors.append("Receiver balance was not updated")

        def wait_for_wallet_balance_to_update(wallet_view, user_name, initial_eth_amount):
            wallet_view.just_fyi("Getting ETH amount in the wallet of the %s after transaction" % user_name)
            if user_name == 'sender':
                exp_amount = round(initial_eth_amount - amount_to_send, 4)
            else:
                exp_amount = round(initial_eth_amount + amount_to_send, 4)

            # for _ in range(12):  # ToDo: 120 sec wait time, enable when autoupdate feature is ready
            wallet_view.wallet_tab.click()
            new_eth_amount = round(wallet_view.get_asset(asset_name='Ether').get_amount(), 4)
            if user_name == 'sender' and new_eth_amount <= exp_amount:
                return
            if user_name == 'receiver' and new_eth_amount >= exp_amount:
                return
                # wallet_view.chats_tab.click()
                # time.sleep(10)
            self.errors.append(
                "Eth amount in the %ss wallet is %s but should be %s" % (user_name, new_eth_amount, exp_amount))

        # ToDo: disable relogin when autoupdate feature ia ready
        self.home_1.just_fyi("Relogin for getting an updated balance")
        self.home_2.just_fyi("Relogin for getting an updated balance")
        for _ in range(6):  # just waiting 1 minute here to be sure that balances are updated
            self.wallet_1.wallet_tab.click()
            self.wallet_2.wallet_tab.click()
            time.sleep(10)
        self.loop.run_until_complete(
            run_in_parallel(((self.home_1.reopen_app,),
                             (self.home_2.reopen_app,))))
        self.loop.run_until_complete(
            run_in_parallel(((wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_1,
                                                                  'user_name': self.sender_username,
                                                                  'initial_eth_amount': eth_amount_sender}),
                             (wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_2,
                                                                  'user_name': self.receiver_username,
                                                                  'initial_eth_amount': eth_amount_receiver}))))

        self.errors.verify_no_errors()

    @marks.testrail_id(727229)
    def test_wallet_send_eth(self):
        sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()

        self.wallet_2.close_account_button.click()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending funds from wallet")
        amount_to_send = 0.0001
        self.wallet_1.send_asset(address=self.receiver['address'], asset_name='Ether', amount=amount_to_send)
        self.wallet_1.close_account_button.click_until_presence_of_element(self.home_1.show_qr_code_button)

        self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                      eth_amount_receiver)

    @marks.testrail_id(727230)
    def test_wallet_send_asset_from_drawer(self):
        sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()
        self.wallet_2.close_account_button.click()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending asset from drawer")
        amount_to_send = 0.0001
        self.wallet_1.send_asset_from_drawer(address=self.receiver['address'], asset_name='Ether',
                                             amount=amount_to_send)
        self.wallet_1.close_account_button.click_until_presence_of_element(self.home_1.show_qr_code_button)

        self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                      eth_amount_receiver)


@pytest.mark.xdist_group(name="new_one_2")
@marks.new_ui_critical
class TestWalletOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in_view = SignInView(self.drivers[0])
        self.sign_in_view.create_user()
        self.home_view = self.sign_in_view.get_home_view()
        self.wallet_view = self.home_view.wallet_tab.click()

    @marks.testrail_id(727231)
    def test_wallet_add_remove_regular_account(self):
        self.wallet_view.just_fyi("Adding new regular account")
        new_account_name = "New Account"
        self.wallet_view.add_regular_account(account_name=new_account_name)

        if self.wallet_view.account_name_text.text != new_account_name:
            pytest.fail("New account is not created")
        self.wallet_view.account_emoji_button.click_until_presence_of_element(self.wallet_view.copy_address_button)
        self.wallet_view.share_address_button.click()
        new_wallet_address = self.wallet_view.sharing_text_native.text
        self.wallet_view.click_system_back_button()
        self.wallet_view.close_account_button.click_until_presence_of_element(self.home_view.show_qr_code_button)

        self.wallet_view.just_fyi("Checking that the new wallet is added to the Share QR Code menu")
        self.home_view.show_qr_code_button.click()
        self.home_view.share_wallet_tab_button.click()
        if self.home_view.account_name_text.text != 'Account 1':
            self.errors.append("Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append("Can't swipe between accounts, newly added account is not shown")
        else:
            shown_address = self.home_view.copy_wallet_address()
            if set(shown_address.split(':')) != set(new_wallet_address.split(':')):
                self.errors.append(
                    "Incorrect address '%s' is shown when swiping between accounts, expected one is '%s'" % (
                        shown_address, new_wallet_address))
        self.home_view.click_system_back_button()

        self.wallet_view.just_fyi("Removing newly added account")
        if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
            self.wallet_view.remove_account(account_name=new_account_name)
            if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
                self.errors.append("Account was not removed from wallet")
        else:
            self.errors.append("Newly added account is not shown in the accounts list")

        self.errors.verify_no_errors()

    @marks.testrail_id(727232)
    def test_wallet_add_remove_watch_only_account(self):
        self.wallet_view.just_fyi("Adding new watch only account")
        new_account_name = "Account to watch"
        address_to_watch = "0x8d2413447ff297d30bdc475f6d5cb00254685aae"
        self.wallet_view.add_watch_only_account(address=address_to_watch, account_name=new_account_name)

        if self.wallet_view.account_name_text.text != new_account_name:
            pytest.fail("Account to watch was not added")
        self.wallet_view.close_account_button.click_until_presence_of_element(self.home_view.show_qr_code_button)

        self.wallet_view.just_fyi("Checking that the new wallet is added to the Share QR Code menu")
        self.home_view.show_qr_code_button.click()
        self.home_view.share_wallet_tab_button.click()
        if self.home_view.account_name_text.text != 'Account 1':
            self.errors.append("Incorrect first account is shown on Share QR Code menu")
        self.home_view.qr_code_image_element.swipe_left_on_element()
        try:
            self.home_view.account_name_text.wait_for_element_text(text=new_account_name, wait_time=3)
        except Failed:
            self.errors.append("Can't swipe between accounts, account to watch is not shown")
        else:
            shown_address = self.home_view.copy_wallet_address()
            if set(shown_address.split(':')) != {'eth', 'arb1', 'opt', address_to_watch}:
                self.home_view.driver.fail(
                    "Incorrect address '%s' is shown when swiping between accounts, expected one is '%s'" % (
                        shown_address, ':'.join(address_to_watch)))
        self.home_view.click_system_back_button()

        self.wallet_view.just_fyi("Removing account to watch")
        if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
            self.wallet_view.remove_account(account_name=new_account_name, watch_only=True)
            if self.wallet_view.get_account_element(account_name=new_account_name).is_element_displayed():
                self.errors.append("Account was not removed from wallet")
        else:
            self.errors.append("Watch only account is not shown in the accounts list")

        self.errors.verify_no_errors()
