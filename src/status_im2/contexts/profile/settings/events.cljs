(ns status-im2.contexts.profile.settings.events
  (:require [clojure.string :as string]
            [status-im.bottom-sheet.events :as bottom-sheet.events]
            [status-im2.constants :as constants]
            status-im2.contexts.profile.settings.effects
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/defn send-contact-update
  [{:keys [db]}]
  (let [{:keys [name preferred-name display-name]} (:profile/profile db)]
    {:json-rpc/call [{:method     "wakuext_sendContactUpdates"
                      :params     [(or preferred-name display-name name) ""]
                      :on-success #(log/debug "sent contact update")}]}))

(rf/defn profile-update
  {:events [:profile.settings/profile-update]}
  [{:keys [db] :as cofx}
   setting setting-value
   {:keys [dont-sync? on-success] :or {on-success #()}}]
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
     (send-contact-update))))

(rf/defn optimistic-profile-update
  [{:keys [db]} setting setting-value]
  {:db (if setting-value
         (assoc-in db [:profile/profile setting] setting-value)
         (update db :profile/profile dissoc setting))})

(rf/defn change-preview-privacy
  [{:keys [db]}]
  (let [private? (get-in db [:profile/profile :preview-privacy?])]
    {:profile.settings/blank-preview-flag-changed private?}))

(rf/defn update-value
  {:events [:profile.settings/update-value]}
  [cofx key value]
  (profile-update cofx key value {}))

(rf/defn change-webview-debug
  {:events [:profile.settings/change-webview-debug]}
  [{:keys [db] :as cofx} value]
  (rf/merge cofx
            {:profile.settings/webview-debug-changed value}
            (profile-update :webview-debug (boolean value) {})))

(rf/reg-event-fx :profile.settings/change-test-networks-enabled
 (fn [_ [value]]
   {:fx [[:dispatch [:profile.settings/profile-update :test-networks-enabled? (boolean value) {}]]]}))

(rf/defn change-preview-privacy-flag
  {:events [:profile.settings/change-preview-privacy]}
  [{:keys [db] :as cofx} private?]
  (rf/merge cofx
            {:profile.settings/blank-preview-flag-changed private?}
            (profile-update
             :preview-privacy?
             (boolean private?)
             {})))


(rf/defn change-profile-pictures-show-to
  {:events [:profile.settings/change-profile-pictures-show-to]}
  [cofx id]
  (rf/merge cofx
            {:json-rpc/call [{:method     "wakuext_changeIdentityImageShowTo"
                              :params     [id]
                              :on-success #(log/debug "picture settings changed successfully")}]}
            (optimistic-profile-update :profile-pictures-show-to id)))

(rf/defn change-appearance
  {:events [:profile.settings/change-appearance]}
  [cofx theme]
  (rf/merge cofx
            {:profile.settings/switch-theme-fx [theme :appearance true]}
            (profile-update :appearance theme {})))

(rf/defn switch-theme
  {:events [:profile.settings/switch-theme]}
  [cofx theme view-id]
  (let [theme (or theme
                  (get-in cofx [:db :profile/profile :appearance])
                  constants/theme-type-dark)]
    {:profile.settings/switch-theme-fx [theme view-id false]}))

(rf/defn get-profile-picture
  {:events [:profile.settings/get-profile-picture]}
  [cofx]
  (let [key-uid (get-in cofx [:db :profile/profile :key-uid])]
    {:json-rpc/call [{:method     "multiaccounts_getIdentityImages"
                      :params     [key-uid]
                      :on-success [:profile.settings/update-local-picture]}]}))

(rf/defn save-profile-picture
  {:events [:profile.settings/save-profile-picture]}
  [cofx path ax ay bx by]
  (let [key-uid (get-in cofx [:db :profile/profile :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_storeIdentityImage"
                                :params     [key-uid (string/replace-first path #"file://" "") ax ay bx
                                             by]
                                :on-success [:profile.settings/update-local-picture]}]}
              (bottom-sheet.events/hide-bottom-sheet-old))))

(rf/defn save-profile-picture-from-url
  {:events [:profile.settings/save-profile-picture-from-url]}
  [cofx url]
  (let [key-uid (get-in cofx [:db :profile/profile :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_storeIdentityImageFromURL"
                                :params     [key-uid url]
                                :on-error   #(log/error "::save-profile-picture-from-url error" %)
                                :on-success [:profile.settings/update-local-picture]}]}
              (bottom-sheet.events/hide-bottom-sheet-old))))

(rf/defn delete-profile-picture
  {:events [:profile.settings/delete-profile-picture]}
  [cofx name]
  (let [key-uid (get-in cofx [:db :profile/profile :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_deleteIdentityImage"
                                :params     [key-uid]
                                ;; NOTE: In case of an error we could fallback to previous image in
                                ;; UI with a toast error
                                :on-success #(log/info "[profile] Delete profile image" %)}]}
              (optimistic-profile-update :images nil)
              (bottom-sheet.events/hide-bottom-sheet-old))))

(rf/defn store-profile-picture
  {:events [:profile.settings/update-local-picture]}
  [cofx pics]
  (optimistic-profile-update cofx :images pics))
