(ns status-im.ui.screens.status.new.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [quo.components.animated.pressable :as pressable]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.status.new.styles :as styles]
            [status-im.ui.screens.status.views :as status.views]
            [status-im.utils.platform :as platform]))

(defn buttons
  []
  [react/view styles/buttons
   [pressable/pressable
    {:type                :scale
     :accessibility-label :take-picture
     :on-press            #(re-frame/dispatch [:chat.ui/show-image-picker-camera-timeline])}
    [icons/icon :main-icons/camera]]
   [react/view {:style {:padding-top 8}}
    [pressable/pressable
     {:on-press            #(re-frame/dispatch [:chat.ui/open-image-picker-timeline])
      :accessibility-label :open-gallery
      :type                :scale}
     [icons/icon :main-icons/gallery]]]])

(defn image-preview
  [uri]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/camera-roll-pick-timeline uri])}
   [react/image
    {:style  styles/image
     :source {:uri uri}}]])

(defview photos
  []
  (letsubs [camera-roll-photos [:camera-roll/photos]]
    {:component-did-mount #(re-frame/dispatch [:chat.ui/camera-roll-get-photos 20])}
    [react/scroll-view
     {:horizontal                   true
      :style                        {:max-height 88}
      :keyboard-should-persist-taps :handled}
     [react/view (styles/photos-buttons)
      [buttons]
      (for [img camera-roll-photos]
        ^{:key (str "image" img)}
        [image-preview img])]]))

(def message-max-length 600)

(defn my-status
  []
  (let [images-opened (reagent/atom false)
        scroll        (reagent/atom nil)
        autoscroll?   (reagent/atom false)
        scroll-height (reagent/atom nil)
        input-text    (re-frame/subscribe [:chats/timeline-chat-input-text])
        sending-image (re-frame/subscribe [:chats/timeline-sending-image])]
    (fn []
      (let [{:keys [uri]} (first (vals @sending-image))
            text-length   (count @input-text)]
        [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
         [:<>
          [react/scroll-view
           {:style                        {:flex 1}
            :ref                          #(reset! scroll %)
            :on-layout                    #(reset! scroll-height
                                             (.-nativeEvent.layout.height ^js %))
            :keyboard-should-persist-taps :handled}
           [react/text-input
            {:style                  {:margin 16}
             :scroll-enabled         false
             :accessibility-label    :my-status-input
             :max-length             (if platform/android?
                                       message-max-length
                                       (when (>= text-length message-max-length)
                                         text-length))
             :auto-focus             true
             :multiline              true
             :on-selection-change    (fn [args]
                                       (let [selection (.-selection ^js (.-nativeEvent ^js args))
                                             end       (.-end ^js selection)]
                                         (reset! autoscroll? (< (- (count @input-text) end) 10))))
             :on-content-size-change #(when (and @autoscroll? @scroll @scroll-height)
                                        (when-let [height (- (.-nativeEvent.contentSize.height ^js %)
                                                             @scroll-height
                                                             -40)]
                                          (.scrollTo @scroll #js {:y height :animated true})))
             :on-change-text         #(re-frame/dispatch [:chat.ui/set-timeline-input-text %])
             :default-value          @input-text
             :placeholder            (i18n/label :t/whats-on-your-mind)}]
           (when uri
             [react/view {:margin-horizontal 16 :margin-bottom 16}
              [status.views/message-content-image uri true]])]
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
                :disabled            (or (> text-length message-max-length)
                                         (and (string/blank? @input-text) (not uri)))
                :on-press            #(do
                                        (re-frame/dispatch [:profile.ui/send-my-status-message])
                                        (re-frame/dispatch [:navigate-back]))}
               (i18n/label :t/wallet-send)]}]
            [react/view styles/count-container
             [react/text
              {:style {:color (if (> text-length message-max-length)
                                colors/red
                                colors/gray)}}
              (str text-length " / " message-max-length)]]]]]]))))
