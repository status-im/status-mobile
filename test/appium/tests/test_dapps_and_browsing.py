import pytest
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestDAppsAndBrowsing(SingleDeviceTestCase):

    @pytest.mark.pr
    def test_browse_link_entering_url_in_dapp_view(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        home_view = sign_in.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('status.im')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()
        browsing_view.find_full_text('Status, the Ethereum discovery tool.')
        browsing_view.back_to_home_button.click()

        assert home_view.chat_name_text.text in 'Status | The Mobile Ethereum Client'
        assert home_view.chat_url_text.text in 'https://status.im/'
