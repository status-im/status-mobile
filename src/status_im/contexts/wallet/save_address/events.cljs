(ns status-im.contexts.wallet.save-address.events
  (:require
    [status-im.constants :as constants]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/save-address
 (fn [_
      [{:keys [address name customization-color on-success on-error chain-short-names ens test?]
        :or   {on-success        (fn [])
               on-error          (fn [])
               name              ""
               ens               ""
               test?             false
               ;; the chain short names should be a string like eth: or eth:arb:oeth:
               chain-short-names (str constants/mainnet-short-name ":")}}]]
   (let [address-to-save {:address           address
                          :name              name
                          :color-id          customization-color
                          :ens               ens
                          :is-test           test?
                          :chain-short-names chain-short-names}]
     {:json-rpc/call
      [{:method     "wakuext_upsertSavedAddress"
        :params     [address-to-save]
        :on-success on-success
        :on-error   on-error}]})))

(rf/reg-event-fx
 :wallet/get-saved-addresses-success
 (fn [{:keys [db]} [saved-addresses]]
   {:db (assoc-in db [:wallet :saved-addresses] saved-addresses)}))

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
