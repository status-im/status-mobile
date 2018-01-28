(ns status-im.chat.views.input.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.suggestions :as style]
            [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.models.commands :as commands-model] 
            [status-im.i18n :as i18n]))

(defn suggestion-item [{:keys [on-press name description last?]}]
  [react/touchable-highlight {:on-press on-press}
   [react/view (style/item-suggestion-container last?)
    [react/view {:style style/item-suggestion-name}
     [react/text {:style style/item-suggestion-name-text
                  :font  :roboto-mono} name]]
    [react/text {:style           style/item-suggestion-description
                 :number-of-lines 2}
     description]]])

(defview response-item [{:keys [name description]
                         {:keys [message-id]} :request :as command} last?]
  [{{:keys [params]} :content} [:get-current-chat-message message-id]]
  [suggestion-item
   {:on-press    #(let [metadata (assoc params :to-message-id message-id)]
                    (re-frame/dispatch [:select-chat-input-command command metadata]))
    :name        (commands-model/command-name command)
    :description description
    :last?       last?}])

(defn command-item [{:keys [name description bot] :as command} last?]
  [suggestion-item
   {:on-press    #(re-frame/dispatch [:select-chat-input-command command nil])
    :name        (commands-model/command-name command)
    :description description
    :last?       last?}])

(defn item-title [top-padding? title]
  [react/view (style/item-title-container top-padding?)
   [react/text {:style style/item-title-text}
    title]])

(defview suggestions-view []
  [show-suggestions? [:show-suggestions?]
   responses [:get-available-responses]
   commands [:get-available-commands]]
  (when show-suggestions?
    [expandable/expandable-view {:key        :suggestions
                                 :draggable? false
                                 :height     212}
     [react/view {:flex 1}
      [react/scroll-view {:keyboardShouldPersistTaps :always}
       (when (seq responses)
         [react/view
          [item-title false (i18n/label :t/suggestions-requests)]
          (for [[i response] (map-indexed vector responses)]
            ^{:key i}
            [response-item response (= i (dec (count responses)))])])
       (when (seq commands)
         [react/view
          [item-title (seq responses) (i18n/label :t/suggestions-commands)]
          (for [[i command] (map-indexed vector commands)]
            ^{:key i}
            [command-item command (= i (dec (count commands)))])])]]]))
