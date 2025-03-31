import pytest

from base_test_case import MultipleSharedDeviceTestCase, create_shared_drivers
from tests import marks
from views.sign_in_view import SignInView


def _check_account_creation(driver):
    sign_in_view = SignInView(driver)
    home_view = sign_in_view.create_user()
    home_view.profile_button.wait_for_visibility_of_element()


@pytest.mark.xdist_group(name="two_1")
@marks.nightly
class TestAndroid12(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(quantity=1, platform_version=12, device_name="Pixel 5")

    @marks.testrail_id(741806)
    def test_create_account_android_12(self):
        _check_account_creation(self.drivers[0])


@pytest.mark.xdist_group(name="three_1")
@marks.nightly
class TestAndroid13(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(quantity=1, platform_version=13)

    @marks.testrail_id(741807)
    def test_create_account_android_13(self):
        _check_account_creation(self.drivers[0])
