(ns status-im.ui.screens.chat.message.command
  (:require [re-frame.core :as re-frame]
            [status-im.commands.core :as commands]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.money :as money]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn- final-status? [command-state]
  (or (= command-state constants/command-state-request-address-for-transaction-declined)
      (= command-state constants/command-state-request-transaction-declined)
      (= command-state constants/command-state-transaction-sent)))

(defn- command-pending-status
  [command-state direction to transaction-type]
  [react/view {:style {:flex-direction :row
                       :height 28
                       :align-items :center
                       :border-width 1
                       :border-color colors/gray-lighter
                       :border-radius 16
                       :padding-horizontal 8
                       :margin-right 12
                       :margin-bottom 2}}
   [vector-icons/icon :tiny-icons/tiny-pending
    {:width 16
     :height 16
     :color colors/gray
     :container-style {:margin-right 6}}]
   [react/text {:style {:color colors/gray
                        :font-weight "500"
                        :line-height 16
                        :margin-right 4
                        :font-size 13}}
    (if (and (or (= command-state constants/command-state-request-transaction)
                 (= command-state constants/command-state-request-address-for-transaction-accepted))
             (= direction :incoming))
      (str (i18n/label :t/shared) " '" (:name @(re-frame/subscribe [:account-by-address to])) "'")
      (i18n/label (cond
                    (= command-state constants/command-state-transaction-pending)
                    :t/status-pending
                    (= command-state constants/command-state-request-address-for-transaction)
                    :t/address-requested
                    (= command-state constants/command-state-request-address-for-transaction-accepted)
                    :t/address-request-accepted
                    (= command-state constants/command-state-transaction-sent)
                    (case transaction-type
                      :pending :t/status-pending
                      :failed :t/transaction-failed
                      :t/status-confirmed)
                    (= command-state constants/command-state-request-transaction)
                    :t/address-received)))]])

(defn- command-final-status
  [command-state direction transaction-type]
  [react/view {:style {:flex-direction :row
                       :height 28
                       :align-items :center
                       :border-width 1
                       :border-color colors/gray-lighter
                       :border-radius 16
                       :padding-horizontal 8
                       :margin-right 12
                       :margin-bottom 2}}
   (if (or (= command-state constants/command-state-request-address-for-transaction-declined)
           (= command-state constants/command-state-request-transaction-declined)
           (= :failed transaction-type))
     [vector-icons/icon :tiny-icons/tiny-warning
      {:width 16
       :height 16
       :container-style {:margin-right 6}}]
     (if (= :pending transaction-type)
       [vector-icons/icon :tiny-icons/tiny-pending
        {:color colors/gray
         :width 16
         :height 16
         :container-style {:margin-right 6}}]
       [vector-icons/icon :tiny-icons/tiny-check
        {:width 16
         :height 16
         :container-style {:margin-right 6}}]))
   [react/text {:style (merge {:margin-right 4
                               :line-height 16
                               :font-size 13}
                              (if (= transaction-type :pending)
                                {:color colors/gray}
                                {:font-weight "500"}))}
    (i18n/label (if (or (= command-state constants/command-state-request-address-for-transaction-declined)
                        (= command-state constants/command-state-request-transaction-declined))
                  :t/transaction-declined
                  (case transaction-type
                    :pending :t/status-pending
                    :failed :t/transaction-failed
                    :t/status-confirmed)))]])

(defn- command-status-and-timestamp
  [command-state direction to timestamp-str transaction-type]
  [react/view {:style {:flex-direction :row
                       :align-items :flex-end
                       :justify-content :space-between}}
   (if (final-status? command-state)
     [command-final-status command-state direction transaction-type]
     [command-pending-status command-state direction to transaction-type])
   [react/text {:style {:font-size 10
                        :line-height 12
                        :text-align-vertical :bottom
                        :color colors/gray}}
    timestamp-str]])

(defn- command-actions
  [accept-label on-accept on-decline]
  [react/view
   [react/touchable-highlight
    {:on-press #(do (react/dismiss-keyboard!)
                    (on-accept))
     :style {:border-color colors/gray-lighter
             :border-top-width 1
             :margin-top 8
             :margin-horizontal -12
             :padding-horizontal 15
             :padding-vertical 10}}
    [react/text {:style {:text-align :center
                         :color colors/blue
                         :font-weight "500"
                         :font-size 15
                         :line-height 22}}
     (i18n/label accept-label)]]
   (when on-decline
     [react/touchable-highlight
      {:on-press on-decline
       :style {:border-color colors/gray-lighter
               :border-top-width 1
               :margin-horizontal -12
               :padding-top 10}}
      [react/text {:style {:text-align :center
                           :color colors/blue
                           :font-size 15
                           :line-height 22}}
       (i18n/label :t/decline)]])])

