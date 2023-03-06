(ns status-im2.common.scroll-page.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.common.scroll-page.style :as style]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]))

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
  [scroll-height
   height
   name
   page-nav
   logo
   sticky-header
   top-nav
   title-colum
   navigate-back?]
  (let [input-range         (if platform/ios? [-47 10] [0 10])
        output-range        (if platform/ios? [-208 0] [-208 -45])
        y                   (reanimated/use-shared-value scroll-height)
        translate-animation (reanimated/interpolate y
                                                    input-range
                                                    output-range
                                                    {:extrapolateLeft  "clamp"
                                                     :extrapolateRight "clamp"})
        opacity-animation   (reanimated/use-shared-value 0)
        threshold           (if platform/ios? 30 170)]
    (rn/use-effect
     #(do
        (reanimated/set-shared-value y scroll-height)
        (reanimated/set-shared-value opacity-animation
                                     (reanimated/with-timing (if (>= scroll-height threshold) 1 0)
                                                             (clj->js {:duration 300}))))
     [scroll-height])
    [:<>
     [reanimated/blur-view
      {:blur-amount   20
       :blur-type     :transparent
       :overlay-color :transparent
       :style         (style/blur-slider translate-animation height)}]
     [rn/view
      {:style {:z-index  6
               :position :absolute
               :top      0
               :left     0
               :right    0}}
      (when logo
        [reanimated/view
         {:style (style/sticky-header-title opacity-animation)}
         [rn/image
          {:source logo
           :style  style/sticky-header-image}]
         [quo/text
          {:size   :paragraph-1
           :weight :semi-bold
           :style  {:line-height 21}}
          name]])
      (if top-nav
        [rn/view {:style {:margin-top (if platform/ios? 44 0)}}
         top-nav]
        [rn/view {:style {:margin-top 44}}
         [quo/page-nav
          (merge {:horizontal-description? true
                  :one-icon-align-left?    true
                  :align-mid?              false
                  :page-nav-color          :transparent
                  :mid-section             {:type            :text-with-description
                                            :main-text       nil
                                            :description-img nil}
                  :right-section-buttons   page-nav}
                 (when navigate-back?
                   {:left-section {:icon                  :i/close
                                   :icon-background-color (icon-color)
                                   :on-press              #(rf/dispatch [:navigate-back])}}))]])
      (when title-colum
        title-colum)
      sticky-header]]))


(defn display-picture
  [scroll-height cover]
  (let [input-range (if platform/ios? [-67 10] [0 150])
        y           (reanimated/use-shared-value scroll-height)
        animation   (reanimated/interpolate y
                                            input-range
                                            [1.2 0.5]
                                            {:extrapolateLeft  "clamp"
                                             :extrapolateRight "clamp"})]
    (rn/use-effect #(do
                      (reanimated/set-shared-value y scroll-height)
                      js/undefined)
                   [scroll-height])
    [reanimated/view
     {:style (style/display-picture-container animation)}
     [rn/image
      {:source cover
       :style  style/display-picture}]]))

(defn scroll-page
  [_ _ _]
  (let [scroll-height (reagent/atom negative-scroll-position-0)]
    (fn
      [{:keys [cover-image
               logo
               page-nav-right-section-buttons
               name
               on-scroll
               height
               top-nav
               title-colum
               background-color
               navigate-back?]}
       sticky-header
       children]
      [:<>
       [:f> scroll-page-header @scroll-height height name page-nav-right-section-buttons
        logo sticky-header top-nav title-colum navigate-back?]
       [rn/scroll-view
        {:content-container-style         (style/scroll-view-container
                                           (diff-with-max-min @scroll-height 16 0))
         :shows-vertical-scroll-indicator false
         :scroll-event-throttle           16
         :on-scroll                       (fn [^js event]
                                            (reset! scroll-height (int
                                                                   (oops/oget
                                                                    event
                                                                    "nativeEvent.contentOffset.y")))
                                            (when on-scroll
                                              (on-scroll @scroll-height)))}
        (when cover-image
          [rn/view {:style {:height 151}}
           [rn/image
            {:source cover-image
             ;; Using negative margin-bottom as a workaround because on Android,
             ;; ScrollView clips its children despite setting overflow: 'visible'.
             ;; Related issue: https://github.com/facebook/react-native/issues/31218
             :style  {:margin-bottom -16
                      :flex          1}}]])
        (when children
          [rn/view
           {:flex             1
            :border-radius    (diff-with-max-min @scroll-height 16 0)
            :background-color background-color}
           (when cover-image
             [:f> display-picture @scroll-height logo])
           children])]])))
