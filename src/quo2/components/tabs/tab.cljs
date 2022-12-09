(ns quo2.components.tabs.tab
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.markdown.text :as text]))

(def themes {:light {:default  {:background-color colors/neutral-20
                                :icon-color       colors/neutral-50
                                :label            {:style {:color colors/black}}}
                     :active   {:background-color colors/neutral-50
                                :icon-color       colors/white
                                :label            {:style {:color colors/white}}}
                     :disabled {:background-color colors/neutral-20
                                :icon-color       colors/neutral-50
                                :label            {:style {:color colors/black}}}}
             :dark  {:default  {:background-color colors/neutral-80
                                :icon-color       colors/neutral-40
                                :label            {:style {:color colors/white}}}
                     :active   {:background-color colors/neutral-60
                                :icon-color       colors/white
                                :label            {:style {:color colors/white}}}
                     :disabled {:background-color colors/neutral-80
                                :icon-color       colors/neutral-40
                                :label            {:style {:color colors/white}}}}})

(defn style-container [size disabled background-color]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     :row
          :border-radius      (case size
                                32 10
                                28 8
                                24 8
                                20 6)
          :background-color   background-color
          :padding-horizontal (case size 32 12 28 12 24 8 20 8)}
         (when disabled
           {:opacity 0.3})))

(defn tab
  "[tab opts \"label\"]
   opts
   {:type :primary/:secondary/:grey/:outline/:ghost/:danger
    :size 40/32/24
    :icon true/false
    :before :icon-keyword
    :after :icon-keyword}"
  [_ _]
  (fn [{:keys [id on-press disabled size before active accessibility-label]
        :or   {size 32}}
       children]
    (let [state (cond disabled :disabled active :active :else :default)
          {:keys [icon-color background-color label]}
          (get-in themes [(theme/get-theme) state])]
      [rn/touchable-without-feedback (merge {:disabled            disabled
                                             :accessibility-label accessibility-label}
                                            (when on-press
                                              {:on-press (fn []
                                                           (on-press id))}))
       [rn/view {:style (style-container size disabled background-color)}
        (when before
          [rn/view
           [icons/icon before {:color icon-color}]])
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