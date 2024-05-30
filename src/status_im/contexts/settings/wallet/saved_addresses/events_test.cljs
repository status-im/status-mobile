(ns status-im.contexts.settings.wallet.saved-addresses.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.settings.wallet.saved-addresses.events :as events]))

(deftest get-saved-addresses
  (testing "get saved addresses - dispatches RPC call"
    (let [cofx        {:db {}}
          effects     (events/get-saved-addresses cofx)
          result-fx   (:fx effects)
          expected-fx [[:json-rpc/call
                        [{:method     "wakuext_getSavedAddresses"
                          :on-success [:wallet/get-saved-addresses-success]
                          :on-error   [:wallet/saved-addresses-rpc-error :get-saved-addresses]}]]]]
      (is (match? expected-fx result-fx)))))

(def sa-1
  {:isTest           false
   :address          "0x1"
   :mixedcaseAddress "0x1"
   :chainShortNames  "eth:arb1:oeth:"
   :name             "Amy"
   :createdAt        1716826806
   :ens              ""
   :colorId          "purple"
   :removed          false})

(def sa-2
  {:isTest           true
   :address          "0x2"
   :mixedcaseAddress "0x2"
   :chainShortNames  "eth:arb1:oeth:"
   :name             "Bob"
   :createdAt        1716826714
   :ens              ""
   :colorId          "blue"
   :removed          false})

(deftest get-saved-addresses-success
  (testing "no saved addresses"
    (let [cofx        {:db {}}
          effects     (events/get-saved-addresses-success cofx nil)
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses {:test {}
                                                  :prod {}}}}]
      (is (match? expected-db result-db))))

  (testing "one test saved address"
    (let [cofx        {:db {}}
          effects     (events/get-saved-addresses-success cofx [[sa-2]])
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses
                                {:test {"0x2" {:test?                     true
                                               :address                   "0x2"
                                               :mixedcase-address         "0x2"
                                               :chain-short-names         "eth:arb1:oeth:"
                                               :has-ens?                  false
                                               :network-preferences-names #{:arbitrum
                                                                            :optimism
                                                                            :mainnet}
                                               :name                      "Bob"
                                               :created-at                1716826714
                                               :ens                       ""
                                               :customization-color       :blue
                                               :removed?                  false}}
                                 :prod {}}}}]
      (is (match? expected-db result-db))))

  (testing "two saved addresses (test and prod)"
    (let [cofx        {:db {}}
          effects     (events/get-saved-addresses-success cofx [[sa-1 sa-2]])
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses
                                {:test {"0x2" {:test?                     true
                                               :address                   "0x2"
                                               :mixedcase-address         "0x2"
                                               :chain-short-names         "eth:arb1:oeth:"
                                               :network-preferences-names #{:arbitrum
                                                                            :optimism
                                                                            :mainnet}
                                               :has-ens?                  false
                                               :name                      "Bob"
                                               :created-at                1716826714
                                               :ens                       ""
                                               :customization-color       :blue
                                               :removed?                  false}}
                                 :prod {"0x1" {:test?                     false
                                               :address                   "0x1"
                                               :mixedcase-address         "0x1"
                                               :chain-short-names         "eth:arb1:oeth:"
                                               :network-preferences-names #{:arbitrum
                                                                            :optimism
                                                                            :mainnet}
                                               :has-ens?                  false
                                               :name                      "Amy"
                                               :created-at                1716826806
                                               :ens                       ""
                                               :customization-color       :purple
                                               :removed?                  false}}}}}]
      (is (match? expected-db result-db)))))

(deftest save-address
  (testing "save address - dispatches RPC call"
    (let [test-networks-enabled? false
          cofx                   {:db {:profile/profile {:test-networks-enabled?
                                                         test-networks-enabled?}}}
          on-success             [:some-success-event]
          on-error               [:some-failure-event]
          name                   "Bob"
          address                "0x3"
          ens                    "bobby.eth"
          customization-color    :yellow
          chain-short-names      "eth:arb1:oeth:"
          args                   {:on-success          on-success
                                  :on-error            on-error
                                  :name                name
                                  :address             address
                                  :customization-color customization-color
                                  :ens                 ens
                                  :chain-short-names   chain-short-names}
          effects                (events/save-address cofx [args])
          result-fx              (:fx effects)
          expected-fx            [[:json-rpc/call
                                   [{:method     "wakuext_upsertSavedAddress"
                                     :params     [{:address         address
                                                   :name            name
                                                   :colorId         customization-color
                                                   :ens             ens
                                                   :isTest          test-networks-enabled?
                                                   :chainShortNames chain-short-names}]
                                     :on-success on-success
                                     :on-error   on-error}]]]]
      (is (match? expected-fx result-fx)))))

(deftest delete-saved-addresses
  (testing "delete saved addresses - dispatches RPC call"
    (let [test-networks-enabled? true
          cofx                   {:db {:profile/profile {:test-networks-enabled?
                                                         test-networks-enabled?}}}
          address                "0x1f69b0904160bf1ce98dabeaf9c2fe147569498d"
          toast-message          "Saved addresses deleted successfully"
          args                   {:address       address
                                  :toast-message toast-message}
          effects                (events/delete-saved-address cofx [args])
          result-fx              (:fx effects)
          expected-fx            [[:json-rpc/call
                                   [{:method     "wakuext_deleteSavedAddress"
                                     :params     [address test-networks-enabled?]
                                     :on-success [:wallet/delete-saved-address-success toast-message]
                                     :on-error   [:wallet/delete-saved-address-failed]}]]]]
      (is (match? expected-fx result-fx)))))
