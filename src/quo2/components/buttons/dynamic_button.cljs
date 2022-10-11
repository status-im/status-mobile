(ns quo2.components.buttons.dynamic-button
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [quo2.components.icon :as icon]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]))

(defn- in?
  "true if collection contains element"
  [collection element]
  (some #(= element %) collection))

(defn- get-button-color [type pressed? customization-color]
  (if (in? '(:jump-to :mention) type)
    (if pressed?
      (colors/custom-color-by-theme customization-color 60 50)
      (colors/custom-color-by-theme customization-color 50 60))
    (if pressed?
      (colors/theme-colors colors/neutral-80-opa-80 colors/white-opa-80)
      (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70))))

(defn- get-icon-and-text-color [type]
  (if (in? '(:jump-to :mention) type)
    colors/white
    (colors/theme-colors colors/white colors/neutral-100)))

(defn- icon-view [type]
  [icon/icon
   (case type
     :jump-to           :main-icons2/jump-to
     :mention           :main-icons2/mention
     :notification-down :main-icons2/arrow-down
     :notification-up   :main-icons2/arrow-up
     :search-with-label :main-icons2/search
     :search            :main-icons2/search
     :bottom            :main-icons2/arrow-down)
   {:size            12
    :color           (get-icon-and-text-color type)
    :container-style {:margin-top    6
                      :margin-bottom 6
                      :margin-left   (case type
                                       :jump-to           0
                                       :mention           8
                                       :notification-down 2
                                       :notification-up   2
                                       :search-with-label 8
                                       :search            6
                                       :bottom            6)
                      :margin-right  (case type
                                       :jump-to           8
                                       :mention           2
                                       :notification-down 8
                                       :notification-up   8
                                       :search-with-label 4
                                       :search            6
                                       :bottom            6)}}])

(defn dynamic-button
  "[:dynamic-button opts]
   opts
   {:type                :jump-to/:mention/:notification-down/:notification-up/:search/:search-with-label/:bottom
    :on-press            fn
    :count               mentions or notifications count
    :customization-color customize jump-to and mention button color}"
  [_]
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [type on-press count customization-color]
          :or {customization-color :primary}}]
      [rn/touchable-without-feedback
       {:on-press-in      #(reset! pressed? true)
        :on-press-out     #(reset! pressed? false)
        :on-press         on-press}
       [rn/view {:style {:flex-direction   :row
                         :height           24
                         :border-radius    12
                         :background-color (get-button-color type @pressed? customization-color)}}
        (when (in? '(:mention :search :search-with-label :bottom) type)
          [icon-view type])
        (when (in? '(:jump-to :mention :notification-down :notification-up :search-with-label) type)
          [text/text {:weight :medium
                      :size   :paragraph-2
                      :style  {:color         (get-icon-and-text-color type)
                               :margin-top    2.5
                               :margin-bottom 3.5
                               :margin-left   (case type
                                                :jump-to           8
                                                :mention           0
                                                :notification-down 8
                                                :notification-up   8
                                                :search-with-label 0)
                               :margin-right  (case type
                                                :jump-to           0
                                                :mention           8
                                                :notification-down 0
                                                :notification-up   0
                                                :search-with-label 8)}}
           (case type
             :jump-to                                       (i18n/label :t/jump-to)
             :search-with-label                             (i18n/label :t/back)
             (:mention :notification-down :notification-up) (str count))])
        (when (in? '(:jump-to :notification-down :notification-up) type)
          [icon-view type])]])))
