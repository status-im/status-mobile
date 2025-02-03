(ns status-im.contexts.settings.wallet.saved-addresses.events
  (:require
    [status-im.contexts.wallet.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn save-address
  [{:keys [db]}
   [{:keys [address name customization-color on-success on-error chain-short-names ens]}]]
  (let [test-networks-enabled? (boolean (get-in db [:profile/profile :test-networks-enabled?]))
        address-to-save        {:address         address
                                :name            name
                                :colorId         customization-color
                                :ens             ens
                                :isTest          test-networks-enabled?
                                :chainShortNames chain-short-names}]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_upsertSavedAddress"
             :params     [address-to-save]
             :on-success on-success
             :on-error   on-error}]]]}))

(rf/reg-event-fx :wallet/save-address save-address)

(defn- update-saved-addresses
  [saved-addresses-db new-saved-addresses]
  (reduce
   (fn [acc {:keys [address removed? test?] :as saved-address}]
     (let [db-key (if test? :test :prod)]
       (if removed?
         (update acc db-key dissoc address)
         (assoc-in acc [db-key address] saved-address))))
   (or saved-addresses-db
       {:test {}
        :prod {}})
   new-saved-addresses))

(defn reconcile-saved-addresses
  [{:keys [db]} [saved-addresses]]
  {:db (update-in db [:wallet :saved-addresses] update-saved-addresses saved-addresses)})

(rf/reg-event-fx :wallet/reconcile-saved-addresses reconcile-saved-addresses)

(defn get-saved-addresses-success
  [_ [raw-saved-addresses]]
  (let [saved-addresses (data-store/rpc->saved-addresses raw-saved-addresses)]
    {:fx [[:dispatch [:wallet/reconcile-saved-addresses saved-addresses]]]}))

(rf/reg-event-fx :wallet/get-saved-addresses-success get-saved-addresses-success)

(defn saved-addresses-rpc-error
  [_ [action error]]
  (log/warn (str "[wallet] [saved-addresses] Failed to " action)
            {:error error}))

(rf/reg-event-fx :wallet/saved-addresses-rpc-error saved-addresses-rpc-error)

(defn get-saved-addresses
  [_]
  {:fx [[:json-rpc/call
         [{:method     "wakuext_getSavedAddresses"
           :on-success [:wallet/get-saved-addresses-success]
           :on-error   [:wallet/saved-addresses-rpc-error :get-saved-addresses]}]]]})

(rf/reg-event-fx :wallet/get-saved-addresses get-saved-addresses)

(defn delete-saved-address-success
  [{:keys [db]} [{:keys [address test-networks-enabled? toast-message]}]]
  (let [db-key        (if test-networks-enabled? :test :prod)
        saved-address (get-in db [:wallet :saved-addresses db-key address])]
    {:fx [[:dispatch [:wallet/reconcile-saved-addresses [(assoc saved-address :removed? true)]]]
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
  [_ [toast-message]]
  {:fx [[:dispatch [:wallet/get-saved-addresses]]
        [:dispatch [:dismiss-modal :screen/settings.add-address-to-save]]
        [:dispatch [:dismiss-modal :screen/settings.save-address]]
        [:dispatch-later
         {:ms       100
          :dispatch [:toasts/upsert
                     {:type  :positive
                      :theme :dark
                      :text  toast-message}]}]]})

(rf/reg-event-fx :wallet/add-saved-address-success add-saved-address-success)

(defn edit-saved-address-success
  [_]
  {:fx [[:dispatch [:wallet/get-saved-addresses]]
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
  {:fx [[:dispatch [:wallet/saved-addresses-rpc-error :add-save-address error]]
        [:dispatch
         [:toasts/upsert
          {:type  :negative
           :theme :dark
           :text  error}]]]})

(rf/reg-event-fx :wallet/add-saved-address-failed add-saved-address-failed)

(defn set-address-to-save
  [{:keys [db]} [args]]
  {:db (assoc-in db [:wallet :ui :saved-address] args)})

(rf/reg-event-fx :wallet/set-address-to-save set-address-to-save)

(defn clear-address-to-save
  [{:keys [db]}]
  {:db (update-in db [:wallet :ui] dissoc :saved-address)})

(rf/reg-event-fx :wallet/clear-address-to-save clear-address-to-save)
