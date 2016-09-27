(ns status-im.chat.views.plain-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                text
                                                touchable-highlight]]
            [status-im.components.animation :as anim]
            [status-im.chat.styles.plain-message :as st]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send []
  (dispatch [:send-chat-message]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn button-animation-logic [{:keys [command? val]}]
  (fn [_]
    (let [to-scale (if @command? 0 1)]
      (anim/start (anim/spring val {:toValue to-scale
                                    :tension 30})))))

(defn list-container [min]
  (fn [{:keys [command? width]}]
    (let [n-width (if @command? min 56)
          delay (if @command? 100 0)]
      (anim/start (anim/timing width {:toValue  n-width
                                      :duration response-input-hiding-duration
                                      :delay    delay})
                  #(dispatch [:set :disable-input false])))))

(defn commands-button [height on-press]
  (let [command? (subscribe [:command?])
        requests (subscribe [:get-requests])
        suggestions (subscribe [:get-suggestions])
        buttons-scale (anim/create-value (if @command? 1 0))
        container-width (anim/create-value (if @command? 20 56))
        context {:command? command?
                 :val      buttons-scale
                 :width    container-width}
        on-update (fn [_]
                    ((button-animation-logic context))
                    ((list-container 20) context))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [on-press]
         [touchable-highlight {:on-press #(do (dispatch [:switch-command-suggestions!])
                                              (on-press))
                               :disabled @command?}
          [animated-view {:style (st/message-input-button-touchable container-width height)}
           (when-not @command?
             [animated-view {:style (st/message-input-button buttons-scale)}
              (if (seq @suggestions)
                [icon :close_gray st/close-icon]
                [icon :input_list st/list-icon])
              (when (and (seq @requests)
                         (not (seq @suggestions)))
                [view st/requests-icon])])]])})))

(defn smile-animation-logic [{:keys [command? val width]}]
  (fn [_]
    (let [to-scale (if @command? 0 1)]
      (when-not @command? (anim/set-value width 56))
      (anim/start (anim/spring val {:toValue to-scale
                                    :tension 30})
                  (fn [e]
                    (when (and @command? (.-finished e))
                      (anim/set-value width 0.1)))))))

(defn smile-button [height]
  (let [command? (subscribe [:command?])
        buttons-scale (anim/create-value (if @command? 1 0))
        container-width (anim/create-value (if @command? 0.1 56))
        context {:command? command?
                 :val      buttons-scale
                 :width    container-width}
        on-update (smile-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn []
         [touchable-highlight {:on-press (fn []
                                           ;; TODO emoticons: not implemented
                                           )
                               :disabled @command?}
          [animated-view {:style (st/message-input-button-touchable container-width height)}
           [animated-view {:style (st/message-input-button buttons-scale)}
            [icon :smile st/smile-icon]]]])})))
