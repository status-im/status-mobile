(ns status-im.test.runner
  (:require-macros [status-im.test.loader :refer [require-test-namespaces]])
  (:require [doo.runner :refer-macros [doo-tests doo-all-tests]]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))



(set! goog.DEBUG false)

(require-test-namespaces "./test/cljs/status_im/test")

(println (macroexpand '(require-test-namespaces "./test/cljs/status_im/test")))

(doo-all-tests #"status\-im\.test.*")
