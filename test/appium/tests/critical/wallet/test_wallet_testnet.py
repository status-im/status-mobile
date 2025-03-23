import datetime
import time

import pytest
from selenium.common import NoSuchElementException

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from support.api.network_api import NetworkApi
from tests import marks, run_in_parallel
from users import transaction_senders
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_four_2")
@marks.nightly
@marks.secured
@marks.smoke
class TestWalletMultipleDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.network_api = NetworkApi()
        self.drivers, self.loop = create_shared_drivers(2)
        self.sign_in_1, self.sign_in_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.sender, self.receiver = transaction_senders['ETH_1'], transaction_senders['ETH_2']
        self.sender['wallet_address'] = '0x' + self.sender['address']
        self.receiver['wallet_address'] = '0x' + self.receiver['address']
        self.loop.run_until_complete(
            run_in_parallel(((self.sign_in_1.recover_access, {'passphrase': self.sender['passphrase']}),
                             (self.sign_in_2.recover_access, {'passphrase': self.receiver['passphrase']}))))
        self.home_1, self.home_2 = self.sign_in_1.get_home_view(), self.sign_in_2.get_home_view()
        self.sender_username, self.receiver_username = self.home_1.get_username(), self.home_2.get_username()
        self.wallet_1, self.wallet_2 = self.sign_in_1.get_wallet_view(), self.sign_in_2.get_wallet_view()
        self.wallet_1.wallet_tab.click()
        self.wallet_2.wallet_tab.click()
        self.network = "Arbitrum"

    def _get_balances_before_tx(self):
        # ToDo: Arbiscan API is down, looking for analogue
        # sender_balance = self.network_api.get_balance(self.sender['wallet_address'])
        # receiver_balance = self.network_api.get_balance(self.receiver['wallet_address'])
        self.wallet_1.just_fyi("Getting ETH amount in the wallet of the sender before transaction")
        self.wallet_1.get_account_element().click()
        eth_amount_sender = self.wallet_1.get_asset(asset_name='Ether').get_amount()
        self.wallet_2.just_fyi("Getting ETH amount in the wallet of the receiver before transaction")
        self.wallet_2.get_account_element().click()
        eth_amount_receiver = self.wallet_2.get_asset(asset_name='Ether').get_amount()
        # return sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver
        return eth_amount_sender, eth_amount_receiver

    def _check_balances_after_tx(self, amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
                                 eth_amount_receiver):
        # ToDo: Arbiscan API is down, looking for analogue
        # try:
        #     self.network_api.wait_for_balance_to_be(address=self.sender['wallet_address'],
        #                                             expected_balance=sender_balance - amount_to_send)
        # except TimeoutException as e:
        #     self.errors.append("Sender " + e.msg)
        # try:
        #     self.network_api.wait_for_balance_to_be(address=self.receiver['wallet_address'],
        #                                             expected_balance=receiver_balance + amount_to_send)
        # except TimeoutException as e:
        #     self.errors.append("Receiver " + e.msg)

        def wait_for_wallet_balance_to_update(wallet_view, user_name, initial_eth_amount):
            wallet_view.just_fyi("Getting ETH amount in the wallet of the %s after transaction" % user_name)
            if user_name == self.sender_username:
                exp_amount = round(initial_eth_amount - amount_to_send, 4)
            else:
                exp_amount = round(initial_eth_amount + amount_to_send, 4)

            # for _ in range(12):  # ToDo: 120 sec wait time, enable when autoupdate feature is ready
            new_eth_amount = round(wallet_view.get_asset(asset_name='Ether').get_amount(), 4)
            if user_name == self.sender_username and new_eth_amount <= exp_amount:
                return
            if user_name == self.receiver_username and new_eth_amount >= exp_amount:
                return
            self.errors.append(wallet_view,
                               "Eth amount in the %s's wallet is %s but should be %s" % (
                                   user_name, new_eth_amount, exp_amount))

        # ToDo: disable relogin when autoupdate feature is ready
        self.home_1.just_fyi("Relogin for getting an updated balance")
        self.home_2.just_fyi("Relogin for getting an updated balance")
        for _ in range(6):  # just waiting 1 minute here to be sure that balances are updated
            self.wallet_1.wallet_tab.is_element_displayed()
            self.wallet_2.wallet_tab.is_element_displayed()
            time.sleep(10)
        self.loop.run_until_complete(
            run_in_parallel(((self.home_1.reopen_app, {'user_name': self.sender_username}),
                             (self.home_2.reopen_app, {'user_name': self.receiver_username}))))
        self.wallet_1.wallet_tab.wait_and_click()
        self.wallet_2.wallet_tab.wait_and_click()
        self.wallet_1.set_network_in_wallet(network_name=self.network)
        self.wallet_2.set_network_in_wallet(network_name=self.network)
        self.loop.run_until_complete(
            run_in_parallel(((wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_1,
                                                                  'user_name': self.sender_username,
                                                                  'initial_eth_amount': eth_amount_sender}),
                             (wait_for_wallet_balance_to_update, {'wallet_view': self.wallet_2,
                                                                  'user_name': self.receiver_username,
                                                                  'initial_eth_amount': eth_amount_receiver}))))

    def _check_last_transaction_in_activity(self, wallet_view, device_time, amount_to_send, sender=True):
        wallet_view.get_account_element().click()
        wallet_view.activity_tab.click()
        wallet_view.just_fyi("Checking the transaction in the activity tab")
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_time = "Today %s" % current_time.strftime('%-I:%M %p')
        possible_times = [expected_time,
                          "Today %s" % (current_time + datetime.timedelta(minutes=1)).strftime('%-I:%M %p')]
        sender_address_short = self.sender['wallet_address'].replace(self.sender['wallet_address'][5:-3], '...').lower()
        receiver_address_short = self.receiver['wallet_address'].replace(self.receiver['wallet_address'][5:-3],
                                                                         '...').lower()
        activity_element = wallet_view.get_activity_element()
        try:
            if not all((activity_element.header == 'Send' if sender else 'Receive',
                        activity_element.timestamp in possible_times,
                        activity_element.amount == '%s ETH' % amount_to_send,
                        activity_element.from_text == sender_address_short,
                        activity_element.to_text == receiver_address_short)):
                self.errors.append(
                    wallet_view,
                    "The last transaction is not listed in activity for the %s, expected timestamp is %s" %
                    ('sender' if sender else 'receiver', expected_time))
        except NoSuchElementException:
            self.errors.append(wallet_view,
                               "Can't find the last transaction for the %s" % ('sender' if sender else 'receiver'))
        finally:
            wallet_view.close_account_button.click_until_presence_of_element(wallet_view.show_qr_code_button)

    @marks.testrail_id(727229)
    def test_wallet_send_eth(self):
        self.wallet_1.set_network_in_wallet(network_name=self.network)
        self.wallet_2.set_network_in_wallet(network_name=self.network)
        # sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()
        eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()

        self.wallet_2.close_account_button.click()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending funds from wallet")
        amount_to_send = 0.0001
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.send_asset(address='arb1:' + self.receiver['wallet_address'],
                                 asset_name='Ether',
                                 amount=f"{amount_to_send:.4f}",
                                 network_name=self.network)
        # ToDo: Arbiscan API is down, looking for analogue
        # self.network_api.wait_for_confirmation_of_transaction(address=self.sender['wallet_address'],
        #                                                       tx_time=device_time_before_sending)

        device_time_after_sending = self.wallet_1.driver.device_time

        # self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
        #                               eth_amount_receiver)
        self._check_balances_after_tx(amount_to_send, None, None, eth_amount_sender, eth_amount_receiver)

        # ToDo: enable when issues 20807 and 20808 are fixed
        # self.loop.run_until_complete(
        #     run_in_parallel(((self._check_last_transaction_in_activity, {'wallet_view': self.wallet_1,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send}),
        #                      (self._check_last_transaction_in_activity, {'wallet_view': self.wallet_2,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send,
        #                                                                  'sender': False}))))
        self.errors.verify_no_errors()

    @marks.testrail_id(727230)
    def test_wallet_send_asset_from_drawer(self):
        self.wallet_1.navigate_back_to_wallet_view()
        # sender_balance, receiver_balance, eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()
        eth_amount_sender, eth_amount_receiver = self._get_balances_before_tx()
        self.wallet_2.close_account_button.click_if_shown()
        self.wallet_2.chats_tab.click()

        self.wallet_1.just_fyi("Sending asset from drawer")
        amount_to_send = 0.0001
        device_time_before_sending = self.wallet_1.driver.device_time
        self.wallet_1.send_asset_from_drawer(address='arb1:' + self.receiver['wallet_address'],
                                             asset_name='Ether',
                                             amount=f"{amount_to_send:.4f}",
                                             network_name=self.network)
        # ToDo: Arbiscan API is down, looking for analogue
        # self.network_api.wait_for_confirmation_of_transaction(address=self.sender['wallet_address'],
        #                                                       tx_time=device_time_before_sending)
        device_time_after_sending = self.wallet_1.driver.device_time

        # self._check_balances_after_tx(amount_to_send, sender_balance, receiver_balance, eth_amount_sender,
        #                               eth_amount_receiver)
        self._check_balances_after_tx(amount_to_send, None, None, eth_amount_sender, eth_amount_receiver)

        # ToDo: enable when issues 20807 and 20808 are fixed
        # self.loop.run_until_complete(
        #     run_in_parallel(((self._check_last_transaction_in_activity, {'wallet_view': self.wallet_1,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send}),
        #                      (self._check_last_transaction_in_activity, {'wallet_view': self.wallet_2,
        #                                                                  'device_time': device_time,
        #                                                                  'amount_to_send': amount_to_send,
        #                                                                  'sender': False}))))
        self.errors.verify_no_errors()
