(ns status-im.ui.screens.wallet.send.views.overview
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.send.views.common :as common]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.utils.money :as money]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.wallet.send.events :as events]
            [status-im.ui.components.list.styles :as list.styles]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.ui.screens.chat.photos :as photos]))

(def signing-popup
  {:background-color        colors/white
   :border-top-left-radius  8
   :border-top-right-radius 8
   :position                :absolute
   :left                    0
   :right                   0
   :bottom                  0})

(defn confirm-modal [signing? {:keys [transaction total-amount gas-amount native-currency fiat-currency total-fiat]}]
  [react/view {:style signing-popup}
   [react/text {:style {:color       colors/black
                        :font-size   15
                        :line-height 22
                        :margin-top  23
                        :text-align  :center}}
    (i18n/label :t/total)]
   [react/text {:style {:color       colors/black
                        :margin-top  4
                        :font-size   22
                        :line-height 28
                        :text-align  :center}}
    (str total-amount " " (name (:symbol transaction)))]
   (when-not (= :ETH (:symbol transaction))
     [react/text {:style {:color       colors/black
                          :margin-top  5
                          :font-size   22
                          :line-height 28
                          :text-align  :center}}
      (str gas-amount " " (name (:symbol native-currency)))])
   [react/text {:style {:color       colors/gray
                        :text-align  :center
                        :margin-top  3
                        :line-height 21
                        :font-size   15}}
    (str "~ " (:symbol fiat-currency "$") total-fiat)]
   [react/view {:style {:flex-direction  :row
                        :justify-content :center
                        :padding-top     16
                        :padding-bottom  24}}
    [react/touchable-highlight
     {:on-press #(reset! signing? true)
      :style    {:padding-horizontal 39
                 :padding-vertical   12
                 :border-radius      8
                 :background-color   colors/blue-light}}
     [react/text {:style {:font-size   15
                          :line-height 22
                          :color       colors/blue}}
      (i18n/label :t/confirm)]]]])

(defn- phrase-word [word]
  [react/text {:style {:color       colors/blue
                       :font-size   15
                       :line-height 22
                       :font-weight "500"
                       :width       "33%"
                       :text-align  :center}}
   word])

(defn- phrase-separator []
  [react/view {:style {:height           "100%"
                       :width            1
                       :background-color colors/gray-light}}])

(defview sign-modal [account {:keys [transaction contact total-amount gas-amount native-currency fiat-currency
                                     total-fiat all-tokens chain flow]}]
  (letsubs [password     (reagent/atom nil)
            in-progress? (reagent/atom nil)]
    (let [phrase (string/split (:signing-phrase account) #" ")]
      [react/view {:style {:position :absolute
                           :left     0
                           :right    0
                           :bottom   0}}
       [tooltip/tooltip (i18n/label :t/wallet-passphrase-reminder)
        {:bottom-value 12
         :color        colors/white
         :text-color   colors/blue
         :font-size    12}]
       [react/view {:style {:background-color        colors/white
                            :border-top-left-radius  8
                            :border-top-right-radius 8}}
        [react/view {:flex              1
                     :height            46
                     :margin-top        18
                     :flex-direction    :row
                     :align-items       :center
                     :margin-horizontal "15%"
                     :border-width      1
                     :border-color      colors/gray-light
                     :border-radius     218}
         [phrase-word (first phrase)]
         [phrase-separator]
         [phrase-word (second phrase)]
         [phrase-separator]
         [phrase-word (last phrase)]]
        [react/text {:style {:color       colors/black
                             :margin-top  13
                             :font-size   22
                             :line-height 28
                             :text-align  :center}}
         (str "Send" " " total-amount " " (name (:symbol transaction)))]
        (when-not (= :ETH (:symbol transaction))
          [react/text {:style {:color       colors/black
                               :margin-top  5
                               :font-size   22
                               :line-height 28
                               :text-align  :center}}
           (str "Send" " " gas-amount " " (name (:symbol native-currency)))])
        [react/text {:style {:color       colors/gray
                             :text-align  :center
                             :margin-top  3
                             :line-height 21
                             :font-size   15}}
         (str "~ " (:symbol fiat-currency "$") total-fiat)]
        [react/text-input
         {:auto-focus             false
          :secure-text-entry      true
          :placeholder            (i18n/label :t/enter-your-login-password)
          :placeholder-text-color colors/gray
          :on-change-text         #(reset! password %)
          :style                  {:flex              1
                                   :margin-top        15
                                   :margin-horizontal 15
                                   :padding           14
                                   :padding-bottom    18
                                   :background-color  colors/gray-lighter
                                   :border-radius     8
                                   :font-size         15
                                   :letter-spacing    -0.2
                                   :height            52}
          :accessibility-label    :enter-password-input
          :keyboard-appearance    :dark
          :auto-capitalize        :none}]
        [react/view {:style {:flex-direction  :row
                             :justify-content :center
                             :padding-top     16
                             :padding-bottom  24}}
         [react/touchable-highlight
          {:on-press #(events/send-transaction-wrapper {:transaction  transaction
                                                        :password     @password
                                                        :flow         flow
                                                        :all-tokens   all-tokens
                                                        :in-progress? in-progress?
                                                        :chain        chain
                                                        :contact      contact
                                                        :account      account})
           :disabled @in-progress?
           :style    {:padding-horizontal 39
                      :padding-vertical   12
                      :border-radius      8
                      :background-color   colors/blue-light}}
          [react/text {:style {:font-size   15
                               :line-height 22
                               :color       colors/blue}}
           (i18n/label :t/send)]]]]])))

