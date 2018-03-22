(ns status-im.ui.screens.desktop.main.tabs.profile.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.views :as profile]))

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
    [react/text {:number-of-lines 1
                 :ellipsizeMode   :middle}
     value]]])

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
     [react/touchable-highlight {:on-press #(profile/navigate-to-accounts false)
                                 :style {:margin-top 60}}
      [react/view
       [react/text {:style {:color :red}} "Log out"]]]]))
