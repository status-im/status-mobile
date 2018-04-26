import asyncio
import logging
from datetime import datetime


@asyncio.coroutine
def start_threads(quantity: int, func: type, returns: dict, *args):
    loop = asyncio.get_event_loop()
    for i in range(quantity):
        returns[i] = loop.run_in_executor(None, func, *args)
    for k in returns:
        returns[k] = yield from returns[k]
    return returns


def get_current_time():
    return datetime.now().strftime('%-m%-d%-H%-M%-S')


def info(text: str):
    if "Base" not in text:
        logging.info(text)
        test_suite_data.current_test.steps.append(text)


class SingleTestData(object):
    def __init__(self, name, steps=list(), jobs=list(), error=None):
        self.name = name
        self.steps = steps
        self.jobs = jobs
        self.error = error


class TestSuiteData(object):
    def __init__(self):
        self.apk_name = None
        self.current_test = None
        self.tests = list()

    def add_test(self, test):
        self.tests.append(test)
        self.current_test = test


test_suite_data = TestSuiteData()


basic_user = dict()
basic_user['password'] = "newuniquepassword12"
basic_user['passphrase'] = "tree weekend ceiling awkward universe pyramid glimpse raven pair lounge grant grief"
basic_user['username'] = "Splendid Useless Racerunner"
basic_user['public_key'] = "0x0448243ea6adfd2f825f083a02a1fea11e323a3ba32c9dc9992d3d465e932964" \
                           "38792f11380e14c6700f598e89bafaddd2579823f4273358f9f66828fcac7dd465"

transaction_users = dict()
transaction_users['A_USER'] = dict()
transaction_users['A_USER']['password'] = "qwerty"
transaction_users['A_USER']['passphrase'] = "pet letter very ozone shop humor shuffle bounce convince soda hint brave"
transaction_users['A_USER']['username'] = "Evergreen Handsome Cottontail"
transaction_users['A_USER']['address'] = "67a50ef1d26de6d65dbfbb88172ac1e7017e766d"
transaction_users['A_USER']['public_key'] = "0x040e016b940e067997be8d91298d893ff2bc3580504b4ccb155ea03d183b85f1" \
                                                   "8e771a763d99f60fec70edf637eb6bad9f96d3e8a544168d3ad144f83b4cf7625c"
transaction_users['B_USER'] = dict()
transaction_users['B_USER']['password'] = "qwerty"
transaction_users['B_USER']['passphrase'] = "resemble soap taxi meat reason inflict dilemma calm warrior key gloom again"
transaction_users['B_USER']['username'] = "Brief Organic Xenops"
transaction_users['B_USER']['address'] = "3d672407a7e1250bbff85b7cfdb456f5015164db"
transaction_users['B_USER']['public_key'] = "0x0406b17e5cdfadb2a05e84508b1a2916def6395e6295f57e92b85f915d40bca3" \
                                                   "f4a7e4c6d6b25afa840dd042fac83d3f856181d553f34f1c2b12878e774adde099"
transaction_users['C_USER'] = dict()
transaction_users['C_USER']['password'] = "qwerty"
transaction_users['C_USER']['passphrase'] = "romance emerge transfer trial enemy average casino decline old bag mandate winner"
transaction_users['C_USER']['username'] = "Speedy Occasional Lightningbug"
transaction_users['C_USER']['address'] = "853bdd57e881ed09d045ceee53564a00e1da3cf9"
transaction_users['C_USER']['public_key'] = "0x04aa3f3977b0c06ce04f4cdfd2ea5baf5560e18aba52324f6d4bf69bbd603b60" \
                                                   "0e874ed483920d9613882e7345b70b1d95c6ca4ee8b18089da1d064c498355c944"
transaction_users['D_USER'] = dict()
transaction_users['D_USER']['password'] = "qwerty"
transaction_users['D_USER']['passphrase'] = "grit half victory alarm code chicken drill worth valve rug clown guess"
transaction_users['D_USER']['username'] = "Frilly Mediumspringgreen Kingsnake"
transaction_users['D_USER']['address'] = "1bd914e370a63714ee144692002046c7e6d83348"
transaction_users['D_USER']['public_key'] = "0x041aa2bbf1cc1253bf658a4a44289cdd0031f0038f8d085b1a43b69550e3467b" \
                                                   "c1f7963c35d0016d9c23d28960b984045c00bd9373b413b1d19aa4370261b8d084"
