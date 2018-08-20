(ns status-im.test.wallet.transactions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.datetime :as time]
            [status-im.models.transactions :as wallet.transactions]))

(deftest test-store-chat-transaction-hash
  (is (= (wallet.transactions/store-chat-transaction-hash "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"
                                                          {:db {:wallet {:chat-transactions #{"0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"}}}})
         {:db {:wallet {:chat-transactions #{"0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b" "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"}}}})))

(deftest test-load-missing-chat-transactions
  (is (= (wallet.transactions/load-missing-chat-transactions {:db {:wallet {:transactions {"0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"
                                                                                           {:confirmations "15"}}}
                                                                   :chats  {"0x0462203ba96495c4caed83b46b01fe9b8f27af300f46cc944c6c47141969337ecfc4566dd5910323763816d350d3256a3d8f2f2a6536cfa120733aeea2efb11860"
                                                                            {:messages {"0xfc6e31d35f5a722b8aa185c8c7d1cd798e546e8dcd97adcc8305ca1a3c044f0d"
                                                                                        {:content-type "command"
                                                                                         :content      {:params {:tx-hash "0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"}}}}}
                                                                            "0x04d9bbe8e00318a70108980b45ba45c91cd406cdd7ffeb043be0ce4ec026fed3854515345b4bbce4c1694cc99240ee5fda93101ca861c3ceef548dedb5ec2a983a"
                                                                            {:messages {"0x1f8a9472997b58435a43776ec5a89a12e42891f71064fd890ef3d87dda41fabf"
                                                                                        {:content-type "command"
                                                                                         :content      {:params {:tx-hash "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"}}}}}}}})

         {:db {:wallet {:transactions      {"0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"
                                            {:confirmations "15"}}
                        :chat-transactions #{"0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"}}
               :chats  {"0x0462203ba96495c4caed83b46b01fe9b8f27af300f46cc944c6c47141969337ecfc4566dd5910323763816d350d3256a3d8f2f2a6536cfa120733aeea2efb11860"
                        {:messages {"0xfc6e31d35f5a722b8aa185c8c7d1cd798e546e8dcd97adcc8305ca1a3c044f0d"
                                    {:content-type "command"
                                     :content      {:params {:tx-hash "0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"}}}}}
                        "0x04d9bbe8e00318a70108980b45ba45c91cd406cdd7ffeb043be0ce4ec026fed3854515345b4bbce4c1694cc99240ee5fda93101ca861c3ceef548dedb5ec2a983a"
                        {:messages {"0x1f8a9472997b58435a43776ec5a89a12e42891f71064fd890ef3d87dda41fabf"
                                    {:content-type "command"
                                     :content      {:params {:tx-hash "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"}}}}}}}})))

(deftest test-sync
  (with-redefs [time/timestamp (constantly 1531734120829)]
    (testing "update in progress"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :online
                                             :wallet         {:transactions          {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                      {:confirmations "0"}}
                                                              :transactions-loading? true}}})
             {:utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "app is offline"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :offline
                                             :wallet         {:transactions {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                             {:confirmations "0"}}}}})
             {:utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "app is in background"
      (is (= (wallet.transactions/sync {:db {:app-state      "background"
                                             :network-status :online
                                             :wallet         {:transactions {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                             {:confirmations "0"}}}}})
             {:utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "last request was made recently"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :online
                                             :wallet         {:transactions                 {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                             {:confirmations "10"}}
                                                              :transactions-last-updated-at 1531734119729
                                                              :transactions-loading?        false}}})
             {:utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "transaction has 0 confirmations"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :online
                                             :wallet         {:transactions          {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                      {:confirmations "0"}}
                                                              :transactions-loading? false}}})
             {:get-transactions     {:account-id      nil
                                     :token-addresses (),
                                     :chain           nil
                                     :web3            nil
                                     :success-event   :update-transactions-success
                                     :error-event     :update-transactions-fail}
              :db                   {:app-state      "active"
                                     :network-status :online
                                     :wallet         {:transactions                 {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                     {:confirmations "0"}}
                                                      :transactions-loading?        true
                                                      :transactions-last-updated-at 1531734120829
                                                      :errors                       nil}}
              :utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "transaction has 10 confirmations"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :online
                                             :wallet         {:transactions          {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                      {:confirmations "10"}}
                                                              :transactions-loading? false}}})
             {:get-transactions     {:account-id      nil
                                     :token-addresses (),
                                     :chain           nil
                                     :web3            nil
                                     :success-event   :update-transactions-success
                                     :error-event     :update-transactions-fail}
              :db                   {:app-state      "active"
                                     :network-status :online
                                     :wallet         {:transactions                 {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                     {:confirmations "10"}}
                                                      :transactions-loading?        true
                                                      :transactions-last-updated-at 1531734120829
                                                      :errors                       nil}}
              :utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))
    (testing "chat transaction is missing from wallet transactions"
      (is (= (wallet.transactions/sync {:db {:app-state      "active"
                                             :network-status :online
                                             :wallet         {:transactions          {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                      {:confirmations "15"}
                                                                                      "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"
                                                                                      {:confirmations "20"}}
                                                              :chat-transactions     #{"0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"
                                                                                       "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"}
                                                              :transactions-loading? false}}})
             {:get-transactions     {:account-id      nil
                                     :token-addresses (),
                                     :chain           nil
                                     :web3            nil
                                     :success-event   :update-transactions-success
                                     :error-event     :update-transactions-fail}
              :db                   {:app-state      "active"
                                     :network-status :online
                                     :wallet         {:transactions                 {"0x59bd7cca850671c6bd2b6f2f75e001d8b7a1a254f0ba4a5660041ba7c2f19295"
                                                                                     {:confirmations "15"}
                                                                                     "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"
                                                                                     {:confirmations "20"}}
                                                      :chat-transactions            #{"0x0873923e4d8b39ccfeb8c1af9701a9da02fdc76947a48de7c5df1540f77fdc5b"
                                                                                      "0x318f566edd98eb29965067d3394c555050bf9f8e20183792c7f1a6bbc1bb34db"}
                                                      :transactions-loading?        true
                                                      :transactions-last-updated-at 1531734120829
                                                      :errors                       nil}}
              :utils/dispatch-later [{:ms       wallet.transactions/sync-interval-ms
                                      :dispatch [:sync-wallet-transactions]}]})))))
