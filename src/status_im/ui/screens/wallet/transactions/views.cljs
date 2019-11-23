(ns status-im.ui.screens.wallet.transactions.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.transactions.styles :as styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn history-action
  [all-filters?]
  (cond-> {:icon      :main-icons/filter
           :icon-opts {:accessibility-label :filters-button}
           :handler   #(re-frame/dispatch [:navigate-to :wallet-transactions-filter])}

    (not all-filters?)
    (assoc-in [:icon-opts :overlay-style] styles/corner-dot)))

(defn- toolbar-view
  [all-filters?]
  [toolbar/toolbar nil
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/transactions-history)]
   [toolbar/actions
    [(history-action all-filters?)]]])

(defn- transaction-icon
  [icon-key background-color color]
  {:icon      icon-key
   :icon-opts {:color color}
   :style     (styles/transaction-icon-background background-color)})

(defn- transaction-type->icon
  [k]
  (case k
    :inbound (transaction-icon :main-icons/arrow-left
                               colors/green-transparent-10
                               colors/green)
    :outbound (transaction-icon :main-icons/arrow-right
                                colors/blue-transparent-10
                                colors/blue)
    :failed (transaction-icon :main-icons/warning
                              colors/black-transparent
                              colors/red)
    :pending (transaction-icon :main-icons/arrow-right
                               colors/black-transparent colors/gray)
    (throw (str "Unknown transaction type: " k))))

(defn render-transaction
  [{:keys [label contact address contact-accessibility-label
           address-accessibility-label currency-text amount-text
           time-formatted on-touch-fn type]}]
  [list/touchable-item on-touch-fn
   [react/view {:accessibility-label :transaction-item}
    [list/item
     (when type
       [list/item-icon (transaction-type->icon (keyword type))])
     [list/item-content
      [react/view {:style styles/amount-time}
       [react/nested-text {:style           styles/tx-amount
                           :ellipsize-mode  "tail"
                           :number-of-lines 1}
        [{:accessibility-label :amount-text}
         amount-text]
        " "
        [{:accessibility-label :currency-text}
         currency-text]]
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
     [list/item-icon {:icon      :main-icons/next
                      :style     {:margin-top 10}
                      :icon-opts (merge styles/forward
                                        {:accessibility-label :show-transaction-button})}]]]])

(defn history-list
  [transactions-history-sections]
  [react/view components.styles/flex
   [list/section-list {:sections        transactions-history-sections
                       :key-fn          :hash
                       :render-fn       #(render-transaction %)
                       :empty-component
                       [react/i18n-text {:style styles/empty-text
                                         :key   :transactions-history-empty}]
                       :refreshing      false}]])

(defn- render-item-filter [{:keys [id label checked? on-touch]}]
  [react/view {:accessibility-label :filter-item}
   [list/list-item-with-checkbox
    {:checked?        checked?
     :on-value-change on-touch}
    [list/item
     [list/item-icon (transaction-type->icon id)]
     [list/item-content
      [list/item-primary-only {:accessibility-label :filter-name-text}
       label]]]]])

(defview filter-history []
  (letsubs [{:keys [filters all-filters? on-touch-select-all]}
            [:wallet.transactions.filters/screen]]
    [react/view styles/filter-container
     [toolbar/toolbar {}
      [toolbar/nav-clear-text
       {:accessibility-label :done-button}
       (i18n/label :t/done)]
      [toolbar/content-title
       (i18n/label :t/transactions-filter-title)]
      [toolbar/text-action
       {:handler             on-touch-select-all
        :disabled?           all-filters?
        :accessibility-label :select-all-button}
       (i18n/label :t/transactions-filter-select-all)]]
     [react/view
      {:style (merge {:background-color :white} components.styles/flex)}
      [list/section-list {:sections [{:title
                                      (i18n/label :t/type)
                                      :key       :type
                                      :render-fn render-item-filter
                                      :data filters}]
                          :key-fn   :id}]]]))

(defn details-header
  [date type amount-text currency-text]
  [react/view {:style styles/details-header}
   [react/view {:style styles/details-header-icon}
    (when type
      [list/item-icon (transaction-type->icon type)])]
   [react/view {:style styles/details-header-infos}
    [react/nested-text {:style styles/details-header-value}
     [{:accessibility-label :amount-text} amount-text]
     " "
     [{:accessibility-label :currency-text} currency-text]]
    [react/text {:style styles/details-header-date} date]]])

(defn progress-bar [progress failed?]
  [react/view {:style styles/progress-bar}
   [react/view {:style (styles/progress-bar-done progress failed?)}]
   [react/view {:style (styles/progress-bar-todo (- 100 progress) failed?)}]])

(defn details-confirmations
  [confirmations confirmations-progress failed?]
  [react/view {:style styles/details-block}
   [progress-bar confirmations-progress failed?]
   (if failed?
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

(defn details-list
  [{:keys [block hash
           from from-wallet from-contact
           to to-wallet to-contact
           gas-limit gas-price-gwei gas-price-eth gas-used
           cost nonce data]}]
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
  [(actions/opts [{:label (i18n/label :t/copy-transaction-hash)
                   :action #(react/copy-to-clipboard hash)}
                  {:label  (i18n/label :t/open-on-etherscan)
                   :action #(.openURL react/linking url)}])])

(defview transaction-details-view [hash address]
  (letsubs [{:keys [url type confirmations confirmations-progress
                    date amount-text currency-text]
             :as transaction}
            [:wallet.transactions.details/screen hash address]]
    [react/view {:style components.styles/flex}
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/transaction-details)]
      (when transaction [toolbar/actions (details-action hash url)])]
     [react/scroll-view {:style components.styles/main-container}
      [details-header date type amount-text currency-text]
      [details-confirmations confirmations confirmations-progress (= :failed type)]
      [react/view {:style styles/details-separator}]
      [details-list transaction]]]))

(defview transaction-details []
  (letsubs [{:keys [hash address]} [:get-screen-params]]
    (when (and hash address)
      [transaction-details-view hash address])))