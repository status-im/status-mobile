import asyncio


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

test_data = TestData()
