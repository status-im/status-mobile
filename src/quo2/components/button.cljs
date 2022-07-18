(ns quo2.components.button
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.text :as text]
            [quo.theme :as theme]
            [reagent.core :as reagent]
            [quo2.components.icon :as quo2.icons]))

(def themes {:light {:primary   {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/primary-50
                                                    :pressed  colors/primary-70
                                                    :disabled colors/primary-50}}
                     :secondary {:icon-color       colors/primary-50
                                 :label            {:style {:color colors/primary-50}}
                                 :background-color {:default  colors/primary-50-opa-20
                                                    :pressed  colors/primary-50-opa-40
                                                    :disabled colors/primary-50-opa-20}}
                     :grey      {:icon-color       colors/black
                                 :label            {:style {:color colors/black}}
                                 :background-color {:default  colors/neutral-20
                                                    :pressed  colors/neutral-30
                                                    :disabled colors/neutral-20}}
                     :outline   {:icon-color   colors/neutral-50
                                 :label        {:style {:color colors/black}}
                                 :border-color {:default colors/neutral-30
                                                :pressed colors/neutral-40}}
                     :ghost     {:icon-color       colors/neutral-50
                                 :label            {:style {:color colors/black}}
                                 :background-color {:pr:pressedess colors/neutral-10}}
                     :success   {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default colors/success-50
                                                    :pressed colors/success-70
                                                    :disabled colors/success-50}}
                     :danger    {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/danger-50
                                                    :pressed  colors/danger-60
                                                    :disabled colors/danger-50}}}
             :dark  {:primary   {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/primary-60
                                                    :pressed  colors/primary-40
                                                    :disabled colors/primary-60}}
                     :secondary {:icon-color       colors/primary-50
                                 :label            {:style {:color colors/primary-50}}
                                 :background-color {:default  colors/primary-50-opa-20
                                                    :pressed  colors/primary-50-opa-30
                                                    :disabled colors/primary-50-opa-20}}
                     :grey      {:icon-color       colors/neutral-40
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/neutral-80
                                                    :pressed  colors/neutral-60
                                                    :disabled colors/neutral-80}}
                     :outline   {:icon-color   colors/neutral-40
                                 :label        {:style {:color colors/white}}
                                 :border-color {:default colors/neutral-70
                                                :pressed colors/neutral-60}}
                     :ghost     {:icon-color       colors/neutral-40
                                 :label            {:style {:color colors/white}}
                                 :background-color {:pressed colors/neutral-80}}
                     :success   {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default colors/success-60
                                                    :pressed colors/success-40
                                                    :disabled colors/success-60}}
                     :danger    {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/danger-50
                                                    :pressed  colors/danger-40
                                                    :disabled colors/danger-50}}}})

(defn style-container [type size disabled background-color border-color icon above width before after]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     (if above :column :row)
          :border-radius      (if (and icon (#{:primary :secondary :danger} type))
                                24
                                (case size
                                  56 12
                                  40 12
                                  32 10
                                  24 8))
          :background-color   background-color
          :padding-horizontal (when-not (or icon before after)
                                (case size 56 16 40 16 32 12 24 8))
          :padding-left       (when-not (or icon before)
                                (case size 56 16 40 16 32 12 24 8))
          :padding-right      (when-not (or icon after)
                                (case size 56 16 40 16 32 12 24 8))}
         (when width
           {:width width})
         (when icon
           {:width size})
         (when border-color
           {:border-color border-color
            :border-width 1})
         (when disabled
           {:opacity 0.3})))

(defn button
  "with label
   [button opts \"label\"]
   opts
   {:type :primary/:secondary/:grey/:outline/:ghost/:danger
    :size 40/32/24
    :icon true/false
    :before :icon-keyword
    :after :icon-keyword}

   only icon
   [button {:icon true} :main-icons/close-circle]"
  [_ _]
  (let [pressed (reagent/atom false)]
    (fn [{:keys [on-press disabled type size before after above width
                 override-theme
                 on-long-press accessibility-label icon style]
          :or   {type :primary
                 size 40}}
         children]
      (let [{:keys [icon-color background-color label border-color]}
            (get-in themes [(or
                             override-theme
                             (theme/get-theme)) type])
            state (cond disabled :disabled @pressed :pressed :else :default)
            icon-size (when (= 24 size) 12)]
        [rn/touchable-without-feedback (merge {:disabled            disabled
                                               :accessibility-label accessibility-label}
                                              (when on-press
                                                {:on-press (fn []
                                                             (on-press))})
                                              (when on-long-press
                                                {:on-long-press (fn []
                                                                  (on-long-press))})
                                              {:on-press-in (fn []
                                                              (reset! pressed true))}
                                              {:on-press-out (fn []
                                                               (reset! pressed nil))})

         [rn/view {:style (merge
                           (style-container
                            type
                            size
                            disabled
                            (get background-color state)
                            (get border-color state)
                            icon
                            above
                            width
                            before
                            after)
                           style)}
          (when above
            [rn/view
             [quo2.icons/icon above {:container-style {:margin-bottom 2}
                                     :color           icon-color
                                     :size            icon-size}]])
          (when before
            [rn/view
             [quo2.icons/icon before {:container-style {:margin-left  (if (= size 40) 12 8)
                                                        :margin-right 4}
                                      :color           icon-color
                                      :size            icon-size}]])
          [rn/view
           (cond
             icon
             [quo2.icons/icon children {:color icon-color
                                        :size  icon-size}]

             (string? children)
             [text/text (merge {:size            (when (#{56 24} size) :paragraph-2)
                                :weight          :medium
                                :number-of-lines 1}
                               label)
              children]

             (vector? children)
             children)]
          (when after
            [rn/view
             [quo2.icons/icon after {:container-style {:margin-left  4
                                                       :margin-right (if (= size 40) 12 8)}
                                     :color           icon-color
                                     :size            icon-size}]])]]))))
