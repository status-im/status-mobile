(ns quo2.components.settings.privacy-option.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.settings.privacy-option.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

(defn- bullet
  [theme]
  [rn/view {:style style/bullet-container}
   [rn/view {:style (style/bullet theme)}]])

(defn- unordered-list
  [{:keys [container-style theme]} list-items]
  [rn/view {:style (merge style/list-container container-style)}
   (for [item list-items]
     ^{:key (hash item)}
     [rn/view {:style style/list-item}
      [bullet theme]
      [text/text {:size :paragraph-2} item]])])

(defn- card-footer
  [{:keys [active? label on-toggle theme]}]
  [rn/touchable-without-feedback
   [rn/view {:style (style/card-footer theme)}
    [rn/view {:style style/card-footer-label-container}
     [text/text {:size :paragraph-2} label]]
    [rn/view {:style style/card-footer-toggle-container}
     [selectors/toggle
      {:disabled? (not active?)
       :on-change on-toggle}]]]])

(defn- selection-indicator
  [active? theme]
  [rn/view {:style (style/selection-indicator-container active? theme)}
   [rn/view {:style (style/selection-indicator active? theme)}]])

(defn- card-header
  [{:keys [icon label active? theme]}]
  [rn/view {:style style/card-header-container}
   [icons/icon icon
    {:size  20
     :color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}]
   [rn/view {:style style/card-header-label-container}
    [text/text {:weight :semi-bold} label]]
   [selection-indicator active?]])

(defn- view-internal
  [{:keys [active? header footer list-items icon on-select on-toggle theme]
    :or   {icon    :i/world
           active? false}}]
  [rn/touchable-without-feedback
   {:on-press            on-select
    :accessibility-label :privacy-option-card
    :testID              :privacy-option-card}
   [rn/view (style/privacy-option-card active? theme)
    [card-header
     {:theme   theme
      :active? active?
      :icon    icon
      :label   header}]
    [unordered-list
     {:theme           theme
      :container-style (when-not footer {:margin-bottom 8})} list-items]
    (when footer
      [card-footer
       {:theme     theme
        :active?   active?
        :label     footer
        :on-toggle on-toggle}])]])

(def view (quo.theme/with-theme view-internal))
