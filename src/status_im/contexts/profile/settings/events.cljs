(ns status-im.contexts.profile.settings.events
  (:require [clojure.string :as string]
            [status-im.common.json-rpc.events :as json-rpc]
            [status-im.constants :as constants]
            status-im.contexts.profile.settings.effects
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- set-setting-value
  [db setting setting-value]
  (if setting-value
    (assoc-in db [:profile/profile setting] setting-value)
    (update db :profile/profile dissoc setting)))

(rf/reg-event-fx :profile.settings/profile-update
 (fn [{:keys [db]} [setting setting-value {:keys [dont-sync? on-success]}]]
   {:db (-> db
            (set-setting-value setting setting-value))
    :fx [[:json-rpc/call
          [{:method     "settings_saveSetting"
            :params     [setting setting-value]
            :on-success on-success}]]

         (when (#{:name :preferred-name} setting)
           [:profile/get-profiles-overview #(rf/dispatch [:multiaccounts.ui/update-name %])])

         (when (and (not dont-sync?) (#{:name :preferred-name} setting))
           (let [{:keys [name preferred-name display-name]} (:profile/profile db)]
             [:json-rpc/call
              [{:method     "wakuext_sendContactUpdates"
                :params     [(or preferred-name display-name name) "" ""]
                :on-success #(log/debug "sent contact update")}]]))]}))

(rf/reg-event-fx :profile.settings/change-webview-debug
 (fn [_ [value]]
   (let [value' (boolean value)]
     {:fx [[:dispatch [:profile.settings/profile-update :webview-debug value']]
           [:profile.settings/webview-debug-changed value']]})))

(rf/reg-event-fx :profile.settings/toggle-test-networks
 (fn [{:keys [db]}]
   (let [value      (get-in db [:profile/profile :test-networks-enabled?])
         on-success #(rf/dispatch [:logout])]
     {:fx [[:ui/show-confirmation
            {:title     (i18n/label :t/testnet-mode-prompt-title)
             :content   (i18n/label :t/testnet-mode-prompt-content)
             :on-accept #(rf/dispatch [:profile.settings/profile-update :test-networks-enabled?
                                       (not value)
                                       {:on-success on-success}])
             :on-cancel nil}]]})))

(rf/reg-event-fx
 :profile.settings/toggle-goerli-test-network
 (fn [{:keys [db]}]
   (let [value      (get-in db [:profile/profile :goerli-enabled?])
         on-success #(rf/dispatch [:logout])]
     {:fx [[:ui/show-confirmation
            {:content   (i18n/label :t/goerli-testnet-toggle-confirmation)
             :on-accept #(rf/dispatch [:profile.settings/profile-update :goerli-enabled?
                                       (not value)
                                       {:on-success on-success}])
             :on-cancel nil}]]})))

(rf/reg-event-fx :profile.settings/change-preview-privacy
 (fn [_ [private?]]
   (let [private?' (boolean private?)]
     {:fx [[:dispatch [:profile.settings/profile-update :preview-privacy? private?']]
           [:profile.settings/blank-preview-flag-changed private?']]})))

(rf/reg-event-fx :profile.settings/change-profile-pictures-show-to
 (fn [{:keys [db]} [id]]
   {:db (-> db
            (assoc-in [:profile/profile :profile-pictures-show-to] id))
    :fx [[:json-rpc/call
          [{:method     "wakuext_changeIdentityImageShowTo"
            :params     [id]
            :on-success #(log/debug "picture settings changed successfully")}]]]}))

(rf/reg-event-fx :profile.settings/toggle-peer-syncing
 (fn [{:keys [db]}]
   (let [value     (get-in db [:profile/profile :peer-syncing-enabled?])
         new-value (not value)]
     {:db (assoc-in db [:profile/profile :peer-syncing-enabled?] new-value)
      :fx [[:json-rpc/call
            [{:method   "wakuext_togglePeerSyncing"
              :params   [{:enabled new-value}]
              :on-error #(log/error "failed to toggle peer syncing" new-value %)}]]]})))

(rf/reg-event-fx :profile.settings/change-appearance
 (fn [_ [theme]]
   {:fx [[:dispatch [:profile.settings/profile-update :appearance theme]]
         [:profile.settings/switch-theme-fx [theme :appearance true]]]}))

(rf/reg-event-fx :profile.settings/switch-theme
 (fn [{:keys [db]} [theme view-id]]
   (let [theme (or theme
                   (get-in db [:profile/profile :appearance])
                   constants/theme-type-dark)]
     {:fx [[:profile.settings/switch-theme-fx [theme view-id false]]]})))

(rf/reg-fx :profile.settings/get-profile-picture
 (fn [key-uid]
   (json-rpc/call {:method     "multiaccounts_getIdentityImages"
                   :params     [key-uid]
                   :on-success [:profile.settings/update-local-picture]})))

(rf/reg-event-fx :profile.settings/save-profile-picture
 (fn [{:keys [db]} [path ax ay bx by]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db (-> db
              (assoc :bottom-sheet/show? false))
      :fx [[:json-rpc/call
            [{:method     "multiaccounts_storeIdentityImage"
              :params     [key-uid (string/replace-first path #"file://" "") ax ay bx
                           by]
              :on-success [:profile.settings/update-local-picture]}]]
           [:dismiss-bottom-sheet-overlay-old]]})))

(rf/reg-event-fx :profile.settings/save-profile-picture-from-url
 (fn [{:keys [db]} [url]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db (-> db
              (assoc :bottom-sheet/show? false))
      :fx [[:json-rpc/call
            [{:method     "multiaccounts_storeIdentityImageFromURL"
              :params     [key-uid url]
              :on-error   #(log/error "::save-profile-picture-from-url error" %)
              :on-success [:profile.settings/update-local-picture]}]]
           [:dismiss-bottom-sheet-overlay-old]]})))

(rf/reg-event-fx :profile.settings/delete-profile-picture
 (fn [{:keys [db]}]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db (-> db
              (update :profile/profile dissoc :images)
              (assoc :bottom-sheet/show? false))
      :fx [[:json-rpc/call
            [{:method     "multiaccounts_deleteIdentityImage"
              :params     [key-uid]
              ;; NOTE: In case of an error we could fallback to previous image in
              ;; UI with a toast error
              :on-success #(log/info "[profile] Delete profile image" %)}]]
           [:dismiss-bottom-sheet-overlay-old]]})))

(rf/reg-event-fx :profile.settings/update-local-picture
 (fn [{:keys [db]} [images]]
   {:db (assoc-in db [:profile/profile :images] images)}))

(rf/reg-event-fx :profile.settings/mnemonic-was-shown
 (fn [_]
   {:fx [[:json-rpc/call
          [{:method     "settings_mnemonicWasShown"
            :on-success #(log/debug "mnemonic was marked as shown")
            :on-error   #(log/error "mnemonic was not marked as shown" %)}]]]}))
