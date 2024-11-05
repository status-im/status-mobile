(ns status-im.contexts.wallet.send.transaction-confirmation.account-summary
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- account-summary-header
  [{:keys [label accessibility-label]}]
  (let [theme (quo.theme/use-theme)]
    [quo/text
     {:size                :paragraph-2
      :weight              :medium
      :style               (style/section-label theme)
      :accessibility-label accessibility-label}
     label]))

(defn- current-user-account-summary
  [{:keys [network-values]}]
  (let [{:keys [color emoji name address]} (rf/sub [:wallet/current-viewing-account])]
    [quo/summary-info
     {:type          :status-account
      :networks?     true
      :values        network-values
      :account-props {:customization-color color
                      :size                32
                      :emoji               emoji
                      :type                :default
                      :name                name
                      :address             (utils/get-shortened-address address)}}]))

(defn- recipient-account-summary
  [{:keys [network-values]}]
  (let [{:keys [label emoji customization-color recipient-type]} (rf/sub [:wallet/send-recipient])
        address                                                  (rf/sub [:wallet/send-to-address])
        shortened-address                                        (utils/get-shortened-address address)
        summary-type                                             (condp = recipient-type
                                                                   :saved-address :saved-account
                                                                   :account       :status-account
                                                                   :address       :account
                                                                   :account)]
    [quo/summary-info
     {:type          summary-type
      :networks?     true
      :values        network-values
      :account-props (cond-> {:customization-color customization-color
                              :size                32
                              :emoji               emoji
                              :type                :default
                              :name                (or label address)
                              :full-name           label
                              :address             shortened-address}

                       (= recipient-type :address)
                       (assoc :full-name  "0 x"
                              :monospace? true
                              :lowercase? true))}]))

(defn from-summary
  []
  (let [network-values (rf/sub [:wallet/network-values false])]
    [rn/view {:style style/summary-container}
     [account-summary-header
      {:label               (i18n/label :t/from-capitalized)
       :accessibility-label :summary-from-label}]
     [current-user-account-summary {:network-values network-values}]]))

(defn to-summary
  []
  (let [network-values (rf/sub [:wallet/network-values true])
        tx-type        (rf/sub [:wallet/send-tx-type])]
    [rn/view {:style style/summary-container}
     [account-summary-header
      {:label               (i18n/label :t/to-capitalized)
       :accessibility-label :summary-to-label}]
     (if (= tx-type :tx/bridge)
       [current-user-account-summary {:network-values network-values}]
       [recipient-account-summary {:network-values network-values}])]))
