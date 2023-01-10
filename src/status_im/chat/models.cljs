(ns status-im.chat.models
  (:require [utils.i18n :as i18n]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
<<<<<<< HEAD
            [taoensso.timbre :as log]
            [status-im.add-new.db :as new-public-chat.db]
            [status-im.data-store.chats :as chats-store]))
=======
            [status-im.utils.utils :as utils]
            [status-im2.contexts.chat.messages.delete-message-for-me.events :as delete-for-me]
            [status-im2.contexts.chat.messages.delete-message.events :as delete-message]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]))

(defn- get-chat
  [cofx chat-id]
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

(defn active-chat?
  [cofx chat-id]
  (let [chat (get-chat cofx chat-id)]
    (:active chat)))

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
    {:chat-id          chat-id
     :name             (or name "")
     :color            (rand-nth colors/chat-colors)
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

(defn filter-chats
  [db]
  (fn [val]
    (and (not (get-in db [:chats (:chat-id val)])) (:public? val))))

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
  [{:keys [db] :as cofx} chat-id remove-chat?]
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

(rf/defn clear-history-handler
  "Clears history of the particular chat"
  {:events [:chat.ui/clear-history]}
  [{:keys [db] :as cofx} chat-id remove-chat?]
  (rf/merge cofx
            {:db            db
             :json-rpc/call [{:method     "wakuext_clearHistory"
                              :params     [{:id chat-id}]
                              :on-success #(re-frame/dispatch [::history-cleared chat-id %])
                              :on-error   #(log/error "failed to clear history " chat-id %)}]}
            (clear-history chat-id remove-chat?)))

(rf/defn chat-deactivated
  {:events [::chat-deactivated]}
  [_ chat-id]
  (log/debug "chat deactivated" chat-id))

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
                     :on-success #(re-frame/dispatch [::chat-deactivated chat-id])
                     :on-error   #(log/error "failed to create public chat" chat-id %)}]}
   (clear-history chat-id true)))

(rf/defn offload-messages
  {:events [:offload-messages]}
  [{:keys [db]} chat-id]
  (merge {:db (-> db
                  (update :messages dissoc chat-id)
                  (update :message-lists dissoc chat-id)
                  (update :pagination-info dissoc chat-id))}
         (when (and (= chat-id constants/timeline-chat-id) (= (:view-id db) :status))
           {:dispatch [:init-timeline-chat]})))

(rf/defn close-chat
  {:events [:close-chat]}
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

(rf/defn show-more-chats
  {:events [:chat.ui/show-more-chats]}
  [{:keys [db]}]
  (when (< (:home-items-show-number db) (count (:chats db)))
    {:db (update db :home-items-show-number + 40)}))

(rf/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  {:events [:chat.ui/preload-chat-data]}
  [cofx chat-id]
  (loading/load-messages cofx chat-id))

(rf/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-chat]}
  [{db :db :as cofx} chat-id]
  (rf/merge cofx
            {:dispatch [:navigate-to :chat]}
            (navigation/change-tab :chat)
            (when-not (= (:view-id db) :community)
              (navigation/pop-to-root-tab :chat-stack))
            (close-chat false)
            (force-close-chat chat-id)
            (fn [{:keys [db]}]
              {:db (assoc db :current-chat-id chat-id)})
            (preload-chat-data chat-id)
            #(when (group-chat? cofx chat-id)
               (loading/load-chat % chat-id))))

(rf/defn navigate-to-chat-nav2
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-chat-nav2]}
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
  {:events [::history-cleared]}
  [{:keys [db]} chat-id response]
  (let [chat (chats-store/<-rpc (first (:chats response)))]
    {:db (assoc-in db [:chats chat-id] chat)}))

(rf/defn handle-one-to-one-chat-created
  {:events [::one-to-one-chat-created]}
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
     :dispatch [:chat.ui/navigate-to-chat-nav2 chat-id]}))

(rf/defn navigate-to-user-pinned-messages
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-pinned-messages]}
  [cofx chat-id]
  (navigation/navigate-to cofx :chat-pinned-messages {:chat-id chat-id}))

(rf/defn navigate-to-horizontal-images
         {:events [:chat.ui/navigate-to-horizontal-images]}
         [cofx messages index]
         (navigation/navigate-to cofx :images-horizontal {:messages messages :index index}))

(rf/defn update-shared-element-id
         {:events [:chat.ui/update-shared-element-id]}
         [{:keys [db]} shared-element-id]
         {:db (assoc db :shared-element-id shared-element-id)})


(rf/defn start-chat
  "Start a chat, making sure it exists"
  {:events [:chat.ui/start-chat]}
  [{:keys [db] :as cofx} chat-id ens-name]
  ;; don't allow to open chat with yourself
  (when (not= (multiaccounts.model/current-public-key cofx) chat-id)
    {:json-rpc/call [{:method     "wakuext_createOneToOneChat"
                      :params     [{:id chat-id :ensName ens-name}]
                      :on-success #(re-frame/dispatch [::one-to-one-chat-created chat-id %])
                      :on-error   #(log/error "failed to create one-to-on chat" chat-id %)}]}))

(defn profile-chat-topic
  [public-key]
  (str "@" public-key))
>>>>>>> 2ee75ae1c... updates

;; OLD

(rf/defn handle-public-chat-created
  {:events [::public-chat-created]}
  [{:keys [db]} chat-id response]
  {:db       (-> db
                 (assoc-in [:chats chat-id] (chats-store/<-rpc (first (:chats response))))
                 (update :chats-home-list conj chat-id))
   :dispatch [:chat/navigate-to-chat chat-id]})

(rf/defn create-public-chat-go
  [_ chat-id]
  {:json-rpc/call [{:method     "wakuext_createPublicChat"
                    :params     [{:id chat-id}]
                    :on-success #(re-frame/dispatch [::public-chat-created chat-id %])
                    :on-error   #(log/error "failed to create public chat" chat-id %)}]})

(rf/defn start-public-chat
  "Starts a new public chat"
  {:events [:chat.ui/start-public-chat]}
  [cofx topic]
  (if (new-public-chat.db/valid-topic? topic)
    (create-public-chat-go
     cofx
     topic)
    {:utils/show-popup {:title   (i18n/label :t/cant-open-public-chat)
                        :content (i18n/label :t/invalid-public-chat-topic)}}))
