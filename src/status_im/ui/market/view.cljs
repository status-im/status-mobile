(ns status-im.ui.market.view
  (:require
    [oops.core :as oops]
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.refreshable-flat-list.view :as refreshable-flat-list]
    [status-im.ui.market.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn swap-header
  []
  (let [theme             (quo.context/use-theme)
        operable-accounts (rf/sub [:wallet/operable-accounts])]
    [rn/view {:style (style/swap-header-container theme)}
     [quo/text
      {:weight :semi-bold
       :size   :heading-1
       :style  (style/market-header-text theme)}
      (i18n/label :t/market)]
     [quo/button
      {:size      32
       :icon-left :i/swap
       :on-press  #(rf/dispatch [:app.wallet/start-swap operable-accounts])}
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
  [token-id]
  (let [{:keys [price percentage-change market-cap short-name token-rank token-name image]}
        (rf/sub [:ui.market/token token-id])]
    [quo/market-token
     {:token-short-name  short-name
      :token-rank        token-rank
      :token-name        token-name
      :market-cap        market-cap
      :price             price
      :token-image       image
      :percentage-change percentage-change
      ;; TODO: fix if needed
      ;; :on-press          (fn []
      ;;                      (rf/dispatch [:open-modal :screen/market.token]))
     }]))

(defn ensure-tokens-updating
  [event]
  (when-let [first-token-name (oops/oget event "viewableItems.?0.?item")]
    (rf/dispatch [:app.market/ensure-tokens-updating first-token-name
                  (oops/oget event "viewableItems.length")])))

(defn view
  []
  (let [token-ids          (rf/sub [:ui.market/leaderboard])
        tokens-loading?    (rf/sub [:ui.market/leaderboard-loading?])
        reload-leaderboard (rn/use-callback
                            (fn [] (rf/dispatch [:app.market/reload-leaderboard])))
        load-more-tokens   (rn/use-callback
                            (fn [] (rf/dispatch [:app.market/load-more-leaderboard-tokens])))]
    (rn/use-mount
     #(rf/dispatch [:app.market/load-leaderboard]))
    (rn/use-unmount
     #(rf/dispatch [:app.market/close-leaderboard]))
    [rn/view {:style (style/home-container)}
     [common.top-nav/view]
     [refreshable-flat-list/view
      {:refresh-control           [rn/refresh-control
                                   {:refreshing tokens-loading?
                                    :colors     [colors/neutral-40]
                                    :tint-color colors/neutral-40
                                    :on-refresh reload-leaderboard}]
       :header                    [rn/view
                                   [swap-header]
                                   [sort-header]]
       :content-container-style   style/list-container
       :sticky-header-indices     [0]
       :data                      token-ids
       :render-fn                 token
       :on-end-reached            load-more-tokens
       :viewability-config        {:item-visible-percent-threshold 100
                                   :minimum-view-time              200}
       :on-viewable-items-changed ensure-tokens-updating}]]))
