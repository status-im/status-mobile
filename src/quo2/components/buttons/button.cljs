(ns quo2.components.buttons.button
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [quo2.components.icon :as quo2.icons]))

(def themes
  {:light {:primary         {:icon-color           colors/white
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/primary-50
                                                    :pressed  colors/primary-60
                                                    :disabled colors/primary-50}}
           :secondary       {:icon-color           colors/primary-50
                             :label                {:style    {:color colors/primary-50}}
                             :background-color     {:default  colors/primary-50-opa-20
                                                    :pressed  colors/primary-50-opa-40
                                                    :disabled colors/primary-50-opa-20}}
           :grey            {:icon-color           colors/neutral-100
                             :icon-secondary-color colors/neutral-50
                             :label                {:style    {:color colors/neutral-100}}
                             :background-color     {:default  colors/neutral-10
                                                    :pressed  colors/neutral-20
                                                    :disabled colors/neutral-10}}
           :dark-grey       {:icon-color           colors/neutral-100
                             :icon-secondary-color colors/neutral-50
                             :label                {:style    {:color colors/neutral-100}}
                             :background-color     {:default  colors/neutral-20
                                                    :pressed  colors/neutral-30
                                                    :disabled colors/neutral-20}}
           :outline         {:icon-color           colors/neutral-50
                             :icon-secondary-color colors/neutral-50
                             :label                {:style    {:color colors/neutral-100}}
                             :border-color         {:default  colors/neutral-20
                                                    :pressed  colors/neutral-40
                                                    :disabled colors/neutral-20}}
           :ghost           {:icon-color           colors/neutral-50
                             :icon-secondary-color colors/neutral-50
                             :label                {:style    {:color colors/neutral-100}}
                             :background-color     {:pressed colors/neutral-10}}
           :danger          {:icon-color           colors/white
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/danger-50
                                                    :pressed  colors/danger-60
                                                    :disabled colors/danger-50}}
           :positive        {:icon-color       colors/white
                             :label            {:style {:color colors/white}}
                             :background-color {:default  colors/success-50
                                                :pressed  colors/success-60
                                                :disabled colors/success-50-opa-30}}
           :photo-bg        {:icon-color           colors/neutral-100
                             :icon-secondary-color colors/neutral-80-opa-40
                             :label                {:style    {:color colors/neutral-100}}
                             :background-color     {:default  colors/white-opa-40
                                                    :pressed  colors/white-opa-50
                                                    :disabled colors/white-opa-40}}
           :blur-bg         {:icon-color           colors/neutral-100
                             :icon-secondary-color colors/neutral-80-opa-40
                             :label                {:style    {:color colors/neutral-100}}
                             :background-color     {:default  colors/neutral-80-opa-5
                                                    :pressed  colors/neutral-80-opa-10
                                                    :disabled colors/neutral-80-opa-5}}
           :blur-bg-outline {:icon-color           colors/neutral-100
                             :icon-secondary-color colors/neutral-80-opa-40
                             :label                {:style    {:color colors/neutral-100}}
                             :border-color         {:default  colors/neutral-80-opa-10
                                                    :pressed  colors/neutral-80-opa-20
                                                    :disabled colors/neutral-80-opa-10}}
           :shell            {:icon-color           colors/white
                              :label                {:style    {:color colors/white}}
                              :background-color     {:default  colors/neutral-95
                                                     :pressed  colors/neutral-95
                                                     :disabled colors/neutral-95}}}
   :dark  {:primary         {:icon-color          colors/white
                             :label               {:style    {:color colors/white}}
                             :background-color    {:default  colors/primary-60
                                                   :pressed  colors/primary-50
                                                   :disabled colors/primary-60}}
           :secondary       {:icon-color          colors/primary-50
                             :label               {:style    {:color colors/primary-50}}
                             :background-color    {:default  colors/primary-50-opa-20
                                                   :pressed  colors/primary-50-opa-30
                                                   :disabled colors/primary-50-opa-20}}
           :grey            {:icon-color           colors/white
                             :icon-secondary-color colors/neutral-40
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/neutral-80
                                                    :pressed  colors/neutral-60
                                                    :disabled colors/neutral-80}}
           :dark-grey       {:icon-color           colors/white
                             :icon-secondary-color colors/neutral-40
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/neutral-70
                                                    :pressed  colors/neutral-60
                                                    :disabled colors/neutral-70}}
           :outline         {:icon-color           colors/neutral-40
                             :icon-secondary-color colors/neutral-40
                             :label                {:style    {:color colors/white}}
                             :border-color         {:default  colors/neutral-70
                                                    :pressed  colors/neutral-60
                                                    :disabled colors/neutral-70}}
           :ghost           {:icon-color           colors/neutral-40
                             :icon-secondary-color colors/neutral-40
                             :label                {:style    {:color colors/white}}
                             :background-color     {:pressed  colors/neutral-80}}
           :danger          {:icon-color           colors/white
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/danger-60
                                                    :pressed  colors/danger-50
                                                    :disabled colors/danger-60}}
           :positive        {:icon-color       colors/white
                             :label            {:style {:color colors/white}}
                             :background-color {:default  colors/success-60
                                                :pressed  colors/success-50
                                                :disabled colors/success-60-opa-30}}
           :photo-bg        {:icon-color           colors/white
                             :icon-secondary-color colors/neutral-30
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/neutral-80-opa-40
                                                    :pressed  colors/neutral-80-opa-50
                                                    :disabled colors/neutral-80-opa-40}}
           :blur-bg         {:icon-color           colors/white
                             :icon-secondary-color colors/white-opa-40
                             :label                {:style    {:color colors/white}}
                             :background-color     {:default  colors/white-opa-5
                                                    :pressed  colors/white-opa-10
                                                    :disabled colors/white-opa-5}}
           :blur-bg-outline {:icon-color           colors/white
                             :icon-secondary-color colors/white-opa-40
                             :label                {:style    {:color colors/white}}
                             :border-color         {:default  colors/white-opa-10
                                                    :pressed  colors/white-opa-20
                                                    :disabled colors/white-opa-5}}
           :shell            {:icon-color          colors/white
                              :label               {:style   {:color colors/white}}
                              :background-color    {:default colors/neutral-95}}}})

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
                                (case size 56 16 40 16 32 12 24 8))
          :padding-top        (when-not (or icon before after)
                                (case size 56 0 40 9 32 5 24 3))
          :padding-bottom     (when-not (or icon before after)
                                (case size 56 0 40 9 32 5 24 4))}
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
   {:type   :primary/:secondary/:grey/:dark-grey/:outline/:ghost/
            :danger/:photo-bg/:blur-bg/:blur-bg-ouline/:shell
    :size   40/32/24
    :icon   true/false
    :before :icon-keyword
    :after  :icon-keyword}

   only icon
   [button {:icon true} :main-icons/close-circle]"
  [_ _]
  (let [pressed (reagent/atom false)]
    (fn [{:keys [on-press disabled type size before after above width
                 override-theme override-background-color
                 on-long-press accessibility-label icon icon-no-color style test-ID]
          :or   {type :primary
                 size 40}}
         children]
      (let [{:keys [icon-color icon-secondary-color background-color label border-color]}
            (get-in themes [(or
                             override-theme
                             (theme/get-theme)) type])
            state                (cond disabled :disabled @pressed :pressed :else :default)
            icon-size            (when (= 24 size) 12)
            icon-secondary-color (or icon-secondary-color icon-color)]
        [rn/touchable-without-feedback (merge {:test-ID test-ID
                                               :disabled            disabled
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
                            (or override-background-color (get background-color state))
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
                                     :color           icon-secondary-color
                                     :size            icon-size}]])
          (when before
            [rn/view
             [quo2.icons/icon before {:container-style {:margin-left  (if (= size 40) 12 8)
                                                        :margin-right 4}
                                      :color           icon-secondary-color
                                      :size            icon-size}]])
          [rn/view
           (cond
             (or icon icon-no-color)
             [quo2.icons/icon children {:color icon-color
                                        :no-color icon-no-color
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
                                     :color           icon-secondary-color
                                     :size            icon-size}]])]]))))
