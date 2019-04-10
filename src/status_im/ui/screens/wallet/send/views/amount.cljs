(ns status-im.ui.screens.wallet.send.views.amount
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.send.views.common :as common]
            [re-frame.core :as re-frame]
            [status-im.utils.money :as money]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [clojure.string :as string]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [reagent.core :as reagent]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.styles :as components.styles]))

(defn white-toolbar [modal? title]
  (let [action (if modal? actions/close actions/back)]
    [toolbar/toolbar {:style {:background-color    colors/white
                              :border-bottom-width 1
                              :border-bottom-color colors/black-transparent}}
     [toolbar/nav-button (action (if modal?
                                   #(re-frame/dispatch [:wallet/discard-transaction-navigate-back])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color       colors/black
                             :font-size   17} title]]))

(defn- render-token-item [{:keys [name icon decimals amount] :as coin}]
  [list/item
   [list/item-image icon]
   [list/item-content
    [react/text {:style {:margin-right 10, :color colors/black}} name]
    [list/item-secondary (str (wallet.utils/format-amount amount decimals)
                              " "
                              (wallet.utils/display-symbol coin))]]])

(defview choose-asset []
  (letsubs [assets [:wallet/transferrable-assets-with-amount]
            {:keys [on-asset]} [:get-screen-params :wallet-choose-asset]]
    [wallet.components/simple-screen {:avoid-keyboard? true
                                      :status-bar-type :main}
     [white-toolbar false (i18n/label :t/choose-asset)]
     [react/view {:style (assoc components.styles/flex :background-color :white)}
      [list/flat-list {:default-separator? false ;true
                       :data               assets
                       :key-fn             (comp str :symbol)
                       :render-fn          #(do
                                              [react/touchable-highlight {:on-press       (fn [] (on-asset %))
                                                                          :underlay-color colors/black-transparent}
                                               (render-token-item %)])}]]]))

(defn show-current-asset [{:keys [name icon decimals amount] :as token}]
  [react/view {:style {:flex-direction     :row,
                       :justify-content    :center
                       :padding-horizontal 21
                       :padding-vertical   12}}
   [list/item-image icon]
   [react/view {:margin-horizontal 9
                :flex              1}
    [list/item-content
     [react/text {:style {:margin-right 10,
                          :font-weight  "500"
                          :font-size    15
                          :color        colors/white}} name]
     [react/text {:style           {:font-size   14
                                    :padding-top 4
                                    :color       colors/white-transparent}
                  :ellipsize-mode  :middle
                  :number-of-lines 1}
      (str (wallet.utils/format-amount amount decimals)
           " "
           (wallet.utils/display-symbol token))]]]
   list/item-icon-forward])

(defn fetch-token [all-tokens network token-symbol]
  {:pre [(map? all-tokens) (map? network)]}
  (when (keyword? token-symbol)
    (tokens/asset-for all-tokens
                      (ethereum/network->chain-keyword network)
                      token-symbol)))

