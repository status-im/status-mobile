from views.base_element import Button, EditBox, BaseElement
from views.base_view import BaseView, CheckBox, Text
from views.home_view import ChatElement


class DiscoverDappsButton(Button):
    def __init__(self, driver):
        super().__init__(driver, translation_id="open-dapp-store")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)

    def click(self):
        from views.web_views.base_web_view import BaseWebView
        self.click_until_presence_of_element(BaseWebView(self.driver).browser_refresh_page_button)
        return self.navigate()


class EditUrlEditbox(EditBox):
    def __init__(self, driver):
        super().__init__(driver, xpath="(//android.widget.TextView)[1]")

    @property
    def text(self):
        return self.find_element().text


class BrowserEntry(ChatElement):
    def __init__(self, driver, name):
        super().__init__(driver, name)
        self.locator = "//*[contains(@content-desc,'%s')]" % name


class DappsView(BaseView):
    def __init__(self, driver):
        super(DappsView, self).__init__(driver)

        self.enter_url_editbox = EditBox(self.driver, accessibility_id="dapp-url-input")
        self.edit_url_editbox = EditUrlEditbox(self.driver)
        self.discover_dapps_button = DiscoverDappsButton(self.driver)
        self.web_page = BaseElement(self.driver, xpath="(//android.webkit.WebView)[1]")

        # Ens dapp
        self.get_started_ens = Button(self.driver, translation_id="get-started")
        self.ens_name_input = EditBox(self.driver, xpath="//android.widget.EditText")
        self.check_ens_name = Button(self.driver, xpath="(//android.widget.ImageView[@content-desc='icon'])[2]/../..")
        self.agree_on_terms_ens = CheckBox(self.driver, accessibility_id=":checkbox-off")
        self.register_ens_button = Button(self.driver, translation_id="ens-register")
        self.ens_got_it = Button(self.driver, translation_id="ens-got-it")
        self.registration_in_progress = Text(self.driver, translation_id="ens-registration-in-progress")

        # Options on long press
        self.delete_bookmark_button = Button(self.driver, accessibility_id="delete-bookmark")
        self.open_in_new_tab_button = Button(self.driver, accessibility_id="open-in-new-tab")
        self.edit_bookmark_button = Button(self.driver, accessibility_id="edit-bookmark")

        # Select account
        self.select_account_button = Button(self.driver, accessibility_id="select-account")

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

    def get_browser_entry(self, name):
        return BrowserEntry(self.driver, name)

    def browser_entry_long_press(self, name):
        self.driver.info("Long press on '%s' browser entry" % name)
        entry = self.get_browser_entry(name)
        entry.scroll_to_element()
        entry.long_press_element()
        return entry

    def select_account_by_name(self, account_name=''):
        account_name = self.status_account_name if not account_name else account_name
        self.driver.info("Select account by '%s'" % account_name)
        return Button(self.driver,
                      xpath="//*[@text='%s']/../../android.view.ViewGroup/android.view.ViewGroup[2]" % account_name)

    def set_primary_ens_username(self, ens_name):
        self.driver.info("Set '%s' as primary ENS name" % ens_name)
        return Button(self.driver, accessibility_id="not-primary-username")
