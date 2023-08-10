(ns quo2.components.loaders.skeleton-list.view
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [quo2.components.loaders.skeleton-list.style :as style]
            [quo2.components.loaders.skeleton-list.constants :as constants]))

(defn get-content-width
  [content-type index]
  (let [content-data (get constants/content-dimensions content-type)
        dimensions   (get content-data index)
        widths       (map (fn [content-key] (get-in dimensions [content-key :width])) (keys dimensions))]
    (reduce + widths)))

(defn skeleton-item
  [{:keys [index parent-index content color animated?]}]
  [:f>
   (fn []
     (let [content-height (get-in constants/layout-dimensions [content :height])
           content-width (get-content-width content index)
           {window-width :width} (rn/get-window)
           loading-color (colors/theme-colors colors/neutral-10 colors/neutral-60)
           translate-x (reanimated/use-shared-value (- window-width))
           animated-gradient-style
           (reanimated/apply-animations-to-style
            {:transform [{:translateX translate-x}]}
            {:width  window-width
             :height content-height})]

       (when animated?
         (reanimated/animate-shared-value-with-repeat translate-x window-width 1000 :linear (- 1) false))

       [rn/view
        {:style               (style/container content)
         :accessibility-label (str (if animated? "skeleton-animated-" "skeleton-static-")
                                   index
                                   "-"
                                   parent-index)}

        ;; Avatar and its gradient
        [rn/view {:style (style/avatar color)}]

        ;; Content views and their gradients
        [rn/view {:style style/content-container}

         [rn/view {:style {:position "relative" :overflow :hidden}}
          [rn/view
           {:style (style/content-view {:type    (if (= content :list-items) :message :author)
                                        :index   index
                                        :content content
                                        :color   color})}]]

         [rn/view {:style {:position "relative" :overflow :hidden}}
          [rn/view
           {:style (style/content-view {:type    (if (= content :list-items) :author :message)
                                        :index   index
                                        :content content
                                        :color   color})}]

          [rn/view
           {:style {:flex             1
                    :background-color color
                    :top              0
                    :left             0
                    :position         "absolute"
                    ;; :overflow         :visible
                   }}
           (when animated?
             [rn/view
              [reanimated/linear-gradient
               {:colors [color color loading-color color color]
                :start  {:x 0 :y 0}
                :end    {:x 1 :y 0}
                :style  animated-gradient-style}]])]]]]))])

(defn- internal-view
  [{:keys [content theme blur? parent-height animated?] :as props}]
  (let [skeleton-height     (get-in constants/layout-dimensions [content :height])
        number-of-skeletons (int (Math/ceil (/ parent-height skeleton-height)))
        color               (cond
                              blur?           colors/white-opa-5
                              (= theme :dark) colors/neutral-90
                              :else           colors/neutral-5)]

    [rn/view {:style {:padding 8}}
     (doall
      (for [parent-index (range number-of-skeletons)]
        ^{:key parent-index}
        [skeleton-item
         (merge props
                {:index        (mod parent-index 4)
                 :parent-index parent-index
                 :animated?    animated?
                 :color        color})]))]))

(def view (theme/with-theme internal-view))
