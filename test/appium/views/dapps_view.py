from views.base_element import EditBox, BaseElement
from views.base_view import BaseView


class DappsView(BaseView):
    def __init__(self, driver):
        super(DappsView, self).__init__(driver)

        self.enter_url_editbox = EditBox(self.driver, accessibility_id="dapp-url-input")
        self.web_page = BaseElement(self.driver, xpath="(//android.webkit.WebView)[1]")

    def open_url(self, url):
        self.driver.info("Open url '%s'" % url)
        from views.web_views.base_web_view import BaseWebView
        web_view = BaseWebView(self.driver)
        if not self.enter_url_editbox.is_element_displayed():
            web_view.open_tabs_button.click_if_shown()
            web_view.open_new_tab_plus_button.click_if_shown()
            self.enter_url_editbox.wait_for_visibility_of_element(20)
        self.enter_url_editbox.click()
        self.enter_url_editbox.send_keys(url)
        self.confirm()
        from views.web_views.base_web_view import BaseWebView
        BaseWebView(self.driver).wait_for_d_aap_to_load()
        return self.get_base_web_view()

