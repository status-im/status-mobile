import pytest
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
