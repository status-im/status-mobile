(ns status-im.ui.screens.chat.image.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [quo.components.animated.pressable :as pressable]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui.components.react :as react]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]))

(defn take-picture
  []
  (permissions/request-permissions
   {:permissions [:camera]
    :on-allowed  #(re-frame/dispatch [:chat.ui/show-image-picker-camera])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/camera-access-error))
                    50))}))

(defn show-image-picker
  []
  (permissions/request-permissions
   {:permissions [:read-external-storage :write-external-storage]
    :on-allowed  #(re-frame/dispatch [:chat.ui/open-image-picker])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/external-storage-denied))
                    50))}))

(defn buttons
  []
  [react/view
   [pressable/pressable
    {:type                :scale
     :accessibility-label :take-picture
     :on-press            take-picture}
    [react/view {:style {:padding 10}}
     [icons/icon :main-icons/camera]]]
   [react/view {:style {:padding-top 8}}
    [pressable/pressable
     {:on-press            show-image-picker
      :accessibility-label :open-gallery
      :type                :scale}
     [react/view {:style {:padding 10}}
      [icons/icon :main-icons/gallery]]]]])

(defn image-preview
  [uri all-selected first? panel-height]
  (let [wh           (/ (- panel-height 8) 2)
        selected     (get all-selected uri)
        max-selected (>= (count all-selected) config/max-images-batch)]
    [react/touchable-highlight
     {:on-press #(if selected
                   (re-frame/dispatch [:chat.ui/image-unselected uri])
                   (re-frame/dispatch [:chat.ui/camera-roll-pick uri]))
      :disabled (and max-selected (not selected))}
     [react/view
      {:style (merge {:width         wh
                      :height        wh
                      :border-radius 4
                      :overflow      :hidden}
                     (when (and (not selected) max-selected)
                       {:opacity 0.5})
                     (when first?
                       {:margin-bottom 4}))}
      [react/image
       {:style  (merge {:width            wh
                        :height           wh
                        :background-color :black
                        :resize-mode      :cover
                        :border-radius    4})
        :source {:uri uri}}]
      (when selected
        [react/view
         {:style {:position         :absolute
                  :top              0
                  :bottom           0
                  :left             0
                  :right            0
                  :padding          10
                  :background-color (:highlight @colors/theme)
                  :align-items      :flex-end}}
         [quo/radio {:value true}]])]]))

(defview photos
  []
  (letsubs [camera-roll-photos [:camera-roll/photos]
            selected           [:chats/sending-image]
            panel-height       (reagent/atom nil)]
    [react/view
     {:style     {:flex           1
                  :flex-direction :row}
      :on-layout #(reset! panel-height (.-nativeEvent.layout.height ^js %))}
     (let [height @panel-height]
       (for [[first-img second-img] (partition 2 camera-roll-photos)]
         ^{:key (str "image" first-img)}
         [react/view {:margin-left 4}
          (when first-img
            [image-preview first-img selected true height])
          (when second-img
            [image-preview second-img selected false height])]))]))

(defview image-view
  []
  {:component-did-mount
   (fn []
     (permissions/request-permissions
      {:permissions [:read-external-storage :write-external-storage]
       :on-allowed  #(re-frame/dispatch [:chat.ui/camera-roll-get-photos 20])
       :on-denied   (fn []
                      (utils/set-timeout
                       #(utils/show-popup (i18n/label :t/error)
                                          (i18n/label :t/external-storage-denied))
                       50))}))}
  [react/animated-view
   {:style {:background-color (:ui-background @colors/theme)
            :flex             1}}
   [react/scroll-view {:horizontal true :style {:flex 1}}
    [react/view {:flex 1 :flex-direction :row :margin-horizontal 4}
     [buttons]
     [photos]]]])
