(ns status-im.chat.models.link-preview
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]))

(fx/defn enable
  {:events [::enable]}
  [{{:keys [multiaccount]} :db :as cofx} site enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-enabled-sites
             (if enabled?
               (conj (get multiaccount :link-previews-enabled-sites #{}) site)
               (disj (get multiaccount :link-previews-enabled-sites #{}) site))
             {})))

(fx/defn enable-all
  {:events [::enable-all]}
  [{{:keys [multiaccount]} :db :as cofx} link-previews-whitelist enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-enabled-sites
             (if enabled?
               (into #{} (map :title link-previews-whitelist))
               #{})
             {})))

(fx/defn resolve-community-info
  {:events [::resolve-community-info]}
  [cofx community-id]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "requestCommunityInfoFromMailserver")
                     :params     [community-id]
                     :on-success #()
                     :on-error   #(log/error "Failed to request community info from mailserver")}]})

(fx/defn load-link-preview-data
  {:events [::load-link-preview-data]}
  [cofx link]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "getLinkPreviewData")
                     :params     [link]
                     :on-success #(re-frame/dispatch [::cache-link-preview-data link %])
                     :on-error   #(re-frame/dispatch [::cache-link-preview-data
                                                      link
                                                      {:error (str  "Can't get preview data for " link)}])}]})

(fx/defn cache-link-preview-data
  {:events [::cache-link-preview-data]}
  [{{:keys [multiaccount]} :db :as cofx} site data]
  (multiaccounts.update/optimistic
   cofx
   :link-previews-cache
   (assoc (get multiaccount :link-previews-cache {}) site data)))

(defn community-link [id]
  (str "https://join.status.im/c/" id))

(defn cache-community-preview-data
  [{:keys [id] :as community}]
  (re-frame/dispatch [::cache-link-preview-data
                      (community-link id)
                      community]))

(fx/defn should-suggest-link-preview
  {:events [::should-suggest-link-preview]}
  [{:keys [db] :as cofx} enabled?]
  (multiaccounts.update/multiaccount-update
   cofx
   :link-preview-request-enabled (boolean enabled?)
   {}))

(fx/defn request-link-preview-whitelist
  [_]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "getLinkPreviewWhitelist")
                     :params     []
                     :on-success #(re-frame/dispatch [::link-preview-whitelist-received %])
                     :on-error   #(log/error "Failed to get link preview whitelist")}]})

(fx/defn save-link-preview-whitelist
  {:events [::link-preview-whitelist-received]}
  [{:keys [db]} whitelist]
  {:db (assoc db :link-previews-whitelist
              whitelist)})

