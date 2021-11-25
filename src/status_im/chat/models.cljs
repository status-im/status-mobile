(ns status-im.chat.models
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.chat.models.message-list :as message-list]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.colors :as colors]
            [status-im.constants :as constants]
            [status-im.navigation :as navigation]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [status-im.add-new.db :as new-public-chat.db]
            [status-im.chat.models.loading :as loading]
            [status-im.ui.screens.chat.state :as chat.state]
            [clojure.set :as set]))

(defn chats []
  (:chats (types/json->clj (js/require "./chats.js"))))

(defn- get-chat [cofx chat-id]
  (get-in cofx [:db :chats chat-id]))

(defn multi-user-chat?
  ([chat]
   (:group-chat chat))
  ([cofx chat-id]
   (multi-user-chat? (get-chat cofx chat-id))))

(def one-to-one-chat?
  (complement multi-user-chat?))

(defn public-chat?
  ([chat]
   (:public? chat))
  ([cofx chat-id]
   (public-chat? (get-chat cofx chat-id))))

(defn community-chat?
  ([{:keys [chat-type]}]
   (= chat-type constants/community-chat-type))
  ([cofx chat-id]
   (community-chat? (get-chat cofx chat-id))))

(defn active-chat? [cofx chat-id]
  (let [chat (get-chat cofx chat-id)]
    (not (nil? chat))))

(defn foreground-chat?
  [{{:keys [current-chat-id view-id]} :db} chat-id]
  (and (= current-chat-id chat-id)
       (= view-id :chat)))

(defn group-chat?
  ([chat]
   (and (multi-user-chat? chat)
        (not (public-chat? chat))))
  ([cofx chat-id]
   (group-chat? (get-chat cofx chat-id))))

(defn timeline-chat?
  ([chat]
   (:timeline? chat))
  ([cofx chat-id]
   (timeline-chat? (get-chat cofx chat-id))))

(defn profile-chat?
  ([chat]
   (:profile-public-key chat))
  ([cofx chat-id]
   (profile-chat? (get-chat cofx chat-id))))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn- create-new-chat
  [chat-id {:keys [db now]}]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id            chat-id
     :name               (or name "")
     :color              (rand-nth colors/chat-colors)
     :chat-type          constants/one-to-one-chat-type
     :group-chat         false
     :timestamp          now
     :contacts           #{chat-id}
     :last-clock-value   0}))

(defn map-chats [{:keys [db] :as cofx}]
  (fn [val]
    (assoc
     (merge
      (or (get (:chats db) (:chat-id val))
          (create-new-chat (:chat-id val) cofx))
      val)
     :invitation-admin (:invitation-admin val))))

(defn filter-chats [db]
  (fn [val]
    (and (not (get-in db [:chats (:chat-id val)])) (:public? val))))

(fx/defn leave-removed-chat
  [{{:keys [view-id current-chat-id chats]} :db
    :as cofx}]
  (when (and (= view-id :chat)
             (not (contains? chats current-chat-id)))
    (navigation/navigate-back cofx)))

