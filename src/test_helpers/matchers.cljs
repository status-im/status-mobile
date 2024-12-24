(ns test-helpers.matchers
  "Some vars in this namespace solely exist to support the matchers.clj file."
  (:require-macros test-helpers.matchers)
  (:require
    [matcher-combinators.parser]
    [matcher-combinators.printer :as printer]
    [matcher-combinators.result :as result]))

(defrecord Mismatch [summary match-result])

(extend-protocol IPrintWithWriter
 Mismatch
   (-pr-writer [this writer _]
     (-write writer (printer/as-string (-> this :match-result ::result/value)))))

