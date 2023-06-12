(ns quo2.components.settings.settings-list.view
  (:require [quo2.components.settings.settings-list.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tag]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn settings-title
  [title status-tag-props override-theme]
  [rn/view
   {:style style/title-container}
   (when title
     [text/text
      {:accessibility-label :setting-item-name-text
       :ellipsize-mode      :tail
       :style               (style/title override-theme)
       :override-theme      override-theme
       :number-of-lines     1
       :weight              :medium
       :size                :paragraph-1}
      title])
   (when status-tag-props
     [rn/view {:style style/tag-container}
      [status-tag/status-tag
       status-tag-props]])])

(defn left-icon-comp
  [icon]
  [rn/view {:style style/icon}
   [icons/icon icon
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]])

(def chevron-icon
  [rn/view
   [icons/icon :chevron-right
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]])

(defn toggle-button
  [{:keys [checked?
           on-change]}]
  [selectors/toggle
   {:checked?  checked?
    :on-change (fn [new-value] (on-change new-value))}])

(defn badge-icon
  [override-theme]
  [rn/view
   {:accessible          :true
    :accessibility-label :setting-list-badge
    :style               (style/dot override-theme)}])

(defn right-button
  [{:keys [title
           on-press]}
   override-theme]
  [button/button
   {:type           :outline
    :override-theme override-theme
    :on-press       on-press
    :size           24}
   title])

(defn communities-icons
  [{:keys [data
           icon-style]}
   override-theme]
  (let [communities-count (dec (count data))]
    [rn/view
     {:style style/communities-container}
     (map-indexed
      (fn [index {:keys [source accessibility-label]}]
        [rn/image
         {:key                 source
          :source              (if (string? source)
                                 {:uri source}
                                 source)
          :accessibility-label accessibility-label
          :style               (merge (style/community-icon (- communities-count index) override-theme)
                                      icon-style)}])
      data)]))

(defn settings-list
  "Options
   - `title` String to show in the center of the component, right to the icon and left to optional gadgets.
   - `on-press` Callback called when the component is pressed.
   - `accessibility-label` String to use as accessibility-label for VoiceOver.
   - `left-icon` icon keyword for icon on left.
   - `chevron?` Boolean to show/hide chevron at the right border of the component.
   - `toggle-prop` Map with the following keys:
       `checked?` Boolean value to set check or unchecked toggle.
       `on-change` Callback called when user toggles toggle. Will pass the new toggle value to the callback
   - `badge?` Boolean to show/hide badge.
   - `button-props` Map with the following keys:
       `title` String to show as button text.
       `on-press` Callback called when button is pressed.
   - `communities-props` Map with the following keys:
       `data` Array of maps containg source of the community asset.
   - `style` Styles map to be merge with default container styles.
   - `overide-theme` :dark or :light
   - `status-tag-props see the spec for status-tag component
   "

  [{:keys [title
           on-press
           accessibility-label
           left-icon
           chevron?
           toggle-props
           badge?
           button-props
           communities-props
           container-style
           override-theme
           status-tag-props
           border]}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view
    {:style (merge style/item-container container-style (when border
                                                          style/border-style) )} 
    [rn/view {:style style/inner-container}
     (when left-icon
       [left-icon-comp left-icon])
     [settings-title title status-tag-props override-theme]
     (when toggle-props
       [toggle-button toggle-props])
     (when badge? [badge-icon override-theme])
     (when button-props
       [right-button button-props override-theme])
     (when communities-props (communities-icons communities-props override-theme))
     (when chevron? chevron-icon)]]])
