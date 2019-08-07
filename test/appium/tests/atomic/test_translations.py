from itertools import chain

import json
import os
import pytest

from tests import marks
from tests.base_test_case import NoDeviceTestCase


@marks.all
@marks.translations
class TestTranslations(NoDeviceTestCase):

    @marks.testrail_id(6223)
    def test_find_unused_translations(self):
        directory = os.sep.join(__file__.split(os.sep)[:-5])
        with open(os.path.join(directory, 'translations/en.json'), 'r') as f:
            data = set(json.load(f).keys())
        result = []
        paths = ['src/status_im', 'components/src']
        for root, dirs, files in chain.from_iterable(os.walk(os.path.join(directory, path)) for path in paths):
            dirs[:] = [d for d in dirs if d not in ['test', 'translations']]
            for file in [file for file in files if file.endswith('.cljs')]:
                with open(os.path.join(root, file), "r") as source:
                    try:
                        content = source.read()
                        for key_name in data:
                            if key_name in content:
                                result.append(key_name)
                    except UnicodeDecodeError:
                        pass
        unused = data - set(result)
        recheck = [i for i in unused if i[-1].isdigit()]
        error = ''
        if recheck:
            error += 'Translations to recheck: \n %s' % recheck
            unused -= set(recheck)
        if unused:
            error += '\nUnused translations: \n %s' % unused
        if error:
            pytest.fail(error)
