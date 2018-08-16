(ns status-im.ui.screens.profile.components.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.profile.components.styles :as styles]))

;; profile header elements

(defn- profile-name-input [name on-change-text-event & [props]]
  [react/view
   [react/text-input
    (merge {:style               styles/profile-name-input-text
            :placeholder         ""
            :default-value       name
            :auto-focus          true
            :on-change-text      #(when on-change-text-event
                                    (re-frame/dispatch [on-change-text-event %]))
            :accessibility-label :username-input}
           props)]])

(defn- show-profile-icon-actions [options]
  (when (seq options)
    (list-selection/show {:title   (i18n/label :t/image-source-title)
                          :options options})))

(defn- profile-header-display [{:keys [name] :as contact}]
  [react/view styles/profile-header-display
   [chat-icon.screen/my-profile-icon {:account contact
                                      :edit?   false}]
   [react/view styles/profile-header-name-container
    [react/text {:style           styles/profile-name-text
                 :number-of-lines 1}
     name]]])

(defn- profile-header-edit [{:keys [name group-chat] :as contact}
                            icon-options on-change-text-event allow-icon-change?]
  [react/view styles/profile-header-edit
   [react/touchable-highlight {:on-press            #(show-profile-icon-actions icon-options)
                               :accessibility-label :edit-profile-photo-button}
    [react/view styles/modal-menu
     [chat-icon.screen/my-profile-icon {:account contact
                                        :edit?   allow-icon-change?}]]]
   [react/view styles/profile-header-name-container
    [profile-name-input name on-change-text-event
     (when group-chat {:accessibility-label :chat-name-input})]]])

(defn profile-header
  [{:keys [contact edited-contact editing? allow-icon-change? options on-change-text-event]}]
  (if editing?
    [profile-header-edit (or edited-contact contact) options on-change-text-event allow-icon-change?]
    [profile-header-display contact]))

;; settings items elements

(defn settings-item-separator []
  [common/separator styles/settings-item-separator])

(defn settings-title [title]
  [react/text {:style styles/settings-title}
   title])

(defn settings-item
  [{:keys [item-text label-kw value action-fn active? destructive? hide-arrow?
           accessibility-label icon-content]
    :or   {value "" active? true}}]
  [react/touchable-highlight
   (cond-> {:on-press action-fn
            :disabled (not active?)}
     accessibility-label
     (assoc :accessibility-label accessibility-label))
   [react/view styles/settings-item
    [react/view styles/settings-item-text-wrapper
     [react/text {:style           (merge styles/settings-item-text
                                          (when destructive? styles/settings-item-destructive))
                  :number-of-lines 1}
      (or item-text (i18n/label label-kw))]
     (when-not (string/blank? value)
       [react/text {:style           styles/settings-item-value
                    :number-of-lines 1
                    :uppercase?      true}
        value])]
    (if icon-content
      icon-content
      (when (and active? (not hide-arrow?))
        [vector-icons/icon :icons/forward {:color colors/gray}]))]])

(defn settings-switch-item [{:keys [label-kw value action-fn active?] :or {active? true}}]
  [react/view styles/settings-item
   [react/view styles/settings-item-text-wrapper
    [react/i18n-text {:style styles/settings-item-text :key label-kw}]]
   [react/switch {:on-tint-color   colors/blue
                  :value           value
                  :on-value-change action-fn
                  :disabled        (not active?)}]])
