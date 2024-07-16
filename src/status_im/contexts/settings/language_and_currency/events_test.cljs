(ns status-im.contexts.settings.language-and-currency.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    matcher-combinators.test
    [status-im.contexts.settings.language-and-currency.events :as sut]))

(deftest get-currencies-test
  (let [cofx     {:db {}}
        expected {:fx [[:json-rpc/call
                        [{:method     "wakuext_getCurrencies"
                          :on-success [:settings/get-currencies-success]
                          :on-error   [:log-rpc-error {:event :settings/get-currencies}]}]]]}]
    (is (match? expected (sut/get-currencies cofx)))))
