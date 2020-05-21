(ns quo.components.list.item
  (:require [quo.react-native :as rn]
            [quo.platform :as platform]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]
            [quo.components.text :as text]
            ;; FIXME:
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.checkbox.view :as checkbox]
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

(defn container [{:keys [size]} & children]
  (into [rn/view {:style (merge (:tiny spacing/padding-horizontal)
                                {:min-height       (size->container-size size)
                                 :padding-vertical 8
                                 :flex-direction   :row
                                 :flex             1
                                 :align-items      :center
                                 :justify-content  :space-between})}]
        children))

(defn- icon-column [{:keys [icon icon-bg-color icon-color size]}]
  (when icon
    (let [icon-size (if (= size :default)
                      40
                      36)]
      [rn/view {:style (:tiny spacing/padding-horizontal)}
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

(defn title-column [{:keys [title text-color subtitle subtitle-max-lines]}]
  [rn/view {:style (merge (:tiny spacing/padding-horizontal)
                          {:justify-content :center})}
   (cond

     (and title subtitle)
     [:<>
      [text/text {:weight          :medium
                  :style           {:color text-color}
                  :ellipsize-mode  :tail
                  :number-of-lines 1}
       title]
      [text/text {:weight          :regular
                  :color           :secondary
                  :ellipsize-mode  :tail
                  :number-of-lines subtitle-max-lines}
       subtitle]]

     title [text/text {:number-of-lines 1
                       :style           {:color text-color}
                       :ellipsize-mode  :tail
                       :size            :large}
            title])])

(defn left-side [props]
  [rn/view {:style {:flex-direction :row
                    :flex           1
                    :align-items    :center}}
   [icon-column props]
   [title-column props]])

(defn right-side [{:keys [chevron on-press active accessory accessory-text]}]
  [rn/view {:style {:align-items    :center
                    :flex-direction :row}}
   [rn/view {:style (:tiny spacing/padding-horizontal)}
    (case accessory
      :radio    [radio/radio active]
      :checkbox [checkbox/checkbox {:checked? active}]
      :switch   [rn/switch {:value           active
                            :track-color     #js {:true  (:interactive-01 @colors/theme)
                                                  :false nil}
                            :on-value-change on-press}]
      :text     [text/text {:color           :secondary
                            :number-of-lines 1}
                 accessory-text]
      nil)]
   (when (and chevron platform/ios?)
     [rn/view {:style {:padding-right (:tiny spacing/spacing)}}
      [icons/icon :main-icons/next {:container-style {:opacity         0.4
                                                      :align-items     :center
                                                      :justify-content :center}
                                    :resize-mode     :center
                                    :color           (:icon-02 @colors/theme)}]])])

(defn list-item
  [{:keys [theme accessory disabled subtitle-max-lines icon title
           subtitle active on-press on-long-press chevron size
           accessory-text]
    :or   {subtitle-max-lines 1
           theme              :main}}]
  (let [theme (if disabled :disabled theme)
        {:keys [icon-color text-color icon-bg-color
                active-background passive-background]}
        (themes theme)]
    [rn/view {:background-color (if active active-background passive-background)}
     [animated/pressable {:type             :list-item
                          :disabled         disabled
                          :background-color active-background
                          :on-press         on-press
                          :on-long-press    on-long-press}
      [container {:size size}
       [left-side {:icon-color         icon-color
                   :text-color         text-color
                   :icon-bg-color      icon-bg-color
                   :icon               icon
                   :title              title
                   :size               size
                   :subtitle           subtitle
                   :subtitle-max-lines subtitle-max-lines}]
       [right-side {:chevron        chevron
                    :active         active
                    :on-press       on-press
                    :accessory-text accessory-text
                    :accessory      accessory}]]]]))