(fx/defn ensure-chats
  "Add chats to db and update"
  [{:keys [db] :as cofx} chats]
  (let [{:keys [all-chats chats-home-list removed-chats]}
        (reduce (fn [acc {:keys [chat-id profile-public-key timeline? community-id active] :as chat}]
                  (if (not active)
                    (update acc :removed-chats conj chat-id)
                    (cond-> acc
                      (and (not profile-public-key) (not timeline?) (not community-id))
                      (update :chats-home-list conj chat-id)
                      :always
                      (assoc-in [:all-chats chat-id] chat))))
                {:all-chats {}
                 :chats-home-list #{}
                 :removed-chats #{}}
                (map (map-chats cofx) chats))]
    (fx/merge
     cofx
     (merge {:db (-> db
                     (update :chats merge all-chats)
                     (update :chats-home-list set/union chats-home-list)
                     (update :chats #(apply dissoc % removed-chats))
                     (update :chats-home-list set/difference removed-chats))}
            (when (not-empty removed-chats)
              {:clear-multiple-message-notifications removed-chats}))
     leave-removed-chat)))

(fx/defn clear-history
  "Clears history of the particular chat"
  [{:keys [db] :as cofx} chat-id remove-chat?]
  (let [{:keys [last-message public?
                deleted-at-clock-value]} (get-in db [:chats chat-id])
        last-message-clock-value (if (and public? remove-chat?)
                                   0
                                   (or (:clock-value last-message)
                                       deleted-at-clock-value
                                       (utils.clocks/send 0)))]
    {:db            (-> db
                        (assoc-in [:messages chat-id] {})
                        (update-in [:message-lists] dissoc chat-id)
                        (update :chats (fn [chats]
                                         (if (contains? chats chat-id)
                                           (update chats chat-id merge
                                                   {:last-message              nil
                                                    :unviewed-messages-count   0
                                                    :unviewed-mentions-count   0
                                                    :deleted-at-clock-value    last-message-clock-value})
                                           chats))))}))

(fx/defn clear-history-handler
  "Clears history of the particular chat"
  {:events [:chat.ui/clear-history]}
  [{:keys [db] :as cofx} chat-id remove-chat?]
  (fx/merge cofx
            {:db db
             ::json-rpc/call [{:method "wakuext_clearHistory"
                               :params [{:id chat-id}]
                               :on-success #(re-frame/dispatch [::history-cleared chat-id %])
                               :on-error #(log/error "failed to clear history " chat-id %)}]}
            (clear-history chat-id remove-chat?)))

(fx/defn deactivate-chat
  "Deactivate chat in db, no side effects"
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge
   cofx
   {:db (-> db
            (update :chats dissoc chat-id)
            (update :chats-home-list disj chat-id)
            (assoc-in [:current-chat-id] nil))
    ::json-rpc/call [{:method "wakuext_deactivateChat"
                      :params [{:id chat-id}]
                      :on-success #(log/debug "chat deactivated" chat-id)
                      :on-error #(log/error "failed to create public chat" chat-id %)}]}
   (clear-history chat-id true)))

(fx/defn offload-messages
  {:events [:offload-messages]}
  [{:keys [db]} chat-id]
  (merge {:db (-> db
                  (update :messages dissoc chat-id)
                  (update :message-lists dissoc chat-id)
                  (update :pagination-info dissoc chat-id))}
         (when (and (= chat-id constants/timeline-chat-id) (= (:view-id db) :status))
           {:dispatch [:init-timeline-chat]})))

(fx/defn close-chat
  {:events [:close-chat]}
  [{:keys [db] :as cofx} target-chat-id]
  (let [chat-id (:current-chat-id db)]
    (if (:ignore-close-chat db)
      {:db (dissoc db :ignore-close-chat)}
      (when (= target-chat-id chat-id)
        (chat.state/reset-visible-item)
        (fx/merge cofx
                  {:db (dissoc db :current-chat-id)}
                  (offload-messages chat-id))))))

(fx/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  {:events [:chat.ui/remove-chat]}
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge cofx
            (deactivate-chat chat-id)
            (offload-messages chat-id)
            (when (not (= (:view-id db) :home))
              (navigation/pop-to-root-tab :chat-stack))))

(fx/defn show-more-chats
  {:events [:chat.ui/show-more-chats]}
  [{:keys [db]}]
  (when (< (:home-items-show-number db) (count (:chats db)))
    {:db (update db :home-items-show-number + 40)}))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  {:events [:chat.ui/preload-chat-data]}
  [cofx chat-id]
  (loading/load-messages cofx chat-id))

(fx/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-chat]}
  [{db :db :as cofx} chat-id dont-reset?]
  (fx/merge cofx
            (close-chat (:current-chat-id db))
            (fn [{:keys [db]}]
              {:db (assoc db :current-chat-id chat-id :ignore-close-chat true)})
            (preload-chat-data chat-id)
            #(when (group-chat? cofx chat-id)
               (loading/load-chat % chat-id))
            #(when-not dont-reset?
               (navigation/change-tab % :chat))
            #(when-not dont-reset?
               (navigation/pop-to-root-tab % :chat-stack))
            (navigation/navigate-to-cofx :chat nil)))

(fx/defn handle-clear-history-response
  {:events [::history-cleared]}
  [{:keys [db]} chat-id response]
  (let [chat (chats-store/<-rpc (first (:chats response)))]
    {:db (assoc-in db [:chats chat-id] chat)}))

(fx/defn handle-one-to-one-chat-created
  {:events [::one-to-one-chat-created]}
  [{:keys [db]} chat-id response]
  (let [chat (chats-store/<-rpc (first (:chats response)))
        contact-rpc (first (:contacts response))
        contact (when contact-rpc (contacts-store/<-rpc contact-rpc))]
    {:db (cond-> db
           contact
           (assoc-in [:contacts/contacts chat-id] contact)
           :always
           (assoc-in [:chats chat-id] chat)
           :always
           (update :chats-home-list conj chat-id))
     :dispatch [:chat.ui/navigate-to-chat chat-id]}))

