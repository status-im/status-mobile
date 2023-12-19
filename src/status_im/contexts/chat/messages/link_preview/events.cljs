(ns status-im.contexts.chat.messages.link-preview.events
  (:require
    [camel-snake-kebab.core :as csk]
    [legacy.status-im.mailserver.core :as mailserver]
    [schema.core :as schema]
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

(defn community-resolved
  [{:keys [db]} [community-id community]]
  (when community
    {:db (update db :communities/resolve-community-info dissoc community-id)
     :fx [[:dispatch [:communities/handle-community community]]
          [:dispatch
           [:chat.ui/cache-link-preview-data (community-link community-id) community]]]}))

(rf/reg-event-fx :chat.ui/community-resolved community-resolved)

(defn community-failed-to-resolve
  [{:keys [db]} [community-id]]
  {:db (update db :communities/resolve-community-info dissoc community-id)})

(rf/reg-event-fx :chat.ui/community-failed-to-resolve community-failed-to-resolve)

(defn fetch-community
  [{:keys [db]} [community-id]]
  (when community-id
    {:db            (assoc-in db [:communities/resolve-community-info community-id] true)
     :json-rpc/call [{:method     "wakuext_fetchCommunity"
                      :params     [{:CommunityKey    community-id
                                    :TryDatabase     true
                                    :WaitForResponse true}]
                      :on-success (fn [community]
                                    (rf/dispatch [:chat.ui/community-resolved community-id community]))
                      :on-error   (fn [err]
                                    (rf/dispatch [:chat.ui/community-failed-to-resolve community-id])
                                    (log/error {:message
                                                "Failed to request community info from mailserver"
                                                :error err}))}]}))

(schema/=> fetch-community
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema [:catn [:community-id [:? :string]]]]]]
   [:map
    [:db map?]
    [:json-rpc/call :schema.common/rpc-call]]])

(rf/reg-event-fx :chat.ui/fetch-community fetch-community)

(defn spectate-community-success
  [{:keys [db]} [{:keys [communities]}]]
  (when-let [community (first communities)]
    {:db (-> db
             (assoc-in [:communities (:id community) :spectated] true)
             (assoc-in [:communities (:id community) :spectating] false))
     :fx [[:dispatch [:communities/handle-community community]]
          [:dispatch [::mailserver/request-messages]]]}))

(rf/reg-event-fx :chat.ui/spectate-community-success spectate-community-success)

(defn spectate-community-failed
  [{:keys [db]} [community-id]]
  {:db (assoc-in db [:communities community-id :spectating] false)})

(rf/reg-event-fx :chat.ui/spectate-community-failed spectate-community-failed)

(defn spectate-community
  [{:keys [db]} [community-id]]
  (let [{:keys [spectated spectating joined]} (get-in db [:communities community-id])]
    (when (and (not joined) (not spectated) (not spectating))
      {:db            (assoc-in db [:communities community-id :spectating] true)
       :json-rpc/call [{:method     "wakuext_spectateCommunity"
                        :params     [community-id]
                        :on-success [:chat.ui/spectate-community-success]
                        :on-error   (fn [err]
                                      (log/error {:message
                                                  "Failed to spectate community"
                                                  :error err})
                                      (rf/dispatch [:chat.ui/spectate-community-failed
                                                    community-id]))}]})))

(schema/=> spectate-community
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema [:catn [:community-id [:? :string]]]]]]
   [:map
    [:db map?]
    [:json-rpc/call :schema.common/rpc-call]]])

(rf/reg-event-fx :chat.ui/spectate-community spectate-community)

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
