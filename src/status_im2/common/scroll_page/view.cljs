(ns status-im2.common.scroll-page.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.common.scroll-page.style :as style]
            [utils.re-frame :as rf]))

(defn icon-color
  []
  (colors/theme-colors
   colors/white-opa-40
   colors/neutral-80-opa-40))

(defn get-platform-value [value] (if platform/ios? (+ value 44) value))
(def negative-scroll-position-0 (if platform/ios? -44 0))
(def scroll-position-0 (if platform/ios? 44 0))
(def scroll-position-1 (if platform/ios? 86 134))
(def scroll-position-2 (if platform/ios? -26 18))

(defn get-header-size
  [scroll-height]
  (if (<= scroll-height scroll-position-2)
    0
    (->>
     (+ (get-platform-value -17) scroll-height)
     (* (if platform/ios? 3 1))
     (max 0)
     (min (if platform/ios? 100 124)))))

(def max-image-size 80)
(def min-image-size 32)

(defn diff-with-max-min
  [value maximum minimum]
  (->>
   (+ value scroll-position-0)
   (- maximum)
   (max minimum)
   (min maximum)))

(defn icon-top-fn
  [scroll-height]
  (if (<= scroll-height negative-scroll-position-0)
    -40
    (->> (+ scroll-position-0 scroll-height)
         (* (if platform/ios? 3 1))
         (+ -40)
         (min 8))))

(defn icon-size-fn
  [scroll-height]
  (->> (+ scroll-position-0 scroll-height)
       (* (if platform/ios? 3 1))
       (- max-image-size)
       (max min-image-size)
       (min max-image-size)))

(defn scroll-page
  [icon cover page-nav name]
  (let [scroll-height (reagent/atom negative-scroll-position-0)]
    (fn [sticky-header children]
      [:<>
       [:<>
        [rn/image
         {:source   cover
          :position :absolute
          :style    (style/image-slider (get-header-size @scroll-height))}]
        [blur/view (style/blur-slider (get-header-size @scroll-height))]]
       [rn/view {:style {:z-index 6 :margin-top (if platform/ios? 44 0)}}
        [quo/page-nav
         {:horizontal-description? true
          :one-icon-align-left?    true
          :align-mid?              false
          :page-nav-color          :transparent
          :page-nav-background-uri ""
          :mid-section             {:type            :text-with-description
                                    :main-text       (when (>= @scroll-height scroll-position-1) name)
                                    :description-img (when (>= @scroll-height scroll-position-1) icon)}
          :right-section-buttons   (:right-section-buttons page-nav)
          :left-section            {:icon                  :i/close
                                    :icon-background-color (icon-color)
                                    :on-press              #(rf/dispatch [:navigate-back])}}]
        (when sticky-header [sticky-header @scroll-height])]
       [rn/scroll-view
        {:style (style/scroll-view-container (diff-with-max-min @scroll-height 16 0))
         :shows-vertical-scroll-indicator false
         :scroll-event-throttle 4
         :on-scroll #(swap! scroll-height (fn [] (int (oops/oget % "nativeEvent.contentOffset.y"))))}
        [rn/view {:style {:height 151}}
         [rn/image
          {:source cover
           :style  {:overflow :visible
                    :flex     1}}]]
        (when children
          [rn/view
           {:flex             1
            :border-radius    (diff-with-max-min @scroll-height 16 0)
            :background-color (colors/theme-colors
                               colors/white
                               colors/neutral-90)}
           [children @scroll-height icon-top-fn icon-size-fn]])]])))

