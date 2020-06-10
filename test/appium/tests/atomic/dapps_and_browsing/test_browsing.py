import pytest
from tests import marks, connection_not_secure_text, connection_is_secure_text, test_dapp_url
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from views.dapps_view import DappsView
import time


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

    @marks.testrail_id(5395)
    @marks.medium
    def test_navigation_history_kept_after_relogin(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        dapp_view = home_view.dapp_tab_button.click()
        ru_url = 'https://ru.m.wikipedia.org'
        browsing_view = dapp_view.open_url(ru_url)
        browsing_view.find_text_part('Добро пожаловать')

        browsing_view.just_fyi('Navigate to next page and back')
        browsing_view.element_by_text_part('свободную энциклопедию').click()
        browsing_view.element_by_text_part('Свободный контент')
        browsing_view.browser_previous_page_button.click()

        browsing_view.just_fyi('Relogin and check that tap on "Next" navigates to next page')
        browsing_view.relogin()
        home_view.dapp_tab_button.click()
        dapp_view.element_by_text_part(ru_url).click()
        browsing_view.browser_next_page_button.click()
        if not browsing_view.element_by_text_part('Свободный контент').is_element_displayed(20):
            self.driver.fail("Browser history is not kept after relogin")

    @marks.testrail_id(5438)
    @marks.medium
    def test_browser_shows_offline_state(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        home_view.toggle_airplane_mode()
        dapp_view = home_view.dapp_tab_button.click()
        browsing_view = dapp_view.open_url('status.im')
        offline_texts = ['Unable to load page', 'ERR_INTERNET_DISCONNECTED']
        for text in offline_texts:
            browsing_view.find_text_part(text, 15)
        home_view.toggle_airplane_mode()
        browsing_view.browser_refresh_page_button.click_until_presence_of_element(browsing_view.element_by_text_part('An Open Source Community'))

    @marks.testrail_id(5465)
    @marks.medium
    def test_open_invalid_link(self):
        sign_in = SignInView(self.driver)
        home_view = sign_in.create_user()
        daap_view = home_view.dapp_tab_button.click()
        browsing_view = daap_view.open_url('invalid.takoe')
        browsing_view.find_text_part('Unable to load page')
        browsing_view.cross_icon.click()
        if home_view.element_by_text('Recent').is_element_displayed():
            self.driver.fail('Browser entity is shown for an invalid link')

    @marks.testrail_id(6210)
    @marks.high
    def test_open_blocked_site(self):
        home_view = SignInView(self.driver).create_user()
        daap_view = home_view.dapp_tab_button.click()
        daap_view.open_url('https://www.cryptokitties.domainname').find_text_part('This site is blocked')


    @marks.testrail_id(6300)
    @marks.medium
    def test_webview_security(self):
        home_view = SignInView(self.driver).create_user()
        daap_view = home_view.dapp_tab_button.click()

        browsing_view = daap_view.open_url('https://simpledapp.status.im/webviewtest/url-spoof-ssl.html')
        browsing_view.url_edit_box_lock_icon.click()
        if not browsing_view.element_by_text_part(connection_not_secure_text).is_element_displayed():
            self.errors.append("Broken certificate displayed as secure connection \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/webviewtest.html')
        browsing_view.element_by_text_part('204').click()
        if browsing_view.element_by_text_part('google.com').is_element_displayed():
            self.errors.append("URL changed on attempt to redirect to no-content page \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/url-blank.html')
        if daap_view.edit_url_editbox.text == '':
            self.errors.append("Blank URL value. Must show the actual URL \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/port-timeout.html')
        # wait up  ~2.5 mins for port time out
        if daap_view.find_text_part('example.com', 150):
            self.errors.append("URL spoof due to port timeout \n")

        self.errors.verify_no_errors()

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
        dapp_view.remove_browser_entry_long_press('status', clear_all=True)
        home_view.relogin()
        home_view.dapp_tab_button.click()
        if not dapp_view.element_by_text('Browser history will appear here').is_element_displayed():
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
    @marks.critical
    def test_back_forward_buttons_browsing_website(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        daap_view = home.dapp_tab_button.click()
        browsing_view = daap_view.open_url('dap.ps')
        browsing_view.wait_for_element_starts_with_text('View all', 30)
        browsing_view.element_by_text_part('View all', 'button').click()
        if browsing_view.element_by_text_part('View all').is_element_displayed(20):
            self.driver.fail("Failed to access Categories using ''View all'")
        browsing_view.browser_previous_page_button.click()
        browsing_view.find_text_part('Categories', 15)
        browsing_view.browser_next_page_button.click()
        browsing_view.find_text_part('Exchanges', 15)
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

    @marks.testrail_id(5456)
    @marks.medium
    def test_can_access_images_by_link(self):
        urls = {
            'https://cdn.dribbble.com/users/45534/screenshots/3142450/logo_dribbble.png':
                'url_1.png',
            'https://thebitcoinpub-91d3.kxcdn.com/uploads/default/original/2X/d/db97611b41a96cb7642b06636b82c0800678b140.jpg':
                'url_2.png',
            'https://steemitimages.com/DQmYEjeBuAKVRa3b3ZqwLicSHaPUm7WFtQqohGaZdA9ghjx/images%20(4).jpeg':
                'url_3.png'
        }
        sign_in_view = SignInView(self.driver)
        home_view = sign_in_view.create_user()
        dapp_view = home_view.dapp_tab_button.click()
        for url in urls:
            self.driver.set_clipboard_text(url)
            dapp_view.enter_url_editbox.click()
            dapp_view.paste_text()
            dapp_view.confirm()
            dapp_view.progress_bar.wait_for_invisibility_of_element(20)
            if not dapp_view.web_page.is_element_image_equals_template(urls[url]):
                self.driver.fail('Web page does not match expected template %s' % urls[url])
            dapp_view.cross_icon.click()
