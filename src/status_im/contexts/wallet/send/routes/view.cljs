(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(def row-height 44)
(def space-between-rows 11)
(def network-link-linear-height 10)
(def network-link-1x-height 56)
(def network-link-2x-height 111)


(defn- open-preferences
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content
     (fn []
       (let [receiver-networks                        (rf/sub [:wallet/wallet-send-receiver-networks])
             receiver-preferred-networks              (rf/sub
                                                       [:wallet/wallet-send-receiver-preferred-networks])
             {token-symbol   :symbol
              token-networks :supported-networks}     (rf/sub [:wallet/wallet-send-token])
             token-chain-ids-set                      (set (mapv #(:chain-id %) token-networks))
             [selected-receiver-networks
              set-selected-receiver-networks]         (rn/use-state receiver-networks)
             receiver-preferred-networks-set          (set receiver-preferred-networks)
             receiver-selected-preferred-networks     (filter #(contains?
                                                                receiver-preferred-networks-set
                                                                %)
                                                              selected-receiver-networks)
             receiver-selected-non-preferred-networks (filter #(not (contains?
                                                                     receiver-preferred-networks-set
                                                                     %))
                                                              selected-receiver-networks)
             not-available-preferred-networks         (filter (fn [preferred-chain-id]
                                                                (not (contains? token-chain-ids-set
                                                                                preferred-chain-id)))
                                                              receiver-selected-preferred-networks)
             not-available-non-preferred-networks     (filter (fn [preferred-chain-id]
                                                                (not (contains?
                                                                      token-chain-ids-set
                                                                      preferred-chain-id)))
                                                              receiver-selected-non-preferred-networks)
             first-section-warning-label              (when (not-empty not-available-preferred-networks)
                                                        (i18n/label
                                                         :t/token-not-available-on-networks
                                                         {:token-symbol token-symbol
                                                          :networks
                                                          (network-utils/network-ids->formatted-text
                                                           not-available-preferred-networks)}))
             second-section-warning-label             (when (not-empty
                                                             not-available-non-preferred-networks)
                                                        (i18n/label
                                                         :t/token-not-available-on-networks
                                                         {:token-symbol token-symbol
                                                          :networks
                                                          (network-utils/network-ids->formatted-text
                                                           not-available-non-preferred-networks)}))]
         [network-preferences/view
          {:title                        (i18n/label :t/edit-receiver-networks)
           :first-section-label          (i18n/label :t/preferred-by-receiver)
           :second-section-label         (i18n/label :t/not-preferred-by-receiver)
           :selected-networks            (set (map network-utils/id->network
                                                   receiver-networks))
           :receiver-preferred-networks  receiver-preferred-networks
           :button-label                 (i18n/label :t/apply-changes)
           :first-section-warning-label  first-section-warning-label
           :second-section-warning-label second-section-warning-label
           :on-change                    #(set-selected-receiver-networks %)
           :on-save                      (fn [chain-ids]
                                           (rf/dispatch [:hide-bottom-sheet])
                                           (rf/dispatch [:wallet/update-receiver-networks
                                                         chain-ids]))}]))}]))

