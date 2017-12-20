from datetime import datetime


def set_password_as_new_user(*args):
    for view in args:
        view.request_password_icon.click()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.find_full_text(
            "Here is your signing phrase. You will use it to verify your transactions. Write it down and keep it safe!")


def change_user_details(*args):
    users_details = dict()
    for device, view in enumerate(args):
        current_time = datetime.now().strftime('%-m%-d%-H%-M%-S')
        new_status = '#newstatus_%s' % current_time
        new_username = 'New_User_Name_%s' % current_time

        view.user_status_box.click()
        view.user_status_input.clear()
        view.user_status_input.send_keys(new_status)
        view.username_input.clear()
        view.username_input.send_keys(new_username)
        view.save_button.click()

        users_details[device] = dict()
        users_details[device]['status'] = new_status
        users_details[device]['name'] = new_username
    return users_details


def recover_access(home, passphrase, password, username):
    login = home.recover_button.click()
    login.passphrase_input.send_keys(passphrase)
    login.password_input.send_keys(password)
    login.confirm_recover_access.click()
    recovered_user = login.element_by_text(username, 'button')
    login.confirm()
    recovered_user.click()
    login.password_input.send_keys(password)
    login.sign_in_button.click()
    login.find_full_text('Chats', 60)
