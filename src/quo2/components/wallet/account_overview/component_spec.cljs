(ns quo2.components.wallet.account-overview.component-spec
  (:require [quo2.components.wallet.account-overview.view :as account-overview]
            [test-helpers.component :as h]))

(h/describe "Account overview test"
  (h/test "renders correct account name"
    (h/render [account-overview/view
               {:state        :default
                :account-name "Diamond Hand"}])
    (h/is-truthy (h/get-by-text "Diamond Hand")))

  (h/test "renders correct account value"
    (h/render [account-overview/view
               {:current-value "20.0$"}])
    (h/is-truthy (h/get-by-text "20.0$")))

  (h/test "renders correct account changes"
    (h/render [account-overview/view
               {:currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-text "0.00%"))
    (h/is-truthy (h/get-by-text "€0.00")))

  (h/test "renders correct timeframe"
    (h/render [account-overview/view
               {:time-frame           :custom
                :time-frame-string    "15 May"
                :time-frame-to-string "19 May"}])
    (h/is-truthy (h/get-by-text "15 May"))
    (h/is-truthy (h/get-by-text "19 May")))

  (h/test "renders correct account timeframe"
    (h/render [account-overview/view
               {:time-frame        :one-week
                :currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-translation-text :t/one-week-int)))

  (h/test "renders correct account timeframe"
    (h/render [account-overview/view
               {:time-frame        :one-month
                :currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-translation-text :t/one-month-int)))

  (h/test "renders correct account timeframe"
    (h/render [account-overview/view
               {:time-frame        :three-months
                :currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-translation-text :t/three-months-int)))

  (h/test "renders correct account timeframe"
    (h/render [account-overview/view
               {:time-frame        :one-year
                :currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-translation-text :t/one-year)))

  (h/test "renders correct account timeframe"
    (h/render [account-overview/view
               {:time-frame        :all-time
                :currency-change   "€0.00"
                :percentage-change "0.00%"}])
    (h/is-truthy (h/get-by-translation-text :t/all-time))))
