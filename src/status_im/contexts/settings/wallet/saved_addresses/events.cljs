(ns status-im.contexts.settings.wallet.saved-addresses.events
  (:require
    [status-im.contexts.wallet.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn save-address
  [{:keys [db]}
   [{:keys [address name customization-color ens edit?]}]]
  (let [on-error [:wallet/add-saved-address-failed]
        on-success
        (if edit?
          [:wallet/edit-saved-address-success]
          [:wallet/add-saved-address-success])
        test-networks-enabled? (boolean (get-in db [:profile/profile :test-networks-enabled?]))
        address-to-save {:address address
                         :name    name
                         :colorId customization-color
                         :ens     ens
                         :isTest  test-networks-enabled?}]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_upsertSavedAddress"
             :params     [address-to-save]
             :on-success on-success
             :on-error   on-error}]]]}))

(rf/reg-event-fx :app/save-address save-address)

(defn delete-saved-address-success
  [{:keys [db]} [{:keys [address test-networks-enabled? toast-message]}]]
  (let [db-key        (if test-networks-enabled? :test :prod)
        saved-address (get-in db [:wallet :saved-addresses db-key address])]
    {:fx [[:dispatch [:domain/reconcile-saved-addresses [(assoc saved-address :removed? true)]]]
          [:dispatch [:hide-bottom-sheet]]
          [:dispatch-later
           {:ms       100
            :dispatch [:toasts/upsert
                       {:type  :positive
                        :theme :dark
                        :text  toast-message}]}]]}))

(rf/reg-event-fx :wallet/delete-saved-address-success delete-saved-address-success)

(defn delete-saved-address-failed
  [_ [error]]
  {:fx [[:dispatch [:hide-bottom-sheet]]
        [:dispatch-later
         {:ms       100
          :dispatch [:toasts/upsert
                     {:type  :negative
                      :theme :dark
                      :text  error}]}]]})

(rf/reg-event-fx :wallet/delete-saved-address-failed delete-saved-address-failed)

(defn delete-saved-address
  [{:keys [db]} [{:keys [address toast-message]}]]
  (let [test-networks-enabled? (boolean (get-in db [:profile/profile :test-networks-enabled?]))]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_deleteSavedAddress"
             :params     [address test-networks-enabled?]
             :on-success [:wallet/delete-saved-address-success
                          {:address                address
                           :test-networks-enabled? test-networks-enabled?
                           :toast-message          toast-message}]
             :on-error   [:wallet/delete-saved-address-failed]}]]]}))

(rf/reg-event-fx :wallet/delete-saved-address delete-saved-address)

(defn add-saved-address-success
  [_]
  {:fx [[:dispatch [:infra/get-saved-addresses]]
        [:dispatch [:dismiss-modal :screen/settings.add-address-to-save]]
        [:dispatch [:dismiss-modal :screen/settings.save-address]]
        [:dispatch-later
         {:ms       100
          :dispatch [:toasts/upsert
                     {:type  :positive
                      :theme :dark
                      :text  (i18n/label :t/address-saved)}]}]]})

(rf/reg-event-fx :wallet/add-saved-address-success add-saved-address-success)

(defn edit-saved-address-success
  [_]
  {:fx [[:dispatch [:infra/get-saved-addresses]]
        [:dispatch [:dismiss-modal :screen/settings.edit-saved-address]]
        [:dispatch-later
         {:ms       100
          :dispatch [:toasts/upsert
                     {:type  :positive
                      :theme :dark
                      :text  (i18n/label :t/address-edited)}]}]]})

(rf/reg-event-fx :wallet/edit-saved-address-success edit-saved-address-success)

(defn add-saved-address-failed
  [_ [error]]
  {:fx [[:dispatch [:infra/saved-addresses-rpc-error :add-save-address error]]
        [:dispatch
         [:toasts/upsert
          {:type  :negative
           :theme :dark
           :text  error}]]]})

(rf/reg-event-fx :wallet/add-saved-address-failed add-saved-address-failed)

(defn check-remaining-capacity-for-saved-addresses
  [{:keys [db]} [{:keys [on-success on-error]}]]
  (let [test-networks-enabled? (boolean (get-in db [:profile/profile :test-networks-enabled?]))]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_remainingCapacityForSavedAddresses"
             :params     [test-networks-enabled?]
             :on-success on-success
             :on-error   on-error}]]]}))

(rf/reg-event-fx :wallet/check-remaining-capacity-for-saved-addresses
 check-remaining-capacity-for-saved-addresses)