(defn- command-transaction-info
  [contract value]
  (let [{:keys [symbol icon decimals color] :as token}
        (if (seq contract)
          (get @(re-frame/subscribe [:wallet/chain-tokens])
               contract
               transactions/default-erc20-token)
          @(re-frame/subscribe [:ethereum/native-currency]))
        amount (money/internal->formatted value symbol decimals)
        {:keys [code]}
        @(re-frame/subscribe [:wallet/currency])
        prices @(re-frame/subscribe [:prices])
        amount-fiat (money/fiat-amount-value amount symbol (keyword code) prices)]
    [react/view {:style {:flex-direction :row
                         :margin-top 8
                         :margin-bottom 12}}
     (if icon
       [react/image (-> icon
                        (update :source #(%))
                        (assoc-in [:style :height] 24)
                        (assoc-in [:style :width] 24))]
       [react/view {:style {:margin-right     14
                            :padding-vertical 2
                            :justify-content  :flex-start
                            :max-width        40
                            :align-items      :center
                            :align-self       :stretch}}
        [chat-icon/custom-icon-view-list (:name token) color 24]])
     [react/view {:style {:margin-left 6}}
      [react/text {:style {:margin-bottom 2
                           :font-size 20
                           :line-height 24}}
       (str amount " " (name symbol))]
      [react/text {:style {:font-size 12
                           :line-height 16
                           :color colors/gray}}
       (str amount-fiat " " code)]]]))

(defn calculate-direction [outgoing command-state]
  (case command-state
    (constants/command-state-request-address-for-transaction-accepted
     constants/command-state-request-address-for-transaction-declined
     constants/command-state-request-transaction)
    (if outgoing :incoming :outgoing)
    (if outgoing :outgoing :incoming)))

(defn comand-content
  [wrapper {:keys [message-id
                   chat-id
                   outgoing
                   command-parameters
                   timestamp-str] :as message}]
  (let [{:keys [contract value address command-state transaction-hash]} command-parameters
        direction (calculate-direction outgoing command-state)
        transaction (when transaction-hash
                      @(re-frame/subscribe
                        [:wallet/account-by-transaction-hash
                         transaction-hash]))]
    [wrapper (assoc message :outgoing (= direction :outgoing))
     [react/touchable-highlight
      {:on-press #(when (:address transaction)
                    (re-frame/dispatch [:wallet.ui/show-transaction-details
                                        transaction-hash (:address transaction)]))}
      [react/view {:padding-horizontal                     12
                   :padding-bottom                         10
                   :padding-top                            10
                   :margin-top                             4
                   :border-width                           1
                   :border-color                           colors/gray-lighter
                   :border-radius                          16
                   (case direction
                     :outgoing :border-bottom-right-radius
                     :incoming :border-bottom-left-radius) 4
                   :background-color                       colors/white}
       [react/text {:style {:font-size   13
                            :line-height 18
                            :font-weight "500"
                            :color       colors/gray}}
        (case direction
          :outgoing (str "↑ " (i18n/label :t/outgoing-transaction))
          :incoming (str "↓ " (i18n/label :t/incoming-transaction)))]
       [command-transaction-info contract value]
       [command-status-and-timestamp
        command-state direction address timestamp-str (:type transaction)]
       (when (not outgoing)
         (cond
           (= command-state constants/command-state-request-transaction)
           [command-actions
            :t/sign-and-send
            #(re-frame/dispatch
              [:wallet.ui/accept-request-transaction-button-clicked-from-command
               chat-id
               command-parameters])
            #(re-frame/dispatch [::commands/decline-request-transaction message-id])]

           (= command-state
              constants/command-state-request-address-for-transaction-accepted)
           [command-actions
            :t/sign-and-send
            #(re-frame/dispatch
              [:wallet.ui/accept-request-transaction-button-clicked-from-command
               chat-id
               command-parameters])]

           (= command-state constants/command-state-request-address-for-transaction)
           [command-actions
            :t/accept-and-share-address
            #(re-frame/dispatch
              [::commands/prepare-accept-request-address-for-transaction
               message])
            #(re-frame/dispatch
              [::commands/decline-request-address-for-transaction
               message-id])]))]]]))