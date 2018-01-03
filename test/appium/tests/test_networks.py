import pytest
from itertools import combinations_with_replacement
from tests.base_test_case import MultipleDeviceTestCase, SingleDeviceTestCase
from tests import user_flow
from views.console_view import ConsoleView
from selenium.common.exceptions import TimeoutException


class TestNetwork(SingleDeviceTestCase):

    @pytest.mark.network
    @pytest.mark.parametrize("network", ['Ropsten', 'Rinkeby', 'Rinkeby with upstream RPC',
                                         'Mainnet', 'Mainnet with upstream RPC'])
    def test_network_switch(self, network):

        console = ConsoleView(self.driver)
        user_flow.create_user(console)
        console.back_button.click()
        chats = console.get_chat_view()
        profile_drawer = chats.profile_button.click()
        profile_view = profile_drawer.profile_icon.click()
        sign_in_view = profile_view.switch_network(network)
        sign_in_view.first_account_button.click()
        sign_in_view.password_input.send_keys('qwerty1234')
        sign_in_view.sign_in_button.click()
        sign_in_view.find_full_text('Chats', 20)


class TestNetworkChats(MultipleDeviceTestCase):

    network_combinations = list(combinations_with_replacement(
        ['Ropsten', 'Rinkeby', 'Mainnet',
         'Rinkeby with upstream RPC', 'Mainnet with upstream RPC', 'Rinkeby with upstream RPC'], 2))

    @pytest.mark.network_chat
    @pytest.mark.parametrize("network", network_combinations,
                             ids=[i[0] + ' & ' + i[1] for i in network_combinations])
    def test_one_to_one_chat_between(self, network):
        device_1, device_2 = ConsoleView(self.driver_1), ConsoleView(self.driver_2)
        for device in device_1, device_2:
            user_flow.create_user(device)
        device_1.back_button.click()
        device_1_chats = device_1.get_chat_view()
        device_1_profile_drawer= device_1_chats.profile_button.click()
        device_1_profile_view = device_1_profile_drawer.profile_icon.click()
        device_1_public_key = device_1_profile_view.public_key_text.text
        if network[0] != 'Ropsten with upstream RPC':
            login_d1 = device_1_profile_view.switch_network(network[0])
            login_d1.first_account_button.click()
            login_d1.password_input.send_keys('qwerty1234')
            login_d1.sign_in_button.click()
            login_d1.find_full_text('Chats', 60)
        else:
            device_1_profile_view.back_button.click()
        device_2.back_button.click()
        device_2_chats = device_2.get_chat_view()
        if network[1] != 'Ropsten with upstream RPC':
            device_2_profile_drawer = device_2_chats.profile_button.click()
            device_2_profile_view = device_2_profile_drawer.profile_icon.click()
            device_2_sign_in = device_2_profile_view.switch_network(network[1])
            device_2_sign_in.first_account_button.click()
            device_2_sign_in.password_input.send_keys('qwerty1234')
            device_2_sign_in.sign_in_button.click()
            device_2_sign_in.find_full_text('Chats', 60)
        user_flow.add_contact(device_2_chats,device_1_public_key )
        message_1 = network[0]
        message_2 = network[1]
        user_d1_name = device_2_chats.user_name_text.text
        device_2_chats.chat_message_input.send_keys(message_2)
        device_2_chats.send_message_button.click()
        errors = list()
        try:
            device_1_chats.find_full_text(message_2)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #1")
        one_to_one_chat_d1 = device_1_chats.element_by_text(message_2, 'button')
        one_to_one_chat_d1.click()
        one_to_one_chat_d2 = device_2_chats.element_by_text(user_d1_name, 'button')
        one_to_one_chat_d2.click()
        device_1_chats.chat_message_input.send_keys(message_1)
        device_1_chats.send_message_button.click()
        try:
            device_2_chats.find_full_text(message_1)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #2")
        if errors:
            msg = ''
            for error in errors:
                msg += (error + '\n')
            pytest.fail(msg, pytrace=False)
