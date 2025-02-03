(ns status-im.contexts.settings.wallet.saved-addresses.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.settings.wallet.saved-addresses.events :as events]
    [status-im.contexts.wallet.data-store :as data-store]))

(deftest get-saved-addresses-test
  (testing "get saved addresses - dispatches RPC call"
    (let [cofx        {:db {}}
          effects     (events/get-saved-addresses cofx)
          result-fx   (:fx effects)
          expected-fx [[:json-rpc/call
                        [{:method     "wakuext_getSavedAddresses"
                          :on-success [:wallet/get-saved-addresses-success]
                          :on-error   [:wallet/saved-addresses-rpc-error :get-saved-addresses]}]]]]
      (is (match? expected-fx result-fx)))))

(def saved-address-rpc-1
  {:isTest           false
   :address          "0x1"
   :mixedcaseAddress "0x1"
   :chainShortNames  "eth:arb1:oeth:"
   :name             "Amy"
   :createdAt        1716826806
   :ens              ""
   :colorId          "purple"
   :removed          false})

(def saved-address-1
  {:test?                     true
   :address                   "0x1"
   :mixedcase-address         "0x1"
   :chain-short-names         "eth:oeth:"
   :network-preferences-names `(:mainnet :optimism)
   :name                      "Bob"
   :created-at                1716826714
   :ens                       ""
   :ens?                      false
   :customization-color       :blue
   :removed?                  false})

(def saved-address-2
  {:test?                     false
   :address                   "0x2"
   :mixedcase-address         "0x2"
   :chain-short-names         "eth:arb1:oeth:"
   :network-preferences-names `(:mainnet :arbitrum :optimism)
   :name                      "Alicia Keys"
   :created-at                1716826806
   :ens                       "alicia.eth"
   :ens?                      true
   :customization-color       :purple
   :removed?                  false})

(deftest get-saved-addresses-success-test
  (let [cofx                {:db {}}
        raw-saved-addresses [saved-address-rpc-1]
        effects             (events/get-saved-addresses-success cofx [raw-saved-addresses])
        saved-addresses     (data-store/rpc->saved-addresses raw-saved-addresses)
        result-fx           (:fx effects)
        expected-fx         [[:dispatch [:wallet/reconcile-saved-addresses saved-addresses]]]]
    (is (match? expected-fx result-fx))))

(deftest reconcile-saved-addresses-test
  (testing "no saved addresses"
    (let [cofx        {:db {}}
          effects     (events/reconcile-saved-addresses cofx nil)
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses {:test {}
                                                  :prod {}}}}]
      (is (match? expected-db result-db))))

  (testing "one test saved address"
    (let [cofx        {:db {}}
          effects     (events/reconcile-saved-addresses cofx [[saved-address-1]])
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses
                                {:test {"0x1" {:test?                     true
                                               :address                   "0x1"
                                               :mixedcase-address         "0x1"
                                               :chain-short-names         "eth:oeth:"
                                               :ens?                      false
                                               :network-preferences-names `(:mainnet :optimism)
                                               :name                      "Bob"
                                               :created-at                1716826714
                                               :ens                       ""
                                               :customization-color       :blue
                                               :removed?                  false}}
                                 :prod {}}}}]
      (is (match? expected-db result-db))))

  (testing "two saved addresses (test and prod)"
    (let [cofx        {:db {}}
          effects     (events/reconcile-saved-addresses cofx [[saved-address-1 saved-address-2]])
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses
                                {:test {"0x1" {:test?                     true
                                               :address                   "0x1"
                                               :mixedcase-address         "0x1"
                                               :chain-short-names         "eth:oeth:"
                                               :ens?                      false
                                               :network-preferences-names `(:mainnet :optimism)
                                               :name                      "Bob"
                                               :created-at                1716826714
                                               :ens                       ""
                                               :customization-color       :blue
                                               :removed?                  false}}
                                 :prod {"0x2" {:test?                     false
                                               :address                   "0x2"
                                               :mixedcase-address         "0x2"
                                               :chain-short-names         "eth:arb1:oeth:"
                                               :network-preferences-names `(:mainnet :arbitrum :optimism)
                                               :ens?                      true
                                               :name                      "Alicia Keys"
                                               :created-at                1716826806
                                               :ens                       "alicia.eth"
                                               :customization-color       :purple
                                               :removed?                  false}}}}}]
      (is (match? expected-db result-db))))

  (testing "remove a test saved addresses"
    (let [cofx        {:db {:wallet {:saved-addresses
                                     {:test {"0x1" {:test?                     true
                                                    :address                   "0x1"
                                                    :mixedcase-address         "0x1"
                                                    :chain-short-names         "eth:oeth:"
                                                    :ens?                      false
                                                    :network-preferences-names `(:mainnet :optimism)
                                                    :name                      "Bob"
                                                    :created-at                1716826714
                                                    :ens                       ""
                                                    :customization-color       :blue
                                                    :removed?                  false}}
                                      :prod {"0x2" {:test?                     false
                                                    :address                   "0x2"
                                                    :mixedcase-address         "0x2"
                                                    :chain-short-names         "eth:arb1:oeth:"
                                                    :network-preferences-names `(:mainnet :arbitrum
                                                                                          :optimism)
                                                    :ens?                      true
                                                    :name                      "Alicia Keys"
                                                    :created-at                1716826806
                                                    :ens                       "alicia.eth"
                                                    :customization-color       :purple
                                                    :removed?                  false}}}}}}
          effects     (events/reconcile-saved-addresses cofx
                                                        [[(assoc saved-address-1 :removed? true)
                                                          saved-address-2]])
          result-db   (:db effects)
          expected-db {:wallet {:saved-addresses
                                {:test {}
                                 :prod {"0x2" {:test?                     false
                                               :address                   "0x2"
                                               :mixedcase-address         "0x2"
                                               :chain-short-names         "eth:arb1:oeth:"
                                               :network-preferences-names `(:mainnet :arbitrum :optimism)
                                               :ens?                      true
                                               :name                      "Alicia Keys"
                                               :created-at                1716826806
                                               :ens                       "alicia.eth"
                                               :customization-color       :purple
                                               :removed?                  false}}}}}]
      (is (match? expected-db result-db)))))

