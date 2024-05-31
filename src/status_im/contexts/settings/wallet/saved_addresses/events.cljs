(ns status-im.contexts.settings.wallet.saved-addresses.events
  (:require
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn save-address
  [{:keys [db]}
   [{:keys [address name customization-color on-success on-error chain-short-names ens]
     :or   {on-success          (fn [])
            on-error            (fn [])
            name                ""
            customization-color constants/account-default-customization-color
            ens                 ""
            ;; the chain short names should be a string like eth: or eth:arb:oeth:
            chain-short-names   (str constants/mainnet-short-name ":")}}]]
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

(defn get-saved-addresses-success
  [{:keys [db]} [raw-saved-addresses]]
  (let [saved-addresses (reduce
                         (fn [result {:keys [address test?] :as saved-address}]
                           (assoc-in result [(if test? :test :prod) address] saved-address))
                         {:test {}
                          :prod {}}
                         (data-store/rpc->saved-addresses raw-saved-addresses))]
    {:db (assoc-in db [:wallet :saved-addresses] saved-addresses)}))

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
  [_ [toast-message]]
  {:fx [[:dispatch [:wallet/get-saved-addresses]]
        [:dispatch [:hide-bottom-sheet]]
        [:dispatch-later
         {:ms       100
          :dispatch [:toasts/upsert
                     {:type  :positive
                      :theme :dark
                      :text  toast-message}]}]]})

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
             :on-success [:wallet/delete-saved-address-success toast-message]
             :on-error   [:wallet/delete-saved-address-failed]}]]]}))

(rf/reg-event-fx :wallet/delete-saved-address delete-saved-address)
