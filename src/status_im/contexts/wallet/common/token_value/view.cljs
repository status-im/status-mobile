(ns status-im.contexts.wallet.common.token-value.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
            [status-im.feature-flags :as ff]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- action-buy
  []
  {:icon                :i/buy
   :accessibility-label :buy
   :label               (i18n/label :t/buy)
   :on-press            #(rf/dispatch [:show-bottom-sheet
                                       {:content buy-token/view}])
   :right-icon          :i/external})

(defn- action-send
  [token-data]
  {:icon                :i/send
   :accessibility-label :send
   :label               (i18n/label :t/send)
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet/clean-send-data])
                          (rf/dispatch [:wallet/send-select-token
                                        {:token       token-data
                                         :start-flow? true}]))})

(defn- action-receive
  []
  {:icon                :i/receive
   :accessibility-label :receive
   :label               (i18n/label :t/receive)
   :on-press            #(rf/dispatch [:open-modal :screen/wallet.share-address {:status :receive}])})

(defn- action-bridge
  []
  {:icon                :i/bridge
   :accessibility-label :bridge
   :label               (i18n/label :t/bridge)
   :on-press            #(js/alert "to be implemented")})

(defn- action-manage-tokens
  [watch-only?]
  {:icon                :i/settings
   :accessibility-label :settings
   :label               (i18n/label :t/manage-tokens)
   :on-press            #(js/alert "to be implemented")
   :add-divider?        (not watch-only?)})

(defn- action-hide
  []
  {:icon                :i/hide
   :accessibility-label :hide
   :label               (i18n/label :t/hide)
   :on-press            #(js/alert "to be implemented")})

(defn token-value-drawer
  [token watch-only?]
  (let [token-data (first (rf/sub [:wallet/current-viewing-account-tokens-filtered (:token token)]))]
    [quo/action-drawer
     [(cond->> [(when (ff/enabled? ::ff/wallet.assets-modal-manage-tokens)
                  (action-manage-tokens watch-only?))
                (when (ff/enabled? ::ff/wallet.assets-modal-hide)
                  (action-hide))]
        (not watch-only?) (concat [(action-buy)
                                   (action-send token-data)
                                   (action-receive)
                                   (action-bridge)]))]]))

(defn view
  [item _ _ {:keys [watch-only?]}]
  [quo/token-value
   (cond-> item
     (or (not watch-only?) (ff/enabled? ::ff/wallet.long-press-watch-only-asset))
     (assoc :on-long-press
            #(rf/dispatch
              [:show-bottom-sheet
               {:content       (fn [] [token-value-drawer item watch-only?])
                :selected-item (fn [] [quo/token-value item])}])))])
