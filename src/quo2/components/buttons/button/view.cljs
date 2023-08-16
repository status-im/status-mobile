(ns quo2.components.buttons.button.view
  (:require [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as text]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [quo2.components.buttons.button.style :as style]
            [quo2.components.buttons.button.properties :as button-properties]
            [quo2.foundations.customization-colors :as customization-colors]))

(defn- button-internal
  "with label
   [button opts \"label\"]
   opts
   {on-press callback
    on-long-press callback
    disabled? boolean
    :type   :primary/:positive/:grey/:dark-grey/:outline/:ghost/
            :danger/:black
    background :photo/:blur or nil
    :size   40 [default] /32/24/56
    :icon-only?   true/false
    :icon-top  :icon-keyword
    :icon-left :icon-keyword
    :icon-right  :icon-keyword}
    :customization-color keyword or hexstring 
    :theme :light/:dark
   only icon
   [button {:icon-only? true} :i/close-circle]"
  [_ _]
  (let [pressed? (reagent/atom false)]
    (fn
      [{:keys [on-press on-long-press disabled? type background size icon-left icon-right icon-top
               customization-color theme accessibility-label icon-only? container-style inner-style]
        :or   {type                :primary
               size                40
               customization-color (cond (= type :primary)  :blue
                                         (= type :positive) :success
                                         (= type :danger)   :danger
                                         :else              nil)}}
       children]
      (let [{:keys [icon-color background-color label-color border-color blur-type
                    blur-overlay-color border-radius]}
            (button-properties/get-values {:customization-color customization-color
                                           :background          background
                                           :type                type
                                           :theme               theme
                                           :pressed?            @pressed?
                                           :icon-only?          icon-only?})
            icon-size (when (= 24 size) 12)]
        [rn/touchable-without-feedback
         {:disabled            disabled?
          :accessibility-label accessibility-label
          :on-press-in         #(reset! pressed? true)
          :on-press-out        #(reset! pressed? nil)
          :on-press            on-press
          :on-long-press       on-long-press}
         [rn/view
          {:style (merge
                   (style/shape-style-container size border-radius)
                   container-style)}
          [rn/view
           {:style (merge
                    (style/style-container {:size             size
                                            :disabled?        disabled?
                                            :border-radius    border-radius
                                            :background-color background-color
                                            :border-color     border-color
                                            :icon-only?       icon-only?
                                            :icon-top         icon-top
                                            :icon-left        icon-left
                                            :icon-right       icon-right})
                    inner-style)}
           (when customization-color
             [customization-colors/overlay
              {:customization-color customization-color
               :theme               theme
               :pressed?            @pressed?}])
           (when (= background :photo)
             [blur/view
              {:blur-radius   20
               :blur-type     blur-type
               :overlay-color blur-overlay-color
               :style         style/blur-view}])
           (when icon-top
             [rn/view
              [quo2.icons/icon icon-top
               {:container-style {:margin-bottom 2}
                :color           icon-color
                :size            icon-size}]])
           (when icon-left
             [rn/view
              {:style (style/icon-left-icon-style
                       {:size      size
                        :icon-size icon-size})}
              [quo2.icons/icon icon-left
               {:color icon-color
                :size  icon-size}]])
           [rn/view
            (cond
              icon-only?
              [quo2.icons/icon children
               {:color label-color
                :size  icon-size}]

              (string? children)
              [text/text
               {:size            (when (#{56 24} size) :paragraph-2)
                :weight          :medium
                :number-of-lines 1
                :style           {:color label-color}}
               children]

              (vector? children)
              children)]
           (when icon-right
             [rn/view
              {:style (style/icon-right-icon-style
                       {:size      size
                        :icon-size icon-size})}
              [quo2.icons/icon icon-right
               {:color icon-color
                :size  icon-size}]])]]]))))

(def button (theme/with-theme button-internal))
