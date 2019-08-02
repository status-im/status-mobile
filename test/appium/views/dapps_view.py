from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView
from views.home_view import ChatElement


class OpenDAppButton(BaseButton):
    def __init__(self, driver):
        super(OpenDAppButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-dapp-button')


class OpenButton(BaseButton):
    def __init__(self, driver):
        super(OpenButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-dapp-button')

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class EnterUrlEditbox(BaseEditBox):
    def __init__(self, driver):
        super(EnterUrlEditbox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('dapp-url-input')


class BrowserEntry(ChatElement):
    def __init__(self, driver, name):
        super(BrowserEntry, self).__init__(driver, name)
        self.locator = self.Locator.xpath_selector('//*[@text="%s"]/..' % name)

class EnsName(BaseEditBox):
    def __init__(self, driver):
        super(EnsName, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.EditText')

class EnsCheckName(BaseButton):
        def __init__(self, driver):
            super(EnsCheckName, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//android.widget.EditText//following-sibling::android.view.ViewGroup[1]')

class DappsView(BaseView):

    def __init__(self, driver):
        super(DappsView, self).__init__(driver)

        self.open_d_app_button = OpenDAppButton(self.driver)
        self.open_button = OpenButton(self.driver)
        self.enter_url_editbox = EnterUrlEditbox(self.driver)

        #ens dapp
        self.ens_name = EnsName(self.driver)
        self.check_ens_name = EnsCheckName(self.driver)

    def open_url(self, url):
        self.enter_url_editbox.click()
        self.enter_url_editbox.send_keys(url)
        self.confirm()
        return self.get_base_web_view()

    def get_browser_entry(self, name):
        return BrowserEntry(self.driver, name)
