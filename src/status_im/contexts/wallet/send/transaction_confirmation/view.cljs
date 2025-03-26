(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [clojure.string :as string]
    [quo.context :as quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [status-im.contexts.wallet.send.transaction-settings.view :as transaction-settings]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
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
    [rn/view {:style style/summary-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label accessibility-label}
      label]
     [quo/summary-info
      {:type             summary-info-type
       :networks-to-show (when bridge-tx?
                           (send-utils/network-values-for-ui network-values))
       :account-props    (cond-> account-props
                           (and account-to? (not bridge-tx?))
                           (assoc
                            :size                32
                            :name                (:label recipient)
                            :full-name           (:label recipient)
                            :emoji               (:emoji recipient)
                            :customization-color (:customization-color recipient)))}]]))

(defn- network-summary
  [{:keys [theme label accessibility-label]}]
  (let [network (rf/sub [:wallet/send-selected-network])]
    (when network
      [rn/view {:style style/summary-container}
       [quo/text
        {:size                :paragraph-2
         :weight              :medium
         :style               (style/section-label theme)
         :accessibility-label accessibility-label}
        label]
       [quo/summary-info
        {:type          :network
         :network-props network}]])))

(defn- data-item
  [{:keys [title subtitle subtitle-color loading?]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :status          (if loading? :loading :default)
    :size            :small
    :title           title
    :subtitle        subtitle
    :subtitle-color  subtitle-color}])

(defn- error-banner
  []
  (let [enough-assets?   (rf/sub [:wallet/send-enough-assets?])
        no-routes-found? (rf/sub [:wallet/no-routes-found?])]
    (cond
      no-routes-found?
      [quo/alert-banner
       {:text                 (i18n/label :t/no-routes-found-confirmation)
        :text-number-of-lines 2}]

      (not enough-assets?)
      [quo/alert-banner
       {:action?         true
        :text            (i18n/label :t/not-enough-assets-to-pay-gas-fees)
        :button-text     (i18n/label :t/add-eth)
        :on-button-press #(rf/dispatch [:show-bottom-sheet
                                        {:content buy-token/view}])}]

      :else nil)))

(defn- transaction-details
  [{:keys [max-fees to-network
           transaction-type route-loaded?]}]
  (let [theme                     (quo.context/use-theme)
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        estimated-time            (rf/sub [:wallet/send-estimated-time])
        enough-assets?            (rf/sub [:wallet/send-enough-assets?])
        no-routes-found?          (rf/sub [:wallet/no-routes-found?])
        loading?                  (or loading-suggested-routes? no-routes-found?)
        amount                    (rf/sub [:wallet/send-total-amount-formatted])]
    [rn/view
     {:style (style/details-container
              {:loading-suggested-routes? loading-suggested-routes?
               :route-loaded?             route-loaded?})}
     [quo/button
      {:icon-only?          true
       :type                :outline
       :size                32
       :inner-style         {:opacity 1}
       :accessibility-label :advanced-button
       :container-style     {:margin-right 8}
       :disabled?           no-routes-found?
       :on-press            #(rf/dispatch
                              [:show-bottom-sheet
                               {:content transaction-settings/settings-sheet}])}
      :i/advanced]
     [data-item
      {:title    (i18n/label :t/est-time)
       :loading? loading?
       :subtitle (i18n/label :t/time-in-sec
                             {:seconds estimated-time})}]
     [data-item
      {:title          (i18n/label :t/max-fees)
       :loading?       loading?
       :subtitle       max-fees
       :subtitle-color (when-not enough-assets?
                         (colors/theme-colors colors/danger-50
                                              colors/danger-60
                                              theme))}]
     [data-item
      {:title    (if (= transaction-type :tx/bridge)
                   (i18n/label :t/bridged-to
                               {:network (:abbreviated-name to-network)})
                   (i18n/label :t/recipient-gets))
       :loading? loading?
       :subtitle amount}]]))

(defn view
  [_]
  (let [theme                     (quo.context/use-theme)
        send-transaction-data     (rf/sub [:wallet/wallet-send])
        {:keys [token-display-name collectible amount
                route
                to-address bridge-to-chain-id type
                recipient]}       send-transaction-data
        collectible?              (some? collectible)
        image-url                 (when collectible
                                    (get-in collectible [:preview-url :uri]))
        transaction-type          (:tx-type send-transaction-data)
        token-symbol              (or token-display-name
                                      (-> send-transaction-data :token :symbol))
        fee-formatted             (rf/sub [:wallet/wallet-send-fee-fiat-formatted])
        account                   (rf/sub [:wallet/current-viewing-account])
        account-color             (:color account)
        bridge-to-network         (when bridge-to-chain-id
                                    (rf/sub [:wallet/network-details-by-chain-id
                                             bridge-to-chain-id]))
        loading-suggested-routes? (rf/sub
                                   [:wallet/wallet-send-loading-suggested-routes?])
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
                                               to-address)}
        auth-icon                 (rf/sub [:standard-auth/slider-icon])
        enough-assets?            (rf/sub [:wallet/send-enough-assets?])
        no-routes-found?          (rf/sub [:wallet/no-routes-found?])]
    (hot-reload/use-safe-unmount #(rf/dispatch [:wallet/clean-route-data-for-collectible-tx]))
    (rn/use-mount
     (fn []
       (when (ff/enabled? ::ff/wallet.transaction-params)
         (rf/dispatch [:wallet/init-tx-settings]))))
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/arrow-left
                                   :on-press            events-helper/navigate-back
                                   :margin-top          (safe-area/get-top)
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :blur-options             {:padding-horizontal 0}
       :footer                   [rn/view
                                  [error-banner]
                                  [rn/view {:style {:padding-horizontal 20}}
                                   [transaction-details
                                    {:max-fees         fee-formatted
                                     :to-network       bridge-to-network
                                     :transaction-type transaction-type
                                     :route-loaded?    (and route (seq route))}]
                                   [quo/slide-button
                                    {:size                :size-48
                                     :track-text          (if (= transaction-type :tx/bridge)
                                                            (i18n/label :t/slide-to-bridge)
                                                            (i18n/label :t/slide-to-send))
                                     :container-style     {:z-index 2}
                                     :disabled?           (or (not enough-assets?)
                                                              loading-suggested-routes?
                                                              no-routes-found?)
                                     :customization-color account-color
                                     :track-icon          auth-icon
                                     :on-complete         #(rf/dispatch
                                                            [:wallet.send/auth-slider-completed])}]]]
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
         :bridge-tx?          (= transaction-type :tx/bridge)
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
         :theme               theme}]
       (when-not (= transaction-type :tx/bridge)
         [network-summary
          {:label (i18n/label :t/on-capitalized)
           :theme theme}])]]]))
