(ns status-im.chat.models
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.i18n :as i18n]
            [status-im.transport.chat.core :as transport.chat]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.public-chat :as public-chat]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.desktop.events :as desktop.events]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.priority-map :refer [empty-message-map]]
            [status-im.utils.utils :as utils]))

(defn multi-user-chat? [cofx chat-id]
  (get-in cofx [:db :chats chat-id :group-chat]))

(defn group-chat? [cofx chat-id]
  (and (multi-user-chat? cofx chat-id)
       (not (get-in cofx [:db :chats chat-id :public?]))))

(defn public-chat? [cofx chat-id]
  (get-in cofx [:db :chats chat-id :public?]))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn toggle-chat-ui-prop
  "Toggles chat ui prop in active chat"
  [{:keys [current-chat-id] :as db} ui-element]
  (update-in db [:chat-ui-props current-chat-id ui-element] not))

(defn- create-new-chat
  [chat-id {:keys [db now]}]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id            chat-id
     :name               (or name (gfycat/generate-gfy chat-id))
     :color              (rand-nth colors/chat-colors)
     :group-chat         false
     :is-active          true
     :timestamp          now
     :contacts           #{chat-id}
     :last-clock-value   0
     :messages           empty-message-map}))

(fx/defn upsert-chat
  "Upsert chat when not deleted"
  [{:keys [db] :as cofx} {:keys [chat-id] :as chat-props}]
  (let [chat (merge
              (or (get (:chats db) chat-id)
                  (create-new-chat chat-id cofx))
              chat-props)]

    {:db            (update-in db [:chats chat-id] merge chat)
     :data-store/tx [(chats-store/save-chat-tx chat)]}))

(fx/defn add-public-chat
  "Adds new public group chat to db & realm"
  [cofx topic]
  (upsert-chat cofx
               {:chat-id          topic
                :is-active        true
                :name             topic
                :group-chat       true
                :contacts         #{}
                :public?          true}))

(fx/defn add-group-chat
  "Adds new private group chat to db & realm"
  [cofx chat-id chat-name admin participants]
  (upsert-chat cofx
               {:chat-id     chat-id
                :name        chat-name
                :is-active   true
                :group-chat  true
                :group-admin admin
                :contacts    participants}))

(fx/defn clear-history
  "Clears history of the particular chat"
  [{:keys [db] :as cofx} chat-id]
  (let [{:keys [messages
                deleted-at-clock-value]} (get-in db [:chats chat-id])
        last-message-clock-value (or (->> messages
                                          vals
                                          (sort-by (comp unchecked-negate :clock-value))
                                          first
                                          :clock-value)
                                     deleted-at-clock-value
                                     (utils.clocks/send 0))]
    {:db            (update-in db [:chats chat-id] merge
                               {:messages               empty-message-map
                                :unviewed-messages      #{}
                                :not-loaded-message-ids #{}
                                :deleted-at-clock-value last-message-clock-value})
     :data-store/tx [(chats-store/clear-history-tx chat-id last-message-clock-value)
                     (messages-store/delete-messages-tx chat-id)]}))

(fx/defn deactivate-chat
  [{:keys [db now] :as cofx} chat-id]
  {:db (-> db
           (assoc-in [:chats chat-id :is-active] false)
           (assoc-in [:current-chat-id] nil))
   :data-store/tx [(chats-store/deactivate-chat-tx chat-id now)]})

;; TODO: There's a race condition here, as the removal of the filter (async)
;; is done at the same time as the removal of the chat, so a message
;; might come between and restore the chat. Multiple way to handle this
;; (remove chat only after the filter has been removed, probably the safest,
;; flag the chat to ignore new messages, change receive method for public/group chats)
;; For now to keep the code simplier and avoid significant changes, best to leave as it is.
(fx/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge cofx
            #(when (public-chat? % chat-id)
               (transport.chat/unsubscribe-from-chat % chat-id))
            (deactivate-chat chat-id)
            (clear-history chat-id)
            (navigation/navigate-to-cofx :home {})))

