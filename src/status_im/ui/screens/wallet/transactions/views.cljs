(ns status-im.ui.screens.wallet.transactions.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.components.button.view :as button]
            [status-im.components.checkbox.view :as checkbox]
            [status-im.components.list.views :as list]
            [status-im.components.react :as react]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.styles :as styles]
            [status-im.components.tabs.views :as tabs]
            [status-im.components.toolbar-new.actions :as actions]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.transactions.styles :as transactions.styles]
            [status-im.ui.screens.wallet.views :as wallet.views]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn on-sign-transaction
  [password]
  ;; TODO(yenda) implement
  (re-frame/dispatch [:accept-transactions password]))

(defn on-delete-transaction
  [{:keys [id]}]
  ;; TODO(andrey) implement alert
  (re-frame/dispatch [:wallet/discard-unsigned-transaction id]))

;; TODO (andrey) implement
(defn unsigned-action [unsigned-transactions-count]
  [toolbar/text-action {:disabled? (zero? unsigned-transactions-count)
                        :handler #(re-frame/dispatch [:navigate-to-modal :wallet-transactions-sign-all])}
   (i18n/label :t/transactions-sign-all)])

(def history-action
  {:icon      :icons/filter
   :handler   #(utils/show-popup "TODO" "Not implemented") #_(re-frame/dispatch [:navigate-to-modal :wallet-transactions-sign-all])})

(defn toolbar-view [view-id unsigned-transactions-count]
  [toolbar/toolbar2 {:flat? true}
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/transactions)]
   (case @view-id
     :wallet-transactions-unsigned
     nil ;; TODO (andrey) implement [unsigned-action unsigned-transactions-count]

     :wallet-transactions-history
     [toolbar/actions
      [history-action]])])

(defn action-buttons [{:keys [id to value] :as transaction}]
  [react/view {:style transactions.styles/action-buttons}
   [button/primary-button {:style    {:margin-right 12}
                           :on-press #(re-frame/dispatch [:navigate-to-modal
                                                          :wallet-send-transaction-modal
                                                          {:amount         (str (money/wei->ether value))
                                                           :transaction-id id
                                                           :to-address     to
                                                           :to-name        to}])}
                          (i18n/label :t/transactions-sign)]
   [button/secondary-button {:on-press #(on-delete-transaction transaction)}
                            (i18n/label :t/delete)]])

(defn- inbound? [type] (= "inbound" type))
(defn- unsigned? [type] (= "unsigned" type))

(defn- transaction-icon [k background-color color]
  {:icon      k
   :icon-opts {:color color}
   :style     (transactions.styles/transaction-icon-background background-color)})

(defn- transaction-type->icon [k]
  (case k
    :unsigned               (transaction-icon :icons/dots-horizontal styles/color-gray4-transparent styles/color-gray7)
    :inbound                (transaction-icon :icons/arrow-left styles/color-green-3-light styles/color-green-3)
    :outbound               (transaction-icon :icons/arrow-right styles/color-blue4-transparent styles/color-blue4)
    (:postponed :pending)   (transaction-icon :icons/arrow-right styles/color-gray4-transparent styles/color-gray7)
    (throw (str "Unknown transaction type: " k))))

(defn render-transaction [{:keys [hash to from type value symbol] :as transaction}]
  [list/touchable-item #(re-frame/dispatch [:show-transaction-details hash])
   [react/view
    [list/item
     [list/item-icon (transaction-type->icon (keyword type))]
     [list/item-content
      (money/wei->str :eth value)
      (if (inbound? type)
        (str (i18n/label :t/from) " " from)
        (str (i18n/label :t/to) " " to))
      (when (unsigned? type)
        [action-buttons transaction])]
     [list/item-icon {:icon :icons/forward
                      :style {:margin-top 10}
                      :icon-opts transactions.styles/forward}]]]])

;; TODO(yenda) hook with re-frame
(defn- empty-text [s] [react/text {:style transactions.styles/empty-text} s])

(defview history-list []
  (letsubs [transactions-history-list [:wallet.transactions/transactions-history-list]
            transactions-loading?     [:wallet.transactions/transactions-loading?]
            error-message             [:wallet.transactions/error-message?]]
    [react/view {:style styles/flex}
     (when error-message
       [wallet.views/error-message-view transactions.styles/error-container transactions.styles/error-message])
     [list/section-list {:sections        transactions-history-list
                         :render-fn       render-transaction
                         :empty-component (empty-text (i18n/label :t/transactions-history-empty))
                         :on-refresh      #(re-frame/dispatch [:update-transactions])
                         :refreshing      (boolean transactions-loading?)}]]))

(defview unsigned-list []
  (letsubs [transactions [:wallet.transactions/unsigned-transactions-list]]
    [react/view {:style styles/flex}
     [list/flat-list {:data            transactions
                      :render-fn       render-transaction
                      :empty-component (empty-text (i18n/label :t/transactions-unsigned-empty))}]]))

(defn- unsigned-transactions-title [unsigned-transactions-count]
  (str (i18n/label :t/transactions-unsigned)
       (if (pos? unsigned-transactions-count) (str " " unsigned-transactions-count))))

(defn- tab-list [unsigned-transactions-count]
  [{:view-id :wallet-transactions-history
    :title   (i18n/label :t/transactions-history)
    :screen  [history-list]}
   {:view-id :wallet-transactions-unsigned
    :title   (unsigned-transactions-title unsigned-transactions-count)
    :screen  [unsigned-list]}])

;; Filter history

