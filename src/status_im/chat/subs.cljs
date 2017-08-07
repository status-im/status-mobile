(ns status-im.chat.subs
  (:require [re-frame.core :refer [reg-sub dispatch subscribe path]]
            [status-im.data-store.chats :as chats]
            [status-im.chat.constants :as const]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.constants :refer [response-suggesstion-resize-duration
                                         content-type-status
                                         console-chat-id]]
            [status-im.commands.utils :as commands-utils]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(reg-sub
  :chat-ui-props
  (fn [db [_ ui-element chat-id]]
    (let [current-chat-id (subscribe [:get-current-chat-id])
          data (get-in db [:chat-ui-props (or chat-id @current-chat-id) ui-element])]
      (cond-> data
        (:markup data)
        (update :markup commands-utils/generate-hiccup)

        (and  (= ui-element :validation-messages) data)
        commands-utils/generate-hiccup))))

(reg-sub
  :chat-input-margin
  :<- [:get :keyboard-height]
  (fn [kb-height]
    (if ios? kb-height 0)))

(reg-sub
  :chats
  (fn [db]
    (:chats db)))

(reg-sub
  :chat-actions
  :<- [:chats]
  :<- [:get-current-chat-id]
  :<- [:chat :input-text]
  (fn [[chats current-chat-id text] [_ type chat-id]]
    (->> (get-in chats [(or chat-id current-chat-id) type])
         (filter #(or (str/includes? (chat-utils/command-name %) (or text "")))))))

(reg-sub
  :chat
  :<- [:chats]
  :<- [:get-current-chat-id]
  (fn [[chats id] [_ k chat-id]]
    (get-in chats [(or chat-id id) k])))

(reg-sub
  :get-current-chat-id
  (fn [db]
    (:current-chat-id db)))

(reg-sub
  :get-chat-by-id
  (fn [_ [_ chat-id]]
    (chats/get-by-id chat-id)))

(reg-sub :get-commands-and-responses
  (fn [{:keys [chats global-commands] :contacts/keys [contacts]} [_ chat-id]]
    (->> (get-in chats [chat-id :contacts])
         (filter :is-in-chat)
         (mapv (fn [{:keys [identity]}]
                 (let [{:keys [commands responses]} (get contacts identity)]
                   (merge responses commands))))
         (apply merge)
         (merge global-commands))))

(reg-sub
  :selected-chat-command
  (fn [db [_ chat-id]]
    (let [current-chat-id (subscribe [:get :current-chat-id])
          input-text      (subscribe [:chat :input-text])]
      (input-model/selected-chat-command db (or chat-id @current-chat-id) @input-text))))

(reg-sub
  :current-chat-argument-position
  (fn [db]
    (let [command       (subscribe [:selected-chat-command])
          input-text    (subscribe [:chat :input-text])
          seq-arguments (subscribe [:chat :seq-arguments])
          selection     (subscribe [:chat-ui-props :selection])]
      (input-model/current-chat-argument-position @command @input-text @selection @seq-arguments))))

(reg-sub
  :chat-parameter-box
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])
          command (subscribe [:selected-chat-command])
          index   (subscribe [:current-chat-argument-position])]
      (cond
        (and @command (not= @index input-model/*no-argument-error*))
        (let [command-name (get-in @command [:command :name])]
          (get-in db [:chats @chat-id :parameter-boxes command-name @index]))

        (not @command)
        (get-in db [:chats @chat-id :parameter-boxes :message])

        :default
        nil))))

(reg-sub
 :show-parameter-box?
  :<- [:chat-parameter-box]
  :<- [:show-suggestions?]
  :<- [:chat :input-text]
  :<- [:chat-ui-props :validation-messages]
  (fn [[chat-parameter-box show-suggestions? input-text validation-messages]]
    (and (get chat-parameter-box :markup)
         (not validation-messages)
         (not show-suggestions?))))

(reg-sub
  :command-completion
  (fn [db [_ chat-id]]
    (input-model/command-completion db chat-id)))

(reg-sub
  :show-suggestions?
  (fn [db [_ chat-id]]
    (let [chat-id           (or chat-id (db :current-chat-id))
          show-suggestions? (subscribe [:chat-ui-props :show-suggestions? chat-id])
          input-text        (subscribe [:chat :input-text chat-id])
          selected-command  (subscribe [:selected-chat-command chat-id])
          requests          (subscribe [:chat :possible-requests chat-id])
          commands          (subscribe [:chat :possible-commands chat-id])]
      (and (or @show-suggestions? (input-model/starts-as-command? (str/trim (or @input-text ""))))
           (not (:command @selected-command))
           (or (not-empty @requests)
               (not-empty @commands))))))

(reg-sub :get-current-chat
  (fn [db]
    (let [current-chat-id (:current-chat-id db)]
      (get-in db [:chats current-chat-id]))))

(reg-sub :get-chat
  (fn [db [_ chat-id]]
    (get-in db [:chats chat-id])))

(reg-sub :get-response
  (fn [db [_ n]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:contacts/contacts @chat-id :responses n]))))

(reg-sub :is-request-answered?
  :<- [:chat :requests]
  (fn [requests [_ message-id]]
    (not-any? #(= message-id (:message-id %)) requests)))

(reg-sub :unviewed-messages-count
  (fn [db [_ chat-id]]
    (get-in db [:unviewed-messages chat-id :count])))

(reg-sub :web-view-extra-js
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:web-view-extra-js @chat-id]))))

(reg-sub :all-messages-loaded?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:chats @chat-id :all-loaded?]))))

(reg-sub :photo-path
  :<- [:get-contacts]
  (fn [contacts [_ id]]
    (:photo-path (contacts id))))

(reg-sub :get-last-message
  (fn [db [_ chat-id]]
    (let [{:keys [last-message messages]} (get-in db [:chats chat-id])]
      (->> (conj messages last-message)
        (sort-by :clock-value >)
        (filter :show?)
        (first)))))

(reg-sub :get-message-short-preview-markup
  (fn [db [_ message-id]]
    (get-in db [:message-data :short-preview message-id :markup])))

(reg-sub :get-last-message-short-preview
  (fn [db [_ chat-id]]
    (let [last-message (subscribe [:get-last-message chat-id])
          preview (subscribe [:get-message-short-preview-markup (:message-id @last-message)])]
      (when-let [markup @preview]
        (commands-utils/generate-hiccup markup)))))

(reg-sub :get-default-container-area-height
  :<- [:chat-ui-props :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/default-container-area-height bottom layout-height))))

(reg-sub :get-max-container-area-height
  :<- [:chat-ui-props :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/max-container-area-height bottom layout-height))))

(reg-sub :chat-animations
  (fn [db [_ key type]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:chat-animations @chat-id key type]))))

(reg-sub :get-chat-last-outgoing-message
  (fn [db [_ chat-id]]
    (->> (:messages (get-in db [:chats chat-id]))
         (filter :outgoing)
         (sort-by :clock-value >)
         (first))))

(reg-sub :get-message-preview-markup
  (fn [db [_ message-id]]
    (get-in db [:message-data :preview message-id :markup])))

(reg-sub :get-message-preview
  (fn [db [_ message-id]]
    (let [preview (subscribe [:get-message-preview-markup message-id])]
      (when-let [markup @preview]
        (commands-utils/generate-hiccup markup)))))
