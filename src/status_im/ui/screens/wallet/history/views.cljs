(ns status-im.ui.screens.wallet.history.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.components.button.view :as button]
            [status-im.components.checkbox.view :as checkbox]
            [status-im.components.list.views :as list]
            [status-im.components.react :as react]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.styles :as styles]
            [status-im.components.tabs.views :as tabs]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.history.styles :as history.styles]
            [status-im.ui.screens.wallet.views :as wallet.views]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn on-sign-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Sign Transaction"))

(defn on-delete-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Delete Transaction"))

(defn unsigned-action [unsigned-transactions]
  [toolbar/text-action {:disabled? (zero? (count unsigned-transactions)) :handler #(re-frame/dispatch [:navigate-to-modal :wallet-transactions-sign-all])}
   (i18n/label :t/transactions-sign-all)])

(def history-action
  {:icon      :icons/filter
   :handler   #(utils/show-popup "TODO" "Not implemented") #_(re-frame/dispatch [:navigate-to-modal :wallet-transactions-sign-all])})

(defn toolbar-view [view-id unsigned-transactions]
  [toolbar/toolbar2 {:flat? true}
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/transactions)]
   (case @view-id
     :wallet-transactions-unsigned
     [unsigned-action unsigned-transactions]

     :wallet-transactions-history
     [toolbar/actions
      [history-action]])])

(defn action-buttons [m]
  [react/view {:style history.styles/action-buttons}
   [button/primary-button {:text (i18n/label :t/transactions-sign) :on-press #(on-sign-transaction m)}]
   [button/secondary-button {:text (i18n/label :t/delete) :on-press #(on-delete-transaction m)}]])

(defn- unsigned? [type] (= "unsigned" type))

(defn- transaction-icon [k background-color color] {:icon k :icon-opts {:color color} :style (history.styles/transaction-icon-background background-color)})

(defn- transaction-type->icon [s]
  (case s
    "unsigned" (transaction-icon :icons/dots-horizontal styles/color-gray4-transparent styles/color-gray7)
    "inbound" (transaction-icon :icons/arrow-left styles/color-green-3-light styles/color-green-3)
    "outbound" (transaction-icon :icons/arrow-right styles/color-blue4-transparent styles/color-blue4)
    ("postponed" "pending")  (transaction-icon :icons/arrow-right styles/color-gray4-transparent styles/color-gray7)
    (throw (str "Unknown transaction type: " s))))

(defn render-transaction [{:keys [to from type value symbol] :as m}]
  [list/item
   [list/item-icon (transaction-type->icon type)]
   [list/item-content
    (str value " " symbol)
    (if to
      (str (i18n/label :t/to) " " to)
      (str (i18n/label :t/from) " " from))
    (when (unsigned? type)
      [action-buttons m])]
   [list/item-icon {:icon :icons/forward :icon-opts history.styles/forward}]])

;; TODO(yenda) hook with re-frame
(defn- empty-text [s] [react/text {:style history.styles/empty-text} s])

(defview history-list []
  (letsubs [transactions-history-list [:wallet/transactions-history-list]
            transactions-loading?     [:wallet/transactions-loading?]
            error-message             [:wallet.transactions/error-message?]]
    [react/scroll-view {:style styles/flex}
     (when error-message [wallet.views/error-message-view history.styles/error-container history.styles/error-message])
     [list/section-list {:sections        transactions-history-list
                         :render-fn       render-transaction
                         :empty-component (empty-text (i18n/label :t/transactions-history-empty))
                         :on-refresh      #(re-frame/dispatch [:update-transactions])
                         :refreshing      (boolean transactions-loading?)}]]))

(defview unsigned-list [transactions]
  []
  [react/scroll-view {:style styles/flex}
   [list/flat-list {:data            transactions
                    :render-fn       render-transaction
                    :empty-component (empty-text (i18n/label :t/transactions-unsigned-empty))}]])

(defn- unsigned-transactions-title [transactions]
  (let [count (count transactions)]
    (str (i18n/label :t/transactions-unsigned)
         (if (pos? count) (str " " count)))))

(defn- tab-list [unsigned-transactions]
  [{:view-id :wallet-transactions-unsigned
    :title   (unsigned-transactions-title unsigned-transactions)
    :screen  [unsigned-list unsigned-transactions]}
   {:view-id :wallet-transactions-history
    :title   (i18n/label :t/transactions-history)
    :screen  [history-list]}])

;; Sign all

(defview sign-all []
  []
  [react/keyboard-avoiding-view {:style history.styles/sign-all-view}
   [react/view {:style history.styles/sign-all-done}
    [button/primary-button {:style    history.styles/sign-all-done-button
                            :text     (i18n/label :t/done)
                            :on-press #(re-frame/dispatch [:navigate-back])}]]
   [react/view {:style history.styles/sign-all-popup}
    [react/text {:style history.styles/sign-all-popup-sign-phrase} "one two three"] ;; TODO hook
    [react/text {:style history.styles/sign-all-popup-text} (i18n/label :t/transactions-sign-all-text)]
    [react/view {:style history.styles/sign-all-actions}
     [react/text-input {:style             history.styles/sign-all-input
                        :secure-text-entry true
                        :placeholder       (i18n/label :t/transactions-sign-input-placeholder)}]
     [button/primary-button {:text (i18n/label :t/transactions-sign-all) :on-press #(on-sign-transaction %)}]]]])

;; Filter history

(defn- item-tokens [{:keys [symbol label checked?]}]
  [list/item
   [list/item-icon (transaction-type->icon "pending")] ;; TODO(jeluard) add proper token data
   [list/item-content label symbol]
   [checkbox/checkbox  {:checked? true #_checked?}]])

(defn- item-type [{:keys [id label checked?]}]
  [list/item
   [list/item-icon (transaction-type->icon id)]
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
   [react/scroll-view
    [list/section-list {:sections filter-data}]]])

(defn- main-section [view-id tabs]
  (let [prev-view-id (reagent/atom @view-id)]
    [tabs/swipable-tabs {:style            history.styles/main-section
                         :style-tabs       history.styles/tabs
                         :style-tab-active history.styles/tab-active
                         :on-view-change   #(do (reset! prev-view-id @view-id)
                                                (reset! view-id %))}
      tabs prev-view-id view-id]))

;; TODO(yenda) must reflect selected wallet

(defview transactions []
  [unsigned-transactions [:wallet/unsigned-transactions]]
  (let [tabs         (tab-list unsigned-transactions)
        default-view (get-in tabs [0 :view-id])
        view-id      (reagent/atom default-view)]
    [react/view {:style styles/flex}
     [status-bar/status-bar {:type :modal}]
     [toolbar-view view-id unsigned-transactions]
     [main-section view-id tabs]]))
