(ns status-im.ui2.screens.chat.photo-selector.view
  (:require [i18n.i18n :as i18n]
            [quo.components.safe-area :as safe-area]
            [quo2.components.notifications.info-count :as info-count]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [reagent.core :as reagent]
            [status-im.ui2.screens.chat.photo-selector.style :as style]
            [status-im.utils.core :as utils]
            [utils.re-frame :as rf]))

(def selected (reagent/atom []))

(defn bottom-gradient
  [chat-id selected-images]
  [:f>
   (fn []
     (let [safe-area (safe-area/use-safe-area)]
       (when (or (pos? (count @selected)) selected-images)
         [linear-gradient/linear-gradient
          {:colors [:black :transparent]
           :start  {:x 0 :y 1}
           :end    {:x 0 :y 0}
           :style  (style/gradient-container safe-area)}
          [quo2/button
           {:style    {:align-self        :stretch
                       :margin-horizontal 20}
            :on-press #(do
                         (rf/dispatch [:chat.ui/clear-sending-images chat-id])
                         (doseq [item @selected]
                           (rf/dispatch [:chat.ui/camera-roll-pick item]))
                         (reset! selected [])
                         (rf/dispatch [:bottom-sheet/hide]))}
           (i18n/label :t/confirm-selection)]])))])

(defn clear-button
  []
  (when (pos? (count @selected))
    [rn/touchable-opacity
     {:on-press #(reset! selected [])
      :style    (style/clear-container)}
     [quo2/text {:weight :medium} (i18n/label :t/clear)]]))

(defn image
  [item index _ {:keys [window-width]}]
  [rn/touchable-opacity
   {:active-opacity 1
    :on-press       (fn []
                      (if (some #{item} @selected)
                        (reset! selected (vec (remove #(= % item) @selected)))
                        (swap! selected conj item)))}
   [rn/image
    {:source {:uri item}
     :style  (style/image window-width index)}]
   (when (some #{item} @selected)
     [rn/view {:style (style/overlay window-width)}])
   (when (some #{item} @selected)
     [info-count/info-count {:style style/image-count}
      (inc (utils/first-index #(= item %) @selected))])])

(defn photo-selector
  [chat-id]
  (rf/dispatch [:chat.ui/camera-roll-get-photos 20])
  (let [selected-images (keys (get-in (rf/sub [:chat/inputs]) [chat-id :metadata :sending-image]))]
    (when selected-images
      (reset! selected (vec selected-images)))
    [:f>
     (fn []
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
           [quo2/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
          [rn/view
           {:style {:flex-direction :row
                    :position       :absolute
                    :align-self     :center}}
           [quo2/text {:weight :medium} (i18n/label :t/recent)]
           [rn/view {:style (style/chevron-container)}
            [quo2/icon :i/chevron-down {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]
          [clear-button]
          [rn/flat-list
           {:key-fn                  (fn [item] item)
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

