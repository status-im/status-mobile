(ns status-im.contexts.wallet.common.token-value.view
  (:require [quo.core :as quo]
            [status-im.common.not-implemented :as not-implemented]
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
  [send-params entry-point]
  {:icon                :i/send
   :accessibility-label :send
   :label               (i18n/label :t/send)
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet/clean-send-data])
                          (rf/dispatch [:wallet/set-token-to-send send-params entry-point]))})

(defn- action-receive
  [selected-account?]
  {:icon                :i/receive
   :accessibility-label :receive
   :label               (i18n/label :t/receive)
   :on-press            (if selected-account?
                          #(rf/dispatch [:open-modal :screen/wallet.share-address {:status :receive}])
                          #(rf/dispatch [:open-modal :screen/share-shell {:initial-tab :wallet}]))})

(defn- action-bridge
  [bridge-params]
  {:icon                :i/bridge
   :accessibility-label :bridge
   :label               (i18n/label :t/bridge)
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet/bridge-select-token bridge-params]))})

(defn- action-swap
  []
  {:icon                :i/swap
   :accessibility-label :swap
   :label               (i18n/label :t/swap)
   :on-press            #(not-implemented/alert)})

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
  [token watch-only? entry-point]
  (let [token-symbol          (:token token)
        token-data            (first (rf/sub [:wallet/current-viewing-account-tokens-filtered
                                              token-symbol]))
        selected-account?     (rf/sub [:wallet/current-viewing-account-address])
        token-owners          (rf/sub [:wallet/operable-addresses-with-token-symbol token-symbol])
        send-or-bridge-params (if selected-account?
                                {:token       token-data
                                 :stack-id    :screen/wallet.accounts
                                 :start-flow? true
                                 :owners      token-owners}
                                {:token-symbol token-symbol
                                 :stack-id     :wallet-stack
                                 :start-flow?  true
                                 :owners       token-owners})]
    [quo/action-drawer
     [(cond->> [(when (ff/enabled? ::ff/wallet.assets-modal-manage-tokens)
                  (action-manage-tokens watch-only?))
                (when (ff/enabled? ::ff/wallet.assets-modal-hide)
                  (action-hide))]
        (not watch-only?) (concat [(action-buy)
                                   (when (seq token-owners)
                                     (action-send send-or-bridge-params entry-point))
                                   (action-receive selected-account?)
                                   (when (ff/enabled? ::ff/wallet.swap) (action-swap))
                                   (when (seq (seq token-owners))
                                     (action-bridge send-or-bridge-params))]))]]))

(defn view
  [item _ _ {:keys [watch-only? entry-point]}]
  [quo/token-value
   (cond-> item
     (or (not watch-only?) (ff/enabled? ::ff/wallet.long-press-watch-only-asset))
     (assoc :on-long-press
            #(rf/dispatch
              [:show-bottom-sheet
               {:content       (fn [] [token-value-drawer item watch-only? entry-point])
                :selected-item (fn [] [quo/token-value item])}])))])
