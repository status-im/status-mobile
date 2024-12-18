(ns status-im.contexts.wallet.common.activity-tab.activity-types.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.common.activity-tab.options.view :as activity-options]
            [utils.re-frame :as rf]))

;; Common helpers
(defn- open-options
  [transactions]
  (rf/dispatch
   [:show-bottom-sheet
    {:content #(activity-options/view (first transactions))}]))

(defn- common-activity-props
  [{:keys [tx-type relative-date status transactions]}]
  {:transaction      tx-type
   :timestamp        relative-date
   :status           status
   :blur?            false
   :on-press-options #(open-options transactions)})

(defn- network-tag
  [{:keys [network-name network-logo]}]
  {:type         :network
   :network-name network-name
   :network-logo network-logo})

(defn- collectible-tag
  [{:keys [nft-url nft-name amount token-id]}]
  {:type               :collectible
   :collectible        nft-url
   :collectible-name   (if (> amount 1)
                         (str amount " " nft-name)
                         nft-name)
   :collectible-number (when (not= token-id "0")
                         token-id)})

(defn- token-tag
  [{:keys [token amount]}]
  {:type   :token
   :token  token
   :amount amount})

;; Activity type views
(defn send-activity
  [{:keys [symbol-out amount-out token-id sender-tag recipient-tag network-name-out network-logo-out]
    :as   activity-data}]
  (let [base-props (common-activity-props activity-data)
        first-tag  (if token-id
                     (collectible-tag activity-data)
                     (token-tag {:token symbol-out :amount amount-out}))]
    [quo/wallet-activity
     (assoc base-props
            :first-tag         first-tag
            :second-tag-prefix :t/from
            :second-tag        sender-tag
            :third-tag-prefix  :t/to
            :third-tag         recipient-tag
            :fourth-tag-prefix :t/on
            :fourth-tag        (network-tag {:network-name network-name-out
                                             :network-logo network-logo-out}))]))

(defn bridge-activity
  [{:keys [symbol-out amount-out network-name-out network-logo-out network-name-in network-logo-in
           sender-tag]
    :as   activity-data}]
  [quo/wallet-activity
   (assoc (common-activity-props activity-data)
          :first-tag         (token-tag {:token symbol-out :amount amount-out})
          :second-tag-prefix :t/from
          :second-tag        (network-tag {:network-name network-name-out
                                           :network-logo network-logo-out})
          :third-tag-prefix  :t/to
          :third-tag         (network-tag {:network-name network-name-in
                                           :network-logo network-logo-in})
          :fourth-tag-prefix :t/in
          :fourth-tag        sender-tag)])

(defn swap-activity
  [{:keys [symbol-out amount-out symbol-in amount-in sender-tag network-name-out network-logo-out]
    :as   activity-data}]
  [quo/wallet-activity
   (assoc (common-activity-props activity-data)
          :first-tag         (token-tag {:token symbol-out :amount amount-out})
          :second-tag-prefix :t/to
          :second-tag        (token-tag {:token symbol-in :amount amount-in})
          :third-tag-prefix  :t/in
          :third-tag         sender-tag
          :fourth-tag-prefix :t/on
          :fourth-tag        (network-tag {:network-name network-name-out
                                           :network-logo network-logo-out}))])

(defn approval-activity
  [{:keys [symbol-out amount-out sender-tag spender-tag network-name-out network-logo-out]
    :as   activity-data}]
  [quo/wallet-activity
   (assoc (common-activity-props activity-data)
          :first-tag         (token-tag {:token symbol-out :amount amount-out})
          :second-tag-prefix :t/in
          :second-tag        sender-tag
          :third-tag-prefix  :t/for
          :third-tag         spender-tag
          :fourth-tag-prefix :t/on
          :fourth-tag        (network-tag {:network-name network-name-out
                                           :network-logo network-logo-out}))])
