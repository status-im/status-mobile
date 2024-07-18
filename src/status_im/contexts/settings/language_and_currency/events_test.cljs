(ns status-im.contexts.settings.language-and-currency.events-test
  (:require
    [cljs.test :refer-macros [is]]
    matcher-combinators.test
    status-im.contexts.settings.language-and-currency.events
    [test-helpers.unit :as h]))

(h/deftest-event :settings/get-currencies
  [event-id dispatch]
  (let [expected-effects {:fx [[:json-rpc/call
                                [{:method     "wakuext_getCurrencies"
                                  :on-success [:settings/get-currencies-success]
                                  :on-error   [:log-rpc-error {:event :settings/get-currencies}]}]]]}]
    (is (match? expected-effects (dispatch [event-id])))))
