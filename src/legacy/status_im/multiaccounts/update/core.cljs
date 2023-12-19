(ns legacy.status-im.multiaccounts.update.core
  (:require
    [legacy.status-im.utils.deprecated-types :as types]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]
    [utils.ens.core :as utils.ens]
    [utils.re-frame :as rf]))

(rf/defn send-contact-update
  [{:keys [db]}]
  (let [{:keys [name preferred-name display-name address]} (:profile/profile db)]
    {:json-rpc/call [{:method     "wakuext_sendContactUpdates"
                      :params     [(or preferred-name display-name name) ""]
                      :on-success #(log/debug "sent contact update")}]}))

(rf/defn update-multiaccount-account-name
  "This updates the profile name in the profile list before login"
  {:events [:multiaccounts.ui/update-name]}
  [{:keys [db] :as cofx} raw-multiaccounts-from-status-go]
  (let [{:keys [key-uid name preferred-name
                display-name]} (:profile/profile db)
        account                (some #(and (= (:key-uid %) key-uid) %) raw-multiaccounts-from-status-go)]
    (when-let [new-name (and account (or preferred-name display-name name))]
      (rf/merge
       cofx
       {:db            (assoc-in db
                        [:profile/profile :ens-name?]
                        (utils.ens/is-valid-eth-name? new-name))
        :json-rpc/call [{:method     "multiaccounts_updateAccount"
                         :params     [(assoc account :name new-name)]
                         :on-success #(log/debug "sent multiaccount update")}]}))))

(rf/defn multiaccount-update
  "Takes effects (containing :db) + new multiaccount fields, adds all effects necessary for multiaccount update.
  Optionally, one can specify a success-event to be dispatched after fields are persisted."
  [{:keys [db] :as cofx}
   setting setting-value
   {:keys [dont-sync? on-success] :or {on-success #()}}]
  (let [current-multiaccount (:profile/profile db)]
    (if (empty? current-multiaccount)
      ;; NOTE: this should never happen, but if it does this is a critical error
      ;; and it is better to crash than risk having an unstable state
      (throw
       (js/Error.
        "Please shake the phone to report this error and restart the app. multiaccount is currently empty, which means something went wrong when trying to update it with"))
      (rf/merge
       cofx
       {:db (if setting-value
              (assoc-in db [:profile/profile setting] setting-value)
              (update db :profile/profile dissoc setting))
        :json-rpc/call
        [{:method     "settings_saveSetting"
          :params     [setting setting-value]
          :on-success on-success}]}

       (when (#{:name :preferred-name} setting)
         (constantly {:profile/get-profiles-overview #(rf/dispatch [:multiaccounts.ui/update-name %])}))

       (when (and (not dont-sync?) (#{:name :preferred-name} setting))
         (send-contact-update))))))

(rf/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx on-success]
  (multiaccount-update cofx :mnemonic nil on-success))

(defn augment-synchronized-recent-stickers
  "Add 'url' parameter to stickers that are synchronized from other devices.
   It is not sent from another devices but we have it in our db."
  [synced-stickers stickers-from-db]
  (mapv #(assoc %
                :url
                (->> (get stickers-from-db (or (:packID %) (:pack %)))
                     (:stickers)
                     (filter (fn [sticker-db] (= (:hash sticker-db) (:hash %))))
                     (first)
                     (:url)))
        synced-stickers))

(rf/defn optimistic
  [{:keys [db] :as cofx} setting setting-value]
  (let [current-multiaccount (:profile/profile db)
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
           (assoc-in db [:profile/profile setting] setting-value)
           (update db :profile/profile dissoc setting))}))

(rf/defn set-many-js
  [cofx settings-js]
  (apply rf/merge
         cofx
         (map
          #(optimistic
            (keyword (.-name %))
            (.-value %))
          settings-js)))

(rf/defn toggle-backup-enabled
  {:events [:multiaccounts.ui/switch-backup-enabled]}
  [cofx enabled?]
  (multiaccount-update cofx :backup-enabled? enabled? {}))

(rf/defn toggle-opensea-nfts-visibility
  {:events [::toggle-opensea-nfts-visiblity]}
  [cofx visible?]
  (rf/merge cofx
            {:db       (assoc-in (:db cofx) [:profile/profile :opensea-enabled?] visible?)
             ;; need to add fully qualified namespace to counter circular deps
             :dispatch [:legacy.status-im.wallet.core/fetch-collectibles-collection]}
            (multiaccount-update :opensea-enabled? visible? {})))
