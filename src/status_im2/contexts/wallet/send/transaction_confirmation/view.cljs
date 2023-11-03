(ns status-im2.contexts.wallet.send.transaction-confirmation.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :tab/collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}])

(defn- network-names
  [balances-per-chain]
  (mapv (fn [chain-id-keyword]
          (let [chain-id-str (name chain-id-keyword)
                chain-id     (js/parseInt chain-id-str)]
            (case chain-id
              10    {:source (quo.resources/get-network :optimism)}
              42161 {:source (quo.resources/get-network :arbitrum)}
              5     {:source (quo.resources/get-network :ethereum)}
              1     {:source (quo.resources/get-network :ethereum)}
              :unknown))) ; Default case if the chain-id is not recognized
        (keys balances-per-chain)))

(defn- asset-component
  []
  (fn [token _ _ _]
    (let [on-press                #(js/alert "Not implemented yet")
          total-balance           (reduce +
                                          (map #(js/parseFloat (:balance %))
                                               (vals (:balancesPerChain token))))
          total-balance-formatted (.toFixed total-balance 2)
          currency                :usd
          currency-symbol         "$"
          price-fiat-currency     (get-in token [:marketValuesPerCurrency currency :price])
          balance-fiat            (* total-balance price-fiat-currency)
          balance-fiat-formatted  (.toFixed balance-fiat 2)
          networks-list           (network-names (:balancesPerChain token))]
      [quo/token-network
       {:token       (quo.resources/get-token (keyword (string/lower-case (:symbol token))))
        :label       (:name token)
        :token-value (str total-balance-formatted " " (:symbol token))
        :fiat-value  (str currency-symbol balance-fiat-formatted)
        :networks    networks-list
        :on-press    on-press}])))

(defn- asset-list
  [account-address search-text]
  (let [tokens          (rf/sub [:wallet/tokens])
        account-tokens  (get tokens (keyword account-address))
        sorted-tokens   (sort-by :name compare account-tokens)
        filtered-tokens (filter #(or (string/starts-with? (string/lower-case (:name %))
                                                          (string/lower-case search-text))
                                     (string/starts-with? (string/lower-case (:symbol %))
                                                          (string/lower-case search-text)))
                                sorted-tokens)]
    [rn/view {:style {:flex 1}}
     [rn/flat-list
      {:data                         filtered-tokens
       :content-container-style      {:padding-horizontal 8}
       :keyboard-should-persist-taps :handled
       :key-fn                       :id
       :on-scroll-to-index-failed    identity
       :render-fn                    asset-component}]]))

(defn- tab-view
  [account search-text selected-tab]
  (case selected-tab
    :tab/assets       [asset-list account search-text]
    :tab/collectibles [quo/empty-state
                       {:title           (i18n/label :t/no-collectibles)
                        :description     (i18n/label :t/no-collectibles-description)
                        :placeholder?    true
                        :container-style style/empty-container-style}]))

(defn- search-input
  [search-text]
  (let [on-change-text #(reset! search-text %)]
    (fn []
      [rn/view {:style style/search-input-container}
       [quo/input
        {:small?         true
         :placeholder    (i18n/label :t/search-assets)
         :icon-name      :i/search
         :value          @search-text
         :on-change-text on-change-text}]])))

(defn- f-view-internal
  [account-address]
  (let [margin-top      (safe-area/get-top)
        selected-tab    (reagent/atom (:id (first tabs-data)))
        search-text     (reagent/atom "")
        account-address (string/lower-case (or account-address
                                               (rf/sub [:get-screen-params :wallet-accounts])))
        on-close        #(rf/dispatch [:navigate-back-within-stack :wallet-select-asset])]
    (fn []
      [rn/scroll-view
       {:content-container-style      (style/container margin-top)
        :keyboard-should-persist-taps :handled
        :scroll-enabled               false}
       [quo/page-nav
        {:icon-name           :i/arrow-left
         :on-press            on-close
         :accessibility-label :top-bar
         :right-side          :account-switcher
         :account-switcher    {:customization-color :purple
                               :on-press            #(js/alert "Not implemented yet")
                               :state               :default
                               :emoji               "🍑"}}]
       [quo/text-combinations
        {:title                     (i18n/label :t/select-asset)
         :container-style           style/title-container
         :title-accessibility-label :title-label}]
       [quo/segmented-control
        {:size            32
         :blur?           false
         :symbol          false
         :default-active  :tab/assets
         :container-style {:margin-horizontal 20
                           :margin-vertical   8}
         :data            tabs-data
         :on-change       #(reset! selected-tab %)}]
       [search-input search-text]
       [tab-view account-address @search-text @selected-tab]])))

(defn- view-internal
  [{:keys [account-address]}]
  [:f> f-view-internal account-address])

(def view (quo.theme/with-theme view-internal))
