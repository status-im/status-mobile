(ns status-im.chat.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.db :as chat.db]
            [status-im.models.transactions :as transactions]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as links]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.screens.chat.stickers.styles :as stickers.styles]))

(re-frame/reg-sub ::chats :chats)
(re-frame/reg-sub ::access-scope->command-id :access-scope->command-id)
(re-frame/reg-sub ::chat-ui-props :chat-ui-props)

(re-frame/reg-sub
 ::cooldown-enabled?
 (fn [db]
   (:chat/cooldown-enabled? db)))

(re-frame/reg-sub
 ::show-suggestions?
 :<- [::show-suggestions-view?]
 :<- [:chats/selected-chat-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not selected-command))))

(re-frame/reg-sub
 ::show-suggestions-view?
 :<- [:chats/current-chat-ui-prop :show-suggestions?]
 :<- [:chats/current-chat]
 :<- [:chats/all-available-commands]
 (fn [[show-suggestions? {:keys [input-text]} commands]]
   (and (or show-suggestions?
            (commands.input/starts-as-command? (string/trim (or input-text ""))))
        (seq commands))))

(re-frame/reg-sub
 ::get-commands-for-chat
 :<- [:chats/id->command]
 :<- [::access-scope->command-id]
 :<- [:chats/current-chat]
 (fn [[id->command access-scope->command-id chat]]
   (commands/chat-commands id->command access-scope->command-id chat)))

(re-frame/reg-sub :chats/id->command :id->command)
(re-frame/reg-sub :chats/current-chat-id :current-chat-id)

(re-frame/reg-sub
 :chats/chat
 :<- [:chats/active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
 :chats/content-layout-height
 :<- [:get :content-layout-height]
 :<- [:chats/current-chat-ui-prop :input-height]
 :<- [:chats/current-chat-ui-prop :input-focused?]
 :<- [:get :keyboard-height]
 :<- [:chats/current-chat-ui-prop :show-stickers?]
 (fn [[home-content-layout-height input-height input-focused? kheight stickers?]]
   (- (+ home-content-layout-height tabs.styles/tabs-height)
      (if platform/iphone-x?
        (* 2 toolbar.styles/toolbar-height)
        toolbar.styles/toolbar-height)
      (if input-height input-height 0)
      (if stickers?
        (stickers.styles/stickers-panel-height)
        kheight)
      (if input-focused?
        (cond
          platform/iphone-x? 0
          platform/ios?      tabs.styles/tabs-diff
          :else              0)
        (cond
          platform/iphone-x? (* 2 tabs.styles/minimized-tabs-height)
          platform/ios?      tabs.styles/tabs-height
          :else              tabs.styles/minimized-tabs-height)))))

(re-frame/reg-sub
 :chats/current-chat-ui-props
 :<- [::chat-ui-props]
 :<- [:chats/current-chat-id]
 (fn [[chat-ui-props id]]
   (get chat-ui-props id)))

(re-frame/reg-sub
 :chats/current-chat-ui-prop
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props [_ prop]]
   (get ui-props prop)))

(re-frame/reg-sub
 :chats/validation-messages
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :validation-messages)))

(re-frame/reg-sub
 :chats/input-margin
 :<- [:get :keyboard-height]
 (fn [kb-height]
   (cond
     (and platform/iphone-x? (> kb-height 0))
     (- kb-height (* 2 tabs.styles/minimized-tabs-height))

     platform/ios?
     (+ kb-height (- (if (> kb-height 0)
                       tabs.styles/minimized-tabs-height
                       0)))

     :default 0)))

(re-frame/reg-sub
 :chats/active-chats
 :<- [:contacts/contacts]
 :<- [::chats]
 :<- [:account/account]
 (fn [[contacts chats account]]
   (chat.db/active-chats contacts chats account)))

(re-frame/reg-sub
 :chats/current-chat
 :<- [:chats/active-chats]
 :<- [:chats/current-chat-id]
 (fn [[chats current-chat-id]]
   (let [current-chat (get chats current-chat-id)
         messages     (:messages current-chat)]
     (if (empty? messages)
       (assoc current-chat
              :universal-link
              (links/generate-link :public-chat :external current-chat-id))
       current-chat))))

