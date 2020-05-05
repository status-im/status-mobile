(ns status-im.multiaccounts.update.core
  (:require [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn send-multiaccount-update [{:keys [db] :as cofx}]
  (let [multiaccount (:multiaccount db)
        {:keys [name preferred-name photo-path address]} multiaccount]
    {::json-rpc/call [{:method (json-rpc/call-ext-method (get-in db [:multiaccount :waku-enabled]) "sendContactUpdates")
                       :params [(or preferred-name name) photo-path]
                       :on-success #(log/debug "sent contact update")}]}))

(fx/defn multiaccount-update
  "Takes effects (containing :db) + new multiaccount fields, adds all effects necessary for multiaccount update.
  Optionally, one can specify a success-event to be dispatched after fields are persisted."
  [{:keys [db] :as cofx}
   setting setting-value
   {:keys [dont-sync? on-success] :or {on-success #()}}]
  (let [current-multiaccount (:multiaccount db)]
    (if (empty? current-multiaccount)
      ;; NOTE: this should never happen, but if it does this is a critical error
      ;; and it is better to crash than risk having an unstable state
      (throw (js/Error. "Please shake the phone to report this error and restart the app. multiaccount is currently empty, which means something went wrong when trying to update it with"))
      (fx/merge cofx
                {:db (if setting-value
                       (assoc-in db [:multiaccount setting] setting-value)
                       (update db :multiaccount dissoc setting))
                 ::json-rpc/call
                 [{:method "settings_saveSetting"
                   :params [setting setting-value]
                   :on-success on-success}]}
                (when (and (not dont-sync?)
                           (#{:name :photo-path :prefered-name} setting))
                  (send-multiaccount-update))))))

(fx/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx]
  (multiaccount-update cofx
                       :mnemonic nil
                       {}))
