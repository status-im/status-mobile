(ns status-im2.contexts.chat.events
  (:require [clojure.set :as set]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [status-im2.contexts.chat.messages.list.state :as chat.state]
            [status-im2.contexts.chat.messages.delete-message-for-me.events :as delete-for-me]
            [status-im2.contexts.chat.messages.delete-message.events :as delete-message]
            [status-im2.navigation.events :as navigation]
            [status-im2.constants :as constants]
            [status-im.chat.models.loading :as loading]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.utils.clocks :as utils.clocks]))

(defn- get-chat
  [cofx chat-id]
  (get-in cofx [:db :chats chat-id]))

(defn multi-user-chat?
  ([chat]
   (:group-chat chat))
  ([cofx chat-id]
   (multi-user-chat? (get-chat cofx chat-id))))

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

(defn active-chat?
  [cofx chat-id]
  (let [chat (get-chat cofx chat-id)]
    (:active chat)))

(defn group-chat?
  ([chat]
   (and (multi-user-chat? chat)
        (not (public-chat? chat))))
  ([cofx chat-id]
   (group-chat? (get-chat cofx chat-id))))

(defn- create-new-chat
  [chat-id {:keys [db now]}]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id          chat-id
     :name             (or name "")
     :chat-type        constants/one-to-one-chat-type
     :group-chat       false
     :timestamp        now
     :contacts         #{chat-id}
     :last-clock-value 0}))

(defn map-chats
  [{:keys [db] :as cofx}]
  (fn [val]
    (assoc
     (merge
      (or (get (:chats db) (:chat-id val))
          (create-new-chat (:chat-id val) cofx))
      val)
     :invitation-admin
     (:invitation-admin val))))

(rf/defn leave-removed-chat
  [{{:keys [view-id current-chat-id chats]} :db
    :as                                     cofx}]
  (when (and (= view-id :chat)
             (not (contains? chats current-chat-id)))
    (navigation/navigate-back cofx)))

