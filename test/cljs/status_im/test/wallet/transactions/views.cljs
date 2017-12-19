(ns status-im.test.wallet.transactions.views
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.ui.screens.wallet.transactions.views :as views]))

(deftest filtered-transaction?
  (is (not (true? (views/filtered-transaction? {:type :inbound} {:type [{:id :outbound :checked? true}]}))))
  (is (not (true? (views/filtered-transaction? {:type :inbound} {:type [{:id :inbound :checked? false}]}))))
  (is (true? (views/filtered-transaction? {:type :inbound} {:type [{:id :inbound :checked? true}]})))
  (is (true? (views/filtered-transaction? {:type :inbound} {:type [{:id :outbound :checked? true} {:id :inbound :checked? true}]}))))

(deftest update-transactions
  (is (= {:data '()} (views/update-transactions {:data {:type :inbound}} {:type [{:id :outbound :checked? true}]}))))