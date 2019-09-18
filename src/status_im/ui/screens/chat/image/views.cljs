(ns status-im.ui.screens.chat.image.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.screens.chat.image.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn button [images-showing?]
  [react/touchable-highlight
   {:on-press
    (fn [_]
      (re-frame/dispatch [:chat.ui/set-chat-ui-props
                          {:input-bottom-sheet (when-not images-showing? :images)}])
      (when-not platform/desktop? (js/setTimeout #(react/dismiss-keyboard!) 100)))
    :accessibility-label :show-photo-icon}
   [icons/icon
    :main-icons/photo
    {:container-style {:margin 14 :margin-right 6}
     :color           (if images-showing? colors/blue colors/gray)}]])

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue 0
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue  1
                               :duration 500
                               :useNativeDriver true})])))

(defn select-button [title icon on-press]
  [react/touchable-highlight {:on-press on-press :style {:flex 1}}
   [react/view {:background-color colors/black :max-height 223 :flex 1 :border-radius 16
                :align-items :center :justify-content :center}
    [react/view {:height 48 :width 48 :align-items :center :justify-content :center :border-radius 24
                 :border-width 2 :border-color colors/gray}
     [icons/icon icon {:color :white}]]
    [react/text {:style {:margin-top 9 :typography :caption :color colors/gray}} title]]])

(defn take-picture []
  (re-frame/dispatch [:request-permissions
                      {:permissions [:camera]
                       :on-allowed  #(re-frame/dispatch [:navigate-to :profile-photo-capture])
                       :on-denied (fn []
                                    (utils/set-timeout
                                     #(utils/show-popup (i18n/label :t/error)
                                                        (i18n/label :t/camera-access-error))
                                     50))}]))

(defn round-button [on-press cancel? loading?]
  [react/touchable-highlight {:on-press (when-not loading? on-press)}
   [react/view {:width 32 :height 32 :border-radius 16 :background-color (if cancel? colors/gray colors/blue)
                :align-items :center :justify-content :center}
    (if loading?
      [react/activity-indicator {:color     :white
                                 :animating true}]
      [icons/icon (if cancel? :main-icons/close :main-icons/arrow-up) {:color :white}])]])

(defview image-view []
  (letsubs [send-image        [:chats/current-chat-ui-prop :send-image]
            loading?        [:chats/current-chat-ui-prop :send-image-loading?]
            bottom-anim-value  (anim/create-value styles/image-panel-height)
            alpha-value        (anim/create-value 0)]
    {:component-did-mount #(show-panel-anim bottom-anim-value alpha-value)}
    [react/animated-view {:style {:background-color :white
                                  :height           styles/image-panel-height
                                  :transform        [{:translateY bottom-anim-value}]
                                  :opacity          alpha-value}}
     (if send-image
       [react/view {:align-items :center :flex-direction :row :flex 1 :justify-content :space-between
                    :padding-horizontal 20}
        [round-button #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:send-image nil}]) true false]
        [react/image {:source {:uri send-image} :style {:width 150 :height 150 :border-radius 8}}]
        [round-button #(re-frame/dispatch [:chat.ui/send-image send-image]) false loading?]]
       [react/view {:flex-direction :row :padding-horizontal 16 :padding-top 12 :flex 1}
        [select-button "Take a picture" :main-icons/camera take-picture]
        [react/view {:width 16}]
        [select-button "Choose photo" :main-icons/photo #(re-frame/dispatch [:chat.ui/open-image-picker])]])]))