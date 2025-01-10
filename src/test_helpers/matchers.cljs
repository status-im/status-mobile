(ns test-helpers.matchers
  "Some vars in this namespace solely exist to support the matchers.clj file."
  (:require-macros test-helpers.matchers)
  (:require
    [cljs.test :as t]
    [matcher-combinators.parser]
    [matcher-combinators.printer :as printer]
    [matcher-combinators.result :as result]))

(defrecord Mismatch [summary match-result])

;; used in matchers.clj
#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn tagged-for-pretty-printing
  [actual-summary result]
  (->Mismatch actual-summary result))

(extend-protocol IPrintWithWriter
 Mismatch
   (-pr-writer [this writer _]
     (-write writer (printer/as-string (-> this :match-result ::result/value)))))

;; used in matchers.clj
#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn with-file+line-info
  [report]
  (merge (t/file-and-line (js/Error.) 4)
         report))
