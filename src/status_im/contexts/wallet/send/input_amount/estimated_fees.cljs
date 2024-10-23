(ns status-im.contexts.wallet.send.input-amount.estimated-fees
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- bonder-fee-info-sheet
  []
  [:<>
   [quo/drawer-top
    {:title (i18n/label :t/understanding-bonder-fees-title)}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:weight :regular
      :size   :paragraph-2}
     (i18n/label :t/understanding-bonder-fees-description)]]])

(defn- show-bonder-fee-info
  []
  (rf/dispatch
   [:show-bottom-sheet {:content bonder-fee-info-sheet}]))

(defn- received-amount
  [{:keys [loading-routes?]}]
  (let [amount              (rf/sub [:wallet/send-total-amount-formatted])
        tx-type             (rf/sub [:wallet/wallet-send-tx-type])
        {:keys [full-name]} (rf/sub [:wallet/bridge-to-network-details])]
    [quo/data-item
     (cond-> {:container-style style/amount-data-item
              :status          (if loading-routes? :loading :default)
              :size            :small
              :title           (i18n/label :t/recipient-gets)
              :subtitle        amount}

       (= tx-type :tx/bridge)
       (assoc
        :title-icon :i/info
        :title      (i18n/label :t/bridged-to
                                {:network full-name})
        :on-press   show-bonder-fee-info))]))

(defn view
  [{:keys [loading-routes? fees]}]
  [rn/view {:style style/estimated-fees-container}
   (when (ff/enabled? ::ff/wallet.advanced-sending)
     [rn/view {:style style/estimated-fees-content-container}
      [quo/button
       {:icon-only?          true
        :type                :outline
        :size                32
        :inner-style         {:opacity 1}
        :accessibility-label :advanced-button
        :disabled?           loading-routes?
        :on-press            not-implemented/alert}
       :i/advanced]])
   [quo/data-item
    {:container-style style/fees-data-item
     :status          (if loading-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/fees)
     :subtitle        fees}]
   [received-amount {:loading-routes? loading-routes?}]])
