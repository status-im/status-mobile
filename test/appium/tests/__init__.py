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
        self.name = None

tests_data = TestData()