transaction_users['E_USER'] = dict()
transaction_users['E_USER']['password'] = "qwerty"
transaction_users['E_USER']['passphrase'] = "nose where inch arrive mango lift token hotel impact series open spray"
transaction_users['E_USER']['username'] = "Informal Altruistic Tasmaniandevil"
transaction_users['E_USER']['address'] = "f3ad2b2814702052e0b6ae016961c49b702c03a6"
transaction_users['E_USER']['public_key'] = "0x0404a161053a628a1da23a3ba1696c145bd77b0be4d7c4089f8075e78b3b7f9f" \
                                                   "553aafcbd0ec052f348dd692c6b6890a2e4d596002024ed0b78b2c21fe663369e8"
transaction_users['F_USER'] = dict()
transaction_users['F_USER']['password'] = "qwerty"
transaction_users['F_USER']['passphrase'] = "exotic grit ticket medal alpha travel rapid hedgehog desert history security town"
transaction_users['F_USER']['username'] = "Front Bronze Lemming"
transaction_users['F_USER']['address'] = "08fee8015ec71d78b1855937988d5bf45892bc34"
transaction_users['F_USER']['public_key'] = "0x0445284807c9fb9080cec6f1bd24f8d546c5c2c0dd2d06bdbf91d1af70507885" \
                                                   "1b9ee0d3bb04736abd00f5a8dce2f20a579a437ee3bea9920eefba7fa46266f8df"

transaction_users['G_USER'] = dict()
transaction_users['G_USER']['password'] = 'qwerty'
transaction_users['G_USER']['passphrase'] = 'sorry assume clutch category grace lift text drift ankle tenant price inside'
transaction_users['G_USER']['username'] = 'Corny Jumpy Argusfish'
transaction_users['G_USER']['address'] = '0xcd22ac97164257fa832104b94286e0d839a42cfc'
transaction_users['G_USER']['public_key'] = '0x04e53b6e5c602208d34b436fec90d4b85d171e5583d4371be57c7994a247e4ab3333c8ea2' \
                                            '42978d80dda9a0279c90050f4d33ea83b1bb71b7b1297a68e769be9b7'

transaction_users['H_USER'] = dict()
transaction_users['H_USER']['password'] = 'qwerty'
transaction_users['H_USER']['passphrase'] = 'expect attract panther inhale essence illegal muffin power cabbage correct market gun'
transaction_users['H_USER']['username'] = 'Marvelous Round Argali'
transaction_users['H_USER']['address'] = '0xbbb5bf58c92bd48e27fa508ed544da8472bbb26c'
transaction_users['H_USER']['public_key'] = '0x04862eb3a2f08bb0469380a0ea8deb06f1c9af57e839cc7e783edd209058b72a0049596a' \
                                            '16faba47f53b629958b435d19857b949fb3bb4a8cfc8f577cbac96609d'

transaction_users_wallet = dict()
transaction_users_wallet['A_USER'] = dict()
transaction_users_wallet['A_USER']['password'] = "new_unique_password"
transaction_users_wallet['A_USER']['passphrase'] = "kiss catch paper awesome ecology surface trumpet quit index open stage brave"
transaction_users_wallet['A_USER']['username'] = "Impractical Afraid Watermoccasin"
transaction_users_wallet['A_USER']['address'] = "a409e5faf758a5739f334bae186d8bc11c98ea4d"
transaction_users_wallet['A_USER']['public_key'] = "0x04630e0acd973ad448c7a54e2345d6bacaaa4de5a0ec938f802a0f503bf144e" \
                                                   "80521833be71d4ddfefacfa571a473ebe4542dde102aca4e90d2abe0bb67ee2f99b"