(rf/defn ensure-chats
  "Add chats to db and update"
  [{:keys [db] :as cofx} chats]
  (let [{:keys [all-chats chats-home-list removed-chats]}
        (reduce
         (fn [acc {:keys [chat-id profile-public-key timeline? community-id active muted] :as chat}]
           (if (not (or active muted))
             (update acc :removed-chats conj chat-id)
             (cond-> acc
               (and (not profile-public-key) (not timeline?) (not community-id) active)
               (update :chats-home-list conj chat-id)
               :always
               (assoc-in [:all-chats chat-id] chat))))
         {:all-chats       {}
          :chats-home-list #{}
          :removed-chats   #{}}
         (map (map-chats cofx) chats))]
    (rf/merge
     cofx
     (merge {:db (-> db
                     (update :chats merge all-chats)
                     (update :chats-home-list set/union chats-home-list)
                     (update :chats #(apply dissoc % removed-chats))
                     (update :chats-home-list set/difference removed-chats))}
            (when (not-empty removed-chats)
              {:clear-message-notifications
               [removed-chats
                (get-in db [:multiaccount :remote-push-notifications-enabled?])]}))
     leave-removed-chat)))

(rf/defn clear-history
  "Clears history of the particular chat"
  [{:keys [db]} chat-id remove-chat?]
  (let [{:keys [last-message public?
                deleted-at-clock-value]}
        (get-in db [:chats chat-id])
        last-message-clock-value (if (and public? remove-chat?)
                                   0
                                   (or (:clock-value last-message)
                                       deleted-at-clock-value
                                       (utils.clocks/send 0)))]
    {:db (-> db
             (assoc-in [:messages chat-id] {})
             (update-in [:message-lists] dissoc chat-id)
             (update :chats
                     (fn [chats]
                       (if (contains? chats chat-id)
                         (update chats
                                 chat-id
                                 merge
                                 {:last-message            nil
                                  :unviewed-messages-count 0
                                  :unviewed-mentions-count 0
                                  :deleted-at-clock-value  last-message-clock-value})
                         chats))))}))

(rf/defn deactivate-chat
  "Deactivate chat in db, no side effects"
  [{:keys [db now] :as cofx} chat-id]
  (rf/merge
   cofx
   {:db            (-> (if (get-in db [:chats chat-id :muted])
                           (assoc-in db [:chats chat-id :active] false)
                           (update db :chats dissoc chat-id))
                       (update :chats-home-list disj chat-id)
                       (assoc :current-chat-id nil))
    :json-rpc/call [{:method     "wakuext_deactivateChat"
                     :params     [{:id chat-id}]
                     :on-success #()
                     :on-error   #(log/error "failed to create public chat" chat-id %)}]}
   (clear-history chat-id true)))

(rf/defn offload-messages
  {:events [:chat/offload-messages]}
  [{:keys [db]} chat-id]
  {:db (-> db
           (update :messages dissoc chat-id)
           (update :message-lists dissoc chat-id)
           (update :pagination-info dissoc chat-id))})

(rf/defn close-chat
  {:events [:chat/close]}
  [{:keys [db] :as cofx} navigate-to-shell?]
  (when-let [chat-id (:current-chat-id db)]
    (chat.state/reset-visible-item)
    (rf/merge cofx
              (merge
               {:db (dissoc db :current-chat-id)}
               (let [community-id (get-in db [:chats chat-id :community-id])]
                 ;; When navigating back from community chat to community, update switcher card
                 (when (and community-id (not navigate-to-shell?))
                   {:dispatch [:shell/add-switcher-card
                               :community {:community-id community-id}]})))
              (delete-for-me/sync-all)
              (delete-message/send-all)
              (offload-messages chat-id))))

(rf/defn force-close-chat
  [{:keys [db] :as cofx} chat-id]
  (do
    (chat.state/reset-visible-item)
    (rf/merge cofx
              {:db (dissoc db :current-chat-id)}
              (offload-messages chat-id))))

(rf/defn show-more-chats
  {:events [:chat/show-more-chats]}
  [{:keys [db]}]
  (when (< (:home-items-show-number db) (count (:chats db)))
    {:db (update db :home-items-show-number + 40)}))

(rf/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  {:events [:chat/preload-data]}
  [cofx chat-id]
  (loading/load-messages cofx chat-id))

(rf/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat/navigate-to-chat]}
  [{db :db :as cofx} chat-id from-shell?]
  (rf/merge cofx
            {:dispatch [:navigate-to-nav2 :chat chat-id from-shell?]}
            (when-not (= (:view-id db) :community)
              (navigation/pop-to-root-tab :shell-stack))
            (close-chat false)
            (force-close-chat chat-id)
            (fn [{:keys [db]}]
              {:db (assoc db :current-chat-id chat-id)})
            (preload-chat-data chat-id)
            #(when (group-chat? cofx chat-id)
               (loading/load-chat % chat-id))))

(rf/defn handle-clear-history-response
  {:events [:chat/history-cleared]}
  [{:keys [db]} chat-id response]
  (let [chat (chats-store/<-rpc (first (:chats response)))]
    {:db (assoc-in db [:chats chat-id] chat)}))

(rf/defn handle-one-to-one-chat-created
  {:events [:chat/one-to-one-chat-created]}
  [{:keys [db]} chat-id response]
  (let [chat        (chats-store/<-rpc (first (:chats response)))
        contact-rpc (first (:contacts response))
        contact     (when contact-rpc (contacts-store/<-rpc contact-rpc))]
    {:db       (cond-> db
                 contact
                 (assoc-in [:contacts/contacts chat-id] contact)
                 :always
                 (assoc-in [:chats chat-id] chat)
                 :always
                 (update :chats-home-list conj chat-id))
     :dispatch [:chat/navigate-to-chat chat-id]}))

(rf/defn decrease-unviewed-count
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

;;;; UI

(rf/defn start-chat
  "Start a chat, making sure it exists"
  {:events [:chat.ui/start-chat]}
  [cofx chat-id ens-name]
  (when (not= (multiaccounts.model/current-public-key cofx) chat-id)
    {:json-rpc/call [{:method     "wakuext_createOneToOneChat"
                      :params     [{:id chat-id :ensName ens-name}]
                      :on-success #(rf/dispatch [:chat/one-to-one-chat-created chat-id %])
                      :on-error   #(log/error "failed to create one-to-on chat" chat-id %)}]}))

(rf/defn clear-history-handler
  "Clears history of the particular chat"
  {:events [:chat.ui/clear-history]}
  [{:keys [db] :as cofx} chat-id remove-chat?]
  (rf/merge cofx
            {:db            db
             :json-rpc/call [{:method     "wakuext_clearHistory"
                              :params     [{:id chat-id}]
                              :on-success #(rf/dispatch [:chat/history-cleared chat-id %])
                              :on-error   #(log/error "failed to clear history " chat-id %)}]}
            (clear-history chat-id remove-chat?)))

(rf/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  {:events [:chat.ui/remove-chat]}
  [{:keys [db now] :as cofx} chat-id]
  (rf/merge cofx
            {:clear-message-notifications
             [[chat-id] (get-in db [:multiaccount :remote-push-notifications-enabled?])]
             :dispatch                    [:shell/close-switcher-card chat-id]}
            (deactivate-chat chat-id)
            (offload-messages chat-id)))

(rf/defn mute-chat-failed
  {:events [:chat/mute-failed]}
  [{:keys [db]} chat-id muted? error]
  (log/error "mute chat failed" chat-id error)
  {:db (assoc-in db [:chats chat-id :muted] (not muted?))})

(rf/defn mute-chat
  {:events [:chat.ui/mute]}
  [{:keys [db]} chat-id muted?]
  (let [method (if muted? "wakuext_muteChat" "wakuext_unmuteChat")]
    {:db            (assoc-in db [:chats chat-id :muted] muted?)
     :json-rpc/call [{:method     method
                      :params     [chat-id]
                      :on-error   #(rf/dispatch [:chat/mute-failed chat-id muted? %])
                      :on-success #()}]}))

(rf/defn show-clear-history-confirmation
  {:events [:chat.ui/show-clear-history-confirmation]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/clear-history-title)
    :content             (i18n/label :t/clear-history-confirmation-content)
    :confirm-button-text (i18n/label :t/clear-history-action)
    :on-accept           #(do
                            (rf/dispatch [:bottom-sheet/hide])
                            (rf/dispatch [:chat.ui/clear-history chat-id false]))}})

(rf/defn show-remove-chat-confirmation
  {:events [:chat.ui/show-remove-confirmation]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-confirmation)
    :content             (i18n/label :t/delete-chat-confirmation)
    :confirm-button-text (i18n/label :t/delete)
    :on-accept           #(do
                            (rf/dispatch [:bottom-sheet/hide])
                            (rf/dispatch [:chat.ui/remove-chat chat-id]))}})

(rf/defn navigate-to-user-pinned-messages
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-pinned-messages]}
  [cofx chat-id]
  (navigation/navigate-to cofx :chat-pinned-messages {:chat-id chat-id}))
