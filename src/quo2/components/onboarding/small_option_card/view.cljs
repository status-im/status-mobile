(ns quo2.components.onboarding.small-option-card.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.onboarding.small-option-card.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn- texts
  [{:keys [title subtitle]}]
  [rn/view {:style style/text-container}
   [text/text
    {:style           style/title
     :size            :paragraph-1
     :weight          :semi-bold
     :number-of-lines 1}
    title]
   [text/text
    {:style           style/subtitle
     :size            :paragraph-2
     :weight          :regular
     :number-of-lines 1}
    subtitle]])

(defn- icon-variant
  [{:keys [title subtitle image]}]
  [rn/view {:style style/icon-variant}
   [rn/view {:style style/icon-variant-image-container}
    [fast-image/fast-image
     {:accessibility-label :small-option-card-icon-image
      :style               style/icon-variant-image
      :resize-mode         :contain
      :source              image}]]
   [texts
    {:title    title
     :subtitle subtitle}]])

(defn- main-variant
  [{:keys [title subtitle image]}]
  [rn/view {:style style/main-variant}
   [rn/view {:style style/main-variant-text-container}
    [texts
     {:title    title
      :subtitle subtitle}]]
   [fast-image/fast-image
    {:accessibility-label :small-option-card-main-image
     :style               {:flex 1}
     :resize-mode         :contain
     :source              image}]])

(defn small-option-card
  "Variants: `:main` or `:icon`"
  [{:keys [variant title subtitle image on-press]
    :or   {variant :main}}]
  (let [main-variant?  (= variant :main)
        card-component (if main-variant? main-variant icon-variant)]
    [rn/view
     [rn/touchable-highlight
      {:accessibility-label :small-option-card
       :style               style/touchable-overlay
       :active-opacity      1
       :underlay-color      colors/white-opa-5
       :on-press            on-press}
      [rn/view {:style (style/card main-variant?)}
       [card-component
        {:title    title
         :subtitle subtitle
         :image    image}]]]
     (when main-variant?
       [rn/view {:style style/main-variant-extra-space}])]))
