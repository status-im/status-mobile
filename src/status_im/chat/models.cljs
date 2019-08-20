(ns status-im.chat.models
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.contact.core :as contact.core]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.message.protocol :as transport.protocol]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.desktop.events :as desktop.events]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.priority-map :refer [empty-message-map]]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

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
    (fx/merge cofx
              {:db (update-in db [:chats chat-id] merge chat)}
              (chats-store/save-chat chat))))

(fx/defn add-public-chat
  "Adds new public group chat to db"
  [cofx topic]
  (if config/use-status-go-protocol?
    {::json-rpc/call [{:method "status_joinPublicChat"
                       :params [topic]
                       :on-success
                       #(log/debug "successfully joined a public chat:" topic)
                       :on-error
                       (fn [error]
                         (log/error "can't join a public chat:" error))}]}
    (upsert-chat cofx
                 {:chat-id                        topic
                  :is-active                      true
                  :name                           topic
                  :group-chat                     true
                  :contacts                       #{}
                  :public?                        true
                  :might-have-join-time-messages? true
                  :unviewed-messages-count        0
                  :loaded-unviewed-messages-ids   #{}})))

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
    (fx/merge
     cofx
     {:db            (update-in db [:chats chat-id] merge
                                {:messages                  empty-message-map
                                 :message-groups            {}
                                 :last-message-content      nil
                                 :last-message-content-type nil
                                 :unviewed-messages-count   0
                                 :deleted-at-clock-value    last-message-clock-value})}
     (messages-store/delete-messages-by-chat-id chat-id)
     #(chats-store/save-chat % (get-in % [:db :chats chat-id])))))

(fx/defn deactivate-chat
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:chats chat-id :is-active] false)
                     (assoc-in [:current-chat-id] nil))}
            #(chats-store/save-chat % (get-in % [:db :chats chat-id]))))

(fx/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  [{:keys [db now] :as cofx} chat-id]
  (if config/use-status-go-protocol?
    (fx/merge cofx
              {::json-rpc/call [{:method "status_removeChat"
                                 :params [chat-id]
                                 :on-success
                                 #(log/debug "successfully removed a chat:" chat-id)
                                 :on-error
                                 (fn [error]
                                   (log/error "can't remove a chat:" error))}]}
              (when (not (= (:view-id db) :home))
                (navigation/navigate-to-cofx :home {})))
    (fx/merge cofx
              (mailserver/remove-gaps chat-id)
              (mailserver/remove-range chat-id)
              (deactivate-chat chat-id)
              (clear-history chat-id)
              (transport.filters/stop-listening chat-id)
              (when (not (= (:view-id db) :home))
                (navigation/navigate-to-cofx :home {})))))

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
  [{:keys [db] :as cofx} {:keys [chat-id loaded-unviewed-messages-ids]}]
  (let [{:keys [loaded-unviewed-messages-ids unviewed-messages-count]}
        (get-in db [:chats chat-id])]
    (upsert-chat
     cofx
     {:chat-id                      chat-id
      :unviewed-messages-count      (subtract-seen-messages
                                     unviewed-messages-count
                                     loaded-unviewed-messages-ids)
      :loaded-unviewed-messages-ids #{}})))

(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  [{:keys [db] :as cofx} chat-id]
  (let [loaded-unviewed-ids (get-in db [:chats chat-id :loaded-unviewed-messages-ids])]
    (when (seq loaded-unviewed-ids)
      (fx/merge cofx
                {:db            (reduce (fn [acc message-id]
                                          (assoc-in acc [:chats chat-id :messages
                                                         message-id :seen]
                                                    true))
                                        db
                                        loaded-unviewed-ids)}
                (messages-store/mark-messages-seen loaded-unviewed-ids)
                (update-chats-unviewed-messages-count {:chat-id chat-id})
                (when platform/desktop?
                  (update-dock-badge-label))))))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [{:keys [db] :as cofx} chat-id]
  (fx/merge cofx
            {:db (-> (assoc db :current-chat-id chat-id)
                     (set-chat-ui-props {:validation-messages nil}))}
            ;; Group chat don't need this to load as all the loading of topics
            ;; happens on membership changes
            (when-not (group-chat? cofx chat-id)
              (transport.filters/load-chat chat-id))
            (when platform/desktop?
              (mark-messages-seen chat-id))
            (tribute-to-talk/check-tribute chat-id)))

(fx/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  [cofx chat-id {:keys [modal? navigation-reset?]}]
  (cond
    modal?
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat-modal {})
              (preload-chat-data chat-id))

    :else
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat {})
              (preload-chat-data chat-id))))

(fx/defn start-chat
  "Start a chat, making sure it exists"
  [{:keys [db] :as cofx} chat-id opts]
  ;; don't allow to open chat with yourself
  (when (not= (multiaccounts.model/current-public-key cofx) chat-id)
    (if config/use-status-go-protocol?
      (fx/merge cofx
                {::json-rpc/call [{:method "status_startOneOnOneChat"
                                   :params [chat-id]
                                   :on-success
                                   #(log/debug "successfully started a 1-1 chat with:" chat-id)
                                   :on-error
                                   (fn [error]
                                     (log/error "can't start a 1-1 chat:" error))}]}
                (navigate-to-chat chat-id opts))
      (fx/merge cofx
                (upsert-chat {:chat-id   chat-id
                              :is-active true})
                (transport.filters/load-chat chat-id)
                (navigate-to-chat chat-id opts)))))

(fx/defn start-public-chat
  "Starts a new public chat"
  [cofx topic {:keys [dont-navigate?] :as opts}]
  (if (active-chat? cofx topic)
    (when-not dont-navigate?
      (navigate-to-chat cofx topic opts))
    (fx/merge cofx
              (add-public-chat topic)
              (transport.filters/load-chat topic)
              #(when-not dont-navigate?
                 (navigate-to-chat % topic opts))
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

(fx/defn show-profile
  {:events [:chat.ui/show-profile]}
  [cofx identity]
  (fx/merge (assoc-in cofx [:db :contacts/identity] identity)
            (contact.core/create-contact identity)
            (tribute-to-talk/check-tribute identity)
            (navigation/navigate-to-cofx :profile nil)))
