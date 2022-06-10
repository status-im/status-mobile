import random
from datetime import timedelta

import emoji
import pytest
from dateutil import parser

from tests import marks, test_dapp_name, test_dapp_url
from tests.base_test_case import create_shared_drivers, MultipleSharedDeviceTestCase
from views.sign_in_view import SignInView
from selenium.common.exceptions import NoSuchElementException


@pytest.mark.xdist_group(name="one_2")
@marks.critical
class TestPublicChatMultipleDeviceMerged(MultipleSharedDeviceTestCase):

    def prepare_devices(self):
        self.drivers, self.loop = create_shared_drivers(2)
        device_1, device_2 = SignInView(self.drivers[0]), SignInView(self.drivers[1])
        self.home_1, self.home_2 = device_1.create_user(), device_2.create_user()
        profile_1 = self.home_1.profile_button.click()
        self.username_1 = profile_1.default_username_text.text
        profile_1.home_button.click()
        self.pub_chat_delete_long_press = 'pub-chat'
        self.text_message = 'hello'
        self.home_1.join_public_chat(self.pub_chat_delete_long_press)
        [home.home_button.click() for home in (self.home_1, self.home_2)]
        self.public_chat_name = self.home_1.get_random_chat_name()
        self.chat_1 = self.home_1.join_public_chat(self.public_chat_name)
        self.chat_2 = self.home_2.join_public_chat(self.public_chat_name)
        self.chat_1.send_message(self.text_message)

    @marks.testrail_id(5313)
    def test_public_chat_message_send_check_timestamps_while_on_different_tab(self):
        message = self.text_message
        self.chat_2.dapp_tab_button.click()
        sent_time_variants = self.chat_1.convert_device_time_to_chat_timestamp()
        timestamp = self.chat_1.chat_element_by_text(message).timestamp_on_tap
        if sent_time_variants and timestamp:
            if timestamp not in sent_time_variants:
                self.errors.append("Timestamp is not shown, expected: '%s', in fact: '%s'" %
                                   (sent_time_variants.join(','), timestamp))
        self.chat_2.home_button.click(desired_view='chat')
        for chat in self.chat_1, self.chat_2:
            chat.verify_message_is_under_today_text(message, self.errors)
        if self.chat_2.chat_element_by_text(message).username.text != self.username_1:
            self.errors.append("Default username '%s' is not shown next to the received message" % self.username_1)
        self.errors.verify_no_errors()

    @marks.testrail_id(700734)
    def test_public_chat_message_edit(self):
        if not self.chat_2.chat_message_input.is_element_displayed():
            self.chat_2.home_button.click(desired_view='chat')
        message_before_edit, message_after_edit = self.text_message, "Message AFTER edit 2"
        self.chat_1.edit_message_in_chat(message_before_edit, message_after_edit)
        for chat in (self.chat_1, self.chat_2):
            if not chat.element_by_text_part("⌫ Edited").is_element_displayed(60):
                self.errors.append('No mark in message bubble about this message was edited')
        if not self.chat_2.element_by_text_part(message_after_edit).is_element_displayed(60):
            self.errors.append('Message is not edited')
        self.errors.verify_no_errors()

    @marks.testrail_id(700735)
    def test_public_chat_message_delete(self):
        message_to_delete = 'delete me, please'
        self.chat_1.send_message(message_to_delete)
        self.chat_1.delete_message_in_chat(message_to_delete)
        for chat in (self.chat_1, self.chat_2):
            if not chat.chat_element_by_text(message_to_delete).is_element_disappeared(30):
                self.errors.append("Deleted message is shown in chat view for public chat")
        self.errors.verify_no_errors()

    @marks.testrail_id(700719)
    def test_public_chat_emoji_send_copy_paste_reply(self):
        emoji_name = random.choice(list(emoji.EMOJI_UNICODE))
        emoji_unicode = emoji.EMOJI_UNICODE[emoji_name]
        emoji_message = emoji.emojize(emoji_name)
        self.chat_1.send_message(emoji_message)
        for chat in self.chat_1, self.chat_2:
            if not chat.chat_element_by_text(emoji_unicode).is_element_displayed(30):
                self.errors.append('Message with emoji was not sent or received in public chat')

        self.chat_1.just_fyi("Can copy and paste emojis")
        self.chat_1.element_by_text_part(emoji_unicode).long_press_element()
        self.chat_1.element_by_text('Copy').click()
        self.chat_1.chat_message_input.paste_text_from_clipboard()
        if self.chat_1.chat_message_input.text != emoji_unicode:
            self.errors.append('Emoji message was not copied')

        self.chat_1.just_fyi("Can reply to emojis")
        self.chat_2.quote_message(emoji_unicode)
        message_text = 'test message'
        self.chat_2.chat_message_input.send_keys(message_text)
        self.chat_2.send_message_button.click()
        chat_element_1 = self.chat_1.chat_element_by_text(message_text)
        if not chat_element_1.is_element_displayed(sec=10) or chat_element_1.replied_message_text != emoji_unicode:
            self.errors.append('Reply message was not received by the sender')
        self.errors.verify_no_errors()

    @marks.testrail_id(5360)
    def test_public_chat_unread_messages_counter(self):
        self.chat_1.send_message('пиу')
        home_1 = self.chat_1.home_button.click()
        message = 'test message'
        self.chat_2.send_message(message)
        if not self.chat_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')
        chat_element = home_1.get_chat('#' + self.public_chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')
        self.errors.verify_no_errors()

    @marks.testrail_id(700718)
    def test_public_chat_unread_messages_counter_for_mention_relogin(self):
        message = 'test message2'
        [chat.get_back_to_home_view() for chat in (self.chat_1, self.chat_2)]
        chat_element = self.home_1.get_chat('#' + self.public_chat_name)
        self.home_2.get_chat('#' + self.public_chat_name).click()
        self.chat_2.select_mention_from_suggestion_list(self.username_1, self.username_1[:2])
        self.chat_2.send_message_button.click()
        chat_element.new_messages_counter.wait_for_element(30)
        chat_element.new_messages_counter.wait_for_element_text("1", 60)
        chat_element.click()
        self.home_1.home_button.double_click()
        if self.home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while there are no unread messages')
        [home.get_chat('#' + self.public_chat_name).click() for home in (self.home_1, self.home_2)]
        self.chat_1.send_message(message)
        self.chat_2.chat_element_by_text(message).wait_for_element(20)
        self.home_2.reopen_app()
        chat_element = self.home_2.get_chat('#' + self.public_chat_name)
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.drivers[0].fail('New messages counter is shown after relogin')
        self.errors.verify_no_errors()

    @marks.testrail_id(5319)
    def test_public_chat_delete_chat_long_press(self):
        [chat.get_back_to_home_view() for chat in (self.chat_1, self.chat_2)]
        self.home_1.delete_chat_long_press('#%s' % self.pub_chat_delete_long_press)
        self.home_2.just_fyi("Send message to deleted chat")
        self.deleted_chat_2 = self.home_2.join_public_chat(self.pub_chat_delete_long_press)
        self.deleted_chat_2.send_message()
        if self.home_1.get_chat_from_home_view('#%s' % self.pub_chat_delete_long_press).is_element_displayed():
            self.drivers[0].fail('Deleted public chat reappears after sending message to it')
        self.home_1.reopen_app()
        if self.home_1.get_chat_from_home_view('#%s' % self.pub_chat_delete_long_press).is_element_displayed():
            self.drivers[0].fail('Deleted public chat reappears after relogin')

    @marks.testrail_id(700736)
    def test_public_chat_link_send_open(self):
        [chat.get_back_to_home_view() for chat in (self.chat_1, self.chat_2)]
        [home.get_chat('#' + self.public_chat_name).click() for home in (self.home_1, self.home_2)]
        url_message = 'http://status.im'
        self.chat_2.send_message(url_message)
        self.chat_1.element_starts_with_text(url_message, 'button').click()
        web_view = self.chat_1.open_in_status_button.click()
        if not web_view.element_by_text('Private, Secure Communication').is_element_displayed(60):
            self.drivers[0].fail('URL was not opened from public chat')

    @marks.testrail_id(700737)
    def test_public_chat_links_with_previews_github_youtube_twitter_gif_send_enable(self):
        [chat.home_button.double_click() for chat in (self.chat_1, self.chat_2)]
        [home.get_chat('#' + self.public_chat_name).click() for home in (self.home_1, self.home_2)]
        giphy_url = 'https://giphy.com/gifs/this-is-fine-QMHoU66sBXqqLqYvGO'
        preview_urls = {'github_pr': {'url': 'https://github.com/status-im/status-react/pull/11707',
                                      'txt': 'Update translations by jinhojang6 · Pull Request #11707 · status-im/status-react',
                                      'subtitle': 'GitHub'},
                        'yotube': {
                            'url': 'https://www.youtube.com/watch?v=XN-SVmuJH2g&list=PLbrz7IuP1hrgNtYe9g6YHwHO6F3OqNMao',
                            'txt': 'Status & Keycard – Hardware-Enforced Security',
                            'subtitle': 'YouTube'},
                        'twitter': {
                            'url': 'https://twitter.com/ethdotorg/status/1445161651771162627?s=20',
                            'txt': "We've rethought how we translate content, allowing us to translate",
                            'subtitle': 'Twitter'
                        }}

        self.home_1.just_fyi("Check enabling and sending first gif")
        self.chat_2.send_message(giphy_url)
        self.chat_2.element_by_translation_id("dont-ask").click()
        self.chat_1.element_by_translation_id("enable").wait_and_click()
        self.chat_1.element_by_translation_id("enable-all").wait_and_click()
        self.chat_1.close_modal_view_from_chat_button.click()
        if not self.chat_1.get_preview_message_by_text(giphy_url).preview_image:
            self.errors.append("No preview is shown for %s" % giphy_url)
        for key in preview_urls:
            self.home_2.just_fyi("Checking %s preview case" % key)
            data = preview_urls[key]
            self.chat_2.send_message(data['url'])
            message = self.chat_1.get_preview_message_by_text(data['url'])
            if data['txt'] not in message.preview_title.text:
                self.errors.append("Title '%s' does not match expected" % message.preview_title.text)
            if message.preview_subtitle.text != data['subtitle']:
                self.errors.append("Subtitle '%s' does not match expected" % message.preview_subtitle.text)

        self.home_2.just_fyi("Check if after do not ask again previews are not shown and no enable button appear")
        if self.chat_2.element_by_translation_id("enable").is_element_displayed():
            self.errors.append("Enable button is still shown after clicking on 'Den't ask again'")
        if self.chat_2.get_preview_message_by_text(giphy_url).preview_image:
            self.errors.append("Preview is shown for sender without permission")
        self.errors.verify_no_errors()

    @marks.testrail_id(6270)
    def test_public_chat_mark_all_messages_as_read(self):
        [chat.get_back_to_home_view() for chat in (self.chat_1, self.chat_2)]
        self.home_2.get_chat('#' + self.public_chat_name).click()
        self.chat_2.send_message(self.text_message)
        if not self.home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is not shown on Home button')
        chat_element = self.home_1.get_chat('#' + self.public_chat_name)
        if not chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('New messages counter is not shown in public chat')
        chat_element.long_press_element()
        self.home_1.mark_all_messages_as_read_button.click()
        self.home_1.home_button.double_click()
        if self.home_1.home_button.public_unread_messages.is_element_displayed():
            self.errors.append('New messages public chat badge is shown on Home button after marking messages as read')
        if chat_element.new_messages_public_chat.is_element_displayed():
            self.errors.append('Unread messages badge is shown in public chat while while there are no unread messages')

        self.errors.verify_no_errors()


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
                             # TODO: blocked with 11161 (rechecked 23.11.21, valid)
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
