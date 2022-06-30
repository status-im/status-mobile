(ns quo2.components.filter-tag
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.components.text :as text]))

(def themes {:light {:default  {:border-color     colors/neutral-20
                                :label            {:style {:color colors/black}}}
                     :active   {:border-color     colors/neutral-20
                                :label            {:style {:color colors/black}}}}
             :dark  {:default  {:border-color     colors/neutral-70
                                :label            {:style {:color colors/white}}}
                     :active   {:border-color     colors/neutral-70
                                :label            {:style {:color colors/white}}}}})

(defn style-container [size disabled border-color]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     :row
          :border-radius      (case size
                                32 20
                                28 20
                                24 20
                                20 20)
          :border-color   border-color
          :border-width        1
          :padding-horizontal (case size 32 12 28 12 24 8 20 8)}
         (when disabled
           {:opacity 0.3})))

(defn tag
  [_ _]
  (fn [{:keys [id on-press disabled size resource active accessibility-label]
        :or   {size 32}}
       children]
    (let [state (cond disabled :disabled active :active :else :default)
          {:keys [border-color label]}
          (get-in themes [(theme/get-theme) state])]
      [rn/touchable-without-feedback (merge {:disabled            disabled
                                             :accessibility-label accessibility-label}
                                            (when on-press
                                              {:on-press (fn []
                                                           (on-press id))}))
       [rn/view {:style (style-container size disabled border-color)}
        (when resource
          [rn/image {:source resource
                     :style  {:height       12
                              :width        12
                              :margin-right 6}}])
        [rn/view
         (cond
           (string? children)
           [text/text (merge {:size            (case size 24 :paragraph-2 20 :label nil)
                              :weight          :medium
                              :number-of-lines 1}
                             label)
            children]
           (vector? children)
           children)]]])))