import random
from datetime import timedelta

import emoji
import pytest
from dateutil import parser
from selenium.common.exceptions import NoSuchElementException

from tests import marks, test_dapp_name, test_dapp_url, run_in_parallel, common_password
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.chat_view import CommunityView
from views.sign_in_view import SignInView
from views.dbs.waku_backup import user as waku_user


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
        self.drivers[0].close_app()
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

        self.home = self.sign_in.create_user(username=self.username)
        self.home.communities_tab.click_until_presence_of_element(self.home.plus_community_button)
        self.community_name = self.home.get_random_chat_name()
        self.channel_name = 'general'
        self.community = self.home.create_community(name=self.community_name, description='test description')

        self.home.get_chat(self.community_name, community=True).click()
        community_view = self.home.get_community_view()
        self.channel = community_view.get_channel(self.channel_name).click()

    @marks.testrail_id(702846)
    def test_community_navigate_to_channel_when_relaunch(self):
        text_message = 'some_text'
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.communities_tab.double_click()
            self.home.get_chat(self.community_name, community=True).click()
            self.community.get_chat(self.channel_name).click()
        self.channel.send_message(text_message)
        self.channel.reopen_app()
        if not self.channel.chat_element_by_text(text_message).is_element_displayed(30):
            self.drivers[0].fail("Not navigated to channel view after reopening app")

    @marks.testrail_id(702742)
    def test_community_copy_and_paste_message_in_chat_input(self):
        message_texts = ['mmmeowesage_text', 'https://status.im']
        if not self.channel.chat_message_input.is_element_displayed():
            self.home.click_system_back_button_until_element_is_shown()
            self.home.get_to_community_channel_from_home(self.community_name)

        for message in message_texts:
            self.channel.send_message(message)
            self.channel.copy_message_text(message)
            actual_copied_text = self.channel.driver.get_clipboard_text()
            if actual_copied_text != message:
                self.errors.append('Message %s text was not copied in community channel, text in clipboard %s' % actual_copied_text)

        self.errors.verify_no_errors()

    @marks.testrail_id(703133)
    def test_restore_multiaccount_with_waku_backup_remove_switch(self):
        self.home.click_system_back_button_until_element_is_shown()
        profile = self.home.profile_button.click()
        profile.logout()
        self.sign_in.recover_access(passphrase=waku_user.seed, second_user=True)

        self.home.just_fyi("Restore user with predefined communities, check communities")
        self.home.communities_tab.click()
        for key in ['admin_open', 'member_open', 'admin_closed', 'member_closed']:
            if not self.home.element_by_text(waku_user.communities[key]).is_element_displayed(30):
                self.errors.append("%s was not restored from waku-backup!!" % key)
        # TODO: there is a bug when pending community sometimes restored as joined; needs investigation
        # self.home.opened_communities_tab.click()
        # if not self.home.element_by_text(waku_user.communities['member_pending']).is_element_displayed(30):
        #     self.errors.append("Pending community %s was not restored from waku-backup!" % waku_user.communities['member_pending'])

        self.home.just_fyi("Check contacts/blocked users")
        self.home.chats_tab.click()
        self.home.contacts_tab.click()
        for contact in waku_user.contacts:
            if not self.home.element_by_text(contact).is_element_displayed(30):
                self.errors.append("Contact %s was not restored from backup!" % contact)

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


