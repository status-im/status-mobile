import os
import pytest
import re
import time
from os import path
from support.api.third_parties_api import get_token_info


def get_parameters():
    directory = os.sep.join(__file__.split(os.sep)[:-5])
    file_path = path.join(directory, 'src/status_im/ethereum/tokens.cljs')
    # with open(file_path, 'r') as f:
    #     data = f.read()
    # return re.findall(r'{:symbol\s*:(.*)\n\s*:name\s*"(.*)"\n\s*:address\s*"(.*)"\n\s*:decimals\s*(.*)}', data)
    return []

class TestAPi(object):

    @pytest.mark.parametrize('symbol,name,address,decimals', get_parameters())
    @pytest.mark.skip
    def test_tokens_verification(self, symbol, name, address, decimals):
        res = get_token_info(address)
        errors = list()
        if str(res['decimals']) != decimals:
            errors.append("decimals value %s doesn't match expected %s" % (decimals, res['decimals']))
        if res['symbol'] != symbol:
            errors.append("symbol '%s' doesn't match expected '%s'" % (symbol, res['symbol']))
        if res['name'] != name:
            errors.append("token name '%s' doesn't match expected '%s'" % (name, res['name']))
        if errors:
            pytest.fail('For address %s %s' % (address, ', '.join(errors)))

    @staticmethod
    def teardown():
        time.sleep(3)
