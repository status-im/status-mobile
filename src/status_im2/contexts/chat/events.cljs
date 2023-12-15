(ns status-im2.contexts.chat.events
  (:require
    [clojure.set :as set]
    [quo.foundations.colors :as colors]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.chat.models.loading :as loading]
    [status-im.data-store.chats :as chats-store]
    [status-im2.common.muting.helpers :refer [format-mute-till]]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.composer.link-preview.events :as link-preview]
    status-im2.contexts.chat.effects
    status-im2.contexts.chat.lightbox.events
    status-im2.contexts.chat.messages.content.reactions.events
    [status-im2.contexts.chat.messages.delete-message-for-me.events :as delete-for-me]
    [status-im2.contexts.chat.messages.delete-message.events :as delete-message]
    [status-im2.contexts.chat.messages.list.state :as chat.state]
    [status-im2.contexts.contacts.events :as contacts-store]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

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
  (fn [chat]
    (let [base-chat (or (get (:chats db) (:chat-id chat))
                        (create-new-chat (:chat-id chat) cofx))]
      (assoc (merge
              (cond-> base-chat
                (comp not :muted) (dissoc base-chat :muted-till))
              chat)
             :invitation-admin
             (:invitation-admin chat)))))

(rf/defn leave-removed-chat
  {:events [:chat/leave-removed-chat]}
  [{{:keys [view-id current-chat-id chats]} :db
    :as                                     cofx}]
  (when (and (= view-id :chat)
             (not (contains? chats current-chat-id)))
    (navigation/navigate-back cofx)))

(defn ensure-chats
  [{:keys [db] :as cofx} [chats]]
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
    {:db (-> db
             (update :chats merge all-chats)
             (update :chats-home-list set/union chats-home-list)
             (update :chats #(apply dissoc % removed-chats))
             (update :chats-home-list set/difference removed-chats))
     :fx [(when (not-empty removed-chats)
            [:effects/push-notifications-clear-message-notifications removed-chats])
          [:dispatch [:chat/leave-removed-chat]]]}))

(re-frame/reg-event-fx :chat/ensure-chats ensure-chats)

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
                                       (datetime/timestamp)))]
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
                     :params     [{:id chat-id :preserveHistory true}]
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
  [{:keys [db] :as cofx}]
  (when-let [chat-id (:current-chat-id db)]
    (chat.state/reset-visible-item)
    (rf/merge cofx
              (merge
               {:db                        (-> db
                                               (dissoc :current-chat-id)
                                               (assoc-in [:chat/inputs chat-id :focused?] false))
                :effects.async-storage/set {:chat-id nil
                                            :key-uid nil}})
              (link-preview/reset-all)
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
  [{db :db :as cofx} chat-id animation]
  (rf/merge cofx
            {:dispatch [(if animation :shell/navigate-to :navigate-to) :chat chat-id animation]}
            (close-chat)
            (force-close-chat chat-id)
            (fn [{:keys [db]}]
              {:db (assoc db :current-chat-id chat-id)})
            (preload-chat-data chat-id)
            #(when (group-chat? cofx chat-id)
               (loading/load-chat % chat-id))))

(rf/defn pop-to-root-and-navigate-to-chat
  {:events [:chat/pop-to-root-and-navigate-to-chat]}
  [cofx chat-id animation]
  (rf/merge
   cofx
   (navigation/pop-to-root :shell-stack)
   (navigate-to-chat chat-id animation)))

(rf/defn handle-clear-history-response
  {:events [:chat/history-cleared]}
  [{:keys [db]} chat-id response]
  (let [chat (chats-store/<-rpc (first (:chats response)))]
    {:db (assoc-in db [:chats chat-id] chat)}))

(rf/defn handle-one-to-one-chat-created
  {:events [:chat/one-to-one-chat-created]}
  [{:keys [db]} chat-id response-js]
  (let [chat       (chats-store/<-rpc (first (transforms/js->clj (.-chats ^js response-js))))
        contact-js (first (.-contacts ^js response-js))
        contact    (when contact-js (contacts-store/<-rpc-js contact-js))]
    {:db       (cond-> db
                 contact
                 (assoc-in [:contacts/contacts chat-id] contact)
                 :always
                 (assoc-in [:chats chat-id] chat)
                 :always
                 (update :chats-home-list conj chat-id))
     :dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id]}))

