(ns status-im.ui.screens.wallet.send.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.accounts.common :as common]))

(views/defview assets
  [address]
  (views/letsubs [{:keys [tokens]} [:wallet/visible-assets-with-values address]
                  currency         [:wallet/currency]]
    [:<>
     (for [token tokens]
       ^{:key (str (:symbol token))}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:wallet.send/set-symbol (:symbol token)])}
        [common/render-asset token nil nil (:code currency)]])]))

(defn render-account
  [account _ _ {:keys [field event]}]
  [quo/list-item
   {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
    :title    (:name account)
    :on-press #(re-frame/dispatch [event field account])}])

(views/defview accounts-list
  [field event]
  (views/letsubs [accounts                [:multiaccount/visible-accounts]
                  accounts-whithout-watch [:visible-accounts-without-watch-only]]
    [list/flat-list
     {:data        (if (= :to field) accounts accounts-whithout-watch)
      :key-fn      :address
      :render-data {:field field
                    :event event}
      :render-fn   render-account}]))

(defn show-accounts-list
  []
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (js/setTimeout #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                      {:content        (fn [] [accounts-list :to :wallet.send/set-field])
                                       :content-height 300}])
                 400))

(defn choose-recipient
  []
  [react/view
   (for [item [{:title               (i18n/label :t/accounts)
                :icon                :main-icons/profile
                :theme               :accent
                :accessibility-label :chose-recipient-accounts-button
                :on-press            show-accounts-list}
               {:title               (i18n/label :t/scan-qr)
                :icon                :main-icons/qr
                :theme               :accent
                :accessibility-label :chose-recipient-scan-qr
                :on-press            #(re-frame/dispatch [:wallet.send/qr-scanner
                                                          {:handler :wallet.send/qr-scanner-result}])}
               {:title               (i18n/label :t/recipient-code)
                :icon                :main-icons/address
                :theme               :accent
                :accessibility-label :choose-recipient-recipient-code
                :on-press            #(re-frame/dispatch [:wallet.send/navigate-to-recipient-code])}]]
     ^{:key item}
     [quo/list-item item])])
