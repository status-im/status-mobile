from datetime import datetime
import dateutil.parser

from selenium.common.exceptions import TimeoutException, NoSuchElementException

from tests import common_password
from views.base_element import BaseButton, BaseEditBox, BaseText, BaseElement
from views.base_view import BaseView, ProgressBar
from views.profile_view import ProfilePictureElement, ProfileAddressText


class ChatMessageInput(BaseEditBox):
    def __init__(self, driver):
        super(ChatMessageInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-message-input')


class TinyReplyIconInMessageInput(BaseElement):
    def __init__(self, driver):
        super(TinyReplyIconInMessageInput, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('tiny-reply-icon')


class QuoteUsernameInMessageInput(BaseText):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.view.ViewGroup[@content-desc='cancel-message-reply']/"
                                                   "..//android.widget.TextView[1]")


class CancelReplyButton(BaseEditBox):
    def __init__(self, driver):
        super(CancelReplyButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('cancel-message-reply')


class AddToContacts(BaseButton):
    def __init__(self, driver):
        super(AddToContacts, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('add-to-contacts-button')


class RemoveFromContactsButton(BaseButton):
    def __init__(self, driver):
        super(RemoveFromContactsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('in-contacts-button')


class AddGroupChatMembersButton(BaseButton):
    def __init__(self, driver):
        super(AddGroupChatMembersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Add members']")

    def navigate(self):
        from views.contacts_view import ContactsView
        return ContactsView(self.driver)


class UserNameText(BaseText):
    def __init__(self, driver):
        super(UserNameText, self).__init__(driver)
        self.locator = \
            self.Locator.accessibility_id('chat-name-text')

class SendCommand(BaseButton):
    def __init__(self, driver):
        super(SendCommand, self).__init__(driver)
        self.locator = self.Locator.text_selector('Send transaction')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)

    def click(self):
        self.wait_for_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class RequestCommand(BaseButton):
    def __init__(self, driver):
        super(RequestCommand, self).__init__(driver)
        self.locator = self.Locator.text_selector('Request transaction')

    def navigate(self):
        from views.send_transaction_view import SendTransactionView
        return SendTransactionView(self.driver)

    def click(self):
        self.wait_for_element().click()
        self.driver.info('Tap on %s' % self.name)
        return self.navigate()


class ChatMenuButton(BaseButton):
    def __init__(self, driver):
        super(ChatMenuButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('chat-menu-button')


class MembersButton(BaseButton):
    def __init__(self, driver):
        super(MembersButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('(//android.view.ViewGroup[@content-desc="action"])[1]')


class DeleteChatButton(BaseButton):
    def __init__(self, driver):
        super(DeleteChatButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Delete chat"]')


class ClearHistoryButton(BaseButton):
    def __init__(self, driver):
        super(ClearHistoryButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Clear history"]')


class ReplyMessageButton(BaseButton):
    def __init__(self, driver):
        super(ReplyMessageButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Reply")

class SaveImageButton(BaseButton):
    def __init__(self, driver):
        super(SaveImageButton, self).__init__(driver)
        self.locator = self.Locator.text_selector("Save")


class ImageInRecentInGalleryElement(BaseElement):
    def __init__(self, driver):
        super(ImageInRecentInGalleryElement, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[contains(@resource-id,"thumbnail")]')

class ProfileDetailsOtherUser(BaseButton):
    def __init__(self, driver):
        super(ProfileDetailsOtherUser, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('profile-public-key')


class ShareChatButton(BaseButton):
    def __init__(self, driver):
        super(ShareChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('share-chat-button')


class GroupInfoButton(BaseButton):

    def __init__(self, driver):
        super(GroupInfoButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="Group info"]')

    def navigate(self):
        return GroupChatInfoView(self.driver)

    def click(self):
        self.wait_for_element().click()
        return self.navigate()


class LeaveChatButton(BaseButton):
    def __init__(self, driver):
        super(LeaveChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('leave-chat-button')


class ClearButton(BaseButton):
    def __init__(self, driver):
        super(ClearButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="CLEAR"]')


class BlockContactButton(BaseButton):
    def __init__(self, driver):
        super(BlockContactButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('block-contact-confirm')


class UnblockContactButton(BaseButton):
    def __init__(self, driver):
        super(UnblockContactButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('unblock-contact')


class LeaveButton(BaseButton):
    def __init__(self, driver):
        super(LeaveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="LEAVE"]')


class ChatSettings(BaseButton):
    def __init__(self, driver):
        super(ChatSettings, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Settings']")


class UserOptions(BaseButton):
    def __init__(self, driver):
        super(UserOptions, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('options')


class RemoveButton(BaseButton):
    def __init__(self, driver):
        super(RemoveButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//android.widget.TextView[@text='Remove']")


class OpenInStatusButton(BaseButton):
    def __init__(self, driver):
        super(OpenInStatusButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='Open in Status']")

    def navigate(self):
        from views.web_views.base_web_view import BaseWebView
        return BaseWebView(self.driver)


class CommandsButton(BaseButton):
    def __init__(self, driver):
        super(CommandsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('show-extensions-icon')


class ShowStickersButton(BaseButton):
    def __init__(self, driver):
        super(ShowStickersButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('show-stickers-icon')


class GetStickers(BaseButton):
    def __init__(self, driver):
        super(GetStickers, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[contains(@text, "Get Stickers")]')


class StickerIcon(BaseButton):
    def __init__(self, driver):
        super(StickerIcon, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('sticker-icon')

class ShowImagesButton(BaseButton):
    def __init__(self, driver):
        super(ShowImagesButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('show-photo-icon')


class TakePhotoButton(BaseButton):
    def __init__(self, driver):
        super(TakePhotoButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('take-picture')

class ImageFromGalleryButton(BaseButton):
    def __init__(self, driver):
        super(ImageFromGalleryButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('open-gallery')

class FirstElementFromGalleryButton(BaseButton):
    def __init__(self, driver):
        super(FirstElementFromGalleryButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="open-gallery"]/../following-sibling::android.view.ViewGroup[1]')

class ViewProfileButton(BaseButton):
    def __init__(self, driver):
        super(ViewProfileButton, self).__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="View profile"]')


class NoMessagesInChatText(BaseText):
    def __init__(self, driver):
        super(NoMessagesInChatText, self).__init__(driver)
        self.locator = self.Locator.text_part_selector(
            'Any messages you send here are encrypted and can only be read by you and')


class ProfileSendMessageButton(BaseButton):
    def __init__(self, driver):
        super(ProfileSendMessageButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('start-conversation-button')


class ProfileSendTransactionButton(BaseButton):
    def __init__(self, driver):
        super(ProfileSendTransactionButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('send-transaction-button')


class ProfileBlockContactButton(BaseButton):
    def __init__(self, driver):
        super(ProfileBlockContactButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('block-contact')

class ProfileAddToContactsButton(BaseButton):
    def __init__(self, driver):
        super(ProfileAddToContactsButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('add-to-contacts-button')


class JoinChatButton(BaseButton):
    def __init__(self, driver):
        super(JoinChatButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('Join group')


class DeclineChatButton(BaseButton):
    def __init__(self, driver):
        super(DeclineChatButton, self).__init__(driver)
        self.locator = self.Locator.text_part_selector('Decline invitation')


class RemoveFromChatButton(BaseButton):
    def __init__(self, driver):
        super(RemoveFromChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('remove-from-chat')


class MakeAdminButton(BaseButton):
    def __init__(self, driver):
        super(MakeAdminButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id('make-admin')


class ChatElementByText(BaseText):
    def __init__(self, driver, text):
        super(ChatElementByText, self).__init__(driver)
        self.message_text = text
        self.message_locator = "//*[starts-with(@text,'%s')]" % text
        self.locator = self.Locator.xpath_selector(
            self.message_locator + "/ancestor::android.view.ViewGroup[@content-desc='chat-item']")

    def find_element(self):
        self.driver.info("Looking for message with text '%s'" % self.message_text)
        for _ in range(2):
            try:
                return super(ChatElementByText, self).find_element()
            except NoSuchElementException:
                ChatView(self.driver).reconnect()

    @property
    def status(self):
        class StatusText(BaseText):
            def __init__(self, driver, parent_locator: str):
                super(StatusText, self).__init__(driver)
                text = "//android.widget.TextView[@text='Not sent. Tap for options']"
                self.locator = self.Locator.xpath_selector(parent_locator + text)

        return StatusText(self.driver, self.locator.value).wait_for_element(10)

    @property
    def image_in_reply(self):
        class ImageInReply(BaseElement):
            def __init__(self, driver, parent_locator):
                super(ImageInReply, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(
                    parent_locator + "//android.widget.ImageView")
        try:
            return ImageInReply(self.driver, self.locator.value)
        except NoSuchElementException:
            return ''

    @property
    def timestamp_message(self):
        class TimeStampText(BaseText):
            def __init__(self, driver, parent_locator: str):
                super(TimeStampText, self).__init__(driver)
                text = "//*[1]/*[1]/*[6]"
                self.locator = self.Locator.xpath_selector(parent_locator + text)

        return TimeStampText(self.driver, self.locator.value)

    @property
    def progress_bar(self):
        return ProgressBar(self.driver, self.locator.value)

    @property
    def member_photo(self):
        class MemberPhoto(BaseButton):
            def __init__(self, driver, parent_locator):
                super(MemberPhoto, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "//*[@content-desc='member-photo']")

        return MemberPhoto(self.driver, self.locator.value)

    @property
    def username(self):
        class Username(BaseText):
            def __init__(self, driver, parent_locator):
                super(Username, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "/*[2]/android.widget.TextView")

        return Username(self.driver, self.locator.value)

    @property
    def send_request_button(self):
        class SendRequestButton(BaseButton):
            def __init__(self, driver, parent_locator):
                super(SendRequestButton, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + '//*[@text="Send"]')

        return SendRequestButton(self.driver, self.locator.value)

    @property
    def transaction_status(self):
        class TransactionStatus(BaseText):
            def __init__(self, driver, parent_locator):
                super(TransactionStatus, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "/*[1]/*[1]/*[5]/android.widget.TextView")

        return TransactionStatus(self.driver, self.locator.value)

    def contains_text(self, text, wait_time=5) -> bool:
        element = BaseText(self.driver)
        element.locator = element.Locator.xpath_selector(
            self.locator.value + "//android.view.ViewGroup//android.widget.TextView[contains(@text,'%s')]" % text)
        return element.is_element_displayed(wait_time)

    @property
    def accept_and_share_address(self):
        class AcceptAndShareAddress(BaseButton):
            def __init__(self, driver, parent_locator):
                super(AcceptAndShareAddress, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "//*[@text='Accept and share address']")

            def navigate(self):
                from views.send_transaction_view import SendTransactionView
                return SendTransactionView(self.driver)

            def click(self):
                self.wait_for_element().click()
                self.driver.info('Tap on %s' % self.name)
                return self.navigate()

        return AcceptAndShareAddress(self.driver, self.locator.value)

    @property
    def decline_transaction(self):
        class DeclineTransaction(BaseButton):
            def __init__(self, driver, parent_locator):
                super(DeclineTransaction, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "//*[@text='Decline']")

            def click(self):
                self.wait_for_element().click()
                self.driver.info('Tap on %s' % self.name)
                return self.navigate()

        return DeclineTransaction(self.driver, self.locator.value)

    @property
    def sign_and_send(self):
        class SignAndSend(BaseButton):
            def __init__(self, driver, parent_locator):
                super(SignAndSend, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(parent_locator + "//*[@text='Sign and send']")

            def navigate(self):
                from views.send_transaction_view import SendTransactionView
                return SendTransactionView(self.driver)

            def click(self):
                self.wait_for_element().click()
                self.driver.info('Tap on %s' % self.name)
                return self.navigate()

        return SignAndSend(self.driver, self.locator.value)

    @property
    def replied_message_text(self):
        class RepliedMessageText(BaseText):
            def __init__(self, driver, parent_locator):
                super(RepliedMessageText, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(
                    parent_locator + "/preceding-sibling::*[1]/android.widget.TextView[2]")
        try:
            return RepliedMessageText(self.driver, self.message_locator).text
        except NoSuchElementException:
            return ''

    @property
    def replied_to_username_text(self):
        class RepliedToUsernameText(BaseText):
            def __init__(self, driver, parent_locator):
                super(RepliedToUsernameText, self).__init__(driver)
                self.locator = self.Locator.xpath_selector(
                    parent_locator + "/preceding-sibling::*[1]/android.widget.TextView[1]")
        try:
            return RepliedToUsernameText(self.driver, self.message_locator).text
        except NoSuchElementException:
            return ''


class EmptyPublicChatMessage(BaseText):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.text_part_selector("It's been quite here")


class ChatItem(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="chat-item"]')

class ImageChatItem(BaseElement):
    def __init__(self, driver):
        super().__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@content-desc="chat-item"]//android.widget.ImageView')


class HistoryTimeMarker(BaseText):
    def __init__(self, driver, marker='Today'):
        super().__init__(driver)
        self.locator = self.Locator.xpath_selector('//*[@text="%s"]' % marker)


class UsernameOptions(BaseButton):
    def __init__(self, driver, username):
        super(UsernameOptions, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/..//*[@content-desc='menu-option']" % username)

    def navigate(self):
        return ChatView(self.driver)

    def click(self):
        self.wait_for_element().click()
        return self.navigate()


class UserNameInGroupInfo(BaseButton):
    def __init__(self, driver, username):
        super(UserNameInGroupInfo, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']" % username)


class AdminUser(BaseButton):
    def __init__(self, driver, username):
        super(AdminUser, self).__init__(driver)
        self.locator = self.Locator.xpath_selector("//*[@text='%s']/..//*[@text='Admin']" % username)


class EditGroupChatButton(BaseButton):
    def __init__(self, driver):
        super(EditGroupChatButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("edit-button")


class EditGroupChatEditBox(BaseEditBox):
    def __init__(self, driver):
        super(EditGroupChatEditBox, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("new-chat-name")


class DoneButton(BaseButton):
    def __init__(self, driver):
        super(DoneButton, self).__init__(driver)
        self.locator = self.Locator.accessibility_id("done")

class GroupChatInfoView(BaseView):
    def __init__(self, driver):
        super(GroupChatInfoView, self).__init__(driver)
        self.add_members = AddGroupChatMembersButton(self.driver)

    def get_username_options(self, username: str):
        return UsernameOptions(self.driver, username)

    def user_admin(self, username: str):
        return AdminUser(self.driver, username)

    def get_user_from_group_info(self, username: str):
        return UserNameInGroupInfo(self.driver, username)




class ChatView(BaseView):
    def __init__(self, driver):
        super(ChatView, self).__init__(driver)

        self.chat_message_input = ChatMessageInput(self.driver)
        self.tiny_reply_icon_in_message_input = TinyReplyIconInMessageInput(self.driver)
        self.quote_username_in_message_input = QuoteUsernameInMessageInput(self.driver)
        self.cancel_reply_button = CancelReplyButton(self.driver)
        self.add_to_contacts = AddToContacts(self.driver)
        self.remove_from_contacts = RemoveFromContactsButton(self.driver)
        self.user_name_text = UserNameText(self.driver)
        self.no_messages_in_chat = NoMessagesInChatText(self.driver)
        self.empty_public_chat_message = EmptyPublicChatMessage(self.driver)
        self.chat_item = ChatItem(self.driver)

        self.commands_button = CommandsButton(self.driver)
        self.send_command = SendCommand(self.driver)
        self.request_command = RequestCommand(self.driver)

        self.show_stickers_button = ShowStickersButton(self.driver)
        self.get_stickers = GetStickers(self.driver)
        self.sticker_icon = StickerIcon(self.driver)

        # Images
        self.show_images_button = ShowImagesButton(self.driver)
        self.take_photo_button = TakePhotoButton(self.driver)
        self.image_from_gallery_button = ImageFromGalleryButton(self.driver)
        self.first_image_from_gallery = FirstElementFromGalleryButton(self.driver)
        self.image_chat_item = ImageChatItem(self.driver)
        self.save_image_button = SaveImageButton(self.driver)
        self.recent_image_in_gallery = ImageInRecentInGalleryElement(self.driver)



        self.chat_options = ChatMenuButton(self.driver)
        self.members_button = MembersButton(self.driver)
        self.delete_chat_button = DeleteChatButton(self.driver)
        self.clear_history_button = ClearHistoryButton(self.driver)
        self.reply_message_button = ReplyMessageButton(self.driver)
        self.share_chat_button = ShareChatButton(self.driver)
        self.clear_button = ClearButton(self.driver)
        self.block_contact_button = BlockContactButton(self.driver)
        self.unblock_contact_button = UnblockContactButton(self.driver)

        # Group chats
        self.group_info = GroupInfoButton(self.driver)
        self.leave_chat_button = LeaveChatButton(self.driver)
        self.leave_button = LeaveButton(self.driver)
        self.join_chat_button = JoinChatButton(self.driver)
        self.decline_invitation_button = DeclineChatButton(self.driver)
        self.remove_user_button = RemoveFromChatButton(self.driver)
        self.make_admin_button = MakeAdminButton(self.driver)
        self.edit_group_chat_name_button = EditGroupChatButton(self.driver)
        self.edit_group_chat_name_edit_box = EditGroupChatEditBox(self.driver)
        self.done_button = DoneButton(self.driver)

        self.chat_settings = ChatSettings(self.driver)
        self.view_profile_button = ViewProfileButton(self.driver)
        self.user_options = UserOptions(self.driver)
        self.remove_button = RemoveButton(self.driver)

        self.open_in_status_button = OpenInStatusButton(self.driver)

        # Contact's profile
        self.contact_profile_picture = ProfilePictureElement(self.driver)
        self.profile_send_message = ProfileSendMessageButton(self.driver)
        self.profile_send_transaction = ProfileSendTransactionButton(self.driver)
        self.profile_address_text = ProfileAddressText(self.driver)
        self.profile_block_contact = ProfileBlockContactButton(self.driver)
        self.profile_add_to_contacts = ProfileAddToContactsButton(self.driver)
        self.profile_details = ProfileDetailsOtherUser(self.driver)

    def delete_chat(self):
        self.chat_options.click()
        self.delete_chat_button.click()
        self.delete_button.click()

    def leave_chat(self):
        self.chat_options.click()
        self.leave_chat_button.click()
        self.leave_button.click()

    def clear_history(self):
        self.chat_options.click()
        self.clear_history_button.click()
        self.clear_button.click()


    def leave_chat_via_group_info(self):
        self.chat_options.click()
        self.group_info.click()
        self.leave_chat_button.click()
        self.leave_button.click()

    def rename_chat_via_group_info(self, new_chat_name):
        self.chat_options.click()
        self.group_info.click()
        self.edit_group_chat_name_button.click()
        self.edit_group_chat_name_edit_box.set_value(new_chat_name)
        self.done_button.click()

    def send_transaction_in_group_chat(self, amount, password, recipient):
        self.commands_button.click()
        self.send_command.click()
        self.find_full_text(recipient['username']).click()
        self.send_as_keyevent(amount)
        self.send_message_button.click()

        send_transaction_view = self.get_send_transaction_view()
        self.send_message_button.click_until_presence_of_element(send_transaction_view.sign_transaction_button)
        send_transaction_view.sign_transaction(password)
        send_transaction_view.find_full_text(amount)
        self.find_full_text('to  ' + recipient['username'], 10)

    def add_members_to_group_chat(self, user_names_to_add: list):
        self.chat_options.click()
        group_info_view = self.group_info.click()
        add_members_view = group_info_view.add_members.click()
        for user_name in user_names_to_add:
            user_contact = add_members_view.get_username_checkbox(user_name)
            user_contact.scroll_to_element()
            user_contact.click()
        add_members_view.add_button.click()

    def get_user_options(self, username: str):
        self.chat_options.click()
        group_info_view = self.group_info.click()
        group_info_view.get_username_options(username).click()
        return self

    def request_transaction_in_1_1_chat(self, asset, amount):
        self.commands_button.click()
        self.request_command.click()
        self.asset_by_name(asset).click()
        self.send_as_keyevent(amount)
        self.send_message_button.click()

    def chat_element_by_text(self, text):
        self.driver.info("Looking for a message by text: '%s'" % text)
        return ChatElementByText(self.driver, text)

    def verify_message_is_under_today_text(self, text, errors):
        message_element = self.chat_element_by_text(text)
        message_element.wait_for_visibility_of_element()
        message_location = message_element.find_element().location['y']
        today_text_element = self.element_by_text('Today').find_element()
        today_location = today_text_element.location['y']
        today_height = today_text_element.size['height']
        if message_location < today_location + today_height:
            errors.append("Message '%s' is not under 'Today' text" % text)

    def send_message(self, message: str = 'test message'):
        self.chat_message_input.send_keys(message)
        self.send_message_button.click()

    def quote_message(self, message = str):
        self.element_by_text_part(message).long_press_element()
        self.reply_message_button.click()

    def view_profile_long_press(self, message = str):
        self.chat_element_by_text(message).long_press_element()
        self.view_profile_button.click()
        self.profile_block_contact.wait_for_visibility_of_element(5)

    def move_to_messages_by_time_marker(self, marker='Today'):
        self.driver.info("Moving to messages by time marker: '%s'" % marker)
        HistoryTimeMarker(self.driver, marker).scroll_to_element(depth=50, direction='up')

    def install_sticker_pack_by_name(self, pack_name: str):
        element = BaseButton(self.driver)
        element.locator = element.Locator.xpath_selector(
            "//*[@content-desc='sticker-pack-name'][@text='%s']/..//*[@content-desc='sticker-pack-price']" % pack_name)
        element.scroll_to_element()
        element.click()
        element.wait_for_invisibility_of_element()

    def block_contact(self):
        self.profile_block_contact.click()
        self.block_contact_button.click()

    def convert_device_time_to_chat_timestamp(self):
        sent_time_object = dateutil.parser.parse(self.driver.device_time)
        timestamp = datetime.strptime("%s:%s" % (sent_time_object.hour, sent_time_object.minute), '%H:%M').strftime("%I:%M %p")
        timestamp = timestamp[1:] if timestamp[0] == '0' else timestamp
        return timestamp

    # Group chat system messages
    @staticmethod
    def leave_system_message(username):
        return "%s left the group" % username

    @staticmethod
    def has_been_made_admin_system_message(admin, new_admin):
        return "%s has made %s admin" % (admin, new_admin)

    @staticmethod
    def create_system_message(admin, chat_name):
        return '%s created the group %s' % (admin, chat_name)

    @staticmethod
    def invite_system_message(admin, invited_user):
        return '%s has invited %s' % (admin, invited_user)

    @staticmethod
    def join_system_message(username):
        return '%s joined the group' % username

    @staticmethod
    def create_for_admin_system_message(chat_name):
        return 'You created the group %s' % chat_name

    @staticmethod
    def changed_group_name_system_message(admin, chat_name):
        return "%s changed the group's name to %s" % (admin, chat_name)