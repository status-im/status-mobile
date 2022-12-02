(ns status-im.test-helpers
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as walk]))

(defn- subscription-name->test-name
  [sub-name]
  (->> [(namespace sub-name)
        (name sub-name)
        "test"]
       (remove nil?)
       (map #(string/replace % #"\." "-"))
       (string/join "-")))

(defmacro ^:private restore-app-db
  "Saves app db, executes `body` and restores to original app-db.

  Also clears the subscription cache for good measure."
  [& body]
  `(do (re-frame.subs/clear-subscription-cache!)
       (let [original-db# (deref re-frame.db/app-db)]
         (try
           ~@body
           (finally
             (reset! re-frame.db/app-db original-db#))))))

(defmacro ^:private testing-subscription
  [description & body]
  `(cljs.test/testing ~description
     (restore-app-db ~@body)))

(s/fdef deftest-sub
  :args (s/cat :sub-name keyword?
               :args (s/coll-of symbol? :count 1)
               :body (s/* any?)))

(defmacro deftest-sub
  "Defines a test based on `sub-name`, executes `body` and restores the app db.

  Any usage of the `cljs.test/testing` macro inside `body` will be modified to
  also make sure the app db is restored and the subscription cache is reset.

  Expressions in `body` will have access to `sub-name`, which should be used to
  avoid needlessly repeating the subscription name.

  Example:

  ```clojure
  (require '[status-im.test-helpers :as h])

  (h/deftest-sub :wallet/sorted-tokens
    [sub-name]
    (testing \"sorts tokens by name, lowercased\"
      ;; Arrange
      (swap! rf-db/app-db assoc-in [<db-path>] <value>)

      ;; Act and Assert
      (is (= <expected> (rf/sub [sub-name])))))
  ```"
  [sub-name args & body]
  `(let [sub-name# ~sub-name]
     (cljs.test/deftest ~(symbol (subscription-name->test-name sub-name))
       (restore-app-db
        (let [~args [sub-name#]]
          ~@(clojure.walk/postwalk-replace
             {'cljs.test/testing `testing-subscription
              'testing           `testing-subscription}
             body))))))
