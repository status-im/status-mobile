(ns quo2.components.wallet.account-card.component-spec
  (:require [quo2.components.wallet.account-card.view :as account-card]
            [test-helpers.component :as h]))

(def username "Alisher account")
(def empty-username "Account 1")

(defn get-test-data
  [type watch-only? empty-type? loading? metrics?]
  {:name                (if empty-type? empty-username username)
   :balance             "€1,000.00"
   :percentage-value    "50%"
   :amount              "€500.00"
   :customization-color :blue
   :watch-only?         watch-only?
   :loading?            loading?
   :metrics?            metrics?
   :type                type})

(h/describe "Account_card tests"
  (h/test "Renders Default"
    (let [data (get-test-data :default false false false true)]
      (h/render [account-card/view data])
      (h/is-truthy (h/get-by-text username))))

  (h/test "Renders Watch-Only"
    (let [data (get-test-data :watch-only true false false true)]
      (h/render [account-card/view data])
      (h/is-truthy (h/get-by-text username))))

  (h/test "Renders Add-Account"
    (let [data {:type :add-account}]
      (h/render [account-card/view data])
      (h/is-truthy (h/get-by-label-text :add-account))))

  (h/test "Renders Empty"
    (let [data (get-test-data :empty false true false true)]
      (h/render [account-card/view data])
      (h/is-truthy (h/get-by-text empty-username))))

  (h/test "Add account on press fires correctly"
    (let [on-press (h/mock-fn)
          data     {:type     :add-account
                    :on-press on-press}]
      (h/render [account-card/view data])
      (h/fire-event :on-press (h/get-by-label-text :add-account))
      (h/was-called on-press)))

  (h/test "Renders component without metrics"
    (let [data (get-test-data :default false false false false)]
      (h/render [account-card/view data])
      (h/is-falsy (h/query-by-label-text :metrics))))

  (h/test "Renders loading state"
    (let [data (get-test-data :default false false true true)]
      (h/render [account-card/view data])
      (h/is-truthy (h/get-by-label-text :loading))))

  (h/test "Renders loading state without metrics"
    (let [data (get-test-data :default false false true false)]
      (h/render [account-card/view data])
      (h/is-falsy (h/query-by-label-text :metrics)))))
