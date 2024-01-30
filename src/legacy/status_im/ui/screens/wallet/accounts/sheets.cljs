(ns legacy.status-im.ui.screens.wallet.accounts.sheets
  (:require
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn accounts-options
  [mnemonic]
  (fn []
    [:<>
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/wallet-manage-accounts)
       :icon                :main-icons/account
       :accessibility-label :wallet-manage-accounts
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :manage-accounts])}]
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/wallet-manage-assets)
       :icon                :main-icons/token
       :accessibility-label :wallet-manage-assets
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :wallet-settings-assets])}]
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/scan-tokens)
       :icon                :main-icons/refresh
       :accessibility-label :wallet-scan-token
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet-legacy/update-balances nil true])}]
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/set-currency)
       :icon                :main-icons/language
       :accessibility-label :wallet-set-currency
       :on-press            #(hide-sheet-and-dispatch
                              [:navigate-to :currency-settings])}]
     [list.item/list-item
      {:theme    :accent
       :title    (i18n/label :t/view-signing)
       :icon     :main-icons/info
       :on-press #(hide-sheet-and-dispatch
                   [:show-popover {:view :signing-phrase}])}]
     (when mnemonic
       [list.item/list-item
        {:theme               :negative
         :title               (i18n/label :t/wallet-backup-recovery-title)
         :icon                :main-icons/security
         :accessibility-label :wallet-backup-recovery-title
         :on-press            #(hide-sheet-and-dispatch
                                [:navigate-to :backup-seed])}])]))

(defn account-card-actions
  [account type wallet]
  [react/view
   (when-not (= type :watch)
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/wallet-send)
       :icon                :main-icons/send
       :accessibility-label :send-transaction-button
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet-legacy/prepare-transaction-from-wallet account])}])
   [list.item/list-item
    {:theme               :accent
     :title               (i18n/label :t/share)
     :icon                :main-icons/share
     :accessibility-label :share-account-button
     :on-press            #(hide-sheet-and-dispatch
                            [:wallet-legacy/share-popover (:address account)])}]
   (when-not wallet
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/hide)
       :icon                :main-icons/hide
       :accessibility-label :hide-account-button
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet-legacy.accounts/save-account account {:hidden true}])}])])

(defn add-account
  []
  (let [keycard? @(re-frame/subscribe [:keycard-multiaccount?])]
    [react/view
     [list.item/list-item
      {:title               (i18n/label :t/generate-a-new-account)
       :theme               :accent
       :icon                :main-icons/add
       :accessibility-label :add-account-sheet-generate
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet-legacy.accounts/start-adding-new-account
                               {:type :generate}])}]
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/add-a-watch-account)
       :icon                :main-icons/show
       :accessibility-label :add-account-sheet-watch
       :on-press            #(hide-sheet-and-dispatch
                              [:wallet-legacy.accounts/start-adding-new-account
                               {:type :watch}])}]
     (when-not keycard?
       [list.item/list-item
        {:title               (i18n/label :t/enter-a-seed-phrase)
         :theme               :accent
         :icon                :main-icons/text
         :accessibility-label :add-account-sheet-seed
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet-legacy.accounts/start-adding-new-account
                                 {:type :seed}])}])
     (when-not keycard?
       [list.item/list-item
        {:title               (i18n/label :t/enter-a-private-key)
         :theme               :accent
         :icon                :main-icons/address
         :accessibility-label :add-account-sheet-private-key
         :on-press            #(hide-sheet-and-dispatch
                                [:wallet-legacy.accounts/start-adding-new-account
                                 {:type :key}])}])]))

(defn account-settings
  []
  [react/view
   [list.item/list-item
    {:theme               :accent
     :title               (i18n/label :t/account-settings)
     :accessibility-label :account-settings-bottom-sheet
     :icon                :main-icons/info
     :on-press            #(hide-sheet-and-dispatch
                            [:navigate-to :account-settings])}]])
