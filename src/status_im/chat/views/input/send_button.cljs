(ns status-im.chat.views.input.send-button
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.chat.styles.input.send-button :as style]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.utils.utils :as utils]))

(defn send-button-view-on-update [{:keys [spin-value command-completion]}]
  (fn [_]
    (let [to-spin-value (if (some #{:complete :no-command} [@command-completion]) 1 0)]
      (animation/start
       (animation/timing spin-value {:toValue  to-spin-value
                                     :duration 300})))))

(defn sendable? [input-text]
  (let [trimmed (string/trim input-text)]
    (not (or (string/blank? trimmed) (= trimmed "/")))))

(defview send-button-view []
  (letsubs [command-completion                      [:command-completion]
            selected-command                        [:selected-chat-command]
            {:keys [input-text seq-arg-input-text]} [:get-current-chat]
            spin-value                              (animation/create-value 1)
            on-update                               (send-button-view-on-update {:spin-value         spin-value
                                                                                 :command-completion command-completion})]
    {:component-did-update on-update}
    (when (and (sendable? input-text)
               (or (not selected-command)
                   (some #{:complete :less-than-needed} [command-completion])))
      [react/touchable-highlight {:on-press #(status-im.thread/dispatch [:send-current-message])}
       (let [spin (.interpolate spin-value (clj->js {:inputRange  [0 1]
                                                     :outputRange ["0deg" "90deg"]}))]
         [react/animated-view
          {:style               (style/send-message-container spin)
           :accessibility-label :send-message-button}
          [vi/icon :icons/input-send {:container-style style/send-message-icon
                                      :color           :white}]])])))
