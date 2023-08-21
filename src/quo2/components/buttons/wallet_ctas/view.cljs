(ns quo2.components.buttons.wallet-ctas.view
  (:require
    [quo2.components.buttons.wallet-button.view :as wallet-button]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [quo2.components.buttons.wallet-ctas.style :as style]))


(defn action-button
  [{:keys [icon text on-press theme accessibility-label]}]
  [rn/view
   {:style               style/button-container
    :accessibility-label accessibility-label}
   [wallet-button/view
    {:icon          icon
     :on-press      on-press
     :on-long-press #(js/alert "long pressed")}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:margin-top 4
              :color      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}} text]])

(defn view-internal
  [{:keys [theme buy-action send-action receive-action bridge-action]}]
  [rn/view {:style style/container}
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
     :accessibility-label :bridge}]])

(def view (quo.theme/with-theme view-internal))