(defn render-network-values
  [{:keys [network-values token-symbol on-press on-long-press receiver? loading-routes?
           token-not-supported-in-receiver-networks?]}]
  [rn/view
   (doall
    (map-indexed (fn [index
                      {chain-id           :chain-id
                       network-value-type :type
                       total-amount       :total-amount}]
                   (let [status           (cond (and (= network-value-type :not-available)
                                                     loading-routes?
                                                     token-not-supported-in-receiver-networks?)
                                                :loading
                                                (= network-value-type :not-available)
                                                :disabled
                                                :else network-value-type)
                         amount-formatted (-> (rf/sub [:wallet/send-amount-fixed total-amount])
                                              (str " " token-symbol))]
                     [rn/view
                      {:key   (str (if receiver? "to" "from") "-" chain-id)
                       :style {:margin-top (if (pos? index) 11 7.5)}}
                      [quo/network-bridge
                       {:amount        (if (= network-value-type :not-available)
                                         (i18n/label :t/not-available)
                                         amount-formatted)
                        :network       (network-utils/id->network chain-id)
                        :status        status
                        :on-press      #(when (not loading-routes?)
                                          (cond
                                            (= network-value-type :edit)
                                            (open-preferences)
                                            on-press (on-press chain-id total-amount)))
                        :on-long-press #(when (and (not loading-routes?) (not= status :disabled))
                                          (cond
                                            (= network-value-type :add)
                                            (open-preferences)
                                            on-long-press (on-long-press chain-id total-amount)))}]]))
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

(defn disable-chain
  [chain-id disabled-from-chain-ids token-available-networks-for-suggested-routes]
  (let [disabled-chain-ids
        (if (contains? (set
                        disabled-from-chain-ids)
                       chain-id)
          (vec (remove #(= % chain-id)
                       disabled-from-chain-ids))
          (conj disabled-from-chain-ids
                chain-id))
        re-enabling-chain?
        (< (count disabled-chain-ids)
           (count disabled-from-chain-ids))]
    (if (or re-enabling-chain?
            (> (count token-available-networks-for-suggested-routes) 1))
      (rf/dispatch [:wallet/disable-from-networks
                    disabled-chain-ids])
      (rf/dispatch [:toasts/upsert
                    {:id   :disable-chain-error
                     :type :negative
                     :text (i18n/label :t/at-least-one-network-must-be-activated)}]))))

(defn view
  [{:keys [token theme valid-input? request-fetch-routes on-press-to-network current-screen-id
           token-not-supported-in-receiver-networks? input-value]}]
  (let [token-symbol (:symbol token)
        nav-current-screen-id (rf/sub [:view-id])
        active-screen? (= nav-current-screen-id current-screen-id)
        loading-routes? (rf/sub
                         [:wallet/wallet-send-loading-suggested-routes?])
        sender-network-values (rf/sub
                               [:wallet/wallet-send-sender-network-values])
        receiver-network-values (rf/sub
                                 [:wallet/wallet-send-receiver-network-values])
        network-links (rf/sub [:wallet/wallet-send-network-links])
        disabled-from-chain-ids (rf/sub
                                 [:wallet/wallet-send-disabled-from-chain-ids])
        {token-balances-per-chain :balances-per-chain} (rf/sub [:wallet/wallet-send-token])
        token-available-networks-for-suggested-routes
        (send-utils/token-available-networks-for-suggested-routes
         {:balances-per-chain token-balances-per-chain
          :disabled-chain-ids disabled-from-chain-ids})
        show-routes? (not-empty sender-network-values)]
    (rn/use-effect
     (fn []
       (when (and active-screen?
                  (> (count token-available-networks-for-suggested-routes) 0))
         (request-fetch-routes 2000)))
     [input-value valid-input?])
    (rn/use-effect
     #(when (and active-screen? (> (count token-available-networks-for-suggested-routes) 0))
        (request-fetch-routes 0))
     [disabled-from-chain-ids])
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
       {:token-symbol                              token-symbol
        :network-values                            sender-network-values
        :on-press                                  (fn [chain-id-to-disable]
                                                     (disable-chain
                                                      chain-id-to-disable
                                                      disabled-from-chain-ids
                                                      token-available-networks-for-suggested-routes))
        :receiver?                                 false
        :theme                                     theme
        :loading-routes?                           loading-routes?
        :token-not-supported-in-receiver-networks? false}]
      [render-network-links
       {:network-links         network-links
        :sender-network-values sender-network-values}]
      [render-network-values
       {:token-symbol                              token-symbol
        :network-values                            receiver-network-values
        :on-press                                  on-press-to-network
        :receiver?                                 true
        :loading-routes?                           loading-routes?
        :theme                                     theme
        :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
        :on-save                                   #(request-fetch-routes 0)}]]]))

