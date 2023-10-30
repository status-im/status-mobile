(ns status-im2.contexts.wallet.events
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :as cske]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [react-native.background-timer :as background-timer]
    [status-im2.common.data-store.wallet :as data-store]
    [status-im2.contexts.wallet.temp :as temp]
    [taoensso.timbre :as log]
    [utils.ethereum.chain :as chain]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as types]))

(rf/defn get-wallet-token
  {:events [:wallet/get-wallet-token]}
  [{:keys [db]}]
  (let [params (map :address (:profile/wallet-accounts db))]
    {:json-rpc/call [{:method     "wallet_getWalletToken"
                      :params     [params]
                      :on-success #(rf/dispatch [:wallet/get-wallet-token-success %])
                      :on-error   (fn [error]
                                    (log/info "failed to get wallet token"
                                              {:event  :wallet/get-wallet-token
                                               :error  error
                                               :params params}))}]}))

(rf/defn get-wallet-token-success
  {:events [:wallet/get-wallet-token-success]}
  [{:keys [db]} data]
  {:db (assoc db
              :wallet/tokens          data
              :wallet/tokens-loading? false)})

(rf/defn scan-address-success
  {:events [:wallet/scan-address-success]}
  [{:keys [db]} address]
  {:db (assoc db :wallet/scanned-address address)})

(rf/defn clean-scanned-address
  {:events [:wallet/clean-scanned-address]}
  [{:keys [db]}]
  {:db (dissoc db :wallet/scanned-address :wallet/send-address)})

