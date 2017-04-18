(ns status-im.transactions.screens.unsigned-transactions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.react :as rn]
            [status-im.components.sticky-button :as sticky-button]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.transactions.views.list-item :as transactions-list-item]
            [status-im.transactions.views.password-form :as password-form]
            [status-im.transactions.styles.screens :as st]
            [status-im.utils.listview :as lw]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]))

(defn toolbar-view [transactions]
  [toolbar/toolbar
   {:background-color st/transactions-toolbar-background
    :nav-action       (act/close-white #(rf/dispatch [:navigate-back]))
    :border-style     st/toolbar-border
    :custom-content   [rn/view {:style st/toolbar-title-container}
                       [rn/text {:style st/toolbar-title-text
                                 :font :toolbar-title}
                        (i18n/label :t/unsigned-transactions)]
                       [rn/text {:style st/toolbar-title-count
                                 :font :toolbar-title}
                        (count transactions)]]}])

(defn render-separator-fn [transactions-count]
  (fn [_ row-id _]
    (when (< row-id (dec transactions-count))
      (rn/list-item
       ^{:key row-id}
       [common/separator {} st/transactions-list-separator]))))

(defn render-row-fn [row _ _]
  (rn/list-item
   [rn/touchable-highlight {:on-press #(rf/dispatch [:navigate-to-modal :transaction-details row])}
    [rn/view
     [transactions-list-item/view row]]]))

(defview unsigned-transactions []
  [transactions [:transactions]
   {:keys [password]} [:get :confirm-transactions]
   confirmed?        [:get-in [:transactions-list-ui-props :confirmed?]]]
  {:component-did-update #(when-not (seq transactions) (rf/dispatch [:navigate-back]))
   :component-will-unmount #(rf/dispatch [:set-in [:transactions-list-ui-props :confirmed?] false])}
  [(if platform/ios? rn/keyboard-avoiding-view rn/view) (merge {:behavior :padding} st/transactions-screen)
   [status-bar/status-bar {:type (if platform/ios? :transparent :main)}]
   [toolbar-view transactions]
   [rn/view {:style st/transactions-screen-content-container}
    [rn/list-view {:style           st/transactions-list
                   :dataSource      (lw/to-datasource transactions)
                   :renderSeparator (render-separator-fn (count transactions))
                   :renderRow       render-row-fn}]
    (when confirmed?
      [password-form/view (count transactions)])]
   (let [confirm-text (if confirmed?
                        (i18n/label :t/confirm)
                        (i18n/label-pluralize (count transactions) :t/confirm-transactions))
         confirm-fn   (if confirmed?
                        #(do (rf/dispatch [:accept-transactions password])
                             (rf/dispatch [:set :confirmed-transactions-count (count transactions)]))
                        #(rf/dispatch [:set-in [:transactions-list-ui-props :confirmed?] true]))]
     [sticky-button/sticky-button confirm-text confirm-fn])])
