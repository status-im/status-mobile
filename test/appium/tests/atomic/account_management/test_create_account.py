import random

from tests import marks, common_password
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView
from tests.users import basic_user


class TestCreateAccount(SingleDeviceTestCase):

    @marks.testrail_id(5356)
    @marks.critical
    def test_switch_users_and_add_new_account(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        public_key = home.get_public_key_and_username()
        profile = home.get_profile_view()
        profile.logout()
        if sign_in.ok_button.is_element_displayed():
            sign_in.ok_button.click()
        sign_in.your_keys_more_icon.click()
        sign_in.generate_new_key_button.click()
        sign_in.generate_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.confirm_your_password_input.set_value(common_password)
        sign_in.next_button.click()
        sign_in.maybe_later_button.click_until_presence_of_element(sign_in.lets_go_button)
        sign_in.lets_go_button.click()

        if sign_in.get_public_key_and_username() == public_key:
            self.driver.fail('New account was not created')

    @marks.testrail_id(5379)
    @marks.high
    def test_home_view(self):
        sign_in = SignInView(self.driver)
        sign_in.get_started_button.click()
        sign_in.generate_key_button.click()
        from views.sign_in_view import MultiAccountButton
        account_button = sign_in.get_multiaccount_by_position(position=random.randint(1, 4), element_class=MultiAccountButton)
        username = account_button.username.text
        account_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        sign_in.create_password_input.set_value(common_password)
        sign_in.confirm_your_password_input.set_value(common_password)
        sign_in.next_button.click()
        [element.wait_and_click(10) for element in (sign_in.maybe_later_button, sign_in.lets_go_button)]
        home = sign_in.get_home_view()
        texts = ["chat-and-transact", "follow-your-interests"]
        for text in texts:
            if not home.element_by_translation_id(text).is_element_displayed():
                self.errors.append("'%s' text is not shown" % self.get_translation_by_key(text))
        for chat in ('#status', '#crypto'):
            sign_in.element_by_text(chat).click()
            sign_in.back_button.click_until_presence_of_element(home.search_input)
        profile = home.profile_button.click()
        shown_username = profile.default_username_text.text
        if shown_username != username:
            self.errors.append("Default username '%s' doesn't match '%s'" % (shown_username, username))
        profile.home_button.click_until_presence_of_element(home.element_by_text('#status'))
        home.cross_icon_inside_welcome_screen_button.click()
        for chat in ('#status', '#crypto'):
            home.delete_chat_long_press(chat)
        if home.element_by_text(texts[0]).is_element_displayed():
            self.errors.append("'%s' text is shown, but welcome view was closed" % texts[0])
        home.relogin()
        if home.element_by_text(texts[0]).is_element_displayed():
            self.errors.append("'%s' text is shown after relogin, but welcome view was closed" % texts[0])
        if not home.element_by_translation_id("welcome-blank-message").is_element_displayed():
            self.errors.append("'%s' text is not shown after welcome view was closed" %  home.get_translation_by_key("welcome-blank-message"))

        self.errors.verify_no_errors()

    @marks.testrail_id(5460)
    @marks.medium
    def test_create_account_short_and_mismatch_password(self):
        sign_in = SignInView(self.driver)
        sign_in.get_started_button.click()
        sign_in.generate_key_button.click()
        sign_in.next_button.click()
        sign_in.next_button.click()
        cases = ['password is not confirmed', 'password is too short', "passwords don't match"]
        error = "Can create multiaccount when"

        sign_in.just_fyi('Checking case when %s' % cases[0])
        sign_in.create_password_input.send_keys('123456')
        sign_in.next_button.click()
        if sign_in.maybe_later_button.is_element_displayed(10):
            self.driver.fail('%s  %s' % (error, cases[0]))

        sign_in.just_fyi('Checking case when %s'% cases[1])
        sign_in.create_password_input.send_keys('123456')
        [field.send_keys('123456') for field in (sign_in.create_password_input, sign_in.confirm_your_password_input)]
        sign_in.confirm_your_password_input.delete_last_symbols(1)
        sign_in.create_password_input.delete_last_symbols(1)
        sign_in.next_button.click()
        if sign_in.maybe_later_button.is_element_displayed(10):
            self.driver.fail('%s  %s' % (error, cases[1]))

        sign_in.just_fyi("Checking case %s" % cases[2])
        sign_in.create_password_input.send_keys('1234565')
        sign_in.confirm_your_password_input.send_keys('1234567')
        if not sign_in.element_by_translation_id("password_error1").is_element_displayed():
            self.errors.append("'%s' is not shown" % sign_in.get_translation_by_key("password_error1"))
        sign_in.next_button.click()
        if sign_in.maybe_later_button.is_element_displayed(10):
            self.driver.fail('%s  %s' % (error, cases[2]))

        self.errors.verify_no_errors()

    @marks.testrail_id(5718)
    @marks.medium
    def test_special_characters_in_password_when_creating_new_account(self):
        sign_in = SignInView(self.driver)
        sign_in.create_user(password=basic_user['special_chars_password'])
        sign_in.relogin(password=basic_user['special_chars_password'])
