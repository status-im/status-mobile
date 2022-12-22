(ns status-im.multiaccounts.update.core
  (:require [status-im.constants :as constants]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(fx/defn send-multiaccount-update
  [{:keys [db] :as cofx}]
  (let [multiaccount                          (:multiaccount db)
        {:keys [name preferred-name address]} multiaccount]
    {:json-rpc/call [{:method     "wakuext_sendContactUpdates"
                      :params     [(or preferred-name name) ""]
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
      (throw
       (js/Error.
        "Please shake the phone to report this error and restart the app. multiaccount is currently empty, which means something went wrong when trying to update it with"))
      (fx/merge cofx
                {:db            (if setting-value
                                  (assoc-in db [:multiaccount setting] setting-value)
                                  (update db :multiaccount dissoc setting))
                 :json-rpc/call
                 [{:method     "settings_saveSetting"
                   :params     [setting setting-value]
                   :on-success on-success}]}
                (when (and (not dont-sync?)
                           (#{:name :prefered-name} setting))
                  (send-multiaccount-update))))))

(fx/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx on-success]
  (multiaccount-update cofx :mnemonic nil on-success))

(defn augment-synchronized-recent-stickers
  "Add 'url' parameter to stickers that are synchronized from other devices.
   It is not sent from aanother devices but we have it in our db."
  [synced-stickers stickers-from-db]
  (mapv #(assoc %
                :url
                (->> (get stickers-from-db (or (:packID %) (:pack %)))
                     (:stickers)
                     (filter (fn [sticker-db] (= (:hash sticker-db) (:hash %))))
                     (first)
                     (:url)))
        synced-stickers))

(fx/defn optimistic
  [{:keys [db] :as cofx} setting setting-value]
  (let [current-multiaccount (:multiaccount db)
        setting-value        (if (= :currency setting)
                               (keyword setting-value)
                               setting-value)
        db                   (case setting
                               :stickers/packs-pending
                               (let [packs-pending (keys (js->clj setting-value))]
                                 (update db :stickers/packs-pending conj packs-pending))
                               :stickers/packs-installed
                               (let [packs-installed-keys (keys (js->clj setting-value))]
                                 (reduce #(assoc-in %1
                                           [:stickers/packs %2 :status]
                                           constants/sticker-pack-status-installed)
                                         db
                                         packs-installed-keys))
                               :stickers/recent-stickers
                               (let [recent-stickers-from-remote (augment-synchronized-recent-stickers
                                                                  (types/js->clj setting-value)
                                                                  (:stickers/packs db))
                                     merged                      (into recent-stickers-from-remote
                                                                       (:stickers/recent-stickers db))]
                                 (assoc db :stickers/recent-stickers recent-stickers-from-remote))
                               db)]
    {:db (if setting-value
           (assoc-in db [:multiaccount setting] setting-value)
           (update db :multiaccount dissoc setting))}))

(fx/defn set-many-js
  [cofx settings-js]
  (apply fx/merge
         cofx
         (map
          #(optimistic
            (keyword (.-name %))
            (.-value %))
          settings-js)))

(fx/defn toggle-backup-enabled
  {:events [:multiaccounts.ui/switch-backup-enabled]}
  [cofx enabled?]
  (multiaccount-update cofx :backup-enabled? enabled? {}))

(fx/defn toggle-opensea-nfts-visibility
  {:events [::toggle-opensea-nfts-visiblity]}
  [cofx visible?]
  (fx/merge cofx
            {:db       (assoc-in (:db cofx) [:multiaccount :opensea-enabled?] visible?)
             ;; need to add fully qualified namespace to counter circular deps
             :dispatch [:status-im.wallet.core/fetch-collectibles-collection]}
            (multiaccount-update :opensea-enabled? visible? {})))
