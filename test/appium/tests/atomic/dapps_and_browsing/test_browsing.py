import pytest
from tests import marks, connection_not_secure_text, connection_is_secure_text
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from views.dapps_view import DappsView


@pytest.mark.all
class TestBrowsing(SingleDeviceTestCase):

    @marks.testrail_id(5424)
    @marks.medium
    def test_browse_page_with_non_english_text(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        daap_view = home_view.dapp_tab_button.click()
        browsing_view = daap_view.open_url('www.wikipedia.org')
        wiki_texts = ['Español', '日本語', 'Français', '中文', 'Português']
        for wiki_text in wiki_texts:
            browsing_view.find_text_part(wiki_text, 15)

    @marks.testrail_id(5465)
    @marks.medium
    def test_open_invalid_link(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        daap_view = home_view.dapp_tab_button.click()
        browsing_view = daap_view.open_url('invalid.takoe')
        browsing_view.find_text_part('Unable to load page')
        browsing_view.cross_icon.click()
        if home_view.element_by_text('Browser').is_element_displayed():
            pytest.fail('Browser entity is shown for an invalid link')

    @marks.testrail_id(6210)
    @marks.high
    def test_open_blocked_site(self):
        home_view = SignInView(self.driver).create_user()
        daap_view = home_view.dapp_tab_button.click()
        daap_view.open_url('https://www.cryptokitties.domainname').find_text_part('This site is blocked')

    @marks.testrail_id(5430)
    @marks.medium
    def test_connection_is_not_secure(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        daap_view = home_view.dapp_tab_button.click()
        browsing_view = daap_view.open_url('http://www.dvwa.co.uk')
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_not_secure_text)

    @marks.testrail_id(5402)
    @marks.high
    def test_connection_is_secure(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        daap_view = home_view.dapp_tab_button.click()
        browsing_view = daap_view.open_url('https://www.bbc.com')
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_is_secure_text)
        browsing_view.cross_icon.click()

        browsing_view = daap_view.open_url('https://instant.airswap.io')
        browsing_view.url_edit_box_lock_icon.click()
        browsing_view.find_full_text(connection_is_secure_text)

    @marks.testrail_id(5390)
    @marks.high
    def test_long_press_delete_clear_all_dapps(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        dapp_view = home_view.dapp_tab_button.click()
        browsing_view = dapp_view.open_url('google.com')
        browsing_view.cross_icon.click()
        dapp_view = DappsView(self.driver)
        browser_entry = dapp_view.remove_browser_entry_long_press('Google')
        home_view.relogin()
        home_view.dapp_tab_button.click()
        if browser_entry.is_element_present(20):
            self.errors.append('The browser entry is present after re-login')
        for entry in ('google.com', 'status.im'):
            browsing_view = dapp_view.open_url(entry)
            browsing_view.cross_icon.click()
        dapp_view.remove_browser_entry_long_press('Status - Private', clear_all=True)
        home_view.relogin()
        home_view.dapp_tab_button.click()
        if not dapp_view.element_by_text('Browsed websites will appear here.').is_element_displayed():
            self.errors.append('Browser history is not empty')

    @marks.testrail_id(5320)
    @marks.critical
    def test_open_google_com_via_open_dapp(self):
        sign_in_view = SignInView(self.driver)
        home = sign_in_view.create_user()
        open_dapp_view = home.dapp_tab_button.click()
        open_dapp_view.open_url('google.com')
        browsing_view = open_dapp_view.get_base_web_view()
        browsing_view.element_by_text('Google').wait_for_element(30)

    @marks.testrail_id(5321)
    @marks.skip
    @marks.critical
    def test_back_forward_buttons_browsing_website(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        daap_view = home.dapp_tab_button.click()
        browsing_view = daap_view.open_url('www.wikipedia.org')
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

    @marks.testrail_id(5785)
    @marks.critical
    def test_can_open_dapp_from_dapp_store(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        daap_view = home.dapp_tab_button.click()
        dapp_store_view = daap_view.discover_dapps_button.click()
        dapp_store_view.element_by_text_part("CryptoKitties").click()
        if not dapp_store_view.element_by_text_part("Start", "text").is_element_displayed(20):
            self.driver.fail("Failed to access CryptoKitties Dapp from Discover Dapp store")
