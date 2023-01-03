(ns status-im2.common.scroll-page.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo.react]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.common.scroll-page.style :as style]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [quo.animated :as animated]))

(defn icon-color
  []
  (colors/theme-colors
   colors/white-opa-40
   colors/neutral-80-opa-40))

(def negative-scroll-position-0 (if platform/ios? -44 0))
(def scroll-position-0 (if platform/ios? 44 0))

(defn diff-with-max-min
  [value maximum minimum]
  (->>
   (+ value scroll-position-0)
   (- maximum)
   (max minimum)
   (min maximum)))

(defn scroll-page-header
  [scroll-height name page-nav cover sticky-header]
  [:f>
   (fn []
     (let [input-range         (if platform/ios? [-47 10] [0 150])
           output-range        (if platform/ios? [-100 0] [-169 -45])
           y                   (animated/value @scroll-height)
           translate-animation (animated/interpolate y
                                                     {:inputRange  input-range
                                                      :outputRange output-range
                                                      :extrapolate (:clamp animated/extrapolate)})
           opacity-animation   (reanimated/use-shared-value 0)
           threshold           (if platform/ios? 30 170)]
       (quo.react/effect!
        #(reanimated/set-shared-value opacity-animation
                                      (reanimated/with-timing (if (>= @scroll-height threshold) 1 0))))
       [:<>
        [animated/blur-view
         {:blur-amount   32
          :blur-type     :xlight
          :overlay-color (if platform/ios? colors/white-opa-70 :transparent)
          :style         (merge
                          {:transform [{:translateY translate-animation}]}
                          style/blur-slider)}]
        [rn/view {:style {:z-index 6 :margin-top (if platform/ios? 44 0)}}
         [reanimated/view
          {:style (reanimated/apply-animations-to-style
                   {:opacity opacity-animation}
                   style/sticky-header-title)}
          [rn/image
           {:source cover
            :style  style/sticky-header-image}]
          [quo/text
           {:size   :paragraph-1
            :weight :semi-bold
            :style  {:line-height 21}}
           name]]
         [quo/page-nav
          {:horizontal-description? true
           :one-icon-align-left?    true
           :align-mid?              false
           :page-nav-color          :transparent
           :page-nav-background-uri ""
           :mid-section             {:type            :text-with-description
                                     :main-text       nil
                                     :description-img nil}
           :right-section-buttons   (:right-section-buttons page-nav)
           :left-section            {:icon                  :i/close
                                     :icon-background-color (icon-color)
                                     :on-press              #(rf/dispatch [:navigate-back])}}]
         (when sticky-header [sticky-header @scroll-height])
        ]]))])

(defn scroll-page
  [cover page-nav name]
  (let [scroll-height (reagent/atom negative-scroll-position-0)
        input-range   (if platform/ios? [-67 10] [0 150])]
    (fn [sticky-header children]
<<<<<<< HEAD
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
=======
      [:f>
       (fn []
         (let [y         (animated/value @scroll-height)
               animation (animated/interpolate y
                                               {:inputRange  input-range
                                                :outputRange [1.2 0.5]
                                                :extrapolate (:clamp animated/extrapolate)})]
           [:<>
            [scroll-page-header scroll-height name page-nav cover sticky-header]
            [rn/scroll-view
             {:style                           (style/scroll-view-container
                                                (diff-with-max-min @scroll-height 16 0))
              :shows-vertical-scroll-indicator false
              :scroll-event-throttle           4
              :on-scroll                       #(swap! scroll-height
                                                  (fn []
                                                    (int (oops/oget % "nativeEvent.contentOffset.y"))))}
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

                [animated/view
                 {:style (style/display-picture-container animation)}
                 [rn/image
                  {:source cover
                   :style  style/display-picture}]]
                [children]])]]))])))
>>>>>>> bad96c919... feat: scroll page animations