transaction_users_wallet['B_USER'] = dict()
transaction_users_wallet['B_USER']['password'] = "new_unique_password"
transaction_users_wallet['B_USER']['passphrase'] = "twenty engine fitness clay faculty supreme garbage armor broccoli agree end sad"
transaction_users_wallet['B_USER']['username'] = "Muffled Purple Milksnake"
transaction_users_wallet['B_USER']['address'] = "5261ceba31e3a7204b498b2dd20220a6057738d1"
transaction_users_wallet['B_USER']['public_key'] = "0x04cd70746f3df6cae7b45c32c211bd7e9e95ed5a1ec470db8f3b1f244eed182" \
                                                   "1d4a2053d7671802c5f7ce5b81f5fc2016a8109e1bc83f151ceff21f08c0cdcc6e4"
transaction_users_wallet['C_USER'] = dict()
transaction_users_wallet['C_USER']['password'] = "new_unique_password"
transaction_users_wallet['C_USER']['passphrase'] = "purchase ensure mistake crystal person similar shaft family shield clog risk market"
transaction_users_wallet['C_USER']['username'] = "Light Milky Fawn"
transaction_users_wallet['C_USER']['address'] = "4091ac456cd0f504952fd3f74426b5cbd00804ff"
transaction_users_wallet['C_USER']['public_key'] = "0x04b82317a2721f3f65ef5ff73240310d5d7a623066521ecd89f9c72f886651e3" \
                                                   "0c6c6dc22bc8ee82a938f15b919f0f0571280635ca78c009c242a259e1e29e9c58"
transaction_users_wallet['D_USER'] = dict()
transaction_users_wallet['D_USER']['password'] = "new_unique_password"
transaction_users_wallet['D_USER']['passphrase'] = "night grit town donate length zoo meat collect vapor brush topic check"
transaction_users_wallet['D_USER']['username'] = "Classic High Mutt"
transaction_users_wallet['D_USER']['address'] = "7701bd9eab8c59a9ff51ca0b0e0b8e909cd3e9e2"
transaction_users_wallet['D_USER']['public_key'] = "0x044b9b9325f18ee95e044237464a4d15cc4e5f47a63aee52e1dbfdb91de0ec1ac" \
                                                   "4cddfaf4192d22aaad0d91cfaf746dca7c8404a726705dc54c3f3768fc7aa4ded"

group_chat_users = dict()
group_chat_users['A_USER'] = dict()
group_chat_users['A_USER']['password'] = "qwerty"
group_chat_users['A_USER']['passphrase'] = "thank fruit brisk pond opera census grid husband claw sight chunk arena"
group_chat_users['A_USER']['username'] = "Soupy Thorough Arrowcrab"
group_chat_users['A_USER']['public_key'] = "0x04354b5882a1a0c1612d81271477925e7209d676f6fa310f8c28b761499ea39d83aacff" \
                                           "380a181362dc5507ff116e8388a03b53f085d39536642529b5212f7cc00"

group_chat_users['B_USER'] = dict()
group_chat_users['B_USER']['password'] = "qwerty"
group_chat_users['B_USER']['passphrase'] = "label ill slender audit atom love vote snap edit program climb beyond"
group_chat_users['B_USER']['username'] = "Sleepy Friendly Eel"
group_chat_users['B_USER']['public_key'] = "0x049fca07d1d85cd8fac7f2615c61323e0e32ad46b93169dfb2b1cdc3bfeb66d1974c575" \
                                           "ba84b79e63fa567ba62d93d45a4f28442a63a9bee353423c02c1b7a7134"

group_chat_users['C_USER'] = dict()
group_chat_users['C_USER']['password'] = "qwerty"
group_chat_users['C_USER']['passphrase'] = "you salmon ticket antique spray panther flee neck scale mad trial exile"
group_chat_users['C_USER']['username'] = "Voluminous Buoyant Mouflon"
group_chat_users['C_USER']['public_key'] = "0x042a59230f87211cf5f6c86203fdf415fd0a5a9ea600baa7b69d476a83e0a7826aa8c87" \
                                           "b9f6aa2c459c0c7115d8c82887a5462d7c2fd4590c2e98aa87ed6caa9ce"
