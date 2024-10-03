(ns cljs.test
  (:require [clj-kondo.hooks-api :as hooks]))

(defn deftest
  "Verify test name passed to `cljs.test/deftest` is suffixed with -test and not
  prefixed with test-."
  [{:keys [node]}]
  (let [[_ test-name-node & _] (:children node)
        test-name              (str (hooks/sexpr test-name-node))]
    (when (and (hooks/token-node? test-name-node)
               (or (not (re-find #"^.*-test$" test-name))
                   (re-find #"^test-.*$" test-name)))
      (hooks/reg-finding! (assoc (meta test-name-node)
                                 :message "Test name should be suffixed with -test"
                                 :type    :status-im.linter/inconsistent-test-name)))))

(comment
  ;; Invalid
  (deftest {:node (hooks/parse-string "(deftest foo-tes (println :hello))")})
  (deftest {:node (hooks/parse-string "(deftest test-foo-test (println :hello))")})
)
