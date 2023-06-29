(ns quo2.components.buttons.button
  (:require [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [quo2.components.buttons.style :as style]))

(defn themes
  [customization-color]
  {:light
   {:primary         {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  (colors/custom-color customization-color 50)
                                             :pressed  (colors/custom-color customization-color 60)
                                             :disabled (colors/custom-color customization-color 50)}}
    :secondary       {:icon-color       colors/primary-50
                      :label-color      colors/primary-50
                      :background-color {:default  colors/primary-50-opa-20
                                         :pressed  colors/primary-50-opa-40
                                         :disabled colors/primary-50-opa-20}}
    :grey            {:icon-color           colors/neutral-100
                      :icon-secondary-color colors/neutral-50
                      :label-color          colors/neutral-100
                      :background-color     {:default  colors/neutral-10
                                             :pressed  colors/neutral-20
                                             :disabled colors/neutral-10}}
    :dark-grey       {:icon-color           colors/neutral-100
                      :icon-secondary-color colors/neutral-50
                      :label-color          colors/neutral-100
                      :background-color     {:default  colors/neutral-20
                                             :pressed  colors/neutral-30
                                             :disabled colors/neutral-20}}
    :outline         {:icon-color           colors/neutral-50
                      :icon-secondary-color colors/neutral-50
                      :label-color          colors/neutral-100
                      :border-color         {:default  colors/neutral-30
                                             :pressed  colors/neutral-40
                                             :disabled colors/neutral-30}}
    :ghost           {:icon-color           colors/neutral-50
                      :icon-secondary-color colors/neutral-50
                      :label-color          colors/neutral-100
                      :background-color     {:pressed colors/neutral-10}}
    :danger          {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  colors/danger-50
                                             :pressed  colors/danger-60
                                             :disabled colors/danger-50}}
    :positive        {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  colors/success-50
                                             :pressed  colors/success-60
                                             :disabled colors/success-50-opa-30}}
    :photo-bg        {:icon-color           colors/neutral-100
                      :icon-secondary-color colors/neutral-80-opa-40
                      :label-color          colors/neutral-100
                      :background-color     {:default  colors/white-opa-40
                                             :pressed  colors/white-opa-50
                                             :disabled colors/white-opa-40}}
    :blur-bg         {:icon-color           colors/neutral-100
                      :icon-secondary-color colors/neutral-80-opa-40
                      :label-color          colors/neutral-100
                      :background-color     {:default  colors/neutral-80-opa-5
                                             :pressed  colors/neutral-80-opa-10
                                             :disabled colors/neutral-80-opa-5}}
    :blurred         {:icon-color            colors/neutral-100
                      :icon-secondary-color  colors/neutral-100
                      :icon-background-color {:default colors/neutral-20
                                              :blurred colors/neutral-80-opa-10}
                      :label-color           colors/neutral-100
                      :background-color      {:default  colors/neutral-10
                                              :pressed  colors/neutral-10
                                              :disabled colors/neutral-10-opa-10-blur}
                      :blur-overlay-color    colors/neutral-10-opa-40-blur
                      :blur-type             :light}
    :blur-bg-outline {:icon-color           colors/neutral-100
                      :icon-secondary-color colors/neutral-80-opa-40
                      :label-color          colors/neutral-100
                      :border-color         {:default  colors/neutral-80-opa-10
                                             :pressed  colors/neutral-80-opa-20
                                             :disabled colors/neutral-80-opa-10}}
    :shell           {:icon-color       colors/white
                      :label-color      colors/white
                      :background-color {:default  colors/neutral-95
                                         :pressed  colors/neutral-95
                                         :disabled colors/neutral-95}}}
   :dark
   {:primary         {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  (colors/custom-color customization-color 60)
                                             :pressed  (colors/custom-color customization-color 50)
                                             :disabled (colors/custom-color customization-color 60)}}
    :secondary       {:icon-color       colors/primary-50
                      :label-color      colors/primary-50
                      :background-color {:default  colors/primary-50-opa-20
                                         :pressed  colors/primary-50-opa-30
                                         :disabled colors/primary-50-opa-20}}
    :grey            {:icon-color           colors/white
                      :icon-secondary-color colors/neutral-40
                      :label-color          colors/white
                      :background-color     {:default  colors/neutral-90
                                             :pressed  colors/neutral-60
                                             :disabled colors/neutral-90}}
    :dark-grey       {:icon-color           colors/white
                      :icon-secondary-color colors/neutral-40
                      :label-color          colors/white
                      :background-color     {:default  colors/neutral-70
                                             :pressed  colors/neutral-60
                                             :disabled colors/neutral-70}}
    :outline         {:icon-color           colors/neutral-40
                      :icon-secondary-color colors/neutral-40
                      :label-color          colors/white
                      :border-color         {:default  colors/neutral-70
                                             :pressed  colors/neutral-60
                                             :disabled colors/neutral-70}}
    :ghost           {:icon-color           colors/neutral-40
                      :icon-secondary-color colors/neutral-40
                      :label-color          colors/white
                      :background-color     {:pressed colors/neutral-80}}
    :danger          {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  colors/danger-60
                                             :pressed  colors/danger-50
                                             :disabled colors/danger-60}}
    :positive        {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  colors/success-60
                                             :pressed  colors/success-50
                                             :disabled colors/success-60-opa-30}}
    :photo-bg        {:icon-color           colors/white
                      :icon-secondary-color colors/neutral-30
                      :label-color          colors/white
                      :background-color     {:default  colors/neutral-80-opa-40
                                             :pressed  colors/neutral-80-opa-50
                                             :disabled colors/neutral-80-opa-40}}
    :blur-bg         {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-70
                      :label-color          colors/white
                      :background-color     {:default  colors/white-opa-5
                                             :pressed  colors/white-opa-10
                                             :disabled colors/white-opa-5}}
    :blurred         {:icon-color            colors/white
                      :icon-secondary-color  colors/white
                      :icon-background-color {:default colors/neutral-80
                                              :blurred colors/white-opa-10}
                      :label-color           colors/white
                      :background-color      {:default  colors/neutral-90
                                              :pressed  colors/neutral-90
                                              :disabled colors/neutral-90-opa-10-blur}
                      :blur-overlay-color    colors/neutral-80-opa-40
                      :blur-type             :dark}
    :blur-bg-outline {:icon-color           colors/white
                      :icon-secondary-color colors/white-opa-40
                      :label-color          colors/white
                      :border-color         {:default  colors/white-opa-10
                                             :pressed  colors/white-opa-20
                                             :disabled colors/white-opa-5}}
    :shell           {:icon-color       colors/white
                      :label-color      colors/white
                      :background-color {:default colors/neutral-95}}}})

