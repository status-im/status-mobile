import pytest
from itertools import combinations_with_replacement
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from selenium.common.exceptions import TimeoutException

from views.sign_in_view import SignInView


class TestNetwork(SingleDeviceTestCase):

    @pytest.mark.network
    @pytest.mark.parametrize("network", ['Ropsten', 'Rinkeby', 'Rinkeby with upstream RPC',
                                         'Mainnet', 'Mainnet with upstream RPC'])
    def test_network_switch(self, network):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.switch_network(network)
        sign_in_view.click_account_by_position(0)
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        sign_in_view.find_full_text('Wallet', 20)


class TestNetworkChats(MultipleDeviceTestCase):
    network_combinations = list(combinations_with_replacement(
        ['Ropsten', 'Rinkeby', 'Mainnet',
         'Rinkeby with upstream RPC', 'Mainnet with upstream RPC', 'Rinkeby with upstream RPC'], 2))

    @pytest.mark.network_chat
    @pytest.mark.parametrize("network", network_combinations,
                             ids=[i[0] + ' & ' + i[1] for i in network_combinations])
    def test_one_to_one_chat_between(self, network):
        self.create_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        for sign_in in device_1, device_2:
            sign_in.create_user()
        device_1_profile_view = device_1.profile_button.click()
        device_1_public_key = device_1_profile_view.public_key_text.text
        if network[0] != 'Ropsten with upstream RPC':
            login_d1 = device_1_profile_view.switch_network(network[0])
            login_d1.click_account_by_position(0)
            login_d1.password_input.send_keys('qwerty1234')
            login_d1.sign_in_button.click()
            login_d1.find_full_text('Wallet', 60)
        else:
            device_1_profile_view.back_button.click()
        device_2_home_view = device_2.get_home_view()
        if network[1] != 'Ropsten with upstream RPC':
            device_2_profile_view = device_2.profile_button.click()
            device_2_sign_in = device_2_profile_view.switch_network(network[1])
            device_2_sign_in.click_account_by_position(0)
            device_2_sign_in.password_input.send_keys('qwerty1234')
            device_2_home_view = device_2_sign_in.sign_in_button.click()
            device_2_home_view.find_full_text('Wallet', 60)
        device_2_home_view.add_contact(device_1_public_key)
        device_2_chat = device_2.get_chat_view()
        message_1 = network[0]
        message_2 = network[1]
        device_2_chat.chat_message_input.send_keys(message_2)
        device_2_chat.send_message_button.click()
        errors = list()
        try:
            device_1.find_full_text(message_2)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #1")
        device_1.element_by_text(message_2, 'button').click()
        device_1_chat = device_1.get_chat_view()
        device_1_chat.chat_message_input.send_keys(message_1)
        device_1_chat.send_message_button.click()
        try:
            device_2_chat.find_full_text(message_1)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #2")
        if errors:
            msg = ''
            for error in errors:
                msg += (error + '\n')
            pytest.fail(msg, pytrace=False)
