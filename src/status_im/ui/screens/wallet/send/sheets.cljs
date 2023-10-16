(ns status-im.ui.screens.wallet.send.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.chat-icon.screen :as chat-icon]
    [status-im.ui.components.list.item :as list.item]
    [status-im.ui.components.list.views :as list]
    [status-im.ui.components.react :as react]
    [status-im.ui.screens.wallet.accounts.common :as common]))

(defn asset
  [currency token]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:wallet.send/set-symbol (:symbol token)])}
   [common/render-asset token nil nil (:code currency)]])

(views/defview assets
  [address]
  (views/letsubs [{:keys [tokens]} [:wallet/visible-assets-with-values address]
                  currency         [:wallet/currency]]
    [react/view
     {:style {:height 300}}
     [list/flat-list
      {:data      tokens
       :key-fn    :symbol
       :render-fn (partial asset currency)}]]))

(defn render-account
  [account _ _ {:keys [field event]}]
  [list.item/list-item
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
