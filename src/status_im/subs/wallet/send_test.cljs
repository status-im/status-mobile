(ns status-im.subs.wallet.send-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.send
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/send-tab
  [sub-name]
  (testing "returns active tab for selecting address"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :select-address-tab] :tabs/recent)
    (is (= :tabs/recent (rf/sub [sub-name])))))
