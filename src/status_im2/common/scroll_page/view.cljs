(ns status-im2.common.scroll-page.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.scroll-page.style :as style]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]))

(def negative-scroll-position-0 0)
(def scroll-position-0 0)

(defn diff-with-max-min
  [value maximum minimum]
  (->>
    (+ value scroll-position-0)
    (- maximum)
    (max minimum)
    (min maximum)))

(defn page-header-threshold
  [collapsed?]
  (if collapsed? 50 170))

(defn f-scroll-page-header
  [scroll-height height name page-nav-right-side logo sticky-header top-nav title-colum navigate-back?
   collapsed?]
  (let [input-range         [0 10]
        output-range        [-208 -45]
        y                   (reanimated/use-shared-value scroll-height)
        translate-animation (reanimated/interpolate y
                                                    input-range
                                                    output-range
                                                    {:extrapolateLeft  "clamp"
                                                     :extrapolateRight "clamp"})
        opacity-animation   (reanimated/use-shared-value 0)
        threshold           (page-header-threshold collapsed?)]
    (rn/use-effect
     (fn []
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
        [rn/view {:style {:margin-top 0}}
         top-nav]
        [quo/page-nav
         (cond-> {:margin-top 44
                  :type       :no-title
                  :background (if (= 1 (reanimated/get-shared-value opacity-animation))
                                :blur
                                :photo)
                  :right-side page-nav-right-side}
           navigate-back? (assoc :icon-name :i/close
                                 :on-press  #(rf/dispatch [:navigate-back])))])
      (when title-colum
        title-colum)
      sticky-header]]))


(defn f-display-picture
  [scroll-height cover theme]
  (let [input-range [0 150]
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
       :style  (style/display-picture theme)}]]))

(defn scroll-page
  [_ _ _]
  (let [scroll-height (reagent/atom negative-scroll-position-0)]
    (fn [{:keys [name theme cover-image logo page-nav-right-section-buttons on-scroll
                 collapsed?
                 height top-nav title-colum background-color navigate-back?]}
         sticky-header
         children]
      [:<>
       [:f> f-scroll-page-header @scroll-height height name page-nav-right-section-buttons
        logo sticky-header top-nav title-colum navigate-back? collapsed?]
       [rn/scroll-view
        {:content-container-style           {:flex-grow 1}
         :content-inset-adjustment-behavior :never
         :shows-vertical-scroll-indicator   false
         :scroll-event-throttle             16
         :on-scroll                         (fn [^js event]
                                              (reset! scroll-height (int
                                                                     (oops/oget
                                                                      event
                                                                      "nativeEvent.contentOffset.y")))
                                              (when on-scroll
                                                (on-scroll @scroll-height)))}
        (when cover-image
          [rn/view {:style {:height (if collapsed? 110 151)}}
           [rn/image
            {:source cover-image
             ;; Using negative margin-bottom as a workaround because on Android,
             ;; ScrollView clips its children despite setting overflow: 'visible'.
             ;; Related issue: https://github.com/facebook/react-native/issues/31218
             :style  {:margin-bottom -16
                      :flex          1}}]])
        (when children
          [rn/view
           {:style (style/children-container {:border-radius    (diff-with-max-min @scroll-height 16 0)
                                              :background-color background-color})}
           (when (and (not collapsed?) cover-image)
             [:f> f-display-picture @scroll-height logo theme])
           children])]])))
