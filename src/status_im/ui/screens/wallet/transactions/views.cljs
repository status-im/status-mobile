(ns status-im.ui.screens.wallet.transactions.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.screens.wallet.transactions.styles :as styles]
            [status-im.utils.money :as money]))

(defn on-delete-transaction
  [{:keys [id]}]
  (re-frame/dispatch [:wallet/discard-unsigned-transaction-with-confirmation id]))

(defn history-action [filter?]
  (cond->
   {:icon      :icons/filter
    :icon-opts {:accessibility-label :filters-button}
    :handler   #(re-frame/dispatch [:navigate-to-modal :wallet-transactions-filter])}
    filter? (assoc-in [:icon-opts :overlay-style] styles/corner-dot)))

(defn- all-checked? [filter-data]
  (and (every? :checked? (:type filter-data))
       (every? :checked? (:tokens filter-data))))

(defn- toolbar-view [current-tab filter-data]
  [toolbar/toolbar nil
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/transactions)]
   (case current-tab
     :transactions-history [toolbar/actions [(history-action (not (all-checked? filter-data)))]]
     :unsigned-transactions nil
     nil)])                                                 ;; TODO (andrey) implement [unsigned-action unsigned-transactions-count]


(defn action-buttons [{:keys [id] :as transaction}]
  [react/view {:style styles/action-buttons}
   [button/primary-button {:style               {:margin-right 12}
                           :on-press            #(re-frame/dispatch [:wallet/show-sign-transaction id])
                           :accessibility-label :sign-button}
    (i18n/label :t/transactions-sign)]
   [button/secondary-button {:on-press            #(on-delete-transaction transaction)
                             :accessibility-label :delete-button}
    (i18n/label :t/delete)]])

(defn- inbound? [type] (= :inbound type))
(defn- unsigned? [type] (= :unsigned type))

(defn- transaction-icon [k background-color color]
  {:icon      k
   :icon-opts {:color color}
   :style     (styles/transaction-icon-background background-color)})

(defn- transaction-type->icon [k]
  (case k
    :unsigned (transaction-icon :icons/dots-horizontal components.styles/color-gray4-transparent components.styles/color-gray7)
    :inbound (transaction-icon :icons/arrow-left components.styles/color-green-3-light components.styles/color-green-3)
    :outbound (transaction-icon :icons/arrow-right components.styles/color-blue4-transparent components.styles/color-blue4)
    (:postponed :pending) (transaction-icon :icons/arrow-right components.styles/color-gray4-transparent components.styles/color-gray7)
    (throw (str "Unknown transaction type: " k))))

(defn render-transaction [{:keys [hash from-contact to-contact to from type value time-formatted] :as transaction}]
  (let [[label contact address
         contact-accessibility-label
         address-accessibility-label] (if (inbound? type)
                                        [(i18n/label :t/from) from-contact from :sender-text :sender-address-text]
                                        [(i18n/label :t/to) to-contact to :recipient-name-text :recipient-address-text])
        unsigned? (unsigned? type)]
    [list/touchable-item #(re-frame/dispatch [:show-transaction-details hash])
     [react/view {:accessibility-label (if unsigned? :unsigned-transaction-item :transaction-item)}
      [list/item
       [list/item-icon (transaction-type->icon (keyword type))]
       [list/item-content
        [react/view {:style styles/amount-time}
         [react/text {:style           styles/tx-amount
                      :ellipsize-mode  "tail"
                      :number-of-lines 1}
          [react/text {:accessibility-label :amount-text}
           (->> value (money/wei-> :eth) money/to-fixed str)]
          " "
          [react/text {:accessibility-label :currency-text}
           (clojure.string/upper-case (name :eth))]]
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
          address]]
        (when unsigned?
          [action-buttons transaction])]
       [list/item-icon {:icon      :icons/forward
                        :style     {:margin-top 10}
                        :icon-opts (merge styles/forward
                                          {:accessibility-label :show-transaction-button})}]]]]))

