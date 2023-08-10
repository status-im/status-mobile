(ns status-im2.contexts.wallet.home.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [quo2.core :as quo]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.home.view :as common.home]
    [status-im2.contexts.wallet.home.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn wallet-temporary-navigation
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   [quo/text {} "TEMPORARY NAVIGATION"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-accounts])}
    "Navigate to Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-create-account])}
    "Create Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-saved-addresses])}
    "Saved Addresses"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-collectibles])}
    "Collectibles"]])

;(defn view
;  []
;  (let [top (safe-area/get-top)]
;    [rn/view
;     {:style {:margin-top      top
;              :flex            1
;              :align-items     :center
;              :justify-content :center}}
;     [quo/button
;      {:icon-only?      true
;       :type            :grey
;       :on-press        (fn [] (rf/dispatch [:show-bottom-sheet {:content wallet-temporary-navigation}]))
;       :container-style {:position :absolute
;                         :top      20
;                         :right    20}} :i/options]
;     [quo/text {} "New Wallet Home"]]))

(def wallet-overview-state {:state             :default
                            :time-frame        :none
                            :metrics           :none
                            :balance           "€0.00"
                            :date              "20 Nov 2023"
                            :begin-date        "16 May"
                            :end-date          "25 May"
                            :currency-change   "€0.00"
                            :percentage-change "0.00%"})

(def account-cards [{:name                "Account 1"
                     :balance             "€0.00"
                     :percentage-value    "€0.00"
                     :customization-color :blue
                     :metrics?            true
                     :type                :empty
                     :emoji               "🍑"}
                    {:customization-color :blue
                     :on-press            #(js/alert "Button pressed")
                     :metrics?            true
                     :type                :add-account}])

(def tabs-data
  [{:id :assets :label (i18n/label :t/assets) :accessibility-label :assets-tab}
   {:id :collectibles :label (i18n/label :t/collectibles) :accessibility-label :cllectibles-tab}
   {:id :activity :label (i18n/label :t/activity) :accessibility-label :activity-tab}])

(def tokens [{:token               :snt
              :state               :default
              :status              :empty
              :customization-color :blue
              :metrics?            true
              :values              {:crypto-value      "0.00"
                                    :fiat-value        "€0.00"
                                    :percentage-change "0.00"
                                    :fiat-change       "€0.00"}}
             {:token               :eth
              :state               :default
              :status              :empty
              :customization-color :blue
              :metrics?            true
              :values              {:crypto-value      "0.00"
                                    :fiat-value        "€0.00"
                                    :percentage-change "0.00"
                                    :fiat-change       "€0.00"}}
             {:token               :dai
              :state               :default
              :status              :empty
              :customization-color :blue
              :metrics?            true
              :values              {:crypto-value      "0.00"
                                    :fiat-value        "€0.00"
                                    :percentage-change "0.00"
                                    :fiat-change       "€0.00"}}])

(defn view
  []
  (let [top          (safe-area/get-top)
        selected-tab (reagent/atom (:id (first tabs-data)))]
    [rn/view {:style {:top  top}}
     [common.home/top-nav {:type :grey}]
     [quo/wallet-overview wallet-overview-state]
     [rn/view {:style {:height           96
                       :background-color colors/danger
                       :justify-content  :center
                       :align-items      :center}}
      [quo/text "Illustration here"]]
     [rn/flat-list {:style      {:padding-horizontal 20
                                 :padding-top        32
                                 :padding-bottom     12}
                    :data       account-cards
                    :horizontal true
                    :separator  [rn/view {:style {:width 12}}]
                    :render-fn  quo/account-card}]
     [quo/tabs
      {:style          style/tabs
       :size           32
       :default-active @selected-tab
       :data           tabs-data
       ;:on-change      (fn [tab]
       ;                  (reset-banner-animation scroll-shared-value)
       ;                  (some-> scroll-ref
       ;                          deref
       ;                          reset-scroll)
       ;                  (on-tab-change tab))
       }]
     [rn/flat-list
      {:render-fn quo/token-value
       :data tokens
       :content-container-style {:padding-horizontal 8}}]
     ]))
