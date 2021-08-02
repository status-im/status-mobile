(ns status-im.ui.screens.wallet.transactions.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.wallet.transactions.styles :as styles]
            [quo.core :as quo]
            [status-im.ui.components.toolbar :as toolbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

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
           time-formatted on-touch-fn type hash]}
   _ _ {:keys [keycard-account? transactions-management-enabled?]}]
  [react/view
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
        [quo/text {:style               styles/address-hash
                   :monospace           true
                   :color               :secondary
                   :ellipsize-mode      "middle"
                   :number-of-lines     1
                   :accessibility-label address-accessibility-label}
         address]]]
      [list/item-icon {:icon      :main-icons/next
                       :style     {:margin-top 10}
                       :icon-opts (merge styles/forward
                                         {:accessibility-label :show-transaction-button})}]]]]
   (when (and transactions-management-enabled?
              (not keycard-account?)
              (= type :pending))
     [react/view {:flex-direction :row :padding 16 :justify-content :space-between}
      [quo/button
       {:on-press #(re-frame/dispatch [:signing.ui/increase-gas-pressed hash])}
       (i18n/label :t/increase-gas)]
      [quo/button
       {:on-press #(re-frame/dispatch [:signing.ui/cancel-transaction-pressed hash])}
       (i18n/label :t/cancel)]])])

(defn etherscan-link [address]
  (let [link @(re-frame/subscribe [:wallet/etherscan-link address])]
    [react/touchable-highlight
     {:on-press #(when link
                   (.openURL ^js react/linking link))}
     [react/view
      {:style {:flex             1
               :padding-horizontal 14
               :flex-direction   :row
               :align-items :center
               :background-color colors/blue-light
               :height           52}}
      [icons/tiny-icon
       :tiny-icons/tiny-external
       {:color           colors/blue
        :container-style {:margin-right 5}}]
      [react/text
       {:style {:color colors/blue}}
       (i18n/label :t/check-on-etherscan)]]]))

(defn custom-node []
  [react/view
   {:style {:flex               1
            :padding-horizontal 14
            :flex-direction     :row
            :align-items        :center
            :background-color   (quo/get-color :warning-02)
            :height             52}}
   [react/text
    {:style {:color (quo/get-color :warning-01)}}
    (i18n/label :t/custom-node)]])

(defn non-archival-node []
  [react/view
   {:style {:flex             1
            :padding-horizontal 14
            :flex-direction   :row
            :align-items :center
            :background-color (quo/get-color :negative-02)
            :height           52}}
   [react/text
    {:style {:color (quo/get-color :negative-01)}}
    (i18n/label :t/non-archival-node)]])

(defn history-list
  [{:keys [transaction-history-sections total]} address]
  (let [fetching-recent-history?         @(re-frame/subscribe [:wallet/fetching-recent-tx-history? address])
        fetching-more-history?           @(re-frame/subscribe [:wallet/fetching-tx-history? address])
        keycard-account?                 @(re-frame/subscribe [:multiaccounts/keycard-account?])
        transactions-management-enabled? @(re-frame/subscribe [:wallet/transactions-management-enabled?])
        custom-rpc-node?                 @(re-frame/subscribe [:custom-rpc-node])
        non-archival-rpc-node?           @(re-frame/subscribe [:wallet/non-archival-node])
        all-fetched?                     @(re-frame/subscribe [:wallet/tx-history-fetched? address])
        syncing-allowed?                 @(re-frame/subscribe [:mobile-network/syncing-allowed?])]
    [react/view {:flex 1}
     [etherscan-link address]
     (cond non-archival-rpc-node?
           [non-archival-node]
           custom-rpc-node?
           [custom-node])
     (when fetching-recent-history?
       [react/view
        {:style {:flex            1
                 :height          40
                 :margin-vertical 16}}
        [react/activity-indicator {:size      :large
                                   :animating true}]])
     [list/section-list
      {:sections   transaction-history-sections
       :key-fn     :hash
       :render-data {:keycard-account? keycard-account?
                     :transactions-management-enabled? transactions-management-enabled?}
       :render-fn  render-transaction
       :empty-component
       [react/i18n-text {:style styles/empty-text
                         :key   (if (or fetching-recent-history? fetching-more-history?)
                                  :transactions-history-loading
                                  :transactions-history-empty)}]}]
     (when (and (not fetching-recent-history?)
                (not= all-fetched? :all))
       (if fetching-more-history?
         [react/view
          {:style {:flex            1
                   :height          40
                   :margin-vertical 8}}
          [react/activity-indicator {:size      :large
                                     :animating true}]]
         [toolbar/toolbar
          {:center
           [quo/button {:type     :secondary
                        :disabled (and (not syncing-allowed?)
                                       (or (= all-fetched? :all-preloaded)
                                           (zero? total)))
                        :on-press (when-not fetching-more-history?
                                    #(re-frame/dispatch
                                      [:transactions/fetch-more address]))}
            (i18n/label :t/transactions-load-more)]}]))]))

;; NOTE: Is this needed?
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
   (let [[props value]             (if (string? props-value)
                                     [nil props-value]
                                     props-value)
         [extra-props extra-value] (if (string? extra-props-value)
                                     [nil extra-props-value]
                                     extra-props-value)]
     [react/view {:style styles/details-row}
      [react/i18n-text {:style styles/details-item-label :key label}]
      [react/view {:style styles/details-item-value-wrapper}
       [quo/text (merge {:size      :small
                         :monospace true}
                        props)
        (str (or value "-"))]
       [quo/text (merge {:size      :small
                         :color     :secondary
                         :monospace true}
                        extra-props)
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
   [details-list-row :t/gas-limit [{:monospace true} gas-limit]]
   [details-list-row :t/gas-price gas-price-gwei [{:monospace false} gas-price-eth]]
   [details-list-row :t/gas-used gas-used]
   [details-list-row :t/cost-fee [{:monospace false} cost]]
   [details-list-row :t/nonce nonce]
   [details-list-row :t/data data]])

(defn details-action [hash url]
  [{:label  (i18n/label :t/copy-transaction-hash)
    :action #(react/copy-to-clipboard hash)}
   {:label  (i18n/label :t/open-on-etherscan)
    :action #(.openURL ^js react/linking url)}])

(defview transaction-details-view [hash address]
  (letsubs [{:keys [url type confirmations confirmations-progress
                    date amount-text currency-text]
             :as   transaction}
            [:wallet.transactions.details/screen hash address]]
    [react/view {:flex 1}
     ;;TODO options should be replaced by bottom sheet ,and topbar should be used here
     [topbar/topbar {:title             (i18n/label :t/transaction-details)
                     :right-accessories (when transaction
                                          [{:icon     :main-icons/more
                                            :on-press #(list-selection/show {:options
                                                                             (details-action hash url)})}])}]
     [react/scroll-view {:flex 1}
      [details-header date type amount-text currency-text]
      [details-confirmations confirmations confirmations-progress (= :failed type)]
      [react/view {:style styles/details-separator}]
      [details-list transaction]]]))

(defview transaction-details []
  (letsubs [{:keys [hash address]} [:get-screen-params]]
    (when (and hash address)
      [transaction-details-view hash address])))