(defn shape-style-container
  [type icon size]
  {:height        size
   :border-radius (if (and icon (#{:primary :secondary :danger} type))
                    24
                    (case size
                      56 12
                      40 12
                      32 10
                      24 8))})

(defn style-container
  [type size disabled background-color border-color icon above width before after blur-active?]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     (if above :column :row)
          :padding-horizontal (when-not (or icon before after)
                                (case size
                                  56 16
                                  40 16
                                  32 12
                                  24 8))
          :padding-left       (when-not (or icon before)
                                (case size
                                  56 16
                                  40 16
                                  32 12
                                  24 8))
          :padding-right      (when-not (or icon after)
                                (case size
                                  56 16
                                  40 16
                                  32 12
                                  24 8))
          :padding-top        (when-not (or icon before after)
                                (case size
                                  56 0
                                  40 9
                                  32 5
                                  24 3))
          :padding-bottom     (when-not (or icon before after)
                                (case size
                                  56 0
                                  40 9
                                  32 5
                                  24 4))
          :overflow           :hidden}
         (when (or (and (= type :blurred) (not blur-active?))
                   (not= type :blurred))
           {:background-color background-color})
         (shape-style-container type icon size)
         (when width
           {:width width})
         (when icon
           {:width size})
         (when border-color
           {:border-color border-color
            :border-width 1})
         (when disabled
           {:opacity 0.3})))

