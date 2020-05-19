(ns status-im.ui.screens.chat.image.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as anim]))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         0
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         1
                               :duration        500
                               :useNativeDriver true})])))

(defn input-button [images-showing?]
  [quo/button
   {:on-press            (fn [_]
                           (re-frame/dispatch [:chat.ui/set-chat-ui-props
                                               {:input-bottom-sheet (when-not images-showing? :images)}])
                           (when-not platform/desktop? (js/setTimeout #(react/dismiss-keyboard!) 100)))
    :accessibility-label :show-photo-icon
    :type                :icon
    :theme               (if images-showing? :main :disabled)}
   :main-icons/photo])

(defn take-picture []
  (react/show-image-picker-camera #(re-frame/dispatch [:chat.ui/image-captured (.-path %)]) {}))

(defn buttons []
  [react/view
   [quo/button {:type     :icon
                :theme    :icon
                :accessibility-label :take-picture
                :on-press take-picture}
    :main-icons/camera]
   [react/view {:style {:padding-top 8}}
    [quo/button {:on-press #(re-frame/dispatch [:chat.ui/open-image-picker])
                 :accessibility-label :open-gallery
                 :type     :icon
                 :theme    :icon}
     :main-icons/gallery]]])

(defn image-preview [uri first? panel-height]
  (let [wh (/ (- panel-height 8) 2)]
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/image-selected uri])}
     [react/image {:style  (merge {:width            wh
                                   :height           wh
                                   :background-color :black
                                   :border-radius    4}
                                  (when first?
                                    {:margin-bottom 8}))
                   :source {:uri uri}}]]))

(defview photos [panel-height]
  (letsubs [camera-roll-photos [:camera-roll-photos]]
    [react/view {:flex 1 :flex-direction :row}
     (for [[first-img second-img] (partition 2 camera-roll-photos)]
       ^{:key (str "image" first-img)}
       [react/view {:margin-left 8}
        (when first-img
          [image-preview first-img true panel-height])
        (when second-img
          [image-preview second-img false panel-height])])]))

(defview image-view []
  (letsubs [panel-height      [:chats/chat-panel-height]
            bottom-anim-value (anim/create-value @panel-height)
            alpha-value       (anim/create-value 0)]
    {:component-did-mount (fn []
                            (show-panel-anim bottom-anim-value alpha-value)
                            (re-frame/dispatch [:chat.ui/camera-roll-get-photos 20]))}
    [react/animated-view {:style {:background-color colors/white
                                  :height           panel-height
                                  :transform        [{:translateY bottom-anim-value}]
                                  :opacity          alpha-value}}
     [react/scroll-view {:horizontal true :style {:flex 1}}
      [react/view {:flex 1 :flex-direction :row :margin-horizontal 8}
       [buttons]
       [photos panel-height]]]]))
