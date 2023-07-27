(ns quo2.components.settings.accounts.view
  (:require [quo2.components.avatars.account-avatar :as account-avatar]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.settings.accounts.style :as style]
            [react-native.core :as rn]))

(defn card-background
  [{:keys [customization-color]}]
  [:<>
   [rn/view {:style (style/background-top customization-color)}]
   [rn/view {:style (style/background-bottom)}]])

(defn avatar
  [avatar-props]
  [rn/view {:style (style/avatar-border)}
   [account-avatar/account-avatar (assoc avatar-props :size 48)]])

(defn menu-button
  [{:keys [on-press]}]
  [rn/view {:style style/menu-button-container}
   [button/button
    {:style    (style/menu-button-color)
     :type     :gray
     :icon     true
     :size     24
     :on-press on-press}
    :i/more]])

(defn account
  [{:keys [account-name account-address avatar-icon customization-color on-press-menu]}]
  [rn/view {:style style/card}
   [card-background {:customization-color customization-color}]
   [rn/view {:style style/card-top}
    [avatar
     {:color customization-color
      :icon  avatar-icon}]
    [menu-button {:on-press on-press-menu}]]
   [rn/view {:style style/card-bottom}
    [text/text {:size :paragraph-1 :weight :semi-bold}
     account-name]
    [text/text
     {:style  (style/address-text)
      :size   :paragraph-2
      :weight :medium}
     account-address]]])
