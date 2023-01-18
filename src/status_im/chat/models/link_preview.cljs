(ns status-im.chat.models.link-preview
  (:require [re-frame.core :as re-frame]
            [status-im.communities.core :as models.communities]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(rf/defn enable
  {:events [::enable]}
  [{{:keys [multiaccount]} :db :as cofx} site enabled?]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-enabled-sites
             (if enabled?
               (conj (get multiaccount :link-previews-enabled-sites #{}) site)
               (disj (get multiaccount :link-previews-enabled-sites #{}) site))
             {})))

(rf/defn enable-all
  {:events [::enable-all]}
  [{{:keys [multiaccount]} :db :as cofx} link-previews-whitelist enabled?]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-enabled-sites
             (if enabled?
               (into #{} (map :title link-previews-whitelist))
               #{})
             {})))

(defn community-resolved
  [db community-id]
  (update db :communities/resolve-community-info dissoc community-id))

(defn community-failed-to-resolve
  [db community-id]
  (update db :communities/resolve-community-info dissoc community-id))

(defn community-resolving
  [db community-id]
  (assoc-in db [:communities/resolve-community-info community-id] true))

(rf/defn handle-community-failed-to-resolve
  {:events [::community-failed-to-resolve]}
  [{:keys [db]} community-id]
  {:db (community-failed-to-resolve db community-id)})

(defn community-link
  [id]
  (str "https://join.status.im/c/" id))

(rf/defn handle-community-resolved
  {:events [::community-resolved]}
  [{:keys [db] :as cofx} community-id community]
  (rf/merge cofx
            (cond-> {:db (community-resolved db community-id)}
              (some? community)
              (assoc :dispatch
                     [::cache-link-preview-data
                      (community-link community-id) community]))
            (models.communities/handle-community community)))

(rf/defn resolve-community-info
  {:events [:communities.ui/resolve-community-info]}
  [{:keys [db]} community-id]
  {:db            (community-resolving db community-id)
   :json-rpc/call [{:method     "wakuext_requestCommunityInfoFromMailserver"
                    :params     [community-id]
                    :on-success #(re-frame/dispatch [::community-resolved community-id %])
                    :on-error   #(do
                                   (re-frame/dispatch [::community-failed-to-resolve community-id])
                                   (log/error "Failed to request community info from mailserver"))}]})

(rf/defn load-link-preview-data
  {:events [::load-link-preview-data]}
  [cofx link]
  {:json-rpc/call [{:method     "wakuext_getLinkPreviewData"
                    :params     [link]
                    :on-success #(re-frame/dispatch [::cache-link-preview-data link %])
                    :on-error   #(re-frame/dispatch
                                  [::cache-link-preview-data
                                   link
                                   {:error (str "Can't get preview data for " link)}])}]})

(rf/defn cache-link-preview-data
  {:events [::cache-link-preview-data]}
  [{{:keys [multiaccount]} :db :as cofx} site data]
  (multiaccounts.update/optimistic
   cofx
   :link-previews-cache
   (assoc (get multiaccount :link-previews-cache {}) site data)))

(defn cache-community-preview-data
  [{:keys [id] :as community}]
  (re-frame/dispatch [::cache-link-preview-data
                      (community-link id)
                      community]))

(rf/defn should-suggest-link-preview
  {:events [::should-suggest-link-preview]}
  [{:keys [db] :as cofx} enabled?]
  (multiaccounts.update/multiaccount-update
   cofx
   :link-preview-request-enabled
   (boolean enabled?)
   {}))

(rf/defn request-link-preview-whitelist
  [_]
  {:json-rpc/call [{:method     "wakuext_getLinkPreviewWhitelist"
                    :params     []
                    :on-success #(re-frame/dispatch [::link-preview-whitelist-received %])
                    :on-error   #(log/error "Failed to get link preview whitelist")}]})

(rf/defn save-link-preview-whitelist
  {:events [::link-preview-whitelist-received]}
  [{:keys [db]} whitelist]
  {:db (assoc db
              :link-previews-whitelist
              whitelist)})
