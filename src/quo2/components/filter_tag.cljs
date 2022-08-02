(ns quo2.components.filter-tag
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.components.base-tag :as base-tag]))

(def themes {:light {:default  {:border-color     colors/neutral-20
                                :icon-color       colors/neutral-50
                                :text-color            {:style {:color colors/black}}}
                     :active   {:border-color     colors/neutral-30
                                :icon-color       colors/neutral-50
                                :label            {:style {:color colors/black}}}
                     :disabled {:border-color     colors/neutral-20
                                :icon-color       colors/neutral-50
                                :text-color            {:style {:color colors/black}}}}
             :dark  {:default  {:border-color     colors/neutral-70
                                :icon-color       colors/neutral-40
                                :text-color            {:style {:color colors/white}}}
                     :active   {:border-color     colors/neutral-60
                                :icon-color       colors/neutral-40
                                :text-color            {:style {:color colors/white}}}
                     :disabled {:border-color     colors/neutral-70
                                :icon-color       colors/neutral-40
                                :text-color            {:style {:color colors/white}}}}})

(defn filter-tag
  [_ _]
  (fn [{:keys [id on-press disabled size emoji icon active accessibility-label label]
        :or   {size 32}}]
    (let [state (cond disabled :disabled active :active :else :default)
          {:keys [icon-color border-color background-color text-color]}
          (get-in themes [(theme/get-theme) state])]
      [rn/touchable-without-feedback (merge {:disabled            disabled
                                             :accessibility-label accessibility-label}
                                            (when on-press
                                              {:on-press #(on-press id)}))
       [rn/view
        [base-tag/base-tag {:size             size
                            :text-color       text-color
                            :icon             icon
                            :emoji            emoji
                            :icon-color       icon-color
                            :label            label
                            :border-color     border-color
                            :background-color background-color
                            :disabled         disabled}]]])))

