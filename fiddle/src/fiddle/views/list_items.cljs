(ns fiddle.views.list-items
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-item.views :as list-item]
            [fiddle.frame :as frame]
            [status-im.ui.components.badge :as badge]))

(defn item [name content]
  [react/view
   [react/text {:style {:color colors/gray :margin-bottom 10 :margin-top 40}} name]
   [react/view {:background-color :white :width frame/width}
    content]])

(defn list-header []
  [react/view
   {:background-color colors/gray-lighter
    :flex             1
    :padding          20
    :flex-direction   :row
    :flex-wrap        :wrap}
   [react/view {:width 375}
    [react/view {:background-color :white}
     [list-item/list-item {:type :section-header :title "Header title"}]]]])

(defn list-items []
  [react/view
   {:background-color colors/gray-lighter
    :flex             1
    :padding          20
    :flex-direction   :row
    :flex-wrap        :wrap}
   [react/view {:margin-right 10}
    [item "Default with Image"
     [list-item/list-item
      {:title    "George"
       :on-press #()
       :icon     [react/view
                  {:width            40
                   :height           40
                   :border-radius    20
                   :background-color colors/green}]}]]
    [item "Default with Image, icon"
     [list-item/list-item
      {:title       "George"
       :on-press    #()
       :icon        [react/view
                     {:width            40
                      :height           40
                      :border-radius    20
                      :background-color colors/green}]
       :accessories [:main-icons/more]}]]
    [item "With radio button (TODO!)"
     [list-item/list-item
      {:title    "George"
       :on-press #()
       :icon     [react/view
                  {:width            40
                   :height           40
                   :border-radius    20
                   :background-color colors/green}]}]]]
   [react/view {:margin-right 10}
    [item "Default wIth icon in circle"
     [list-item/list-item
      {:title    "Clear History"
       :theme    :action
       :icon     :main-icons/close
       :on-press #()}]]
    [item "Default wIth icon in circle, chevron"
     [list-item/list-item
      {:title       "Get a ENS username?"
       :icon        :main-icons/address
       :on-press    #()
       :accessories [:chevron]}]]
    [item "Default wIth icon in circle, chevron and accessory"
     [list-item/list-item
      {:title       "Contacts"
       :icon        :main-icons/in-contacts
       :on-press    #()
       :accessories [[react/text {:style {:color colors/gray}} "4"]
                     :chevron]}]]
    [item "Default wIth icon in circle, chevron and badge"
     [list-item/list-item
      {:title    "Privacy and Security"
       :icon     :main-icons/profile
       :on-press #()
       :accessories [[badge/badge "333"] :chevron]}]]
    [item "Default wIth icon in circle, switch"
     [list-item/list-item
      {:title    "Notifications"
       :icon     :main-icons/notification
       :on-press #()
       :accessories
       [[react/switch {}]]}]]
    [item "Default wIth icon in circle, red"
     [list-item/list-item
      {:title    "Delete and Leave"
       :theme    :action-red
       :icon     :main-icons/delete
       :on-press #()}]]]
   [react/view {:margin-right 10}
    [item "Two lines with icon in circle, chevron"
     [list-item/list-item
      {:title       "alex.stateofus.eth"
       :subtitle    "ENS name"
       :icon        :main-icons/address
       :accessories [:chevron]
       :on-press    #()}]]
    [item "Two lines with icon in circle"
     [list-item/list-item
      {:title    "alex.stateofus.eth"
       :subtitle "ENS name"
       :icon     :main-icons/address
       :on-press #()}]]
    [item "Two lines with icon in circle, blue title"
     [list-item/list-item
      {:title    "Add or Create a Profile"
       :subtitle "Requires signout"
       :theme    :action
       :icon     :main-icons/address
       :on-press #()}]]
    [item "Two lines with dapp icon,title and subtitle"
     [list-item/list-item
      {:title    "CryptoKitties"
       :subtitle "https://cryptokitties.co"
       :icon     [react/view {:width            40
                              :height           40
                              :border-radius    20
                              :background-color colors/green}]
       :on-press #()}]]]
   [react/view {:margin-right 10}
    [item "Default Small"
     [list-item/list-item
      {:title    "Everybody"
       :type     :small
       :on-press #()}]]
    [item "Default Small button"
     [list-item/list-item
      {:title    "Change Passcode"
       :type     :small
       :theme    :action
       :on-press #()}]]
    [item "Default Small red button"
     [list-item/list-item
      {:title    "Delete all Contacts"
       :type     :small
       :theme    :action-red
       :on-press #()}]]
    [item "Default Small with chevron"
     [list-item/list-item
      {:title       "Recovery Phrase"
       :type        :small
       :on-press    #()
       :accessories [:chevron]}]]
    [item "Default Small with chevron and accessory"
     [list-item/list-item
      {:title       "Main Currency"
       :type        :small
       :on-press    #()
       :accessories [[react/text {:style {:color colors/gray}} "USD"]
                     :chevron]}]]
    [item "Default Small with chevron, badge"
     [list-item/list-item
      {:title    "Recovery Phrase"
       :type     :small
       :on-press #()
       :accessories [[badge/badge "2"] :chevron]}]]]
   [react/view {:margin-right 10}
    [item "Long title"
     [list-item/list-item
      {:title       "loooooooooong loooooooooooooong looooooong title"
       :subtitle    "ENS name"
       :icon        :main-icons/address
       :accessories [:chevron]
       :on-press    #()}]]
    [item "Long subtitle"
     [list-item/list-item
      {:title       "alex.stateofus.eth"
       :subtitle    "loooooooooooooooong loooooong looooooong subtitle"
       :icon        :main-icons/address
       :accessories [:chevron]
       :on-press    #()}]]
    [item "Title-prefix + long title"
     [list-item/list-item
      {:title-prefix       "From"
       :title-prefix-width 45
       :title              "title-prefix and loooooong title really loooonglooo00oooong"
       :type               :small
       :on-press           #()}]]]])
