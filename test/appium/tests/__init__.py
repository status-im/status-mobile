import asyncio
from datetime import datetime


@asyncio.coroutine
def start_threads(amount, func, *args):
    features = dict()
    loop = asyncio.get_event_loop()
    for i in range(amount):
        features['feature_' + str(i)] = loop.run_in_executor(None, func, *args)
    for k in features:
        features[k] = yield from features[k]
    return (features[k] for k in features)


class TestData(object):

    def __init__(self):
        self.test_name = None
        self.apk_name = None


test_data = TestData()


basic_user = {'password': "newuniquepassword12",
              'passphrase': "tree weekend ceiling awkward universe pyramid glimpse raven pair lounge grant grief",
              'username': "Splendid Useless Racerunner"}

transaction_users = {
                    'A_USER': {'password': "qwerty",
                               'passphrase': "pet letter very ozone shop humor "
                                             "shuffle bounce convince soda hint brave",
                               'public_key': '0x040e016b940e067997be8d91298d893ff2bc3580504b4ccb155ea03d183b85f1'
                                             '8e771a763d99f60fec70edf637eb6bad9f96d3e8a544168d3ad144f83b4cf7625c',
                               'address': '67a50ef1d26de6d65dbfbb88172ac1e7017e766d',
                               'username': 'Evergreen Handsome Cottontail'},

                    'B_USER': {'password': "qwerty",
                               'passphrase': "resemble soap taxi meat reason "
                                             "inflict dilemma calm warrior key gloom again",
                               'public_key': '0x0406b17e5cdfadb2a05e84508b1a2916def6395e6295f57e92b85f915d40bca3'
                                             'f4a7e4c6d6b25afa840dd042fac83d3f856181d553f34f1c2b12878e774adde099',
                               'address': '3d672407a7e1250bbff85b7cfdb456f5015164db',
                               'username': 'Brief Organic Xenops'
                               },
                     }


transaction_users_wallet = dict()

transaction_users_wallet['A_USER'] = dict()
transaction_users_wallet['A_USER']['password'] = "new_unique_password"
transaction_users_wallet['A_USER']['passphrase'] = "kiss catch paper awesome ecology surface " \
                                                   "trumpet quit index open stage brave"
transaction_users_wallet['A_USER']['username'] = "Impractical Afraid Watermoccasin"
transaction_users_wallet['A_USER']['address'] = "a409e5faf758a5739f334bae186d8bc11c98ea4d"
transaction_users_wallet['A_USER']['public_key'] = "0x04630e0acd973ad448c7a54e2345d6bacaaa4de5a0ec938f802a0f503bf144e" \
                                                   "80521833be71d4ddfefacfa571a473ebe4542dde102aca4e90d2abe0bb67ee2f99b"

transaction_users_wallet['B_USER'] = dict()
transaction_users_wallet['B_USER']['password'] = "new_unique_password"
transaction_users_wallet['B_USER']['passphrase'] = "twenty engine fitness clay faculty supreme " \
                                                   "garbage armor broccoli agree end sad"
transaction_users_wallet['B_USER']['username'] = "Muffled Purple Milksnake"
transaction_users_wallet['B_USER']['address'] = "5261ceba31e3a7204b498b2dd20220a6057738d1"
transaction_users_wallet['B_USER']['public_key'] = "0x04cd70746f3df6cae7b45c32c211bd7e9e95ed5a1ec470db8f3b1f244eed182" \
                                                   "1d4a2053d7671802c5f7ce5b81f5fc2016a8109e1bc83f151ceff21f08c0cdcc6e4"


def get_current_time():
    return datetime.now().strftime('%-m%-d%-H%-M%-S')
