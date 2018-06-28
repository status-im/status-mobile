import time

from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@marks.chat
class TestMessagesPublicChat(SingleDeviceTestCase):

    @marks.testrail_id(1380)
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
        first_request_time = time.time()
        console_view.chat_element_by_text('Faucet request has been received').wait_for_visibility_of_element()
        console_view.send_faucet_request()
        console_view.chat_element_by_text('Faucet request error').wait_for_visibility_of_element()
        console_view.get_back_to_home_view()
        wallet_view = profile_view.wallet_button.click()
        wallet_view.set_up_wallet()
        wallet_view.wait_balance_changed_on_wallet_screen()
        wallet_view.home_button.click()
        console_chat.click()
        wait_time = 300 - (time.time() - first_request_time)
        time.sleep(wait_time if wait_time > 0 else 0)
        console_view.send_faucet_request()
        console_view.chat_element_by_text('Faucet request has been received').wait_for_visibility_of_element()
