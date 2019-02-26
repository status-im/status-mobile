(ns status-im.ui.screens.mobile-network-settings.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.mobile-network-settings.sheets-styles :as styles]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.i18n :as i18n]
            [status-im.ui.components.lists.cell.view :as cell]
            [re-frame.core :as re-frame]))

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
    {:style styles/go-to-settings
     :on-press #(re-frame/dispatch [:mobile-network/navigate-to-settings])}
    (i18n/label :mobile-network-go-to-settings)]])

(views/defview checkbox []
  (views/letsubs
    [checked? [:get :mobile-network/remember-choice?]]
    [react/view
     {:style styles/checkbox-line-container}
     [checkbox/checkbox
      {:checked?        checked?
       :style           styles/checkbox
       :icon-style      styles/checkbox-icon
       :on-value-change #(re-frame/dispatch [:mobile-network/remember-choice? %])}]
     [react/view
      {:style styles/checkbox-text-container}
      [react/text {:style styles/checkbox-text}
       (i18n/label :mobile-network-sheet-remember-choice)]]]))

(defn settings []
  [react/view
   {:style styles/settings-container}
   [react/text
    {:style    styles/settings-text
     :on-press #(re-frame/dispatch [:mobile-network/navigate-to-settings])}
    (i18n/label :mobile-network-sheet-configure)
    [react/text {:style styles/settings-link}
     (str " " (i18n/label :mobile-network-sheet-settings))]]])

(views/defview settings-sheet []
  [react/view {:style styles/container}
   [title :mobile-syncing-sheet-title]
   [details :mobile-syncing-sheet-details]
   [cell/cell
    {:title    (i18n/label :mobile-network-continue-syncing)
     :details  (i18n/label :mobile-network-continue-syncing-details)
     :icon     :main-icons/network
     :style    styles/network-icon
     :on-press #(re-frame/dispatch [:mobile-network/continue-syncing])}]
   [cell/cell
    {:title    (i18n/label :mobile-network-stop-syncing)
     :details  (i18n/label :mobile-network-stop-syncing-details)
     :icon     :main-icons/cancel
     :style    styles/cancel-icon
     :on-press #(re-frame/dispatch [:mobile-network/stop-syncing])}]
   [separator]
   [react/view {:flex       1
                :align-self :stretch}
    [checkbox]
    [settings]]])

(views/defview offline-sheet []
  [react/view {:style styles/container}
   [title :t/mobile-network-sheet-offline]
   [details :t/mobile-network-sheet-offline-details]
   [cell/cell
    {:title    (i18n/label :mobile-network-start-syncing)
     :details  (i18n/label :mobile-network-continue-syncing-details)
     :icon     :main-icons/network
     :style    styles/network-icon
     :on-press #(re-frame/dispatch [:mobile-network/continue-syncing])}]
   [separator]
   [go-to-settings]])
