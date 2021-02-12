(ns status-im.chat.models
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.contact.core :as contact.core]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.ui.components.colors :as colors]
            [status-im.constants :as constants]
            [status-im.navigation :as navigation]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.utils.types :as types]
            [status-im.add-new.db :as new-public-chat.db]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.mailserver.constants :as mailserver.constants]))

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

(defn community-chat? [{:keys [chat-type]}]
  (= chat-type constants/community-chat-type))

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

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn dissoc-join-time-fields [db chat-id]
  (update-in db [:chats chat-id] dissoc
             :join-time-mail-request-id
             :might-have-join-time-messages?))

(fx/defn join-time-messages-checked
  "The key :might-have-join-time-messages? in public chats signals that
  the public chat is freshly (re)created and requests for messages to the
  mailserver for the topic has not completed yet. Likewise, the key
  :join-time-mail-request-id is associated a little bit after, to signal that
  the request to mailserver was a success. When request is signalled complete
  by mailserver, corresponding event :chat.ui/join-time-messages-checked
  dissociates these two fileds via this function, thereby signalling that the
  public chat is not fresh anymore."
  {:events [:chat.ui/join-time-messages-checked]}
  [{:keys [db] :as cofx} chat-id]
  (when (:might-have-join-time-messages? (get-chat cofx chat-id))
    {:db (dissoc-join-time-fields db chat-id)}))

(fx/defn join-time-messages-checked-for-chats
  [{:keys [db]} chat-ids]
  {:db (reduce #(if (:might-have-join-time-messages? (get-chat {:db %1} %2))
                  (dissoc-join-time-fields %1 %2)
                  %1)
               db
               chat-ids)})

(defn- create-new-chat
  [chat-id {:keys [db now]}]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id            chat-id
     :name               (or name "")
     :color              (rand-nth colors/chat-colors)
     :chat-type          constants/one-to-one-chat-type
     :group-chat         false
     :is-active          true
     :timestamp          now
     :contacts           #{chat-id}
     :last-clock-value   0}))

(fx/defn ensure-chat
  "Add chat to db and update"
  [{:keys [db] :as cofx} {:keys [chat-id timeline?] :as chat-props}]
  (let [chat (merge
              (or (get (:chats db) chat-id)
                  (create-new-chat chat-id cofx))
              chat-props)
        new? (not (get-in db [:chats chat-id]))
        public? (public-chat? chat)]
    (fx/merge cofx
              {:db (update-in db [:chats chat-id] merge chat)}
              (when (and public? new? (not timeline?))
                (transport.filters/load-chat chat-id)))))

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

(fx/defn ensure-chats
  "Add chats to db and update"
  [{:keys [db] :as cofx} chats]
  (let [chats (map (map-chats cofx) chats)
        filtered-chats (filter (filter-chats db) chats)]
    (fx/merge cofx
              {:db (update db :chats #(reduce
                                       (fn [acc {:keys [chat-id] :as chat}]
                                         (update acc chat-id merge chat))
                                       %
                                       chats))}
              (transport.filters/load-chats filtered-chats))))

