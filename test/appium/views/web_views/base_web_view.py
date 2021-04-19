import time

from views.base_element import EditBox, Button, BaseElement
from views.base_view import BaseView

class BaseWebView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)

        self.progress_bar_icon = Button(self.driver, xpath="//android.widget.ProgressBar")
        self.url_edit_box_lock_icon = Button(self.driver, xpath="'(//android.view.ViewGroup[@content-desc='icon'])[2]")
        self.policy_summary = Button(self.driver, xpath="//*[@content-desc='Policy summary'] | //*[@text='Policy summary']")
        self.browser_previous_page_button = Button(self.driver, accessibility_id="previous-page-button")
        self.browser_next_page_button = Button(self.driver, accessibility_id="next-page-button")

        self.web_view_browser = Button(self.driver, xpath="//*[contains(@text,'WebView Browser Tester')]")
        self.always_button = Button(self.driver, xpath="//*[contains(@text,'ALWAYS')]")
        self.browser_refresh_page_button = Button(self.driver, accessibility_id="refresh-page-button")
        self.share_url_button = Button(self.driver, accessibility_id="share")
        self.go_back_button = Button(self.driver, translation_id="browsing-site-blocked-go-back")
        self.options_button = Button(self.driver, accessibility_id="browser-options")
        self.connect_account_button = Button(self.driver, accessibility_id="connect-account")
        self.connected_account_button = Button(self.driver, accessibility_id="connected-account")
        self.open_chat_from_dapp_button = Button(self.driver, accessibility_id="open-chat")
        self.new_tab_button = Button(self.driver, accessibility_id="new-tab")
        self.continue_anyway_button = Button(self.driver, translation_id="continue-anyway")
        self.open_tabs_button = Button(self.driver, accessibility_id="browser-open-tabs")
        self.close_all_button = Button(self.driver, accessibility_id="close-all")
        self.camera_image_in_dapp = BaseElement(self.driver, class_name="android.widget.Image")

        # bookmarks management
        self.add_remove_favorites_button = Button(self.driver, accessibility_id="add-remove-fav")
        self.bookmark_name_input = EditBox(self.driver, accessibility_id="bookmark-input")
        self.save_bookmark_button = Button(self.driver, accessibility_id="save-bookmark")

    def wait_for_d_aap_to_load(self, wait_time=35):
        self.driver.info("**Waiting %ss for dapp to load**" % wait_time)
        counter = 0
        while self.progress_bar_icon.is_element_present(5):
            time.sleep(1)
            counter += 1
            if counter > wait_time:
                self.driver.fail("Page is not loaded during %s seconds" % wait_time)

    def open_in_webview(self):
        self.driver.info("**Opening in webview**")
        if self.web_view_browser.is_element_displayed():
            self.web_view_browser.click()
        if self.always_button.is_element_displayed():
            self.always_button.click()

    def remove_tab(self, name='', clear_all=False):
        self.open_tabs_button.click()
        if clear_all:
            self.driver.info("**Closing all tabs**")
            self.close_all_button.click()
        else:
            self.driver.info("**Removing '%s' from recent websites**")
            close_button = Button(self.driver, xpath="//*[contains(@text, '%s')]/../../../../*[@content-desc='empty-tab']"% name)
            close_button.scroll_to_element()
            close_button.click()

    def edit_bookmark_name(self, name):
        self.driver.info("**Editing bookmark name to '%s'**" % name)
        self.bookmark_name_input.clear()
        self.bookmark_name_input.send_keys(name)
        self.save_bookmark_button.click()

    def add_to_bookmarks(self, name=''):
        self.driver.info("**Adding '%s' to bookmarks**" % name)
        self.options_button.click()
        self.add_remove_favorites_button.click()
        if name:
            self.edit_bookmark_name(name)
            bookmark_name = name
        else:
            bookmark_name = self.bookmark_name_input.text
            self.save_bookmark_button.click()
        return bookmark_name

