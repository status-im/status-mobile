(ns status-im.ui.screens.profile.user.components
  (:require [status-im.ui.components.react :as react]
            [quo.components.header :as header]
            [quo.core :as quo]
            [status-im.ui.screens.profile.user.styles :as styles]
            [status-im2.contexts.chat.messages.avatar.view :as avatar]
            [status-im.multiaccounts.core :as multiaccounts]))

(def top-background-view
  [react/view
   {:style styles/top-background-view}])

(defn button
  [{:keys [icon accessibility-label on-press style]}]
  [header/header-action
   {:icon                icon
    :on-press            on-press
    :accessibility-label accessibility-label
    :style               (merge styles/header-icon-style style)}])

(defn fixed-toolbar
  [{:keys [on-close on-switch-profile on-show-qr on-share]}]
  [react/view
   {:style styles/toolbar}
   [button
    {:icon                :main-icons/close
     :accessibility-label :close-header-button
     :on-press            on-close
     :style               {:margin-left 20}}]
   [react/view
    {:style styles/right-accessories}
    [button
     {:icon                :main-icons/multi-profile
      :accessibility-label :multi-profile-header-button
      :on-press            on-switch-profile}]
    [button
     {:icon                :main-icons/qr2
      :accessibility-label :qr-header-button
      :on-press            on-show-qr
      :style               {:margin-left 14}}]
    [button
     {:icon                :main-icons/share
      :accessibility-label :share-header-button
      :on-press            on-share}]]])

(defn user-info
  [{:keys [emoji-hash account public-key about]}]
  [react/view
   [react/view
    {:style styles/avatar}
    [avatar/avatar public-key :big]]

   [react/view
    {:style styles/user-info}
    [quo/text
     {:weight :semi-bold
      :size   :xx-large
      :style  {:margin-top 16}}
     (multiaccounts/displayed-name account)]
    [quo/text
     {:size  :large
      :style {:margin-top 8}}
     about]
    [quo/text
     {:size  :large
      :style {:margin-top 8}}
     emoji-hash]]])
