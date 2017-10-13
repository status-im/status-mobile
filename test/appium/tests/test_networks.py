import pytest
from itertools import combinations_with_replacement
from tests.basetestcase import MultiplyDeviceTestCase, SingleDeviceTestCase
from tests.preconditions import set_password_as_new_user
from views.home import HomeView
from selenium.common.exceptions import TimeoutException


class TestNetwork(SingleDeviceTestCase):

    @pytest.mark.network
    @pytest.mark.parametrize("network", ['Ropsten', 'Rinkeby', 'Rinkeby with upstream RPC',
                                         'Mainnet', 'Mainnet with upstream RPC'])
    def test_network_switch(self, network):

        home = HomeView(self.driver)
        set_password_as_new_user(home)
        home.back_button.click()
        chats = home.get_chats()
        chats.profile_button.click()
        profile = chats.profile_icon.click()
        login = profile.switch_network(network)
        login.first_account_button.click()
        login.password_input.send_keys('qwerty1234')
        login.sign_in_button.click()
        login.find_full_text('Chats', 20)


class TestNetworkChats(MultiplyDeviceTestCase):

    network_combinations = list(combinations_with_replacement(
        ['Ropsten', 'Rinkeby', 'Mainnet',
         'Rinkeby with upstream RPC', 'Mainnet with upstream RPC', 'Rinkeby with upstream RPC'], 2))

    @pytest.mark.network_chat
    @pytest.mark.parametrize("network", network_combinations,
                             ids=[i[0] + ' & ' + i[1] for i in network_combinations])
    def test_one_to_one_chat_between(self, network):
        device_1, device_2 = HomeView(self.driver_1), HomeView(self.driver_2)
        set_password_as_new_user(device_1, device_2)
        device_1.back_button.click()
        chats_d1 = device_1.get_chats()
        chats_d1.profile_button.click()
        profile_d1 = chats_d1.profile_icon.click()
        key = profile_d1.public_key_text.text
        if network[0] != 'Ropsten with upstream RPC':
            login_d1 = profile_d1.switch_network(network[0])
            login_d1.first_account_button.click()
            login_d1.password_input.send_keys('qwerty1234')
            login_d1.sign_in_button.click()
            login_d1.find_full_text('Chats', 60)
        else:
            profile_d1.back_button.click()
        device_2.back_button.click()
        chats_d2 = device_2.get_chats()
        if network[1] != 'Ropsten with upstream RPC':
            chats_d2.profile_button.click()
            profile_d2 = chats_d2.profile_icon.click()
            login_d2 = profile_d2.switch_network(network[1])
            login_d2.first_account_button.click()
            login_d2.password_input.send_keys('qwerty1234')
            login_d2.sign_in_button.click()
            login_d2.find_full_text('Chats', 60)
        chats_d2.plus_button.click()
        chats_d2.add_new_contact.click()
        chats_d2.public_key_edit_box.send_keys(key)
        chats_d2.confirm()
        chats_d2.confirm_public_key_button.click()
        message_1 = network[0]
        message_2 = network[1]
        user_d1_name = chats_d2.user_name_text.text
        chats_d2.chat_message_input.send_keys(message_2)
        chats_d2.send_message_button.click()
        errors = list()
        try:
            chats_d1.find_full_text(message_2)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #1")
        one_to_one_chat_d1 = chats_d1.element_by_text(message_2, 'button')
        one_to_one_chat_d1.click()
        one_to_one_chat_d2 = chats_d2.element_by_text(user_d1_name, 'button')
        one_to_one_chat_d2.click()
        chats_d1.chat_message_input.send_keys(message_1)
        chats_d1.send_message_button.click()
        try:
            chats_d2.find_full_text(message_1)
        except TimeoutException:
            errors.append("Message '%s' wasn't received by Device #2")
        if errors:
            msg = ''
            for error in errors:
                msg += (error + '\n')
            pytest.fail(msg, pytrace=False)
