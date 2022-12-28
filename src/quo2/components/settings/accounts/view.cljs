(ns quo2.components.settings.accounts.view
  (:require [quo.react-native :as rn]
            [quo2.components.avatars.account-avatar :as av]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.settings.accounts.style :as style]))

(defn card-background
  [{:keys [custom-color]}]
  [:<>
   [rn/view {:style (style/background-top custom-color)}]
   [rn/view {:style (style/background-bottom)}]])

(defn avatar
  [avatar-props]
  [rn/view {:style (style/avatar-border)}
   [av/account-avatar (assoc avatar-props :size 48)]])

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
  [{:keys [account-name account-address avatar-icon custom-color on-press-menu]}]
  [rn/view {:style style/card}
   [card-background {:custom-color custom-color}]
   [rn/view {:style style/card-top}
    [avatar
     {:color custom-color
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