(fx/defn upsert-chat
  "Upsert chat when not deleted"
  [{:keys [db] :as cofx} {:keys [chat-id] :as chat-props} on-success]
  (fx/merge cofx
            (ensure-chat chat-props)
            #(chats-store/save-chat % (get-in % [:db :chats chat-id]) on-success)))

(fx/defn handle-save-chat
  {:events [::save-chat]}
  [{:keys [db] :as cofx} chat-id on-success]
  (chats-store/save-chat cofx (get-in db [:chats chat-id]) on-success))

(fx/defn handle-mark-all-read-successful
  {:events [::mark-all-read-successful]}
  [{:keys [db] :as cofx} chat-id]
  {:db (assoc-in db [:chats chat-id :unviewed-messages-count] 0)})

(fx/defn handle-mark-all-read
  {:events [:chat.ui/mark-all-read-pressed
            :chat.ui/mark-public-all-read]}
  [{:keys [db] :as cofx} chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "markAllRead")
                     :params [chat-id]
                     :on-success #(re-frame/dispatch [::mark-all-read-successful chat-id])}]})

(fx/defn add-public-chat
  "Adds new public group chat to db"
  [cofx topic profile-public-key timeline?]
  (upsert-chat cofx
               {:chat-id                        topic
                :timeline?                      timeline?
                :profile-public-key             profile-public-key
                :is-active                      true
                :name                           topic
                :chat-name                      (str "#" topic)
                :group-chat                     true
                :chat-type                      (if timeline?
                                                  constants/timeline-chat-type
                                                  constants/public-chat-type)
                :contacts                       #{}
                :public?                        true
                :might-have-join-time-messages? (get-in cofx [:db :multiaccount :use-mailservers?])
                :unviewed-messages-count        0
                :loaded-unviewed-messages-ids   #{}}
               nil))

(fx/defn clear-history
  "Clears history of the particular chat"
  {:events [:chat.ui/clear-history]}
  [{:keys [db] :as cofx} chat-id remove-chat?]
  (let [{:keys [last-message public?
                deleted-at-clock-value]} (get-in db [:chats chat-id])
        last-message-clock-value (if (and public? remove-chat?)
                                   0
                                   (or (:clock-value last-message)
                                       deleted-at-clock-value
                                       (utils.clocks/send 0)))]
    (fx/merge
     cofx
     {:db            (-> db
                         (assoc-in [:messages chat-id] {})
                         (update-in [:message-lists] dissoc chat-id)
                         (update-in [:chats chat-id] merge
                                    {:last-message              nil
                                     :unviewed-messages-count   0
                                     :deleted-at-clock-value    last-message-clock-value}))}
     (messages-store/delete-messages-by-chat-id chat-id)
     #(chats-store/save-chat % (get-in % [:db :chats chat-id]) nil))))

(fx/defn deactivate-chat
  "Deactivate chat in db, no side effects"
  [{:keys [db now] :as cofx} chat-id]
  {:db (-> db
           (assoc-in [:chats chat-id :is-active] false)
           (assoc-in [:current-chat-id] nil))})

(fx/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  {:events [:chat.ui/remove-chat]}
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge cofx
            (mailserver/remove-gaps chat-id)
            (mailserver/remove-range chat-id)
            (deactivate-chat chat-id)
            (clear-history chat-id true)
            (transport.filters/stop-listening chat-id)
            (when (not (= (:view-id db) :home))
              (navigation/navigate-to-cofx :home {}))))

(fx/defn offload-all-messages
  {:events [::offload-all-messages]}
  [{:keys [db] :as cofx}]
  (when-let [current-chat-id (:current-chat-id db)]
    {:db
     (-> db
         (dissoc :loaded-chat-id)
         (update :messages dissoc current-chat-id)
         (update :message-lists dissoc current-chat-id)
         (update :pagination-info dissoc current-chat-id))}))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [{:keys [db] :as cofx} chat-id]
  (let [old-current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:dispatch [:load-messages]}
              (when-not (= old-current-chat-id chat-id)
                (offload-all-messages))
              (fn [{:keys [db]}]
                {:db (assoc db :current-chat-id chat-id)})
              ;; Group chat don't need this to load as all the loading of topics
              ;; happens on membership changes
              (when-not (or (group-chat? cofx chat-id) (timeline-chat? cofx chat-id))
                (transport.filters/load-chat chat-id)))))

(fx/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-chat]}
  [{db :db :as cofx} chat-id]
  (fx/merge cofx
            {:db (assoc db :inactive-chat-id chat-id)}
            (preload-chat-data chat-id)
            (navigation/navigate-to-cofx :chat-stack {:screen :chat})))

(fx/defn start-chat
  "Start a chat, making sure it exists"
  {:events [:chat.ui/start-chat]}
  [{:keys [db] :as cofx} chat-id]
  ;; don't allow to open chat with yourself
  (when (not= (multiaccounts.model/current-public-key cofx) chat-id)
    (fx/merge cofx
              (upsert-chat {:chat-id   chat-id
                            :is-active true}
                           nil)
              (transport.filters/load-chat chat-id)
              (navigate-to-chat chat-id))))

(def timeline-chat-id "@timeline70bd746ddcc12beb96b2c9d572d0784ab137ffc774f5383e50585a932080b57cca0484b259e61cecbaa33a4c98a300a")

(defn profile-chat-topic [public-key]
  (str "@" public-key))

(fx/defn start-public-chat
  "Starts a new public chat"
  {:events [:chat.ui/start-public-chat]}
  [cofx topic {:keys [dont-navigate? profile-public-key]}]
  (if (or (new-public-chat.db/valid-topic? topic) profile-public-key)
    (if (active-chat? cofx topic)
      (when-not dont-navigate?
        (navigate-to-chat cofx topic))
      (fx/merge cofx
                (add-public-chat topic profile-public-key false)
                (transport.filters/load-chat topic)
                #(when-not dont-navigate?
                   (navigate-to-chat % topic))))
    {:utils/show-popup {:title   (i18n/label :t/cant-open-public-chat)
                        :content (i18n/label :t/invalid-public-chat-topic)}}))

(fx/defn start-profile-chat
  "Starts a new profile chat"
  {:events [:start-profile-chat]}
  [cofx profile-public-key]
  (let [topic (profile-chat-topic profile-public-key)]
    (when-not (active-chat? cofx topic)
      (fx/merge cofx
                (add-public-chat topic profile-public-key false)
                (transport.filters/load-chat topic)))))

(fx/defn start-timeline-chat
  "Starts a new timeline chat"
  [cofx]
  (when-not (active-chat? cofx timeline-chat-id)
    (add-public-chat cofx timeline-chat-id nil true)))

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

(fx/defn show-profile-without-adding-contact
  {:events [:chat.ui/show-profile-without-adding-contact]}
  [{:keys [db] :as cofx} identity]
  (let [my-public-key (get-in db [:multiaccount :public-key])]
    (if (= my-public-key identity)
      (navigation/navigate-to-cofx cofx :profile-stack {:screen :my-profile})
      (fx/merge
       cofx
       {:db (assoc db :contacts/identity identity)}
       (navigation/navigate-to-cofx :profile nil)))))

(fx/defn mute-chat-failed
  {:events [::mute-chat-failed]}
  [{:keys [db] :as cofx} chat-id muted? error]
  (log/error "mute chat failed" chat-id error)
  {:db (assoc-in db [:chats chat-id :muted] (not muted?))})

(fx/defn mute-chat
  {:events [::mute-chat-toggled]}
  [{:keys [db] :as cofx} chat-id muted?]
  (let [method (if muted? "muteChat" "unmuteChat")
        chat (get-in db [:chats chat-id])]
    ;; chat does not exist, create and then mute
    (if-not chat
      (upsert-chat cofx
                   {:is-active true
                    :chat-id chat-id}
                   #(re-frame/dispatch [::mute-chat-toggled chat-id muted?]))
      {:db (assoc-in db [:chats chat-id :muted] muted?)
       ::json-rpc/call [{:method (json-rpc/call-ext-method method)
                         :params [chat-id]
                         :on-error #(re-frame/dispatch [::mute-chat-failed chat-id muted? %])
                         :on-success #(log/info method "successful" chat-id)}]})))

(fx/defn show-profile
  {:events [:chat.ui/show-profile]}
  [cofx identity]
  (fx/merge (assoc-in cofx [:db :contacts/identity] identity)
            (contact.core/create-contact identity)
            (navigation/navigate-to-cofx :profile nil)))

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

(fx/defn chat-ui-fill-gaps
  {:events [:chat.ui/fill-gaps]}
  [{:keys [db] :as cofx} gap-ids]
  (let [chat-id (:current-chat-id db)
        topics (mailserver.topics/topics-for-current-chat db)
        gaps (keep
              (fn [id]
                (get-in db [:mailserver/gaps chat-id id]))
              gap-ids)]
    (mailserver/fill-the-gap
     cofx
     {:gaps    gaps
      :topics  topics
      :chat-id chat-id})))

(fx/defn chat-ui-fetch-more
  {:events [:chat.ui/fetch-more]}
  [{:keys [db] :as cofx}]
  (let [chat-id (:current-chat-id db)

        {:keys [lowest-request-from]}
        (get-in db [:mailserver/ranges chat-id])

        topics (mailserver.topics/topics-for-current-chat db)
        gaps [{:id   :first-gap
               :to   lowest-request-from
               :from (- lowest-request-from mailserver.constants/one-day)}]]
    (mailserver/fill-the-gap
     cofx
     {:gaps    gaps
      :topics  topics
      :chat-id chat-id})))

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