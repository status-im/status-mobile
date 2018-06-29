(ns status-im.chat.views.input.suggestions
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.chat.styles.input.suggestions :as style]
            [status-im.chat.views.input.animations.expandable :as expandable]
            [status-im.chat.models.commands :as commands-model]
            [status-im.i18n :as i18n]
            [taoensso.timbre :as log]))

(defn suggestion-item [{:keys [on-press name description last? accessibility-label]}]
  [react/touchable-highlight (cond-> {:on-press on-press}
                               accessibility-label (assoc :accessibility-label accessibility-label))
   [react/view style/item-suggestion-container
    [react/text {:style style/item-suggestion-name}
     name]
    [react/text {:style           style/item-suggestion-description
                 :number-of-lines 2}
     description]]])

(defview suggestions-view []
  (letsubs [commands [:get-available-commands]]
    [expandable/expandable-view {:key :suggestions}
     [react/view
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :bounces                      false}
       (when (seq commands)
         (for [[i {:keys [description] :as command}] (map-indexed vector commands)]
           ^{:key i}
           [suggestion-item {:on-press            #(re-frame/dispatch [:select-chat-input-command command nil])
                             :name                (commands-model/command-name command)
                             :description         description
                             :last?               (= i (dec (count commands)))
                             :accessibility-label (case (:name command)
                                                    "send"    :send-payment-button
                                                    "request" :request-payment-button
                                                    nil)}]))]]]))
