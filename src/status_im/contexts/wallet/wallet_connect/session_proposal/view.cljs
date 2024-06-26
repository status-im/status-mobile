(ns status-im.contexts.wallet.wallet-connect.session-proposal.view
  (:require
    [clojure.string :as string]
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

(defn- format-network-name
  [network]
  (-> network :network-name name string/capitalize))

(defn- connection-category
  []
  (let [{:keys [name emoji customization-color]} (first (rf/sub
                                                         [:wallet/accounts-without-watched-accounts]))
        {:keys [session-networks
                all-networks-in-session?]}       (rf/sub
                                                  [:wallet-connect/session-proposal-network-details])
        network-names                            (->> session-networks
                                                      (map format-network-name)
                                                      (string/join ", "))
        network-images                           (mapv :source session-networks)
        data-item-common-props                   {:blur?       false
                                                  :description :default
                                                  :card?       false
                                                  :label       :preview
                                                  :status      :default
                                                  :size        :large}
        account-data-item-props                  (assoc data-item-common-props
                                                        :right-content {:type :accounts
                                                                        :size :size-32
                                                                        :data [{:emoji emoji
                                                                                :customization-color
                                                                                customization-color}]}
                                                        :on-press      #(js/alert "Not yet implemented")
                                                        :title         (i18n/label :t/account-title)
                                                        :subtitle      name
                                                        :icon-right?   true
                                                        :right-icon    :i/chevron-right
                                                        :icon-color    colors/neutral-10)
        networks-data-item-props                 (assoc data-item-common-props
                                                        :right-content {:type :network
                                                                        :data network-images}
                                                        :title         (i18n/label :t/networks)
                                                        :subtitle      (if all-networks-in-session?
                                                                         (i18n/label :t/all-networks)
                                                                         network-names))]
    [quo/category
     {:blur?     false
      :list-type :data-item
      :data      [account-data-item-props
                  networks-data-item-props]}]))

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
    [connection-category]
    [approval-note]]])
