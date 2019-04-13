(ns status-im.chat.models
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.contact-code.core :as contact-code]
            [status-im.i18n :as i18n]
            [status-im.transport.chat.core :as transport.chat]
            [status-im.transport.utils :as transport.utils]
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
            [status-im.utils.utils :as utils]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.partitioned-topic :as transport.topic]))

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

(defn active-chat?
  ([chat]
   (:is-active chat))
  ([cofx chat-id]
   (active-chat? (get-chat cofx chat-id))))

(defn group-chat?
  ([chat]
   (and (multi-user-chat? chat)
        (not (public-chat? chat))))
  ([cofx chat-id]
   (group-chat? (get-chat cofx chat-id))))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn toggle-chat-ui-prop
  "Toggles chat ui prop in active chat"
  [{:keys [current-chat-id] :as db} ui-element]
  (update-in db [:chat-ui-props current-chat-id ui-element] not))

(fx/defn join-time-messages-checked
  "The key :might-have-join-time-messages? in public chats signals that
  the public chat is freshly (re)created and requests for messages to the
  mailserver for the topic has not completed yet. Likewise, the key
  :join-time-mail-request-id is associated a little bit after, to signal that
  the request to mailserver was a success. When request is signalled complete
  by mailserver, corresponding event :chat.ui/join-time-messages-checked
  dissociates these two fileds via this function, thereby signalling that the
  public chat is not fresh anymore."
  [{:keys [db] :as cofx} chat-id]
  (when (:might-have-join-time-messages? (get-chat cofx chat-id))
    {:db (update-in db
                    [:chats chat-id]
                    dissoc
                    :join-time-mail-request-id
                    :might-have-join-time-messages?)}))

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
               {:chat-id                        topic
                :is-active                      true
                :name                           topic
                :group-chat                     true
                :contacts                       #{}
                :public?                        true
                :might-have-join-time-messages? true
                :unviewed-messages-count        0
                :loaded-unviewed-messages-ids   #{}}))

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
                               {:messages                  empty-message-map
                                :message-groups            {}
                                :last-message-content      nil
                                :last-message-content-type nil
                                :unviewed-messages-count   0
                                :deleted-at-clock-value    last-message-clock-value})
     :data-store/tx [(chats-store/clear-history-tx chat-id last-message-clock-value)
                     (messages-store/delete-chat-messages-tx chat-id)]}))

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
            #(when (group-chat? % chat-id)
               (mailserver/remove-chat-from-mailserver-topic % chat-id))
            (mailserver/remove-gaps chat-id)
            (mailserver/remove-range chat-id)
            (deactivate-chat chat-id)
            (clear-history chat-id)
            #(when (one-to-one-chat? % chat-id)
               (contact-code/stop-listening % chat-id))
            (navigation/navigate-to-cofx :home {})))

(fx/defn send-messages-seen
  [{:keys [db] :as cofx} chat-id message-ids]
  (when (not (get-in db [:chats chat-id :group-chat]))
    (protocol/send (protocol/map->MessagesSeen {:message-ids message-ids}) chat-id cofx)))

(defn- unread-messages-number [chats]
  (apply + (map :unviewed-messages-count chats)))

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

(defn subtract-seen-messages
  [old-count new-seen-messages-ids]
  (max 0 (- old-count (count new-seen-messages-ids))))

(fx/defn update-chats-unviewed-messages-count
  [{:keys [db] :as cofx} {:keys [chat-id new-loaded-unviewed-messages-ids]}]
  (let [{:keys [loaded-unviewed-messages-ids unviewed-messages-count]}
        (get-in db [:chats chat-id])

        unviewed-messages-ids (if (seq new-loaded-unviewed-messages-ids)
                                new-loaded-unviewed-messages-ids
                                loaded-unviewed-messages-ids)]
    (upsert-chat
     cofx
     {:chat-id                      chat-id
      :unviewed-messages-count      (subtract-seen-messages
                                     unviewed-messages-count
                                     unviewed-messages-ids)
      :loaded-unviewed-messages-ids #{}})))

;; TODO (janherich) - ressurect `constants/system` messages for group chats in the future
(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  [{:keys [db] :as cofx} chat-id]
  (let [public-key          (accounts.db/current-public-key cofx)
        loaded-unviewed-ids (get-in db [:chats chat-id :loaded-unviewed-messages-ids])
        updated-statuses    (map (fn [message-id]
                                   {:chat-id    chat-id
                                    :message-id message-id
                                    :status-id  (str chat-id "-" message-id)
                                    :public-key public-key
                                    :status     :seen})
                                 loaded-unviewed-ids)]
    (when (seq loaded-unviewed-ids)
      (fx/merge cofx
                {:db            (reduce (fn [acc {:keys [message-id status]}]
                                          (assoc-in acc [:chats chat-id :message-statuses
                                                         message-id public-key :status]
                                                    status))
                                        db
                                        updated-statuses)
                 :data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]}
                (update-chats-unviewed-messages-count {:chat-id chat-id})
                ;;TODO(rasom): uncomment when seen messages will be revisited
                #_(send-messages-seen chat-id loaded-unviewed-ids)
                (when platform/desktop?
                  (update-dock-badge-label))))))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [{:keys [db] :as cofx} chat-id]
  (fx/merge cofx
            {:db (-> (assoc db :current-chat-id chat-id)
                     (set-chat-ui-props {:validation-messages nil}))}
            (contact-code/listen-to-chat chat-id)
            (when platform/desktop?
              (mark-messages-seen chat-id))))

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
              (upsert-chat {:chat-id   chat-id
                            :is-active true})
              (navigate-to-chat chat-id opts))))

(fx/defn start-public-chat
  "Starts a new public chat"
  [cofx topic {:keys [dont-navigate?] :as opts}]
  (if (active-chat? cofx topic)
    (when-not dont-navigate?
      (navigate-to-chat cofx topic opts))
    (fx/merge cofx
              (add-public-chat topic)
              #(when-not dont-navigate?
                 (navigate-to-chat % topic opts))
              (public-chat/join-public-chat topic)
              #(when platform/desktop?
                 (desktop.events/change-tab % :home)))))

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
