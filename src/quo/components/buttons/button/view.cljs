(ns quo.components.buttons.button.view
  (:require
    [quo.components.blur.view :as blur]
    [quo.components.buttons.button.properties :as button-properties]
    [quo.components.buttons.button.style :as style]
    [quo.components.icon :as quo.icons]
    [quo.components.markdown.text :as text]
    [quo.foundations.customization-colors :as customization-colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn button
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
  [{:keys [on-press on-long-press disabled? type background size icon-left icon-left-color icon-right
           icon-right-color icon-top icon-top-color customization-color accessibility-label icon-only?
           container-style inner-style pressed? on-press-in on-press-out allow-multiple-presses?]
    :or   {type                :primary
           size                40
           customization-color (if (= type :primary) :blue nil)}}
   children]
  (let [[pressed-state? set-pressed-state] (rn/use-state false)
        theme (quo.theme/use-theme)
        {:keys [icon-color background-color label-color border-color blur-type
                blur-overlay-color border-radius overlay-customization-color]}
        (button-properties/get-values {:customization-color customization-color
                                       :background          background
                                       :type                type
                                       :theme               theme
                                       :pressed?            (if pressed? pressed? pressed-state?)
                                       :icon-only?          icon-only?})
        icon-size (when (= 24 size) 12)
        on-press-in-cb (rn/use-callback
                        (fn []
                          (set-pressed-state true)
                          (when on-press-in (on-press-in))))
        on-press-out-cb (rn/use-callback
                         (fn []
                           (set-pressed-state nil)
                           (when on-press-out (on-press-out))))]
    [rn/pressable
     {:disabled                disabled?
      :accessibility-label     accessibility-label
      :on-press-in             on-press-in-cb
      :on-press-out            on-press-out-cb
      :on-press                on-press
      :allow-multiple-presses? allow-multiple-presses?
      :on-long-press           on-long-press}
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
       (when overlay-customization-color
         [customization-colors/overlay
          {:customization-color overlay-customization-color
           :theme               theme
           :pressed?            (if pressed? pressed? pressed-state?)}])
       (when (= background :photo)
         [blur/view
          {:blur-radius   20
           :blur-type     blur-type
           :overlay-color blur-overlay-color
           :style         style/blur-view}])
       (when icon-top
         [rn/view
          [quo.icons/icon icon-top
           {:container-style {:margin-bottom 2
                              :opacity       (when disabled? 0.3)}
            :color           (or icon-top-color icon-color)
            :size            icon-size}]])
       (when icon-left
         [rn/view
          {:style (style/icon-left-icon-style
                   {:size      size
                    :icon-size icon-size
                    :disabled? disabled?})}
          [quo.icons/icon icon-left
           {:color (or icon-left-color icon-color)
            :size  icon-size}]])
       [rn/view
        (cond
          icon-only?
          [quo.icons/icon children
           {:color label-color
            :size  icon-size}]

          (string? children)
          [text/text
           {:size            (when (#{56 24} size) :paragraph-2)
            :weight          :medium
            :number-of-lines 1
            :style           {:color   label-color
                              :opacity (when (and disabled? (= theme :dark)) 0.3)}}
           children]

          (vector? children)
          children)]
       (when icon-right
         [rn/view
          {:style (style/icon-right-icon-style
                   {:size      size
                    :icon-size icon-size
                    :disabled? disabled?})}
          [quo.icons/icon icon-right
           {:color (or icon-right-color icon-color)
            :size  icon-size}]])]]]))
