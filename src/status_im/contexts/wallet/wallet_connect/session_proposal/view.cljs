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

(defn- dapp-metadata
  []
  (let [proposer                 (rf/sub [:wallet-connect/session-proposer])
        {:keys [icons name url]} (:metadata proposer)]
    [:<>
     [rn/view {:style style/dapp-avatar}
      [quo/user-avatar
       {:profile-picture (first icons)
        :size            :big}]]
     [quo/page-top
      {:title       name
       :description :context-tag
       :context-tag {:type    :icon
                     :size    32
                     :icon    :i/link
                     :context url}}]]))

(defn- approval-note
  []
  (let [dapp-name (rf/sub [:wallet-connect/session-proposer-name])
        labels    [(i18n/label :t/check-your-account-balance-and-activity)
                   (i18n/label :t/request-txns-and-message-signing)]]
    [rn/view {:style style/approval-note-container}
     [quo/text {:style style/approval-note-title}
      (i18n/label :t/dapp-will-be-able-to {:dapp-name dapp-name})]
     (map-indexed
      (fn [idx label]
        ^{:key (str idx label)}
        [rn/view {:style style/approval-note-li}
         [quo/icon :i/bullet
          {:color colors/neutral-50}]
         [rn/view {:style style/approval-li-spacer}]
         [quo/text label]])
      labels)]))

(defn- accounts-data-item
  []
  ;; TODO. This account is currently hard coded in
  ;; `status-im.contexts.wallet.wallet-connect.events`. Should be selectable and changeable
  (let [accounts (rf/sub [:wallet/accounts-without-watched-accounts])
        name     (-> accounts first :name)]
    [quo/data-item
     {:container-style style/detail-item
      :blur?           false
      :description     :default
      :icon-right?     true
      :right-icon      :i/chevron-right
      :icon-color      colors/neutral-10
      :card?           false
      :label           :preview
      ;; TODO. The quo component for data item doesn't support showing accounts yet
      :status          :default
      :size            :small
      :title           (i18n/label :t/account-title)
      :subtitle        name}]))

(defn- networks-data-item
  []
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :description     :default
    :icon-right?     true
    :card?           true
    :label           :none
    :status          :default
    :size            :small
    :title           (i18n/label :t/networks)
    ;; TODO. The quo component for data-item does not support showing networks yet
    :subtitle        "Networks will show up here"}])

(defn- footer
  []
  (let [customization-color (rf/sub [:profile/customization-color])]
    [quo/bottom-actions
     {:actions          :two-actions
      :button-two-label (i18n/label :t/decline)
      :button-two-props {:type                :grey
                         :accessibility-label :wc-deny-connection
                         :on-press            #(do (rf/dispatch [:navigate-back])
                                                   (rf/dispatch
                                                    [:wallet-connect/reset-current-session]))}
      :button-one-label (i18n/label :t/connect)
      :button-one-props {:customization-color customization-color
                         :type                :primary
                         :accessibility-label :wc-connect
                         :on-press            #(rf/dispatch [:wallet-connect/approve-session])}}]))

(defn- header
  []
  [quo/page-nav
   {:type                :no-title
    :background          :blur
    :icon-name           :i/close
    :on-press            (rn/use-callback #(rf/dispatch [:navigate-back]))
    :accessibility-label :wc-session-proposal-top-bar}])

(defn view
  []
  [floating-button-page/view
   {:footer-container-padding 0
    :header                   [header]
    :footer                   [footer]}
   [rn/view
    [dapp-metadata]
    [accounts-data-item]
    [networks-data-item]
    [approval-note]]])
