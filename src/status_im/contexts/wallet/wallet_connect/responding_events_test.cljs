(ns status-im.contexts.wallet.wallet-connect.events.session-responses-test
  (:require
    [cljs.test :refer-macros [is]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    status-im.contexts.wallet.wallet-connect.events.session-responses
    [status-im.contexts.wallet.wallet-connect.utils.data-transformations :as data-transformations]
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
    (with-redefs [data-transformations/get-current-request-dapp
                  (fn [_ _] (first sessions))
                  transforms/json->clj
                  (fn [json] (js/JSON.parse json))
                  data-transformations/get-dapp-redirect-url
                  (fn [_] "native://redirect-url")]

      (is (match? {:fx [[:open-url "native://redirect-url"]]}
                  (dispatch [event-id]))))))
