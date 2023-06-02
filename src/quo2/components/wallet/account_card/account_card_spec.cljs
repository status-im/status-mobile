(ns quo2.components.wallet.account-card.account-card-spec
  (:require [quo2.components.wallet.account-card.view :as account-card]
            [test-helpers.component :as h]))

(h/describe "Account_card tests"
(def john-doe-name "John Doe")

(defn get-test-data
  [type watch-only]
  {:name                john-doe-name
   :balance             "€1,000.00"
   :percentage-value    "50%"
   :amount              "€500.00"
   :customization-color :blue
   :watch-only          watch-only
   :type                type})

(h/test "Renders normal"
  (let [data (get-test-data :default false)]
    (h/render [account-card/view data])
    (h/is-truthy (h/get-by-text john-doe-name))))

(h/test "Renders watch-only"
  (let [data (get-test-data :watch-only true)]
    (h/render [account-card/view data])
    (h/is-truthy (h/get-by-text john-doe-name))))

(h/test "Renders add-account"
  (let [data {:type :add-account}]
    (h/render [account-card/view data])
    (h/is-truthy (h/get-by-label-text :plus-button))))

(h/test "Add account on press fires correctly"
  (let [handler (h/mock-fn)
        data    {:type    :add-account
                 :handler handler}]
    (h/render [account-card/view data])
    (h/fire-event :on-press (h/get-by-label-text :plus-button))
    (h/was-called handler))))
