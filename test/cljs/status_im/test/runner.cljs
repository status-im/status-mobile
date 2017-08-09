(ns status-im.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.contacts.handlers]
            [status-im.test.chat.models.input]
            [status-im.test.handlers]
            [status-im.test.utils.utils]
            [status-im.test.utils.money]
            [status-im.test.utils.clocks]
            [status-im.test.utils.random]
            [status-im.test.utils.gfycat.core]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests 'status-im.test.contacts.handlers
           'status-im.test.chat.models.input
           'status-im.test.handlers
           'status-im.test.utils.utils
           'status-im.test.utils.money
           'status-im.test.utils.clocks
           'status-im.test.utils.random
           'status-im.test.utils.gfycat.core)