(re-frame/reg-sub
 :chats/current-chat-message
 :<- [:chats/current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(re-frame/reg-sub
 :chats/current-chat-messages
 :<- [:chats/current-chat]
 (fn [{:keys [messages]}]
   (or messages {})))

(re-frame/reg-sub
 :chats/current-chat-message-groups
 :<- [:chats/current-chat]
 (fn [{:keys [message-groups]}]
   (or message-groups {})))

(re-frame/reg-sub
 :chats/current-chat-message-statuses
 :<- [:chats/current-chat]
 (fn [{:keys [message-statuses]}]
   (or message-statuses {})))

(re-frame/reg-sub
 :chats/current-chat-referenced-messages
 :<- [:chats/current-chat]
 (fn [{:keys [referenced-messages]}]
   (or referenced-messages {})))

(re-frame/reg-sub
 :chats/current-chat-topic
 (fn [db]
   (chat.db/topic-by-current-chat db)))

(re-frame/reg-sub
 :chats/messages-gaps
 :<- [:get-in [:mailserver/gaps]]
 :<- [:chats/current-chat-id]
 (fn [[gaps chat-id]]
   (sort-by :from (vals (get gaps chat-id)))))

(re-frame/reg-sub
 :chats/current-chat-messages-stream
 :<- [:chats/current-chat-messages]
 :<- [:chats/current-chat-message-groups]
 :<- [:chats/current-chat-message-statuses]
 :<- [:chats/current-chat-referenced-messages]
 :<- [:chats/messages-gaps]
 (fn [[messages message-groups message-statuses referenced-messages messages-gaps]]
   (-> (chat.db/sort-message-groups message-groups messages)
       (chat.db/messages-with-datemarks-and-statuses messages message-statuses referenced-messages messages-gaps)
       chat.db/messages-stream)))

(re-frame/reg-sub
 :chats/fetching-gap-in-progress?
 (fn [db [_ ids]]
   (let [chat-id (:current-chat-id db)
         gaps (:mailserver/fetching-gaps-in-progress db)]
     (seq (select-keys (get gaps chat-id) ids)))))

(re-frame/reg-sub
 :chats/current-chat-intro-status
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-messages]
 (fn [[{:keys [might-have-join-time-messages?]} messages]]
   (if might-have-join-time-messages?
     :loading
     (if (empty? messages)
       :empty
       :messages))))

(re-frame/reg-sub
 :chats/available-commands
 :<- [::get-commands-for-chat]
 :<- [:chats/current-chat]
 (fn [[commands chat]]
   (chat.db/available-commands commands chat)))

(re-frame/reg-sub
 :chats/all-available-commands
 :<- [::get-commands-for-chat]
 (fn [commands]
   (chat.db/map->sorted-seq commands)))

(re-frame/reg-sub
 :chats/selected-chat-command
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-ui-prop :selection]
 :<- [::get-commands-for-chat]
 (fn [[{:keys [input-text]} selection commands]]
   (commands.input/selected-chat-command input-text selection commands)))

(re-frame/reg-sub
 :chats/input-placeholder
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[{:keys [input-text]} {:keys [params current-param-position cursor-in-the-end?]}]]
   (when (and cursor-in-the-end? (string/ends-with? (or input-text "") chat.constants/spacing-char))
     (get-in params [current-param-position :placeholder]))))

(re-frame/reg-sub
 :chats/parameter-box
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[_ {:keys [current-param-position params]}]]
   (when (and params current-param-position)
     (get-in params [current-param-position :suggestions]))))

(re-frame/reg-sub
 :chats/show-parameter-box?
 :<- [:chats/parameter-box]
 :<- [::show-suggestions?]
 :<- [:chats/validation-messages]
 :<- [:chats/selected-chat-command]
 (fn [[chat-parameter-box show-suggestions? validation-messages {:keys [command-completion]}]]
   (and chat-parameter-box
        (not validation-messages)
        (not show-suggestions?)
        (not (= :complete command-completion)))))

(re-frame/reg-sub
 :chats/unviewed-messages-count
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [unviewed-messages-count]}]
   unviewed-messages-count))

(re-frame/reg-sub
 :chats/photo-path
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[contacts account] [_ id]]
   (or (:photo-path (contacts id))
       (when (= id (:public-key account))
         (:photo-path account)))))

(re-frame/reg-sub
 :chats/unread-messages-number
 :<- [:chats/active-chats]
 (fn [chats _]
   (apply + (map :unviewed-messages-count (vals chats)))))

(re-frame/reg-sub
 :chats/transaction-confirmed?
 (fn [db [_ tx-hash]]
   (-> (get-in db [:wallet :transactions tx-hash :confirmations] "0")
       (js/parseInt)
       (>= transactions/confirmations-count-threshold))))

(re-frame/reg-sub
 :chats/wallet-transaction-exists?
 (fn [db [_ tx-hash]]
   (not (nil? (get-in db [:wallet :transactions tx-hash])))))

(re-frame/reg-sub
 :chats/cooldown-enabled?
 :<- [:chats/current-chat]
 :<- [::cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(re-frame/reg-sub
 :chats/reply-message
 :<- [:chats/current-chat]
 (fn [{:keys [metadata messages]}]
   (get messages (get-in metadata [:responding-to-message :message-id]))))
