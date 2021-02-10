(ns quo.components.list.item
  (:require [quo.react-native :as rn]
            [quo.platform :as platform]
            [quo.haptic :as haptic]
            [quo.gesture-handler :as gh]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]
            [quo.components.text :as text]
            [quo.components.controls.view :as controls]
            [quo.components.tooltip :as tooltip]
            ;; FIXME:
            [status-im.ui.components.icons.vector-icons :as icons]
            [quo.components.animated.pressable :as animated]))

(defn themes [theme]
  (case theme
    :main     {:icon-color         (:icon-04 @colors/theme)
               :icon-bg-color      (:interactive-02 @colors/theme)
               :active-background  (:interactive-02 @colors/theme)
               :passive-background (:ui-background @colors/theme)
               :text-color         (:text-01 @colors/theme)}
    :accent   {:icon-color         (:icon-04 @colors/theme)
               :icon-bg-color      (:interactive-02 @colors/theme)
               :active-background  (:interactive-02 @colors/theme)
               :passive-background (:ui-background @colors/theme)
               :text-color         (:text-04 @colors/theme)}
    :negative {:icon-color         (:negative-01 @colors/theme)
               :icon-bg-color      (:negative-02 @colors/theme)
               :active-background  (:negative-02 @colors/theme)
               :passive-background (:ui-background @colors/theme)
               :text-color         (:negative-01 @colors/theme)}
    :positive {:icon-color         (:positive-01 @colors/theme)
               :icon-bg-color      (:positive-02 @colors/theme)
               :active-background  (:positive-02 @colors/theme)
               :passive-background (:ui-background @colors/theme)
               :text-color         (:positive-01 @colors/theme)}
    :disabled {:icon-color         (:icon-02 @colors/theme)
               :icon-bg-color      (:ui-01 @colors/theme)
               :active-background  (:ui-01 @colors/theme)
               :passive-background (:ui-background @colors/theme)
               :text-color         (:text-02 @colors/theme)}))

(defn size->icon-size [size]
  (case size
    :small 36
    40))

(defn size->container-size [size]
  (case size
    :small 52
    64))

(defn size->single-title-size [size]
  (case size
    :small :base
    :large))

(defn container [{:keys [size]} & children]
  (into [rn/view {:style (merge (:tiny spacing/padding-horizontal)
                                {:min-height       (size->container-size size)
                                 :padding-vertical 8
                                 :flex-direction   :row
                                 :align-items      :center
                                 :justify-content  :space-between})}]
        children))

(defn icon-column
  [{:keys [icon icon-bg-color icon-color size icon-container-style]}]
  (when icon
    (let [icon-size (size->icon-size size)]
      [rn/view {:style (or icon-container-style (:tiny spacing/padding-horizontal))}
       (cond
         (vector? icon)
         icon

         (keyword? icon)
         [rn/view {:style {:width            icon-size
                           :height           icon-size
                           :align-items      :center
                           :justify-content  :center
                           :border-radius    (/ icon-size 2)
                           :background-color icon-bg-color}}
          [icons/icon icon {:color icon-color}]])])))

(defn title-column
  [{:keys [title text-color subtitle subtitle-max-lines
           title-accessibility-label size text-size title-text-weight
           right-side-present?]}]
  [rn/view {:style (merge (:tiny spacing/padding-horizontal)
                          ;; make left-side title grow if nothing is present on right-side
                          (when-not right-side-present?
                            {:flex            1
                             :justify-content :center}))}
   (cond

     (and title subtitle)
     [:<>
      ;; FIXME(Ferossgp): ReactNative 63 will support view inside text on andrid, remove thess if when migrating
      (if (string? title)
        [text/text {:weight              (or title-text-weight :medium)
                    :style               {:color text-color}
                    :accessibility-label title-accessibility-label
                    :ellipsize-mode      :tail
                    :number-of-lines     1
                    :size                text-size}
         title]
        title)
      (if (string? subtitle)
        [text/text {:weight          :regular
                    :color           :secondary
                    :ellipsize-mode  :tail
                    :number-of-lines subtitle-max-lines
                    :size            text-size}
         subtitle]
        subtitle)]

     title
     (if (string? title)
       [text/text {:weight                    (or title-text-weight :regular)
                   :number-of-lines           1
                   :style                     {:color text-color}
                   :title-accessibility-label title-accessibility-label
                   :ellipsize-mode            :tail
                   :size                      (or text-size (size->single-title-size size))}
        title]
       title))])

