(ns status-im.contexts.wallet.sheets.select-asset.asset-list.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- my-token-component
  [{token-symbol     :symbol
    token-name       :name
    total-balance    :total-balance
    bridge-disabled? :bridge-disabled?
    :as              token}
   {:keys [network currency currency-symbol on-token-press disable-token-fn preselected-token-symbol]}]
  (let [fiat-value       (utils/calculate-token-fiat-value
                          {:currency currency
                           :balance  total-balance
                           :token    token})
        crypto-formatted (utils/get-standard-crypto-format token total-balance)
        fiat-formatted   (utils/get-standard-fiat-format crypto-formatted currency-symbol fiat-value)]
    [rn/view {:style {:padding-horizontal 8}}
     [quo/token-network
      {:token       token-symbol
       :label       token-name
       :token-value (str crypto-formatted " " token-symbol)
       :fiat-value  fiat-formatted
       :networks    [network]
       :on-press    #(on-token-press token)
       :state       (cond
                      (or bridge-disabled?
                          (when disable-token-fn
                            (disable-token-fn constants/swap-tokens-my token)))
                      :disabled

                      (= preselected-token-symbol token-symbol)
                      :selected)}]]))

(defn- popular-token-component
  [{token-symbol :symbol
    token-name   :name
    :as          token}
   {:keys [on-token-press disable-token-fn]}]
  [rn/view {:style {:padding-horizontal 8}}
   [quo/token-info
    {:token    token-symbol
     :label    token-name
     :state    (when (and disable-token-fn (disable-token-fn constants/swap-tokens-popular token))
                 :disabled)
     :on-press #(on-token-press token)}]])

(defn- list-component
  [{:keys [type] :as token} _ _ render-data]
  (if (= type constants/swap-tokens-popular)
    [popular-token-component token render-data]
    [my-token-component token render-data]))

(defn section-header
  [{:keys [title]}]
  [quo/divider-label
   {:container-style {:padding-horizontal 20
                      :margin-top         12
                      :margin-bottom      8}
    :tight?          false}
   title])

(defn view
  [{:keys [search-text on-token-press preselected-token-symbol network chain-ids hide-token-fn
           disable-token-fn]}]
  (let [my-tokens       (rf/sub [:wallet/current-viewing-account-tokens-filtered
                                 {:query         search-text
                                  :chain-ids     chain-ids
                                  :hide-token-fn hide-token-fn}])
        popular-tokens  (rf/sub [:wallet/tokens-filtered
                                 {:query         search-text
                                  :hide-token-fn hide-token-fn}])
        currency        (rf/sub [:profile/currency])
        currency-symbol (rf/sub [:profile/currency-symbol])
        sectioned-data  (cond-> []
                          (pos? (count my-tokens))
                          (conj {:title (i18n/label :t/your-assets-on-network
                                                    {:network (:full-name network)})
                                 :data  (map #(assoc % :type constants/swap-tokens-my)
                                             my-tokens)})

                          (pos? (count popular-tokens))
                          (conj {:title (i18n/label :t/popular-assets-on-network
                                                    {:network (:full-name network)})
                                 :data  (map #(assoc % :type constants/swap-tokens-popular)
                                             popular-tokens)}))
        render-data     {:currency                 currency
                         :currency-symbol          currency-symbol
                         :network                  network
                         :on-token-press           on-token-press
                         :preselected-token-symbol preselected-token-symbol
                         :disable-token-fn         disable-token-fn}]
    [gesture/section-list
     {:data                           sectioned-data
      :sections                       sectioned-data
      :render-data                    render-data
      :render-fn                      list-component
      :sticky-section-headers-enabled false
      :render-section-header-fn       section-header
      :style                          {:flex 1}
      :content-container-style        {:padding-bottom 20}
      :keyboard-should-persist-taps   :handled
      :key-fn                         (fn [{:keys [type title] :as token}]
                                        (str (:symbol token) "-" type "-" title))
      :on-scroll-to-index-failed      identity}]))
