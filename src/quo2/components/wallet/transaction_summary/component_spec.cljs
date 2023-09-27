(ns quo2.components.wallet.transaction-summary.component-spec
  (:require [quo2.components.wallet.transaction-summary.view :as transaction-summary]
            [test-helpers.component :as h]))

(h/describe "Transaction summary"
  (h/test "default render"
    (h/render [transaction-summary/view {}])
    (h/is-truthy (h/query-by-label-text :transaction-summary)))

  (h/test "incorrect setting doesn't crash render"
    (h/render [transaction-summary/view {:transaction :unknown}])
    (h/is-truthy (h/query-by-label-text :transaction-summary)))

  (h/test "icon displayed"
    (h/render [transaction-summary/view {:transaction :send}])
    (h/is-truthy (h/query-by-label-text :header-icon)))

  (h/test "Context tag rendered"
    (h/render [transaction-summary/view
               {:transaction :send
                :first-tag   {:size       24
                              :type       :token
                              :token-name "SNT"
                              :amount     1500}}])
    (h/is-truthy (h/query-by-label-text :context-tag))))







