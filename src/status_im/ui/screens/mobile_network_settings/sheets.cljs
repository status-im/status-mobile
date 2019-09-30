(ns status-im.ui.screens.mobile-network-settings.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.mobile-network-settings.sheets-styles :as styles]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.list-item.views :as list-item]))

(defn title [label]
  [react/view {:style styles/title}
   [react/text
    {:style styles/title-text}
    (i18n/label label)]])

(defn details [label]
  [react/view
   {:style styles/details}
   [react/text
    {:style styles/details-text}
    (i18n/label label)]])

(defn separator []
  [react/view {:style styles/separator}])

(defn go-to-settings []
  [react/view
   {:style styles/go-to-settings-container}
   [react/text
    {:style    styles/go-to-settings
     :on-press #(re-frame/dispatch [:mobile-network/navigate-to-settings])}
    (i18n/label :mobile-network-go-to-settings)]])

(views/defview checkbox []
  (views/letsubs [checked? [:mobile-network/remember-choice?]]
    [react/view
     {:style styles/checkbox-line-container}
     [checkbox/checkbox
      {:checked?        checked?
       :style           styles/checkbox
       :icon-style      styles/checkbox-icon
       :on-value-change #(re-frame/dispatch [:mobile-network/remember-choice? %])}]
     [react/view
      {:style styles/checkbox-text-container}
      [react/text (i18n/label :mobile-network-sheet-remember-choice)]]]))

(defn settings []
  [react/view
   {:style styles/settings-container}
   [react/nested-text
    {:style    styles/settings-text
     :on-press #(re-frame/dispatch [:mobile-network/navigate-to-settings])}
    (i18n/label :mobile-network-sheet-configure)
    [{:style styles/settings-link}
     (str " " (i18n/label :mobile-network-sheet-settings))]]])

(views/defview settings-sheet []
  [react/view {:flex 1}
   [react/view {:align-items :center}
    [title :mobile-syncing-sheet-title]
    [details :mobile-syncing-sheet-details]]
   [list-item/list-item
    {:theme    :action
     :title    :t/mobile-network-continue-syncing
     :subtitle :t/mobile-network-continue-syncing-details
     :icon     :main-icons/network
     :on-press #(re-frame/dispatch [:mobile-network/continue-syncing])}]
   [list-item/list-item
    {:theme    :action-destructive
     :title    :t/mobile-network-stop-syncing
     :subtitle :t/mobile-network-stop-syncing-details
     :icon     :main-icons/cancel
     :on-press #(re-frame/dispatch [:mobile-network/stop-syncing])}]
   [separator]
   [react/view {:flex       1
                :align-self :stretch}
    [checkbox]
    [settings]]])

(views/defview offline-sheet []
  [react/view {:flex 1}
   [react/view {:align-items :center}
    [title :t/mobile-network-sheet-offline]
    [details :t/mobile-network-sheet-offline-details]]
   [list-item/list-item
    {:theme    :action
     :title    :t/mobile-network-start-syncing
     :subtitle :t/mobile-network-continue-syncing-details
     :icon     :main-icons/network
     :on-press #(re-frame/dispatch [:mobile-network/continue-syncing])}]
   [separator]
   [go-to-settings]])
