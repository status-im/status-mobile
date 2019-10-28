(ns status-im.ui.screens.wallet.accounts.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn accounts-options [seed-backed-up?]
  (fn []
    [react/view
     [list-item/list-item
      {:theme               :action
       :title               :t/wallet-manage-assets
       :icon                :main-icons/token
       :accessibility-label :wallet-manage-assets
       :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-settings-assets])}]
     [list-item/list-item
      {:theme               :action
       :title               :t/set-currency
       :icon                :main-icons/language
       :accessibility-label :wallet-set-currency
       :on-press            #(hide-sheet-and-dispatch [:navigate-to :currency-settings])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/view-signing
       :icon     :main-icons/info
       :on-press #(hide-sheet-and-dispatch [:show-popover {:view :signing-phrase}])}]
     (when-not seed-backed-up?
       [list-item/list-item
        {:theme               :action-destructive
         :title               :t/wallet-backup-recovery-title
         :icon                :main-icons/security
         :accessibility-label :wallet-backup-recovery-title
         :on-press            #(hide-sheet-and-dispatch [:navigate-to :backup-seed])}])]))

(defn send-receive [address]
  [react/view
   [list-item/list-item
    {:theme               :action
     :title               :t/wallet-send
     :icon                :main-icons/send
     :accessibility-label :send-transaction-button
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :wallet-send-transaction address])}]
   [list-item/list-item
    {:theme               :action
     :title               :t/receive
     :icon                :main-icons/receive
     :accessibility-label :receive-transaction-button
     :on-press            #(hide-sheet-and-dispatch [:show-popover {:view :share-account :address address}])}]])

(defn add-account []
  [react/view
   [list-item/list-item
    {:theme    :action
     :title    :t/add-an-account
     :icon     :main-icons/add
     :on-press #(hide-sheet-and-dispatch [:navigate-to :add-new-account])}]
   [list-item/list-item
    {:theme     :action
     :title     :t/add-a-watch-account
     :icon      :main-icons/watch
     :disabled? true}]])

(defn account-settings []
  [react/view
   [list-item/list-item
    {:theme     :action
     :title     :t/account-settings
     :accessibility-label :account-settings-bottom-sheet
     :icon      :main-icons/info
     :on-press #(hide-sheet-and-dispatch [:navigate-to :account-settings])}]
   ;; Commented out for v1
   #_[list-item/list-item
      {:theme     :action
       :title     :t/export-account
       :icon      :main-icons/copy
       :disabled? true}]])
