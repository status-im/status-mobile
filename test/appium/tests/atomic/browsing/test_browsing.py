import pytest
from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestBrowsing(SingleDeviceTestCase):

    @marks.testrail_id(1411)
    def test_browse_page_with_non_english_text(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        home_view = sign_in.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('www.wikipedia.org')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()
        wiki_texts = ['Español', '日本語', 'Français', '中文', 'Português']
        for wiki_text in wiki_texts:
            browsing_view.find_text_part(wiki_text, 15)

    @marks.testrail_id(1412)
    def test_open_invalid_link(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        home_view = sign_in.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('invalid.takoe')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.find_text_part('Unable to load page')

    @marks.testrail_id(3705)
    def test_connection_is_not_secure(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user()
        home_view = sign_in.get_home_view()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('google.com')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.find_full_text("Connection is not proven secure. Make sure you trust this site before signing "
                                     "transactions or entering personal data.")

    @marks.testrail_id(3731)
    def test_swipe_to_delete_browser_entry(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('google.com')
        start_new_chat.confirm()
        browsing_view = home_view.get_base_web_view()
        browsing_view.browser_cross_icon.click()
        home_view.get_chat_with_user('Browser').swipe_and_delete()
        home_view.relogin()
        if home_view.get_chat_with_user('Browser').is_element_present(20):
            pytest.fail('The browser entry is present after re-login')

    @marks.testrail_id(1396)
    @marks.smoke_1
    def test_open_google_com_via_open_dapp(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('google.com')
        start_new_chat.confirm()
        browsing_view = start_new_chat.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()
        assert browsing_view.element_by_text('Google').is_element_displayed()

    @marks.testrail_id(1397)
    @marks.smoke_1
    def test_back_forward_buttons_browsing_website(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_d_app_button.click()
        start_new_chat.enter_url_editbox.set_value('www.wikipedia.org')
        start_new_chat.confirm()
        browsing_view = start_new_chat.get_base_web_view()
        browsing_view.wait_for_d_aap_to_load()

        browsing_view.element_by_text_part('Русский', 'button').click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.browser_previous_page_button.click()
        browsing_view.find_text_part('English', 15)

        browsing_view.browser_next_page_button.click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.back_to_home_button.click()

    @marks.testrail_id(3783)
    @marks.smoke_1
    def test_refresh_button_browsing_app_webview(self):
        sign_in_view = SignInView(self.driver)
        sign_in_view.create_user()
        status_test_dapp = sign_in_view.open_status_test_dapp()
        status_test_dapp.wait_for_d_aap_to_load()
        status_test_dapp.transactions_button.click()
        status_test_dapp.find_full_text('Sign message')
        status_test_dapp.browser_refresh_page_button.click()
        status_test_dapp.find_full_text('defaultAccount')
