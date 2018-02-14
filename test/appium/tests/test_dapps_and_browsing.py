import pytest
from views.console_view import ConsoleView
from tests.base_test_case import SingleDeviceTestCase


@pytest.mark.all
class TestDappsAnsBrowsing(SingleDeviceTestCase):

    @pytest.mark.pr
    def test_browse_link_entering_url_in_dapp_view(self):
        console = ConsoleView(self.driver)
        console.create_user()
        console.back_button.click()
        home_view = console.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.send_keys('status.im')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()
        browsing_view.find_full_text('Status, the Ethereum discovery tool.')
        browsing_view.back_to_home_button.click()

        assert home_view.first_chat_element_title.text == 'Status | The Mobile Ethereum Client'
