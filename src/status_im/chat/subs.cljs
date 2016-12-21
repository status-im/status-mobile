(ns status-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub dispatch subscribe path]]
            [status-im.utils.platform :refer [ios?]]
            [status-im.models.commands :as commands]
            [status-im.data-store.chats :as chats]
            [status-im.constants :refer [response-suggesstion-resize-duration]]
            [status-im.chat.constants :as c]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.constants :refer [content-type-status]]
            [status-im.utils.platform :refer [platform-specific]]))

(register-sub :chat-properties
  (fn [db [_ properties]]
    (->> properties
         (map (fn [k]
                [k (-> @db
                       (get-in [:chats (:current-chat-id @db) k])
                       (reaction))]))
         (into {}))))

(register-sub
  :chat-ui-props
  (fn [db [_ ui-element]]
    (reaction (get-in @db [:chat-ui-props ui-element]))))

(register-sub :chat
  (fn [db [_ k]]
    (-> @db
        (get-in [:chats (:current-chat-id @db) k])
        (reaction))))

(register-sub :get-current-chat-id
  (fn [db _]
    (reaction (:current-chat-id @db))))

(register-sub :get-suggestions
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:command-suggestions @chat-id])))))

(register-sub :get-commands
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :commands]) {})))))

(register-sub :get-chat-by-id
  (fn [_ [_ chat-id]]
    (reaction (chats/get-by-id chat-id))))

(register-sub :get-responses
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :responses]) {})))))

(register-sub :get-commands-and-responses
  (fn [db [_ chat-id]]
    (reaction (or (->> (get-in @db [:chats chat-id])
                       ((juxt :commands :responses))
                       (apply merge))
                  (->> (get @db :all-commands)
                       ((juxt :commands :responses))
                       (apply merge))))))

(register-sub :get-chat-input-text
  (fn [db _]
    (->> [:chats (:current-chat-id @db) :input-text]
         (get-in @db)
         (reaction))))

(register-sub :get-chat-staged-commands
  (fn [db _]
    (->> [:chats (:current-chat-id @db) :staged-commands]
         (get-in @db)
         vals
         (reaction))))

(register-sub :get-chat-staged-commands-ids
  (fn [db _]
    (->> [:chats (:current-chat-id @db) :staged-commands]
         (get-in @db)
         vals
         (keep :to-message)
         (reaction))))

(register-sub :staged-response?
  (fn [_ [_ id]]
    (let [commands (subscribe [:get-chat-staged-commands])]
      (reaction (some #(= id (:to-message %)) @commands)))))

(register-sub :get-message-input-view-height
  (fn [db _]
    (reaction (get-in @db [:chats (:current-chat-id @db) :message-input-height]))))

(register-sub :valid-plain-message?
  (fn [_ _]
    (let [input-message   (subscribe [:get-chat-input-text])
          staged-commands (subscribe [:get-chat-staged-commands])]
      (reaction
        (plain-message/message-valid? @staged-commands @input-message)))))

(register-sub :valid-command?
  (fn [_ [_ validator]]
    (let [input (subscribe [:get-chat-command-content])]
      (reaction (command/valid? @input validator)))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (commands/get-chat-command @db))))

(register-sub :get-command-parameter
  (fn [db]
    (let [command (subscribe [:get-chat-command])
          chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (let [parameter-index (commands/get-command-parameter-index @db @chat-id)]
          (when parameter-index (nth (:params @command) parameter-index)))))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (commands/get-chat-command-content @db))))

(register-sub :get-chat-command-to-message-id
  (fn [db _]
    (reaction (commands/get-chat-command-to-message-id @db))))

(register-sub :chat-command-request
  (fn [db _]
    (reaction (commands/get-chat-command-request @db))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (:current-chat-id @db)]
      (reaction (get-in @db [:chats current-chat-id])))))

(register-sub :get-chat
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:chats chat-id]))))

(register-sub :get-content-suggestions
  (fn [db _]
    (reaction (get-in @db [:suggestions (:current-chat-id @db)]))))

(register-sub :command?
  (fn [db]
    (->> (get-in @db [:edit-mode (:current-chat-id @db)])
         (= :command)
         (reaction))))

