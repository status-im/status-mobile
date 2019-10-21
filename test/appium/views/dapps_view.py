from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView
from views.home_view import ChatElement


class DiscoverDappsButton(BaseButton):
    def __init__(self, driver):
        super(DiscoverDappsButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Discover ÐApps')

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)

    def click(self):
        from views.web_views.base_web_view import BrowserRefreshPageButton
        self.click_until_presence_of_element(BrowserRefreshPageButton(self.driver))
        return self.navigate()


class EnterUrlEditbox(BaseEditBox):
    def __init__(self, driver):
        super(EnterUrlEditbox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('dapp-url-input')


class BrowserEntry(ChatElement):
    def __init__(self, driver, name):
        super(BrowserEntry, self).__init__(driver, name)
        self.locator = self.locator.text_part_selector(name)


class EnsName(BaseEditBox):
    def __init__(self, driver):
        super(EnsName, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//android.widget.EditText')


class EnsCheckName(BaseButton):
        def __init__(self, driver):
            super(EnsCheckName, self).__init__(driver)
            self.locator = self.Locator.xpath_selector('//android.widget.EditText//following-sibling::android.view.ViewGroup[1]')


class RemoveDappButton(BaseButton):
    def __init__(self, driver):
        super(RemoveDappButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('remove-dapp-from-list')


class ClearAllDappButton(BaseButton):
    def __init__(self, driver):
        super(ClearAllDappButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('clear-all-dapps')

class SelectAccountButton(BaseButton):
    def __init__(self, driver):
        super(SelectAccountButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('select-account')

class SelectAccountRadioButton(BaseButton):
    def __init__(self, driver, account_name):
        super(SelectAccountRadioButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/../../android.view.ViewGroup/android.view.ViewGroup[2]" % account_name)


class AlwaysAllowRadioButton(BaseButton):
    def __init__(self, driver):
        super(AlwaysAllowRadioButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Always allow']/../android.view.ViewGroup")


class CrossCloseWeb3PermissionButton(BaseButton):
    def __init__(self, driver):
        super(CrossCloseWeb3PermissionButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            '//*[contains(@text,"ÐApps can access")]/../android.view.ViewGroup[1]/android.view.ViewGroup')

class DappsView(BaseView):

    def __init__(self, driver):
        super(DappsView, self).__init__(driver)

        self.enter_url_editbox = EnterUrlEditbox(self.driver)
        self.discover_dapps_button = DiscoverDappsButton(self.driver)

        #ens dapp
        self.ens_name = EnsName(self.driver)
        self.check_ens_name = EnsCheckName(self.driver)

        #options on long press
        self.remove_d_app_button = RemoveDappButton(self.driver)
        self.clear_all_d_app_button = ClearAllDappButton(self.driver)

        #select account
        self.select_account_button = SelectAccountButton(self.driver)
        self.select_account_radio_button = SelectAccountRadioButton(self.driver,
                                                                    account_name='Status account')
        #permissions window
        self.always_allow_radio_button = AlwaysAllowRadioButton(self.driver)
        self.close_web3_permissions_window_button = CrossCloseWeb3PermissionButton(self.driver)


    def open_url(self, url):
        self.enter_url_editbox.click()
        self.enter_url_editbox.send_keys(url)
        self.confirm()
        return self.get_base_web_view()

    def get_browser_entry(self, name):
        return BrowserEntry(self.driver, name)

    def remove_browser_entry_long_press(self, name, clear_all=False):
        entry = self.get_browser_entry(name)
        entry.scroll_to_element()
        entry.long_press_element()
        self.clear_all_d_app_button if clear_all else self.remove_d_app_button.click()
        return entry

    def select_account_by_name(self, account_name='Status account'):
        return SelectAccountRadioButton(self.driver, account_name)
