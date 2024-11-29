(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- transaction-title
  [{:keys [token-display-name amount account route to-network image-url transaction-type
           collectible? recipient]}]
  (let [to-network-name  (:network-name to-network)
        to-network-color (if (= to-network-name :mainnet) :ethereum to-network-name)]
    [rn/view {:style style/content-container}
     [rn/view {:style {:flex-direction :row}}
      [quo/text
       {:size                :heading-1
        :weight              :semi-bold
        :style               style/title-container
        :accessibility-label :send-label}
       (if (= transaction-type :tx/bridge)
         (i18n/label :t/bridge)
         (i18n/label :t/send))]
      [quo/summary-tag
       (cond-> {:token (if collectible? "" token-display-name)
                :label (str amount " " token-display-name)
                :type  (if collectible? :collectible :token)}
         collectible? (assoc :image-source image-url))]]
     (if (= transaction-type :tx/bridge)
       (doall
        (map-indexed
         (fn [idx path]
           (let [from-network             (:from path)
                 chain-id                 (:chain-id from-network)
                 network                  (rf/sub [:wallet/network-details-by-chain-id
                                                   chain-id])
                 network-name             (:network-name network)
                 network-name-text        (name network-name)
                 network-name-capitalized (when (seq network-name-text)
                                            (string/capitalize network-name-text))
                 network-color            (if (= network-name :mainnet) :ethereum network-name)]
             (with-meta
               [rn/view
                {:style {:flex-direction :row
                         :margin-top     4}}
                (if (zero? idx)
                  [:<>
                   [quo/text
                    {:size                :heading-1
                     :weight              :semi-bold
                     :style               style/title-container
                     :accessibility-label :send-label}
                    (i18n/label :t/from)]
                   [quo/summary-tag
                    {:label               network-name-capitalized
                     :type                :network
                     :image-source        (:source network)
                     :customization-color network-color}]]
                  [:<>
                   [quo/text
                    {:size                :heading-1
                     :weight              :semi-bold
                     :style               style/title-container
                     :accessibility-label :send-label}
                    (str (i18n/label :t/and) " ")]
                   [quo/summary-tag
                    {:label               network-name-capitalized
                     :type                :network
                     :image-source        (:source network)
                     :customization-color network-color}]])]
               {:key (str "transaction-title" idx)})))
         route))
       [rn/view
        {:style {:flex-direction :row
                 :margin-top     4}}
        [quo/text
         {:size                :heading-1
          :weight              :semi-bold
          :style               style/title-container
          :accessibility-label :send-label}
         (i18n/label :t/from)]
        [quo/summary-tag
         {:label               (:name account)
          :type                :account
          :emoji               (:emoji account)
          :customization-color (:color account)}]])
     [rn/view
      {:style {:flex-direction :row
               :margin-top     4}}
      [quo/text
       {:size                :heading-1
        :weight              :semi-bold
        :style               style/title-container
        :accessibility-label :send-label}
       (i18n/label :t/to)]
      (if (= transaction-type :tx/bridge)
        [quo/summary-tag
         {:type                :network
          :image-source        (:source to-network)
          :label               (string/capitalize (name (:network-name to-network)))
          :customization-color to-network-color}]
        [quo/summary-tag (assoc recipient :type (:recipient-type recipient))])]
     (when (= transaction-type :tx/bridge)
       [rn/view
        {:style {:flex-direction :row
                 :margin-top     4}}
        [quo/text
         {:size                :heading-1
          :weight              :semi-bold
          :style               style/title-container
          :accessibility-label :send-label}
         (i18n/label :t/in)]
        [quo/summary-tag
         {:label               (:name account)
          :type                :account
          :emoji               (:emoji account)
          :customization-color (:color account)}]])]))

(defn- user-summary
  [{:keys [account-props theme label accessibility-label summary-type recipient bridge-tx? account-to?]}]
  (let [network-values    (rf/sub [:wallet/network-values account-to?])
        summary-info-type (case (:recipient-type recipient)
                            :saved-address :saved-account
                            :account       :status-account
                            summary-type)]
    [rn/view
     {:style {:padding-horizontal 20
              :padding-bottom     16}}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label accessibility-label}
      label]
     [quo/summary-info
      {:type          summary-info-type
       :networks?     true
       :values        (send-utils/network-values-for-ui network-values)
       :account-props (cond-> account-props
                        (and account-to? (not bridge-tx?))
                        (assoc
                         :size                32
                         :name                (:label recipient)
                         :full-name           (:label recipient)
                         :emoji               (:emoji recipient)
                         :customization-color (:customization-color recipient)))}]]))

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

