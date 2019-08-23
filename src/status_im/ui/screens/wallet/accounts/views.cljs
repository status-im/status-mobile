(ns status-im.ui.screens.wallet.accounts.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]
            [reagent.core :as reagent]
            [status-im.utils.money :as money]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.accounts.sheets :as sheets]
            [status-im.ethereum.core :as ethereum]
            [status-im.ui.screens.wallet.accounts.styles :as styles]))

(def state (reagent/atom {:tab :assets}))

(defn total-tilde [value]
  (when (and (not= "0" value) (not= "..." value)) "~"))

(views/defview account-card [{:keys [name color address] :as account}]
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:account-portfolio-value address]]
    [react/touchable-highlight {:on-press      #(re-frame/dispatch [:navigate-to :wallet-account account])
                                :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                    {:content        (fn [] [sheets/send-receive address])
                                                                     :content-height 130}])}
     [react/view {:style (styles/card color)}
      [react/view {:flex-direction :row :align-items :center :justify-content :space-between}
       [react/nested-text {:style {:color colors/white-transparent :font-weight "500"}}
        (total-tilde portfolio-value)
        [{:style {:color colors/white}} portfolio-value]
        " "
        (:code currency)]
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-popover {:view :share-account :address address}])}
        [icons/icon :main-icons/share {:color colors/white}]]]
      [react/view
       [react/text {:style {:color colors/white :font-weight "500" :line-height 22}} name]
       [react/text {:number-of-lines 1 :ellipsize-mode :middle
                    :style {:line-height 22 :font-size 13
                            :font-family "monospace"
                            :color colors/white-transparent-70}}
        address]]]]))

(defn add-card []
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                             {:content        sheets/add-account
                                                              :content-height 130}])}
   [react/view {:style styles/add-card}
    [react/view {:width       40 :height 40 :justify-content :center :border-radius 20
                 :align-items :center :background-color colors/blue-transparent-10 :margin-bottom 8}
     [icons/icon :main-icons/add {:color colors/blue}]]
    [react/text {:style {:color colors/blue}} (i18n/label :t/add-account)]]])

(defn tab-title [state key label active?]
  [react/view {:align-items :center}
   [react/touchable-highlight {:on-press #(swap! state assoc :tab key)
                               :underlay-color colors/gray-lighter
                               :style {:border-radius 8}}
    [react/view {:padding-horizontal 12 :padding-vertical 9}
     [react/text {:style {:font-weight "500" :color (if active? colors/black colors/gray) :line-height 22}}
      label]]]
   (when active?
     [react/view {:width 24 :height 3 :border-radius 4 :background-color colors/blue}])])

(defn render-asset [currency]
  (fn [{:keys [icon decimals amount color value] :as token}]
    [list-item/list-item
     {:title-prefix         (wallet.utils/format-amount amount decimals)
      :title                (wallet.utils/display-symbol token)
      :title-color-override colors/gray
      :subtitle             (str (if value value 0) " " currency)
      :icon                 (if icon
                              [list/item-image icon]
                              [chat-icon/custom-icon-view-list (:name token) color])}]))

(defn render-collectible [{:keys [name icon amount] :as collectible}]
  (let [items-number (money/to-fixed amount)
        details?     (pos? items-number)]
    [list-item/list-item
     {:title       (wallet.utils/display-symbol collectible)
      :subtitle    name
      :icon        [list/item-image icon]
      :on-press    (when details?
                     #(re-frame/dispatch
                       [:show-collectibles-list collectible]))
      :accessories [items-number :chevron]}]))

(views/defview assets-and-collections []
  (views/letsubs [{:keys [tokens nfts]} [:wallet/all-visible-assets-with-values]
                  currency [:wallet/currency]]
    (let [{:keys [tab]} @state]
      [react/view {:flex 1}
       [react/view {:flex-direction :row :margin-bottom 8 :margin-horizontal 4}
        [tab-title state :assets (i18n/label :t/wallet-assets) (= tab :assets)]
        [tab-title state :nft (i18n/label :t/wallet-collectibles) (= tab :nft)]]
       (if (= tab :assets)
         [list/flat-list {:data               tokens
                          :default-separator? false
                          :key-fn             :name
                          :footer             [react/view
                                               {:style {:height     tabs.styles/tabs-diff
                                                        :align-self :stretch}}]
                          :render-fn          (render-asset (:code currency))}]
         (if (seq nfts)
           [list/flat-list {:data               nfts
                            :default-separator? false
                            :key-fn             :name
                            :footer             [react/view
                                                 {:style {:height     tabs.styles/tabs-diff
                                                          :align-self :stretch}}]
                            :render-fn          render-collectible}]
           [react/view {:align-items :center :margin-top 32}
            [react/text {:style {:color colors/gray}}
             (i18n/label :t/no-collectibles)]]))])))

(views/defview total-value []
  (views/letsubs [currency        [:wallet/currency]
                  portfolio-value [:portfolio-value]]
    [react/view
     [react/nested-text {:style {:font-size 32 :color colors/gray :font-weight "600"}}
      (total-tilde portfolio-value)
      [{:style {:color colors/black}} portfolio-value]
      " "
      (:code currency)]
     [react/text {:style {:color colors/gray}} (i18n/label :t/wallet-total-value)]]))

(views/defview accounts-options []
  (views/letsubs [{:keys [seed-backed-up?]} [:multiaccount]]
    [react/view {:flex-direction :row :align-items :center}
     [react/view {:flex 1 :padding-left 16}
      (when-not seed-backed-up?
        [react/view {:flex-direction :row :align-items :center}
         [react/view {:width           14 :height 14 :background-color colors/gray :border-radius 7 :align-items :center
                      :justify-content :center :margin-right 9}
          [react/text {:style {:color colors/white :font-size 13 :font-weight "700"}} "!"]]
         [react/text {:style {:color colors/gray}
                      :accessibility-label :back-up-your-seed-phrase-warning} (i18n/label :t/back-up-your-seed-phrase)]])]
     [react/touchable-highlight {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                {:content        (sheets/accounts-options seed-backed-up?)
                                                                 :content-height (if seed-backed-up? 190 250)}])}
      [react/view {:height          toolbar.styles/toolbar-height :width toolbar.styles/toolbar-height :align-items :center
                   :justify-content :center}
       [icons/icon :main-icons/more {:accessibility-label :accounts-more-options}]]]]))

(views/defview accounts []
  (views/letsubs [{:keys [accounts]} [:multiaccount]]
    [react/scroll-view {:horizontal true}
     [react/view {:flex-direction :row :padding-top 11 :padding-bottom 12}
      (for [account accounts]
        ^{:key account}
        [account-card account])
      [add-card]]]))

(defn accounts-overview []
  [react/view {:flex 1}
   [status-bar/status-bar]
   [react/scroll-view
    [accounts-options]
    [react/view {:margin-top 8 :padding-horizontal 16}
     [total-value]
     [accounts]]
    [assets-and-collections]]])
