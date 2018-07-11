import time

import pytest

from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesPublicChat(SingleDeviceTestCase):

    @marks.testrail_id(1380)
    @marks.smoke_1
    def test_faucet_console_command(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.debug_mode_toggle.click()
        home_view = profile_view.home_button.click()
        console_chat = home_view.get_chat_with_user('Status Console')
        console_view = console_chat.click()
        console_view.send_faucet_request()
        console_view.chat_element_by_text('Faucet request has been received').wait_for_visibility_of_element()
        console_view.get_back_to_home_view()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_changed_on_wallet_screen()

    @marks.testrail_id(1400)
    @marks.smoke_1
    def test_web3_block_number(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        profile_view = sign_in_view.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.debug_mode_toggle.click()
        home_view = profile_view.home_button.click()
        chat_view = home_view.get_chat_with_user('Status Console').click()
        chat_view.chat_message_input.send_keys('web3.eth.blockNumber')
        block_number = self.network_api.get_latest_block_number()
        chat_view.send_message_button.click()
        for i in range(4):
            if chat_view.chat_element_by_text(str(block_number + i)).is_element_displayed():
                break
        else:
            pytest.fail('Actual block number is not shown')
