(ns status-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub dispatch subscribe path]]
            [status-im.data-store.chats :as chats]
            [status-im.chat.constants :as const]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.constants :refer [response-suggesstion-resize-duration
                                         content-type-status
                                         console-chat-id]]
            [status-im.models.commands :as commands]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(register-sub
  :chat-properties
  (fn [db [_ properties]]
    (->> properties
         (map (fn [k]
                [k (-> @db
                       (get-in [:chats (:current-chat-id @db) k])
                       (reaction))]))
         (into {}))))


(register-sub
  :chat-ui-props
  (fn [db [_ ui-element chat-id]]
    (let [chat-id (or chat-id (:current-chat-id @db))]
      (reaction (get-in @db [:chat-ui-props chat-id ui-element])))))

(register-sub
  :chat-input-margin
  (fn []
    (let [kb-height (subscribe [:get :keyboard-height])]
      (reaction
        (if ios? @kb-height 0)))))

(register-sub
  :chat
  (fn [db [_ k chat-id]]
    (-> @db
        (get-in [:chats (or chat-id (:current-chat-id @db)) k])
        (reaction))))

(register-sub
  :get-current-chat-id
  (fn [db _]
    (reaction (:current-chat-id @db))))

(register-sub
  :get-chat-by-id
  (fn [_ [_ chat-id]]
    (reaction (chats/get-by-id chat-id))))

(register-sub
  :get-commands
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :commands]) {})))))

(register-sub
  :get-responses
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :responses]) {})))))

(register-sub
  :possible-chat-actions
  (fn [db [_ chat-id]]
    "Returns a vector of [command message-id] values. `message-id` can be `:any`.
     Example: [[browse-command :any] [debug-command :any] [phone-command '1489161286111-58a2cd...']]"
    (let [chat-id (or chat-id (@db :current-chat-id))]
      (reaction
        (input-model/possible-chat-actions @db chat-id)))))

(register-sub
  :selected-chat-command
  (fn [db [_ chat-id]]
    (let [chat-id (or chat-id (@db :current-chat-id))]
      (reaction
        (input-model/selected-chat-command @db chat-id)))))

(register-sub
  :current-chat-argument-position
  (fn [db [_ chat-id]]
    (let [chat-id    (or chat-id (@db :current-chat-id))
          command    (subscribe [:selected-chat-command chat-id])
          input-text (subscribe [:chat :input-text chat-id])]
      (reaction
        (input-model/current-chat-argument-position @command @input-text)))))

(register-sub
  :chat-parameter-box
  (fn [db [_ chat-id]]
    (let [chat-id (or chat-id (@db :current-chat-id))
          command (subscribe [:selected-chat-command chat-id])
          index   (subscribe [:current-chat-argument-position chat-id])]
      (reaction
        (cond
          (and @command (> @index -1))
          (let [command-name (get-in @command [:command :name])]
            (get-in @db [:chats chat-id :parameter-boxes command-name @index]))

          (not @command)
          (get-in @db [:chats chat-id :parameter-boxes :message])

          :default
          nil)))))

(register-sub
  :command-complete?
  (fn [db [_ chat-id]]
    (reaction
      (input-model/command-complete? @db chat-id))))

(register-sub
  :show-suggestions?
  (fn [db [_ chat-id]]
    (let [chat-id           (or chat-id (@db :current-chat-id))
          show-suggestions? (subscribe [:chat-ui-props :show-suggestions? chat-id])
          input-text        (subscribe [:chat :input-text chat-id])
          selected-command  (subscribe [:selected-chat-command chat-id])
          requests          (subscribe [:chat :request-suggestions chat-id])
          commands          (subscribe [:chat :command-suggestions chat-id])]
      (reaction
        (and (or @show-suggestions?
                 (.startsWith (or @input-text "") const/command-char))
             (not (:command @selected-command))
             (or (not-empty @requests)
                 (not-empty @commands)))))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (:current-chat-id @db)]
      (reaction (get-in @db [:chats current-chat-id])))))

(register-sub :get-chat
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:chats chat-id]))))

(register-sub :get-response
  (fn [db [_ n]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :responses n])))))

(register-sub :is-request-answered?
  (fn [_ [_ message-id]]
    (let [requests (subscribe [:chat :requests])]
      (reaction (not-any? #(= message-id (:message-id %)) @requests)))))

(register-sub :unviewed-messages-count
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:unviewed-messages chat-id :count]))))

(register-sub :web-view-extra-js
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:web-view-extra-js @chat-id])))))

(register-sub :all-messages-loaded?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :all-loaded?])))))

(register-sub :photo-path
  (fn [_ [_ id]]
    (let [contacts (subscribe [:get :contacts])]
      (reaction (:photo-path (@contacts id))))))

(register-sub :get-last-message
  (fn [db [_ chat-id]]
    (reaction
      (let [{:keys [last-message messages]} (get-in @db [:chats chat-id])]
        (first
          (sort-by :clock-value > (conj messages last-message)))))))

(register-sub :get-last-message-short-preview
  (fn [db [_ chat-id]]
    (let [last-message (subscribe [:get-last-message chat-id])]
      (reaction
        (get-in @db [:message-data :short-preview (:message-id @last-message)])))))

(register-sub :get-default-container-area-height
  (fn [db]
    (reaction
      (let [input-height      (subscribe [:chat-ui-props :input-height])
            layout-height     (subscribe [:get :layout-height])
            chat-input-margin (subscribe [:chat-input-margin])
            bottom            (+ @input-height @chat-input-margin)]
        (input-utils/default-container-area-height bottom @layout-height)))))

(register-sub :get-max-container-area-height
  (fn [db]
    (reaction
      (let [input-height      (subscribe [:chat-ui-props :input-height])
            layout-height     (subscribe [:get :layout-height])
            chat-input-margin (subscribe [:chat-input-margin])
            bottom            (+ @input-height @chat-input-margin)]
        (input-utils/max-container-area-height bottom @layout-height)))))

(register-sub :chat-animations
  (fn [db [_ key type]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chat-animations @chat-id key type])))))
