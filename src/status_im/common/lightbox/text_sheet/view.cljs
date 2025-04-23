(ns status-im.common.lightbox.text-sheet.view
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.lightbox.constants :as constants]
    [status-im.common.lightbox.text-sheet.style :as style]
    [status-im.common.lightbox.text-sheet.utils :as utils]))

(defn- text-sheet
  [_]
  (let [text-height (reagent/atom 0)
        expanded?   (reagent/atom false)
        dragging?   (atom false)]
    (fn [{:keys [overlay-opacity overlay-z-index text-sheet-lock? text-component]}]
      (let [insets           safe-area/insets
            window-height    (:height (rn/get-window))
            max-height       (- window-height
                                constants/text-min-height
                                constants/top-view-height
                                (:bottom insets)
                                (when platform/ios? (:top insets)))
            full-height      (+ constants/bar-container-height
                                constants/text-margin
                                constants/line-height
                                @text-height)
            expanded-height  (min max-height full-height)
            animations       (utils/init-animations overlay-opacity)
            derived          (utils/init-derived-animations animations)
            expandable-text? (> @text-height (* constants/line-height 2))]
        [rn/view
         [reanimated/linear-gradient
          {:colors         [colors/neutral-100-opa-0 colors/neutral-100]
           :pointer-events :none
           :locations      [0 0.3]
           :start          {:x 0 :y 1}
           :end            {:x 0 :y 0}
           :style          (style/top-gradient animations derived insets max-height)}]
         [gesture/gesture-detector
          {:gesture (utils/sheet-gesture animations
                                         expanded-height
                                         max-height
                                         full-height
                                         overlay-z-index
                                         expanded?
                                         dragging?
                                         expandable-text?)}
          [gesture/gesture-detector
           {:gesture (-> (gesture/gesture-tap)
                         (gesture/enabled (and expandable-text? (not @expanded?)))
                         (gesture/on-start (fn []
                                             (utils/expand-sheet animations
                                                                 expanded-height
                                                                 max-height
                                                                 overlay-z-index
                                                                 expanded?
                                                                 text-sheet-lock?))))}
           [reanimated/view {:style (style/sheet-container derived)}
            (when expandable-text?
              [rn/view {:style style/bar-container}
               [rn/view {:style style/bar}]])
            [linear-gradient/linear-gradient
             {:colors    [colors/neutral-100-opa-100 colors/neutral-100-opa-70 colors/neutral-100-opa-0]
              :start     {:x 0 :y 1}
              :end       {:x 0 :y 0}
              :locations [0.7 0.8 1]
              :style     (style/bottom-gradient (:bottom insets))}]

            [gesture/scroll-view
             {:scroll-enabled          false
              :scroll-event-throttle   16
              :bounces                 false
              :style                   {:height (- max-height constants/bar-container-height)}
              :content-container-style {:padding-top (when (not expandable-text?)
                                                       constants/bar-container-height)}}
             [rn/view {:on-layout #(utils/on-layout % text-height)}
              text-component]]]]]]))))

(defn view
  [props]
  [:f> text-sheet props])
