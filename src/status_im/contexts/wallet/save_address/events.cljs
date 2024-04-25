(ns status-im.contexts.wallet.save-address.events
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as wallet.utils]
    [utils.re-frame :as rf]))

(def ^:private <-rpc-spec
  {:isTest          :test
   :chainShortNames :chain-short-names
   :createdAt       :created-at
   :colorId         :customization-color})

(rf/reg-event-fx
 :wallet/save-address
 (fn [_
      [{:keys [address name customization-color on-success on-error chain-short-names ens test?]
        :or   {on-success        (fn [])
               on-error          (fn [])
               name              ""
               ens               ""
               test?             false
               ;; the chain short names should be a string like eth: or eth:arb:opt:
               chain-short-names (let [address-prefixes
                                       (-> address wallet.utils/split-prefix-and-address first)]
                                   (if (string/blank? address-prefixes)
                                     (str constants/mainnet-short-name ":")
                                     address-prefixes))}}]]
   (let [address-to-save {:address         address
                          :name            name
                          :colorId         customization-color
                          :ens             ens
                          :isTest          test?
                          :chainShortNames chain-short-names}]
     {:json-rpc/call [{:method     "wakuext_upsertSavedAddress"
                       :params     [address-to-save]
                       :on-success on-success
                       :on-error   on-error}]})))

(defn- <-rpc
  [saved-addresses]
  (map #(set/rename-keys % <-rpc-spec) saved-addresses))

(rf/reg-event-fx
 :wallet/get-saved-addresses-success
 (fn [{:keys [db]} [saved-addresses]]
   {:db (assoc-in db [:wallet :saved-addresses] (<-rpc saved-addresses))}))

(rf/reg-event-fx
 :wallet/get-saved-addresses-error
 (fn [{:keys [db]} [err]]
   {:db (assoc db [:wallet :get-saved-addresses-error] err)}))

(rf/reg-event-fx
 :wallet/get-saved-addresses
 (fn [_ _]
   {:json-rpc/call
    [{:method     "wakuext_getSavedAddresses"
      :on-success [:wallet/get-saved-addresses-success]
      :on-error   [:wallet/get-saved-addresses-error]}]}))
