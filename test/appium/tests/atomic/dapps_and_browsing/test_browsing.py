from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestBrowsing(SingleDeviceTestCase):

    # TODO: waiting mode (rechecked 23.11.21, valid)
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

