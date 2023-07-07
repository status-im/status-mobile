(ns quo2.components.dropdowns.old-button-view
  (:require [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as text]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [quo2.components.dropdowns.old-button-style :as style]))
;TODO: Dropdown needs to be implemented as its own component - https://github.com/status-im/status-mobile/issues/16456

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
            (get-in (style/themes customization-color)
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
                   (style/shape-style-container type icon size)
                   {:width width}
                   style)}
          [rn/view
           {:style (merge
                    (style/style-container {:type type
                                            :size size
                                            :disabled disabled
                                            :background-color
                                            (or override-background-color (get background-color state))
                                            :border-color
                                            (get border-color state)
                                            :icon icon
                                            :above above
                                            :width width
                                            :before before
                                            :after after
                                            :blur-active? blur-active?})
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
