(ns status-im.contexts.wallet.common.token-value.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
            [status-im.feature-flags :as ff]
            [utils.i18n :as i18n]
            [utils.money :as money]
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
  [{:keys [disabled?] :as params} entry-point]
  {:icon                :i/send
   :accessibility-label :send
   :label               (i18n/label :t/send)
   :disabled?           disabled?
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet/clean-send-data])
                          (rf/dispatch [:wallet/set-token-to-send params entry-point]))})

(defn- action-receive
  [selected-account?]
  {:icon                :i/receive
   :accessibility-label :receive
   :label               (i18n/label :t/receive)
   :on-press            (if selected-account?
                          #(rf/dispatch [:open-modal :screen/wallet.share-address {:status :receive}])
                          #(rf/dispatch [:open-modal :screen/share-shell {:initial-tab :wallet}]))})

(defn- action-bridge
  [{:keys [bridge-disabled?] :as params}]
  {:icon                :i/bridge
   :accessibility-label :bridge
   :label               (i18n/label :t/bridge)
   :disabled?           bridge-disabled?
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet/bridge-select-token params]))})

(defn- action-swap
  [{:keys [token token-symbol]}]
  {:icon                :i/swap
   :accessibility-label :swap
   :label               (i18n/label :t/swap)
   :on-press            (fn []
                          (rf/dispatch [:hide-bottom-sheet])
                          (rf/dispatch [:wallet.swap/start
                                        {:asset-to-pay     (or token {:symbol token-symbol})
                                         :open-new-screen? true}]))})

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
  (let [token-symbol     (:token token)
        token-data       (first (rf/sub [:wallet/current-viewing-account-tokens-filtered
                                         {:query token-symbol}]))
        selected-account (rf/sub [:wallet/current-viewing-account-address])
        token-owners     (rf/sub [:wallet/operable-addresses-with-token-symbol token-symbol])
        params           (cond-> {:start-flow? true
                                  :owners      token-owners}
                           selected-account
                           (assoc :token        token-data
                                  :stack-id     :screen/wallet.accounts
                                  :has-balance? (-> (get-in token [:values :fiat-unformatted-value])
                                                    (money/greater-than (money/bignumber "0"))))
                           (not selected-account)
                           (assoc :token-symbol token-symbol
                                  :stack-id     :wallet-stack))]
    [quo/action-drawer
     [(cond->> [(when (ff/enabled? ::ff/wallet.assets-modal-manage-tokens)
                  (action-manage-tokens watch-only?))
                (when (ff/enabled? ::ff/wallet.assets-modal-hide)
                  (action-hide))]
        (not watch-only?)
        (concat [(action-buy)
                 (when (seq token-owners)
                   (action-send params entry-point))
                 (action-receive selected-account)
                 (when (ff/enabled? ::ff/wallet.swap)
                   (action-swap params))
                 (when (seq token-owners)
                   (action-bridge (assoc params
                                         :bridge-disabled?
                                         (send-utils/bridge-disabled? token-symbol))))]))]]))

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
