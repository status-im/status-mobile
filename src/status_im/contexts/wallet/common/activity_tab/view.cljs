(ns status-im.contexts.wallet.common.activity-tab.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn send-and-receive-activity
  [{:keys [transaction relative-date status sender recipient token amount network-name
           network-logo token-id nft-url nft-name]}]
  (if token-id
    [quo/wallet-activity
     {:transaction       transaction
      :timestamp         relative-date
      :status            status
      :counter           1
      :first-tag         {:size               24
                          :type               :collectible
                          :collectible        nft-url
                          :collectible-name   (if (> amount 1)
                                                (str amount " " nft-name)
                                                nft-name)
                          :collectible-number (when (not= token-id "0")
                                                token-id)}
      :second-tag-prefix :t/from
      :second-tag        {:type :address :address sender}
      :third-tag-prefix  :t/to
      :third-tag         {:type :address :address recipient}
      :fourth-tag-prefix :t/via
      :fourth-tag        {:size         24
                          :type         :network
                          :network-name network-name
                          :network-logo network-logo}
      :blur?             false}]
    [quo/wallet-activity
     {:transaction       transaction
      :timestamp         relative-date
      :status            status
      :counter           1
      :first-tag         {:size   24
                          :type   :token
                          :token  token
                          :amount amount}
      :second-tag-prefix :t/from
      :second-tag        {:type :address :address sender}
      :third-tag-prefix  :t/to
      :third-tag         {:type :address :address recipient}
      :fourth-tag-prefix :t/via
      :fourth-tag        {:size         24
                          :type         :network
                          :network-name network-name
                          :network-logo network-logo}
      :blur?             false}]))

;; WIP to add the mint activity.
;(defn mint-activity
;  [{:keys [transaction relative-date status recipient network-name
;           network-logo nft-name nft-url token-id]}]
;  [quo/wallet-activity
;   {:transaction       transaction
;    :timestamp         relative-date
;    :status            status
;    :counter           1
;    :first-tag         {:size               24
;                        :type               :collectible
;                        :collectible        nft-url
;                        :collectible-name   nft-name
;                        :collectible-number token-id}
;    :second-tag-prefix :t/at
;    :second-tag        {:type :address :address recipient}
;    :third-tag-prefix  :t/to
;    :third-tag         {:type :address :address recipient}
;    :fourth-tag-prefix :t/via
;    :fourth-tag        {:size         24
;                        :type         :network
;                        :network-name network-name
;                        :network-logo network-logo}
;    :blur?             false}])

(defn- section-header
  [{:keys [title]}]
  [quo/divider-date title])

(defn activity-item
  [{:keys [transaction] :as activity}]
  (case transaction
    (:send :receive) [send-and-receive-activity activity]
    ;; WIP to add the mint activity.
    ;; :mint            [mint-activity activity]
    nil))

(defn- pressable-text
  [{:keys [on-press text]}]
  [rn/text
   {:style    {:text-decoration-line :underline}
    :on-press on-press}
   text])

(defn view
  []
  (let [theme                    (quo.theme/use-theme)
        address                  (rf/sub [:wallet/current-viewing-account-address])
        activity-list            (rf/sub [:wallet/activities-for-current-viewing-account])
        open-eth-chain-explorer  (rn/use-callback
                                  #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                 {:address address
                                                  :network constants/mainnet-network-name}])
                                  [address])
        open-oeth-chain-explorer (rn/use-callback
                                  #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                 {:address address
                                                  :network constants/optimism-network-name}])
                                  [address])
        open-arb-chain-explorer  (rn/use-callback
                                  #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                 {:address address
                                                  :network constants/arbitrum-network-name}])
                                  [address])]
    [:<>
     [quo/information-box
      {:type      :informative
       :icon      :i/info
       :closable? false
       :style     {:margin-horizontal 20 :margin-vertical 8}}
      [:<>
       (str (i18n/label :t/wallet-activity-beta-message) " ")
       [pressable-text
        {:on-press open-eth-chain-explorer
         :text     (i18n/label :t/etherscan)}]
       ", "
       [pressable-text
        {:on-press open-oeth-chain-explorer
         :text     (i18n/label :t/op-explorer)}]
       (str ", " (string/lower-case (i18n/label :t/or)) " ")
       [pressable-text
        {:on-press open-arb-chain-explorer
         :text     (i18n/label :t/arbiscan)}]
       "."]]
     (if (empty? activity-list)
       [empty-tab/view
        {:title       (i18n/label :t/no-activity)
         :description (i18n/label :t/empty-tab-description)
         :image       (resources/get-themed-image :no-activity theme)}]
       [rn/section-list
        {:sections                       activity-list
         :sticky-section-headers-enabled false
         :style                          {:flex               1
                                          :padding-horizontal 8}
         :content-container-style        {:padding-bottom shell.constants/floating-shell-button-height}
         :render-fn                      activity-item
         :render-section-header-fn       section-header}])]))
