(ns status-im.contexts.chat.messenger.messages.link-preview.events
  (:require
    [camel-snake-kebab.core :as csk]
    [status-im.contexts.profile.settings.events :as profile.settings.events]
    [taoensso.timbre :as log]
    [utils.collection]
    [utils.re-frame :as rf]))

(defn community-link
  [id]
  (str "https://status.app/c#" id))

(rf/defn cache-link-preview-data
  {:events [:chat.ui/cache-link-preview-data]}
  [{{:profile/keys [profile]} :db :as cofx} site data]
  (let [link-previews-cache (get profile :link-previews-cache {})]
    (profile.settings.events/optimistic-profile-update
     cofx
     :link-previews-cache
     (assoc link-previews-cache site (utils.collection/map-keys csk/->kebab-case-keyword data)))))

(rf/defn load-link-preview-data
  {:events [:chat.ui/load-link-preview-data]}
  [{{:profile/keys [profile] :as db} :db} link]
  (let [{:keys [error] :as cache-data} (get-in profile [:link-previews-cache link])]
    (if (or (not cache-data) error)
      {:json-rpc/call [{:method     "wakuext_getLinkPreviewData"
                        :params     [link]
                        :on-success #(rf/dispatch [:chat.ui/cache-link-preview-data link %])
                        :on-error   #(rf/dispatch [:chat.ui/cache-link-preview-data link
                                                   {:error (str "Can't get preview data for " link)}])}]}
      {:db db})))

(rf/defn should-suggest-link-preview
  {:events [:chat.ui/should-suggest-link-preview]}
  [{:keys [db] :as cofx} enabled?]
  (profile.settings.events/profile-update
   cofx
   :link-preview-request-enabled
   (boolean enabled?)
   {}))

(rf/defn save-link-preview-whitelist
  {:events [:chat.ui/link-preview-whitelist-received]}
  [{:keys [db]} whitelist]
  {:db (assoc db :link-previews-whitelist whitelist)})

(rf/defn request-link-preview-whitelist
  [_]
  {:json-rpc/call [{:method     "wakuext_getLinkPreviewWhitelist"
                    :params     []
                    :on-success #(rf/dispatch [:chat.ui/link-preview-whitelist-received %])
                    :on-error   #(log/error "Failed to get link preview whitelist")}]})

(defn cache-community-preview-data
  [{:keys [id] :as community}]
  (rf/dispatch [:chat.ui/cache-link-preview-data (community-link id) community]))

(rf/defn enable
  {:events [:chat.ui/enable-link-previews]}
  [{{:profile/keys [profile]} :db :as cofx} site enabled?]
  (profile.settings.events/profile-update
   cofx
   :link-previews-enabled-sites
   (if enabled?
     (conj (get profile :link-previews-enabled-sites #{}) site)
     (disj (get profile :link-previews-enabled-sites #{}) site))
   {}))

(rf/defn enable-all
  {:events [:chat.ui/enable-all-link-previews]}
  [cofx link-previews-whitelist enabled?]
  (profile.settings.events/profile-update
   cofx
   :link-previews-enabled-sites
   (if enabled?
     (into #{} (map :title link-previews-whitelist))
     #{})
   {}))
