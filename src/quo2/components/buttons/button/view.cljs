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
    disabled boolean
    :type   :primary/:positive:grey/:dark-grey/:outline/:ghost/
            :danger/:black  (TODO remove :photo-bg/:blur-bg/:blur-bg-outline/:shell/:community)
    background :photo/:blur or nil
    :size   40 [default] /32/24/56
    :icon   true/false
    :above  :icon-keyword
    :before :icon-keyword
    :after  :icon-keyword}
    :customization-color keyword or hexstring 
    :theme :light/:dark
   only icon
   [button {:icon true} :i/close-circle]"
  [_ _]
  (let [pressed-in (reagent/atom false)]
    (fn
      [{:keys [on-press on-long-press disabled type background size before after above
               width customization-color theme override-background-color pressed
               accessibility-label icon style inner-style
               blur-active?]
        :or   {type                :primary
               size                40
               customization-color :primary
               blur-active?        true}}
       children]
      (let [pressed? (or @pressed-in pressed)
            icon-only? icon ;; TODO Update external api to icon-only? -
                            ;; https://github.com/status-im/status-mobile/issues/16535
            container-style style ;; TODO Update external api to container-style and remove prop width
            icon-left before ;; TODO Update external api to icon-left
            icon-right after ;; TODO Update external api to icon-right
            icon-above above ;; TODO Update external api to icon-above
            {:keys [icon-color icon-secondary-color background-color label-color border-color blur-type
                    blur-overlay-color icon-background-color border-radius]}
            (button-properties/get-values {:customization-color customization-color
                                           :background          background
                                           :type                type
                                           :theme               theme
                                           :pressed?            pressed?
                                           :icon-only?          icon-only?})
            state (cond disabled                 :disabled
                        (or @pressed-in pressed) :pressed
                        :else                    :default)
            blur-state (if blur-active? :blurred :default)
            icon-size (when (= 24 size) 12)
            icon-secondary-color (or icon-secondary-color icon-color)]
        [rn/touchable-without-feedback
         {:disabled            disabled
          :accessibility-label accessibility-label
          :on-press-in         #(reset! pressed-in true)
          :on-press-out        #(reset! pressed-in nil)
          :on-press            on-press
          :on-long-press       on-long-press}
         [rn/view
          {:style (merge
                   (style/shape-style-container size border-radius)
                   {:width width}
                   container-style)}
          [rn/view
           {:style (merge
                    (style/style-container {:type type
                                            :size size
                                            :disabled disabled
                                            :border-radius border-radius
                                            :background-color
                                            (or override-background-color background-color)
                                            :border-color (get border-color state)
                                            :icon-only? icon-only?
                                            :above above
                                            :width width
                                            :icon-left icon-left
                                            :icon-right icon-right
                                            :blur-active? blur-active?})
                    inner-style)}
           (when customization-color
             [customization-colors/overlay
              {:theme    theme
               :pressed? pressed?}])
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
           (when icon-left
             [rn/view
              {:style (style/icon-left-icon-style
                       {:size                  size
                        :icon-background-color (get icon-background-color blur-state)
                        :icon-size             icon-size})}
              [quo2.icons/icon icon-left
               {:color icon-secondary-color
                :size  icon-size}]])
           [rn/view
            (cond
              icon-only?
              [quo2.icons/icon children
               {:color icon-color
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
                       {:size                  size
                        :icon-background-color (get icon-background-color blur-state)
                        :icon-size             icon-size})}
              [quo2.icons/icon icon-right
               {:color icon-secondary-color
                :size  icon-size}]])]]]))))

(def button (theme/with-theme button-internal))
