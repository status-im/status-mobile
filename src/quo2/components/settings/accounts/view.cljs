(ns quo2.components.settings.accounts.view
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.settings.accounts.style :as style]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

(defn card-background
  [{:keys [customization-color theme]}]
  [:<>
   [rn/view {:style (style/background-top customization-color theme)}]
   [rn/view {:style (style/background-bottom theme)}]])

(defn avatar
  [avatar-props theme]
  [rn/view {:style (style/avatar-border theme)}
   [account-avatar/view (assoc avatar-props :size 48)]])

(defn menu-button
  [{:keys [on-press theme]}]
  [rn/view {:style style/menu-button-container}
   [button/button
    {:container-style (style/menu-button-color theme)
     :type            :grey
     :icon-only?      true
     :size            24
     :on-press        on-press}
    :i/more]])

(defn- account-internal
  [{:keys [account-name account-address avatar-icon
           customization-color on-press-menu theme]}]
  [rn/view {:style style/card}
   [card-background
    {:customization-color customization-color
     :theme               theme}]
   [rn/view {:style style/card-top}
    [avatar
     {:color customization-color
      :icon  avatar-icon}
     theme]
    [menu-button
     {:on-press on-press-menu
      :theme    theme}]]
   [rn/view {:style style/card-bottom}
    [text/text
     {:size   :paragraph-1
      :weight :semi-bold}
     account-name]
    [text/text
     {:style  (style/address-text theme)
      :size   :paragraph-2
      :weight :medium}
     account-address]]])

(def account (quo.theme/with-theme account-internal))
