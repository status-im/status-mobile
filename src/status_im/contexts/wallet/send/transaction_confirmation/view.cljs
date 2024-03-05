(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.utils.hex :as utils.hex]
    [legacy.status-im.utils.utils :as utils]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- transaction-title
  [{:keys [token-symbol amount account to-address route to-network image-url transaction-type
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
       (if (= transaction-type :bridge)
         (i18n/label :t/bridge)
         (i18n/label :t/send))]
      [quo/summary-tag
       {:token        (if collectible? "" token-symbol)
        :label        (str amount " " token-symbol)
        :type         (if collectible? :collectible :token)
        :image-source (if collectible? image-url :eth)}]]
     (if (= transaction-type :bridge)
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
      (if (= transaction-type :bridge)
        [quo/summary-tag
         {:type                :network
          :image-source        (:source to-network)
          :label               (string/capitalize (name (:network-name to-network)))
          :customization-color to-network-color}]
        [quo/summary-tag
         {:type  :address
          :label (utils/get-shortened-address to-address)}])]
     (when (= transaction-type :bridge)
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
  [{:keys [token-symbol token-decimals account-props route to? theme label accessibility-label
           summary-type]}]
  (letfn [(merge-sum [a b]
            (merge-with money/add a b))]
    (let [network-amounts
          (reduce (fn [acc path]
                    (let [network         (if to? (:to path) (:from path))
                          chain-id        (:chain-id network)
                          amount-hex      (if to? (:amount-in path) (:amount-out path))
                          amount-units    (native-module/hex-to-number
                                           (utils.hex/normalize-hex amount-hex))
                          amount          (money/with-precision
                                           (if (= token-symbol "ETH")
                                             (money/wei->ether amount-units)
                                             (money/token->unit amount-units
                                                                token-decimals))
                                           6)
                          network-details (rf/sub [:wallet/network-details-by-chain-id chain-id])
                          network-name    (:network-name network-details)
                          network-name    (if (= network-name :mainnet) :ethereum network-name)]
                      (merge-sum acc {network-name amount})))
                  {}
                  route)
          network-values
          (reduce-kv (fn [acc k v]
                       (assoc acc k {:amount v :token-symbol token-symbol}))
                     {}
                     network-amounts)]
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
         :account-props account-props}]])))

(defn- transaction-details
  [{:keys [estimated-time-min max-fees token-symbol amount to-address to-network route transaction-type
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
         [quo/data-item
          {:container-style style/detail-item
           :blur?           false
           :description     :default
           :icon-right?     false
           :card?           false
           :label           :none
           :status          :default
           :size            :small
           :title           (i18n/label :t/est-time)
           :subtitle        (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
         [quo/data-item
          {:container-style style/detail-item
           :blur?           false
           :description     :default
           :icon-right?     false
           :card?           false
           :label           :none
           :status          :default
           :size            :small
           :title           (i18n/label :t/max-fees)
           :subtitle        (i18n/label :t/amount-with-currency-symbol
                                        {:amount (str max-fees)
                                         :symbol currency-symbol})}]
         [quo/data-item
          {:container-style style/detail-item
           :blur?           false
           :description     :default
           :icon-right?     false
           :card?           false
           :label           :none
           :status          :default
           :size            :small
           :title           (if (= transaction-type :bridge)
                              (i18n/label :t/bridged-to
                                          {:network (:abbreviated-name to-network)})
                              (i18n/label :t/user-gets {:name (utils/get-shortened-address to-address)}))
           :subtitle        (str amount " " token-symbol)}]]
        :else
        [quo/text {:style {:align-self :center}}
         (i18n/label :t/no-routes-found-confirmation)])]]))

(defn- view-internal
  [_]
  (let [on-close (fn []
                   (rf/dispatch [:navigate-back-within-stack :wallet-transaction-confirmation])
                   (rf/dispatch [:wallet/clean-suggested-routes])
                   (rf/dispatch [:wallet/clean-selected-collectible]))]
    (fn [{:keys [theme]}]
      (let [send-transaction-data (rf/sub [:wallet/wallet-send])
            token                 (:token send-transaction-data)
            collectible           (:collectible send-transaction-data)
            collection-data       (:collection-data collectible)
            collectible-data      (:collectible-data collectible)
            collectible-id        (get-in collectible [:id :token-id])
            token-symbol          (if collectible
                                    (first (remove
                                            string/blank?
                                            [(:name collectible-data)
                                             (str (:name collection-data) " #" collectible-id)]))
                                    (:symbol token))
            token-decimals        (if collectible 0 (:decimals token))
            image-url             (when collectible (:image-url collectible-data))
            amount                (:amount send-transaction-data)
            route                 (:route send-transaction-data)
            transaction-type      (:type send-transaction-data)
            estimated-time-min    (reduce + (map :estimated-time route))
            max-fees              "-"
            to-address            (:to-address send-transaction-data)
            account               (rf/sub [:wallet/current-viewing-account])
            account-color         (:color account)
            bridge-to-chain-id    (:bridge-to-chain-id send-transaction-data)
            bridge-to-network     (when bridge-to-chain-id
                                    (rf/sub [:wallet/network-details-by-chain-id bridge-to-chain-id]))
            from-account-props    {:customization-color account-color
                                   :size                32
                                   :emoji               (:emoji account)
                                   :type                :default
                                   :name                (:name account)
                                   :address             (utils/get-shortened-address (:address
                                                                                      account))}
            user-props            {:full-name to-address
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
                                         :track-text          (if (= transaction-type :bridge)
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
            {:token-symbol     token-symbol
             :amount           amount
             :account          account
             :to-address       to-address
             :route            route
             :to-network       bridge-to-network
             :image-url        image-url
             :transaction-type transaction-type
             :collectible?     (some? collectible)}]
           [user-summary
            {:token-symbol        token-symbol
             :token-decimals      token-decimals
             :summary-type        :status-account
             :accessibility-label :summary-from-label
             :label               (i18n/label :t/from-capitalized)
             :route               route
             :to?                 false
             :account-props       from-account-props
             :theme               theme}]
           [user-summary
            {:token-symbol        token-symbol
             :token-decimals      token-decimals
             :summary-type        (if (= transaction-type :bridge)
                                    :status-account
                                    :account)
             :accessibility-label :summary-to-label
             :label               (i18n/label :t/to-capitalized)
             :account-props       (if (= transaction-type :bridge)
                                    from-account-props
                                    user-props)
             :route               route
             :to?                 true
             :theme               theme}]
           [transaction-details
            {:estimated-time-min estimated-time-min
             :max-fees           max-fees
             :token-symbol       token-symbol
             :amount             amount
             :to-address         to-address
             :to-network         bridge-to-network
             :theme              theme
             :route              route
             :transaction-type   transaction-type}]]]]))))

(def view (quo.theme/with-theme view-internal))
