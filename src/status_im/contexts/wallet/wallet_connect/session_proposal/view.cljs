(ns status-im.contexts.wallet.wallet-connect.session-proposal.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as quo.resources]
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

(defn- get-placeholder-networks
  []
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn- set-current-proposal-address
  [acc]
  (fn []
    (rf/dispatch [:wallet-connect/set-current-proposal-address (:address acc)])
    (rf/dispatch [:hide-bottom-sheet])))

(defn- accounts-list
  []
  (let [accounts         (rf/sub [:wallet/accounts-without-watched-accounts])
        selected-address (rf/sub [:wallet-connect/current-proposal-address])]
    [rn/view {:style style/account-switcher-list}
     (for [account accounts]
       ^{:key (-> account :address str)}
       [quo/account-item
        {:type          :default
         :state         (if (and selected-address
                                 (= (account :address)
                                    selected-address))
                          :selected
                          :default)
         :account-props account
         :on-press      (set-current-proposal-address account)}])]))

(defn- account-switcher-sheet
  []
  [:<>
   [rn/view {:style style/account-switcher-title}
    [quo/text
     {:size                :heading-2
      :weight              :semi-bold
      :accessibility-label "select-account-title"}
     (i18n/label :t/select-account)]]
   [accounts-list]])

(defn- show-account-switcher-bottom-sheet
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content account-switcher-sheet}]))

(defn- connection-category
  []
  (let [address                  (rf/sub [:wallet-connect/current-proposal-address])
        {:keys
         [name
          customization-color
          emoji]}                (rf/sub [:wallet-connect/account-details-by-address address])
        data-item-common-props   {:blur?       false
                                  :description :default
                                  :card?       false
                                  :label       :preview
                                  :status      :default
                                  :size        :default}
        account-data-item-props  (assoc data-item-common-props
                                        :right-content {:type :accounts
                                                        :size :size-32
                                                        :data [{:emoji emoji
                                                                :customization-color
                                                                customization-color}]}
                                        :on-press      show-account-switcher-bottom-sheet
                                        :title         (i18n/label :t/account-title)
                                        :subtitle      name
                                        :icon-right?   true
                                        :right-icon    :i/chevron-right
                                        :icon-color    colors/neutral-10)
        networks-data-item-props (assoc data-item-common-props
                                        :right-content {:type :network
                                                        :data
                                                        (get-placeholder-networks)}
                                        :title         (i18n/label :t/networks)
                                        ;; TODO. The quo component for data-item
                                        ;; does not support showing networks yet
                                        :subtitle      "Networks placeholder")]
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
