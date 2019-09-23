(ns status-im.ui.screens.chat.image.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.screens.chat.image.styles :as styles]
            [status-im.ui.components.button :as button]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(defn button [show-image?]
  [react/touchable-highlight
   {:on-press            (fn [_]
                           (re-frame/dispatch [:chat.ui/set-chat-ui-props {:show-image? (not show-image?)}])
                           (when-not platform/desktop? (js/setTimeout #(react/dismiss-keyboard!) 100)))
    :accessibility-label :show-photo-icon}
   [vector-icons/icon :main-icons/photo {:container-style {:margin 14 :margin-right 6}
                                         :color           (if show-image? colors/blue colors/gray)}]])

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue 0
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue  1
                               :duration 500
                               :useNativeDriver true})])))

(defview image-view []
  (letsubs [send-image        [:chats/current-chat-ui-prop :send-image]
            bottom-anim-value  (anim/create-value (styles/image-panel-height))
            alpha-value        (anim/create-value 0)]
    {:component-did-mount #(show-panel-anim bottom-anim-value alpha-value)}
    [react/animated-view {:style {:background-color :white
                                  :height           (styles/image-panel-height)
                                  :transform        [{:translateY bottom-anim-value}]
                                  :opacity          alpha-value}}
     [react/view {:align-items :center :justify-content :center}
      (if send-image
        [react/view {:align-items :center}
         [react/image {:source {:uri (str "data:image/jpeg;base64," send-image)} :style {:width 150 :height 150}}]
         [react/view {:flex-direction :row}
          [button/button {:label :t/cancel :on-press #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:send-image nil}])}]
          [button/button {:label "Send" :on-press #(re-frame/dispatch [:chat.ui/send-image (str "data:image/jpeg;base64," send-image)])}]]]
        [react/view {:flex-direction :row}
         [button/button {:label "Camera" :on-press (fn []
                                                     (re-frame/dispatch [:request-permissions
                                                                         {:permissions [:camera]
                                                                          :on-allowed  #(re-frame/dispatch [:navigate-to :profile-photo-capture])
                                                                          :on-denied (fn []
                                                                                       (utils/set-timeout
                                                                                        #(utils/show-popup (i18n/label :t/error)
                                                                                                           (i18n/label :t/camera-access-error))
                                                                                        50))}]))}]
         [button/button {:label "Galery" :on-press #(re-frame/dispatch [:chat.ui/open-image-picker])}]])]]))