(register-sub :command-type
  (fn []
    (let [command (subscribe [:get-chat-command])]
      (reaction (:type @command)))))

(register-sub :messages-offset
  (fn []
    (let [command?            (subscribe [:command?])
          type                (subscribe [:command-type])
          command-suggestions (subscribe [:get-content-suggestions])
          staged-commands     (subscribe [:get-chat-staged-commands])]
      (reaction
        (cond (and @command? (= @type :response) (not (seq @staged-commands)))
              c/request-info-height

              (and @command? (= @type :command) (seq @command-suggestions))
              c/suggestions-header-height

              :else 0)))))

(register-sub :command-icon-width
  (fn []
    (let [width (subscribe [:get :command-icon-width])
          type  (subscribe [:command-type])]
      (reaction (if (= :command @type)
                  @width
                  0)))))

(register-sub :get-requests
  (fn [db]
    (let [chat-id    (subscribe [:get-current-chat-id])
          staged-ids (subscribe [:get-chat-staged-commands-ids])]
      (reaction
        (let [ids      (set @staged-ids)
              requests (get-in @db [:chats @chat-id :requests])]
          (remove #(ids (:message-id %)) requests))))))

(register-sub :get-requests-map
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (->> (get-in @db [:chats @chat-id :requests])
                     (map #(vector (:message-id %) %))
                     (into {}))))))

(register-sub :get-request
  (fn [_ [_ message-id]]
    (let [requests (subscribe [:get-requests-map])]
      (reaction (get @requests message-id)))))

(register-sub :get-current-request
  (fn []
    (let [requests   (subscribe [:get-requests-map])
          message-id (subscribe [:get-chat-command-to-message-id])]
      (reaction (@requests @message-id)))))

(register-sub :get-response
  (fn [db [_ n]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :responses n])))))

(register-sub :is-request-answered?
  (fn [_ [_ message-id]]
    (let [requests (subscribe [:get-requests])]
      (reaction (not (some #(= message-id (:message-id %)) @requests))))))

(register-sub :validation-errors
  (fn [db]
    (let [current-chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:validation-errors @current-chat-id])))))

(register-sub :custom-validation-errors
  (fn [db]
    (let [current-chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:custom-validation-errors @current-chat-id])))))

(register-sub :unviewed-messages-count
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:unviewed-messages chat-id :count]))))

(register-sub :command-suggestions-height
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (get-in @db [:animations :command-suggestions-height @chat-id])))))

(register-sub :response-height
  (fn [db [_ status-bar]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (min (get-in @db [:animations :to-response-height @chat-id])
             (if (> (:layout-height @db) 0)
               (- (:layout-height @db)
                  (get-in platform-specific [:component-styles :status-bar status-bar :height]))
               0))))))

(register-sub :web-view-url
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:web-view-url @chat-id])))))

(register-sub :web-view-extra-js
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:web-view-extra-js @chat-id])))))

(register-sub :animate?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animate? @chat-id])))))

(register-sub :kb-mode
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:kb-mode @chat-id])))))

(register-sub :input-margin
  (fn []
    (let [kb-height   (subscribe [:get :keyboard-height])
          focused     (subscribe [:get :focused])
          mode        (subscribe [:kb-mode])
          kb-max      (subscribe [:get :keyboard-max-height])
          show-emoji? (subscribe [:chat-ui-props :show-emoji?])]
      (reaction
       (cond @show-emoji? (or @kb-max c/emoji-container-height)
             ios? @kb-height
             (and @focused (= :pan @mode) (pos? @kb-height)) 20
             :else 0)))))

(register-sub :max-layout-height
  (fn [db [_ status-bar]]
    (let [layout-height     (subscribe [:get :layout-height])
          input-margin      (subscribe [:input-margin])
          status-bar-height (get-in platform-specific [:component-styles :status-bar status-bar :height])]
      (reaction
        (- @layout-height @input-margin status-bar-height)))))

(register-sub :all-messages-loaded?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :all-loaded?])))))

(register-sub :photo-path
  (fn [_ [_ id]]
    (let [contacts (subscribe [:get :contacts])]
      (reaction (:photo-path (@contacts id))))))
