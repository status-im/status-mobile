(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as common-utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def row-height 44)
(def space-between-rows 11)
(def network-link-linear-height 10)
(def network-link-1x-height 56)
(def network-link-2x-height 111)

(defn render-network-values
  [{:keys [network-values token-symbol token-decimals receiver?]}]
  [rn/view
   (doall
    (map-indexed (fn [index
                      {chain-id           :chain-id
                       network-value-type :type
                       total-amount       :total-amount}]
                   (let [amount-formatted (-> total-amount
                                              (common-utils/sanitized-token-amount-to-display
                                               token-decimals)
                                              (str " " token-symbol))]
                     [rn/view
                      {:key   (str (if receiver? "to" "from") "-" chain-id)
                       :style {:margin-top (if (pos? index) 11 7.5)}}
                      [quo/network-bridge
                       {:amount  amount-formatted
                        :network (network-utils/id->network chain-id)
                        :status  network-value-type}]]))
                 network-values))])

(defn render-network-links
  [{:keys [network-links sender-network-values]}]
  [rn/view {:style style/network-links-container}
   (map
    (fn [{:keys [from-chain-id to-chain-id position-diff]}]
      (let [position-diff-absolute (js/Math.abs position-diff)
            shape                  (case position-diff-absolute
                                     0 :linear
                                     1 :1x
                                     2 :2x)
            height                 (case position-diff-absolute
                                     0 network-link-linear-height
                                     1 network-link-1x-height
                                     2 network-link-2x-height)
            inverted?              (neg? position-diff)
            source                 (network-utils/id->network from-chain-id)
            destination            (network-utils/id->network to-chain-id)
            from-chain-id-index    (first (keep-indexed #(when (= from-chain-id (:chain-id %2)) %1)
                                                        sender-network-values))
            base-margin-top        (* (+ row-height space-between-rows)
                                      from-chain-id-index)
            margin-top             (if (zero? position-diff)
                                     (+ base-margin-top
                                        (- (/ row-height 2) (/ height 2)))
                                     (+ base-margin-top
                                        (- (/ row-height 2) height)
                                        (if inverted? height 0)))]
        [rn/view
         {:key   (str "from-" from-chain-id "-to-" to-chain-id)
          :style (style/network-link-container margin-top inverted?)}
         [rn/view {:style {:flex 1}}
          [quo/network-link
           {:shape       shape
            :source      source
            :destination destination}]]]))
    network-links)])

(defn view
  []
  (let [token-symbol            (rf/sub [:wallet/wallet-send-token-symbol])
        sender-network-values   (rf/sub [:wallet/wallet-send-sender-network-values])
        receiver-network-values (rf/sub [:wallet/wallet-send-receiver-network-values])
        network-links           (rf/sub [:wallet/wallet-send-network-links])
        token-decimals          (rf/sub [:wallet/send-display-token-decimals])
        show-routes?            (not-empty sender-network-values)]
    [rn/scroll-view {:content-container-style style/routes-container}
     (when show-routes?
       [rn/view {:style style/routes-header-container}
        [quo/section-label
         {:section         (i18n/label :t/from-label)
          :container-style style/section-label-left}]
        [quo/section-label
         {:section         (i18n/label :t/to-label)
          :container-style style/section-label-right}]])
     [rn/view {:style style/routes-inner-container}
      [render-network-values
       {:token-symbol   token-symbol
        :network-values sender-network-values
        :token-decimals token-decimals
        :receiver?      false}]
      [render-network-links
       {:network-links         network-links
        :sender-network-values sender-network-values}]
      [render-network-values
       {:token-symbol   token-symbol
        :network-values receiver-network-values
        :token-decimals token-decimals
        :receiver?      true}]]]))

