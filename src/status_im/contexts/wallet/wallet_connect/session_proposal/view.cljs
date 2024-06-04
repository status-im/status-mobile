(ns status-im.contexts.wallet.wallet-connect.session-proposal.view
  (:require
   [quo.core :as quo]
   [quo.foundations.colors :as colors]
   [quo.theme]
   [react-native.core :as rn]
   [status-im.common.floating-button-page.view :as floating-button-page]
   [status-im.contexts.wallet.wallet-connect.session-proposal.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- dapp-metadata []
  (let [proposer  (rf/sub [:wallet-connect/session-proposer])
        metadata  (-> proposer :metadata)
        icon      (-> metadata :icons first)
        dapp-name (-> metadata :name)
        url       (-> metadata :url)]
    [:<>
     [rn/view {:style style/dapp-avatar}
      [quo/user-avatar
       {:profile-picture icon
        :size            :big}]]
     [quo/page-top {:title       dapp-name
                    :description :context-tag
                    :context-tag {:type    :icon
                                  :size    32
                                  :icon    :i/link
                                  :context url}}]]))

(defn- approval-note []
  (let [dapp-name (rf/sub [:wallet-connect/session-proposer-name])
        labels    [(i18n/label :t/check-your-account-balance-and-activity)
                   (i18n/label :t/request-txns-and-message-signing)]]
    [rn/view {:style style/approval-note-container}
     [rn/text {:style style/approval-note-title}
      (i18n/label :t/dapp-will-be-able-to {:dapp-name dapp-name})]
     (map-indexed
      (fn [idx label]
        ^{:key (str idx label)}
        [rn/view {:style style/approval-note-li}
         [quo/icon :i/bullet
          {:color colors/neutral-50}]
         [rn/view {:style style/approval-li-spacer}]
         [rn/text label]])
      labels)]))

(defn view []
  [floating-button-page/view
   {:footer-container-padding 0
    :header                   [quo/page-nav
                               {:type                :no-title
                                :background          :blur
                                :icon-name           :i/close
                                :on-press            (rn/use-callback #(rf/dispatch [:navigate-back]))
                                :accessibility-label :save-address-top-bar}]
    :footer                   [quo/button
                               {:accessibility-label :save-address-button
                                :type                :primary
                                ;; :container-style     style/save-address-button
                                }
                               (i18n/label :t/save-address)]}
   [rn/view
    {:style style/container}
    [dapp-metadata]
    [approval-note]]])
