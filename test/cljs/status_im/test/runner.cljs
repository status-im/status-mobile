(ns status-im.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.handlers]
            [status-im.test.commands.handlers]))

(doo-tests 'status-im.test.handlers
           'status-im.test.commands.handlers)
