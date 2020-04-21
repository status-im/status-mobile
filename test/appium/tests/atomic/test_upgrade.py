from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView

@marks.all
@marks.upgrade
class TestUpgradeApplication(SingleDeviceTestCase):

    @marks.testrail_id(6284)
    def test_apk_upgrade(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        profile.about_button.click()
        old_version = profile.app_version_text.text
        profile.upgrade_app()

        sign_in.driver.launch_app()
        home = sign_in.sign_in()

        profile = home.profile_button.click()
        profile.about_button.click()
        new_version = profile.app_version_text.text
        print('Upgraded app version is %s vs base version is %s ' % (new_version, old_version))
        assert new_version != old_version
