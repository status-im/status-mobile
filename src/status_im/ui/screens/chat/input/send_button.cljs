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

(defn sendable? [input-text disconnected? login-processing?]
  (let [trimmed (string/trim input-text)]
    (not (or (string/blank? trimmed)
             (= trimmed "/")
             login-processing?
             disconnected?))))

(defview send-button-view []
  (letsubs [{:keys [command-completion]}            [:chats/selected-chat-command]
            {:keys [input-text seq-arg-input-text]} [:chats/current-chat]
            disconnected?                           [:disconnected?]
            login-processing?                       [:get-in [:accounts/login :processing]]
            spin-value                              (animation/create-value 1)]
    {:component-did-update (send-button-view-on-update {:spin-value         spin-value
                                                        :command-completion command-completion})}
    (when (and (sendable? input-text disconnected? login-processing?)
               (or (not command-completion)
                   (#{:complete :less-than-needed} command-completion)))
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/send-current-message])}
       (let [spin (.interpolate spin-value (clj->js {:inputRange  [0 1]
                                                     :outputRange ["0deg" "90deg"]}))]
         [react/animated-view
          {:style               (style/send-message-container spin)
           :accessibility-label :send-message-button}
          [vi/icon :main-icons/arrow-up {:container-style style/send-message-icon
                                         :color           :white}]])])))
