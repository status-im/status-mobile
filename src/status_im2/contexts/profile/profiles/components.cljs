(ns status-im2.contexts.profile.profiles.components
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [status-im2.contexts.profile.profiles.style :as styles]
            [status-im2.contexts.chat.messages.avatar.view :as avatar]
            [status-im.multiaccounts.core :as multiaccounts]))

(def top-background-view
  [rn/view
   {:style styles/top-background-view}])

(defn button
  [{:keys [icon accessibility-label on-press style]}]
  [quo/button
   {:icon-only?          true
    :type                :grey
    :background          :blur
    :size                32
    :container-style     style
    :accessibility-label accessibility-label
    :on-press            on-press}
   icon])

(defn fixed-toolbar
  [{:keys [on-close on-switch-profile on-show-qr on-share]}]
  [rn/view
   {:style styles/toolbar}
   [button
    {:icon                :i/close
     :accessibility-label :close-header-button
     :on-press            on-close
     :style               {:margin-left 20}}]
   [rn/view
    {:style styles/right-accessories}
    [button
     {:icon                :i/multi-profile
      :accessibility-label :multi-profile-header-button
      :on-press            on-switch-profile}]
    [button
     {:icon                :i/qr-code
      :accessibility-label :qr-header-button
      :on-press            on-show-qr
      :style               {:margin-horizontal 14}}]
    [button
     {:icon                :i/share
      :accessibility-label :share-header-button
      :on-press            on-share}]]])

(defn user-info
  [{:keys [emoji-hash account public-key about]}]
  [rn/view
   [rn/view
    {:style styles/avatar}
    [avatar/avatar public-key :big]]

   [rn/view
    {:style styles/user-info}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:margin-top 16}}
     (multiaccounts/displayed-name account)]
    [quo/text
     {:size  :paragraph-1
      :style {:margin-top 8}}
     about]
    [quo/text
     {:size  :paragraph-1
      :style {:margin-top 8}}
     emoji-hash]]])
