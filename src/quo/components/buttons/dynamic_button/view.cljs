(ns quo.components.buttons.dynamic-button.view
  (:require
    [quo.components.buttons.dynamic-button.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- get-button-color
  [{:keys [type pressed? customization-color theme]}]
  (if (= type :mention)
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
  (if (= type :mention)
    colors/white
    (colors/theme-colors colors/white colors/neutral-100 theme)))

(defn- icon-view
  [type theme]
  [icon/icon
   (case type
     :mention           :i/mention
     :notification-down :i/arrow-down
     :notification-up   :i/arrow-up
     :search-with-label :i/search
     :search            :i/search
     :scroll-to-bottom  :i/arrow-down)
   {:size            12
    :color           (get-icon-and-text-color type theme)
    :container-style (style/container type)}])

(defn view
  "[dynamic-button opts]
   opts
   {:type                :mention/:notification-down/:notification-up/:search/:search-with-label/:scroll-to-bottom
    :on-press            fn
    :count               mentions or notifications count
    :customization-color customize and mention button color}"
  [{:keys [type label on-press customization-color style] :as args}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        button-color           (get-button-color {:type                type
                                                  :pressed?            pressed?
                                                  :customization-color (or customization-color :primary)
                                                  :theme               theme})
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/touchable-opacity
     {:on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :active-opacity      1
      :hit-slop            {:top 5 :bottom 5 :left 5 :right 5}
      :pointer-events      :auto
      :style               {:height 24}
      :accessibility-label type}
     [rn/view
      {:style (merge
               {:flex-direction   :row
                :height           24
                :border-radius    12
                :background-color button-color}
               style)}
      (when (#{:mention :search :search-with-label :scroll-to-bottom} type)
        [icon-view type theme])
      (when (#{:mention :notification-down :notification-up :search-with-label} type)
        [text/text
         {:weight :medium
          :size   :paragraph-2
          :style  (assoc (style/text type) :color (get-icon-and-text-color type theme))}
         (case type
           :search-with-label                             label
           (:mention :notification-down :notification-up) (str (:count args)))])
      (when (#{:notification-down :notification-up} type)
        [icon-view type theme])]]))
