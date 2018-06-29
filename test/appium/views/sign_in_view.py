from tests import get_current_time, common_password
from views.base_element import BaseButton, BaseEditBox
from views.base_view import BaseView


class AccountButton(BaseButton):

    def __init__(self, driver):
        super(AccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[contains(@text,'0x')]")


class PasswordInput(BaseEditBox):

    def __init__(self, driver):
        super(PasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Password']"
                                                   "/following-sibling::android.view.ViewGroup/android.widget.EditText")


class SignInButton(BaseButton):

    def __init__(self, driver):
        super(SignInButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Sign in' or @text='SIGN IN']")

    def navigate(self):
        from views.home_view import HomeView
        return HomeView(self.driver)


class RecoverAccessButton(BaseButton):

    def __init__(self, driver):
        super(RecoverAccessButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Recover access']")

    def navigate(self):
        from views.recover_access_view import RecoverAccessView
        return RecoverAccessView(self.driver)


class CreateAccountButton(BaseButton):
    def __init__(self, driver):
        super(CreateAccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector(
            "//android.widget.TextView[@text='CREATE ACCOUNT' or @text='CREATE NEW ACCOUNT']")


class IHaveAccountButton(RecoverAccessButton):
    def __init__(self, driver):
        super(IHaveAccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='I ALREADY HAVE AN ACCOUNT']")


class AddExistingAccountButton(RecoverAccessButton):
    def __init__(self, driver):
        super(AddExistingAccountButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='ADD EXISTING ACCOUNT']")


class ConfirmPasswordInput(BaseEditBox):
    def __init__(self, driver):
        super(ConfirmPasswordInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Confirm']")


class NameInput(BaseEditBox):
    def __init__(self, driver):
        super(NameInput, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.EditText")


class LearnMoreLink(BaseButton):

    def __init__(self, driver):
        super(LearnMoreLink, self).__init__(driver)
        self.locator = self.Locator.text_selector('Learn more about what we collect')


class ShareDataButton(BaseButton):

    def __init__(self, driver):
        super(ShareDataButton, self).__init__(driver)
        self.locator = self.Locator.text_selector('Share data')


class DonNotShareButton(BaseButton):

    def __init__(self, driver):
        super(DonNotShareButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="NO, I DON%sT WANT TO SHARE" '
                                                   'or @text="Do not share"]' % "'")


class SignInView(BaseView):

    def __init__(self, driver):
        super(SignInView, self).__init__(driver)
        self.driver = driver

        self.account_button = AccountButton(self.driver)
        self.password_input = PasswordInput(self.driver)
        self.sign_in_button = SignInButton(self.driver)
        self.recover_access_button = RecoverAccessButton(self.driver)

        # new design
        self.create_account_button = CreateAccountButton(self.driver)
        self.i_have_account_button = IHaveAccountButton(self.driver)
        self.add_existing_account_button = AddExistingAccountButton(self.driver)
        self.confirm_password_input = ConfirmPasswordInput(self.driver)
        self.name_input = NameInput(self.driver)
        self.learn_more_link = LearnMoreLink(self.driver)
        self.share_data_button = ShareDataButton(self.driver)
        self.do_not_share_button = DonNotShareButton(self.driver)

    def create_user(self, username: str = '', password=common_password):
        self.create_account_button.click()
        self.password_input.set_value(password)
        self.next_button.click()
        self.confirm_password_input.set_value(password)
        self.next_button.click()

        self.element_by_text_part('Display name').wait_for_element(30)
        username = username if username else 'user_%s' % get_current_time()
        self.name_input.send_keys(username)

        self.next_button.click()
        self.do_not_share_button.wait_for_visibility_of_element(10)
        self.do_not_share_button.click_until_presence_of_element(self.home_button)
        return self.get_home_view()

    def recover_access(self, passphrase, password):
        recover_access_view = self.i_have_account_button.click()
        recover_access_view.passphrase_input.click()
        recover_access_view.send_as_keyevent(passphrase)
        recover_access_view.password_input.click()
        recover_access_view.send_as_keyevent(password)
        recover_access_view.sign_in_button.click()
        self.do_not_share_button.wait_for_element(10)
        self.do_not_share_button.click_until_presence_of_element(self.home_button)
        return self.get_home_view()

    def open_status_test_dapp(self):
        profile_view = self.profile_button.click()
        profile_view.advanced_button.click()
        profile_view.debug_mode_toggle.click()
        home_view = profile_view.home_button.click()
        start_new_chat_view = home_view.plus_button.click()
        start_new_chat_view.open_d_app_button.click()
        start_new_chat_view.status_test_dapp_button.scroll_to_element()
        status_test_daap = start_new_chat_view.status_test_dapp_button.click()
        start_new_chat_view.open_button.click()
        return status_test_daap

    def sign_in(self, password=common_password):
        self.password_input.set_value(password)
        return self.sign_in_button.click()

    def click_account_by_position(self, position: int):
        self.account_button.find_elements()[position].click()
