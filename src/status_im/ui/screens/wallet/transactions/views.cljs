(ns status-im.ui.screens.wallet.transactions.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.screens.wallet.transactions.styles :as styles]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.ui.screens.wallet.utils :as wallet.utils]))

(defn history-action [filter?]
  (cond->
   {:icon      :main-icons/filter
    :icon-opts {:accessibility-label :filters-button}
    :handler   #(re-frame/dispatch [:navigate-to :wallet-transactions-filter])}
    filter? (assoc-in [:icon-opts :overlay-style] styles/corner-dot)))

(defn- all-checked? [filter-data]
  (and (every? :checked? (:type filter-data))
       (every? :checked? (:tokens filter-data))))

(defn- toolbar-view [filter-data]
  [toolbar/toolbar nil
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/transactions-history)]
   [toolbar/actions
    [(history-action (not (all-checked? filter-data)))]]])

(defn- inbound? [type] (= :inbound type))
(defn- failed? [type] (= :failed type))

(defn- transaction-icon [k background-color color]
  {:icon      k
   :icon-opts {:color color}
   :style     (styles/transaction-icon-background background-color)})

(defn- transaction-type->icon [k]
  (case k
    :inbound (transaction-icon :main-icons/arrow-left (colors/alpha colors/green 0.2) colors/green)
    :outbound (transaction-icon :main-icons/arrow-right (colors/alpha colors/blue 0.1) colors/blue)
    :failed (transaction-icon :main-icons/warning colors/gray-light colors/red)
    (:postponed :pending) (transaction-icon :main-icons/arrow-right colors/gray-light colors/gray)
    (throw (str "Unknown transaction type: " k))))

(defn render-transaction [{:keys [hash from-contact to-contact to from type value time-formatted symbol]}
                          network all-tokens hide-details?]
  (let [[label contact address
         contact-accessibility-label
         address-accessibility-label] (if (inbound? type)
                                        [(i18n/label :t/from) from-contact from :sender-text :sender-address-text]
                                        [(i18n/label :t/to) to-contact to :recipient-name-text :recipient-address-text])
        {:keys [decimals] :as token}   (tokens/asset-for all-tokens (ethereum/network->chain-keyword network) symbol)]
    [list/touchable-item #(when-not hide-details? (re-frame/dispatch [:show-transaction-details hash]))
     [react/view {:accessibility-label :transaction-item}
      [list/item
       [list/item-icon (transaction-type->icon (keyword type))]
       [list/item-content
        [react/view {:style styles/amount-time}
         [react/text {:style           styles/tx-amount
                      :ellipsize-mode  "tail"
                      :number-of-lines 1}
          [react/text {:accessibility-label :amount-text
                       :style               styles/amount-text}
           (-> value  (money/internal->formatted symbol decimals) money/to-fixed str)]
          " "
          [react/text {:accessibility-label :currency-text}
           (wallet.utils/display-symbol token)]]
         [react/text {:style styles/tx-time}
          time-formatted]]
        [react/view {:style styles/address-row}
         [react/text {:style styles/address-label}
          label]
         (when contact
           [react/text {:style               styles/address-contact
                        :accessibility-label contact-accessibility-label}
            contact])
         [react/text {:style               styles/address-hash
                      :ellipsize-mode      "middle"
                      :number-of-lines     1
                      :accessibility-label address-accessibility-label}
          address]]]
       (when-not hide-details?
         [list/item-icon {:icon      :main-icons/next
                          :style     {:margin-top 10}
                          :icon-opts (merge styles/forward
                                            {:accessibility-label :show-transaction-button})}])]]]))

