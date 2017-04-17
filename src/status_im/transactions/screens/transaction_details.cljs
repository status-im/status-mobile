(ns status-im.transactions.screens.transaction-details
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.react :as rn]
            [status-im.components.common.common :as common]
            [status-im.components.sticky-button :as sticky-button]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.transactions.styles.screens :as st]
            [status-im.transactions.views.list-item :as transactions-list-item]
            [status-im.transactions.views.password-form :as password-form]
            [status-im.utils.platform :as platform]))

(defn toolbar-view []
  [toolbar/toolbar
   {:background-color st/transactions-toolbar-background
    :nav-action       (act/back-white #(rf/dispatch [:navigate-to-modal :unsigned-transactions]))
    :border-style     st/toolbar-border
    :custom-content   [rn/view {:style st/toolbar-title-container}
                       [rn/text {:style st/toolbar-title-text
                                 :font  :toolbar-title}
                        (i18n/label :t/transaction)]]}])

(defn detail-item [title content name?]
  [rn/view {:style st/details-item}
   [rn/text {:style st/details-item-title} title]
   [rn/text {:style           (st/details-item-content name?)
             :number-of-lines 1}
    content]])

(defn detail-data [content]
  [rn/view {:style st/details-data}
   [rn/text {:style st/details-data-title} (i18n/label :t/data)]
   [rn/text {:style st/details-data-content} content]])

(defview details [{:keys [to data gas gas-price] :as transaction}]
  [current-account [:get-current-account]
   recipient       [:contact-by-address to]]
  (let [recipient-name (or (:name recipient) to)
        gas-price      (.fromWei js/Web3.prototype gas-price "ether")
        fee-value      (* gas gas-price)
        estimated-fee  (str fee-value " ETH")]
    [rn/view st/details-container
     [detail-item (i18n/label :t/to) recipient-name true]
     [detail-item (i18n/label :t/from) (:name current-account) true]
     [detail-item (i18n/label :t/estimated-fee) estimated-fee]
     [detail-data data]]))

(defview transaction-details []
  [{:keys [id] :as transaction} [:get :selected-transaction]
   {:keys [password]}           [:get :confirm-transactions]
   confirmed?                   [:get-in [:transaction-details-ui-props :confirmed?]]]
  {:component-did-update #(when-not transaction (rf/dispatch [:navigate-to-modal :unsigned-transactions]))
   :component-will-unmount #(rf/dispatch [:set-in [:transaction-details-ui-props :confirmed?] false])}
  [rn/keyboard-avoiding-view {:style st/transactions-screen}
   [status-bar/status-bar {:type (if platform/ios? :transparent :main)}]
   [toolbar-view]
   [rn/scroll-view st/details-screen-content-container
    [transactions-list-item/view transaction #(rf/dispatch [:navigate-to-modal :unsigned-transactions])]
    [common/separator st/details-separator st/details-separator-wrapper]
    [details transaction]]
   (when confirmed? [password-form/view 1])
   (let [confirm-text (if confirmed?
                        (i18n/label :t/confirm)
                        (i18n/label-pluralize 1 :t/confirm-transactions))
         confirm-fn   (if confirmed?
                        #(do (rf/dispatch [:accept-transaction password id])
                             (rf/dispatch [:set :confirmed-transactions-count 1]))
                        #(rf/dispatch [:set-in [:transaction-details-ui-props :confirmed?] true]))]
     [sticky-button/sticky-button confirm-text confirm-fn])])