(defview confirm-and-sign [params]
  (letsubs [signing? (reagent/atom false)
            account  [:account/account]]
    (if-not @signing?
      [confirm-modal signing? params]
      [sign-modal account params])))

(defn render-transaction-overview [{:keys [flow transaction contact token native-currency
                                           fiat-currency prices all-tokens chain web3]}]
  (let [tx-atom                (reagent/atom transaction)
        network-fees-modal-ref (atom nil)
        open-network-fees!     #(common/anim-ref-send @network-fees-modal-ref :open!)
        close-network-fees!    #(common/anim-ref-send @network-fees-modal-ref :close!)
        modal?                 (= :dapp flow)]
    (when-not (common/optimal-gas-present? transaction)
      (common/refresh-optimal-gas web3 tx-atom))
    (fn []
      (let [transaction @tx-atom
            gas-gas-price->fiat
            (fn [gas-map]
              (common/network-fees prices token fiat-currency (common/max-fee gas-map)))

            network-fee-eth (common/max-fee (common/current-gas transaction))
            network-fee-fiat
            (when (common/optimal-gas-present? transaction)
              (common/network-fees prices native-currency fiat-currency network-fee-eth))

            formatted-amount
            (money/internal->formatted (:amount transaction)
                                       (:symbol token)
                                       (:decimals token))
            amount-str (str formatted-amount
                            " " (wallet.utils/display-symbol token))

            fiat-amount (some-> (common/token->fiat-conversion prices token fiat-currency formatted-amount)
                                (money/with-precision 2))

            total-amount (some-> (if (= :ETH (:symbol transaction))
                                   (.add (money/bignumber formatted-amount) network-fee-eth)
                                   (money/bignumber formatted-amount))
                                 (money/with-precision (:decimals token)))
            gas-amount (some-> (when-not (= :ETH (:symbol transaction))
                                 (money/bignumber network-fee-eth))
                               (money/with-precision 18))

            total-fiat (some-> (common/token->fiat-conversion prices token fiat-currency total-amount)
                               (money/with-precision 2))]
        [wallet.components/simple-screen {:avoid-keyboard? (not modal?)
                                          :status-bar-type (if modal? :modal-wallet :wallet)}
         [common/toolbar flow (i18n/label :t/send-amount) (:public-key contact)]
         [react/view {:style {:flex             1
                              :border-top-width 1
                              :border-top-color colors/white-light-transparent}}
          (when (common/optimal-gas-present? @tx-atom)
            [common/slide-up-modal {:anim-ref       #(reset! network-fees-modal-ref %)
                                    :swipe-dismiss? true}
             [common/custom-gas-input-panel
              (-> (select-keys @tx-atom [:gas :gas-price :optimal-gas :optimal-gas-price])
                  (assoc
                   :fiat-currency fiat-currency
                   :gas-gas-price->fiat gas-gas-price->fiat
                   :on-submit (fn [{:keys [gas gas-price]}]
                                (when (and gas gas-price)
                                  (swap! tx-atom assoc :gas gas :gas-price gas-price))
                                (close-network-fees!))))]])
          [react/text {:style {:margin-top 18
                               :text-align :center
                               :font-size  15
                               :color      colors/white-transparent}}
           (i18n/label :t/recipient)]
          [react/view
           (when contact
             [react/view {:style {:margin-top      10
                                  :flex-direction  :row
                                  :justify-content :center}}
              [photos/photo (:photo-path contact) {:size list.styles/image-size}]])
           [react/text {:style {:color             colors/white
                                :margin-horizontal 24
                                :margin-top        10
                                :line-height       22
                                :font-size         15
                                :text-align        :center}}
            (ethereum/normalized-address (:to transaction))]]
          [react/text {:style {:margin-top 18
                               :font-size  15
                               :text-align :center
                               :color      colors/white-transparent}}
           (i18n/label :t/amount)]
          [react/view {:style {:flex-direction    :row
                               :align-items       :center
                               :margin-top        10
                               :margin-horizontal 24}}
           [react/text {:style {:color     colors/white
                                :font-size 15}} (i18n/label :t/sending)]
           [react/view {:style {:flex 1}}
            [react/text {:style {:color       colors/white
                                 :line-height 21
                                 :font-size   15
                                 :font-weight "500"
                                 :text-align  :right}}
             amount-str]
            [react/text {:style {:color       colors/white-transparent
                                 :line-height 21
                                 :font-size   15
                                 :text-align  :right}}
             (str "~ " (:symbol fiat-currency "$")  fiat-amount " " (:code fiat-currency))]]]
          [react/view {:style {:margin-horizontal 24
                               :margin-top        10
                               :padding-top       10
                               :border-top-width  1
                               :border-top-color  colors/white-light-transparent}}
           [react/view {:style {:flex-direction :row
                                :align-items    :center}}
            [react/touchable-highlight {:on-press            #(open-network-fees!)
                                        :accessibility-label :network-fee-button
                                        :style               {:background-color   colors/black-transparent
                                                              :padding-horizontal 16
                                                              :padding-vertical   9
                                                              :border-radius      8}}
             [react/view {:style {:flex-direction :row
                                  :align-items    :center}}
              [react/text {:style {:color         colors/white
                                   :padding-right 10
                                   :font-size     15}} (i18n/label :t/network-fee)]
              [vector-icons/icon :main-icons/settings {:color colors/white}]]]
            [react/view {:style {:flex 1}}
             [react/text {:style {:color       colors/white
                                  :line-height 21
                                  :font-size   15
                                  :font-weight "500"
                                  :text-align  :right}
                          :accessibility-label :total-fee}
              (str network-fee-eth " " (wallet.utils/display-symbol native-currency))]
             [react/text {:style {:color       colors/white-transparent
                                  :line-height 21
                                  :font-size   15
                                  :text-align  :right}}
              (str "~ "  network-fee-fiat " " (:code fiat-currency))]]]]
          [confirm-and-sign {:transaction     transaction
                             :contact         contact
                             :total-amount    total-amount
                             :gas-amount      gas-amount
                             :native-currency native-currency
                             :fiat-currency   fiat-currency
                             :total-fiat      total-fiat
                             :all-tokens      all-tokens
                             :chain           chain
                             :flow            flow}]]]))))