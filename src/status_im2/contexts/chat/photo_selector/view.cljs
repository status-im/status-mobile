(ns status-im2.contexts.chat.photo-selector.view
  (:require [i18n.i18n :as i18n]
            [quo.components.safe-area :as safe-area]
            [quo2.components.notifications.info-count :as info-count]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.photo-selector.style :as style]
            [status-im.utils.core :as utils]
            [quo.react]
            [utils.re-frame :as rf]))

(def selected (reagent/atom []))

(defn on-press-confirm-selection
  [chat-id]
  (rf/dispatch [:chat.ui/clear-sending-images chat-id])
  (doseq [item @selected]
    (rf/dispatch [:chat.ui/camera-roll-pick item]))
  (reset! selected [])
  (rf/dispatch [:bottom-sheet/hide]))

(defn bottom-gradient
  [chat-id selected-images]
  [:f>
   (fn []
     (let [safe-area (safe-area/use-safe-area)]
       (when (or (seq @selected) selected-images)
         [linear-gradient/linear-gradient
          {:colors [:black :transparent]
           :start  {:x 0 :y 1}
           :end    {:x 0 :y 0}
           :style  (style/gradient-container safe-area)}
          [quo/button
           {:style               {:align-self        :stretch
                                  :margin-horizontal 20}
            :on-press            #(on-press-confirm-selection chat-id)
            :accessibility-label :confirm-selection}
           (i18n/label :t/confirm-selection)]])))])

(defn clear-button
  []
  (when (seq @selected)
    [rn/touchable-opacity
     {:on-press            #(reset! selected [])
      :style               (style/clear-container)
      :accessibility-label :clear}
     [quo/text {:weight :medium} (i18n/label :t/clear)]]))

(defn remove-selected
  [coll item]
  (vec (remove #(= % item) coll)))

(defn image
  [item index _ {:keys [window-width]}]
  [rn/touchable-opacity
   {:active-opacity      1
    :on-press            (fn []
                           (if (some #{item} @selected)
                             (swap! selected remove-selected item)
                             (swap! selected conj item)))
    :accessibility-label (str "image-" index)}
   [rn/image
    {:source {:uri item}
     :style  (style/image window-width index)}]
   (when (some #{item} @selected)
     [rn/view {:style (style/overlay window-width)}])
   (when (some #{item} @selected)
     [info-count/info-count
      {:style               style/image-count
       :accessibility-label (str "count-" index)}
      (inc (utils/first-index #(= item %) @selected))])])

(defn photo-selector
  [chat-id]
  (rf/dispatch [:chat.ui/camera-roll-get-photos 20])
  (let [selected-images (keys (rf/sub [:chats/sending-image]))]
    [:f>
     (fn []
       (rn/use-effect-once
         #(do
            (if selected-images
              (reset! selected (vec selected-images))
              (reset! selected []))
            js/undefined))
       (let [{window-height :height window-width :width} (rn/use-window-dimensions)
             safe-area                                   (safe-area/use-safe-area)
             camera-roll-photos                          (rf/sub [:camera-roll/photos])
             end-cursor                                  (rf/sub [:camera-roll/end-cursor])
             loading?                                    (rf/sub [:camera-roll/loading-more])
             has-next-page?                              (rf/sub [:camera-roll/has-next-page])]
         [rn/view {:style {:height (- window-height (:top safe-area))}}
          [rn/touchable-opacity
           {:on-press #(js/alert "Camera: not implemented")
            :style    (style/camera-button-container)}
           [quo/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
          [rn/view
           {:style style/title-container}
           [quo/text {:weight :medium} (i18n/label :t/recent)]
           [rn/view {:style (style/chevron-container)}
            [quo/icon :i/chevron-down {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]
          [clear-button]
          [rn/flat-list
           {:key-fn                  identity
            :render-fn               image
            :render-data             {:window-width window-width}
            :data                    camera-roll-photos
            :num-columns             3
            :content-container-style {:width          "100%"
                                      :padding-bottom (+ (:bottom safe-area) 100)}
            :style                   {:border-radius 20}
            :on-end-reached          #(rf/dispatch [:camera-roll/on-end-reached end-cursor loading?
                                                    has-next-page?])}]
          [bottom-gradient chat-id selected-images]]))]))


