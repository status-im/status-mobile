(ns test-helpers.component (:refer-clojure :exclude [test]))

(defmacro describe
  [description & body]
  `(js/global.describe
     ~description
     (fn []
       ~@body
       ;; We need to return 'undefined', otherwise Jest gives a
       ;; warning: "Describe callback must not return a value".
       js/undefined)))

(defmacro test
  [description & body]
  `(js/global.test
     ~description
     (fn [] ~@body)))

(defmacro before-each
  [description & body]
  `(js/beforeEach
    ~description
    (fn [] ~@body)))

(defmacro after-each
  [description & body]
  `(js/afterEach
    ~description
    (fn [] ~@body)))
