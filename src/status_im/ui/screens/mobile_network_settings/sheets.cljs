(ns status-im.ui.screens.mobile-network-settings.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.list.item :as list.item]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.mobile-network-settings.sheets-styles :as styles]
            [utils.i18n :as i18n]))

(defn title
  [label]
  [react/view {:style styles/title}
   [react/text
    {:style styles/title-text}
    (i18n/label label)]])

(defn details
  [label]
  [react/view
   {:style styles/details}
   [react/text
    {:style styles/details-text}
    (i18n/label label)]])

(defn separator
  []
  [react/view {:style styles/separator}])

(defn go-to-settings
  []
  [react/view
   {:style styles/go-to-settings-container}
   [react/text
    {:style    styles/go-to-settings
     :on-press #(re-frame/dispatch [:mobile-network/navigate-to-settings])}
    (i18n/label :t/mobile-network-go-to-settings)]])

(views/defview offline-sheet
  []
  [react/view {:flex 1}
   [react/view {:align-items :center}
    [title :t/mobile-network-sheet-offline]
    [details :t/mobile-network-sheet-offline-details]]
   [list.item/list-item
    {:theme              :accent
     :title              (i18n/label :t/mobile-network-start-syncing)
     :subtitle           (i18n/label :t/mobile-network-continue-syncing-details)
     :subtitle-max-lines 2
     :icon               :main-icons/network
     :on-press           #(re-frame/dispatch [:mobile-network/continue-syncing])}]
   [separator]
   [go-to-settings]])
