import datetime
import random
from datetime import timedelta

import emoji
import pytest
from _pytest.outcomes import Failed
from dateutil import parser
from selenium.common.exceptions import NoSuchElementException, TimeoutException

from tests import marks, test_dapp_name, test_dapp_url, run_in_parallel, pytest_config_global, transl
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.chat_view import CommunityView
from views.dbs.waku_backup import user as waku_user
from views.sign_in_view import SignInView


@pytest.mark.xdist_group(name="three_1")
@marks.critical
class TestPublicChatBrowserOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])

        self.home = self.sign_in.create_user()
        self.public_chat_name = self.home.get_random_chat_name()
        self.chat = self.home.join_public_chat(self.public_chat_name)

    @marks.testrail_id(5675)
    def test_public_chat_fetch_more_history(self):
        self.home.just_fyi("Check that can fetch previous history for several days")
        device_time = parser.parse(self.drivers[0].device_time)
        yesterday = (device_time - timedelta(days=1)).strftime("%b %-d, %Y")
        before_yesterday = (device_time - timedelta(days=2)).strftime("%b %-d, %Y")
        quiet_time_yesterday, quiet_time_before_yesterday = '24 hours', '2 days'
        fetch_more = self.home.get_translation_by_key("load-more-messages")
        for message in (yesterday, quiet_time_yesterday):
            if not self.chat.element_by_text_part(message).is_element_displayed(120):
                self.drivers[0].fail('"%s" is not shown' % message)
        self.chat.element_by_text_part(fetch_more).wait_and_click(120)
        self.chat.element_by_text_part(fetch_more).wait_for_visibility_of_element(180)
        for message in (before_yesterday, quiet_time_before_yesterday):
            if not self.chat.element_by_text_part(message).is_element_displayed():
                self.drivers[0].fail('"%s" is not shown' % message)
        self.home.just_fyi("Check that can fetch previous history for month")
        times = {
            "three-days": '5 days',
            "one-week": '12 days',
            "one-month": ['43 days', '42 days', '41 days', '40 days'],
        }
        profile = self.home.profile_button.click()
        profile.sync_settings_button.click()
        profile.sync_history_for_button.click()
        for period in times:
            profile.just_fyi("Checking %s period" % period)
            profile.element_by_translation_id(period).click()
            profile.home_button.click(desired_view='chat')
            self.chat.element_by_text_part(fetch_more).wait_and_click(120)
            if period != "one-month":
                if not profile.element_by_text_part(times[period]).is_element_displayed(30):
                    self.errors.append("'Quiet here for %s' is not shown after fetching more history" % times[period])
            else:
                variants = times[period]
                self.chat.element_by_text_part(fetch_more).wait_for_invisibility_of_element(120)
                res = any(profile.element_by_text_part(variant).is_element_displayed(30) for variant in variants)
                if not res:
                    self.errors.append("History is not fetched for one month!")
            self.home.profile_button.click(desired_element_text=profile.get_translation_by_key("default-sync-period"))
            self.errors.verify_no_errors()

    @marks.testrail_id(5396)
    def test_public_chat_navigate_to_chat_when_relaunch(self):
        text_message = 'some_text'
        self.home.home_button.double_click()
        self.home.get_chat('#%s' % self.public_chat_name).click()
        self.chat.send_message(text_message)
        self.chat.reopen_app()
        if not self.chat.chat_element_by_text(text_message).is_element_displayed(30):
            self.drivers[0].fail("Not navigated to chat view after reopening app")

    @marks.testrail_id(5317)
    def test_public_chat_copy_and_paste_message_in_chat_input(self):
        message_text = {'text_message': 'mmmeowesage_text'}
        formatted_message = {'message_with_link': 'https://status.im',
                             # TODO: blocked with 11161 (rechecked 04.10.22, valid)
                             # 'message_with_tag': '#successishere'
                             }
        message_input = self.chat.chat_message_input
        if not message_input.is_element_displayed():
            self.home.get_chat('#%s' % self.public_chat_name).click()
        message_input.send_keys(message_text['text_message'])
        self.chat.send_message_button.click()

        self.chat.copy_message_text(message_text['text_message'])

        message_input.paste_text_from_clipboard()
        if message_input.text != message_text['text_message']:
            self.errors.append('Message %s text was not copied in a public chat' % message_text['text_message'])
        message_input.clear()

        for message in formatted_message:
            message_input.send_keys(formatted_message[message])
            self.chat.send_message_button.click()

            message_bubble = self.chat.chat_element_by_text(formatted_message[message])
            message_bubble.sent_status_checkmark.long_press_element()
            self.chat.element_by_text('Copy').click()

            message_input.paste_text_from_clipboard()
            if message_input.text != formatted_message[message]:
                self.errors.append('Message %s text was not copied in a public chat' % formatted_message[message])
            message_input.clear()

        self.errors.verify_no_errors()

    @marks.testrail_id(700738)
    def test_public_chat_tag_message(self):
        tag_message = '#wuuut'
        self.home.home_button.double_click()
        self.home.get_chat('#%s' % self.public_chat_name).click()
        self.home.just_fyi("Check that will be redirected to chat view on tap on tag message")
        self.chat.send_message(tag_message)
        self.chat.element_starts_with_text(tag_message).click()
        self.chat.element_by_text_part(self.public_chat_name).wait_for_invisibility_of_element()
        if not self.chat.user_name_text.text == tag_message:
            self.errors.append('Could not redirect a user to a public chat tapping the tag message.')
        self.home.just_fyi("Check that chat is added to home view")
        self.chat.home_button.double_click()
        if not self.home.element_by_text(tag_message).is_element_displayed():
            self.errors.append('Could not find the public chat in user chat list.')
        self.errors.verify_no_errors()

    @marks.testrail_id(700739)
    def test_public_chat_open_using_deep_link(self):
        self.drivers[0].terminate_app(self.drivers[0].current_package)
        chat_name = self.home.get_random_chat_name()
        deep_link = 'status-im://%s' % chat_name
        self.sign_in.open_weblink_and_login(deep_link)
        try:
            assert self.chat.user_name_text.text == '#' + chat_name
        except (AssertionError, NoSuchElementException):
            self.drivers[0].fail("Public chat '%s' is not opened" % chat_name)

    @marks.testrail_id(702072)
    def test_browser_blocked_url(self):
        dapp = self.home.dapp_tab_button.click()
        for url in ('metamask.site', 'cryptokitties.domainname'):
            dapp.just_fyi('Checking blocked website %s' % url)
            dapp_detail = dapp.open_url(url)
            dapp_detail.element_by_translation_id('browsing-site-blocked-title')
            if dapp_detail.browser_refresh_page_button.is_element_displayed():
                self.errors.append("Refresh button is present in blocked site")
            dapp_detail.go_back_button.click()
            dapp_detail.open_tabs_button.click()
            dapp.element_by_text_part(url[:8]).click()
            dapp_detail.continue_anyway_button.click()
            if dapp_detail.element_by_text('This site is blocked').is_element_displayed():
                self.errors.append("Failed to open Dapp after 'Continue anyway' tapped for %s" % url)
            dapp_detail.open_tabs_button.click()
            dapp_detail.empty_tab_button.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702073)
    def test_browser_connection_is_secure_not_secure_warning(self):
        dapp = self.home.dapp_tab_button.click()
        web_page = dapp.open_url('http://www.dvwa.co.uk')
        web_page.url_edit_box_lock_icon.click_until_presence_of_element(
            web_page.element_by_translation_id("browser-not-secure"))
        web_page.open_tabs_button.click()
        web_page.empty_tab_button.click()
        dapp.just_fyi('Checking connection is secure for Airswap')
        web_page = dapp.open_url('https://instant.airswap.io')
        web_page.wait_for_d_aap_to_load()
        web_page.url_edit_box_lock_icon.click_until_presence_of_element(
            web_page.element_by_translation_id("browser-secure"))
        web_page.open_tabs_button.click()
        web_page.empty_tab_button.click()

    @marks.testrail_id(702074)
    def test_browser_invalid_url(self):
        dapp = self.home.dapp_tab_button.click()
        browsing_view = dapp.open_url('invalid.takoe')
        browsing_view.element_by_translation_id("web-view-error").wait_for_element(20)

    @marks.testrail_id(702075)
    def test_browser_offline(self):
        dapp = self.home.dapp_tab_button.click()
        self.home.toggle_airplane_mode()
        browsing_view = dapp.open_url('status.im')
        offline_texts = ['Unable to load page', 'ERR_INTERNET_DISCONNECTED']
        for text in offline_texts:
            browsing_view.element_by_text_part(text).wait_for_element(15)
        self.home.toggle_airplane_mode()
        browsing_view.browser_refresh_page_button.click_until_presence_of_element(
            browsing_view.element_by_text_part('An Open Source Community'))

    @marks.testrail_id(702076)
    def test_browser_delete_close_tabs(self):
        dapp = self.home.dapp_tab_button.click()
        urls = {
            'google.com': 'Google',
            'status.im': 'Status - Private',
            'bbc.com': 'bbc.com'
        }
        for url in urls:
            web_page = dapp.open_url(url)
            web_page.open_tabs_button.click()
            web_page.empty_tab_button.click()

        self.home.just_fyi('Delete one tab')
        web_page.remove_tab(name=urls['bbc.com'])
        web_page.open_tabs_button.wait_for_invisibility_of_element()
        web_page.element_by_text_part(urls['bbc.com']).wait_for_invisibility_of_element()

        self.home.just_fyi('Close all tabs via "Close all", relogin and check that it is not reappearing')
        web_page.close_all_button.click()
        self.home.reopen_app()
        web_page.dapp_tab_button.click()
        web_page.open_tabs_button.click()
        if web_page.element_by_text_part(urls['status.im']).is_element_displayed():
            self.errors.append('Tabs are not closed or reappeared after re-login!')

        self.errors.verify_no_errors()

    @marks.testrail_id(702077)
    def test_browser_bookmarks_create_edit_remove(self):
        dapp = self.home.dapp_tab_button.click()

        self.home.just_fyi('Add some url to bookmarks with default name')
        web_page = dapp.open_url('status.im')
        default_bookmark_name = web_page.add_to_bookmarks()
        web_page.browser_previous_page_button.click()
        if not web_page.element_by_text(default_bookmark_name).is_element_displayed():
            self.errors.append("Bookmark with default name is not added!")

        self.home.just_fyi('Add some url to bookmarks with custom name')
        custom_name = 'Custom BBC'
        dapp.open_url('bbc.com')
        web_page.add_to_bookmarks(custom_name)
        web_page.open_tabs_button.click()
        web_page.empty_tab_button.click()
        if not web_page.element_by_text(custom_name).is_element_displayed():
            self.driver.fail("Bookmark with custom name is not added!")

        self.home.just_fyi('Checking "Open in new tab"')
        dapp.browser_entry_long_press(custom_name)
        dapp.open_in_new_tab_button.click()
        web_page.options_button.click()
        if not web_page.element_by_translation_id('remove-favourite').is_element_displayed():
            self.errors.append("Remove favourite is not shown on added bookmark!")
        dapp.click_system_back_button()

        self.home.just_fyi('Check deleting bookmark')
        web_page.open_tabs_button.click()
        web_page.empty_tab_button.click()
        dapp.browser_entry_long_press(custom_name)
        dapp.delete_bookmark_button.click()
        if web_page.element_by_text(custom_name).is_element_displayed():
            self.errors.append("Bookmark with custom name is not deleted!")

        self.home.just_fyi('Check "Edit bookmark" and "Open in new tab"')
        edited_name = 'My Fav Status'
        dapp.browser_entry_long_press(default_bookmark_name)
        dapp.edit_bookmark_button.click()
        web_page.edit_bookmark_name(edited_name)
        if not web_page.element_by_text(edited_name).is_element_displayed():
            self.driver.fail("Edited bookmark name is not shown!")

        self.errors.verify_no_errors()

    @marks.testrail_id(702078)
    def test_browser_web3_permissions_testdapp(self):
        self.home.home_button.double_click()

        self.home.just_fyi('open Status Test Dapp, allow all and check permissions in Profile')
        web_view = self.home.open_status_test_dapp()
        dapp = self.home.dapp_tab_button.click()
        profile = self.home.profile_button.click()
        profile.privacy_and_security_button.click()
        profile.dapp_permissions_button.click()
        profile.element_by_text(test_dapp_name).click()
        if not profile.element_by_text(self.home.status_account_name).is_element_displayed():
            self.errors.append('Wallet permission was not granted')
        if not profile.element_by_translation_id("chat-key").is_element_displayed():
            self.errors.append('Contact code permission was not granted')

        profile.just_fyi('revoke access and check that they are asked second time')
        profile.revoke_access_button.click()
        profile.get_back_to_home_view()
        profile.dapp_tab_button.click()
        web_view.open_tabs_button.click()
        web_view.empty_tab_button.click()
        dapp.open_url(test_dapp_url)
        if not dapp.element_by_text_part(self.home.status_account_name).is_element_displayed():
            self.errors.append('Wallet permission is not asked')
        if dapp.allow_button.is_element_displayed():
            dapp.allow_button.click(times_to_click=1)
        if not dapp.element_by_translation_id("your-contact-code").is_element_displayed():
            self.errors.append('Profile permission is not asked')
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_one_1")
@marks.new_ui_critical
class TestCommunityOneDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(1)
        self.sign_in = SignInView(self.drivers[0])
        self.username = 'first user'
        self.discovery_community_attributes = "Contributors' test community", 'test anything here', 'Web3', 'Software dev'

        self.home = self.sign_in.create_user(username=self.username)
        self.home.communities_tab.click_until_presence_of_element(self.home.plus_community_button)
        self.community_name = "closed community"
        self.channel_name = "cats"
        self.community = self.home.create_community(community_type="closed")

        self.home.get_chat(self.community_name, community=True).click()
        self.community_view = self.home.get_community_view()
        self.channel = self.community_view.get_channel(self.channel_name).click()

    @marks.testrail_id(703503)
    @marks.xfail(reason="https://github.com/status-im/status-mobile/issues/17175", run=False)
    def test_community_discovery(self):
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.discover_communities_button.click()
        for text in self.discovery_community_attributes:
            if not self.home.element_by_text(text).is_element_displayed(10):
                self.errors.append("%s in not in Discovery!" % text)
        self.home.element_by_text(self.discovery_community_attributes[0]).click()
        element_templates = {
            self.community_view.join_button: 'discovery_join_button.png',
            self.community_view.get_channel_avatar(): 'discovery_general_channel.png',
        }
        for element, template in element_templates.items():
            if element.is_element_differs_from_template(template):
                element.save_new_screenshot_of_element('%s_different.png' % element.name)
                self.errors.append("Element %s is different from expected template %s!" % (element.locator, template))
        self.errors.verify_no_errors()

    @marks.testrail_id(702846)
    def test_community_navigate_to_channel_when_relaunch(self):
        text_message = 'some_text'
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)

        self.channel.send_message(text_message)
        self.channel.chat_element_by_text(text_message).wait_for_visibility_of_element()
        self.channel.reopen_app()
        if not self.channel.chat_element_by_text(text_message).is_element_displayed(30):
            self.drivers[0].fail("Not navigated to channel view after reopening app")

    @marks.testrail_id(702742)
    def test_community_copy_and_paste_message_in_chat_input(self):
        message_texts = ['mmmeowesage_text', 'https://status.im']
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)

        for message in message_texts:
            self.channel.send_message(message)
            self.channel.copy_message_text(message)
            actual_copied_text = self.channel.driver.get_clipboard_text()
            if actual_copied_text != message:
                self.errors.append(
                    'Message %s text was not copied in community channel, text in clipboard %s' % actual_copied_text)

        self.errors.verify_no_errors()

    @marks.testrail_id(702869)
    def test_community_undo_delete_message(self):
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.navigate_back_to_home_view()
            self.home.get_to_community_channel_from_home(self.community_name)
        message_to_delete = "message to delete and undo"
        self.channel.send_message(message_to_delete)
        self.channel.delete_message_in_chat(message_to_delete)
        self.channel.element_by_text("Undo").click()
        try:
            self.channel.chat_element_by_text(message_to_delete).wait_for_visibility_of_element()
        except TimeoutException:
            pytest.fail("Message was not restored by clicking 'Undo' button")
        if self.channel.element_starts_with_text("Message deleted").is_element_displayed():
            pytest.fail("Text about deleted message is shown in the chat")

    @marks.testrail_id(703382)
    def test_community_mute_community_and_channel(self):
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.just_fyi("Mute community and check that channels are also muted")
        self.home.mute_chat_long_press(chat_name=self.community_name, mute_period="mute-for-1-hour", community=True)
        device_time = self.home.driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_time = current_time + datetime.timedelta(hours=1)
        expected_text = "Muted until %s %s" % (
            expected_time.strftime('%H:%M'),
            "today" if current_time.hour < 23 else "tomorrow"
        )
        self.home.get_chat(self.community_name, community=True).long_press_element()
        if not self.home.element_by_text(expected_text).is_element_displayed():
            self.errors.append("Text '%s' is not shown for muted community" % expected_text)
        self.home.click_system_back_button()

        self.home.get_chat(self.community_name, community=True).click()
        self.community_view.get_channel(self.channel_name).long_press_element()
        if not self.home.element_by_text(expected_text).is_element_displayed():
            self.errors.append("Text '%s' is not shown for a channel in muted community" % expected_text)

        self.home.just_fyi("Unmute channel and check that the community is also unmuted")
        self.home.mute_channel_button.click()
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.get_chat(self.community_name, community=True).long_press_element()
        if not self.home.element_by_text("Mute community").is_element_displayed():
            self.errors.append("Community is not unmuted when channel is unmuted")
        self.home.click_system_back_button()

        self.home.just_fyi("Mute channel and check that community is not muted")
        self.home.get_chat(self.community_name, community=True).click()
        self.home.mute_chat_long_press(chat_name=self.channel_name, mute_period="mute-for-1-week",
                                       community_channel=True)
        device_time = self.home.driver.device_time
        current_time = datetime.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z")
        expected_time = current_time + datetime.timedelta(days=7)
        expected_text = "Muted until %s" % expected_time.strftime('%H:%M %a %-d %b')
        self.community_view.get_channel(self.channel_name).long_press_element()
        if not self.home.element_by_text(expected_text).is_element_displayed():
            self.errors.append("Text '%s' is not shown for a muted community channel" % expected_text)
        self.home.click_system_back_button()
        self.home.navigate_back_to_home_view()
        self.home.communities_tab.click()
        self.home.get_chat(self.community_name, community=True).long_press_element()
        if self.home.element_by_text(expected_text).is_element_displayed() or self.home.mute_community_button.text != \
                transl["mute-community"]:
            self.errors.append("Community is muted when channel is muted")
        self.home.click_system_back_button()

        self.errors.verify_no_errors()

    @marks.testrail_id(703133)
    def test_restore_multiaccount_with_waku_backup_remove_switch(self):
        self.home.navigate_back_to_home_view()
        profile = self.home.profile_button.click()
        profile.logout()
        self.home.just_fyi("Restore user with predefined communities and contacts")
        self.sign_in.recover_access(passphrase=waku_user.seed, second_user=True)

        self.home.just_fyi("Restore user with predefined communities and contacts")

        self.home.just_fyi("Check contacts/blocked users")
        self.home.chats_tab.click()
        self.home.contacts_tab.click()
        contacts_number = self.home.get_contact_rows_count()
        if contacts_number != len(waku_user.contacts):
            self.errors.append(
                "Incorrect contacts number restored: %s instead of %s" % (contacts_number, len(waku_user.contacts)))
        else:
            for i in range(contacts_number):
                key = waku_user.contacts[i]
                if not self.home.element_by_text(key).is_element_displayed(30):
                    self.errors.append('%s was not restored as a contact from waku backup!' % key)
                # Disabled for simple check as sometimes from waku-backup users restored with 3-random names
                # self.home.click_system_back_button_until_element_is_shown()
                # contact_row = self.home.contact_details_row(index=i + 1)
                # shown_name_text = contact_row.username_text.text
                # if shown_name_text in waku_user.contacts:
                #     waku_user.contacts.remove(shown_name_text)
                #     continue
                # else:
                #     contact_row.click()
                #     shown_name_text = profile.default_username_text.text
                #     if shown_name_text in waku_user.contacts:
                #         waku_user.contacts.remove(shown_name_text)
                #         continue
                #     else:
                #         chat = self.home.get_chat_view()
                #         chat.profile_send_message_button.click()
                #         for name in waku_user.contacts:
                #             if chat.element_starts_with_text(name).is_element_displayed(sec=20):
                #                 waku_user.contacts.remove(name)
                #                 continue
        # if waku_user.contacts:
        #     self.errors.append(
        #         "Contact(s) was (were) not restored from backup: %s!" % ", ".join(waku_user.contacts))

        self.home.just_fyi("Check restored communities")
        self.home.communities_tab.click()
        for key in ['admin_open', 'member_open', 'admin_closed', 'member_closed']:
            if not self.home.element_by_text(waku_user.communities[key]).is_element_displayed(30):
                self.errors.append("%s was not restored from waku-backup!!" % key)
        # TODO: there is a bug when pending community sometimes restored as joined; needs investigation
        # self.home.opened_communities_tab.click()
        # if not self.home.element_by_text(waku_user.communities['member_pending']).is_element_displayed(30):
        #     self.errors.append("Pending community %s was not restored from waku-backup!" % waku_user.communities['member_pending'])

        if not pytest_config_global['pr_number']:
            self.home.just_fyi("Perform back up")
            self.home.navigate_back_to_home_view()
            self.home.profile_button.click()
            profile.sync_settings_button.click()
            profile.backup_settings_button.click()
            profile.perform_backup_button.click()

        self.home.just_fyi("Check that can login with different user")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.click()
        self.sign_in.element_by_text(self.username).click()
        self.sign_in.sign_in()
        self.home.communities_tab.click()
        if self.home.element_by_text(waku_user.communities['admin_open']).is_element_displayed(30):
            self.errors.append("Community of previous user is shown!")

        self.home.just_fyi("Check that can remove user from logged out state")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.click()
        user_card = self.sign_in.get_user(username=self.username)
        user_card.open_user_options()
        self.sign_in.remove_profile_button.click()
        if not self.sign_in.element_by_translation_id("remove-profile-confirm-message").is_element_displayed(30):
            self.errors.append("Warning is not shown on removing profile!")
        self.sign_in.element_by_translation_id("remove").click()

        self.home.just_fyi("Check that removed user is not shown in the list anymore")
        self.home.reopen_app(sign_in=False)
        self.sign_in.show_profiles_button.click()
        if self.sign_in.element_by_text(self.username).is_element_displayed():
            self.errors.append("Removed user is re-appeared after relogin!")

        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_three_2")