(fx/defn send-messages-seen
  [{:keys [db] :as cofx} chat-id message-ids]
  (when (not (get-in db [:chats chat-id :group-chat]))
    (protocol/send (protocol/map->MessagesSeen {:message-ids message-ids}) chat-id cofx)))

(defn- unread-messages-number [chats]
  (apply + (map (comp count :unviewed-messages) chats)))

(fx/defn update-dock-badge-label
  [cofx]
  (let [chats (get-in cofx [:db :chats])
        active-chats (filter :is-active (vals chats))
        private-chats (filter (complement :public?) active-chats)
        public-chats (filter :public? active-chats)
        private-chats-unread-count (unread-messages-number private-chats)
        public-chats-unread-count (unread-messages-number public-chats)
        label (cond
                (pos? private-chats-unread-count) private-chats-unread-count
                (pos? public-chats-unread-count) "•"
                :else nil)]
    {:set-dock-badge-label label}))

;; TODO (janherich) - ressurect `constants/system` messages for group chats in the future
(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  [{:keys [db] :as cofx} chat-id]
  (when-let [all-unviewed-ids (seq (get-in db [:chats chat-id :unviewed-messages]))]
    (let [me                  (accounts.db/current-public-key cofx)
          updated-statuses    (keep (fn [message-id]
                                      (some-> db
                                              (get-in [:chats chat-id :message-statuses
                                                       message-id me])
                                              (assoc :status :seen)))
                                    all-unviewed-ids)
          loaded-unviewed-ids (map :message-id updated-statuses)]
      (when (seq loaded-unviewed-ids)
        (fx/merge cofx
                  {:db (-> (reduce (fn [acc {:keys [message-id status]}]
                                     (assoc-in acc [:chats chat-id :message-statuses
                                                    message-id me :status]
                                               status))
                                   db
                                   updated-statuses)
                           (update-in [:chats chat-id :unviewed-messages]
                                      #(apply disj % loaded-unviewed-ids)))
                   :data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]}
                  (send-messages-seen chat-id loaded-unviewed-ids)
                  (when platform/desktop?
                    (update-dock-badge-label)))))))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [{:keys [db] :as cofx} chat-id]
  (fx/merge cofx
            {:db (-> (assoc db :current-chat-id chat-id)
                     (set-chat-ui-props {:validation-messages nil}))}
            (mark-messages-seen chat-id)))

(fx/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  [cofx chat-id {:keys [modal? navigation-reset?]}]
  (cond
    modal?
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat-modal {})
              (preload-chat-data chat-id))

    navigation-reset?
    (fx/merge cofx
              (navigation/navigate-reset {:index   1
                                          :actions [{:routeName :home}
                                                    {:routeName :chat}]})
              (preload-chat-data chat-id))

    :else
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat {})
              (preload-chat-data chat-id))))

(fx/defn start-chat
  "Start a chat, making sure it exists"
  [{:keys [db] :as cofx} chat-id opts]
  ;; don't allow to open chat with yourself
  (when (not= (accounts.db/current-public-key cofx) chat-id)
    (fx/merge cofx
              (upsert-chat {:chat-id chat-id
                            :is-active true})
              (navigate-to-chat chat-id opts))))

(fx/defn start-public-chat
  "Starts a new public chat"
  [cofx topic opts]
  (fx/merge cofx
            (add-public-chat topic)
            (navigate-to-chat topic opts)
            (public-chat/join-public-chat topic)
            (when platform/desktop?
              (desktop.events/change-tab :home))))

(fx/defn disable-chat-cooldown
  "Turns off chat cooldown (protection against message spamming)"
  [{:keys [db]}]
  {:db (assoc db :chat/cooldown-enabled? false)})

;; effects
(re-frame/reg-fx
 :show-cooldown-warning
 (fn [_]
   (utils/show-popup nil
                     (i18n/label :cooldown/warning-message)
                     #())))

(defn set-dock-badge-label [label]
  "Sets dock badge label (OSX only for now).
   Label must be a string. Pass nil or empty string to clear the label."
  (.setDockBadgeLabel react/desktop-notification label))

(re-frame/reg-fx
 :set-dock-badge-label
 set-dock-badge-label)
