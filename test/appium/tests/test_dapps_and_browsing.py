import pytest
from tests import transaction_users, marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestDAppsAndBrowsing(SingleDeviceTestCase):

    @pytest.mark.pr
    @pytest.mark.testrail_case_id(3389)
    def test_browse_link_entering_url_in_dapp_view(self):
        """
        Navigate to non-Dapp site with non-english content
        Check back and forward browsing buttons works as expected
        """

        sign_in = SignInView(self.driver)
        sign_in.create_user()
        home_view = sign_in.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('www.wikipedia.org')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()

        wikipedia_home_text_list = ['Español','日本語', 'Français', 'English']
        for wikitext in wikipedia_home_text_list:
            browsing_view.find_text_part(wikitext, 15)
        browsing_view.element_by_text_part('Русский', 'button').click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.browser_previous_page_button.click()
        browsing_view.find_text_part(wikipedia_home_text_list[0], 15)

        browsing_view.browser_next_page_button.click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.back_to_home_button.click()
        expected_title = 'Browser'
        expected_url = 'https://ru.m.wikipedia.org'

        if not home_view.chat_name_text.text.startswith(expected_title):
            self.errors.append("'%s' web page title instead of '%s'", (home_view.chat_name_text.text, expected_title))
        if not home_view.chat_url_text.text.startswith(expected_url):
            self.errors.append("'%s' web page URL instead of '%s'", (home_view.chat_url_text.text, expected_url))

        self.verify_no_errors()

    @marks.pr
    @marks.testrail_case_id(3404)
    def test_send_transaction_from_daap(self):
        sender = transaction_users['B_USER']
        sign_in_view = SignInView(self.driver)
        sign_in_view.recover_access(sender['passphrase'], sender['password'])
        address = transaction_users['B_USER']['address']
        initial_balance = self.network_api.get_balance(address)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.assets_button.click()
        send_transaction_view = status_test_dapp.request_stt_button.click()
        send_transaction_view.sign_transaction(sender['password'])
        self.network_api.verify_balance_is_updated(initial_balance, address)

    @marks.pr
    @marks.testrail_case_id(3675)
    def test_sign_message_from_daap(self):
        password = 'passwordfordaap'
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user(password)
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        send_transaction_view = status_test_dapp.sign_message_button.click()
        send_transaction_view.find_full_text('Kudos to Andrey!')
        send_transaction_view.sign_transaction_button.click_until_presence_of_element(
            send_transaction_view.enter_password_input)
        send_transaction_view.enter_password_input.send_keys(password)
        send_transaction_view.sign_transaction_button.click()

