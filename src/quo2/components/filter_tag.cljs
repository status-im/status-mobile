(ns quo2.components.filter-tag
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.components.icon :as icons]
            [quo2.components.text :as text]))

(def themes {:light {:default  {:border-color     colors/neutral-20
                                :label            {:style {:color colors/black}}}
                     :active   {:border-color     colors/neutral-30
                                :label            {:style {:color colors/black}}}}
             :dark  {:default  {:border-color     colors/neutral-70
                                :label            {:style {:color colors/white}}}
                     :active   {:border-color     colors/neutral-60
                                :label            {:style {:color colors/white}}}}})

(defn style-container [size disabled border-color with-label]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     :row
          :border-color       border-color
          :border-width       1}
         (if-not with-label
           {:padding            (case size 32 0 24 0)
            :border-radius      size
            :width              size}
           {:padding-horizontal (case size 32 12 24 12)
            :border-radius      (case size 32 20 24 20)})
         (when disabled
           {:opacity 0.3})))

(defn tag
  [_ _]
  (fn [{:keys [id on-press disabled size emoji icon icon-color
               active accessibility-label with-label]
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

       (if with-label
         [rn/view {:style (style-container size disabled border-color with-label)}
          (when icon
            [icons/icon icon {:container-style {:align-items     :center
                                                :justify-content :center
                                                :margin-right 4}
                              :resize-mode      :center
                              :size             (case size
                                                  32 20
                                                  24 12)
                              :color            icon-color}])
          (when emoji
            [rn/image {:source emoji
                       :style  (merge (case size
                                        32 {:height 20
                                            :width  20}
                                        24 {:height 12
                                            :width  12})
                                      {:margin-right 4})}])
          [rn/view
           [text/text (merge {:size            (case size
                                                 32 :paragraph-1
                                                 24 :paragraph-2
                                                 20 :label nil)
                              :weight          :medium
                              :number-of-lines 1}
                             label)
            children]]]
         [rn/view {:style (style-container size disabled border-color with-label)}
          (when icon
            [icons/icon icon {:container-style {:align-items     :center
                                                :justify-content :center}
                              :resize-mode      :center
                              :size             (case size
                                                  32 20
                                                  24 12)
                              :color            icon-color}])
          (when emoji
            [rn/image {:source emoji
                       :style  (case size
                                 32 {:height 20
                                     :width  20}
                                 24 {:height 12
                                     :width  12})}])])])))