(rf/reg-event-fx :wallet/create-derived-addresses
 (fn [{:keys [db]} [password {:keys [path]} on-success]]
   (let [{:keys [wallet-root-address]} (:profile/profile db)
         sha3-pwd                      (native-module/sha3 (str (security/safe-unmask-data password)))]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getDerivedAddresses"
              :params     [sha3-pwd wallet-root-address [path]]
              :on-success on-success
              :on-error   #(log/info "failed to derive address " %)}]]]})))

(rf/reg-event-fx :wallet/add-account
 (fn [{:keys [db]} [password {:keys [emoji account-name color]} {:keys [public-key address path]}]]
   (let [key-uid        (get-in db [:profile/profile :key-uid])
         sha3-pwd       (native-module/sha3 (security/safe-unmask-data password))
         account-config {:key-uid    key-uid
                         :wallet     false
                         :chat       false
                         :type       :generated
                         :name       account-name
                         :emoji      emoji
                         :path       path
                         :address    address
                         :public-key public-key
                         :colorID    color}]
     {:fx [[:json-rpc/call
            [{:method     "accounts_addAccount"
              :params     [sha3-pwd account-config]
              :on-success #(rf/dispatch [:navigate-to :wallet-accounts])
              :on-error   #(log/info "failed to create account " %)}]]]})))

(rf/reg-event-fx :wallet/derive-address-and-add-account
 (fn [_ [password account-details]]
   (let [on-success (fn [derived-adress-details]
                      (rf/dispatch [:wallet/add-account password account-details
                                    (first derived-adress-details)]))]
     {:fx [[:dispatch [:wallet/create-derived-addresses password account-details on-success]]]})))

(rf/defn get-ethereum-chains
  {:events [:wallet/get-ethereum-chains]}
  [{:keys [db]}]
  {:fx [[:json-rpc/call
         [{:method     "wallet_getEthereumChains"
           :params     []
           :on-success [:wallet/get-ethereum-chains-success]
           :on-error   #(log/info "failed to get networks " %)}]]]})

(rf/reg-event-fx
 :wallet/get-ethereum-chains-success
 (fn [{:keys [db]} [data]]
   (let [network-data
         {:test (map #(->> %
                           :Test
                           data-store/<-rpc)
                     data)
          :prod (map #(->> %
                           :Prod
                           data-store/<-rpc)
                     data)}]
     {:db (assoc db :wallet/networks network-data)})))

(def collectibles-request-batch-size 1000)

(defn displayable-collectible?
  [{:keys [image-url animation-url]}]
  (or (not (string/blank? animation-url))
      (not (string/blank? image-url))))

(defn store-collectibles
  [{:keys [db]} [collectibles]]
  (let [stored-collectibles      (get-in db [:wallet :collectibles])
        displayable-collectibles (filter displayable-collectible? collectibles)]
    {:db (assoc-in db
          [:wallet :collectibles]
          (reduce conj displayable-collectibles stored-collectibles))}))

(rf/reg-event-fx :wallet/store-collectibles store-collectibles)

(defn clear-stored-collectibles
  [{:keys [db]}]
  {:db (update db :wallet dissoc :collectibles)})

(rf/reg-event-fx :wallet/clear-stored-collectibles clear-stored-collectibles)

(rf/reg-event-fx :wallet/request-collectibles
 (fn [{:keys [db]} [{:keys [start-at-index new-request?]}]]
   (let [request-params [0
                         [(chain/chain-id db)]
                         (map :address (:profile/wallet-accounts db))
                         start-at-index
                         collectibles-request-batch-size]]
     {:json-rpc/call [{:method     "wallet_filterOwnedCollectiblesAsync"
                       :params     request-params
                       :on-success #()
                       :on-error   (fn [error]
                                     (log/error "failed to request collectibles"
                                                {:event  :wallet/request-collectibles
                                                 :error  error
                                                 :params request-params}))}]
      :fx            (if new-request?
                       [[:dispatch [:wallet/clear-stored-collectibles]]]
                       [])})))

(rf/reg-event-fx :wallet/owned-collectibles-filtering-done
 (fn [_ [{:keys [message]}]]
   (let [response                               (cske/transform-keys csk/->kebab-case-keyword
                                                                     (types/json->clj message))
         {:keys [collectibles has-more offset]} response
         start-at-index                         (+ offset (count collectibles))]
     {:fx
      [[:dispatch [:wallet/store-collectibles collectibles]]
       (when has-more
         [:dispatch
          [:wallet/request-collectibles
           {:start-at-index start-at-index}]])]})))

(rf/reg-event-fx :wallet/fetch-address-suggestions
 (fn [{:keys [db]} [address]]
   {:db (assoc db
               :wallet/local-suggestions
               (cond
                 (= address
                    (get-in
                     temp/address-local-suggestion-saved-contact-address-mock
                     [:accounts 0 :address]))
                 [temp/address-local-suggestion-saved-contact-address-mock]
                 (= address
                    (get temp/address-local-suggestion-saved-address-mock
                         :address))
                 [temp/address-local-suggestion-saved-address-mock]
                 :else (temp/find-matching-addresses address))
               :wallet/valid-ens-or-address?
               false)}))

(rf/reg-event-fx :wallet/ens-validation-success
 (fn [{:keys [db]} [ens]]
   {:db (assoc db
               :wallet/local-suggestions     (if (= ens
                                                    (:ens temp/ens-local-suggestion-saved-address-mock))
                                               [temp/ens-local-suggestion-saved-address-mock]
                                               [temp/ens-local-suggestion-mock])
               :wallet/valid-ens-or-address? true)}))

(rf/reg-event-fx :wallet/address-validation-success
 (fn [{:keys [db]} [_]]
   {:db (assoc db :wallet/valid-ens-or-address? true)}))

(rf/reg-event-fx :wallet/validate-address
 (fn [{:keys [db]} [address]]
   (let [current-timeout (get db :wallet/search-timeout)
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/address-validation-success address])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db
                 :wallet/valid-ens-or-address? false
                 :wallet/search-timeout        timeout)})))

(rf/reg-event-fx :wallet/validate-ens
 (fn [{:keys [db]} [ens]]
   (let [current-timeout (get db :wallet/search-timeout)
         timeout         (background-timer/set-timeout
                          #(rf/dispatch [:wallet/ens-validation-success ens])
                          2000)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db
                 :wallet/valid-ens-or-address? false
                 :wallet/search-timeout        timeout)})))

(rf/reg-event-fx :wallet/clean-local-suggestions
 (fn [{:keys [db]}]
   (let [current-timeout (get db :wallet/search-timeout)]
     (background-timer/clear-timeout current-timeout)
     {:db (assoc db :wallet/local-suggestions [] :wallet/valid-ens-or-address? false)})))

(rf/reg-event-fx :wallet/select-send-address
 (fn [{:keys [db]} [address]]
   {:db (assoc db :wallet/send-address address)}))
