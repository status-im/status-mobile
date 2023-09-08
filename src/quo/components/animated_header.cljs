(ns quo.components.animated-header
  (:require [oops.core :refer [oget]]
            [quo.animated :as animated]
            [quo.components.header :as header]
            [quo.design-system.colors :as colors]
            [quo.platform :as platform]
            [reagent.core :as reagent]
            [react-native.safe-area :as safe-area]))

(defn header-wrapper-style
  [{:keys [value offset]}]
  (merge
   {:background-color (:ui-background @colors/theme)}
   (when (and offset platform/android?)
     {:elevation 2})
   (when (and offset platform/ios?)
     {:z-index        2
      :shadow-opacity 0.4
      :shadow-radius  16
      :shadow-color   (:shadow-01 @colors/theme)
      :shadow-offset  {:width 0 :height 4}})))

(defn title-style
  [layout]
  {:flex            1
   :justify-content :center
   :padding-right   (get-in layout [:right :width])})

(defn header-container
  []
  (let [y               0 ;;(animated/value 0)
        animation-value 0 ;;(animated/value 0)
        ;        animation       (animated/with-timing-transition
        ;                         animation-value
        ;                         {:duration 250
        ;                          :easing   (:ease-in animated/easings)})
;        on-scroll       (animated/on-scroll {:y y})
        layout          (reagent/atom {})
        offset          (reagent/atom 0)
        on-layout       (fn [evt]
                          (reset! offset (oget evt "nativeEvent" "layout" "height")))]
    (fn [{:keys [extended-header refresh-control refreshing-sub refreshing-counter] :as props} children]
      [animated/view
       {:flex           1
        :pointer-events :box-none}
       ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
       ;TODO: replace this with an updated implementation
       ;       [animated/code
       ;        {:key  (str @offset)
       ;         :exec (animated/cond*
       ;                (animated/and* (animated/greater-or-eq y @offset)
       ;                               (animated/greater-or-eq y 1))
       ;                (animated/set animation-value 1)
       ;                (animated/set animation-value 0))}]
       [animated/view
        {:pointer-events :box-none
         :style          (header-wrapper-style {:value  y
                                                :offset @offset})}
        [header/header
         (merge
          {:get-layout      (fn [el l] (swap! layout assoc el l))
           :border-bottom   false
           :title-component [animated/view {:style (title-style @layout)}
                             [extended-header
                              {:value     y
                               ;                               :animation animation
                               :minimized true
                               :offset    @offset}]]
           :title-align     :left}
          (dissoc props :extended-header))]]
              (into [animated/scroll-view
                     {
;                       :on-scroll           on-scroll
                      :refreshControl      (when refresh-control
                                             (refresh-control
                                              (and @refreshing-sub
                                                   @refreshing-counter)))
                      :style               {:z-index 1}
                      :scrollEventThrottle 16}
                     [animated/view {:pointer-events :box-none}
                      [animated/view
                       {:pointer-events :box-none
                        :on-layout      on-layout}
                       [extended-header
                        {:value     y
       ;                  :animation animation
                         :offset    @offset}]]]]
                    children)
      ])))

(defn header
  [{:keys [use-insets] :as props} & children]
  (if use-insets
    [header-container
     (-> props
         (dissoc :use-insets)
         (assoc :insets (safe-area/get-insets)))
     children]
    [header-container props children]))