(rf/defn decrease-unviewed-count
  {:events [:chat/decrease-unviewed-count]}
  [{:keys [db]} chat-id {:keys [count countWithMentions]}]
  {:db (-> db
           ;; There might be some other requests being fired, so we need to make sure the count has
           ;; not been set to
           ;; 0 in the meantime
           (update-in [:chats chat-id :unviewed-messages-count]
                      #(max (- % count) 0))
           (update-in [:chats chat-id :unviewed-mentions-count]
                      #(max (- % countWithMentions) 0)))})

(rf/defn start-chat
  "Start a chat, making sure it exists"
  {:events [:chat.ui/start-chat]}
  [cofx chat-id ens-name]
  (when (not= (get-in cofx [:db :profile/profile :public-key]) chat-id)
    {:json-rpc/call [{:method      "wakuext_createOneToOneChat"
                      :params      [{:id chat-id :ensName ens-name}]
                      :js-response true
                      :on-success  #(rf/dispatch [:chat/one-to-one-chat-created chat-id %])
                      :on-error    #(log/error "failed to create one-to-on chat" chat-id %)}]}))

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

(rf/defn close-and-remove-chat
  "Closes the chat and removes it from chat list while retaining history, producing all necessary effects for that"
  {:events [:chat.ui/close-chat]}
  [{:keys [db now] :as cofx} chat-id]
  (rf/merge cofx
            {:effects/push-notifications-clear-message-notifications [chat-id]
             :dispatch                                               [:shell/close-switcher-card
                                                                      chat-id]}
            (deactivate-chat chat-id)
            (offload-messages chat-id)))

(rf/defn unmute-chat-community
  {:events [:chat/unmute-chat-community]}
  [{:keys [db]} chat-id muted?]
  (let [{:keys [community-id]} (get-in db [:chats chat-id])]
    {:db (assoc-in db [:communities community-id :muted] muted?)}))

(rf/defn mute-chat-failed
  {:events [:chat/mute-failed]}
  [{:keys [db]} chat-id muted? error]
  (log/error "mute chat failed" chat-id error)
  {:db (assoc-in db [:chats chat-id :muted] (not muted?))})

(rf/defn mute-chat-toggled-successfully
  {:events [:chat/mute-successfully]}
  [{:keys [db]} chat-id muted-till mute-type muted? chat-type]
  (log/debug "muted chat successfully" chat-id " for" muted-till)
  (when-not muted?
    (rf/dispatch [:chat/unmute-chat-community chat-id muted?]))
  (let [time-string (fn [duration-kw unmute-time]
                      (i18n/label duration-kw {:duration unmute-time}))
        not-community-chat? #(contains? #{constants/public-chat-type
                                          constants/private-group-chat-type
                                          constants/one-to-one-chat-type}
                                        %)
        mute-duration-text
        (fn [unmute-time]
          (if unmute-time
            (str
             (condp = mute-type
               constants/mute-for-15-mins-type (time-string
                                                (if (not-community-chat? chat-type)
                                                  :t/chat-muted-for-15-minutes
                                                  :t/channel-muted-for-15-minutes)
                                                unmute-time)
               constants/mute-for-1-hour-type  (time-string
                                                (if (not-community-chat? chat-type)
                                                  :t/chat-muted-for-1-hour
                                                  :t/channel-muted-for-1-hour)
                                                unmute-time)
               constants/mute-for-8-hours-type (time-string
                                                (if (not-community-chat? chat-type)
                                                  :t/chat-muted-for-8-hours
                                                  :t/channel-muted-for-8-hours)
                                                unmute-time)
               constants/mute-for-1-week       (time-string
                                                (if (not-community-chat? chat-type)
                                                  :t/chat-muted-for-1-week
                                                  :t/channel-muted-for-1-week)
                                                unmute-time)
               constants/mute-till-unmuted     (time-string
                                                (if (not-community-chat? chat-type)
                                                  :t/chat-muted-till-unmuted
                                                  :t/channel-muted-till-unmuted)
                                                unmute-time)))
            (i18n/label (if (not-community-chat? chat-type)
                          :t/chat-unmuted-successfully
                          :t/channel-unmuted-successfully))))]
    {:db       (assoc-in db [:chats chat-id :muted-till] muted-till)
     :dispatch [:toasts/upsert
                {:icon       :i/correct
                 :icon-color (colors/theme-colors colors/success-60
                                                  colors/success-50)
                 :text       (mute-duration-text (when (some? muted-till)
                                                   (str (format-mute-till muted-till))))}]}))

(rf/defn mute-chat
  {:events [:chat.ui/mute]}
  [{:keys [db]} chat-id muted? mute-type]
  (let [method    (if muted? "wakuext_muteChatV2" "wakuext_unmuteChat")
        params    (if muted? [{:chatId chat-id :mutedType mute-type}] [chat-id])
        chat-type (get-in db [:chats chat-id :chat-type])]
    {:db            (assoc-in db [:chats chat-id :muted] muted?)
     :json-rpc/call [{:method     method
                      :params     params
                      :on-error   #(rf/dispatch [:chat/mute-failed chat-id muted? %])
                      :on-success #(rf/dispatch [:chat/mute-successfully chat-id % mute-type
                                                 muted? chat-type])}]}))

(rf/defn show-clear-history-confirmation
  {:events [:chat.ui/show-clear-history-confirmation]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/clear-history-title)
    :content             (i18n/label :t/clear-history-confirmation-content)
    :confirm-button-text (i18n/label :t/clear-history-action)
    :on-accept           #(do
                            (rf/dispatch [:hide-bottom-sheet])
                            (rf/dispatch [:chat.ui/clear-history chat-id false]))}})

(rf/defn show-remove-chat-confirmation
  {:events [:chat.ui/show-close-confirmation]}
  [_ chat-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-confirmation)
    :content             (i18n/label :t/close-chat-confirmation)
    :confirm-button-text (i18n/label :t/delete)
    :on-accept           #(do
                            (rf/dispatch [:hide-bottom-sheet])
                            (rf/dispatch [:chat.ui/close-chat chat-id]))}})

