(ns legacy.status-im.ui.components.animated-header
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.header :as header]
    [oops.core :refer [oget]]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]))

(defn header-wrapper-style
  [{:keys [offset]}]
  (merge
   {:background-color (:ui-background @colors/theme)}
   (when (and offset platform/ios?)
     {:z-index       2
      :shadow-radius 16
      :shadow-color  (:shadow-01 @colors/theme)
      :shadow-offset {:width 0 :height 4}})))

(defn title-style
  [layout]
  {:flex            1
   :justify-content :center
   :padding-right   (get-in layout [:right :width])})

(defn header-container
  []
  (let [layout    (reagent/atom {})
        offset    (reagent/atom 0)
        on-layout (fn [evt]
                    (reset! offset (oget evt "nativeEvent" "layout" "height")))]
    (fn [{:keys [extended-header refresh-control refreshing-sub refreshing-counter] :as props} children]
      [rn/view
       {:flex           1
        :pointer-events :box-none}
       [rn/view
        {:pointer-events :box-none
         :style          (header-wrapper-style {:offset @offset})}
        [header/header
         (merge
          {:get-layout    (fn [el l] (swap! layout assoc el l))
           :border-bottom false
           :title-align   :left}
          (dissoc props :extended-header))]]
       (into [rn/scroll-view
              {:refreshControl      (when refresh-control
                                      (refresh-control
                                       (and @refreshing-sub
                                            @refreshing-counter)))
               :style               {:z-index 1}
               :scrollEventThrottle 16}
              [rn/view {:pointer-events :box-none}
               [rn/view
                {:pointer-events :box-none
                 :on-layout      on-layout}
                [extended-header
                 {:offset @offset}]]]]
             children)])))

(defn header
  [{:keys [use-insets] :as props} & children]
  (if use-insets
    [header-container
     (-> props
         (dissoc :use-insets)
         (assoc :insets (safe-area/get-insets)))
     children]
    [header-container props children]))
