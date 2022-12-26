(ns status-im.component-test-helpers (:refer-clojure :exclude [test]))

(defmacro describe
  [description & body]
  `(js/global.describe
     ~description
     (fn []
       ~@body
       js/undefined)))

(defmacro test
  [description & body]
  `(js/global.test
     ~description
     (fn [] ~@body)))
