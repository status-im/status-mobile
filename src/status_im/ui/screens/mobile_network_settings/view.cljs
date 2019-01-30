(ns status-im.ui.screens.mobile-network-settings.view
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.mobile-network-settings.style :as styles]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.ui.components.lists.cell.view :as cell]))

(defn title []
  [react/view {:style styles/title}
   [react/text
    {:style styles/title-text}
    (i18n/label :mobile-syncing-sheet-title)]])

(defn details []
  [react/view
   {:style styles/details}
   [react/text
    {:style styles/details-text}
    (i18n/label :mobile-syncing-sheet-details)]])

(defn separator []
  [react/view {:style styles/separator}])

(defn checkbox []
  (let [checked? (reagent/atom false)]
    (fn []
      [react/view
       {:style styles/checkbox-line-container}
       [checkbox/checkbox
        {:checked?        @checked?
         :style           styles/checkbox
         :icon-style      styles/checkbox-icon
         :on-value-change #(swap! checked? not)
         :native?         false}]
       [react/view
        {:style styles/checkbox-text-container}
        [react/text {:style styles/checkbox-text}
         (i18n/label :mobile-network-sheet-remember-choice)]]])))

(defn settings []
  [react/view
   {:style styles/settings-container}
   [react/text {:style styles/settings-text}
    (i18n/label :mobile-network-sheet-configure)
    [react/text {:style styles/settings-link}
     (str " " (i18n/label :mobile-network-sheet-settings))]]])

(views/defview mobile-network-settings-sheet []
  [react/view {:style styles/container}
   [title]
   [details]
   [cell/cell
    {:title   (i18n/label :mobile-network-continue-syncing)
     :details (i18n/label :mobile-network-continue-syncing-details)
     :icon    :main-icons/network
     :style   styles/network-icon}]
   [cell/cell
    {:title   (i18n/label :mobile-network-stop-syncing)
     :details (i18n/label :mobile-network-stop-syncing-details)
     :icon    :main-icons/cancel
     :style   styles/cancel-icon}]
   [separator]
   [react/view {:flex       1
                :align-self :stretch}
    [checkbox]
    [settings]]])
