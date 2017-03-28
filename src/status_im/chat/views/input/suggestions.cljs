(ns status-im.chat.views.input.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.data-store.messages :as messages]
            [status-im.chat.styles.input.suggestions :as style]
            [status-im.chat.constants :as const]
            [status-im.chat.views.input.animations.expandable :refer [expandable-view]]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]))

(defn suggestion-item [{:keys [on-press name description]}]
  [touchable-highlight {:on-press on-press}
   [view (style/item-suggestion-container true)
    [view {:style style/item-suggestion-name}
     [text {:style style/item-suggestion-name-text
            :font  :roboto-mono}
      const/command-char name]]
    [text {:style style/item-suggestion-description}
     description]]])

(defview request-item [index {:keys [type message-id] :as rr}]
  [{:keys [name description] :as response} [:get-response type]
   {:keys [chat-id]} [:get-current-chat]]
  [suggestion-item
   {:on-press    #(let [{:keys [params]} (messages/get-message-content-by-id message-id)
                        metadata (assoc params :to-message-id message-id)]
                    (dispatch [:select-chat-input-command response metadata]))
    :name        name
    :description description}])

(defview command-item [index [_ {:keys [name description] :as command}]]
  []
  [suggestion-item
   {:on-press    #(dispatch [:select-chat-input-command command nil])
    :name        name
    :description description}])

(defn item-title [top-padding? s]
  [view (style/item-title-container top-padding?)
   [text {:style style/item-title-text}
    s]])

(defview suggestions-view []
  [show-suggestions? [:show-suggestions?]
   requests [:chat :request-suggestions]
   commands [:chat :command-suggestions]]
  (when show-suggestions?
    [expandable-view :suggestions
     [view {:flex 1}
      [scroll-view {:keyboardShouldPersistTaps true}
       (when (seq requests)
         [view
          [item-title false (label :t/suggestions-requests)]
          (for [{:keys [chat-id message-id] :as request} requests]
            ^{:key [chat-id message-id]}
            [request-item 0 request])])
       (when (seq commands)
         [view
          [item-title (seq requests) (label :t/suggestions-commands)]
          (for [command (remove #(nil? (:title (second %))) commands)]
            ^{:key (first command)}
            [command-item 0 command])])]]]))