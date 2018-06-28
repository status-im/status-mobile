(ns status-im.ui.screens.desktop.main.tabs.profile.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.views :as profile]
            [status-im.utils.build :as build]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.desktop.main.tabs.profile.styles :as styles]
            [status-im.i18n :as i18n]))

(defn profile-badge [{:keys [name]}]
  [react/view {:margin-vertical 10}
   [react/text {:style {:font-weight :bold}
                :number-of-lines 1}
    name]])

(defn profile-info-item [{:keys [label value]}]
  [react/view
   [react/view
    [react/text
     label]
    [react/view {:height 10}]
    [react/touchable-opacity {:on-press #(react/copy-to-clipboard value)}
     [react/text {:number-of-lines 1
                  :ellipsizeMode   :middle}
      value]]]])

(defn my-profile-info [{:keys [public-key]}]
  [react/view
   [profile-info-item
    {:label "Contact Key"
     :value public-key}]])

(views/defview profile []
  (views/letsubs [current-account [:get-current-account]]
    [react/view {:margin-top 40 :margin-horizontal 10}
     [react/view
      [profile-badge current-account]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/view
      [my-profile-info current-account]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/view {:style styles/logout-row}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:logout])}
       [react/text {:style {:color colors/red}} (i18n/label :t/logout)]]
      [react/text {:style {:color colors/gray}} "V" build/version]]]))
