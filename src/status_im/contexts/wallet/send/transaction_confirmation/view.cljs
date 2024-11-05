(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.send.transaction-confirmation.account-summary :as account-summary]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [status-im.contexts.wallet.send.transaction-confirmation.transaction-title :as transaction-title]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- data-item
  [{:keys [title subtitle]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :status          :default
    :size            :small
    :title           title
    :subtitle        subtitle}])

(defn- estimated-time-view
  []
  (let [estimated-time (rf/sub [:wallet/send-estimated-time])]
    [data-item
     {:title    (i18n/label :t/est-time)
      :subtitle (i18n/label :t/time-in-mins
                            {:minutes (str estimated-time)})}]))

(defn- max-fees-view
  []
  (let [native-currency-symbol (-> (rf/sub [:wallet/send-route])
                                   first
                                   (get-in [:from :native-currency-symbol]))
        max-fees               (rf/sub [:wallet/wallet-send-fee-fiat-formatted
                                        native-currency-symbol])]
    [data-item
     {:title    (i18n/label :t/max-fees)
      :subtitle max-fees}]))

(defn- bridge-received-amount
  []
  (let [to-network (rf/sub [:wallet/bridge-to-network-details])
        amount     (rf/sub [:wallet/send-total-amount-formatted])]
    [data-item
     {:title    (i18n/label :t/bridged-to
                            {:network (:abbreviated-name to-network)})
      :subtitle amount}]))

(defn- send-received-amount
  []
  (let [amount (rf/sub [:wallet/send-total-amount-formatted])]
    [data-item
     {:title    (i18n/label :t/recipient-gets)
      :subtitle amount}]))

(defn- received-amount-view
  []
  (let [tx-type (rf/sub [:wallet/send-tx-type])]
    (if (= tx-type :tx/bridge)
      [bridge-received-amount]
      [send-received-amount])))

(defn- transaction-details
  []
  (let [route-loaded?             (seq (rf/sub [:wallet/send-route]))
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        sending-collectible?      (rf/sub [:wallet/sending-collectible?])]
    [rn/view
     {:style (style/details-container
              {:loading-suggested-routes? loading-suggested-routes?
               :route-loaded?             route-loaded?})}
     (cond
       loading-suggested-routes?
       [rn/activity-indicator {:style {:flex 1}}]

       route-loaded?
       [:<>
        [estimated-time-view]
        [max-fees-view]
        (when-not sending-collectible?
          [received-amount-view])]

       :else
       [quo/text {:style {:align-self :center}}
        (i18n/label :t/no-routes-found-confirmation)])]))

(defn- slide-to-confirm
  []
  (let [route-loaded?             (seq (rf/sub [:wallet/send-route]))
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        tx-type                   (rf/sub [:wallet/send-tx-type])
        customization-color       (rf/sub [:wallet/current-viewing-account-color])]
    (when (and (not loading-suggested-routes?) route-loaded?)
      [standard-auth/slide-button
       {:keycard-supported?  true
        :size                :size-48
        :track-text          (if (= tx-type :tx/bridge)
                               (i18n/label :t/slide-to-bridge)
                               (i18n/label :t/slide-to-send))
        :container-style     {:z-index 2}
        :customization-color customization-color
        :on-auth-success     #(rf/dispatch
                               [:wallet/send-transaction
                                (security/safe-unmask-data %)])
        :auth-button-label   (i18n/label :t/confirm)}])))

(defn- on-close
  []
  (rf/dispatch [:wallet/transaction-confirmation-navigate-back]))

(defn view
  [_]
  (let [customization-color (rf/sub [:wallet/current-viewing-account-color])]
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/arrow-left
                                   :on-press            on-close
                                   :margin-top          (safe-area/get-top)
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [:<>
                                  [transaction-details]
                                  [slide-to-confirm]]
       :gradient-cover?          true
       :customization-color      customization-color}
      [rn/view
       [transaction-title/view]
       [account-summary/from-summary]
       [account-summary/to-summary]]]]))
