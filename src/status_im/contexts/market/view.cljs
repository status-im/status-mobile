(ns status-im.contexts.market.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.refreshable-flat-list.view :as refreshable-flat-list]
    [status-im.contexts.market.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn swap-header
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/swap-header-container theme)}
     [quo/text
      {:weight :semi-bold
       :size   :heading-1
       :style  (style/market-header-text theme)}
      (i18n/label :t/market)]
     [quo/button
      {:size      32
       :icon-left :i/swap
       :on-press  #()}
      (i18n/label :t/swap)]]))

(defn sort-header
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/sort-header-container theme)}
     [quo/text
      {:size  :paragraph-2
       :style (style/sort-text theme)}
      (i18n/label :t/market-cap)]
     [quo/icon :i/arrow-down
      {:size            12
       :color           colors/neutral-50
       :container-style {:margin-left 4}}]]))

(defn token
  [token-data]
  [quo/market-token
   (assoc token-data
          :on-press
          (fn []
            (rf/dispatch [:open-modal :screen/market.token])))])

(defn view
  []
  (let [tokens-loading?                (rf/sub [:wallet/home-tokens-loading?])
        tokens                         (rf/sub [:market/tokens])
        [init-loaded? set-init-loaded] (rn/use-state false)]
    (rn/use-effect
     #(when (and (boolean? tokens-loading?) (not tokens-loading?) (not init-loaded?))
        (set-init-loaded true))
     [tokens-loading?])
    [rn/view {:style (style/home-container)}
     [common.top-nav/view]
     [refreshable-flat-list/view
      {:refresh-control         [rn/refresh-control
                                 {:refreshing (and tokens-loading? init-loaded?)
                                  :colors     [colors/neutral-40]
                                  :tint-color colors/neutral-40
                                  :on-refresh #()}]
       :header                  [rn/view
                                 [swap-header]
                                 [sort-header]]
       :content-container-style style/list-container
       :sticky-header-indices   [0]
       :data                    tokens
       :render-fn               token}]]))
