(ns status-im.test.wallet.subs
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.money :as money]
            [status-im.ui.screens.wallet.subs :as s]))

(deftest test-balance-total-value
  (is (= (s/get-balance-total-value {:ETH (money/bignumber 1000000000000000000)
                                     :SNT (money/bignumber 100000000000000000000)}
                                    {:ETH {:USD {:from "ETH", :to "USD", :price 677.91, :last-day 658.688}}
                                     :SNT {:USD {:from "SNT", :to "USD", :price 0.1562, :last-day 0.15}}}
                                    :USD)
         693.53)))
