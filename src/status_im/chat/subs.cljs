(ns status-im.chat.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.db :as chat.db]
            [status-im.models.transactions :as transactions]
            [status-im.utils.platform :as platform]))

(re-frame/reg-sub
 ::chats
 (fn [db]
   (get db :chats)))

(re-frame/reg-sub
 ::current-chat-id
 (fn [db]
   (get db :current-chat-id)))

(re-frame/reg-sub
 ::access-scope->command-id
 (fn [db]
   (get db :access-scope->command-id)))

(re-frame/reg-sub
 ::chat-ui-props
 (fn [db]
   (get db :chat-ui-props)))

(re-frame/reg-sub
 ::current-chat-ui-props
 :<- [::chat-ui-props]
 :<- [::current-chat-id]
 (fn [[chat-ui-props id]]
   (get chat-ui-props id)))

(re-frame/reg-sub
 ::current-chat
 :<- [::current-chat-id]
 :<- [::chats]
 (fn [[chat-id chats]]
   (get chats chat-id)))

(re-frame/reg-sub
 ::current-chat-messages
 :<- [::current-chat]
 (fn [chat]
   (get chat :messages)))

(re-frame/reg-sub
 ::get-commands-for-chat
 :<- [:chats/id->command]
 :<- [::get-access-scope->command-id]
 :<- [:chats/current]
 (fn [[id->command access-scope->command-id chat]]
   (commands/chat-commands id->command access-scope->command-id chat)))

(re-frame/reg-sub
 ::show-suggestions-view?
 :<- [:chats/current-chat-ui-prop :show-suggestions?]
 :<- [:chats/current-input-text]
 :<- [:chats/all-available-commands]
 (fn [[show-suggestions? input-text commands]]
   (and (or show-suggestions?
            (commands.input/starts-as-command? (string/trim (or input-text ""))))
        (seq commands))))

(re-frame/reg-sub
 ::show-suggestions?
 :<- [::show-suggestions-view?]
 :<- [:chats/selected-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not selected-command))))

(re-frame/reg-sub
 ::cooldown-enabled?
 (fn [db]
   (:chats/cooldown-enabled? db)))

(re-frame/reg-sub
 ::chats-input-text
 (fn [db]
   (get db :chats.ui/input-text)))

(re-frame/reg-sub
 :chats/current-input-text
 :<- [::chats-input-text]
 :<- [::current-chat-id]
 (fn [[chats-input-text chat-id]]
   (get chats-input-text chat-id)))

(re-frame/reg-sub :chats/id->command :id->command)

(re-frame/reg-sub
 :chats/contact
 :<- [:chats/current]
 (fn [chat]
   (:contact chat)))

(re-frame/reg-sub
 :chats/current-chat-ui-prop
 :<- [::current-chat-ui-props]
 (fn [ui-props [_ prop]]
   (get ui-props prop)))

(re-frame/reg-sub
 :chats/validation-messages
 :<- [::current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :validation-messages)))

(re-frame/reg-sub
 :chats/input-margin
 :<- [:get :keyboard-height]
 (fn [kb-height]
   (cond
     (and platform/iphone-x? (> kb-height 0)) (- kb-height 34)
     platform/ios? kb-height
     :default 0)))

(re-frame/reg-sub
 :chats/active-chats
 :<- [::chats]
 :<- [:contacts/contacts]
 :<- [:account/account]
 :<- [:network-name]
 (fn [[chats contacts account network]]
   (chat.db/active-chats chats contacts account network)))

(re-frame/reg-sub
 :chats/current
 :<- [:chats/active-chats]
 :<- [::current-chat-id]
 (fn [[chats current-chat-id]]
   (get chats current-chat-id)))

(re-frame/reg-sub
 :chats/available-commands
 :<- [::get-commands-for-chat]
 :<- [:chats/current]
 (fn [[commands chat]]
   (chat.db/available-commands commands chat)))

(re-frame/reg-sub
 :chats/all-available-commands
 :<- [::get-commands-for-chat]
 (fn [commands]
   (chat.db/map->sorted-seq commands)))

(re-frame/reg-sub
 :chats/selected-command
 :<- [:chats/current-input-text]
 :<- [:chats/current-chat-ui-prop :selection]
 :<- [::get-commands-for-chat]
 (fn [[input-text selection commands]]
   (commands.input/selected-chat-command input-text selection commands)))

(re-frame/reg-sub
 :chats/input-placeholder
 :<- [:chats/current-input-text]
 :<- [:chats/selected-chat-command]
 (fn [[input-text {:keys [params current-param-position]}]]
   (when (string/ends-with? (or input-text "") chat.constants/spacing-char)
     (get-in params [current-param-position :placeholder]))))

(re-frame/reg-sub
 :chats/parameter-box
 :<- [:chats/current]
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
 :chats/unread-messages-number
 :<- [:chats/active-chats]
 (fn [chats]
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
 :<- [:chats/current]
 :<- [::cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(re-frame/reg-sub
 :chats/reply-message
 :<- [:chats/current]
 :<- [::current-chat-messages]
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[{:keys [metadata referenced-messages]} messages contacts account]]
   (when-let [message-id (:responding-to-message metadata)]
     (chat.db/add-response-metadata message-id
                                    messages
                                    referenced-messages
                                    contacts
                                    account))))

(re-frame/reg-sub
 :chats/message-details
 :<- [:chats/current]
 :<- [:account/account]
 ::current-chat-ui-props
 (fn [[{:keys [group-chat] :as chat} {:keys [public-key]} {:keys [message-id]}]]
   (when (and group-chat message-id)
     (chat.db/message-details chat public-key message-id))))
