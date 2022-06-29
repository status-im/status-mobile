(ns quo2.components.status-tags
  (:require [status-im.i18n.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.text :as text]
            [quo.react-native :as rn]))

(def default-container-style
  {:border-radius 20
   :border-width 1})

(def small-container-style
  (merge default-container-style
         {:padding-horizontal 7
          :padding-vertical 3}))

(def large-container-style
  (merge default-container-style
         {:padding-horizontal 11
          :padding-vertical 4}))

(defn base-tag [_]
  (fn [{:keys [size
               border-color
               background-color
               icon
               text-color
               label]}]
    (let [paragraph-size (if (= size :small) :paragraph-2 :paragraph-1)]
      [rn/view
       (assoc (if (= size :small)
                small-container-style
                large-container-style)
              :border-color border-color
              :background-color background-color)
       [text/text {:size paragraph-size}
        [icon/theme-icon icon {:color :none
                               :size 12}]
        [text/text {:size paragraph-size
                    :style {:color text-color}} (str " " label)]]])))

(defn positive [_]
  (fn [size]
    [base-tag {:size size
               :background-color colors/success-50-opa-10
               :icon :verified
               :border-color colors/success-50-opa-20
               :text-color (colors/theme-colors colors/success-50
                                                colors/success-60)
               :label (i18n/label :positive)}]))

(defn negative [_]
  (fn [size]
    [base-tag {:size size
               :icon :untrustworthy
               :background-color colors/danger-50-opa-10
               :border-color colors/danger-50-opa-20
               :text-color (colors/theme-colors colors/danger-50
                                                colors/danger-60)
               :label (i18n/label :negative)}]))

(defn pending [_]
  (fn [size]
    [base-tag {:size size
               :icon :pending
               :background-color (colors/theme-colors colors/neutral-10
                                                      colors/neutral-80)
               :border-color (colors/theme-colors colors/neutral-20
                                                  colors/neutral-70)
               :text-color colors/neutral-50
               :label (i18n/label :pending)}]))

(defn status-tag [_]
  (fn [{:keys [status size]}]
    [(case status
       :positive positive
       :negative negative
       :pending pending
       nil) size]))
