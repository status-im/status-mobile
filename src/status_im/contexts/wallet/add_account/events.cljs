(ns status-im.contexts.wallet.add-account.events
  (:require [clojure.string :as string]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/ens-not-found
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:wallet :ui :add-address-to-watch :activity-state] :invalid-ens)
            (assoc-in [:wallet :ui :add-address-to-watch :validated-address] nil))}))

(rf/reg-event-fx
 :wallet/store-address-activity
 (fn [{:keys [db]} [address {:keys [hasActivity]}]]
   (let [registered-addresses        (-> db :wallet :accounts keys set)
         address-already-registered? (registered-addresses address)]
     (if address-already-registered?
       {:db (-> db
                (assoc-in [:wallet :ui :add-address-to-watch :activity-state]
                          :address-already-registered)
                (assoc-in [:wallet :ui :add-address-to-watch :validated-address] nil))}
       (let [state (if hasActivity :has-activity :no-activity)]
         {:db (-> db
                  (assoc-in [:wallet :ui :add-address-to-watch :activity-state] state)
                  (assoc-in [:wallet :ui :add-address-to-watch :validated-address] address))})))))

(rf/reg-event-fx
 :wallet/clear-address-activity
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :add-address-to-watch)}))

(rf/reg-event-fx
 :wallet/get-address-details
 (fn [{db :db} [address-or-ens]]
   (let [ens?           (string/includes? address-or-ens ".")
         chain-id       (network-utils/network->chain-id db :mainnet)
         request-params [chain-id address-or-ens]]
     {:db (-> db
              (assoc-in [:wallet :ui :add-address-to-watch :activity-state] :scanning)
              (assoc-in [:wallet :ui :add-address-to-watch :validated-address] nil))
      :fx [(if ens?
             [:json-rpc/call
              [{:method     "ens_addressOf"
                :params     request-params
                :on-success [:wallet/get-address-details]
                :on-error   [:wallet/ens-not-found]}]]
             [:json-rpc/call
              [{:method     "wallet_getAddressDetails"
                :params     request-params
                :on-success [:wallet/store-address-activity address-or-ens]
                :on-error   #(log/info "failed to get address details"
                                       {:error %
                                        :event :wallet/get-address-details})}]])]})))

(defn add-address-to-watch
  [{:keys [db]} [props]]
  {:fx [[:dispatch [:navigate-to :screen/wallet.add-address-to-watch props]]]
   :db (assoc-in db [:wallet :ui :currently-added-address] props)})

(rf/reg-event-fx :wallet/add-address-to-watch add-address-to-watch)

(defn add-address-to-save
  [{:keys [db]} [props]]
  {:fx [[:dispatch [:open-modal :screen/wallet.add-address-to-save props]]]
   :db (assoc-in db [:wallet :ui :currently-added-address] props)})

(rf/reg-event-fx :wallet/add-address-to-save add-address-to-save)

(defn confirm-add-address-to-save
  [{:keys [db]} [{:keys [ens? address]}]]
  (let [confirm-screen (get-in db [:wallet :ui :currently-added-address :confirm-screen])]
    {:fx [[:dispatch
           [:open-modal confirm-screen]]]
     :db (update-in db [:wallet :ui :currently-added-address] assoc :ens ens? :address address)}))

(rf/reg-event-fx :wallet/confirm-add-address-to-save confirm-add-address-to-save)

(defn confirm-add-address-to-watch
  [{:keys [db]} [{:keys [ens? address]}]]
  (let [confirm-screen (get-in db [:wallet :ui :currently-added-address :confirm-screen])]
    {:fx [[:dispatch [:navigate-to confirm-screen]]]
     :db (update-in db [:wallet :ui :currently-added-address] assoc :ens ens? :address address)}))

(rf/reg-event-fx :wallet/confirm-add-address-to-watch confirm-add-address-to-watch)

