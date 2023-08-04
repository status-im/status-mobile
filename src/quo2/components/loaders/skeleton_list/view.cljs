(ns quo2.components.loaders.skeleton-list.view
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [react-native.masked-view :as masked-view]
            [react-native.reanimated :as reanimated]
            [quo2.components.loaders.skeleton-list.style :as style]
            [reagent.core :as reagent]
            [quo2.components.loaders.skeleton-list.constants :as constants]))

(defn skeleton-label
  [animated? index parent-index]
  (str (if animated? "skeleton-animated-" "skeleton-static-")
       parent-index
       "-"
       index))

(defn static-skeleton-view
  [{:keys [index parent-index content color]}]
  [rn/view
   {:style               (style/container content)
    :accessibility-label (skeleton-label false index parent-index)}
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
               :color   color})}]]])

(defn animated-skeleton-view
  [{:keys [index parent-index content animated? style theme blur?] :as data}]
  (let [loading-color  (colors/theme-colors colors/neutral-10 colors/neutral-60)
        skeleton-color (cond
                         blur?           colors/white-opa-5
                         (= theme :dark) colors/neutral-90
                         :else           colors/neutral-5)]
    [masked-view/masked-view
     {:style               {:height (get-in constants/layout-dimensions [content :height])}
      :accessibility-label (skeleton-label true index parent-index)
      :maskElement         (reagent/as-element (static-skeleton-view data))}

     [rn/view
      {:style {:flex             1
               :background-color skeleton-color}}
      (when animated?
        [reanimated/linear-gradient
         {:colors [skeleton-color skeleton-color loading-color skeleton-color skeleton-color]
          :start  {:x 0 :y 0}
          :end    {:x 1 :y 0}
          :style  style}])]]))

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
        color                   (cond
                                  blur?           colors/white-opa-5
                                  (= theme :dark) colors/neutral-90
                                  :else           colors/neutral-5)
        component               (if animated? animated-skeleton-view static-skeleton-view)]

    (rn/use-effect
     (fn []
       (when animated?
         (reanimated/animate-shared-value-with-repeat
          translate-x
          window-width
          1000
          :linear
          -1
          false))
       #(when-not animated?
          (reanimated/cancel-animation translate-x))
       [animated?]))

    [rn/view {:style {:padding 8}}
     (doall
      (for [parent-index (range number-of-skeletons)]
        ^{:key parent-index}
        [component
         (merge props
                {:index        (mod parent-index 4)
                 :parent-index parent-index
                 :color        color
                 :translate-x  translate-x
                 :style        animated-gradient-style})]))]))

(defn- internal-view
  [props]
  [:f> f-internal-view props])

(def view (theme/with-theme internal-view))
