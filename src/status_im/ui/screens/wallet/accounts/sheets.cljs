(ns status-im.ui.screens.wallet.accounts.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn accounts-options [mnemonic]
  (fn []
    [react/view
     [list-item/list-item
      {:theme               :action
       :title               :t/wallet-manage-assets
       :icon                :main-icons/token
       :accessibility-label :wallet-manage-assets
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :wallet-settings-assets])}]
     [list-item/list-item
      {:theme               :action
       :title               :t/set-currency
       :icon                :main-icons/language
       :accessibility-label :wallet-set-currency
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :currency-settings])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/view-signing
       :icon     :main-icons/info
       :on-press #(hide-sheet-and-dispatch
                   [:show-popover {:view :signing-phrase}])}]
     (when mnemonic
       [list-item/list-item
        {:theme               :action-destructive
         :title               :t/wallet-backup-recovery-title
         :icon                :main-icons/security
         :accessibility-label :wallet-backup-recovery-title
         :on-press            #(hide-sheet-and-dispatch
                                [:navigate-to :profile-stack {:screen :backup-seed
                                                              :initial false}])}])]))

(defn send-receive [account type]
  [react/view
   (when-not (= type :watch)
     [list-item/list-item
      {:theme               :action
       :title               :t/wallet-send
       :icon                :main-icons/send
       :accessibility-label :send-transaction-button
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet/prepare-transaction-from-wallet account])}])
   [list-item/list-item
    {:theme               :action
     :title               :t/receive
     :icon                :main-icons/receive
     :accessibility-label :receive-transaction-button
     :on-press            #(hide-sheet-and-dispatch
                            [:show-popover {:view    :share-account
                                            :address (:address account)}])}]])

(defn add-account []
  (let [keycard? @(re-frame/subscribe [:keycard-multiaccount?])]
    [react/view
     [list-item/list-item
      {:title               :t/generate-a-new-account
       :theme               :action
       :icon                :main-icons/add
       :accessibility-label :add-account-sheet-generate
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet.accounts/start-adding-new-account
                               {:type :generate}])}]
     [list-item/list-item
      {:theme               :action
       :title               :t/add-a-watch-account
       :icon                :main-icons/show
       :accessibility-label :add-account-sheet-watch
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet.accounts/start-adding-new-account
                               {:type :watch}])}]
     (when-not keycard?
       [list-item/list-item
        {:title               :t/enter-a-seed-phrase
         :theme               :action
         :icon                :main-icons/text
         :accessibility-label :add-account-sheet-seed
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet.accounts/start-adding-new-account
                                 {:type :seed}])}])
     (when-not keycard?
       [list-item/list-item
        {:title               :t/enter-a-private-key
         :theme               :action
         :icon                :main-icons/address
         :accessibility-label :add-account-sheet-private-key
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet.accounts/start-adding-new-account
                                 {:type :key}])}])]))

(defn account-settings []
  [react/view
   [list-item/list-item
    {:theme               :action
     :title               :t/account-settings
     :accessibility-label :account-settings-bottom-sheet
     :icon                :main-icons/info
     :on-press            #(hide-sheet-and-dispatch
                            [:navigate-to :account-settings])}]])
