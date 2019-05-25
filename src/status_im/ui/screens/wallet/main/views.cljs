(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.wallet.onboarding.views :as onboarding.views]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.main.styles :as styles]
            [status-im.ui.screens.wallet.settings.views :as settings]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.toolbar.actions :as action]
            status-im.ui.screens.wallet.collectibles.etheremon.views
            status-im.ui.screens.wallet.collectibles.cryptostrikers.views
            status-im.ui.screens.wallet.collectibles.cryptokitties.views
            status-im.ui.screens.wallet.collectibles.superrare.views
            status-im.ui.screens.wallet.collectibles.kudos.views
            [status-im.ui.components.status-bar.view :as status-bar.view]
            [status-im.ui.screens.wallet.transactions.views :as transactions.views]
            [status-im.ui.components.chat-icon.screen :as chat-icon]))

(defn toolbar-modal [modal-history?]
  [react/view
   [status-bar.view/status-bar {:type :modal-wallet}]
   [toolbar/toolbar {:transparent? true}
    [toolbar/nav-button (action/close-white action/default-handler)]
    [toolbar/content-wrapper]
    [toolbar/actions
     [{:icon      (if modal-history? :main-icons/wallet :main-icons/two-arrows)
       :icon-opts {:color               :white
                   :accessibility-label (if modal-history? :wallet-modal-button :transaction-history-button)}
       :handler #(re-frame/dispatch [:set-in [:wallet :modal-history?] (not modal-history?)])}]]]])

(defn- total-section [value currency]
  [react/nested-text {:style styles/total-balance-text}
   (when (and
          (not= "0" value)
          (not= "..." value))
     "~")
   [{:style                styles/total-balance-value
     :accessibility-label :total-amount-value-text}
    value]
   [{:style               styles/total-balance-currency
     :accessibility-label :total-amount-currency-text}
    (str " " (:code currency))]])

(defn- backup-seed-phrase []
  [react/touchable-highlight {:on-press       #(re-frame/dispatch [:navigate-to :backup-seed])
                              :style          {:background-color colors/blue}
                              :underlay-color colors/blue}
   [react/view {:style styles/backup-seed-phrase-container}
    [react/view
     [react/i18n-text {:style styles/backup-seed-phrase-title
                       :key   :wallet-backup-recovery-title}]
     [react/i18n-text {:style styles/backup-seed-phrase-description
                       :key   :wallet-backup-recovery-description}]]
    [react/view {:style {:flex 1}}]
    [react/view {:style {:align-items     :center
                         :justify-content :center}}
     [vector-icons/icon :main-icons/next {:color colors/white}]]]])

(def actions
  [{:label               (i18n/label :t/wallet-send)
    :accessibility-label :send-transaction-button
    :icon                :main-icons/send
    :action              #(re-frame/dispatch [:navigate-to :wallet-send-transaction])}
   {:label               (i18n/label :t/receive)
    :accessibility-label :receive-transaction-button
    :icon                :main-icons/receive
    :action              #(re-frame/dispatch [:navigate-to :wallet-request-transaction])}
   {:label               (i18n/label :t/transaction-history)
    :accessibility-label :transaction-history-button
    :icon                :main-icons/history
    :action              #(re-frame/dispatch [:navigate-to :transactions-history])}])

(defn- render-asset [currency]
  (fn [{:keys [symbol icon decimals amount color] :as token}]
    (let [asset-value (re-frame/subscribe [:asset-value symbol decimals (-> currency :code keyword)])]
      [react/view {:style styles/asset-item-container}
       [list/item
        (if icon
          [list/item-image icon]
          [chat-icon/custom-icon-view-list (:name token) color])
        [react/view {:style styles/asset-item-value-container}
         [react/text {:style               styles/asset-item-value
                      :number-of-lines     1
                      :ellipsize-mode      :tail
                      :accessibility-label (str (-> symbol name clojure.string/lower-case) "-asset-value-text")}
          (wallet.utils/format-amount amount decimals)]
         [react/text {:style           styles/asset-item-currency
                      :number-of-lines 1}
          (wallet.utils/display-symbol token)]]
        [react/text {:style           styles/asset-item-price
                     :number-of-lines 1}
         (or @asset-value "...")]]])))

(def item-icon-forward
  [list/item-icon {:icon      :main-icons/next
                   :style     {:width 12}
                   :icon-opts {:color :gray}}])

(defn- render-collectible [address-hex {:keys [symbol name icon amount] :as collectible}]
  (let [items-number (money/to-fixed amount)
        details?     (pos? items-number)]
    [react/touchable-highlight
     (when details?
       {:on-press #(re-frame/dispatch [:show-collectibles-list address-hex collectible])})
     [react/view {:style styles/asset-item-container}
      [list/item
       [list/item-image icon]
       [react/view {:style styles/asset-item-value-container}
        [react/text {:style               styles/asset-item-value
                     :number-of-lines     1
                     :ellipsize-mode      :tail
                     :accessibility-label (str (-> symbol clojure.core/name clojure.string/lower-case)
                                               "-collectible-value-text")}
         (or items-number "...")]
        [react/text {:style           styles/asset-item-currency
                     :number-of-lines 1}
         name]]
       (when details?
         item-icon-forward)]]]))

(defn group-assets [v]
  (group-by #(if (:nft? %) :nfts :tokens) v))

(defn- asset-section [assets currency address-hex]
  (let [{:keys [tokens nfts]} (group-assets assets)]
    [list/section-list
     {:scroll-enabled     false
      :style              styles/asset-section
      :key-fn             (comp str :symbol)
      :render-section-header-fn (fn [{:keys [title data]}]
                                  (when (not-empty data)
                                    [react/text {:style styles/asset-section-header}
                                     title]))
      :sections           [{:title     (i18n/label :t/wallet-assets)
                            :key       :assets
                            :data      tokens
                            :render-fn (render-asset currency)}
                           {:title     (i18n/label :t/wallet-collectibles)
                            :key       :collectibles
                            :data      nfts
                            :render-fn #(render-collectible address-hex %)}]}]))

(defn snackbar [error-message]
  [react/view styles/snackbar-container
   [react/text {:style styles/snackbar-text}
    (i18n/label error-message)]])

(views/defview wallet-root [modal?]
  (views/letsubs [assets          [:wallet/visible-assets-with-amount]
                  currency        [:wallet/currency]
                  portfolio-value [:portfolio-value]
                  {:keys [seed-backed-up?]} [:account/account]
                  error-message   [:wallet/error-message]
                  address-hex     [:account/hex-address]]
    [react/view styles/main-section
     [status-bar.view/status-bar {:type :wallet-tab}]
     [settings/toolbar-view]
     [react/scroll-view {:end-fill-color colors/white
                         :refresh-control
                         (reagent/as-element
                          [react/refresh-control {:on-refresh #(re-frame/dispatch [:wallet.ui/pull-to-refresh])
                                                  :tint-color :white
                                                  :refreshing false}])}
      (if error-message
        [snackbar error-message]
        [total-section portfolio-value currency])
      ;; this view is a hack to hide the 1px high white line gap on android
      (when platform/android?
        [react/view {:style {:background-color colors/blue
                             :height 1
                             :margin -1}}])
      (when (and (not modal?)
                 (not seed-backed-up?)
                 (some (fn [{:keys [amount]}]
                         (and amount (not (.isZero amount))))
                       assets))
        [backup-seed-phrase])
      [react/view {:style {:background-color colors/blue}}
       [list/flat-list
        {:data      actions
         :key-fn    (fn [_ i] (str i))
         :render-fn #(list/render-action % {:action-label-style {:font-size 17}})}]]
      [asset-section assets currency address-hex]
      (when platform/ios?
        [react/view {:style styles/scroll-bottom}])]]))

(views/defview wallet []
  (views/letsubs [{:keys [wallet-set-up-passed?]} [:account/account]]
    (if (not wallet-set-up-passed?)
      [onboarding.views/onboarding]
      [wallet-root])))