(defn filtered-transaction? [transaction filter-data]
  (:checked? (some #(when (= (:type transaction) (:id %)) %) (:type filter-data))))

(defn update-transactions [m filter-data]
  (update m :data (fn [v] (filter #(filtered-transaction? % filter-data) v))))

(defview history-list []
  (letsubs [transactions-history-list [:wallet.transactions/transactions-history-list]
            filter-data               [:wallet.transactions/filters]]
           [react/view components.styles/flex
            [list/section-list {:sections        (map #(update-transactions % filter-data) transactions-history-list)
                                :key-fn          :hash
                                :render-fn       render-transaction
                                :empty-component [react/text {:style styles/empty-text}
                                                  (i18n/label :t/transactions-history-empty)]
                                :on-refresh      #(re-frame/dispatch [:update-transactions])
                                :refreshing      false}]]))

(defview unsigned-list []
  (letsubs [transactions [:wallet.transactions/unsigned-transactions-list]]
           [react/view {:style components.styles/flex}
            [list/flat-list {:data            transactions
                             :key-fn          (fn [_ i] (str i))
                             :render-fn       render-transaction
                             :empty-component [react/text {:style               styles/empty-text
                                                           :accessibility-label :no-unsigned-transactions-text}
                                               (i18n/label :t/transactions-unsigned-empty)]}]]))

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

(defn history-tab [active?]
  [react/text {:force-uppercase?    true
               :style               (styles/tab-title active?)
               :accessibility-label :history-button}
   (i18n/label :t/transactions-history)])

(defview unsigned-tab [active?]
  (letsubs [unsigned-transactions-count [:wallet.transactions/unsigned-transactions-count]]
           [react/view {:flex-direction :row}
            [react/text {:style               (styles/tab-title active?)
                         :force-uppercase?    true
                         :accessibility-label :unsigned-transactions-button}
             (i18n/label :t/transactions-unsigned)]
            (when (pos? unsigned-transactions-count)
              [react/text {:style               styles/tab-unsigned-transactions-count
                           :accessibility-label :unsigned-transactions-counter-text}
               (str " " unsigned-transactions-count)])]))

(def tabs-list
  [{:view-id :transactions-history
    :content history-tab}
   {:view-id :unsigned-transactions
    :content unsigned-tab}])

(defn tab [view-id content active?]
  [react/touchable-highlight {:style    components.styles/flex
                              :disabled active?
                              :on-press #(re-frame/dispatch [:navigation-replace view-id])}
   [react/view {:style (styles/tab active?)}
    [content active?]]])

(defn tabs [current-view-id]
  [react/view {:style styles/tabs-container}
   (for [{:keys [content view-id]} tabs-list]
     ^{:key view-id} [tab view-id content (= view-id current-view-id)])])

(defview transactions []
  (letsubs [current-tab [:get :view-id]
            filter-data [:wallet.transactions/filters]]
           [react/view styles/transactions-view
            [status-bar/status-bar]
            [toolbar-view current-tab filter-data]
            [tabs current-tab]
            [(case current-tab
               :transactions-history history-list
               :unsigned-transactions unsigned-list
               react/view)]]))

(defn- pretty-print-asset [symbol amount & [with-currency?]]
  (let [token (case symbol
                ;; TODO (jeluard) Format tokens amount once tokens history is supported
                :ETH :eth
                (throw (str "Unknown asset symbol: " symbol)))]
    (if amount
      (if with-currency?
        (money/wei->str token amount)
        (->> amount (money/wei-> token) money/to-fixed str))
      "...")))

(defn details-header [{:keys [value date type symbol]}]
  [react/view {:style styles/details-header}
   [react/view {:style styles/details-header-icon}
    [list/item-icon (transaction-type->icon type)]]
   [react/view {:style styles/details-header-infos}
    [react/text {:style styles/details-header-value}
     [react/text {:accessibility-label :amount-text}
      (pretty-print-asset symbol value)]
     " "
     [react/text {:accessibility-label :currency-text}
      (clojure.string/upper-case (name symbol))]]
    [react/text {:style styles/details-header-date} date]]])

(defn progress-bar [progress]
  [react/view {:style styles/progress-bar}
   [react/view {:style (styles/progress-bar-done progress)}]
   [react/view {:style (styles/progress-bar-todo (- 100 progress))}]])

(defn details-confirmations [confirmations confirmations-progress]
  [react/view {:style styles/details-block}
   [progress-bar confirmations-progress]
   [react/text {:style styles/details-confirmations-count}
    (str confirmations " " (i18n/label :t/confirmations))]
   [react/text {:style styles/details-confirmations-helper-text}
    (i18n/label :t/confirmations-helper-text)]])

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
      [react/text {:style styles/details-item-label} (i18n/label label)]
      [react/view {:style styles/details-item-value-wrapper}
       [react/text (merge {:style styles/details-item-value} props)
        (str value)]
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
            confirmations-progress [:wallet.transactions.details/confirmations-progress]]
           [react/view {:style components.styles/flex}
            [status-bar/status-bar]
            [toolbar/toolbar {}
             toolbar/default-nav-back
             [toolbar/content-title (i18n/label :t/transaction-details)]
             (when transaction [toolbar/actions (details-action hash url)])]
            (if transaction
              [react/scroll-view {:style components.styles/main-container}
               [details-header transaction]
               [details-confirmations confirmations confirmations-progress]
               [react/view {:style styles/details-separator}]
               [details-list transaction]]
              [react/text {:style styles/empty-text}
               (i18n/label :t/unsigned-transaction-expired)])]))
