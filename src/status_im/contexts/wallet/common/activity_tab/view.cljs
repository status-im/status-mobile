(ns status-im.contexts.wallet.common.activity-tab.view
  (:require
    [legacy.status-im.utils.hex :as utils.hex]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.datetime :as datetime]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(def asset-snt
  {:size   24
   :type   :token
   :token  "SNT"
   :amount 1500})

(def piggy-bank
  {:size         24
   :type         :account
   :account-name "Piggy bank"
   :emoji        "🐷"})

(def aretha-gosling
  {:size            24
   :type            :default
   :full-name       "Aretha Gosling"
   :profile-picture (resources/mock-images :user-picture-female2)})

(def mainnet
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :ethereum)
   :network-name "Mainnet"})

(defn activity-item
  [{:keys [activity-type activity-status timestamp symbol-out symbol-in token-in token-out amount-in
           amount-out sender recipient]}]
  (let [chain-id         (or (:chain-id token-in) (:chain-id token-out))
        amount-in-units  (native-module/hex-to-number
                          (utils.hex/normalize-hex amount-in))
        amount-in-value  (money/with-precision
                          (money/wei->ether amount-in-units)
                          6)
        amount-out-units (native-module/hex-to-number
                          (utils.hex/normalize-hex amount-out))
        amount-out-value (money/with-precision
                          (money/wei->ether amount-out-units)
                          6)
       ]
    [:<>
     ;[quo/divider-date (:date item)]
     [quo/wallet-activity
      {:transaction       (constants/wallet-activity-id->name activity-type)
       :timestamp         (datetime/timestamp->relative (* timestamp 1000))
       :status            (constants/wallet-activity-status->name activity-status)
       :counter           1
       :first-tag         {:size   24
                           :type   :token
                           :token  (or symbol-out symbol-in)
                           :amount (if (= activity-type constants/wallet-activity-type-receive)
                                     amount-in-value
                                     amount-out-value)}
       :second-tag-prefix (constants/second-tag-prefix activity-type)
       :second-tag        {:type    :address
                           :address (if (= activity-type constants/wallet-activity-type-receive)
                                      recipient
                                      sender)}
       :third-tag-prefix  (constants/third-tag-prefix activity-type)
       :third-tag         {:type    :address
                           :address (if (= activity-type constants/wallet-activity-type-receive)
                                      sender
                                      recipient)}
       :fourth-tag-prefix (constants/fourth-tag-prefix activity-type)
       :fourth-tag        {:size         24
                           :type         :network
                           :network-logo (quo.resources/get-network (chain/chain-id->chain-keyword
                                                                     chain-id))
                           :network-name (chain/chain-id->chain-name chain-id)}
       :blur?             false}]]))

(defn view
  []
  (let [theme         (quo.theme/use-theme)
        activity-list (rf/sub [:wallet/all-activities])]
    (if (empty? activity-list)
      [empty-tab/view
       {:title       (i18n/label :t/no-activity)
        :description (i18n/label :t/empty-tab-description)
        :image       (resources/get-themed-image :no-activity theme)}]
      [rn/flat-list
       {:data                    activity-list
        :style                   {:flex 1}
        :render-fn               activity-item
        :content-container-style {:padding-bottom jump-to.constants/floating-shell-button-height}}])))