(defn- transaction-details
  [{:keys [estimated-time-min max-fees to-network route
           transaction-type]}]
  (let [route-loaded?             (and route (seq route))
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        amount                    (rf/sub [:wallet/send-total-amount-formatted])]
    [rn/view
     {:style (style/details-container
              {:loading-suggested-routes? loading-suggested-routes?
               :route-loaded?             route-loaded?})}
     (cond
       loading-suggested-routes?
       [rn/activity-indicator {:style {:flex 1}}]
       route-loaded?
       [:<>
        (when (ff/enabled? ::ff/wallet.transaction-params)
          [quo/button
           {:icon-only?          true
            :type                :outline
            :size                32
            :inner-style         {:opacity 1}
            :accessibility-label :advanced-button
            :container-style     {:margin-right 8}
            :on-press            #(rf/dispatch
                                   [:show-bottom-sheet
                                    {:content transaction-settings/settings-sheet}])}
           :i/advanced])
        [data-item
         {:title    (i18n/label :t/est-time)
          :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
        [data-item
         {:title    (i18n/label :t/max-fees)
          :subtitle max-fees}]
        [data-item
         {:title    (if (= transaction-type :tx/bridge)
                      (i18n/label :t/bridged-to
                                  {:network (:abbreviated-name to-network)})
                      (i18n/label :t/recipient-gets))
          :subtitle amount}]]
       :else
       [quo/text {:style {:align-self :center}}
        (i18n/label :t/no-routes-found-confirmation)])]))

(defn view
  [_]
  (let [on-close #(rf/dispatch [:wallet/transaction-confirmation-navigate-back])]
    (fn []
      (let [theme                     (quo.theme/use-theme)
            send-transaction-data     (rf/sub [:wallet/wallet-send])
            {:keys [token-display-name collectible amount
                    route
                    to-address bridge-to-chain-id type
                    recipient]}       send-transaction-data
            collectible?              (some? collectible)
            image-url                 (when collectible
                                        (get-in collectible [:preview-url :uri]))
            transaction-type          (:tx-type send-transaction-data)
            estimated-time-min        (reduce + (map :estimated-time route))
            token-symbol              (or token-display-name
                                          (-> send-transaction-data :token :symbol))
            first-route               (first route)
            native-currency-symbol    (get-in first-route
                                              [:from :native-currency-symbol])
            fee-formatted             (rf/sub [:wallet/wallet-send-fee-fiat-formatted
                                               native-currency-symbol])
            account                   (rf/sub [:wallet/current-viewing-account])
            account-color             (:color account)
            bridge-to-network         (when bridge-to-chain-id
                                        (rf/sub [:wallet/network-details-by-chain-id
                                                 bridge-to-chain-id]))
            loading-suggested-routes? (rf/sub
                                       [:wallet/wallet-send-loading-suggested-routes?])
            transaction-for-signing   (rf/sub [:wallet/wallet-send-transaction-for-signing])
            from-account-props        {:customization-color account-color
                                       :size                32
                                       :emoji               (:emoji account)
                                       :type                :default
                                       :name                (:name account)
                                       :address             (utils/get-shortened-address
                                                             (:address
                                                              account))}
            user-props                {:full-name to-address
                                       :address   (utils/get-shortened-address
                                                   to-address)}]
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
                                      [transaction-details
                                       {:estimated-time-min estimated-time-min
                                        :max-fees           fee-formatted
                                        :to-network         bridge-to-network
                                        :theme              theme
                                        :route              route
                                        :transaction-type   transaction-type}]
                                      (when (and (not loading-suggested-routes?) route (seq route))
                                        [standard-auth/slide-button
                                         {:keycard-supported? true
                                          :size :size-48
                                          :track-text (if (= transaction-type :tx/bridge)
                                                        (i18n/label :t/slide-to-bridge)
                                                        (i18n/label :t/slide-to-send))
                                          :container-style {:z-index 2}
                                          :disabled? (not transaction-for-signing)
                                          :customization-color account-color
                                          :on-auth-success
                                          (fn [data]
                                            (rf/dispatch
                                             [:wallet/prepare-signatures-for-transactions
                                              :send data]))
                                          :auth-button-label (i18n/label :t/confirm)}])]
           :gradient-cover?          true
           :customization-color      (:color account)}
          [rn/view
           [transaction-title
            {:token-display-name token-symbol
             :amount             amount
             :account            account
             :type               type
             :recipient          recipient
             :route              route
             :to-network         bridge-to-network
             :image-url          image-url
             :transaction-type   transaction-type
             :collectible?       collectible?}]
           [user-summary
            {:summary-type        :status-account
             :accessibility-label :summary-from-label
             :label               (i18n/label :t/from-capitalized)
             :account-props       from-account-props
             :theme               theme}]
           [user-summary
            {:summary-type        (if (= transaction-type :tx/bridge)
                                    :status-account
                                    :account)
             :accessibility-label :summary-to-label
             :label               (i18n/label :t/to-capitalized)
             :account-props       (if (= transaction-type :tx/bridge)
                                    from-account-props
                                    user-props)
             :recipient           recipient
             :bridge-tx?          (= transaction-type :tx/bridge)
             :account-to?         true
             :theme               theme}]]]]))))
