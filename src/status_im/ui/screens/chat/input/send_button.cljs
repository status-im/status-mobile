(ns status-im.ui.screens.chat.input.send-button
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.styles.input.send-button :as style]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]))

(defn send-button-view-on-update [{:keys [spin-value command-completion]}]
  (fn [_]
    (let [to-spin-value (if (#{:complete :no-command} command-completion) 1 0)]
      (animation/start
       (animation/timing spin-value {:toValue  to-spin-value
                                     :duration 300})))))

(defn sendable? [input-text network-status mailserver-connected?]
  (let [trimmed (string/trim input-text)]
    (not (or (string/blank? trimmed)
             (= trimmed "/")
             (= :offline network-status)
             (not mailserver-connected?)))))

(defview send-button-view []
  (letsubs [{:keys [command-completion]}            [:selected-chat-command]
            {:keys [input-text seq-arg-input-text]} [:get-current-chat]
            network-status                          [:network-status]
            mailserver-connected?                   [:mailserver/connected?]
            spin-value                              (animation/create-value 1)]
    {:component-did-update (send-button-view-on-update {:spin-value         spin-value
                                                        :command-completion command-completion})}
    (when (and (sendable? input-text network-status mailserver-connected?)
               (or (not command-completion)
                   (#{:complete :less-than-needed} command-completion)))
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/send-current-message])}
       (let [spin (.interpolate spin-value (clj->js {:inputRange  [0 1]
                                                     :outputRange ["0deg" "90deg"]}))]
         [react/animated-view
          {:style               (style/send-message-container spin)
           :accessibility-label :send-message-button}
          [vi/icon :icons/input-send {:container-style style/send-message-icon
                                      :color           :white}]])])))
