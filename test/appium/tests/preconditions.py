

def set_password_as_new_user(*args):
    for view in args:
        view.request_password_icon.click()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.chat_request_input.send_keys("qwerty1234")
        view.confirm()
        view.find_full_text("Tap here to enter your phone number & I\'ll find your friends")


def recover_access(chats, passphrase, password, username):
    chats.back_button.click()
    chats.profile_button.click()
    login = chats.switch_users_button.click()
    login.recover_access_button.click()
    login.passphrase_input.send_keys(passphrase)
    login.password_input.send_keys(password)
    login.confirm_recover_access.click()
    recovered_user = login.element_by_text(username, 'button')
    recovered_user.click()
    login.password_input.send_keys(password)
    login.sign_in_button.click()
