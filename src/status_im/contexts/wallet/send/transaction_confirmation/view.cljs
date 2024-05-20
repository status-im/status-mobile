(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.utils.utils :as utils]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- transaction-title
  [{:keys [token-display-name amount account to-address route to-network image-url transaction-type
           collectible?]}]
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
       {:token        (if collectible? "" token-display-name)
        :label        (str amount " " token-display-name)
        :type         (if collectible? :collectible :token)
        :image-source (if collectible? image-url :eth)}]]
     (if (= transaction-type :tx/bridge)
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
                  :customization-color network-color}]])]))
        route)
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
        [quo/summary-tag
         {:type  :address
          :label (utils/get-shortened-address to-address)}])]
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
  [{:keys [network-values token-display-name account-props theme label accessibility-label
           summary-type]}]
  (let [network-values
        (reduce-kv
         (fn [acc chain-id amount]
           (let [network-name (network-utils/id->network chain-id)]
             (assoc acc
                    (if (= network-name :mainnet) :ethereum network-name)
                    {:amount amount :token-symbol token-display-name})))
         {}
         network-values)]
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
      {:type          summary-type
       :networks?     true
       :values        network-values
       :account-props account-props}]]))

(defn- data-item
  [{:keys [title subtitle]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :description     :default
    :icon-right?     false
    :card?           false
    :label           :none
    :status          :default
    :size            :small
    :title           title
    :subtitle        subtitle}])

(defn- transaction-details
  [{:keys [estimated-time-min max-fees token-display-name amount to-address to-network route
           transaction-type
           theme]}]
  (let [currency-symbol           (rf/sub [:profile/currency-symbol])
        route-loaded?             (and route (seq route))
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])]
    [rn/view
     {:style style/details-title-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :summary-from-label}
      (i18n/label :t/details)]
     [rn/view
      {:style (style/details-container
               {:loading-suggested-routes? loading-suggested-routes?
                :route-loaded?             route-loaded?
                :theme                     theme})}
      (cond
        loading-suggested-routes?
        [rn/activity-indicator {:style {:align-self :center}}]
        route-loaded?
        [:<>
         [data-item
          {:title    (i18n/label :t/est-time)
           :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
         [data-item
          {:title    (i18n/label :t/max-fees)
           :subtitle (i18n/label :t/amount-with-currency-symbol
                                 {:amount (str max-fees)
                                  :symbol currency-symbol})}]
         [data-item
          {:title    (if (= transaction-type :tx/bridge)
                       (i18n/label :t/bridged-to
                                   {:network (:abbreviated-name to-network)})
                       (i18n/label :t/user-gets {:name (utils/get-shortened-address to-address)}))
           :subtitle (str amount " " token-display-name)}]]
        :else
        [quo/text {:style {:align-self :center}}
         (i18n/label :t/no-routes-found-confirmation)])]]))

(defn view
  [_]
  (let [on-close #(rf/dispatch [:navigate-back])]
    (fn []
      (let [theme                        (quo.theme/use-theme)
            send-transaction-data        (rf/sub [:wallet/wallet-send])
            {:keys [token-display-name collectible amount route
                    to-address bridge-to-chain-id
                    from-values-by-chain
                    to-values-by-chain]} send-transaction-data
            collectible?                 (some? collectible)
            image-url                    (when collectible
                                           (get-in collectible [:preview-url :uri]))
            transaction-type             (:tx-type send-transaction-data)
            estimated-time-min           (reduce + (map :estimated-time route))
            max-fees                     "-"
            account                      (rf/sub [:wallet/current-viewing-account])
            account-color                (:color account)
            bridge-to-network            (when bridge-to-chain-id
                                           (rf/sub [:wallet/network-details-by-chain-id
                                                    bridge-to-chain-id]))
            from-account-props           {:customization-color account-color
                                          :size                32
                                          :emoji               (:emoji account)
                                          :type                :default
                                          :name                (:name account)
                                          :address             (utils/get-shortened-address (:address
                                                                                             account))}
            user-props                   {:full-name to-address
                                          :address   (utils/get-shortened-address to-address)}]
        [rn/view {:style {:flex 1}}
         [floating-button-page/view
          {:footer-container-padding 0
           :header                   [quo/page-nav
                                      {:icon-name           :i/arrow-left
                                       :on-press            on-close
                                       :margin-top          (safe-area/get-top)
                                       :background          :blur
                                       :accessibility-label :top-bar}]
           :footer                   (when (and route (seq route))
                                       [standard-auth/slide-button
                                        {:size                :size-48
                                         :track-text          (if (= transaction-type :tx/bridge)
                                                                (i18n/label :t/slide-to-bridge)
                                                                (i18n/label :t/slide-to-send))
                                         :container-style     {:z-index 2}
                                         :customization-color account-color
                                         :on-auth-success     #(rf/dispatch
                                                                [:wallet/send-transaction
                                                                 (security/safe-unmask-data
                                                                  %)])
                                         :auth-button-label   (i18n/label :t/confirm)}])
           :gradient-cover?          true
           :customization-color      (:color account)}
          [rn/view
           [transaction-title
            {:token-display-name token-display-name
             :amount             amount
             :account            account
             :to-address         to-address
             :route              route
             :to-network         bridge-to-network
             :image-url          image-url
             :transaction-type   transaction-type
             :collectible?       collectible?}]
           [user-summary
            {:token-display-name  token-display-name
             :summary-type        :status-account
             :accessibility-label :summary-from-label
             :label               (i18n/label :t/from-capitalized)
             :network-values      from-values-by-chain
             :account-props       from-account-props
             :theme               theme}]
           [user-summary
            {:token-display-name  token-display-name
             :summary-type        (if (= transaction-type :tx/bridge)
                                    :status-account
                                    :account)
             :accessibility-label :summary-to-label
             :label               (i18n/label :t/to-capitalized)
             :account-props       (if (= transaction-type :tx/bridge)
                                    from-account-props
                                    user-props)
             :network-values      to-values-by-chain
             :theme               theme}]
           [transaction-details
            {:estimated-time-min estimated-time-min
             :max-fees           max-fees
             :token-display-name token-display-name
             :amount             amount
             :to-address         to-address
             :to-network         bridge-to-network
             :theme              theme
             :route              route
             :transaction-type   transaction-type}]]]]))))
