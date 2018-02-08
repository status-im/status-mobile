(ns status-im.chat.views.input.suggestions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.suggestions :as style]
            [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.models.commands :as commands-model]
            [status-im.i18n :as i18n]
            [taoensso.timbre :as log]))

(defn suggestion-item [{:keys [on-press name description last?]}]
  [react/touchable-highlight {:on-press on-press}
   [react/view (style/item-suggestion-container last?)
    [react/text {:style style/item-suggestion-name}
     name]
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

(defview suggestions-view []
  (letsubs [show-suggestions-view? [:show-suggestions-view?]
            responses              [:get-available-responses]
            commands               [:get-available-commands]]
    (let [number-of-entries (+ (count responses) (count commands))]
      [expandable/expandable-view {:key             :suggestions
                                   :draggable?      false
                                   :height          (* number-of-entries
                                                       (+ style/item-height
                                                          style/border-height))
                                   :dynamic-height? true}
       [react/view
        [react/scroll-view {:keyboard-should-persist-taps :always
                            :bounces                      false}
         (when (seq responses)
           (for [[i response] (map-indexed vector responses)]
             ^{:key i}
             [response-item response (= i (dec (count responses)))]))
         (when (seq commands)
           (for [[i command] (map-indexed vector commands)]
             ^{:key i}
             [command-item command (= i (dec (count commands)))]))]]])))
