(ns status-im.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.chat.models.input]
            [status-im.test.handlers]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests 'status-im.test.chat.models.input
           'status-im.test.handlers)
