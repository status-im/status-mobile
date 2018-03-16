(ns status-im.ui.screens.profile.components.views
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.profile.components.styles :as styles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [clojure.string :as string]))

;; profile header elements

(defn profile-name-input [name on-change-text-event]
  [react/view
   [react/text-input
    {:style               styles/profile-name-input-text
     :placeholder         ""
     :default-value       name
     :auto-focus          true
     :on-change-text      #(when on-change-text-event
                             (re-frame/dispatch [on-change-text-event %]))
     :accessibility-label :username-input}]])

(defn show-profile-icon-actions [options]
  (when (seq options)
    (list-selection/show {:title   (i18n/label :t/image-source-title)
                          :options options})))

(defn profile-header-display [{:keys [name] :as contact}]
  [react/view styles/profile-header-display
   [chat-icon.screen/my-profile-icon {:account contact
                                      :edit?   false}]
   [react/view styles/profile-header-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 1}
     name]]])

(defn profile-header-edit [{:keys [name] :as contact}
                           icon-options on-change-text-event allow-icon-change?]
  [react/view styles/profile-header-edit
   [react/touchable-highlight {:on-press            #(show-profile-icon-actions icon-options)
                               :accessibility-label :edit-profile-photo-button}
    [react/view styles/modal-menu
     [chat-icon.screen/my-profile-icon {:account contact
                                        :edit?   allow-icon-change?}]]]
   [react/view styles/profile-header-name-container
    [profile-name-input name on-change-text-event]]])

(defn profile-header [contact editing? allow-icon-change? options on-change-text-event]
  (if editing?
    [profile-header-edit contact options on-change-text-event allow-icon-change?]
    [profile-header-display contact]))

;; settings items elements

(defn settings-item-separator []
  [common/separator styles/settings-item-separator])

(defn settings-title [title]
  [react/text {:style styles/settings-title}
   title])

(defn settings-item [{:keys [label-kw value action-fn active? accessibility-label icon-content]
                      :or {value "" active? true}}]
  [react/touchable-highlight
   (cond-> {:on-press action-fn
            :disabled (not active?)}
     accessibility-label
     (assoc :accessibility-label accessibility-label))
   [react/view styles/settings-item
    [react/view styles/settings-item-text-wrapper
     [react/text {:style           styles/settings-item-text
                  :number-of-lines 1}
      (i18n/label label-kw)]
     (when-not (string/blank? value)
       [react/text {:style           styles/settings-item-value
                    :number-of-lines 1
                    :uppercase?      true}
        value])]
    (if icon-content
      icon-content
      (when active?
        [vector-icons/icon :icons/forward {:color colors/gray}]))]])

(defn settings-switch-item [{:keys [label-kw value action-fn active?] :or {active? true}}]
  [react/view styles/settings-item
   [react/view styles/settings-item-text-wrapper
    [react/text {:style styles/settings-item-text}
     (i18n/label label-kw)]]
   [react/switch {:on-tint-color   colors/blue
                  :value           value
                  :on-value-change action-fn
                  :disabled        (not active?)}]])