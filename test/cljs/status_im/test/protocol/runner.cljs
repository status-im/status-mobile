(ns status-im.test.protocol.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.protocol.core]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests 'status-im.test.protocol.core)