@pytest.mark.xdist_group(name="new_one_2")
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
        self.public_key_2 = self.home_2.get_public_key()
        self.profile_1 = self.home_1.get_profile_view()
        [home.click_system_back_button_until_element_is_shown() for home in self.homes]
        [home.chats_tab.click() for home in self.homes]
        self.home_1.add_contact(self.public_key_2)
        self.home_2.handle_contact_request(self.username_1)
        self.text_message = 'hello'

        self.home_2.just_fyi("Send message to contact (need for blocking contact) test")
        self.chat_1 = self.home_1.get_chat(self.username_2).click()
        self.chat_1.send_message('hey')
        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(self.text_message)
        [home.click_system_back_button_until_element_is_shown() for home in self.homes]

        self.home_1.just_fyi("Open community to message")
        self.home_1.communities_tab.click()
        self.community_name = self.home_1.get_random_chat_name()
        self.channel_name = 'general'
        self.home_1.create_community(name=self.community_name, description='community to test', require_approval=False)
        self.channel_1 = self.home_1.get_to_community_channel_from_home(self.community_name)
        self.channel_1.send_message(self.text_message)

        self.community_1, self.community_2 = self.home_1.get_community_view(), self.home_2.get_community_view()
        self.community_1.send_invite_to_community(self.community_name, self.username_2)
        self.home_1.get_to_community_channel_from_home(self.community_name)

        self.chat_2 = self.home_2.get_chat(self.username_1).click()
        self.chat_2.element_by_text_part('View').click()
        self.community_2.join_community()
        self.channel_2 = self.community_2.get_channel(self.channel_name).click()

    @marks.testrail_id(702838)
    @marks.xfail(reason="blocked by 14797")
    def test_community_message_send_check_timestamps_sender_username(self):
        message = self.text_message
        sent_time_variants = self.channel_1.convert_device_time_to_chat_timestamp()
        timestamp = self.channel_1.chat_element_by_text(message).timestamp
        if sent_time_variants and timestamp:
            if timestamp not in sent_time_variants:
                self.errors.append("Timestamp is not shown, expected: '%s', in fact: '%s'" %
                                   (", ".join(sent_time_variants), timestamp))
        for channel in self.channel_1, self.channel_2:
            channel.verify_message_is_under_today_text(message, self.errors)
        if self.channel_2.chat_element_by_text(message).username.text != self.username_1:
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
        self.channel_2.delete_message_in_chat(message_to_delete_for_me, everyone=False)
        if not self.channel_2.chat_element_by_text(message_to_delete_for_me).is_element_disappeared(30):
            self.errors.append("Deleted for me message is shown in channel for the author of message")
        if not channel.element_by_translation_id('message-deleted-for-you').is_element_displayed(30):
            self.errors.append("System message about deletion for you is not displayed")
        if not self.channel_1.chat_element_by_text(message_to_delete_for_me).is_element_displayed(30):
            self.errors.append("Deleted for me message is deleted all channel members")
        self.errors.verify_no_errors()

    @marks.testrail_id(702859)
    def test_1_1_chat_image_send_reply(self):

        self.home_1.just_fyi('Send image in 1-1 chat from Gallery')
        image_description = 'description'
        self.channel_1.show_images_button.click()
        self.channel_1.allow_button.click_if_shown()
        self.channel_1.first_image_from_gallery.click()
        self.channel_1.images_confirm_selection_button.click()
        self.channel_1.chat_message_input.set_value(image_description)
        self.channel_1.send_message_button.click()
        self.channel_1.chat_message_input.click()
        self.channel_1.chat_element_by_text(image_description).image_in_message.click()
        self.channel_1.click_system_back_button()

        # TODO: options for image are still WIP; add case with edit description of image and after 15901 fix
        # self.home_2.just_fyi('check image, description and options for receiver')
        # self.channel_2.chat_element_by_text(image_description).image_in_message.click()
        # self.home_1.just_fyi('save image')
        # self.chat_1.save_image_button.click_until_presence_of_element(self.chat_1.show_images_button)
        # self.chat_1.show_images_button.click_until_presence_of_element(self.chat_1.image_from_gallery_button)
        # self.chat_1.image_from_gallery_button.click_until_presence_of_element(self.chat_1.recent_image_in_gallery)
        # if not self.chat_1.recent_image_in_gallery.is_element_displayed():
        #     self.errors.append('Saved image is not shown in Recent')
        # self.home_1.click_system_back_button(2)
        # self.home_2.just_fyi('check share and save options on opened image')
        # self.chat_2.image_message_in_chat.scroll_to_element(direction='up')
        # self.chat_2.image_message_in_chat.click_until_presence_of_element(self.chat_2.share_image_icon_button)
        # self.chat_2.share_image_icon_button.click()
        # self.chat_2.share_via_messenger()
        # if not self.chat_2.image_in_android_messenger.is_element_displayed():
        #     self.errors.append("Can't share image")
        # self.chat_2.click_system_back_button_until_element_is_shown(element=self.chat_2.save_image_icon_button)
        # self.chat_2.save_image_icon_button.click()
        # self.chat_2.show_images_button.click()
        # self.chat_2.allow_button.wait_and_click()
        #
        # if not self.chat_2.first_image_from_gallery.is_element_image_similar_to_template('saved.png'):
        #     self.errors.append("New picture was not saved!")
        #
        # self.channel_2.chat_element_by_text(image_description).image_in_message.save_new_screenshot_of_element('images_test.png')

        self.channel_2.just_fyi("Can reply to images")
        self.channel_2.quote_message(image_description)
        message_text = 'reply to image'
        self.channel_2.chat_message_input.send_keys(message_text)
        self.channel_2.send_message_button.click()
        chat_element_1 = self.channel_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=60) or chat_element_1.replied_message_text != 'Image':
            self.errors.append('Reply message was not received by the sender')

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
        giphy_url = 'https://giphy.com/gifs/this-is-fine-QMHoU66sBXqqLqYvGO'
        preview_urls = {'github_pr': {'url': 'https://github.com/status-im/status-mobile/pull/11707',
                                      'txt': 'Update translations by jinhojang6 · Pull Request #11707 · status-im/status-mobile',
                                      'subtitle': 'GitHub'},
                        'yotube_short': {
                            'url': 'https://youtu.be/Je7yErjEVt4',
                            'txt': 'Status, your gateway to Ethereum',
                            'subtitle': 'YouTube'},
                        'yotube_full': {
                            'url': 'https://www.youtube.com/watch?v=XN-SVmuJH2g&list=PLbrz7IuP1hrgNtYe9g6YHwHO6F3OqNMao',
                            'txt': 'Status & Keycard – Hardware-Enforced Security',
                            'subtitle': 'YouTube'},
                        'yotube_mobile': {
                            'url': 'https://m.youtube.com/watch?v=Je7yErjEVt4',
                            'txt': 'Status, your gateway to Ethereum',
                            'subtitle': 'YouTube'},

                        # twitter link is temporary removed from check as current xpath locator in message.preview_title is not applicable for this type of links
                        # 'twitter': {
                        #     'url': 'https://twitter.com/ethdotorg/status/1445161651771162627?s=20',
                        #     'txt': "We've rethought how we translate content, allowing us to translate",
                        #     'subtitle': 'Twitter'
                        # }
                        }

        self.home_1.just_fyi("Check enabling and sending first gif")
        self.channel_2.send_message(giphy_url)
        self.channel_2.element_by_translation_id("dont-ask").click()
        self.channel_1.element_by_text("Enable").wait_and_click()

        self.channel_1.element_by_translation_id("enable-all").wait_and_click()
        self.channel_1.click_system_back_button()
        if not self.channel_1.get_preview_message_by_text(giphy_url).preview_image:
            self.errors.append("No preview is shown for %s" % giphy_url)
        for key in preview_urls:
            self.home_2.just_fyi("Checking %s preview case" % key)
            data = preview_urls[key]
            self.channel_2.send_message(data['url'])
            message = self.channel_1.get_preview_message_by_text(data['url'])
            if message.preview_title:
                if data['txt'] not in message.preview_title.text:
                    self.errors.append("Title '%s' does not match expected" % message.preview_title.text)
            else:
                self.drivers[0].fail("No preview is shown!")
            if message.preview_subtitle:
                if message.preview_subtitle.text != data['subtitle']:
                    self.errors.append("Subtitle '%s' does not match expected" % message.preview_subtitle.text)
            else:
                self.drivers[0].fail("No preview title is shown!")

        self.home_2.just_fyi("Check if after do not ask again previews are not shown and no enable button appear")
        if self.channel_2.element_by_translation_id("enable").is_element_displayed():
            self.errors.append("Enable button is still shown after clicking on 'Don't ask again'")
        if self.channel_2.get_preview_message_by_text(giphy_url).preview_image:
            self.errors.append("Preview is shown for sender without permission")
        self.errors.verify_no_errors()

    @marks.testrail_id(702841)
    def test_community_unread_messages_badge(self):
        self.channel_1.jump_to_communities_home()
        message = 'test message'
        self.channel_2.send_message(message)
        self.home_1.just_fyi('Check new messages badge is shown for community')
        community_element_1 = self.home_1.get_chat(self.community_name, community=True)
        if not community_element_1.new_messages_community.is_element_displayed():
            self.errors.append('New message community badge is not shown')

        community_1 = community_element_1.click()
        channel_1_element = community_1.get_channel(self.channel_name)

        self.home_1.just_fyi('Check new messages badge is shown for community')
        if not community_element_1.new_messages_community.is_element_displayed():
            self.errors.append('New messages channel badge is not shown on channel')
        channel_1_element.click()
        self.errors.verify_no_errors()

    @marks.testrail_id(702894)
    def test_community_contact_block_unblock_offline(self):
        [home.jump_to_card_by_text('# %s' % self.channel_name) for home in self.homes]
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

        self.chat_1.just_fyi('messages from blocked user are hidden in public chat and close app')
        if self.chat_1.chat_element_by_text(message_to_disappear).is_element_displayed():
            self.errors.append("Messages from blocked user is not cleared in public chat ")
        self.chat_1.jump_to_messages_home()
        if self.home_1.element_by_text(self.username_2).is_element_displayed():
            self.errors.append("1-1 chat from blocked user is not removed!")
        self.chat_1.toggle_airplane_mode()

        self.home_2.just_fyi('send message to public chat while device 1 is offline')
        message_blocked, message_unblocked = "Message from blocked user", "Hurray! unblocked"
        self.channel_2.send_message(message_blocked)

        self.chat_1.just_fyi('check that new messages from blocked user are not delivered')
        self.chat_1.toggle_airplane_mode()
        self.home_1.jump_to_card_by_text('# %s' % self.channel_name)
        for message in message_to_disappear, message_blocked:
            if self.chat_1.chat_element_by_text(message).is_element_displayed(30):
                self.errors.append(
                    "'%s' from blocked user is fetched from offline in community channel" % message)

        self.chat_2.just_fyi('Unblock user and check that can see further messages')
        # TODO: still no blocked users in new UI
        profile_1 = self.home_1.get_profile_view()
        self.home_1.jump_to_messages_home()
        self.chat_1.profile_button.click()
        profile_1.contacts_button.wait_and_click()
        profile_1.blocked_users_button.wait_and_click()
        profile_1.element_by_text(self.username_2).click()
        self.chat_1.unblock_contact_button.click()
        self.chat_1.close_button.click()
        self.chat_1.click_system_back_button_until_element_is_shown()

        self.home_2.just_fyi("Check that can send message in community after unblock")
        [home.jump_to_card_by_text('# %s' % self.channel_name) for home in [self.home_1, self.home_2]]
        self.chat_2.send_message(message_unblocked)
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(30):
            self.errors.append("Message was not received in public chat after user unblock!")

        self.home_2.just_fyi("Add blocked user to contacts again after removing(removed automatically when blocked)")
        chat_element = self.channel_1.chat_element_by_text(message_unblocked)
        chat_element.find_element()
        chat_element.member_photo.click()
        self.channel_1.profile_add_to_contacts_button.click()
        self.home_2.jump_to_messages_home()
        self.home_2.handle_contact_request(self.username_1)
        self.channel_1.profile_send_message_button.wait_and_click()
        self.chat_1.send_message("piy")

        self.home_2.just_fyi("Check message in 1-1 chat after unblock")
        self.home_2.get_chat(self.username_1).click()
        self.chat_2.send_message(message_unblocked)
        if not self.chat_1.chat_element_by_text(message_unblocked).is_element_displayed(30):
            self.errors.append("Message was not received in 1-1 chat after user unblock!")
        self.errors.verify_no_errors()

    @marks.testrail_id(703086)
    def test_community_mark_all_messages_as_read(self):
        self.channel_1.jump_to_communities_home()
        self.home_2.jump_to_card_by_text('# %s' % self.channel_name)
        self.channel_2.send_message(self.text_message)
        community_1_element = self.community_1.get_chat(self.community_name)
        if not community_1_element.new_messages_public_chat.is_element_displayed(90):
            self.errors.append('New messages counter is not shown in home > Commmunity element')
        mark_as_read_button = self.community_1.mark_all_messages_as_read_button
        community_1_element.long_press_until_element_is_shown(mark_as_read_button)
        mark_as_read_button.click()
        if community_1_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in community channel while there are no unread messages')
    # TODO: there should be one more check for community channel, which is still not ready

    #     self.community_1.click_system_back_button_until_element_is_shown()
    #     community_1_element = self.home_1.get_chat(self.community_name, community=True)
    #     if community_1_element.new_messages_community.is_element_displayed():
    #         self.errors.append('New messages community badge is shown on community after marking messages as read')
        self.errors.verify_no_errors()

    @marks.testrail_id(702786)
    def test_community_mentions_push_notification(self):
        self.home_1.click_system_back_button_until_element_is_shown()
        if not self.channel_2.chat_message_input.is_element_displayed():
            self.channel_2.click_system_back_button_until_element_is_shown()
            self.home_2.communities_tab.click()
            self.home_2.get_chat(self.community_name, community=True).click()
            self.community_2.get_channel(self.channel_name).click()

        self.device_2.just_fyi("Invited member sends a message with a mention")
        self.channel_2.send_message("hi")
        self.channel_2.mention_user(self.username_1)
        self.channel_2.send_message_button.click()

        self.device_1.just_fyi("Admin gets push notification with the mention and tap it")
        self.device_1.open_notification_bar()
        if self.home_1.get_pn(self.username_1):
            self.device_1.click_upon_push_notification_by_text(self.username_1)
            if not self.channel_1.chat_element_by_text(self.username_1).is_element_displayed():
                if self.channel_1.chat_message_input.is_element_displayed():
                    self.errors.append("Message with the mention is not shown in the chat for the admin")
                else:
                    self.errors.append("Channel did not open by clicking on a notification with the mention for admin")
        else:
            self.errors.append("Push notification with the mention was not received by admin")

        # ToDo: this part is skipped because of an issue - sent messages stuck without any status for a long time
        # and can not be edited during that time
        # self.device_2.just_fyi("Sender edits the message with a mention")
        # self.channel_2.chat_element_by_text(self.username_1).wait_for_sent_state()
        # self.channel_2.chat_element_by_text(self.username_1).long_press_element_by_coordinate(rel_y=0)
        # try:
        #     self.channel_2.element_by_translation_id("edit-message").click()
        #     for i in range(29, 32):
        #         self.channel_2.driver.press_keycode(i)
        #     self.channel_2.send_message_button.click()
        #     edited_message = self.username_1 + " abc"
        #     if not self.channel_2.chat_element_by_text(edited_message).is_element_displayed():
        #         self.errors.append("Edited message is not shown correctly for the sender")
        #     if not self.channel_1.chat_element_by_text(edited_message).is_element_displayed():
        #         self.errors.append("Edited message is not shown correctly for the (receiver) admin")
        # except NoSuchElementException:
        #     self.errors.append("Can not edit a message with a mention")

        # ToDo: enable when https://github.com/status-im/status-mobile/issues/14956 is fixed
        # self.home_2.click_system_back_button_until_element_is_shown()
        # if not self.channel_1.chat_message_input.is_element_displayed():
        #     self.channel_1.click_system_back_button_until_element_is_shown()
        #     self.home_1.communities_tab.click()
        #     self.home_1.get_chat(self.community_name, community=True).click()
        #     self.community_1.get_channel(self.channel_name).click()
        #
        # self.device_1.just_fyi("Admin sends a message with a mention")
        # self.channel_1.mention_user(self.username_2)
        # self.channel_1.send_message_button.click()
        # self.device_2.just_fyi("Invited member gets push notification with the mention and tap it")
        # self.device_2.open_notification_bar()
        # if not self.home_2.get_pn(self.username_2):
        #     self.device_2.driver.fail("Push notification with the mention was not received by the invited member")
        # self.device_2.click_upon_push_notification_by_text(self.username_2)
        # if not self.channel_2.chat_element_by_text(self.username_2).is_element_displayed():
        #     if self.channel_2.chat_message_input.is_element_displayed():
        #         self.device_2.driver.fail("Message with the mention is not shown in the chat for the invited member")
        #     else:
        #         self.device_2.driver.fail(
        #             "Channel did not open by clicking on a notification with the mention for the invited member")
        self.errors.verify_no_errors()

    @marks.testrail_id(702845)
    def test_community_leave(self):
        self.home_2.jump_to_communities_home()
        community = self.home_2.get_chat(self.community_name, community=True)
        community_to_leave = CommunityView(self.drivers[1])
        community.long_press_until_element_is_shown(community_to_leave.leave_community_button)
        community_to_leave.leave_community_button.click()
        community_to_leave.leave_community_button.click()
        if community.is_element_displayed():
            self.errors.append('Community is still shown in the list after leave')
        self.errors.verify_no_errors()
