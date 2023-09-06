(ns quo2.components.loaders.skeleton-list.view
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [react-native.masked-view :as masked-view]
            [react-native.reanimated :as reanimated]
            [quo2.components.loaders.skeleton-list.style :as style]
            [reagent.core :as reagent]
            [quo2.components.loaders.skeleton-list.constants :as constants]))

(defn static-skeleton-view
  [{:keys [index content color]}]
  [rn/view
   {:style (style/container content)}
   [rn/view {:style (style/avatar color)}]
   [rn/view {:style style/content-container}
    [rn/view
     {:style (style/content-view
              {:type    (if (= content :list-items) :message :author)
               :index   index
               :content content
               :color   color})}]
    [rn/view
     {:style (style/content-view
              {:type    (if (= content :list-items) :author :message)
               :index   index
               :content content
               :color   color})}]
    (when (= content :notifications)
      [rn/view
       {:style (style/content-view {:type    :message2
                                    :index   index
                                    :content content
                                    :color   color})}])]])

(defn- f-animated-skeleton-view
  [{:keys [style color skeleton-height animated? translate-x window-width theme] :as data}]
  (let [loading-color (colors/theme-colors colors/neutral-10 colors/neutral-60 theme)]

    (rn/use-effect
     (fn []
       (when-not animated?
         (reanimated/cancel-animation translate-x))
       (reanimated/animate-shared-value-with-repeat translate-x window-width 1000 :linear -1 false)
       #(when-not animated?
          (reanimated/cancel-animation translate-x)))
     [animated?])


    [masked-view/masked-view
     {:style        {:height skeleton-height}
      :mask-element (reagent/as-element [static-skeleton-view data])}
     [rn/view
      {:style {:flex             1
               :background-color color}}
      [reanimated/linear-gradient
       {:colors [color color loading-color color color]
        :start  {:x 0 :y 0}
        :end    {:x 1 :y 0}
        :style  style}]]]))

(defn- animated-skeleton-view
  [props]
  [:f> f-animated-skeleton-view props])

(defn- f-internal-view
  [{:keys [content theme blur? parent-height animated?] :as props}]
  (let [{window-width :width}   (rn/get-window)
        translate-x             (reanimated/use-shared-value (- window-width))
        animated-gradient-style (reanimated/apply-animations-to-style
                                 {:transform [{:translateX translate-x}]}
                                 {:width  window-width
                                  :height "100%"})
        skeleton-height         (get-in constants/layout-dimensions [content :height])
        number-of-skeletons     (int (Math/ceil (/ parent-height skeleton-height)))
        color                   (if (and blur? (= theme :dark))
                                  colors/white-opa-5
                                  (colors/theme-colors colors/neutral-5 colors/neutral-90 theme))
        component               (if animated? animated-skeleton-view static-skeleton-view)]
    [rn/view
     {:style               {:padding 8}
      :accessibility-label :skeleton-list}
     (for [parent-index (range number-of-skeletons)]
       ^{:key parent-index}
       [component
        (merge props
               {:index           (mod parent-index 4)
                :parent-index    parent-index
                :color           color
                :translate-x     translate-x
                :skeleton-height skeleton-height
                :style           animated-gradient-style})])]))

(defn- internal-view
  [props]
  [:f> f-internal-view props])

(def view (theme/with-theme internal-view))
