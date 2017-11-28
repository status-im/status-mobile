(ns status-im.chat.subs
  (:require [re-frame.core :refer [reg-sub dispatch subscribe path]]
            [status-im.data-store.chats :as chats]
            [status-im.chat.constants :as const]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.constants :refer [response-suggesstion-resize-duration
                                         content-type-status
                                         console-chat-id]]
            [status-im.commands.utils :as commands-utils]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(reg-sub :chats :chats)

(reg-sub :get-current-chat-id :current-chat-id)

(reg-sub :chat-ui-props :chat-ui-props)

(reg-sub
  :get-current-chat-ui-props
  :<- [:chat-ui-props]
  :<- [:get-current-chat-id]
  (fn [[chat-ui-props id]]
    (get chat-ui-props id)))

(reg-sub
  :get-current-chat-ui-prop
  :<- [:get-current-chat-ui-props]
  (fn [ui-props [_ prop]]
    (get ui-props prop)))

(reg-sub
  :validation-messages
  :<- [:get-current-chat-ui-props]
  (fn [ui-props]
    (some-> ui-props :validation-messages commands-utils/generate-hiccup)))

(reg-sub
  :result-box-markup
  :<- [:get-current-chat-ui-props]
  (fn [ui-props]
    (some-> ui-props :result-box :markup commands-utils/generate-hiccup)))

(reg-sub
  :chat-input-margin
  :<- [:get :keyboard-height]
  (fn [kb-height]
    (if ios? kb-height 0)))

(reg-sub
  :get-current-chat
  :<- [:chats]
  :<- [:get-current-chat-id]
  (fn [[chats id]]
    (get chats id)))

(reg-sub
  :chat
  :<- [:chats]
  :<- [:get-current-chat-id]
  (fn [[chats id] [_ k chat-id]]
    (get-in chats [(or chat-id id) k])))

(reg-sub
  :get-commands-for-chat
  :<- [:get-commands-responses-by-access-scope]
  :<- [:get-current-account]
  :<- [:get-current-chat]
  :<- [:get-contacts]
  (fn [[commands-responses account chat contacts]]
    (commands-model/commands-responses :command commands-responses account chat contacts)))

(reg-sub
  :get-responses-for-chat
  :<- [:get-commands-responses-by-access-scope]
  :<- [:get-current-account]
  :<- [:get-current-chat]
  :<- [:get-contacts]
  :<- [:chat :requests]
  (fn [[commands-responses account chat contacts requests]]
    (commands-model/requested-responses commands-responses account chat contacts (vals requests))))

(def ^:private map->sorted-seq (comp (partial map second) (partial sort-by first)))

(defn- available-commands-responses [[commands-responses input-text]]
  (->> commands-responses
       map->sorted-seq
       (filter #(str/includes? (chat-utils/command-name %) (or input-text "")))))

(reg-sub
  :get-available-commands
  :<- [:get-commands-for-chat]
  :<- [:chat :input-text]
  available-commands-responses)

(reg-sub
  :get-available-responses
  :<- [:get-responses-for-chat]
  :<- [:chat :input-text]
  available-commands-responses)

(reg-sub
  :get-available-commands-responses
  :<- [:get-commands-for-chat]
  :<- [:get-responses-for-chat]
  (fn [[commands responses]]
    (map->sorted-seq (merge commands responses))))

(reg-sub
  :selected-chat-command
  :<- [:get-current-chat]
  :<- [:get-commands-for-chat]
  :<- [:get-responses-for-chat]
  (fn [[chat commands responses]]
    (input-model/selected-chat-command chat commands responses)))

(reg-sub
  :current-chat-argument-position
  :<- [:selected-chat-command]
  :<- [:chat :input-text]
  :<- [:chat :seq-arguments]
  :<- [:get-current-chat-ui-prop :selection]
  (fn [[command input-text seq-arguments selection]]
    (input-model/current-chat-argument-position command input-text selection seq-arguments)))

(reg-sub
  :chat-parameter-box
  :<- [:get-current-chat]
  :<- [:selected-chat-command]
  :<- [:current-chat-argument-position]
  (fn [[current-chat selected-chat-command argument-position]]
    (cond
      (and selected-chat-command
           (not= argument-position input-model/*no-argument-error*))
      (get-in current-chat [:parameter-boxes
                            (get-in selected-chat-command [:command :name])
                            argument-position])

      (not selected-chat-command)
      (get-in current-chat [:parameter-boxes :message])

      :default
      nil)))

(reg-sub
  :show-parameter-box?
  :<- [:chat-parameter-box]
  :<- [:show-suggestions?]
  :<- [:chat :input-text]
  :<- [:validation-messages]
  (fn [[chat-parameter-box show-suggestions? input-text validation-messages]]
    (and (get chat-parameter-box :markup)
         (not validation-messages)
         (not show-suggestions?))))

(reg-sub
  :command-completion
  :<- [:selected-chat-command]
  input-model/command-completion)

(reg-sub
  :show-suggestions?
  :<- [:get-current-chat-ui-prop :show-suggestions?]
  :<- [:chat :input-text]
  :<- [:selected-chat-command]
  :<- [:get-available-commands-responses]
  (fn [[show-suggestions? input-text selected-command commands-responses]]
    (and (or show-suggestions? (input-model/starts-as-command? (str/trim (or input-text ""))))
         (not (:command selected-command))
         (seq commands-responses))))

(reg-sub
  :is-request-answered?
  :<- [:chat :requests]
  (fn [requests [_ message-id]]
    (not= "open" (get-in requests [message-id :status]))))

(reg-sub
  :unviewed-messages-count
  (fn [db [_ chat-id]]
    (get-in db [:unviewed-messages chat-id :count])))

(reg-sub
  :web-view-extra-js
  :<- [:get-current-chat]
  (fn [current-chat]
    (:web-view-extra-js current-chat)))

(reg-sub
  :all-messages-loaded?
  :<- [:get-current-chat]
  (fn [current-chat]
    (:all-loaded? current-chat)))

(reg-sub
  :photo-path
  :<- [:get-contacts]
  (fn [contacts [_ id]]
    (:photo-path (contacts id))))

;; TODO janherich: this is just bad and horribly ineffecient (always sorting to get last msg +
;; stale `:last-message` in app-db) refactor messages data-model to properly index them ASAP
(reg-sub
  :get-last-message
  :<- [:chats]
  (fn [chats [_ chat-id]]
    (let [{:keys [last-message messages]} (get chats chat-id)]
      (->> (conj messages last-message)
           (sort-by :clock-value >)
           (filter :show?)
           first))))

(reg-sub
  :get-message-short-preview-markup
  (fn [db [_ message-id]]
    (get-in db [:message-data :short-preview message-id :markup])))

(reg-sub
  :get-last-message-short-preview
  (fn [db [_ chat-id]]
    (let [last-message (subscribe [:get-last-message chat-id])
          preview (subscribe [:get-message-short-preview-markup (:message-id @last-message)])]
      (when-let [markup @preview]
        (commands-utils/generate-hiccup markup)))))

(reg-sub
  :get-default-container-area-height
  :<- [:get-current-chat-ui-prop :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/default-container-area-height bottom layout-height))))

(reg-sub
  :get-max-container-area-height
  :<- [:get-current-chat-ui-prop :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/max-container-area-height bottom layout-height))))

(reg-sub
  :chat-animations
  (fn [db [_ key type]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:chat-animations @chat-id key type]))))

(reg-sub
  :get-chat-last-outgoing-message
  :<- [:chats]
  (fn [chats [_ chat-id]]
    (->> (:messages (get chats chat-id))
         (filter :outgoing)
         (sort-by :clock-value >)
         first)))

(reg-sub
  :get-message-preview-markup
  (fn [db [_ message-id]]
    (get-in db [:message-data :preview message-id :markup])))

(reg-sub
  :get-message-preview
  (fn [[_ message-id]]
    [(subscribe [:get-message-preview-markup message-id])])
  (fn [[markup]]
    (when markup
      (commands-utils/generate-hiccup markup))))
