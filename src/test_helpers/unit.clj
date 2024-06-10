(ns test-helpers.unit
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as string]
    [clojure.walk :as walk]))

(defn- keyword->test-name
  [sub-name]
  (->> [(namespace sub-name)
        (name sub-name)
        "test"]
       (remove nil?)
       (map #(string/replace % #"\." "-"))
       (string/join "-")))

(defmacro ^:private testing-restorable-app-db
  [description & body]
  `(cljs.test/testing ~description
     (restore-app-db (fn [] ~@body))))

(s/fdef deftest-sub
  :args (s/cat :sub-name keyword?
               :args     (s/coll-of symbol? :count 1)
               :body     (s/* any?)))

(defmacro deftest-sub
  "Defines a test based on `sub-name`, executes `body` and restores the app db.

  Any usage of the `cljs.test/testing` macro inside `body` will be modified to
  also make sure the app db is restored and the subscription cache is reset.

  Expressions in `body` will have access to `sub-name`, which should be used to
  avoid needlessly repeating the subscription name.

  Example:

  ```clojure
  (require '[test-helpers.unit :as h])

  (h/deftest-sub :wallet-legacy/sorted-tokens
    [sub-name]
    (testing \"sorts tokens by name, lowercased\"
      ;; Arrange
      (swap! rf-db/app-db assoc-in [<db-path>] <value>)

      ;; Act and Assert
      (is (= <expected> (rf/sub [sub-name])))))
  ```"
  [sub-name bindings & body]
  `(let [sub-name# ~sub-name]
     (cljs.test/deftest ~(symbol (keyword->test-name sub-name))
       (let [~bindings [sub-name#]]
         (restore-app-db
          (fn []
            ;; Do not warn about subscriptions being used in non-reactive contexts.
            (with-redefs [re-frame.interop/debug-enabled? false]
              ~@(clojure.walk/postwalk-replace
                 {'cljs.test/testing `testing-restorable-app-db
                  'testing           `testing-restorable-app-db}
                 body))))))))

(defmacro deftest-event
  "Defines a test var for an event `event-id`.

  This macro primarily exists to facilitate testing anonymous event handlers
  directly, without the need to extract them to vars.

  Similar to `deftest-sub`, this macro uses the re-frame machinery.
  Consequently, the test body is no longer guaranteed to be pure, as all
  interceptors will run (except for the standard `:do-fx`).

  Within the test, we can directly mutate `re-frame.db/app-db` atom to set up
  test data. Upon test completion, it is guaranteed that the app-db will be
  restored to its original state.

  Any usage of the `cljs.test/testing` macro within `body` will be modified to
  ensure the app-db is restored.

  Expressions in `body` will have access to `dispatcher`, a function similar to
  the original event handler under test, but without executing effects. It
  behaves like `re-frame.core/dispatch`, except it does not realize effects and
  it returns the `:effects` value from the final context, which is the value we
  want to assert on.

  Example:

  ```clojure
  (ns events-test
    (:require [test-helpers.unit :as h]))

  (h/deftest-event :wallet/dummy-event
    [dispatcher]
    (let [expected {:db {:a false}}]
      ;; Arrange
      (swap! rf-db/app-db {:a true})

      ;; Act and Assert
      (is (match? expected (dispatcher arg1 arg2)))))
  ```
  "
  [event-id bindings & body]
  `(let [event-id# ~event-id]
     (cljs.test/deftest ~(symbol (keyword->test-name event-id))
       (let [all-interceptors#        (re-frame.registrar/get-handler :event event-id#)
             interceptors-without-fx# (remove #(= :do-fx (:id %)) all-interceptors#)
             dispatcher#              (fn [& event-args#]
                                        (-> (into [event-id#] event-args#)
                                            (re-frame.interceptor/execute interceptors-without-fx#)
                                            :effects))
             ~bindings                [dispatcher#]]
         (assert (seq all-interceptors#)
                 (str "Failed to define test for event '" event-id# "'. Event does not exist"))
         (restore-app-db
          (fn []
            (with-redefs [re-frame.interop/debug-enabled? false]
              ~@(clojure.walk/postwalk-replace
                 {'cljs.test/testing `testing-restorable-app-db
                  'testing           `testing-restorable-app-db}
                 body))))))))

(defmacro use-log-fixture
  "Register log fixture which allows inspecting all calls to `taoensso.timbre/log`.

  Usage: Simply call this macro once per test namespace, and use the
  `test-helpers.unit/logs` atom to deref the collection of all logs for the
  test under execution.

  In Clojure(Script), we can rely on fixtures for each `cljs.deftest`, but not
  for individual `cljs.testing` macro calls. So keep that in mind when testing
  for log messages."
  []
  `(cljs.test/use-fixtures
    :each
    {:before test-helpers.unit/log-fixture-before
     :after  test-helpers.unit/log-fixture-after}))

(defmacro run-test-sync
  "Wrap around `re-frame.test/run-test-sync` to make it work with our aliased
  function `utils.re-frame/dispatch`.

  This macro is a must, because the original implementation uses `with-redefs`
  pointing to the original re-frame `dispatch` function, but our event handlers
  are dispatching using our aliased function.

  If tests run using the original `run-test-sync`, then all bets are off, and
  tests start to behave erratically."
  [& body]
  `(day8.re-frame.test/run-test-sync
    (with-redefs [utils.re-frame/dispatch re-frame.core/dispatch]
      ~@body)))
