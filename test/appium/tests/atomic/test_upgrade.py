import pytest
from tests import marks
from tests.base_test_case import SingleDeviceTestCase
from views.sign_in_view import SignInView


class TestUpgradeApplication(SingleDeviceTestCase):

    def setup_method(self, method, **kwargs):
        super(TestUpgradeApplication, self).setup_method(method, app='sauce-storage:app-release.apk')
        self.apk_name = ([i for i in [i for i in pytest.config.getoption('apk').split('/') if '.apk' in i]])[0]

    @pytest.mark.skip
    @marks.testrail_id(5713)
    @marks.upgrade
    def test_apk_upgrade(self):
        sign_in = SignInView(self.driver)
        home = sign_in.create_user()
        profile = home.profile_button.click()
        about = profile.about_button.click()
        old_version = about.version.text

        profile.driver.install_app('https://status-im.ams3.digitaloceanspaces.com/' +
                                   self.apk_name, replace=True)
        sign_in.driver.launch_app()
        home = sign_in.sign_in()

        profile = home.profile_button.click()
        about = profile.about_button.click()
        new_version = about.version.text
        print(new_version, old_version)
        assert new_version != old_version
