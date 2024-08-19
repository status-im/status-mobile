(ns status-im.contexts.wallet.wallet-connect.responding-events-test
  (:require
    [cljs.test :refer-macros [is]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
    status-im.contexts.wallet.wallet-connect.responding-events
    [test-helpers.unit :as h]
    [utils.transforms :as transforms]))

(h/deftest-event :wallet-connect/redirect-to-dapp
  [event-id dispatch]
  (let [current-request {:event {:verifyContext {:verified {:origin "https://dapp.com"}}}}
        session-json    "{\"peer\":{\"metadata\":{\"redirect\":{\"native\":\"native://redirect-url\"}}}}"
        sessions        [{:url         "https://dapp.com"
                          :sessionJson session-json}]]
    (reset! rf-db/app-db {:wallet-connect {:current-request current-request
                                           :sessions        sessions}})
    (with-redefs [wallet-connect-core/get-current-request-dapp
                  (fn [_ _] (first sessions))
                  transforms/json->clj
                  (fn [json] (js/JSON.parse json))
                  wallet-connect-core/get-dapp-redirect-url
                  (fn [_] "native://redirect-url")]

      (is (match? {:fx [[:open-url "native://redirect-url"]]}
                  (dispatch [event-id]))))))
