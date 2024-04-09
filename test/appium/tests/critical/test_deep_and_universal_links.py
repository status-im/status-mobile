import pytest

from tests import marks
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="new_one_1")
@marks.new_ui_critical
class TestDeepLinksOneDevice(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.driver = self.drivers[0]
        self.sign_in = SignInView(self.driver)
        self.username = 'test user'

        self.home = self.sign_in.create_user(username=self.username)
        self.home.communities_tab.click_until_presence_of_element(self.home.plus_community_button)
        self.community_name = "open community"
        self.channel_name = "general"
        self.community = self.home.create_community(community_type="open")
        self.profile_view = self.home.get_profile_view()
        self.browser_view = self.home.get_dapp_view()
        self.home.get_chat(self.community_name, community=True).click()
        self.community_view = self.home.get_community_view()
        self.channel = self.community_view.get_channel(self.channel_name).click()

    @marks.testrail_id(704613)
    def test_links_open_universal_links_from_chat(self):
        profile_urls = {
            "https://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj": "zQ3...arJQSj",
            "https://status.app/u#zQ3shVVxZMwLVEQvuu1KF6h4D2mzVyCC4F4mHLZm5dz5XU1aa": "zQ3...5XU1aa",
            "https://status.app/u/CweACg0KC1Rlc3RVc2VyRTJFAw==#zQ3shcFXYnGXxJZnsMThziUNMwyA5uGLp58bLGmfb3qaWD1F6": "TestUserE2E"}

        for url, text in profile_urls.items():
            self.channel.just_fyi("Opening profile '%s' by the url %s" % (text, url))
            self.channel.chat_message_input.clear()
            self.channel.send_message(url)
            self.channel.chat_element_by_text(url).click_on_link_inside_message_body()
            if self.channel.profile_send_contact_request_button.is_element_displayed(10):
                # username_text = self.profile_view.default_username_text.text
                username_text = self.profile_view.contact_name_text.text
                if not (username_text.endswith(url[-6:]) or username_text == text):
                    self.errors.append("Incorrect username is shown for profile url %s" % url)
            else:
                self.errors.append("Profile was not opened by the profile url %s" % url)
            self.profile_view.close_button.click()

        closed_community_urls = {
            "https://status.app/c/G8EAAMR_fz8tsCQ-aR2QrCS5sVAvvzc_N3mAA-En_Zxy4JA3j7Dl1A50Pd4DbooQOMbWf7E1_4wipgDyGe8XZEappDn-Qomf9l_xyXhSYBuSQic8InCEUBSRGR0oixSTh3iw5ZCxzkGSI95Iyu1EBpcIlFOEMPHpKUBIdkkoKBJglMDKko8O8dBvBtIYncOA8mwztwLpx3C0rK_u59PldFuXe4cx#zQ3shwQnEfMtcXpHXF4qJPyCGgw2F18N3nbGzYbzsVHnMq4yK":
                "Status mobile QA community max",
            "https://status.app/c/G1AAAGR0G-IRb2YJD4lRXwLusAFnGrDHGNl6Wt55MIARwVYvarnO873011-fdVSz1kHSan-qq0G96vOaMqyTRhJnQV74KCUr#zQ3shb9irJR66rhG1E8sQZX8pDU3dpGm4daYSmPVDd2e73ewE":
                "Open community for e2e",
            "https://status.app/c/G00AAGS9TbI9mSR-ZNmFrhRjNuEeXAAbcAIUaLLJyjMOG3ACJQ12oIHD78QhzO9s_T5bUeU7rnATWJg3mGgTUemrAg==#zQ3shspPKCZ1VPVQ9dLXGufUGvGphjxVwrcZ6rkZc7S39T4b3":
                "closed community"
        }
        for url, text in closed_community_urls.items():
            self.channel.just_fyi("Opening community '%s' by the url %s" % (text, url))
            self.channel.chat_message_input.clear()
            self.channel.send_message(url)
            self.channel.chat_element_by_text(url).click_on_link_inside_message_body()
            if not self.community_view.join_button.is_element_displayed(
                    10) or self.community_view.community_title.text != text:
                self.errors.append("Community '%s' was not requested to join by the url %s" % (text, url))
            if text != "open community":  # the last one
                self.home.jump_to_card_by_text(self.community_name)

        self.errors.verify_no_errors()

    @marks.testrail_id(702775)
    def test_links_deep_links(self):
        self.home.navigate_back_to_home_view()
        self.home.browser_tab.click()

        profile_links = {
            "status-app://u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj": None,
            "status-app://u#zQ3shVVxZMwLVEQvuu1KF6h4D2mzVyCC4F4mHLZm5dz5XU1aa": None,
            "status-app://u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2d": "Restored desktop",
            "status-app://u/CweACg0KC1Rlc3RVc2VyRTJFAw==#zQ3shcFXYnGXxJZnsMThziUNMwyA5uGLp58bLGmfb3qaWD1F6": "TestUserE2E"
        }
        for link, text in profile_links.items():
            self.channel.just_fyi("Opening profile link %s" % link)
            self.browser_view.open_url(link)
            # shown_name_text = self.profile_view.default_username_text.text
            shown_name_text = self.profile_view.contact_name_text.text
            if text:
                name_is_shown = shown_name_text == text or shown_name_text.endswith(link[-6:])
            else:
                name_is_shown = shown_name_text.endswith(link[-6:])
            if not self.channel.profile_send_contact_request_button.is_element_displayed(10) or not name_is_shown:
                self.errors.append("Profile was not opened by the profile deep link %s" % link)
            self.browser_view.click_system_back_button()

        community_links = {
            "status.app://c/G8EAAMR_fz8tsCQ-aR2QrCS5sVAvvzc_N3mAA-En_Zxy4JA3j7Dl1A50Pd4DbooQOMbWf7E1_4wipgDyGe8XZEappDn-Qomf9l_xyXhSYBuSQic8InCEUBSRGR0oixSTh3iw5ZCxzkGSI95Iyu1EBpcIlFOEMPHpKUBIdkkoKBJglMDKko8O8dBvBtIYncOA8mwztwLpx3C0rK_u59PldFuXe4cx#zQ3shwQnEfMtcXpHXF4qJPyCGgw2F18N3nbGzYbzsVHnMq4yK":
                "Status mobile QA community max",
            "status.app://c/G1AAAGR0G-IRb2YJD4lRXwLusAFnGrDHGNl6Wt55MIARwVYvarnO873011-fdVSz1kHSan-qq0G96vOaMqyTRhJnQV74KCUr#zQ3shb9irJR66rhG1E8sQZX8pDU3dpGm4daYSmPVDd2e73ewE":
                "Open community for e2e",
            "status.app://c/G00AAGS9TbI9mSR-ZNmFrhRjNuEeXAAbcAIUaLLJyjMOG3ACJQ12oIHD78QhzO9s_T5bUeU7rnATWJg3mGgTUemrAg==#zQ3shspPKCZ1VPVQ9dLXGufUGvGphjxVwrcZ6rkZc7S39T4b3":
                "closed community"
        }
        for link, text in community_links.items():
            self.channel.just_fyi("Opening community '%s' by the link %s" % (text, link))
            self.browser_view.open_url(link)
            if not self.community_view.join_button.is_element_displayed(
                    10) or self.community_view.community_title.text != text:
                self.errors.append("Community '%s' was not requested to join by the deep link %s" % (text, link))
            self.home.navigate_back_to_home_view()
            self.home.browser_tab.click()

        self.errors.verify_no_errors()

    @marks.testrail_id(704614)
    @marks.skip  # ToDo: the feature is not ready yet
    def test_links_open_universal_links_from_other_apps(self):
        app_package = self.driver.current_package
        self.home.just_fyi("Opening a profile URL from google search bar when user is still logged in")
        profile_url = "https://status.app/u#zQ3shVVxZMwLVEQvuu1KF6h4D2mzVyCC4F4mHLZm5dz5XU1aa"
        self.home.click_system_home_button()
        self.home.open_link_from_google_search_app(profile_url, app_package)
        if not self.channel.profile_add_to_contacts_button.is_element_displayed(
                10) or not self.profile_view.default_username_text.text.endswith(profile_url[-6:]):
            self.errors.append("Profile was not opened by the url %s when user is logged in" % profile_url)

        self.home.just_fyi("Opening a community URL from google search bar when user is logged out")
        self.driver.terminate_app(app_package)
        community_url = "https://status.app/c/Ow==#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XK"
        self.home.open_link_from_google_search_app(community_url, app_package)
        self.sign_in.sign_in()
        if not self.community_view.join_button.is_element_displayed(10):
            self.errors.append("Closed community was not requested to join by the url %s" % community_url)

        # ToDo: enable when https://github.com/status-im/status-mobile/issues/18074 is fixed
        # self.home.just_fyi("Opening a community channel URL from google search bar with no account created")
        # self.driver.reset()
        # self.home.click_system_home_button()
        # channel_url = "https://status.app/cc/Ow==#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XK"
        # self.home.open_link_from_google_search_app(channel_url, app_package)
        # self.sign_in.create_user()
        # if not self.home.element_by_translation_id(
        #         "community-admins-will-review-your-request").is_element_displayed(10):
        #     self.errors.append("Created user was not redirected to a community channel by the url %s" % channel_url)

        self.errors.verify_no_errors()
