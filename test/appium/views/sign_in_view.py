from selenium.common.exceptions import NoSuchElementException

from tests import common_password
from views.base_element import Button, EditBox, Text
from views.base_view import BaseView


class LogInButton(Button):
    def __init__(self, driver):
        super().__init__(driver, accessibility_id="login-button")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class UserProfileElement(Button):
    def __init__(self, driver, username: str = None, index: int = None):
        self.username = username
        self.index = index
        if username:
            super().__init__(
                driver,
                xpath="//*[@text='%s']//ancestor::android.view.ViewGroup[@content-desc='profile-card']" % self.username)
        elif index:
            super().__init__(driver, xpath="(//*[@content-desc='profile-card'])[%s]" % self.index)
        else:
            raise AttributeError("Either username or profile index should be defined")

    def open_user_options(self):
        Button(self.driver, xpath='%s//*[@content-desc="profile-card-options"]' % self.locator).click()


class SignInView(BaseView):

    def __init__(self, driver):
        super().__init__(driver)
        self.driver = driver

        # intro screen
        self.explore_new_status_button = Button(self.driver, accessibility_id="explore-new-status")
        self.create_profile_button = Button(self.driver, accessibility_id='new-to-status-button')
        self.log_in_button = Button(self.driver, accessibility_id='log-in')
        self.maybe_later_button = Button(self.driver, accessibility_id="maybe-later-button")
        self.log_in_by_syncing_button = Button(self.driver, accessibility_id="log-in-by-syncing-icon-card")
        self.enter_sync_code_button = Button(self.driver, accessibility_id="Enter sync code")
        self.enter_sync_code_input = EditBox(self.driver, accessibility_id="enter-sync-code-input")
        self.progress_screen_title = Text(self.driver, accessibility_id="progress-screen-title")
        self.try_seed_phrase_button = Button(self.driver, accessibility_id="try-seed-phrase-button")

        # migrate multiaccount
        self.options_button = Button(self.driver, xpath="//androidx.appcompat.widget.LinearLayoutCompat")

        # New onboarding
        self.start_fresh_lets_go_button = Button(self.driver, accessibility_id="start-fresh-main-card")
        self.profile_title_input = EditBox(self.driver, accessibility_id="profile-title-input")
        self.profile_confirm_password_button = Button(self.driver, accessibility_id="Confirm password")
        self.use_recovery_phrase_button = Button(self.driver, accessibility_id="use-a-recovery-phrase-icon-card")
        self.passphrase_edit_box = EditBox(self.driver, accessibility_id="passphrase-input")
        self.show_profiles_button = Button(self.driver, accessibility_id="show-profiles")
        self.plus_profiles_button = Button(self.driver, accessibility_id="show-new-account-options")
        self.create_new_profile_button = Button(self.driver, accessibility_id="create-new-profile")
        self.sync_or_recover_new_profile_button = Button(self.driver, accessibility_id="multi-profile")
        self.remove_profile_button = Button(self.driver, accessibility_id="remove-profile")

    def set_password(self, password: str):
        input_elements = self.password_input.wait_for_elements()
        input_elements[0].send_keys(password)
        input_elements[1].click()
        input_elements[1].send_keys(password)
        self.profile_confirm_password_button.click()

    def create_user(self, password=common_password, first_user=True, enable_notifications=False):
        self.driver.info("## Creating new multiaccount with password:'%s'" % password, device=False)
        if first_user:
            self.create_profile_button.click()
            self.maybe_later_button.wait_and_click()
        else:
            if self.show_profiles_button.is_element_displayed(20):
                self.show_profiles_button.click()
            self.plus_profiles_button.click()
            self.create_new_profile_button.click()
        self.start_fresh_lets_go_button.click()
        self.set_password(password)
        self.chats_tab.wait_for_visibility_of_element(30)
        self.driver.info("## New multiaccount is created successfully!", device=False)
        home_view = self.get_home_view()
        if enable_notifications:
            profile_view = home_view.profile_button.click()
            profile_view.switch_push_notifications()
            profile_view.click_system_back_button(times=2)
        return home_view

    def recover_access(self, passphrase: str, password: str = common_password, enable_notifications=False,
                       second_user=False, after_sync_code=False):
        self.driver.info("## Recover access (password:%s)" % password, device=False)

        if not after_sync_code:
            if not second_user:
                self.create_profile_button.click()
                self.maybe_later_button.wait_and_click()
            else:
                self.plus_profiles_button.click()
                self.create_new_profile_button.click()
            self.use_recovery_phrase_button.click()
        self.passphrase_edit_box.send_keys(passphrase)
        self.continue_button.click()
        self.set_password(password)
        self.chats_tab.wait_for_visibility_of_element(30)
        self.driver.info("## Multiaccount is recovered successfully!", device=False)
        home_view = self.get_home_view()
        if enable_notifications:
            profile_view = home_view.profile_button.click()
            profile_view.switch_push_notifications()
        return home_view

    def sync_profile(self, sync_code: str, first_user: bool = True):
        if first_user:
            self.log_in_button.click()
            self.maybe_later_button.click()
        else:
            self.plus_profiles_button.click()
            self.sync_or_recover_new_profile_button.click()
        self.log_in_by_syncing_button.click()
        for checkbox in self.checkbox_button.find_elements():
            checkbox.click()
        self.continue_button.click()
        self.enter_sync_code_button.click()
        self.enter_sync_code_input.send_keys(sync_code)
        self.confirm_button.click()

    def sign_in(self, user_name, password=common_password):
        self.driver.info("## Sign in (password: %s)" % password, device=False)
        self.get_user_profile_by_name(user_name).click()
        self.password_input.wait_for_visibility_of_element(10)
        self.password_input.send_keys(password)
        self.login_button.click()
        self.driver.info("## Signed in successfully!", device=False)
        return self.get_home_view()

    def get_user_profile_by_name(self, username: str):
        self.driver.info("Getting username card by '%s'" % username)
        expected_element = UserProfileElement(self.driver, username=username)
        if expected_element.is_element_displayed(10):
            return expected_element
        else:
            raise NoSuchElementException(msg="User %s is not found!" % username)
