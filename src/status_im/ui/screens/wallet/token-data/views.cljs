(ns status-im.ui.screens.wallet.token-data.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.react :as rn]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.utils.listview :as lw]
            [status-im.ui.screens.wallet.token-data.styles :as st]))

(defn toolbar-view [transactions]
  [toolbar/toolbar {:nav-action act/default-back}])

(defn token-balance []
  [rn/view {:style st/total-balance-container}
   [rn/view {:style st/total-balance}
    [rn/text {:style st/total-balance-value} "123.4"]
    [rn/text {:style st/total-balance-currency} "GNO"]]
   [rn/view {:style st/value-variation}
    [rn/text {:style st/fiat-value} "2562.56 USD"]
    [rn/view {:style st/today-variation-container}
     [rn/text {:style st/today-variation} "+5.433%"]]]
   [rn/view {:style st/action-buttons-container}
    [rn/view {:style st/action-button-left}
     [rn/text {:style st/action-button-text} "Send GNO"]]
    [rn/view {:style st/action-button}
     [rn/text {:style st/action-button-text} "Exchange GNO"]]]])

(defn transaction-list-item [[id {:keys [to currency amount] :as row}]]
  (let [amount-string (str amount " " (string/upper-case (name currency)))]
    [rn/view {:style st/transaction-item-container}
     [rn/view {:style st/transaction-item-icon-container}
      [rn/image {:style st/transaction-item-icon
                :source {:uri :icon_arrow_right_gray}}]]
     [rn/view {:style st/transaction-item-info-container}
      [rn/text {:style st/transaction-item-value} amount-string]
      [rn/view {:style st/transaction-item-recipient-container}
       [rn/text {:style st/transaction-item-recipient-label} "To"]
       [rn/text {:style st/transaction-item-recipient} to]]]
     [rn/image {:source {:uri :icon_back_gray}
                :style st/transaction-item-details-icon}]]))

(defn render-separator-fn [transactions-count]
  (fn [_ row-id _]
    (rn/list-item
     ^{:key row-id}
     [common/separator {} st/transaction-list-separator])))

(defn render-row-fn [row _ _]
  (rn/list-item
   [rn/touchable-highlight {:on-press #()}
    [rn/view
     [transaction-list-item row]]]))

(defn transactions-section []
  (let [transactions {"1" {:currency :gno :amount 1.3 :to "0xea78969082nfaslkj81hblasf9jh"}
                      "2" {:currency :gno :amount 0.03 :to "0xbi998asjh00987fsnjksuyfa9087"}}]
    [rn/view {:style st/transaction-section}
     [rn/text {:style st/transaction-section-title} "Transactions"]
     [rn/list-view {:style           st/transaction-list
                    :dataSource      (lw/to-datasource transactions)
                    :renderSeparator (render-separator-fn (count transactions))
                    :renderRow       render-row-fn}]]))

(defn market-data-item [title content & large? custom-content?]
  [rn/view {:style st/market-data-item-container}
   [rn/text {:style st/market-data-item-title} title]
   (if custom-content?
     [content]
     [rn/text {:style (st/market-data-item-text large?)} content])])

(defn market-data-link [title]
  [rn/view {:style st/market-data-link}
   [rn/text {:style st/market-data-link-title} title]
   [rn/image {:style st/market-data-link-icon
              :source {:uri :icon_arrow_right_gray}}]])

(defn market-data-links []
  [rn/view {:style st/market-data-links-container}
   [market-data-link "Website"]
   [market-data-link "Facebook"]
   [market-data-link "Reddit"]])

(defn market-value-tab []
  [rn/view
   [rn/view {:style st/market-data-row}
    [rn/view
     [market-data-item "Market Cap" "230,019,822 USD" true]
     [market-data-item "Circulating Supply" "1,104,590 GNO" true]]
    [rn/view
     [market-data-item "Volume 24h" "3,567,910 USD" true]
     [market-data-item "Total Supply" "10,000,000 GNO" true]]]
   [market-data-item "Concept" "Status is a free (libre) and open source mobile client targeting Android & iOS, built entirely on Ethereum technologies."]
   [market-data-item "Crowdsale opening date" "20. Jun 2017"]
   [market-data-item "Crowdsale closing date" "22. Jun 2017"]
   [market-data-item "Links" market-data-links false true]])

(defn my-token-tab []
  [rn/view
   [token-balance]
   [transactions-section]])

(defn screen []
  [rn/scroll-view {:style st/screen-container}
   [toolbar-view]
   [rn/view {:style st/tabs-container}
    [market-value-tab]
    [my-token-tab]]])

;; (defn header []
;;   [rn/view {:style st/header-container}
;;    [rn/image {:style st/token-image
;;               :src }]
;;    [rn/text {:style st/token-name} "Gnosis"]
;;    [rn/text {:style st/token-abbreviation} "GNO"]])
