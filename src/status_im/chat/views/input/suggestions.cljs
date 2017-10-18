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
            [taoensso.timbre :as log]
            [status-im.chat.utils :as chat-utils]))

(defn suggestion-item [{:keys [on-press name description last?]}]
  [touchable-highlight {:on-press on-press}
   [view (style/item-suggestion-container last?)
    [view {:style style/item-suggestion-name}
     [text {:style style/item-suggestion-name-text
            :font  :roboto-mono} name]]
    [text {:style           style/item-suggestion-description
           :number-of-lines 2}
     description]]])

(defview request-item [{:keys [name description]
                        {:keys [type message-id]} :request :as command} last?]
  [{:keys [chat-id]} [:get-current-chat]]
  [suggestion-item
   {:on-press    #(let [{:keys [params]} (messages/get-message-content-by-id message-id)
                        metadata (assoc params :to-message-id message-id)]
                    (dispatch [:select-chat-input-command command metadata]))
    :name        (chat-utils/command-name command)
    :description description
    :last?       last?}])

(defn command-item [{:keys [name description bot] :as command} last?]
  [suggestion-item
   {:on-press    #(dispatch [:select-chat-input-command command nil])
    :name        (chat-utils/command-name command)
    :description description
    :last?       last?}])

(defn item-title [top-padding? s]
  [view (style/item-title-container top-padding?)
   [text {:style style/item-title-text}
    s]])

(defview suggestions-view []
  [show-suggestions? [:show-suggestions?]
   requests [:chat-actions :possible-requests]
   commands [:chat-actions :possible-commands]]
  (when show-suggestions?
    [expandable-view {:key        :suggestions
                      :draggable? false
                      :height     212}
     [view {:flex 1}
      [scroll-view {:keyboardShouldPersistTaps :always}
       (when (seq requests)
         [view
          [item-title false (label :t/suggestions-requests)]
          (for [[i {{:keys [chat-id message-id]} :request :as request}] (map-indexed vector requests)]
            ^{:key [chat-id message-id]}
            [request-item request (= i (dec (count requests)))])])
       (when (seq commands)
         [view
          [item-title (seq requests) (label :t/suggestions-commands)]
          (for [[i command] (->> commands
                                 (remove #(nil? (:title %)))
                                 (map-indexed vector))]
            ^{:key i}
            [command-item command (= i (dec (count commands)))])])]]]))
