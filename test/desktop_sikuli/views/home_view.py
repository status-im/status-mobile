import os

from views.base_element import BaseElement, InputField
from views.base_view import BaseView

IMAGES_PATH = os.path.join(os.path.dirname(__file__), 'images/home_view')


class HomeView(BaseView):
    def __init__(self):
        super(HomeView, self).__init__()
        self.plus_button = BaseElement(IMAGES_PATH + '/plus_button.png')
        self.contact_code_input = InputField(IMAGES_PATH + '/contact_code_input.png')
        self.start_chat_button = BaseElement(IMAGES_PATH + '/start_chat_button.png')
