import pytest
from tests import marks, connection_not_secure_text, connection_is_secure_text
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.all
class TestBrowsing(SingleDeviceTestCase):

    @marks.testrail_id(5424)
    @marks.medium
    def test_browse_page_with_non_english_text(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        browsing_view = start_new_chat.open_url('www.wikipedia.org')
        wiki_texts = ['Español', '日本語', 'Français', '中文', 'Português']
        for wiki_text in wiki_texts:
            browsing_view.find_text_part(wiki_text, 15)

    @marks.testrail_id(5465)
    @marks.medium
    def test_open_invalid_link(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        browsing_view = start_new_chat.open_url('invalid.takoe')
        browsing_view.find_text_part('Unable to load page')
        browsing_view.cross_icon.click()
        if home_view.element_by_text('Browser').is_element_displayed():
            pytest.fail('Browser entity is shown for an invalid link')

    @marks.testrail_id(5430)
    @marks.medium
    def test_connection_is_not_secure(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        browsing_view = start_new_chat.open_url('www.bbc.com')
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_not_secure_text)

    @marks.testrail_id(5402)
    @marks.high
    def test_connection_is_secure(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        browsing_view = start_new_chat.open_url('https://www.bbc.com')
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_is_secure_text)
        browsing_view.cross_icon.click()
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.open_d_app_button.click()
        start_new_chat_view.element_by_text('Airswap').click()
        start_new_chat_view.open_button.click()
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_is_secure_text)

    @marks.testrail_id(5390)
    @marks.high
    def test_swipe_to_delete_browser_entry(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        start_new_chat = home_view.plus_button.click()
        browsing_view = start_new_chat.open_url('google.com')
        browsing_view.cross_icon.click()
        home_view.get_chat_with_user('Browser').swipe_and_delete()
        home_view.relogin()
        if home_view.get_chat_with_user('Browser').is_element_present(20):
            pytest.fail('The browser entry is present after re-login')

    @marks.testrail_id(5320)
    @marks.critical
    def test_open_google_com_via_open_dapp(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        start_new_chat = home.plus_button.click()
        start_new_chat.open_url('google.com')
        browsing_view = start_new_chat.get_base_web_view()
        browsing_view.element_by_text('Google').wait_for_element(30)

    @marks.testrail_id(5321)
    @marks.skip
    @marks.critical
    def test_back_forward_buttons_browsing_website(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        start_new_chat = home.plus_button.click()
        browsing_view = start_new_chat.open_url('www.wikipedia.org')
        browsing_view.element_by_text_part('Русский', 'button').click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.browser_previous_page_button.click()
        browsing_view.find_text_part('English', 15)

        browsing_view.browser_next_page_button.click()
        browsing_view.find_text_part('Избранная статья')
        browsing_view.back_to_home_button.click()

    @marks.testrail_id(5354)
    @marks.critical
    def test_refresh_button_browsing_app_webview(self):
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        status_test_dapp = home_view.open_status_test_dapp()
        status_test_dapp.transactions_button.click()
        status_test_dapp.find_full_text('Sign message')
        status_test_dapp.browser_refresh_page_button.click()
        status_test_dapp.find_full_text('defaultAccount')
