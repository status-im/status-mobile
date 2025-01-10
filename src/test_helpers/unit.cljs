(ns test-helpers.unit
  "Utilities for simplifying the process of writing tests and improving test
  readability.

  Avoid coupling this namespace with particularities of the Status' domain, thus
  prefer to use it for more general purpose concepts, such as the re-frame event
  layer."
  (:require-macros test-helpers.unit)
  ;; We must require test-helpers.matchers namespace to register the custom cljs.test directive
  ;; `match-strict?`
  (:require
    [re-frame.db :as rf-db]
    [re-frame.subs :as rf-subs]
    [taoensso.timbre :as log]
    test-helpers.matchers))
;; We must require `test-helpers.matchers` this namespace to register the custom cljs.test
;; directive `match-strict?`.


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]} ;; used in unit.clj
(defn restore-app-db
  "Saves current app DB, calls `f` and restores the original app DB.

  Always clears the subscription cache after calling `f`."
  [f]
  (rf-subs/clear-subscription-cache!)
  (let [original-db @rf-db/app-db]
    (try
      (f)
      (finally
       (reset! rf-db/app-db original-db)))))

;;;; Log fixture

(def ^:private original-log-config
  (atom nil))

(def logs
  "The collection of all logs registered by `test-log-appender`. Tests can
  de-reference it and verify log messages and their respective levels."
  (atom []))

(defn- test-log-appender
  "Custom log appender that persists all `taoensso.timbre/log` call arguments."
  [{:keys [vargs level]}]
  (swap! logs conj {:args vargs :level level}))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- log-fixture-before
  []
  #_{:clj-kondo/ignore [:unresolved-var]}
  (reset! original-log-config log/*config*)

  ;; We reset the logs *before* running tests instead of *after* because: 1.
  ;; It's just as reliable; 2. It helps when using the REPL, because we can
  ;; easily inspect `logs` after a test has finished.
  (reset! logs [])

  (log/swap-config! assoc-in
                    [:appenders :test]
                    {:enabled? true
                     :fn       test-log-appender}))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- log-fixture-after
  []
  (log/set-config! @original-log-config))
