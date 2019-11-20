(ns status-im.ui.screens.wallet.account.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.accounts.views :as accounts]
            [status-im.ui.screens.wallet.accounts.sheets :as sheets]
            [reagent.core :as reagent]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [status-im.ui.components.list.views :as list]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.screens.wallet.account.styles :as styles]
            [status-im.ui.screens.wallet.transactions.views :as history]
            [status-im.ethereum.core :as ethereum]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.utils.money :as money]
            [status-im.wallet.utils :as wallet.utils]))

(def state (reagent/atom {:tab :assets}))

(defn toolbar-view [title]
  [react/view
   [toolbar/toolbar {:transparent? true}
    toolbar/default-nav-back
    [toolbar/content-title title]
    [toolbar/actions
     [{:icon      :main-icons/more
       :icon-opts {:color :black}
       :handler   #(re-frame/dispatch [:bottom-sheet/show-sheet
                                       {:content        sheets/account-settings
                                        :content-height 60}])}]]]])

(defn button [label icon handler]
  [react/touchable-highlight {:on-press handler :style {:flex 1}}
   [react/view {:flex 1 :align-items :center :justify-content :center}
    [react/view {:flex-direction :row :align-items :center}
     [icons/icon icon {:color colors/white}]
     [react/text {:style {:margin-left 8 :color colors/white}} label]]]])

(views/defview account-card [{:keys [address color] :as account}]
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:account-portfolio-value address]
                  window-width    [:dimensions/window-width]]
    [react/view {:style (styles/card window-width color)}
     [react/view {:padding 16 :padding-bottom 12 :flex 1 :justify-content :space-between}
      [react/nested-text {:style {:color       colors/white-transparent :line-height 38
                                  :font-weight "600" :font-size 32}}
       [{:style {:color colors/white}} portfolio-value]
       " "
       (:code currency)]
      [react/text {:number-of-lines 1 :ellipsize-mode :middle
                   :style           {:width (/ window-width 3)
                                     :line-height 22 :font-size 13
                                     :font-family "monospace"
                                     :color colors/white-transparent-70}}
       (ethereum/normalized-hex address)]]
     [react/view {:position :absolute :top 12 :right 12}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-popover {:view :share-account :address address}])}
       [icons/icon :main-icons/share {:color colors/white
                                      :accessibility-label :share-wallet-address-icon}]]]
     [react/view {:height                     52 :background-color colors/black-transparent-20
                  :border-bottom-right-radius 8 :border-bottom-left-radius 8 :flex-direction :row}
      [button
       (i18n/label :t/wallet-send)
       :main-icons/send
       #(re-frame/dispatch [:wallet/prepare-transaction-from-wallet account])]
      [react/view {:style styles/divider}]
      [button
       (i18n/label :t/receive)
       :main-icons/receive
       #(re-frame/dispatch [:show-popover {:view :share-account :address address}])]]]))

(defn render-collectible [address]
  (fn [{:keys [name icon amount] :as collectible}]
    (let [items-number (money/to-fixed amount)
          details?     (pos? items-number)]
      [list-item/list-item
       {:title       (wallet.utils/display-symbol collectible)
        :subtitle    name
        :icon        [list/item-image icon]
        :on-press    (when details?
                       #(re-frame/dispatch
                         [:show-collectibles-list collectible address]))
        :accessories [items-number :chevron]}])))

(views/defview transactions [address]
  (views/letsubs [{:keys [transaction-history-sections]}
                  [:wallet.transactions.history/screen address]]
    [history/history-list transaction-history-sections]))

(views/defview assets-and-collections [address]
  (views/letsubs [{:keys [tokens nfts]} [:wallet/visible-assets-with-values address]
                  currency [:wallet/currency]]
    (let [{:keys [tab]} @state]
      [react/view {:flex 1}
       [react/view {:flex-direction :row :margin-bottom 8 :padding-horizontal 4}
        [accounts/tab-title state :assets (i18n/label :t/wallet-assets) (= tab :assets)]
        [accounts/tab-title state :nft (i18n/label :t/wallet-collectibles) (= tab :nft)]
        [accounts/tab-title state :history (i18n/label :t/history) (= tab :history)]]
       (cond
         (= tab :assets)
         [list/flat-list {:data               tokens
                          :default-separator? false
                          :key-fn             :name
                          :footer             [react/view
                                               {:style {:height     tabs.styles/tabs-diff
                                                        :align-self :stretch}}]
                          :render-fn          (accounts/render-asset (:code currency))}]
         (= tab :nft)
         (if (seq nfts)
           [list/flat-list {:data               nfts
                            :default-separator? false
                            :key-fn             :name
                            :footer             [react/view
                                                 {:style {:height     tabs.styles/tabs-diff
                                                          :align-self :stretch}}]
                            :render-fn          (render-collectible address)}]
           [react/view {:align-items :center :margin-top 32}
            [react/text {:style {:color colors/gray}}
             (i18n/label :t/no-collectibles)]])
         (= tab :history)
         [transactions address])])))

(views/defview account []
  (views/letsubs [{:keys [name address] :as account} [:current-account]]
    [react/view {:flex 1 :background-color colors/white}
     [toolbar-view name]
     [react/scroll-view
      [react/view {:padding-left 16}
       [react/scroll-view {:horizontal true}
        [react/view {:flex-direction :row :padding-top 8 :padding-bottom 12}
         [account-card account]]]]
      [assets-and-collections address]]]))
