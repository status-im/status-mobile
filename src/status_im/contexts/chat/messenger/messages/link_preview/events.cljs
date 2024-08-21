(ns status-im.contexts.chat.messenger.messages.link-preview.events
  (:require [camel-snake-kebab.core :as csk]
            [utils.collection]
            [utils.re-frame :as rf]))

(defn community-link
  [id]
  (str "https://status.app/c#" id))

(rf/reg-event-fx :chat.ui/cache-link-preview-data
 (fn [{:keys [db]} [site data]]
   (let [{:profile/keys [profile]} db
         link-previews-cache       (-> profile
                                       (get :link-previews-cache {})
                                       (assoc site
                                              (utils.collection/map-keys csk/->kebab-case-keyword
                                                                         data)))]
     {:db (assoc-in db [:profile/profile :link-previews-cache] link-previews-cache)})))

(rf/reg-event-fx :chat.ui/load-link-preview-data
 (fn [{{:profile/keys [profile]} :db} [link]]
   (let [{:keys [error] :as cache-data} (get-in profile [:link-previews-cache link])]
     (when (or (not cache-data) error)
       {:fx [[:json-rpc/call
              [{:method     "wakuext_getLinkPreviewData"
                :params     [link]
                :on-success [:chat.ui/cache-link-preview-data link]
                :on-error   [:chat.ui/cache-link-preview-data link
                             {:error (str "Can't get preview data for " link)}]}]]]}))))

(rf/reg-event-fx :chat.ui/should-suggest-link-preview
 (fn [_ [enabled?]]
   {:fx [[:dispatch
          [:profile.settings/profile-update :link-preview-request-enabled (boolean enabled?)]]]}))

(rf/reg-event-fx :chat.ui/enable-link-previews
 (fn [{{:profile/keys [profile]} :db} [site enabled?]]
   (let [enabled-sites (if enabled?
                         (conj (get profile :link-previews-enabled-sites #{}) site)
                         (disj (get profile :link-previews-enabled-sites #{}) site))]
     {:fx [[:dispatch [:profile.settings/profile-update :link-previews-enabled-sites enabled-sites]]]})))

(rf/reg-event-fx :chat.ui/enable-all-link-previews
 (fn [_ [link-previews-whitelist enabled?]]
   (let [enabled-sites (if enabled?
                         (into #{} (map :title link-previews-whitelist))
                         #{})]
     {:fx [[:dispatch [:profile.settings/profile-update :link-previews-enabled-sites enabled-sites]]]})))
