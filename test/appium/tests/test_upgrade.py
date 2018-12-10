from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestUpgradeApplication(SingleDeviceTestCase):
    @marks.testrail_id(5713)
    @marks.upgrade
    def test_apk_upgrade(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        old_version_info = profile.application_version
        profile.driver.install_app('https://ci.status.im/job/status-react/job/nightly/lastSuccessfulBuild/artifact/StatusIm-181207-085022-915ccb-e2e.apk', replace=True)
        sign_in.driver.launch_app()
        home = sign_in.sign_in()
        profile = home.profile_button.click()
        new_version_info = profile.application_version
        self.driver.fail()
        assert new_version_info != old_version_info
