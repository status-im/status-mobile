(ns status-im.ui.screens.wallet.wallet-list.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.list.views :as list]
            [status-im.components.react :as rn]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.wallet-list.styles :as st]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]))

(defn toolbar-actions []
  [(act/add-wallet #(utils/show-popup "TODO" "Not implemented!"))])

(defn toolbar-view [transactions]
  [toolbar/toolbar {:style      st/toolbar
                    :title      (i18n/label :t/wallets)
                    :nav-action (act/close #(rf/dispatch [:navigate-back]))
                    :actions    (toolbar-actions)}])

(defn- select-wallet []
  (utils/show-popup "TODO" "Not implemented!"))

(defn wallet-list-item [{:keys [name currency amount assets color]}]
  (let [asset-list (string/join "  " assets)]
    [rn/touchable-highlight {:on-press select-wallet}
     [rn/view {:style (st/wallet-item (keyword color))}
      [rn/view {:style st/wallet-info}
       [rn/text {:style st/wallet-name} name]
       [rn/view {:style st/total-balance}
        [rn/text {:style st/total-balance-value} amount]
        [rn/text {:style st/total-balance-currency} currency]]
       [rn/text {:style st/asset-list} asset-list]]
      [rn/icon :forward_gray st/select-wallet-icon]]]))

(def dummy-wallet-data
  [{:name     "Main wallet"
    :currency "USD"
    :amount   12.43
    :assets   ["SNT" "SGT" "GNO" "ETH"]
    :color    "blue-1"}
   {:name     "Other wallet"
    :currency "ETH"
    :amount   0.34
    :assets   ["ETH"]
    :color    "gray-1"}])

(defn wallet-list []
  [rn/scroll-view {:style st/wallet-list-screen}
   [rn/text {:style st/wallet-list-title} (i18n/label :t/your-wallets)]
   [list/flat-list {:data          dummy-wallet-data
                    :render-fn     wallet-list-item
                    :style         st/wallet-list
                    :scrollEnabled false}]])

(defview wallet-list-screen []
  []
  [rn/view {:style st/screen-container}
   [status-bar/status-bar {:type :main}]
   [toolbar-view]
   [wallet-list]])
