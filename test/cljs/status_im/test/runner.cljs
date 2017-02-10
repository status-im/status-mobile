(ns status-im.test.runner
  (:require #_[doo.runner :refer-macros [doo-tests]]
            [status-im.test.handlers]
            [status-im.test.commands.handlers]))

#_(doo-tests 'status-im.test.handlers
           'status-im.test.commands.handlers)
