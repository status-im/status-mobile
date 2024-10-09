(ns quo.components.buttons.wallet-ctas.view
  (:require
    [quo.components.buttons.wallet-button.view :as wallet-button]
    [quo.components.buttons.wallet-ctas.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))


(defn action-button
  [{:keys [icon text on-press theme accessibility-label disabled?]}]
  [rn/view
   {:style               style/button-container
    :accessibility-label accessibility-label}
   [wallet-button/view
    {:icon      icon
     :disabled? disabled?
     :on-press  on-press}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/action-button-text theme disabled?)} text]])

(defn view
  [{:keys [buy-action send-action receive-action bridge-action swap-action bridge-disabled?
           swap-disabled? container-style]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style container-style}
     [rn/view {:style style/inner-container}
      [action-button
       {:icon                :i/add
        :text                (i18n/label :t/buy)
        :on-press            buy-action
        :theme               theme
        :accessibility-label :buy}]
      [action-button
       {:icon                :i/send
        :text                (i18n/label :t/send)
        :on-press            send-action
        :theme               theme
        :accessibility-label :send}]
      [action-button
       {:icon                :i/receive
        :text                (i18n/label :t/receive)
        :on-press            receive-action
        :theme               theme
        :accessibility-label :receive}]
      (when swap-action
        [action-button
         {:icon                :i/transaction
          :text                (i18n/label :t/swap)
          :on-press            swap-action
          :theme               theme
          :disabled?           swap-disabled?
          :accessibility-label :swap}])
      [action-button
       {:icon                :i/bridge
        :text                (i18n/label :t/bridge)
        :on-press            bridge-action
        :theme               theme
        :disabled?           bridge-disabled?
        :accessibility-label :bridge}]]]))
