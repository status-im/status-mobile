from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestBrowsing(SingleDeviceTestCase):

    @marks.testrail_id(5395)
    @marks.high
    def test_back_forward_refresh_navigation_history_kept_after_relogin(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        dapp = home.dapp_tab_button.click()
        ru_url = 'https://ru.m.wikipedia.org'
        browsing = dapp.open_url(ru_url)
        browsing.element_by_text_part('Добро пожаловать').wait_for_element(20)

        browsing.just_fyi("Check next page")
        browsing.just_fyi('Navigate to next page and back')
        browsing.element_by_text_part('свободную энциклопедию').click()
        browsing.element_by_text_part('Свободный контент')
        browsing.browser_previous_page_button.click()
        browsing.wait_for_element_starts_with_text('свободную энциклопедию')

        browsing.just_fyi('Relogin and check that tap on "Next" navigates to next page')
        profile = home.profile_button.click()
        profile.switch_network()
        home.dapp_tab_button.click()
        browsing.open_tabs_button.click()
        dapp.element_by_text_part(ru_url).click()
        browsing.wait_for_element_starts_with_text('свободную энциклопедию')
        browsing.browser_next_page_button.click()
        browsing.element_by_text_part('Свободный контент').wait_for_element(30)

        browsing.just_fyi("Check refresh button")
        browsing.dapp_tab_button.double_click()
        url = 'app.uniswap.org'
        element_on_start_page = dapp.element_by_text('ETH')
        web_page = dapp.open_url(url)
        dapp.allow_button.click()
        element_on_start_page.click()

        # when bottom sheet is opened, elements by text couldn't be found
        element_on_start_page.wait_for_invisibility_of_element(20)
        web_page.browser_refresh_page_button.click()

        if not element_on_start_page.is_element_displayed(30):
            self.driver.fail("Page failed to be refreshed")


    @marks.testrail_id(6210)
    @marks.high
    def test_open_blocked_secure_not_secure_inlalid_offline_urls(self):
        home = SignInView(self.driver).create_user()
        dapp = home.dapp_tab_button.click()
        for url in ('metamask.site', 'https://www.cryptokitties.domainname'):
            dapp.just_fyi('Checking blocked website %s' % url)
            dapp_detail = dapp.open_url(url)
            dapp_detail.element_by_translation_id('browsing-site-blocked-title')
            if dapp_detail.browser_refresh_page_button.is_element_displayed():
                self.errors.append("Refresh button is present in blocked site")
            dapp_detail.go_back_button.click()
            dapp_detail.open_tabs_button.click()
            dapp.element_by_text("Browser").click()
            dapp_detail.continue_anyway_button.click()
            if dapp_detail.element_by_text('This site is blocked').is_element_displayed():
                self.errors.append("Failed to open Dapp after 'Continue anyway' tapped for %s" % url)
            home.dapp_tab_button.click()

        dapp.just_fyi('Checking connection is not secure warning')
        web_page = dapp.open_url('http://www.dvwa.co.uk')
        web_page.url_edit_box_lock_icon.click_until_presence_of_element(web_page.element_by_translation_id("browser-not-secure"))
        home.dapp_tab_button.click()

        for url in ('https://www.bbc.com', 'https://instant.airswap.io'):
            dapp.just_fyi('Checking connection is secure for %s' % url)
            web_page = dapp.open_url(url)
            web_page.wait_for_d_aap_to_load()
            web_page.url_edit_box_lock_icon.click_until_presence_of_element(web_page.element_by_translation_id("browser-secure"))
            home.dapp_tab_button.click()

        dapp.just_fyi('Checking opening invalid link')
        home.dapp_tab_button.double_click()
        browsing_view = dapp.open_url('invalid.takoe')
        browsing_view.element_by_translation_id("web-view-error").wait_for_element(20)
        browsing_view.dapp_tab_button.double_click()

        dapp.just_fyi('Checking offline state')
        home.toggle_airplane_mode()
        browsing_view = dapp.open_url('status.im')
        offline_texts = ['Unable to load page', 'ERR_INTERNET_DISCONNECTED']
        for text in offline_texts:
            browsing_view.element_by_text_part(text).wait_for_element(15)
        home.toggle_airplane_mode()
        browsing_view.browser_refresh_page_button.click_until_presence_of_element(
            browsing_view.element_by_text_part('An Open Source Community'))

        self.errors.verify_no_errors()

    @marks.testrail_id(5390)
    @marks.high
    def test_delete_close_all_tabs(self):
        home_view = SignInView(self.driver).create_user()
        dapp_view = home_view.dapp_tab_button.click()
        urls = {
            'google.com': 'Google',
            'status.im': 'Status - Private',
            'bbc.com' : 'bbc.com'
        }
        for url in urls:
            browsing_view = dapp_view.open_url(url)
            browsing_view.dapp_tab_button.double_click()
        home_view.just_fyi('Close one tab, relogin and check that it is not reappearing')
        browsing_view.remove_tab(name=urls['bbc.com'])
        home_view.relogin()
        home_view.dapp_tab_button.click()
        browsing_view.open_tabs_button.click()
        if browsing_view.element_by_text_part(urls['bbc.com']).is_element_displayed():
             self.errors.append('Closed tab is present after re-login')

        home_view.just_fyi('Close all tabs via "Close all", relogin and check that it is not reappearing')
        browsing_view.close_all_button.click()
        home_view.relogin()
        home_view.dapp_tab_button.click()
        browsing_view.open_tabs_button.click()
        for url in urls:
            if browsing_view.element_by_text_part(urls[url]).is_element_displayed():
                self.errors.append('Closed tab %s present after re-login after "Close all"' % url)

        self.errors.verify_no_errors()

    @marks.testrail_id(6633)
    @marks.high
    def test_browser_managing_bookmarks(self):
        home_view = SignInView(self.driver).create_user()
        dapp_view = home_view.dapp_tab_button.click()

        home_view.just_fyi('Add some url to bookmarks with default name')
        browsing_view = dapp_view.open_url('status.im')
        default_bookmark_name = browsing_view.add_to_bookmarks()
        browsing_view.browser_previous_page_button.click()
        if not browsing_view.element_by_text(default_bookmark_name).is_element_displayed():
            self.errors.append("Bookmark with default name is not added!")

        home_view.just_fyi('Add some url to bookmarks with custom name')
        custom_name = 'Custom BBC'
        dapp_view.open_url('bbc.com')
        browsing_view.add_to_bookmarks(custom_name)
        browsing_view.dapp_tab_button.click()
        if not browsing_view.element_by_text(custom_name).is_element_displayed():
            self.driver.fail("Bookmark with custom name is not added!")

        home_view.just_fyi('Check deleting bookmark on long tap and that it is deleted after relogin')
        dapp_view.browser_entry_long_press(custom_name)
        dapp_view.delete_bookmark_button.click()
        if browsing_view.element_by_text(custom_name).is_element_displayed():
            self.errors.append("Bookmark with custom name is not deleted!")
        profile_view = dapp_view.profile_button.click()
        profile_view.relogin()
        profile_view.dapp_tab_button.click()
        if browsing_view.element_by_text(custom_name).is_element_displayed():
            self.errors.append("Bookmark with custom name is reappeared after relogin!")

        home_view.just_fyi('Check "Edit bookmark" and "Open in new tab"')
        edited_name = 'My Fav Status'
        dapp_view.browser_entry_long_press(default_bookmark_name)
        dapp_view.edit_bookmark_button.click()
        browsing_view.edit_bookmark_name(edited_name)
        if not browsing_view.element_by_text(edited_name).is_element_displayed():
            self.driver.fail("Edited bookmark name is not shown!")
        dapp_view.browser_entry_long_press(edited_name)
        dapp_view.open_in_new_tab_button.click()
        browsing_view.options_button.click()
        if not browsing_view.element_by_translation_id('remove-favourite').is_element_displayed():
            self.errors.append("Remove favourite is not shown on added bookmark!")
        self.errors.verify_no_errors()

    @marks.testrail_id(5424)
    @marks.medium
    def test_open_url_with_non_english_text_connect_revoke_wallet_new_tab_open_chat_options(self):
        home = SignInView(self.driver).create_user()
        web_view = home.dapp_tab_button.click()
        browsing = web_view.open_url('www.wikipedia.org')
        wiki_texts = ['Español', '日本語', 'Français', '中文', 'Português']
        for wiki_text in wiki_texts:
            if not browsing.element_by_text_part(wiki_text).is_element_displayed(15):
                self.errors.append("%s is not shown" % wiki_text)

        web_view.just_fyi("Check that can connect wallet and revoke access")
        browsing.options_button.click()
        browsing.connect_account_button.click()
        browsing.allow_button.click()
        browsing.options_button.click()
        if not browsing.connected_account_button.is_element_displayed():
            self.driver.fail("Account is not connected")
        browsing.click_system_back_button()
        profile = browsing.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        if not profile.element_by_text('wikipedia.org').is_element_displayed():
            self.errors.append("Permissions are not granted")
        profile.dapp_tab_button.click(desired_element_text = wiki_texts[0])
        browsing.options_button.click()
        browsing.connected_account_button.click()
        browsing.element_by_translation_id("revoke-access").click()
        browsing.options_button.click()
        if not browsing.connect_account_button.is_element_displayed():
            self.errors.append("Permission for account is not removed if using 'Revoke access' from dapp view")
        browsing.click_system_back_button()
        browsing.profile_button.click(desired_element_text = 'DApp permissions')
        if profile.element_by_text('wikipedia.org').is_element_displayed():
            self.errors.append("Permissions are not revoked")

        web_view.just_fyi("Check that can open chat view and send some message")
        profile.dapp_tab_button.click(desired_element_text = wiki_texts[0])
        browsing.options_button.click()
        browsing.open_chat_from_dapp_button.click()
        public_chat = browsing.get_chat_view()
        if not public_chat.element_by_text('#wikipedia-org').is_element_displayed():
            self.driver.fail("No redirect to public chat")
        message = public_chat.get_random_message()
        public_chat.send_message(message)
        public_chat.dapp_tab_button.click(desired_element_text = wiki_texts[0])
        browsing.options_button.click()
        browsing.open_chat_from_dapp_button.click()
        if not public_chat.chat_element_by_text(message).is_element_displayed():
            self.errors.append("Messages are not shown if open dapp chat from view")

        web_view.just_fyi("Check that can open new tab using 'New tab' from bottom sheet")
        profile.dapp_tab_button.click(desired_element_text=wiki_texts[0])
        browsing.options_button.click()
        browsing.new_tab_button.click()
        if not browsing.element_by_translation_id("open-dapp-store").is_element_displayed():
            self.errors.append("Was not redirected to home dapp store view using New tab")

        self.errors.verify_no_errors()

    #TODO: waiting mode
    @marks.testrail_id(6300)
    @marks.skip
    @marks.medium
    def test_webview_security(self):
        home_view = SignInView(self.driver).create_user()
        daap_view = home_view.dapp_tab_button.click()

        browsing_view = daap_view.open_url('https://simpledapp.status.im/webviewtest/url-spoof-ssl.html')
        browsing_view.url_edit_box_lock_icon.click()
        if not browsing_view.element_by_translation_id("browser-not-secure").is_element_displayed():
            self.errors.append("Broken certificate displayed as secure connection \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/webviewtest.html')
        browsing_view.element_by_text_part('204').click()
        if browsing_view.element_by_text_part('google.com').is_element_displayed():
            self.errors.append("URL changed on attempt to redirect to no-content page \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/webviewtest.html')
        browsing_view.element_by_text_part('XSS check').click()
        browsing_view.open_in_status_button.click()
        if browsing_view.element_by_text_part('simpledapp.status.im').is_element_displayed():
            self.errors.append("XSS attemp succedded \n")
            browsing_view.ok_button.click()

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/url-blank.html')
        if daap_view.edit_url_editbox.text == '':
            self.errors.append("Blank URL value. Must show the actual URL \n")

        browsing_view.cross_icon.click()
        daap_view.open_url('https://simpledapp.status.im/webviewtest/port-timeout.html')
        # wait up  ~2.5 mins for port time out
        if daap_view.element_by_text_part('example.com').is_element_displayed(150):
            self.errors.append("URL spoof due to port timeout \n")

        self.errors.verify_no_errors()

    @marks.testrail_id(5456)
    @marks.medium
    @marks.skip
    # Decicded to leave for manual testing, as there is no simple way of comparing images depending on phone brightness
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
            if not dapp_view.web_page.is_element_image_equals_template(urls[url]):
                self.errors.append('Web page does not match expected template %s' % urls[url])
            dapp_view.cross_icon.click()
        self.errors.verify_no_errors()
