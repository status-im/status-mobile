(ns status-im.ui.screens.chat.image.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as icons]
            [reagent.core :as reagent]
            [quo.components.animated.pressable :as pressable]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]))

(defn take-picture []
  (react/show-image-picker-camera #(re-frame/dispatch [:chat.ui/image-captured (.-path %)]) {}))

(defn buttons []
  [react/view
   [pressable/pressable {:type                :scale
                         :accessibility-label :take-picture
                         :on-press            take-picture}
    [react/view {:style {:padding 10}}
     [icons/icon :main-icons/camera]]]
   [react/view {:style {:padding-top 8}}
    [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/open-image-picker])
                          :accessibility-label :open-gallery
                          :type                :scale}
     [react/view {:style {:padding 10}}
      [icons/icon :main-icons/gallery]]]]])

(defn image-preview [uri first? panel-height]
  (let [wh (/ (- panel-height 8) 2)]
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/camera-roll-pick uri])}
     [react/image {:style  (merge {:width            wh
                                   :height           wh
                                   :background-color :black
                                   :resize-mode      :cover
                                   :border-radius    4}
                                  (when first?
                                    {:margin-bottom 8}))
                   :source {:uri uri}}]]))

(defview photos []
  (letsubs [camera-roll-photos [:camera-roll-photos]
            panel-height (reagent/atom nil)]
    [react/view {:style     {:flex           1
                             :flex-direction :row}
                 :on-layout #(reset! panel-height (.-nativeEvent.layout.height ^js %))}
     (let [height @panel-height]
       (for [[first-img second-img] (partition 2 camera-roll-photos)]
         ^{:key (str "image" first-img)}
         [react/view {:margin-left 8}
          (when first-img
            [image-preview first-img true height])
          (when second-img
            [image-preview second-img false height])]))]))

(defview image-view []
  {:component-did-mount (fn []
                          (re-frame/dispatch [:chat.ui/camera-roll-get-photos 20]))}
  [react/animated-view {:style {:background-color colors/white
                                :flex             1}}
   [react/scroll-view {:horizontal true :style {:flex 1}}
    [react/view {:flex 1 :flex-direction :row :margin-horizontal 4}
     [buttons]
     [photos]]]])
