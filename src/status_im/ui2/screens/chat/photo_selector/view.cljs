(ns status-im.ui2.screens.chat.photo-selector.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo.components.safe-area :as safe-area]
            [quo2.core :as quo2]
            [i18n.i18n :as i18n]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo2.components.notifications.info-count :as info-count]
            [react-native.linear-gradient :as linear-gradient]
            [status-im.ui2.screens.chat.photo-selector.style :as style]
            [status-im.utils.core :as utils]))

(def selected (reagent/atom []))

(defn bottom-gradient []
  [:f>
   (fn []
     (let [safe-area (safe-area/use-safe-area)]
       (when (pos? (count @selected))
         [linear-gradient/linear-gradient
          {:colors [:black :transparent]
           :start  {:x 0 :y 1}
           :end    {:x 0 :y 0}
           :style  (style/gradient-container safe-area)}
          [quo2/button {:style    {:width "95%"
                                   :align-self :center}
                        :on-press #(do
                                     ;(rf/dispatch [:chat.ui/send-current-message])
                                     (reset! selected [])
                                     (rf/dispatch [:bottom-sheet/hide]))}
           (i18n/label :t/confirm-selection)]])))])

(defn clear-button []
  (when (pos? (count @selected))
    [rn/touchable-opacity {:on-press #(reset! selected [])
                           :style    (style/clear-container)}
     [quo2/text {:weight :medium} (i18n/label :t/clear)]]))

(defn image [item index window-width]
  [rn/touchable-opacity
   {:active-opacity 1
    :on-press       (fn []
                      (if (some #{item} @selected)
                        (do
                          (reset! selected (vec (remove #(= % item) @selected)))
                          (rf/dispatch [:chat.ui/image-unselected item]))
                        (do
                          (swap! selected conj item)
                          (rf/dispatch [:chat.ui/camera-roll-pick item]))))}
   [rn/image {:source {:uri item}
              :style  (style/image window-width index)}]
   (when (some #{item} @selected)
     [rn/view {:style (style/overlay window-width)}])
   (when (some #{item} @selected)
     [info-count/info-count (+ (utils/first-index #(= item %) @selected) 1) (style/image-count)])])

(defn on-end-reached [end-cursor]
  (let [is-loading    (rf/sub [:camera-roll-loading-more])
        has-next-page (rf/sub [:camera-roll-has-next-page])]
    (when (and (not is-loading) has-next-page)
      (rf/dispatch [:chat.ui/camera-roll-loading-more true])
      (rf/dispatch [:chat.ui/camera-roll-get-photos 20 end-cursor]))))

(defn photo-selector []
  (rf/dispatch [:chat.ui/camera-roll-get-photos 20])
  [:f>
   (fn []
     (let [{window-height :height window-width :width} (rn/use-window-dimensions)
           safe-area          (safe-area/use-safe-area)
           camera-roll-photos (rf/sub [:camera-roll-photos])
           end-cursor         (rf/sub [:camera-roll-end-cursor])]
       [rn/view {:style {:height (- window-height (:top safe-area))}}
        [rn/touchable-opacity
         {:on-press #(js/alert "Camera: not implemented")
          :style    (style/camera-button-container)}
         [quo2/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
        [rn/view {:style {:flex-direction :row
                          :position       :absolute
                          :align-self     :center}}
         [quo2/text {:weight :medium} (i18n/label :t/recent)]
         [rn/view {:style (style/chevron-container)}
          [quo2/icon :i/chevron-down {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]
        [clear-button]
        [rn/flat-list {:key-fn                  (fn [item] item)
                       :render-fn               (fn [item index] (image item index window-width))
                       :data                    camera-roll-photos
                       :num-columns             3
                       :content-container-style {:width          "100%"
                                                 :padding-bottom (+ (:bottom safe-area) 100)}
                       :style                   {:border-radius 20}
                       :on-end-reached          (fn []
                                                  (on-end-reached end-cursor))}]
        [bottom-gradient]]))])