(rf/defn navigate-to-user-pinned-messages
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  {:events [:chat.ui/navigate-to-pinned-messages]}
  [cofx chat-id]
  (navigation/navigate-to cofx :chat-pinned-messages {:chat-id chat-id}))


(rf/defn update-shared-element-id
  {:events [:chat.ui/update-shared-element-id]}
  [{:keys [db]} shared-element-id]
  {:db (assoc db :shared-element-id shared-element-id)})

(rf/defn navigate-to-lightbox
  {:events [:chat.ui/navigate-to-lightbox]}
  [{:keys [db]} shared-element-id screen-params]
  (reagent/next-tick #(rf/dispatch [:navigate-to :lightbox screen-params]))
  {:db (assoc db :shared-element-id shared-element-id)})

(rf/defn exit-lightbox-signal
  {:events [:chat.ui/exit-lightbox-signal]}
  [{:keys [db]} value]
  {:db (assoc db :lightbox/exit-signal value)})

(rf/defn zoom-out-signal
  {:events [:chat.ui/zoom-out-signal]}
  [{:keys [db]} value]
  {:db (assoc db :lightbox/zoom-out-signal value)})

(rf/defn orientation-change
  {:events [:chat.ui/orientation-change]}
  [{:keys [db]} value]
  {:db (assoc db :lightbox/orientation value)})

(rf/defn lightbox-scale
  {:events [:chat.ui/lightbox-scale]}
  [{:keys [db]} value]
  {:db (assoc db :lightbox/scale value)})

(rf/defn check-last-chat
  {:events [:chat/check-last-chat]}
  [{:keys [db]}]
  {:effects.chat/open-last-chat (get-in db [:profile/profile :key-uid])})
