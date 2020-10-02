(ns status-im.ui.screens.profile.my-status.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.ui.components.icons.vector-icons :as icons]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.screens.profile.status :as my-status.messages]))

(defn take-picture []
  (react/show-image-picker-camera #(re-frame/dispatch [:chat.ui/image-captured (.-path %)]) {}))

(defn buttons []
  [react/view {:padding-horizontal 14 :padding-vertical 10 :justify-content :space-between :height 88}
   [pressable/pressable {:type                :scale
                         :accessibility-label :take-picture
                         :on-press            take-picture}
    [icons/icon :main-icons/camera]]
   [react/view {:style {:padding-top 8}}
    [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/open-image-picker])
                          :accessibility-label :open-gallery
                          :type                :scale}
     [icons/icon :main-icons/gallery]]]])

(defn image-preview [uri]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/camera-roll-pick uri])}
   [react/image {:style  (merge {:width            72
                                 :height           72
                                 :background-color :black
                                 :resize-mode      :cover
                                 :margin-right     4
                                 :border-radius    4})
                 :source {:uri uri}}]])

(defview photos []
  (letsubs [camera-roll-photos [:camera-roll-photos]]
    {:component-did-mount #(re-frame/dispatch [:chat.ui/camera-roll-get-photos 20])}
    [react/scroll-view {:horizontal                   true :style {:max-height 88}
                        :keyboard-should-persist-taps :handled}
     [react/view {:height         88 :border-top-width 1 :border-top-color colors/gray-lighter
                  :flex-direction :row :align-items :center}
      [buttons]
      (for [img camera-roll-photos]
        ^{:key (str "image" img)}
        (when img
          [image-preview img]))]]))

(defview sending-image []
  (letsubs [{:keys [uri]} [:chats/sending-image]]
    (when uri
      [react/view {:margin-horizontal 16 :margin-bottom 16}
       [my-status.messages/message-content-image uri true]])))

(defn my-status []
  (let [images-opened (reagent/atom false)
        input-text (re-frame/subscribe [:chats/current-chat-input-text])]
    (fn []
      [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
       [react/view {:flex 1}
        [topbar/topbar
         {:modal?        true
          :border-bottom true
          :title         (i18n/label :t/my-status)}]
        [react/scroll-view {:style                        {:flex 1}
                            :keyboard-should-persist-taps :handled}
         [react/text-input
          {:style               {:margin 16}
           :accessibility-label :my-status-input
           :max-length          300
           :auto-focus          true
           :multiline           true
           :on-change-text      #(re-frame/dispatch [:chat.ui/set-chat-input-text %])
           :default-value       @input-text
           :placeholder         (i18n/label :t/whats-on-your-mind)}]
         [sending-image]]
        [react/view
         (when @images-opened
           [photos])
         [react/view
          [toolbar/toolbar
           {:show-border? true
            :left
            [quo/button
             {:accessibility-label :open-images-panel-button
              :type                :secondary
              :on-press            #(swap! images-opened not)}
             [icons/icon :main-icons/photo {:color (if @images-opened colors/blue colors/gray)}]]
            :right
            [quo/button
             {:accessibility-label :send-my-status-button
              :type                :secondary
              :after               :main-icon/send
              :disabled            (string/blank? @input-text)
              :on-press            #(do
                                      (re-frame/dispatch [:profile.ui/send-my-status-message])
                                      (re-frame/dispatch [:navigate-back]))}
             (i18n/label :t/wallet-send)]}]
          [react/view {:top      0 :bottom 0 :left 0 :right 0 :align-items :center :justify-content :center
                       :position :absolute :pointerEvents :none}
           [react/text {:style {:color colors/gray}}
            (str (count @input-text) " / 300")]]]]]])))
