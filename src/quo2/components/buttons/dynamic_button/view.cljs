(ns quo2.components.buttons.dynamic-button.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.theme :as quo.theme]
            [quo2.components.buttons.dynamic-button.style :as style]))

(defn- get-button-color
  [{:keys [type pressed? customization-color theme]}]
  (if (#{:jump-to :mention} type)
    (if pressed?
      (colors/theme-colors
       (colors/custom-color customization-color 60)
       (colors/custom-color customization-color 50)
       theme)
      (colors/theme-colors
       (colors/custom-color customization-color 50)
       (colors/custom-color customization-color 60)
       theme))
    (if pressed?
      (colors/theme-colors colors/neutral-80-opa-80 colors/white-opa-80 theme)
      (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme))))

(defn- get-icon-and-text-color
  [type theme]
  (if (#{:jump-to :mention} type)
    colors/white
    (colors/theme-colors colors/white colors/neutral-100 theme)))

(defn- icon-view
  [type theme]
  [icon/icon
   (case type
     :jump-to           :i/jump-to
     :mention           :i/mention
     :notification-down :i/arrow-down
     :notification-up   :i/arrow-up
     :search-with-label :i/search
     :search            :i/search
     :scroll-to-bottom  :i/arrow-down)
   {:size            12
    :color           (get-icon-and-text-color type theme)
    :container-style (style/container type)}])

(defn- view-internal
  "[dynamic-button opts]
   opts
   {:type                :jump-to/:mention/:notification-down/:notification-up/:search/:search-with-label/:scroll-to-bottom
    :on-press            fn
    :count               mentions or notifications count
    :customization-color customize jump-to and mention button color}"
  [_]
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [type label on-press customization-color style theme] :as args}]
      [rn/touchable-opacity
       {:on-press-in         #(reset! pressed? true)
        :on-press-out        #(reset! pressed? false)
        :on-press            on-press
        :active-opacity      1
        :hit-slop            {:top 5 :bottom 5 :left 5 :right 5}
        :pointer-events      :auto
        :accessibility-label type}
       [rn/view
        {:style (merge
                 {:flex-direction   :row
                  :height           24
                  :border-radius    12
                  :background-color (get-button-color {:type                type
                                                       :pressed?            @pressed?
                                                       :customization-color (or customization-color
                                                                                :primary)
                                                       :theme               theme})}
                 style)}
        (when (#{:mention :search :search-with-label :scroll-to-bottom} type)
          [icon-view type])
        (when (#{:jump-to :mention :notification-down :notification-up :search-with-label} type)
          [text/text
           {:weight :medium
            :size   :paragraph-2
            :style  (assoc (style/text type) :color (get-icon-and-text-color type theme))}
           (case type
             :jump-to                                       label
             :search-with-label                             label
             (:mention :notification-down :notification-up) (str (:count args)))])
        (when (#{:jump-to :notification-down :notification-up} type)
          [icon-view type theme])]])))

(def view (quo.theme/with-theme view-internal))