(deftest save-address-test
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
          args                   {:on-success          on-success
                                  :on-error            on-error
                                  :name                name
                                  :address             address
                                  :customization-color customization-color
                                  :ens                 ens}
          effects                (events/save-address cofx [args])
          result-fx              (:fx effects)
          expected-fx            [[:json-rpc/call
                                   [{:method     "wakuext_upsertSavedAddress"
                                     :params     [{:address address
                                                   :name    name
                                                   :colorId customization-color
                                                   :ens     ens
                                                   :isTest  test-networks-enabled?}]
                                     :on-success on-success
                                     :on-error   on-error}]]]]
      (is (match? expected-fx result-fx)))))

(deftest delete-saved-addresses-test
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
                                     :on-success [:wallet/delete-saved-address-success
                                                  {:address                address
                                                   :test-networks-enabled? test-networks-enabled?
                                                   :toast-message          toast-message}]
                                     :on-error   [:wallet/delete-saved-address-failed]}]]]]
      (is (match? expected-fx result-fx)))))

(deftest add-saved-address-success-test
  (testing "add saved address success test - gets saved addresses, dismiss modals and dispatch toast"
    (let [cofx          {:db {}}
          toast-message "Address saved"
          effects       (events/add-saved-address-success cofx [toast-message])
          result-fx     (:fx effects)
          expected-fx   [[:dispatch [:wallet/get-saved-addresses]]
                         [:dispatch [:dismiss-modal :screen/settings.add-address-to-save]]
                         [:dispatch [:dismiss-modal :screen/settings.save-address]]
                         [:dispatch-later
                          {:ms       100
                           :dispatch [:toasts/upsert
                                      {:type  :positive
                                       :theme :dark
                                       :text  toast-message}]}]]]
      (is (= (count result-fx) 4))
      (is (match? expected-fx result-fx)))))

(deftest edit-saved-address-success-test
  (testing "edit saved address success test - gets saved addresses, dismiss modals and dispatch toast"
    (let [cofx          {:db {}}
          toast-message "Address edited"
          effects       (events/edit-saved-address-success cofx)
          result-fx     (:fx effects)
          expected-fx   [[:dispatch [:wallet/get-saved-addresses]]
                         [:dispatch [:dismiss-modal :screen/settings.edit-saved-address]]
                         [:dispatch-later
                          {:ms       100
                           :dispatch [:toasts/upsert
                                      {:type  :positive
                                       :theme :dark
                                       :text  toast-message}]}]]]
      (is (= (count result-fx) 3))
      (is (match? expected-fx result-fx)))))
