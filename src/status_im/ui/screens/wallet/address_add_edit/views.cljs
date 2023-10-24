(ns status-im.ui.screens.wallet.address-add-edit.views
  (:require
    [reagent.core :as reagent]
    [clojure.string :as string]
    [utils.i18n :as i18n]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [status-im.ui.components.colors :as colors]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.list.item :as list.item]
    [status-im.ui.components.list.views :as list]
    [status-im.utils.utils :as utils]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [account-name (reagent/atom "Account 1")
        account-color (reagent/atom :purple)
        account-emoji (reagent/atom "🍑")
        on-change-name #(reset! account-name %)
        on-change-color #(reset! account-color %)
        on-change-emoji #(reset! account-emoji %)
        input-value (reagent/atom "")]
    (fn []
      [create-or-edit-account/view
       {:page-nav-right-side [{:icon-name :i/delete
                               :on-press  #(js/alert "Delete account: to be implemented")}]
        :account-name        @account-name
        :account-emoji       @account-emoji
        :account-color       @account-color
        :on-change-name      on-change-name
        :on-change-color     on-change-color
        :on-change-emoji     on-change-emoji}
       [quo/input
        {:placeholder     (str "0x123abc... " (string/lower-case (i18n/label :t/or)) " bob.eth")
         :container-style {:margin-right 12
                           :flex         1}
         :weight          :monospace
         :on-change       #(reset! input-value %)
         :default-value   @input-value}]
       [quo/bottom-actions
        {:button-one-label     (i18n/label :t/continue)
         :button-one-disabled? false                       ;; TODO: use variable instead of hardcoded value
         :button-one-press     #(re-frame/dispatch [:navigate-to :wallet-account])}]])))

(def address-add-edit (quo.theme/with-theme view-internal))
