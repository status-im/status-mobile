(ns quo.components.buttons.wallet-ctas.view
  (:require
    [quo.components.buttons.wallet-button.view :as wallet-button]
    [quo.components.buttons.wallet-ctas.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))


(defn action-button
  [{:keys [icon text on-press theme accessibility-label]}]
  [rn/view
   {:style               style/button-container
    :accessibility-label accessibility-label}
   [wallet-button/view
    {:icon     icon
     :on-press on-press}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:margin-top 4
              :color      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}} text]])

(defn view
  [{:keys [buy-action send-action receive-action bridge-action container-style]}]
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
      [action-button
       {:icon                :i/bridge
        :text                (i18n/label :t/bridge)
        :on-press            bridge-action
        :theme               theme
        :accessibility-label :bridge}]]]))
