(ns quo2.components.item
  (:require [quo.react-native :as rn]
            [quo.platform :as platform]
            [quo.haptic :as haptic]
            [quo.gesture-handler :as gh]
            [quo.design-system.spacing :as spacing]
            [quo.components.controls.view :as controls]
            [quo.components.tooltip :as tooltip]
            [quo2.components.text :as text]
            [quo2.foundations.colors :as colors]
            ;; FIXME: 
            [quo2.components.icon :as icons]
            [quo.components.animated.pressable :as animated]))

(defn themes [theme]
  (case theme
    :light  {:icon-color          colors/white
             :active-background   colors/primary-50-opa-5
             :passive-background  colors/white
             :text-color          colors/black}
    :dark   {:icon-color          colors/white
             :active-background   colors/neutral-80
             :passive-background  colors/neutral-90
             :text-color          colors/white}))

(defn size->container-size [size]
  (case size
    :small 52
    48))

(defn size->single-title-size [size]
  (case size
    :small :paragraph-2
    :paragraph-1))

(defn container [{:keys [size container-style]} & children]
  (into [rn/view {:style (merge (:tiny spacing/padding-horizontal)
                                {:min-height       (size->container-size size)
                                 :padding-vertical 8
                                 :flex-direction   :row
                                 :align-items      :center
                                 :justify-content  :space-between} container-style)}]
        children))

(defn icon-column
  [{:keys [icon icon-bg-color icon-color size icon-container-style]}]
  (when icon
    (let [icon-size size]
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
  [{:keys [title text-color subtitle subtitle-max-lines subtitle-secondary
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
      (if (string? subtitle-secondary)
        [rn/view {:flex-direction :row}
         [text/text {:style {:max-width "56.5%"}
                     :weight          :regular
                     :color           :secondary
                     :ellipsize-mode  :tail
                     :number-of-lines subtitle-max-lines
                     :size            text-size}
          subtitle]
         [text/text {:style {:width "7%" :text-align :center}
                     :weight          :regular
                     :color           :secondary
                     :ellipsize-mode  :middle
                     :number-of-lines subtitle-max-lines
                     :size            text-size}
          "•"]
         [text/text {:style {:max-width "36.5%"}
                     :weight          :regular
                     :color           :secondary
                     :ellipsize-mode  :middle
                     :number-of-lines subtitle-max-lines
                     :size            text-size}
          subtitle-secondary]]
        (if (string? subtitle)
          [text/text {:weight          :regular
                      :color           :secondary
                      :ellipsize-mode  :tail
                      :number-of-lines subtitle-max-lines
                      :size            text-size}
           subtitle]
          subtitle))]

     title
     (if (string? title)
       [text/text {:weight                    :medium
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
                    :padding-right  16
                    :align-items    (or (:left-side-alignment props) :center)}}
   [icon-column props]
   [title-column props]])

(defn right-side [{:keys [chevron active disabled accessory accessory-text accessory-style animated-accessory?]}]
  (when (or chevron accessory)
    [rn/view {:style (merge {:align-items     :center
                             :justify-content :flex-end
                             :flex-direction  :row
                             ;; Grow to occupy full space, shrink when need be, but always maitaining 16px left gutter
                             :flex-grow       1
                             :flex-shrink     0
                             :margin-left     16
                             ;; When the left-side leaves no room for right-side, the rendered element is pushed out. A flex-basis ensures that there is some room reserved.
                             ;; The number 80px was determined by trial and error.
                             :flex-basis      80}
                            accessory-style)}
     [rn/view {:style (:tiny spacing/padding-horizontal)}
      (case accessory
        :radio    [controls/radio {:value active :disabled disabled}]
        :checkbox [(if animated-accessory?
                     controls/animated-checkbox
                     controls/checkbox)
                   {:value active :disabled disabled}]
        :switch   [controls/switch {:value active :disabled disabled}]
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
                                      :color          colors/white}]])]))

(defn list-item
  [{:keys [theme accessory disabled subtitle-max-lines icon icon-container-style
           left-side-alignment icon-color icon-bg-color
           title subtitle subtitle-secondary active on-press on-long-press chevron size text-size
           accessory-text accessibility-label title-accessibility-label accessory-style
           haptic-feedback haptic-type error animated animated-accessory? title-text-weight container-style
           active-background-enabled]
    :or   {subtitle-max-lines        1
           theme                     :light
           haptic-feedback           true
           animated                  platform/ios?
           active-background-enabled true
           haptic-type               :selection}}]
  (let [theme           (if disabled :disabled theme)
        {:keys [text-color active-background passive-background]}
        (themes theme)
        icon-color      icon-color
        icon-bg-color   icon-bg-color
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
              :bg-color            (when active-background-enabled active-background)
              :accessibility-label accessibility-label}
             (when on-press
               {:on-press (fn []
                            (optional-haptic)
                            (on-press))})
             (when on-long-press
               {:on-long-press (fn []
                                 (optional-haptic)
                                 (on-long-press))}))
      [container {:size size :container-style container-style}
       [left-side {:icon-color                icon-color
                   :text-color                text-color
                   :left-side-alignment       left-side-alignment
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
                   :subtitle-secondary        subtitle-secondary
                   :right-side-present?       (or accessory chevron)}]
       [right-side {:chevron             chevron
                    :active              active
                    :disabled            disabled
                    :on-press            on-press
                    :accessory-text      accessory-text
                    :animated-accessory? animated-accessory?
                    :accessory-style     accessory-style
                    :accessory           accessory}]]]
     (when error
       [tooltip/tooltip (merge {:bottom-value 0}
                               (when accessibility-label
                                 {:accessibility-label (str (name accessibility-label) "-error")}))
        [text/text {:color :negative
                    :size  :small}
         error]])]))
