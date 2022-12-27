(ns env.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [env.test.utils]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests 'env.test.utils)