(defn filtered-transaction? [transaction filter-data]
  (:checked? (some #(when (= (:type transaction) (:id %)) %) (:type filter-data))))

(defn update-transactions [m filter-data]
  (update m :data (fn [v] (filter #(filtered-transaction? % filter-data) v))))

(defview history-list [& [hide-details?]]
  (letsubs [transactions-history-list [:wallet.transactions/transactions-history-list]
            filter-data               [:wallet.transactions/filters]
            network                   [:account/network]
            all-tokens                [:wallet/all-tokens]]
    [react/view components.styles/flex
     [list/section-list {:sections        (map #(update-transactions % filter-data) transactions-history-list)
                         :key-fn          :hash
                         :render-fn       #(render-transaction % network all-tokens hide-details?)
                         :empty-component [react/i18n-text {:style styles/empty-text
                                                            :key   :transactions-history-empty}]
                         :on-refresh      #(re-frame/dispatch [:update-transactions])
                         :refreshing      false}]]))

;; Filter history

(defn- item-filter [{:keys [icon checked? path]} content]
  [react/view {:accessibility-label :filter-item}
   [list/list-item-with-checkbox
    {:checked?        checked?
     :on-value-change #(re-frame/dispatch [:wallet.transactions/filter path %])}
    [list/item
     [list/item-icon icon]
     content]]])

(defn- render-item-filter [{:keys [id label checked?]}]
  [item-filter {:icon (transaction-type->icon id) :checked? checked? :path {:type id}}
   [list/item-content
    [list/item-primary-only {:accessibility-label :filter-name-text}
     label]]])

(defn- wrap-filter-data [m]
  [{:title     (i18n/label :t/transactions-filter-type)
    :key       :type
    :render-fn render-item-filter
    :data      (:type m)}])

(defview filter-history []
  (letsubs [filter-data [:wallet.transactions/filters]]
    [react/view styles/filter-container
     [status-bar/status-bar {:type :modal-white}]
     [toolbar/toolbar {}
      [toolbar/nav-clear-text {:accessibility-label :done-button} (i18n/label :t/done)]
      [toolbar/content-title (i18n/label :t/transactions-filter-title)]
      [toolbar/text-action {:handler             #(re-frame/dispatch [:wallet.transactions/filter-all])
                            :disabled?           (all-checked? filter-data)
                            :accessibility-label :select-all-button}
       (i18n/label :t/transactions-filter-select-all)]]
     [react/view {:style (merge {:background-color :white} components.styles/flex)}
      [list/section-list {:sections (wrap-filter-data filter-data)
                          :key-fn   (comp str :id)}]]]))

(defview transactions []
  (letsubs [filter-data [:wallet.transactions/filters]]
    [react/view styles/transactions-view
     [status-bar/status-bar]
     [toolbar-view filter-data]
     [history-list]]))

(defn- pretty-print-asset [symbol amount token]
  (if amount
    (if (= :ETH symbol)
      (->> amount (money/wei-> :eth) money/to-fixed str)
      (-> amount (money/token->unit (:decimals token)) money/to-fixed str))
    "..."))

(defn details-header [network all-tokens {:keys [value date type symbol token]}]
  (let [asset (tokens/asset-for all-tokens (ethereum/network->chain-keyword network) symbol)]
    [react/view {:style styles/details-header}
     [react/view {:style styles/details-header-icon}
      [list/item-icon (transaction-type->icon type)]]
     [react/view {:style styles/details-header-infos}
      [react/text {:style styles/details-header-value}
       [react/text {:accessibility-label :amount-text}
        (pretty-print-asset symbol value token)]
       " "
       [react/text {:accessibility-label :currency-text}
        (wallet.utils/display-symbol asset)]]
      [react/text {:style styles/details-header-date} date]]]))

(defn progress-bar [progress failed?]
  [react/view {:style styles/progress-bar}
   [react/view {:style (styles/progress-bar-done progress failed?)}]
   [react/view {:style (styles/progress-bar-todo (- 100 progress) failed?)}]])

(defn details-confirmations [confirmations confirmations-progress type]
  [react/view {:style styles/details-block}
   [progress-bar confirmations-progress (failed? type)]
   (if (failed? type)
     [react/i18n-text {:style styles/details-failed
                       :key   :failed}]
     [react/text {:style styles/details-confirmations-count}
      (str confirmations " " (i18n/label :t/confirmations))])
   [react/i18n-text {:style styles/details-confirmations-helper-text
                     :key   :confirmations-helper-text}]])

(defn details-list-row
  ([label props-value]
   (details-list-row label props-value nil))
  ([label props-value extra-props-value]
   (let [[props value] (if (string? props-value)
                         [nil props-value]
                         props-value)
         [extra-props extra-value] (if (string? extra-props-value)
                                     [nil extra-props-value]
                                     extra-props-value)]
     [react/view {:style styles/details-row}
      [react/i18n-text {:style styles/details-item-label :key label}]
      [react/view {:style styles/details-item-value-wrapper}
       [react/text (merge {:style styles/details-item-value} props)
        (str (or value "-"))]
       [react/text (merge {:style styles/details-item-extra-value} extra-props)
        (str extra-value)]]])))

(defn details-list [{:keys [block hash
                            from from-wallet from-contact
                            to to-wallet to-contact
                            gas-limit gas-price-gwei gas-price-eth gas-used cost nonce data]}]
  [react/view {:style styles/details-block}
   [details-list-row :t/block block]
   [details-list-row :t/hash hash]
   [details-list-row :t/from
    [{:accessibility-label (if from-wallet :sender-name-text :sender-address-text)}
     (or from-wallet from-contact from)]
    (when (or from-wallet from-contact)
      [{:accessibility-label :sender-address-text}
       from])]
   [details-list-row :t/to
    [{:accessibility-label (if to-wallet :recipient-name-text :recipient-address-text)}
     (or to-wallet to-contact to)]
    (when (or to-wallet to-contact)
      [{:accessibility-label :recipient-address-text}
       to])]
   [details-list-row :t/gas-limit gas-limit]
   [details-list-row :t/gas-price gas-price-gwei gas-price-eth]
   [details-list-row :t/gas-used gas-used]
   [details-list-row :t/cost-fee cost]
   [details-list-row :t/nonce nonce]
   [details-list-row :t/data data]])

(defn details-action [hash url]
  [(actions/opts [{:label (i18n/label :t/copy-transaction-hash) :action #(react/copy-to-clipboard hash)}
                  {:label (i18n/label :t/open-on-etherscan) :action #(.openURL react/linking url)}])])

(defview transaction-details []
  (letsubs [{:keys [hash url type] :as transaction} [:wallet.transactions/transaction-details]
            confirmations          [:wallet.transactions.details/confirmations]
            confirmations-progress [:wallet.transactions.details/confirmations-progress]
            network                [:account/network]
            all-tokens             [:wallet/all-tokens]]
    [react/view {:style components.styles/flex}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/transaction-details)]
      (when transaction [toolbar/actions (details-action hash url)])]
     [react/scroll-view {:style components.styles/main-container}
      [details-header network all-tokens transaction]
      [details-confirmations confirmations confirmations-progress type]
      [react/view {:style styles/details-separator}]
      [details-list transaction]]]))