@marks.new_ui_critical
class TestCommunityMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.username_1, self.username_2 = "user_1", "user_2"
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'username': self.username_2}))))
        self.homes = self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.public_key_2 = self.home_2.get_public_key_via_share_profile_tab()
        self.profile_1 = self.home_1.get_profile_view()
        [home.navigate_back_to_home_view() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)
        self.text_message = 'hello'

        # self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.home_1.get_chat(self.username_2).wait_for_visibility_of_element()
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        # self.chat_2.send_message(self.text_message)
        # [home.click_system_back_button_until_element_is_shown() for home in self.homes]
        self.home_1.navigate_back_to_home_view()

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = "open community"
        self.channel_name = 'general'
        self.home_1.create_community(community_type="open")
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)
        self.channel_1.send_message(self.text_message)

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.share_community(self.community_name, self.username_2)
        self.home_1.get_to_community_channel_from_home(self.community_name)

        self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.chat_2.send_message(self.text_message)
        self.chat_2.chat_element_by_text(self.community_name).view_community_button.click()
        self.community_2.join_community()
        self.channel_2 = self.community_2.get_channel(self.channel_name).click()
        self.channel_2.chat_message_input.wait_for_visibility_of_element(20)

    @marks.testrail_id(702838)
    def test_community_message_send_check_timestamps_sender_username(self):
        message = self.text_message
        sent_time_variants = self.channel_1.convert_device_time_to_chat_timestamp()
        timestamp = self.channel_1.chat_element_by_text(message).timestamp
        if sent_time_variants and timestamp:
            if timestamp not in sent_time_variants:
                self.errors.append("Timestamp is not shown, expected: '%s', in fact: '%s'" %
                                   (", ".join(sent_time_variants), timestamp))
        self.channel_1.verify_message_is_under_today_text(message, self.errors)
        self.channel_2.send_message("one more message")
        new_message = "new message"
        self.channel_1.send_message(new_message)
        self.channel_2.verify_message_is_under_today_text(new_message, self.errors, 60)
        if self.channel_2.chat_element_by_text(new_message).username.text != self.username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % self.username_1)
        self.errors.verify_no_errors()

    @marks.testrail_id(702843)
    def test_community_message_edit(self):
        message_before_edit, message_after_edit = 'Message BEFORE edit', "Message AFTER edit 2"
        self.channel_1.send_message(message_before_edit)
        self.channel_1.edit_message_in_chat(message_before_edit, message_after_edit)
        for channel in (self.channel_1, self.channel_2):
            if not channel.element_by_text_part(message_after_edit).is_element_displayed(60):
                self.errors.append('Message is not edited')
        message_text_after_edit = message_after_edit + ' (Edited)'
        self.channel_2.set_reaction(message_text_after_edit)
        try:
            self.channel_1.chat_element_by_text(message_text_after_edit).emojis_below_message().wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Message reaction is not shown for the sender")
        self.errors.verify_no_errors()

    @marks.testrail_id(702839)
    def test_community_message_delete(self):
        message_to_delete_everyone = 'delete for everyone'
        message_to_delete_for_me = 'delete for me'
        self.channel_2.send_message(message_to_delete_everyone)
        self.home_2.just_fyi('Delete for message everyone. Checking that message is deleted for all members')
        self.channel_2.delete_message_in_chat(message_to_delete_everyone)
        for channel in (self.channel_1, self.channel_2):
            if not channel.chat_element_by_text(message_to_delete_everyone).is_element_disappeared(30):
                self.errors.append("Deleted message is shown in channel")
        if not self.channel_2.element_by_translation_id('message-deleted-for-everyone').is_element_displayed(30):
            self.errors.append("System message about deletion for everyone is not displayed")

        self.home_2.just_fyi(
            'Deleting message for me. Checking that message is deleted only for the author of the message')
        self.channel_2.send_message(message_to_delete_for_me)
        self.channel_1.chat_element_by_text(message_to_delete_for_me).wait_for_element(120)
        self.channel_2.delete_message_in_chat(message_to_delete_for_me, everyone=False)
        if not self.channel_2.chat_element_by_text(message_to_delete_for_me).is_element_disappeared(30):
            self.errors.append("Deleted for me message is shown in channel for the author of message")
        if not self.channel_2.element_by_translation_id('message-deleted-for-you').is_element_displayed(30):
            self.errors.append("System message about deletion for you is not displayed")
        if not self.channel_1.chat_element_by_text(message_to_delete_for_me).is_element_displayed(30):
            self.errors.append("Deleted for me message is deleted all channel members")
        self.errors.verify_no_errors()

    @marks.testrail_id(703194)
    def test_community_several_images_send_reply(self):
        self.home_1.just_fyi('Send several images in 1-1 chat from Gallery')
        image_description, file_name = 'gallery', 'gallery_1.png'
        self.channel_1.send_images_with_description(image_description, [0, 1])

        self.channel_2.just_fyi("Check gallery on second device")
        self.channel_2.navigate_back_to_home_view()
        self.home_2.get_to_community_channel_from_home(self.community_name)
        chat_element = self.channel_2.chat_element_by_text(image_description)
        try:
            chat_element.wait_for_visibility_of_element(120)
            received = True
            if chat_element.image_container_in_message.is_element_differs_from_template(file_name, 5):
                self.errors.append("Gallery message do not match the template!")
        except TimeoutException:
            self.errors.append("Gallery message was not received")
            received = False

        if received:
            self.channel_2.just_fyi("Checking an ability to save and share an image from gallery")
            chat_element.image_container_in_message.image_by_index(1).click()
            if not self.channel_2.share_image_icon_button.is_element_displayed():
                self.errors.append("Can't share an image from gallery.")
            if self.channel_2.view_image_options_button.is_element_displayed():
                self.channel_2.view_image_options_button.click()
                if not self.channel_2.save_image_icon_button.is_element_displayed():
                    self.errors.append("Can't save an image from gallery.")
            else:
                self.errors.append("Image options button is not shown for an image from gallery.")

            self.channel_2.navigate_back_to_chat_view()

            self.channel_2.just_fyi("Can reply to gallery")
            self.channel_2.quote_message(image_description)
            message_text = 'reply to gallery'
            self.channel_2.chat_message_input.send_keys(message_text)
            self.channel_2.send_message_button.click()
            chat_element_1 = self.channel_1.chat_element_by_text(message_text)
            if not chat_element_1.is_element_displayed(
                    sec=60) or chat_element_1.replied_message_text != image_description:
                self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(702859)
    def test_community_one_image_send_reply(self):
        self.home_1.just_fyi('Send image in 1-1 chat from Gallery')
        image_description = 'description'
        self.channel_1.send_images_with_description(image_description)

        self.home_2.just_fyi('Check image, description and options for receiver')
        if not self.channel_2.chat_element_by_text(image_description).is_element_displayed(60):
            self.channel_2.hide_keyboard_if_shown()
        self.channel_2.chat_element_by_text(image_description).wait_for_visibility_of_element(10)
        if not self.channel_2.chat_element_by_text(
                image_description).image_in_message.is_element_image_similar_to_template('image_sent_in_community.png'):
            self.errors.append("Not expected image is shown to the receiver")

        if not self.channel_1.chat_element_by_text(image_description).is_element_displayed(60):
            self.channel_1.hide_keyboard_if_shown()
        self.channel_1.chat_element_by_text(image_description).image_in_message.click()
        self.home_1.just_fyi('Save image')
        self.channel_1.view_image_options_button.click()
        self.channel_1.save_image_icon_button.click()
        toast_element = self.channel_1.toast_content_element
        if toast_element.is_element_displayed():
            toast_element_text = toast_element.text
            if toast_element_text != self.channel_1.get_translation_by_key("photo-saved"):
                self.errors.append(
                    "Shown message '%s' doesn't match expected '%s' after saving an image." % (
                        toast_element_text, self.channel_1.get_translation_by_key("photo-saved")))
        else:
            self.errors.append("Message about saving a photo is not shown.")
        self.channel_1.navigate_back_to_chat_view()

        self.channel_1.just_fyi("Check that image is saved in gallery")
        self.channel_1.show_images_button.click()
        self.channel_1.allow_button.click_if_shown()
        if not self.channel_1.get_image_by_index(0).is_element_image_similar_to_template(
                "sauce_dark_image_gallery.png"):
            self.errors.append('Saved image is not shown in Recent')
        self.channel_1.click_system_back_button()

        self.home_2.just_fyi('Check share option on opened image')
        self.channel_2.chat_element_by_text(image_description).image_in_message.click()
        self.channel_2.share_image_icon_button.click()
        self.channel_2.element_starts_with_text("Gmail").click()
        try:
            self.channel_2.wait_for_current_package_to_be('com.google.android.gm')
        except TimeoutException:
            self.errors.append("Can't share image")
        self.channel_2.navigate_back_to_chat_view()

        self.channel_2.just_fyi("Can reply to images")
        self.channel_2.quote_message(image_description)
        message_text = 'reply to image'
        self.channel_2.chat_message_input.send_keys(message_text)
        self.channel_2.send_message_button.click()
        chat_element_1 = self.channel_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=60) or chat_element_1.replied_message_text != image_description:
            self.errors.append('Reply message was not received by the sender')
        self.channel_2.just_fyi("Set a reaction for the image message")
        self.channel_2.set_reaction(message=image_description)
        try:
            self.channel_1.chat_element_by_text(image_description).emojis_below_message().wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Image message reaction is not shown for the sender")
        self.channel_1.just_fyi("Set a reaction for the message reply")
        self.channel_2.set_reaction(message=image_description, emoji="love")
        try:
            self.channel_2.chat_element_by_text(message_text).emojis_below_message(
                emoji="love").wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Reply message reaction is not shown for the reply sender")

        self.errors.verify_no_errors()

    @marks.testrail_id(702840)
    def test_community_emoji_send_copy_paste_reply(self):
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        emoji_message = emoji.emojize(emoji_name)
        self.channel_1.send_message(emoji_message)
        for channel in self.channel_1, self.channel_2:
            if not channel.chat_element_by_text(emoji_unicode).is_element_displayed(30):
                self.errors.append('Message with emoji was not sent or received in community channel')

        self.channel_1.just_fyi("Can copy and paste emojis")
        self.channel_1.copy_message_text(emoji_unicode)
        actual_copied_text = self.channel_1.driver.get_clipboard_text()
        if actual_copied_text != emoji_unicode:
            self.errors.append('Emoji message was not copied, text in clipboard is %s' % actual_copied_text)

        self.channel_1.just_fyi("Can reply to emojis")
        self.channel_2.quote_message(emoji_unicode)
        message_text = 'test message'
        self.channel_2.chat_message_input.send_keys(message_text)
        self.channel_2.send_message_button.click()
        chat_element_1 = self.channel_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=10) or chat_element_1.replied_message_text != emoji_unicode:
            self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(702844)
    def test_community_links_with_previews_github_youtube_twitter_gif_send_enable(self):
        preview_urls = {
            # TODO: disabled because of the bug in 15891
            # 'giphy':{'url': 'https://giphy.com/gifs/this-is-fine-QMHoU66sBXqqLqYvGO',
            #               'title': 'This Is Fine GIF - Find & Share on GIPHY',
            #               'description': 'Discover & share this Meme GIF with everyone you know. GIPHY is how you search, share, discover, and create GIFs.',
            #               'link': 'giphy.com'},
            'github_pr': {'url': 'https://github.com/status-im/status-mobile/pull/11707',
                          'title': 'Update translations by jinhojang6 · Pull Request #11707 · status-im/status-mobile',
                          'description': 'Update translation json files of 19 languages.',
                          'link': 'github.com'},
            'yotube_short': {
                'url': 'https://youtu.be/Je7yErjEVt4',
                'title': 'Status, your gateway to Ethereum',
                'description': 'Learn more at https://status.im. This video aims to provide an explanation '
                               'and brief preview of the utility that will be supported by the Status App - provid...',
                'link': 'youtu.be'},
            'yotube_full': {
                'url': 'https://www.youtube.com/watch?v=XN-SVmuJH2g&list=PLbrz7IuP1hrgNtYe9g6YHwHO6F3OqNMao',
                'title': 'Status & Keycard – Hardware-Enforced Security',
                'description': 'With Status and Keycard, you can enable hardware enforced authorizations to '
                               'your Status account and transactions. Two-factor authentication to access your ac...',
                'link': 'www.youtube.com'},
            'yotube_mobile': {
                'url': 'https://m.youtube.com/watch?v=Je7yErjEVt4',
                'title': 'Status, your gateway to Ethereum',
                'description': 'Learn more at https://status.im. This video aims to provide an explanation '
                               'and brief preview of the utility that will be supported by the Status App - provid...',
                'link': 'm.youtube.com',
            },

            # twitter link is temporary removed from check as current xpath locator in message.preview_title is not applicable for this type of links
            # 'twitter': {
            #     'url': 'https://twitter.com/ethdotorg/status/1445161651771162627?s=20',
            #     'txt': "We've rethought how we translate content, allowing us to translate",
            #     'subtitle': 'Twitter'
            # }
        }
        for home in self.home_1, self.home_2:
            if not home.chat_floating_screen.is_element_displayed():
                home.navigate_back_to_home_view()
                home.get_to_community_channel_from_home(self.community_name)

        for key, data in preview_urls.items():
            self.home_2.just_fyi("Checking %s preview case" % key)
            url = data['url']
            self.channel_2.chat_message_input.send_keys(url)
            self.channel_2.url_preview_composer.wait_for_element(20)
            shown_title = self.channel_2.url_preview_composer_text.text
            if shown_title != data['title']:
                self.errors.append("Preview text is not expected, it is '%s'" % shown_title)
            self.channel_2.send_message_button.click()
            message = self.channel_1.get_preview_message_by_text(url)
            message.wait_for_element(60)
            # if not message.preview_image:
            #     self.errors.append("No preview is shown for %s" % link_data['url'])
            shown_title = message.preview_title.text
            if shown_title != data['title']:
                self.errors.append("Title is not equal expected for '%s', actual is '%s'" % (url, shown_title))
            shown_description = message.preview_subtitle.text
            if shown_description != data['description']:
                self.errors.append(
                    "Description is not equal expected for '%s', actual is '%s'" % (url, shown_description))
            shown_link = message.preview_link.text
            if shown_link != data['link']:
                self.errors.append("Link is not equal expected for '%s', actual is '%s'" % (url, shown_link))

        self.channel_1.just_fyi("Set reaction and check it")
        message_with_reaction = list(preview_urls.values())[-1]['url']
        self.channel_1.set_reaction(message=message_with_reaction, emoji="laugh")
        if not self.channel_2.chat_element_by_text(message_with_reaction).emojis_below_message(
                emoji="laugh").is_element_displayed(10):
            self.channel_2.hide_keyboard_if_shown()
        try:
            self.channel_2.chat_element_by_text(message_with_reaction).emojis_below_message(
                emoji="laugh").wait_for_element_text(1)
        except (Failed, NoSuchElementException):
            self.errors.append("Link message reaction is not shown for the sender")

        self.errors.verify_no_errors()

    @marks.testrail_id(702841)
    def test_community_unread_messages_badge(self):
        self.channel_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        message = 'test message'
        if not self.home_2.chat_floating_screen.is_element_displayed():
            self.home_2.navigate_back_to_home_view()
            self.home_2.communities_tab.click()
            self.home_2.get_to_community_channel_from_home(self.community_name)
        self.channel_2.send_message(message)
        self.home_1.just_fyi('Check new messages badge is shown for community')
        community_element_1 = self.home_1.get_chat(self.community_name, community=True)
        if not community_element_1.new_messages_grey_dot.is_element_displayed(sec=30):
            self.errors.append('New message community badge is not shown')

        community_1 = community_element_1.click()
        channel_1_element = community_1.get_channel(self.channel_name)

        self.home_1.just_fyi('Check new messages badge is shown for community')
        if not community_element_1.new_messages_grey_dot.is_element_displayed():
            self.errors.append('New messages channel badge is not shown on channel')
        channel_1_element.click()
        self.errors.verify_no_errors()

    @marks.xfail(reason="Message can be missed after unblock: https://github.com/status-im/status-mobile/issues/16873")
    @marks.testrail_id(702894)
    def test_community_contact_block_unblock_offline(self):
        for i, channel in enumerate([self.channel_1, self.channel_2]):
            if not channel.chat_message_input.is_element_displayed():
                channel.navigate_back_to_home_view()
                self.homes[i].communities_tab.click()
                self.homes[i].get_to_community_channel_from_home(self.community_name)

        self.channel_1.send_message('message to get avatar of user 2 visible in next message')

        self.channel_2.just_fyi("Sending message before block")
        message_to_disappear = "I should not be in chat"
        self.channel_2.send_message(message_to_disappear)

        self.chat_1.just_fyi('Block user')
        self.channel_1.chat_element_by_text(message_to_disappear).wait_for_visibility_of_element(30)
        chat_element = self.channel_1.chat_element_by_text(message_to_disappear)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.channel_1.block_contact()

        self.chat_1.just_fyi('Check that messages from blocked user are hidden in public chat and close app')
        app_package = self.device_1.driver.current_package
        if not self.chat_1.chat_element_by_text(message_to_disappear).is_element_disappeared(30):
            self.errors.append("Messages from blocked user is not cleared in public chat ")
        self.chat_1.navigate_back_to_home_view()
        self.home_1.chats_tab.click()
        if not self.home_1.element_by_translation_id("no-messages").is_element_displayed():
            self.errors.append("1-1 chat from blocked user is not removed and messages home is not empty!")
        self.chat_1.toggle_airplane_mode()

        # workaround for app closed after airplane mode
        if not self.home_1.chats_tab.is_element_displayed() and \
                not self.chat_1.chat_floating_screen.is_element_displayed():
            self.device_1.driver.activate_app(app_package)
            self.device_1.sign_in()

        self.home_2.just_fyi('Send message to public chat while device 1 is offline')
        message_blocked, message_unblocked = "Message from blocked user", "Hurray! unblocked"
        self.channel_2.send_message(message_blocked)

        self.chat_1.just_fyi('Check that new messages from blocked user are not delivered')
        self.chat_1.toggle_airplane_mode()
        # self.home_1.jump_to_card_by_text('# %s' % self.channel_name)
        self.home_1.communities_tab.click()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.home_1.get_chat(self.channel_name, community_channel=True).click()
        for message in message_to_disappear, message_blocked:
            if self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    "'%s' from blocked user is fetched from offline in community channel" % message)

        self.chat_1.just_fyi('Unblock user and check that can see further messages')
        # TODO: still no blocked users in new UI
        profile_1 = self.home_1.get_profile_view()
        self.home_1.navigate_back_to_home_view()
        self.chat_1.profile_button.click()
        profile_1.contacts_button.wait_and_click()
        profile_1.blocked_users_button.wait_and_click()
        profile_1.element_by_text(self.username_2).click()
        self.chat_1.unblock_contact_button.click()
        self.chat_1.close_button.click()
        self.chat_1.navigate_back_to_home_view()

        self.home_2.just_fyi("Check that can send message in community after unblock")
        self.chat_2.send_message(message_unblocked)
        # self.home_1.jump_to_card_by_text('# %s' % self.channel_name)
        self.home_1.communities_tab.click()
        self.home_1.get_chat(self.community_name, community=True).click()
        self.home_1.get_chat(self.channel_name, community_channel=True).click()
        self.chat_1.hide_keyboard_if_shown()
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(120):
            self.errors.append("%s was not received in public chat after user unblock!" % message_unblocked)
            self.errors.verify_no_errors()

        self.home_1.just_fyi("Add blocked user to contacts again after removing(removed automatically when blocked)")
        chat_element = self.channel_1.chat_element_by_text(message_unblocked)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.channel_1.profile_add_to_contacts_button.click()
        self.home_2.just_fyi("Accept contact request after being unblocked")
        self.home_2.navigate_back_to_home_view()
        self.home_2.handle_contact_request(self.username_1)
        self.channel_1.profile_send_message_button.click_until_absense_of_element(
            desired_element=self.channel_1.profile_send_message_button, attempts=20, timeout=3)
        try:
            self.chat_1.send_message("piy")
            unblocked = True
        except TimeoutException:
            unblocked = False
            self.errors.append("Chat with unblocked user was not enabled after 1 minute")

        if unblocked:
            self.home_2.just_fyi("Check message in 1-1 chat after unblock")
            self.home_2.chats_tab.click()
            self.home_2.get_chat(self.username_1).click()
            self.chat_2.send_message(message_unblocked)
            try:
                self.chat_2.chat_element_by_text(message_unblocked).wait_for_status_to_be(expected_status='Delivered',
                                                                                          timeout=120)
                if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(30):
                    self.errors.append("Message was not received in 1-1 chat after user unblock!")
            except TimeoutException:
                self.errors.append('Message was not delivered after back up online.')

        self.errors.verify_no_errors()

    @marks.testrail_id(703086)
    def test_community_mark_all_messages_as_read(self):
        for home in self.home_1, self.home_2:
            home.navigate_back_to_home_view()
            home.communities_tab.click()
        self.home_2.get_chat(self.community_name, community=True).click()
        self.community_2.get_channel(self.channel_name).click()
        self.channel_2.send_message(self.text_message)
        community_1_element = self.community_1.get_chat(self.community_name, community=True)
        if not community_1_element.new_messages_grey_dot.is_element_displayed(90):
            self.errors.append('New messages counter is not shown in home > Community element')
        community_1_element.click()
        channel_1_element = self.community_1.get_chat(self.channel_name, community_channel=True)
        if not channel_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append("New messages counter is not shown in community channel element")
        self.community_1.click_system_back_button()
        mark_as_read_button = self.community_1.mark_all_messages_as_read_button
        self.home_1.community_floating_screen.wait_for_invisibility_of_element()
        community_1_element.long_press_until_element_is_shown(mark_as_read_button)
        mark_as_read_button.click()
        if community_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append(
                'Unread messages badge is shown in community element while there are no unread messages')
        community_1_element.click()
        if channel_1_element.new_messages_grey_dot.is_element_displayed():
            self.errors.append(
                "New messages badge is shown in community channel element while there are no unread messages")
        self.errors.verify_no_errors()


