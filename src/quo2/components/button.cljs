(ns quo2.components.button
  (:require [quo.react-native :as rn]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.text :as text]
            [quo.theme :as theme]
            [reagent.core :as reagent]))

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
                     :grey      {:icon-color       colors/neutral-50
                                 :label            {:style {:color colors/black}}
                                 :background-color {:default  colors/neutral-40
                                                    :pressed  colors/neutral-30
                                                    :disabled colors/neutral-20}}
                     :outline   {:icon-color   colors/neutral-50
                                 :label        {:style {:color colors/black}}
                                 :border-color {:default colors/neutral-30
                                                :pressed colors/neutral-40}}
                     :ghost     {:icon-color       colors/neutral-50
                                 :label            {:style {:color colors/black}}
                                 :background-color {:pr:pressedess colors/neutral-10}}
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
                     :danger    {:icon-color       colors/white
                                 :label            {:style {:color colors/white}}
                                 :background-color {:default  colors/danger-50
                                                    :pressed  colors/danger-40
                                                    :disabled colors/danger-50}}}})

(defn style-container [type size disabled background-color border-color icon above]
  (println size disabled background-color)
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
          :padding-horizontal (if icon 0 (case size 56 16 40 16 32 12 24 8))}
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
    (fn [{:keys [on-press disabled type size before after above
                 on-long-press accessibility-label icon]
          :or   {type :primary
                 size 40}}
         children]
      (let [{:keys [icon-color background-color label border-color]}
            (get-in themes [(theme/get-theme) type])
            state (cond disabled :disabled @pressed :pressed :else :default)]
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

         [rn/view {:style (style-container type size disabled (get background-color state) (get border-color state) icon above)}
          (when above
            [rn/view
             [icons/icon above {:color icon-color}]])
          (when before
            [rn/view
             [icons/icon before {:color icon-color}]])
          [rn/view
           (cond
             icon
             [icons/icon children {:color icon-color}]

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
             [icons/icon after {:color icon-color}]])]]))))