(fx/defn navigate-to-user-pinned-messages
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-pinned-messages]}
  [cofx chat-id]
  (navigation/navigate-to cofx :chat-pinned-messages {:chat-id chat-id}))

(fx/defn start-chat
  "Start a chat, making sure it exists"
  {:events [:chat.ui/start-chat]}
  [{:keys [db] :as cofx} chat-id ens-name]
  ;; don't allow to open chat with yourself
  (when (not= (multiaccounts.model/current-public-key cofx) chat-id)
    {::json-rpc/call [{:method "wakuext_createOneToOneChat"
                       :params [{:id chat-id :ensName ens-name}]
                       :on-success #(re-frame/dispatch [::one-to-one-chat-created chat-id %])
                       :on-error #(log/error "failed to create one-to-on chat" chat-id %)}]}))

(defn profile-chat-topic [public-key]
  (str "@" public-key))

(defn my-profile-chat-topic [db]
  (profile-chat-topic (get-in db [:multiaccount :public-key])))

(fx/defn handle-public-chat-created
  {:events [::public-chat-created]}
  [{:keys [db]} chat-id {:keys [dont-navigate?]} response]
  (let [chat (chats-store/<-rpc (first (:chats response)))
        db-with-chat {:db (-> db
                              (assoc-in [:chats chat-id] chat)
                              (update :chats-home-list conj chat-id))}]
    (if dont-navigate?
      db-with-chat
      (assoc db-with-chat :dispatch [:chat.ui/navigate-to-chat chat-id]))))

(fx/defn create-public-chat-go [_ chat-id opts]
  {::json-rpc/call [{:method "wakuext_createPublicChat"
                     :params [{:id chat-id}]
                     :on-success #(re-frame/dispatch [::public-chat-created chat-id opts %])
                     :on-error #(log/error "failed to create public chat" chat-id %)}]})

(fx/defn start-public-chat
  "Starts a new public chat"
  {:events [:chat.ui/start-public-chat]}
  [cofx topic {:keys [dont-navigate? profile-public-key] :as opts}]
  (if (or (new-public-chat.db/valid-topic? topic) profile-public-key)
    (if (active-chat? cofx topic)
      (when-not dont-navigate?
        (navigate-to-chat cofx topic false))
      (create-public-chat-go
       cofx
       topic
       opts))
    {:utils/show-popup {:title   (i18n/label :t/cant-open-public-chat)
                        :content (i18n/label :t/invalid-public-chat-topic)}}))

(fx/defn profile-chat-created
  {:events [::profile-chat-created]}
  [{:keys [db] :as cofx} chat-id response navigate-to?]
  (fx/merge
   cofx
   {:db db}
   #(when response
      (let [chat (chats-store/<-rpc (first (:chats response)))]
        {:db (assoc-in db [:chats chat-id] chat)}))
   #(when navigate-to?
      {:dispatch-n [[:chat.ui/preload-chat-data chat-id]
                    [:open-modal :profile]]})))

(fx/defn start-profile-chat
  "Starts a new profile chat"
  {:events [:start-profile-chat]}
  [cofx profile-public-key navigate-to?]
  (let [chat-id (profile-chat-topic profile-public-key)]
    (if (active-chat? cofx chat-id)
      {:dispatch [::profile-chat-created chat-id nil navigate-to?]}
      {::json-rpc/call [{:method "wakuext_createProfileChat"
                         :params [{:id profile-public-key}]
                         :on-success #(re-frame/dispatch [::profile-chat-created chat-id % navigate-to?])
                         :on-error #(log/error "failed to create profile chat" chat-id %)}]})))

(fx/defn disable-chat-cooldown
  "Turns off chat cooldown (protection against message spamming)"
  {:events [:chat/disable-cooldown]}
  [{:keys [db]}]
  {:db (assoc db :chat/cooldown-enabled? false)})

;; effects
(re-frame/reg-fx
 :show-cooldown-warning
 (fn [_]
   (utils/show-popup nil
                     (i18n/label :cooldown/warning-message)
                     #())))

(fx/defn mute-chat-failed
  {:events [::mute-chat-failed]}
  [{:keys [db] :as cofx} chat-id muted? error]
  (log/error "mute chat failed" chat-id error)
  {:db (assoc-in db [:chats chat-id :muted] (not muted?))})

(fx/defn mute-chat
  {:events [::mute-chat-toggled]}
  [{:keys [db] :as cofx} chat-id muted?]
  (let [method (if muted? "muteChat" "unmuteChat")]
    {:db (assoc-in db [:chats chat-id :muted] muted?)
     ::json-rpc/call [{:method (json-rpc/call-ext-method method)
                       :params [chat-id]
                       :on-error #(re-frame/dispatch [::mute-chat-failed chat-id muted? %])
                       :on-success #(log/debug "muted chat successfully")}]}))

(fx/defn show-profile
  {:events [:chat.ui/show-profile]}
  [{:keys [db] :as cofx} identity]
  (let [my-public-key (get-in db [:multiaccount :public-key])]
    (when (not= my-public-key identity)
      (fx/merge
       cofx
       {:db (assoc db :contacts/identity identity)}
       (start-profile-chat identity true)))))

(fx/defn clear-history-pressed
  {:events [:chat.ui/clear-history-pressed]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/clear-history-title)
    :content             (i18n/label :t/clear-history-confirmation-content)
    :confirm-button-text (i18n/label :t/clear-history-action)
    :on-accept           #(do
                            (re-frame/dispatch [:bottom-sheet/hide])
                            (re-frame/dispatch [:chat.ui/clear-history chat-id false]))}})

(fx/defn gaps-failed
  {:events [::gaps-failed]}
  [{:keys [db]} chat-id gap-ids error]
  (log/error "failed to fetch gaps" chat-id gap-ids error)
  {:db (dissoc db :mailserver/fetching-gaps-in-progress)})

(fx/defn sync-chat-from-sync-from-failed
  {:events [::sync-chat-from-sync-from-failed]}
  [{:keys [db]} chat-id error]
  (log/error "failed to sync chat" chat-id error)
  {:db (dissoc db :mailserver/fetching-gaps-in-progress)})

(fx/defn sync-chat-from-sync-from-success
  {:events [::sync-chat-from-sync-from-success]}
  [{:keys [db] :as cofx} chat-id synced-from]
  (log/debug "synced success" chat-id synced-from)
  {:db
   (-> db
       (assoc-in [:chats chat-id :synced-from] synced-from)
       (dissoc :mailserver/fetching-gaps-in-progress))})

(fx/defn gaps-filled
  {:events [::gaps-filled]}
  [{:keys [db] :as cofx} chat-id message-ids]
  (fx/merge
   cofx
   {:db (-> db
            (update-in [:messages chat-id] (fn [messages] (apply dissoc messages message-ids)))
            (dissoc :mailserver/fetching-gaps-in-progress))}
   (message-list/rebuild-message-list chat-id)))

(fx/defn fill-gaps
  [cofx chat-id gap-ids]
  {::json-rpc/call [{:method "wakuext_fillGaps"
                     :params [chat-id gap-ids]
                     :on-success #(re-frame/dispatch [::gaps-filled chat-id gap-ids %])
                     :on-error #(re-frame/dispatch [::gaps-failed chat-id gap-ids %])}]})

(fx/defn sync-chat-from-sync-from
  [cofx chat-id]
  (log/debug "syncing chat from sync from")
  {::json-rpc/call [{:method "wakuext_syncChatFromSyncedFrom"
                     :params [chat-id]
                     :on-success #(re-frame/dispatch [::sync-chat-from-sync-from-success chat-id %])
                     :on-error #(re-frame/dispatch [::sync-chat-from-sync-from-failed chat-id %])}]})

(fx/defn chat-ui-fill-gaps
  {:events [:chat.ui/fill-gaps]}
  [{:keys [db] :as cofx} chat-id gap-ids]
  (log/info "filling gaps" chat-id gap-ids)
  (fx/merge cofx
            {:db (assoc db :mailserver/fetching-gaps-in-progress gap-ids)}
            (if (= gap-ids #{:first-gap})
              (sync-chat-from-sync-from chat-id)
              (fill-gaps chat-id gap-ids))))

(fx/defn chat-ui-remove-chat-pressed
  {:events [:chat.ui/remove-chat-pressed]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-confirmation)
    :content             (i18n/label :t/delete-chat-confirmation)
    :confirm-button-text (i18n/label :t/delete)
    :on-accept           #(do
                            (re-frame/dispatch [:bottom-sheet/hide])
                            (re-frame/dispatch [:chat.ui/remove-chat chat-id]))}})

(fx/defn decrease-unviewed-count
  {:events [:chat/decrease-unviewed-count]}
  [{:keys [db]} chat-id {:keys [count countWithMentions]}]
  {:db (-> db
           ;; There might be some other requests being fired,
           ;; so we need to make sure the count has not been set to
           ;; 0 in the meantime
           (update-in [:chats chat-id :unviewed-messages-count]
                      #(max (- % count) 0))
           (update-in [:chats chat-id :unviewed-mentions-count]
                      #(max (- % countWithMentions) 0)))})
