(ns status-im.contexts.settings.wallet.saved-addresses.events
  (:require [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/remove-account-success
 (fn [_ [toast-message _]]
   {:fx [[:dispatch [:wallet/get-saved-addresses]]
         [:dispatch-later
          {:ms       100
           :dispatch [:wallet/show-account-deleted-toast toast-message]}]]}))

(rf/defn delete-saved-address
  {:events [:wallet/delete-saved-address]}
  [_ {:keys [address test? toast-message]}]
  {:json-rpc/call
   [{:method     "wakuext_deleteSavedAddress"
     :params     [address test?]
     :on-success [:wallet/remove-account-success toast-message]
     :on-error   #(log/error "Failed to delete saved address" %)}]})