(defn- button-internal
  "with label
   [button opts \"label\"]
   opts
   {:type   :primary/:secondary/:grey/:dark-grey/:outline/:ghost/
            :danger/:photo-bg/:blur-bg/:blur-bg-outline/:shell/:community
    :size   40 [default] /32/24
    :icon   true/false
    :community-color '#FFFFFF'
    :community-text-color '#000000'
    :before :icon-keyword
    :after  :icon-keyword}

   only icon
   [button {:icon true} :i/close-circle]"
  [_ _]
  (let [pressed-in (reagent/atom false)]
    (fn
      [{:keys [on-press disabled type size before after above icon-secondary-no-color
               width customization-color theme override-background-color pressed
               on-long-press accessibility-label icon icon-no-color style inner-style test-ID
               blur-active? override-before-margins override-after-margins icon-size icon-container-size
               icon-container-rounded?]
        :or   {type                :primary
               size                40
               customization-color :primary
               blur-active?        true}}
       children]
      (let [{:keys [icon-color icon-secondary-color background-color label-color border-color blur-type
                    blur-overlay-color icon-background-color]}
            (get-in (themes customization-color)
                    [theme type])
            state (cond disabled                 :disabled
                        (or @pressed-in pressed) :pressed
                        :else                    :default)
            blur-state (if blur-active? :blurred :default)
            icon-size (or icon-size (when (= 24 size) 12))
            icon-secondary-color (or icon-secondary-color icon-color)]
        [rn/touchable-without-feedback
         (merge {:test-ID             test-ID
                 :disabled            disabled
                 :accessibility-label accessibility-label
                 :on-press-in         #(reset! pressed-in true)
                 :on-press-out        #(reset! pressed-in nil)}
                (when on-press
                  {:on-press on-press})
                (when on-long-press
                  {:on-long-press on-long-press}))
         [rn/view
          {:style (merge
                   (shape-style-container type icon size)
                   {:width width}
                   style)}
          [rn/view
           {:style (merge
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
                     after
                     blur-active?)
                    (when (= state :pressed) {:opacity 0.9})
                    inner-style)}
           (when (and (= type :blurred)
                      blur-active?)
             [blur/view
              {:blur-radius   20
               :blur-type     blur-type
               :overlay-color blur-overlay-color
               :style         style/blur-view}])
           (when above
             [rn/view
              [quo2.icons/icon above
               {:container-style {:margin-bottom 2}
                :color           icon-secondary-color
                :size            icon-size}]])
           (when before
             [rn/view
              {:style (style/before-icon-style
                       {:override-margins        override-before-margins
                        :size                    size
                        :icon-container-size     icon-container-size
                        :icon-background-color   (get icon-background-color blur-state)
                        :icon-container-rounded? icon-container-rounded?
                        :icon-size               icon-size})}
              [quo2.icons/icon before
               {:color icon-secondary-color
                :size  icon-size}]])
           [rn/view
            (cond
              (or icon icon-no-color)
              [quo2.icons/icon children
               {:color    icon-color
                :no-color icon-no-color
                :size     icon-size}]

              (string? children)
              [text/text
               {:size            (when (#{56 24} size) :paragraph-2)
                :weight          :medium
                :number-of-lines 1
                :style           {:color label-color}}

               children]

              (vector? children)
              children)]
           (when after
             [rn/view
              {:style (style/after-icon-style
                       {:override-margins        override-after-margins
                        :size                    size
                        :icon-container-size     icon-container-size
                        :icon-background-color   (get icon-background-color blur-state)
                        :icon-container-rounded? icon-container-rounded?
                        :icon-size               icon-size})}
              [quo2.icons/icon after
               {:no-color icon-secondary-no-color
                :color    icon-secondary-color
                :size     icon-size}]])]]]))))

(def button (theme/with-theme button-internal))
