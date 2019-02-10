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
            [status-im.ui.components.toolbar.actions :as action]
            status-im.ui.screens.wallet.collectibles.etheremon.views
            status-im.ui.screens.wallet.collectibles.cryptostrikers.views
            status-im.ui.screens.wallet.collectibles.cryptokitties.views
            status-im.ui.screens.wallet.collectibles.superrare.views
            status-im.ui.screens.wallet.collectibles.kudos.views
            [status-im.ui.components.status-bar.view :as status-bar.view]
            [status-im.ui.screens.wallet.transactions.views :as transactions.views]))

(defn toolbar-modal [modal-history?]
  [react/view
   [status-bar.view/status-bar {:type :modal-wallet}]
   [toolbar/toolbar {:style wallet.styles/toolbar :flat? true}
    [toolbar/nav-button (action/close-white action/default-handler)]
    [toolbar/content-wrapper]
    [toolbar/actions
     [{:icon      (if modal-history? :main-icons/wallet :main-icons/two-arrows)
       :icon-opts {:color               :white
                   :accessibility-label (if modal-history? :wallet-modal-button :transaction-history-button)}
       :handler #(re-frame/dispatch [:set-in [:wallet :modal-history?] (not modal-history?)])}]]]])

(defn- total-section [value currency]
  [react/view styles/section
   [react/view {:style styles/total-balance-container}
    [react/view {:style styles/total-balance}
     [react/text {:style               styles/total-balance-value
                  :accessibility-label :total-amount-value-text}
      (when (and
             (not= "0" value)
             (not= "..." value))
        [react/text {:style styles/total-balance-tilde}
         "~"])
      value]
     [react/text {:style               styles/total-balance-currency
                  :accessibility-label :total-amount-currency-text}
      (:code currency)]]]])

(defn- backup-seed-phrase []
  [react/view styles/section
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :backup-seed])}
    [react/view styles/backup-seed-phrase-container
     [react/view styles/backup-seed-phrase-text-container
      [react/i18n-text {:style styles/backup-seed-phrase-title
                        :key   :wallet-backup-recovery-title}]
      [react/i18n-text {:style styles/backup-seed-phrase-description
                        :key   :wallet-backup-recovery-description}]]
     [vector-icons/icon :main-icons/next {:color :white}]]]])

(def actions
  [{:label               (i18n/label :t/wallet-send)
    :accessibility-label :send-transaction-button
    :icon                :main-icons/send
    :action              #(re-frame/dispatch [:navigate-to :wallet-send-transaction])}
   {:label               (i18n/label :t/wallet-deposit)
    :accessibility-label :receive-transaction-button
    :icon                :main-icons/receive
    :action              #(re-frame/dispatch [:navigate-to :wallet-request-transaction])}
   {:label               (i18n/label :t/transaction-history)
    :accessibility-label :transaction-history-button
    :icon                :main-icons/history
    :action              #(re-frame/dispatch [:navigate-to :transactions-history])}])

(defn- render-asset [currency]
  (fn [{:keys [symbol symbol-display icon decimals amount] :as token}]
    (let [asset-value (re-frame/subscribe [:asset-value symbol decimals (-> currency :code keyword)])]
      [react/view {:style styles/asset-item-container}
       [list/item
        [list/item-image icon]
        [react/view {:style styles/asset-item-value-container}
         [react/text {:style               styles/asset-item-value
                      :number-of-lines     1
                      :ellipsize-mode      :tail
                      :accessibility-label (str (-> symbol name clojure.string/lower-case) "-asset-value-text")}
          (wallet.utils/format-amount amount decimals)]
         [react/text {:style           styles/asset-item-currency
                      :uppercase?      true
                      :number-of-lines 1}
          (wallet.utils/display-symbol token)]]
        [react/text {:style           styles/asset-item-price
                     :uppercase?      true
                     :number-of-lines 1}
         (or @asset-value "...")]]])))

(def item-icon-forward
  [list/item-icon {:icon      :main-icons/next
                   :style     {:width 12}
                   :icon-opts {:color :gray}}])

(defn- render-collectible [address-hex {:keys [symbol name icon amount] :as collectible} modal?]
  (let [items-number (money/to-fixed amount)
        details?     (and (pos? items-number) (not modal?))]
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

(defn- asset-section [assets currency address-hex modal?]
  (let [{:keys [tokens nfts]} (group-assets assets)]
    [react/view styles/asset-section
     [list/section-list
      {:scroll-enabled     false
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
                             :render-fn #(render-collectible address-hex % modal?)}]}]]))

(defn snackbar [error-message]
  [react/view styles/snackbar-container
   [react/text {:style styles/snackbar-text}
    (i18n/label error-message)]])

(views/defview wallet-root [modal?]
  (views/letsubs [assets          [:wallet/visible-assets-with-amount]
                  currency        [:wallet/currency]
                  portfolio-value [:portfolio-value]
                  {:keys [modal-history?]} [:get :wallet]
                  {:keys [seed-backed-up?]} [:account/account]
                  error-message   [:wallet/error-message]
                  address-hex     [:account/hex-address]]
    [react/view styles/main-section
     (if modal?
       [toolbar-modal modal-history?]
       [settings/toolbar-view])
     (if (and modal? modal-history?)
       [react/view styles/modal-history
        [transactions.views/history-list true]]
       [react/scroll-view {:refresh-control
                           (reagent/as-element
                            [react/refresh-control {:on-refresh #(re-frame/dispatch [:wallet.ui/pull-to-refresh])
                                                    :tint-color :white
                                                    :refreshing false}])}
        (if error-message
          [snackbar error-message]
          [total-section portfolio-value currency])
        (when (and (not modal?)
                   (not seed-backed-up?)
                   (some (fn [{:keys [amount]}]
                           (and amount (not (.isZero amount))))
                         assets))
          [backup-seed-phrase])
        (if modal?
          [react/view styles/address-section
           [react/text {:style               styles/wallet-address
                        :accessibility-label :address-text
                        :selectable          true}
            address-hex]]
          [react/view (merge {:background-color colors/blue} styles/action-section)
           [list/flat-list
            {:data      actions
             :key-fn    (fn [_ i] (str i))
             :render-fn #(list/render-action % {:action-label-style {:font-size 17}})}]])
        [asset-section assets currency address-hex modal?]
        ;; Hack to allow different colors for bottom scroll view (iOS only)
        [react/view {:style styles/scroll-bottom}]])]))

(views/defview wallet-modal []
  [wallet-root true])

(views/defview wallet []
  (views/letsubs [{:keys [wallet-set-up-passed?]} [:account/account]]
    (if (not wallet-set-up-passed?)
      [onboarding.views/onboarding]
      [wallet-root false])))
