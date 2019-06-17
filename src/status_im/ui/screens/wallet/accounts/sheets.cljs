(ns status-im.ui.screens.wallet.accounts.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn accounts-options [seed-backed-up?]
  (fn []
    [react/view
     [action-button/action-button {:label     (i18n/label :t/wallet-manage-assets)
                                   :icon      :main-icons/token
                                   :icon-opts {:color :blue}
                                   :on-press  #(hide-sheet-and-dispatch [:navigate-to :wallet-settings-assets])}]
     [action-button/action-button {:label     (i18n/label :t/set-currency)
                                   :icon      :main-icons/language
                                   :icon-opts {:color :blue}
                                   :on-press  #(hide-sheet-and-dispatch [:navigate-to :currency-settings])}]
     [action-button/action-button-disabled {:label     (i18n/label :t/view-signing)
                                            :icon      :main-icons/info
                                            :icon-opts {:color :blue}}]
     (when-not seed-backed-up?
       [action-button/action-button {:label        (i18n/label :t/wallet-backup-recovery-title)
                                     :icon         :main-icons/security
                                     :icon-opts    {:color colors/red}
                                     :label-style  {:color colors/red}
                                     :cyrcle-color (colors/alpha colors/red 0.1)
                                     :on-press     #(hide-sheet-and-dispatch [:navigate-to :backup-seed])}])]))

(defn send-receive []
  [react/view
   [action-button/action-button {:label               (i18n/label :t/wallet-send)
                                 :icon                :main-icons/send
                                 :accessibility-label :send-transaction-button
                                 :icon-opts           {:color :blue}
                                 :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-send-transaction])}]
   [action-button/action-button {:label               (i18n/label :t/receive)
                                 :icon                :main-icons/receive
                                 :accessibility-label :receive-transaction-button
                                 :icon-opts           {:color :blue}
                                 :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-request-transaction])}]])

(defn add-account []
  [react/view
   [action-button/action-button-disabled {:label               (i18n/label :t/add-an-account)
                                          :icon                :main-icons/add
                                          :icon-opts           {:color :blue}
                                          :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-send-transaction])}]
   [action-button/action-button-disabled {:label               (i18n/label :t/add-a-watch-account)
                                          :icon                :main-icons/watch
                                          :icon-opts           {:color :blue}
                                          :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-request-transaction])}]])

(defn account-settings []
  [react/view
   [action-button/action-button-disabled {:label               (i18n/label :t/account-settings)
                                          :icon                :main-icons/info
                                          :icon-opts           {:color :blue}}]
   [action-button/action-button-disabled {:label               (i18n/label :t/export-account)
                                          :icon                 :main-icons/copy
                                          :icon-opts           {:color :blue}}]])