@pytest.mark.xdist_group(name="new_five_2")
@marks.new_ui_critical
class TestCommunityMultipleDeviceMergedTwo(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        self.device_1, self.device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.username_1, self.username_2 = "user_1", "user_2"
        self.loop.run_until_complete(run_in_parallel(((self.device_1.create_user, {'enable_notifications': True,
                                                                                   'username': self.username_1}),
                                                      (self.device_2.create_user, {'username': self.username_2}))))
        self.homes = self.home_1, self.home_2 = self.device_1.get_home_view(), self.device_2.get_home_view()
        self.public_key_2 = self.home_2.get_public_key_via_share_profile_tab()
        self.profile_1 = self.home_1.get_profile_view()
        [home.navigate_back_to_home_view() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)
        self.text_message = 'hello'

        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.home_1.navigate_back_to_home_view()

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = "open community"
        self.channel_name = 'general'
        self.home_1.create_community(community_type="open")
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)
        self.channel_1.send_message(self.text_message)

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.share_community(self.community_name, self.username_2)
        self.home_1.get_to_community_channel_from_home(self.community_name)

        self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.chat_2.send_message(self.text_message)
        self.chat_2.chat_element_by_text(self.community_name).view_community_button.click()
        self.community_2.join_community()
        self.channel_2 = self.community_2.get_channel(self.channel_name).click()

    @marks.testrail_id(702786)
    @marks.xfail(reason="Issue with username in PN, issue #6 in 15500")
    def test_community_mentions_push_notification(self):
        self.home_1.navigate_back_to_home_view()
        self.device_1.open_notification_bar()

        self.device_2.just_fyi("Invited member sends a message with a mention")
        self.channel_2.send_message("hi")
        self.channel_2.mention_user(self.username_1)
        self.channel_2.send_message_button.click()

        self.device_1.just_fyi("Admin gets push notification with the mention and tap it")
        message_received = False
        if self.home_1.get_pn(self.username_1):
            self.device_1.click_upon_push_notification_by_text(self.username_1)
            if not self.channel_1.chat_element_by_text(self.username_1).is_element_displayed():
                message_received = True
                if self.channel_1.chat_message_input.is_element_displayed():
                    self.errors.append("Message with the mention is not shown in the chat for the admin")
                else:
                    self.errors.append(
                        "Channel did not open by clicking on a notification with the mention for admin")
        else:
            self.errors.append("Push notification with the mention was not received by admin")

        self.channel_1.click_system_back_button()

        if message_received:
            self.channel_1.just_fyi("Set reaction for the message with a mention")
            self.channel_1.set_reaction(message=self.username_1, emoji="sad")
            try:
                self.channel_2.chat_element_by_text(self.username_1).emojis_below_message(
                    emoji="sad").wait_for_element_text(1)
            except (Failed, NoSuchElementException):
                self.errors.append("Message reaction is not shown for the sender")

        self.device_2.just_fyi("Sender edits the message with a mention")
        self.channel_2.chat_element_by_text(self.username_1).wait_for_sent_state()
        self.channel_2.chat_element_by_text(self.username_1).long_press_element_by_coordinate(rel_y=0)
        try:
            self.channel_2.element_by_translation_id("edit-message").click()
            for i in range(29, 32):
                self.channel_2.driver.press_keycode(i)
            self.channel_2.send_message_button.click()
            edited_message = self.username_1 + " abc"
            if not self.channel_2.chat_element_by_text(edited_message).is_element_displayed():
                self.errors.append("Edited message is not shown correctly for the sender")
            if not self.channel_1.chat_element_by_text(edited_message).is_element_displayed():
                self.errors.append("Edited message is not shown correctly for the (receiver) admin")
        except NoSuchElementException:
            self.errors.append("Can not edit a message with a mention")

        self.home_2.navigate_back_to_home_view()
        if not self.channel_1.chat_message_input.is_element_displayed():
            self.channel_1.navigate_back_to_home_view()
            self.home_1.communities_tab.click()
            self.home_1.get_chat(self.community_name, community=True).click()
            self.community_1.get_channel(self.channel_name).click()

        self.device_1.just_fyi("Admin sends a message with a mention")
        self.channel_1.mention_user(self.username_2)
        self.channel_1.send_message_button.click()
        self.device_2.just_fyi("Invited member gets push notification with the mention and tap it")
        self.device_2.open_notification_bar()
        push_notification_element = self.home_2.get_pn(self.username_2)
        if push_notification_element:
            push_notification_element.click()
            if not self.channel_2.chat_element_by_text(self.username_2).is_element_displayed():
                if self.channel_2.chat_message_input.is_element_displayed():
                    self.errors.append("Message with the mention is not shown in the chat for the invited member")
                else:
                    self.errors.append(
                        "Channel did not open by clicking on a notification with the mention for the invited member")
        else:
            self.errors.append("Push notification with the mention was not received by the invited member")
        self.errors.verify_no_errors()

    @marks.testrail_id(702809)
    def test_community_markdown_support(self):
        markdown = {
            'bold text in asterics': '**',
            'bold text in underscores': '__',
            'italic text in asteric': '*',
            'italic text in underscore': '_',
            'inline code': '`',
            'code blocks': '```',
            'quote reply (one row)': '>',
        }

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.jump_to_communities_home()
            community = home.get_chat(self.community_name, community=True).click()
            community.get_channel(self.channel_name).click()

        for message, symbol in markdown.items():
            self.home_1.just_fyi('Checking that "%s" is applied (%s) in community channel' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.channel_2.send_message(message_to_send)
            if not self.channel_2.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    '%s is not displayed with markdown in community channel for the sender (device 2) \n' % message)

            if not self.channel_1.chat_element_by_text(message).is_element_displayed():
                self.errors.append(
                    '%s is not displayed with markdown in community channel for the recipient (device 1) \n' % message)

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.chats_tab.click()
            home.recent_tab.click()

        self.home_1.get_chat(self.username_2).click()
        self.home_2.get_chat(self.username_1).click()

        for message, symbol in markdown.items():
            self.home_1.just_fyi('Checking that "%s" is applied (%s) in 1-1 chat' % (message, symbol))
            message_to_send = symbol + message + symbol if 'quote' not in message else symbol + message
            self.chat_1.send_message(message_to_send)
            if not self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    '%s is not displayed with markdown in 1-1 chat for the sender (device 1) \n' % message)

            if not self.chat_2.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    '%s is not displayed with markdown in 1-1 chat for the recipient (device 2) \n' % message)

        self.errors.verify_no_errors()

    @marks.testrail_id(702845)
    def test_community_leave(self):
        self.home_2.navigate_back_to_home_view()
        self.home_2.jump_to_communities_home()
        community = self.home_2.get_chat(self.community_name, community=True)
        community_to_leave = CommunityView(self.drivers[1])
        community.long_press_until_element_is_shown(community_to_leave.leave_community_button)
        community_to_leave.leave_community_button.click()
        community_to_leave.leave_community_button.click()
        if not community.is_element_disappeared():
            self.errors.append('Community is still shown in the list after leave')
        self.errors.verify_no_errors()

    @marks.testrail_id(702948)
    def test_community_hashtag_links_to_community_channels(self):
        for home in self.homes:
            home.navigate_back_to_home_view()
        self.home_2.jump_to_messages_home()
        self.home_1.jump_to_communities_home()

        self.home_1.just_fyi("Device 1 creates a closed community")
        self.home_1.create_community(community_type="closed")
        community_name = "closed community"
        self.community_1.share_community(community_name, self.username_2)

        self.home_2.just_fyi("Device 2 joins the community")
        self.home_2.get_chat(self.username_1).click()
        control_message_1_1_chat = "it is just a message text"
        self.chat_2.send_message(control_message_1_1_chat)
        self.chat_2.chat_element_by_text(community_name).view_community_button.click()
        self.community_2.join_community(open_community=False)

        self.home_1.just_fyi("Device 1 accepts the community request")
        self.home_1.jump_to_communities_home()
        try:
            self.home_1.notifications_unread_badge.wait_for_visibility_of_element(120)
        except TimeoutException:
            self.errors.append("Unread indicator is not shown in notifications on membership request")
        self.home_1.open_activity_center_button.click()
        reply_element = self.home_1.get_element_from_activity_center_view(self.username_2)
        reply_element.swipe_right_on_element()
        self.home_1.activity_notification_swipe_button.click()
        self.home_1.close_activity_centre.click()

        dogs_channel, cats_channel = "dogs", "cats"
        cats_message = "Where is a cat?"

        self.home_1.just_fyi("Device 1 sends a message in the cats channel")
        self.home_1.get_to_community_channel_from_home(community_name=community_name, channel_name=cats_channel)
        self.channel_1.send_message(cats_message)
        self.channel_1.navigate_back_to_home_view()
        self.home_1.communities_tab.click()
        self.home_1.get_to_community_channel_from_home(community_name=community_name, channel_name=dogs_channel)

        self.home_1.just_fyi("Device 1 sends a message with hashtag in the dogs channel")
        message_with_hashtag = "#cats"
        self.channel_1.send_message(message_with_hashtag)

        self.home_2.just_fyi("Device 2 clicks on the message with hashtag in the community channel")
        self.home_2.navigate_back_to_home_view()
        self.home_2.communities_tab.click()
        self.home_2.get_to_community_channel_from_home(community_name, dogs_channel)
        self.channel_2.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if not self.channel_2.chat_element_by_text(cats_message).is_element_displayed(30):
            self.errors.append("Receiver was not navigated to the cats channel")

        self.home_1.just_fyi("Device 1 clicks on the message with hashtag in the community channel")
        self.channel_1.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if not self.channel_1.chat_element_by_text(cats_message).is_element_displayed(30):
            self.errors.append("Sender was not navigated to the cats channel")

        for home in self.homes:
            home.navigate_back_to_home_view()
            home.chats_tab.click()

        self.home_2.just_fyi("Device 2 sends a message with hashtag in 1-1 chat")
        self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(message_with_hashtag)

        self.home_1.just_fyi("Device 1 clicks on the message with hashtag in 1-1 chat")
        self.home_1.get_chat(self.username_2).click()
        self.chat_1.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if self.chat_1.chat_element_by_text(control_message_1_1_chat).is_element_disappeared():
            self.errors.append("Receiver was navigated out of 1-1 chat")

        self.home_2.just_fyi("Device 2 clicks on the message with hashtag in 1-1 chat")
        self.chat_2.chat_element_by_text(message_with_hashtag).click_on_link_inside_message_body()
        if self.chat_2.chat_element_by_text(control_message_1_1_chat).is_element_disappeared():
            self.errors.append("Sender was navigated out of 1-1 chat")

        self.errors.verify_no_errors()
