import pytest
from tests import transaction_users, marks
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
        home_view.swipe_and_delete_chat('Browser')
        home_view.relogin()
        if home_view.get_chat_with_user('Browser').is_element_present(20):
            pytest.fail('The browser entry is present after re-login')
