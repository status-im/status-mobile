(ns status-im2.contexts.wallet.account.tabs.view
  (:require
<<<<<<< HEAD
<<<<<<< HEAD
    [quo.theme]
=======
    [clojure.string :as string]
>>>>>>> 500f66b63 (updates)
=======
>>>>>>> df7a5e7cd (review)
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.wallet.account.tabs.about.view :as about]
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im2.contexts.wallet.common.activity-tab.view :as activity]
    [status-im2.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.token-value.view :as token-value]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

<<<<<<< HEAD
(defn prepare-token
  [{:keys [symbol marketValuesPerCurrency] :as item} color]
  (let [fiat-value                      (utils/total-per-token item)
        marketValues                    (:usd marketValuesPerCurrency)
        {:keys [price changePct24hour]} marketValues
        fiat-change                     (* fiat-value (/ changePct24hour (+ 100 changePct24hour)))]
    {:token               (keyword (string/lower-case symbol))
     :state               :default
     :status              (if (pos? changePct24hour)
                            :positive
                            (if (neg? changePct24hour) :negative :empty))
     :customization-color color
     :values              {:crypto-value      (.toFixed (* fiat-value price) 2)
                           :fiat-value        (utils/prettify-balance fiat-value)
                           :percentage-change (.toFixed changePct24hour 2)
                           :fiat-change       (utils/prettify-balance fiat-change)}}))

(defn parse-tokens
  [tokens color]
  (vec (map #(prepare-token % color) tokens)))

<<<<<<< HEAD
(defn- view-internal
  [{:keys [selected-tab theme]}]
  (case selected-tab
=======
=======
>>>>>>> df7a5e7cd (review)
(defn view
  [{:keys [selected-tab]}]
  (let [tokens (rf/sub [:wallet/account-token-values])]
    (case selected-tab
<<<<<<< HEAD
>>>>>>> 500f66b63 (updates)
    :assets       [rn/flat-list
                   {:render-fn               token-value/view
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [collectibles/view]
    :activity     [activity/view]
    :permissions  [empty-tab/view
                   {:title       (i18n/label :t/no-permissions)
                    :description (i18n/label :t/no-collectibles-description)
                    :image       (resources/get-image
                                  (quo.theme/theme-value :no-permissions-light
                                                         :no-permissions-dark
                                                         theme))}]
    :dapps        [dapps/view]
<<<<<<< HEAD
    [about/view]))

(def view (quo.theme/with-theme view-internal))
=======
    [about/view])))
>>>>>>> 500f66b63 (updates)
=======
      :assets       [rn/flat-list
                     {:render-fn               token-value/view
                      :data                    tokens
                      :content-container-style {:padding-horizontal 8}}]
      :collectibles [collectibles/view]
      :activity     [activity/view]
      :permissions  [empty-tab/view
                     {:title        (i18n/label :t/no-permissions)
                      :description  (i18n/label :t/no-collectibles-description)
                      :placeholder? true}]
      :dapps        [dapps/view]
      [about/view])))
>>>>>>> 882153523 (wallet: account real data)