(defn create-initial-state [{:keys [symbol decimals]} amount]
  {:input-amount  (when amount
                    (when-let [amount' (money/internal->formatted amount symbol decimals)]
                      (str amount')))
   :inverted      false
   :edit-gas      false
   :error-message nil})

(defn input-currency-symbol [{:keys [inverted] :as state} {:keys [symbol] :as coin} {:keys [code]}]
  {:pre [(boolean? inverted) (keyword? symbol) (string? code)]}
  (if-not (:inverted state) (wallet.utils/display-symbol coin) code))

(defn converted-currency-symbol [{:keys [inverted] :as state} {:keys [symbol] :as coin} {:keys [code]}]
  {:pre [(boolean? inverted) (keyword? symbol) (string? code)]}
  (if (:inverted state) (wallet.utils/display-symbol coin) code))

(defn valid-input-amount? [input-amount]
  (and (not (string/blank? input-amount))
       (not (:error (wallet.db/parse-amount input-amount 100)))))

(defn converted-currency-amount [{:keys [input-amount inverted]} token fiat-currency prices]
  (when (valid-input-amount? input-amount)
    (some-> (common/token->fiat-conversion prices token fiat-currency input-amount)
            (money/with-precision (if inverted 8 2)))))

(defn converted-currency-phrase [state token fiat-currency prices]
  (str (if-let [amount-bn (converted-currency-amount state token fiat-currency prices)]
         (str amount-bn)
         "0")
       " " (converted-currency-symbol state token fiat-currency)))

(defn current-token-input-amount [{:keys [input-amount inverted] :as state} token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (when input-amount
    (when-let [amount-bn (if inverted
                           (common/fiat->token-conversion prices token fiat-currency input-amount)
                           (money/bignumber input-amount))]
      (money/formatted->internal amount-bn (:symbol token) (:decimals token)))))

(defn update-input-errors [{:keys [input-amount inverted] :as state} token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (let [{:keys [_value error]}
        (wallet.db/parse-amount input-amount
                                (if inverted 2 (:decimals token)))]
    (if-let [error-msg
             (cond
               error error
               (not (money/sufficient-funds? (current-token-input-amount state token fiat-currency prices)
                                             (:amount token)))
               (i18n/label :t/wallet-insufficient-funds)
               :else nil)]
      (assoc state :error-message error-msg)
      state)))

(defn update-input-amount [state input-str token fiat-currency prices]
  {:pre [(map? state) (map? token) (map? fiat-currency) (map? prices)]}
  (let [has-value? (not (string/blank? input-str))]
    (cond-> (-> state
                (assoc :input-amount input-str)
                (dissoc :error-message))
      has-value? (update-input-errors token fiat-currency prices))))

(defn render-choose-amount [{:keys [web3
                                    network
                                    native-currency
                                    all-tokens
                                    contact
                                    transaction]}]
  {:pre [(map? native-currency)]}
  (let [tx-atom                (reagent/atom transaction)
        coin                   (or (fetch-token all-tokens network (:symbol transaction))
                                   native-currency)
        state-atom             (reagent/atom (create-initial-state coin (:amount transaction)))
        amount-input-ref       (atom nil)
        network-fees-modal-ref (atom nil)
        open-network-fees!     #(do (.blur @amount-input-ref)
                                    (common/anim-ref-send @network-fees-modal-ref :open!))
        close-network-fees!    #(common/anim-ref-send @network-fees-modal-ref :close!)]
    (when-not (common/optimal-gas-present? transaction)
      (common/refresh-optimal-gas web3 tx-atom))
    (fn [{:keys [balance network prices fiat-currency
                 native-currency all-tokens modal?]}]
      (let [symbol (some :symbol [@tx-atom native-currency])
            coin  (-> (tokens/asset-for all-tokens (ethereum/network->chain-keyword network) symbol)
                      (assoc :amount (get balance symbol (money/bignumber 0))))
            gas-gas-price->fiat
            (fn [gas-map]
              (common/network-fees prices native-currency fiat-currency (common/max-fee gas-map)))
            update-amount-field #(swap! state-atom update-input-amount % coin fiat-currency prices)]
        [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                          :status-bar-type (if modal? :modal-wallet :wallet)}
         [common/toolbar :wallet (i18n/label :t/send-amount) nil]
         (if (empty? balance)
           (common/info-page (i18n/label :t/wallet-no-assets-enabled))
           (let [{:keys [error-message input-amount] :as state} @state-atom
                 input-symbol     (input-currency-symbol state coin fiat-currency)
                 converted-phrase (converted-currency-phrase state coin fiat-currency prices)]
             [react/view {:flex 1}
              ;; network fees modal
              (when (common/optimal-gas-present? @tx-atom)
                [common/slide-up-modal {:anim-ref       #(reset! network-fees-modal-ref %)
                                        :swipe-dismiss? true}
                 [common/custom-gas-input-panel
                  (-> (select-keys @tx-atom [:gas :gas-price :optimal-gas :optimal-gas-price])
                      (assoc
                       :fiat-currency fiat-currency
                       :gas-gas-price->fiat gas-gas-price->fiat
                       :on-submit (fn [{:keys [gas gas-price]}]
                                    (if (and gas gas-price)
                                      (swap! tx-atom assoc :gas gas :gas-price gas-price)
                                      (swap! tx-atom dissoc :gas :gas-price))
                                    (close-network-fees!))))]])
              [react/touchable-highlight {:style          {:background-color colors/black-transparent}
                                          :on-press       #(re-frame/dispatch
                                                            [:navigate-to :wallet-choose-asset
                                                             {:on-asset (fn [{:keys [symbol]}]
                                                                          (when symbol
                                                                            (if-not (= symbol (:symbol @tx-atom))
                                                                              (update-amount-field nil))
                                                                            (swap! tx-atom assoc :symbol symbol)
                                                                            (common/refresh-optimal-gas web3 tx-atom))
                                                                          (re-frame/dispatch [:navigate-back]))}])
                                          :underlay-color colors/white-transparent}
               [show-current-asset coin]]
              [react/view {:flex 1}
               [react/view {:flex 1}]
               [react/view {:justify-content :center
                            :align-items     :center
                            :flex-direction  :row}
                (when error-message
                  [tooltip/tooltip error-message {:color        colors/white
                                                  :font-size    12
                                                  :bottom-value 15}])
                [react/text-input
                 {:ref                    #(reset! amount-input-ref %)
                  :on-change-text         update-amount-field
                  :keyboard-type          :numeric
                  :accessibility-label    :amount-input
                  :auto-focus             true
                  :auto-capitalize        :none
                  :auto-correct           false
                  :placeholder            "0"
                  :placeholder-text-color colors/blue-shadow
                  :multiline              true
                  :max-length             20
                  :default-value          input-amount
                  :selection-color        colors/green
                  :keyboard-appearance    :dark
                  :style                  {:color               colors/white
                                           :font-size           30
                                           :font-weight         "500"
                                           :padding-horizontal  10
                                           :padding-vertical    7
                                           :max-width           290
                                           :text-align-vertical :center}}]
                [react/text {:style {:color               (if (string/blank? input-amount)
                                                            colors/blue-shadow
                                                            colors/white)
                                     :font-size           30
                                     :font-weight         "500"
                                     :text-align-vertical :center}}
                 input-symbol]]
               [react/view {}
                [react/text {:style {:text-align  :center
                                     :margin-top  16
                                     :font-size   15
                                     :line-height 22
                                     :color       colors/blue-shadow}}
                 converted-phrase]]
               (when (valid-input-amount? input-amount)
                 [react/view {:justify-content :center
                              :flex-direction  :row}
                  [react/touchable-highlight {:on-press open-network-fees!
                                              :style    {:background-color   colors/black-transparent
                                                         :padding-horizontal 13
                                                         :padding-vertical   7
                                                         :margin-top         1
                                                         :border-radius      8}}
                   [react/text {:style {:color       colors/white
                                        :font-size   15
                                        :line-height 22}}
                    (i18n/label :t/network-fee-amount {:amount   (str (or (gas-gas-price->fiat (common/current-gas @tx-atom)) "0"))
                                                       :currency (:code fiat-currency)})]]])
               [react/view {:flex 1}]

               [react/view {:flex-direction :row
                            :padding        3}
                [common/action-button {:underlay-color   (colors/alpha colors/black 0.2)
                                       :background-color colors/black-transparent
                                       :on-press         #(swap! state-atom update :inverted not)}
                 [react/view {:flex-direction :row}
                  [react/text {:style {:color         colors/white
                                       :font-size     15
                                       :line-height   22
                                       :padding-right 10}}
                   (:code fiat-currency)]
                  [vector-icons/icon :main-icons/change {:color colors/white-transparent}]
                  [react/text {:style {:color        colors/white
                                       :font-size    15
                                       :line-height  22
                                       :padding-left 11}}
                   (wallet.utils/display-symbol coin)]]]
                (let [disabled? (or (string/blank? input-amount)
                                    (not (empty? (:error-message @state-atom))))]
                  [common/action-button {:disabled?        disabled?
                                         :underlay-color   colors/black-transparent
                                         :background-color (if disabled? colors/blue colors/white)
                                         :token            coin
                                         :on-press         #(re-frame/dispatch [:navigate-to :wallet-txn-overview
                                                                                {:modal?      modal?
                                                                                 :contact     contact
                                                                                 :transaction (assoc @tx-atom
                                                                                                     :amount (money/formatted->internal
                                                                                                              (money/bignumber input-amount)
                                                                                                              (:symbol coin)
                                                                                                              (:decimals coin)))}])}
                   [react/text {:style {:color       (if disabled?
                                                       (colors/alpha colors/white 0.3)
                                                       colors/blue)
                                        :font-size   15
                                        :line-height 22}}
                    (i18n/label :t/next)]])]]]))]))))
