(ns quo2.components.settings.category-list.view
  (:require [quo2.components.settings.category-list.style :as style]
            [quo2.components.settings.settings-list.view :refer [badge-icon
                                                                 browser-context-icon chevron-icon communities-icons right-button]]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))



(defn category-title
  [title]
   (js/console.log "title: " title)
  [rn/view
   {:style style/title-container}
   [text/text
    {:accessibility-label :setting-item-name-text
     :ellipsize-mode      :tail
     :style               (style/title)
     :number-of-lines     1
     :weight              :medium
     :size                :paragraph-1}
    title]])

(defn category-list
  "Options
   - `title` String to show in the center of the component, right to the icon and left to optional gadgets.
   - `on-press` Callback called when the component is pressed.
   - `accessibility-label` String to use as accessibility-label for VoiceOver.
   - `left-icon` Symbol to indicate icon type on the left side of the component.
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
   - `style` Styles map to be merge with default container styles."
  [{:keys [title
           on-press
           accessibility-label
           left-icon
           chevron?
           badge?
           button-props
           communities-props
           container-style]}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view
    {:style (merge style/item-container container-style)}
    (case left-icon
      :browser-context (browser-context-icon)
      nil)
    [category-title title]
    (when badge? badge-icon)
    (when button-props
      (right-button button-props))
    (when communities-props (communities-icons communities-props))
    (when chevron? chevron-icon)]])
