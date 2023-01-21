(ns quo2.components.tabs.tab
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def themes
  {:light {:default  {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-50
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/neutral-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(def themes-for-blur-background
  {:light {:default  {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-80-opa-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/white-opa-5
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/white-opa-20
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/white-opa-5
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(defn style-container
  [size disabled background-color]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     :row
          :border-radius      (case size
                                32 10
                                28 8
                                24 8
                                20 6
                                nil)
          :background-color   background-color
          :padding-horizontal (case size
                                32 12
                                28 12
                                24 8
                                20 8
                                nil)}
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
  (fn [{:keys [id on-press disabled size before active accessibility-label blur? override-theme]
        :or   {size 32}}
       children]
    (let [state                                       (cond disabled :disabled
                                                            active   :active
                                                            :else    :default)
          {:keys [icon-color background-color label]}
          (get-in (if blur? themes-for-blur-background themes)
                  [(or override-theme (theme/get-theme)) state])]
      [rn/touchable-without-feedback
       (merge {:disabled            disabled
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
           [text/text
            (merge {:size            (case size
                                       24 :paragraph-2
                                       20 :label
                                       nil)
                    :weight          :medium
                    :number-of-lines 1}
                   label)
            children]
           (vector? children)
           children)]]])))