(defn left-side [props]
  [rn/view {:style {:flex-direction :row
                    ;; Occupy only content width, never grow, but shrink if need be
                    :flex-grow      0
                    :flex-shrink    1
                    :align-items    :center}}
   [icon-column props]
   [title-column props]])

(defn right-side [{:keys [chevron active accessory accessory-text animated-accessory?]}]
  (when (or chevron accessory)
    [rn/view {:style {:align-items     :center
                      :justify-content :flex-end
                      :flex-direction  :row
                      ;; Grow to occupy full space, shrink when need be, but always maitaining 16px left gutter
                      :flex-grow       1
                      :flex-shrink     1
                      :margin-left     16
                      ;; When the left-side leaves no room for right-side, the rendered element is pushed out. A flex-basis ensures that there is some room reserved.
                      ;; The number 80px was determined by trial and error.
                      :flex-basis      80}}
     [rn/view {:style (:tiny spacing/padding-horizontal)}
      (case accessory
        :radio    [controls/radio {:value active}]
        :checkbox [(if animated-accessory?
                     controls/animated-checkbox
                     controls/checkbox)
                   {:value active}]
        :switch   [controls/switch {:value active}]
        :text     [text/text {:color           :secondary
                              :ellipsize-mode  :middle
                              :number-of-lines 1}
                   accessory-text]
        accessory)]
     (when (and chevron platform/ios?)
       [rn/view {:style {:padding-right (:tiny spacing/spacing)}}
        [icons/icon :main-icons/next {:container-style {:opacity         0.4
                                                        :align-items     :center
                                                        :justify-content :center}
                                      :resize-mode     :center
                                      :color           (:icon-02 @colors/theme)}]])]))

(defn list-item
  [{:keys [theme accessory disabled subtitle-max-lines icon icon-container-style
           title subtitle active on-press on-long-press chevron size text-size
           accessory-text accessibility-label title-accessibility-label
           haptic-feedback haptic-type error animated animated-accessory? title-text-weight]
    :or   {subtitle-max-lines 1
           theme              :main
           haptic-feedback    true
           animated           platform/ios?
           haptic-type        :selection}}]
  (let [theme           (if disabled :disabled theme)
        {:keys [icon-color text-color icon-bg-color
                active-background passive-background]}
        (themes theme)
        optional-haptic (fn []
                          (when haptic-feedback
                            (haptic/trigger haptic-type)))
        component       (cond
                          (and (not on-press)
                               (not on-long-press))
                          rn/view
                          animated animated/pressable
                          :else    gh/touchable-highlight)]
    [rn/view {:background-color (if (and (= accessory :radio) active)
                                  active-background
                                  passive-background)}
     [component
      (merge {:type                :list-item
              :disabled            disabled
              :bg-color            active-background
              :accessibility-label accessibility-label}
             (when on-press
               {:on-press (fn []
                            (optional-haptic)
                            (on-press))})
             (when on-long-press
               {:on-long-press (fn []
                                 (optional-haptic)
                                 (on-long-press))}))
      [container {:size size}
       [left-side {:icon-color                icon-color
                   :text-color                (if on-press
                                                text-color
                                                (:text-color (themes :main)))
                   :icon-bg-color             icon-bg-color
                   :title-accessibility-label title-accessibility-label
                   :icon                      icon
                   :icon-container-style      icon-container-style
                   :title                     title
                   :title-text-weight         title-text-weight
                   :size                      size
                   :text-size                 text-size
                   :subtitle                  subtitle
                   :subtitle-max-lines        subtitle-max-lines
                   :right-side-present?       (or accessory chevron)}]
       [right-side {:chevron             chevron
                    :active              active
                    :on-press            on-press
                    :accessory-text      accessory-text
                    :animated-accessory? animated-accessory?
                    :accessory           accessory}]]]
     (when error
       [tooltip/tooltip (merge {:bottom-value 0}
                               (when accessibility-label
                                 {:accessibility-label (str (name accessibility-label) "-error")}))
        [text/text {:color :negative
                    :size  :small}
         error]])]))