(defn- item-tokens [{:keys [symbol label checked?]}]
  [list/item
   [list/item-icon (transaction-type->icon :pending)] ;; TODO(jeluard) add proper token data
   [list/item-content label symbol]
   [checkbox/checkbox {:checked? true #_checked?}]])

(defn- item-type [{:keys [id label checked?]}]
  [list/item
   [list/item-icon (transaction-type->icon (keyword id))]
   [list/item-content label]
   [checkbox/checkbox checked?]])

(def filter-data
  [{:title (i18n/label :t/transactions-filter-tokens)
    :key :tokens
    :renderItem (list/wrap-render-fn item-tokens)
    :data [{:symbol "GNO" :label "Gnosis"}
           {:symbol "SNT" :label "Status Network Token"}
           {:symbol "SGT" :label "Status Genesis Token"}
           {:symbol "GOL" :label "Golem"}]}
   {:title (i18n/label :t/transactions-filter-type)
    :key :type
    :renderItem (list/wrap-render-fn item-type)
    :data [{:id :incoming  :label "Incoming"}
           {:id :outgoing  :label "Outgoing"}
           {:id :pending   :label "Pending"}
           {:id :postponed :label "Postponed"}]}])

(defview filter-history []
  []
  [react/view
   [toolbar/toolbar2 {}
    [toolbar/nav-clear-text (i18n/label :t/done)]
    [toolbar/content-title (i18n/label :t/transactions-filter-title)]
    [toolbar/text-action {:handler #(utils/show-popup "TODO" "Select All")}
     (i18n/label :t/transactions-filter-select-all)]]
   [react/view {:style styles/flex}
    [list/section-list {:sections filter-data}]]])

(defn- main-section [view-id tabs]
  (let [prev-view-id (reagent/atom @view-id)]
    [tabs/swipable-tabs {:style            transactions.styles/main-section
                         :style-tabs       transactions.styles/tabs
                         :style-tab-active transactions.styles/tab-active
                         :on-view-change   #(do (reset! prev-view-id @view-id)
                                                (reset! view-id %))}
     tabs prev-view-id view-id]))

;; TODO(yenda) must reflect selected wallet

(defview transactions []
  (letsubs [unsigned-transactions-count [:wallet.transactions/unsigned-transactions-count]]
    (let [tabs         (tab-list unsigned-transactions-count)
          default-view (get-in tabs [0 :view-id])
          view-id      (reagent/atom default-view)]
      [react/view {:style styles/flex}
       [status-bar/status-bar]
       [toolbar-view view-id unsigned-transactions-count]
       [main-section view-id tabs]])))

(defn- pretty-print-asset [symbol amount]
  (case symbol
    "ETH" (if amount (money/wei->str :eth amount) "...")))

(defn details-header [{:keys [value date type symbol]}]
  [react/view {:style transactions.styles/details-header}
   [react/view {:style transactions.styles/details-header-icon}
    [list/item-icon (transaction-type->icon type)]]
   [react/view {:style transactions.styles/details-header-infos}
    [react/text {:style transactions.styles/details-header-value} (pretty-print-asset symbol value)]
    [react/text {:style transactions.styles/details-header-date} date]]])

(defn progress-bar [progress]
  [react/view {:style transactions.styles/progress-bar}
   [react/view {:style (transactions.styles/progress-bar-done progress)}]
   [react/view {:style (transactions.styles/progress-bar-todo (- 100 progress))}]])

(defn details-confirmations [confirmations confirmations-progress]
  [react/view {:style transactions.styles/details-block}
   [progress-bar confirmations-progress]
   [react/text {:style transactions.styles/details-confirmations-count}
    (str confirmations " " (i18n/label :t/confirmations))]
   [react/text {:style transactions.styles/details-confirmations-helper-text}
    (i18n/label :t/confirmations-helper-text)]])

(defn details-list-row
  ([label value]
   (details-list-row label value nil))
  ([label value extra-value]
   [react/view {:style transactions.styles/details-row}
    [react/text {:style transactions.styles/details-item-label} (i18n/label label)]
    [react/view {:style transactions.styles/details-item-value-wrapper}
     [react/text {:style transactions.styles/details-item-value} (str value)]
     [react/text {:style transactions.styles/details-item-extra-value} (str extra-value)]]]))

(defn details-list [{:keys [block hash from from-wallet to to-wallet gas-limit gas-price-gwei gas-price-eth gas-used cost nonce data]}]
  [react/view {:style transactions.styles/details-block}
   [details-list-row :t/block block]
   [details-list-row :t/hash hash]
   [details-list-row :t/from (or from-wallet from) (when from-wallet from)]
   [details-list-row :t/to (or to-wallet to) (when to-wallet to)]
   [details-list-row :t/gas-limit gas-limit]
   [details-list-row :t/gas-price gas-price-gwei gas-price-eth]
   [details-list-row :t/gas-used gas-used]
   [details-list-row :t/cost-fee cost]
   [details-list-row :t/nonce nonce]
   [details-list-row :t/data data]])

(defn details-action [hash url]
  [(actions/opts [{:text (i18n/label :t/copy-transaction-hash) :value #(react/copy-to-clipboard hash)}
                  {:text (i18n/label :t/open-on-etherscan) :value #(.openURL react/linking url)}])])

(defview transaction-details []
  (letsubs [{:keys [hash url type] :as transactions} [:wallet.transactions/transaction-details]
            confirmations                            [:wallet.transactions.details/confirmations]
            confirmations-progress                   [:wallet.transactions.details/confirmations-progress]]
    [react/view {:style styles/flex}
     [status-bar/status-bar]
     [toolbar/toolbar2 {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/transaction-details)]
      [toolbar/actions (details-action hash url)]]
     [react/scroll-view {:style transactions.styles/main-section}
      [details-header transactions]
      [details-confirmations confirmations confirmations-progress]
      [react/view {:style transactions.styles/details-separator}]
      [details-list transactions]]]))
