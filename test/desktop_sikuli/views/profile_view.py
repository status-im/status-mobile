import os

try:
    import org.sikuli.script.SikulixForJython
    from sikuli import *
except Exception:
    pass

from views.base_element import BaseElement, TextElement
from views.base_view import BaseView

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/profile_view')


class MailServerElement(TextElement):
    def __init__(self, server_name):
        super(MailServerElement, self).__init__(server_name)

    class ActiveImage(BaseElement):
        def __init__(self):
            super(MailServerElement.ActiveImage, self).__init__(IMAGES_PATH + '/mail_server_active_image.png')

    def is_active(self):
        print(self.element_line)


class ProfileView(BaseView):
    def __init__(self):
        super(ProfileView, self).__init__()
        self.share_my_code_button = BaseElement(IMAGES_PATH + '/share_my_code_button.png')
        self.copy_code_button = BaseElement(IMAGES_PATH + '/copy_code_button.png')
        self.log_out_button = BaseElement(IMAGES_PATH + '/log_out_button.png')

    def get_mail_servers_list(self):
        server_names = []
        for line in collectLines():
            line_text = line.getText().encode('ascii', 'ignore')
            if 'mail-0' in line_text:
                server_names.append(line_text)

    def get_mail_server(self, name):
        return MailServerElement(name)
