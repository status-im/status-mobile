(ns quo2.components.settings.privacy-option
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.settings.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn- bullet
  []
  [rn/view {:style style/bullet-container}
   [rn/view {:style (style/bullet)}]])

(defn- unordered-list
  [container-style list-items]
  [rn/view {:style (merge style/list-container container-style)}
   (for [item list-items]
     ^{:key (hash item)}
     [rn/view {:style style/list-item}
      [bullet]
      [text/text {:size :paragraph-2} item]])])

(defn- card-footer
  [{:keys [active? label on-toggle]}]
  [rn/touchable-without-feedback
   [rn/view {:style (style/card-footer)}
    [rn/view {:style style/card-footer-label-container}
     [text/text {:size :paragraph-2} label]]
    [rn/view {:style style/card-footer-toggle-container}
     [selectors/toggle
      {:disabled? (not active?)
       :on-change on-toggle}]]]])

(defn- selection-indicator
  [active?]
  [rn/view {:style (style/selection-indicator-container active?)}
   [rn/view {:style (style/selection-indicator active?)}]])

(defn- card-header
  [{:keys [icon label active?]}]
  [rn/view {:style style/card-header-container}
   [icons/icon icon
    {:size  20
     :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]
   [rn/view {:style style/card-header-label-container}
    [text/text {:weight :semi-bold} label]]
   [selection-indicator active?]])

(defn card
  [{:keys [active? header footer list-items icon on-select on-toggle]
    :or   {icon    :i/world
           active? false}}]
  [rn/touchable-without-feedback
   {:on-press            on-select
    :accessibility-label :privacy-option-card
    :testID              :privacy-option-card}
   [rn/view (style/privacy-option-card active?)
    [card-header
     {:active? active?
      :icon    icon
      :label   header}]
    [unordered-list (when-not footer {:margin-bottom 8}) list-items]
    (when footer
      [card-footer
       {:active?   active?
        :label     footer
        :on-toggle on-toggle}])]])
