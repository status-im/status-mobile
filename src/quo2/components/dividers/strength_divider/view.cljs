(ns quo2.components.dividers.strength-divider.view
  (:require [quo2.components.dividers.strength-divider.style :as style]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.svg :as svg]
            [utils.i18n :as i18n]))

(def strength-divider-types
  {:very-weak   {:default-text (i18n/label :t/strength-divider-very-weak-label)
                 :color        colors/danger-60
                 :percentage   20}
   :weak        {:default-text (i18n/label :t/strength-divider-weak-label)
                 :color        (colors/custom-color :orange 60)
                 :percentage   40}
   :okay        {:default-text (i18n/label :t/strength-divider-okay-label)
                 :color        (colors/custom-color :yellow 60)
                 :percentage   60}
   :strong      {:default-text (i18n/label :t/strength-divider-strong-label)
                 :color        colors/success-60
                 :percentage   80}
   :very-strong {:default-text (i18n/label :t/strength-divider-very-strong-label)
                 :color        colors/success-60
                 :percentage   100}
   :info        {:color colors/white-opa-40}
   :alert       {:color colors/danger-60}})

(defn circular-progress
  [{:keys [color percentage]}]
  (let [strength-indicator-radius        6.5
        strength-indicator-circumference (* 2 Math/PI strength-indicator-radius)]
    [svg/svg
     {:view-box  "0 0 16 16"
      :width     14.2
      :transform [{:rotate "270deg"}]
      :height    14.2}
     [svg/circle
      {:cx           8
       :cy           8
       :r            strength-indicator-radius
       :fill         :transparent
       :stroke-width 1.2
       :stroke       (colors/alpha color 0.2)}]
     [svg/circle
      {:cx                8
       :cy                8
       :r                 strength-indicator-radius
       :fill              :transparent
       :stroke-width      1.2
       :stroke-dasharray  strength-indicator-circumference
       :stroke-dashoffset (* (- 100 percentage) 0.01 strength-indicator-circumference)
       :stroke            color}]
     [svg/circle]]))

(defn strength-indicator
  [type]
  (let [{:keys [color percentage]} (strength-divider-types type)]
    (case type
      :info  nil
      :alert [icon/icon :i/alert
              {:color color
               :size  16}]
      [circular-progress {:color color :percentage percentage}])))

(defn view
  "Options
   - `:type` `:very-weak`/`:weak`/`:okay`/`:strong`/`:very-strong`/`:info`/`:alert`)

   `text` message string(only works when type is `info`/`alert`)"
  [{:keys [type] :or {type :very-weak}} text]
  (let [{:keys [color default-text]} (strength-divider-types type)]
    [linear-gradient/linear-gradient
     {:colors [(colors/alpha color 0.05) (colors/alpha color 0)]}
     [rn/view
      {:style               style/container
       :accessibility-label :strength-divider}
      [strength-indicator type]
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  (style/text color)}
       (or default-text text)]]]))

