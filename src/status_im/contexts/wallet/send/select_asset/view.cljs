(ns status-im.contexts.wallet.send.select-asset.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.select-asset.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def tabs-data
  [{:id :tab/assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :tab/collectibles :label (i18n/label :t/collectibles) :accessibility-label :collectibles-tab}])

(defn- asset-component
  []
  (fn [token _ _ {:keys [currency currency-symbol]}]
    (let [on-press
          #(rf/dispatch [:wallet/send-select-token
                         {:token    token
                          :stack-id :wallet-select-asset}])
          token-units (utils/total-token-units-in-all-chains token)
          balance-crypto-formatted (utils/get-standard-crypto-format token token-units)
          fiat-value (utils/total-token-fiat-value currency token)
          balance-fiat-formatted
          (utils/get-standard-fiat-format balance-crypto-formatted currency-symbol fiat-value)]
      [quo/token-network
       {:token       (:symbol token)
        :label       (:name token)
        :token-value (str balance-crypto-formatted " " (:symbol token))
        :fiat-value  balance-fiat-formatted
        :networks    (:networks token)
        :on-press    on-press}])))

(defn- asset-list
  [search-text]
  (let [filtered-tokens (rf/sub [:wallet/tokens-filtered search-text])
        currency        (rf/sub [:profile/currency])
        currency-symbol (rf/sub [:profile/currency-symbol])]
    [rn/flat-list
     {:data                         filtered-tokens
      :render-data                  {:currency        currency
                                     :currency-symbol currency-symbol}
      :style                        {:flex 1}
      :content-container-style      {:padding-horizontal 8}
      :keyboard-should-persist-taps :handled
      :key-fn                       :id
      :on-scroll-to-index-failed    identity
      :render-fn                    asset-component}]))

(defn- tab-view
  [search-text selected-tab]
  (case selected-tab
    :tab/assets       [asset-list search-text]
    :tab/collectibles [quo/empty-state
                       {:title           (i18n/label :t/no-collectibles)
                        :description     (i18n/label :t/no-collectibles-description)
                        :placeholder?    true
                        :container-style (style/empty-container-style (safe-area/get-top))}]))

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
  []
  (let [selected-tab (reagent/atom (:id (first tabs-data)))
        search-text  (reagent/atom "")
        on-close     #(rf/dispatch [:navigate-back-within-stack
                                    :wallet-select-asset])]
    (fn []
      [rn/safe-area-view {:style style/container}
       [rn/scroll-view
        {:content-container-style      {:flex 1}
         :keyboard-should-persist-taps :handled
         :scroll-enabled               false}
        [account-switcher/view
         {:icon-name     :i/arrow-left
          :on-press      on-close
          :switcher-type :select-account}]
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
        [tab-view @search-text @selected-tab]]])))

(defn- view-internal
  []
  [:f> f-view-internal])

(def view (quo.theme/with-theme view-internal))
