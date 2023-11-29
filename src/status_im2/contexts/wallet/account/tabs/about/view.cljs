(ns status-im2.contexts.wallet.account.tabs.about.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.tabs.about.style :as style]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn about-options
  []
  [quo/action-drawer
   [[{:icon                :i/link
      :accessibility-label :view-on-eth
      :label               (i18n/label :t/view-on-eth)
      :right-icon          :i/external}
     {:icon                :i/link
      :accessibility-label :view-on-opt
      :label               (i18n/label :t/view-on-opt)
      :right-icon          :i/external}
     {:icon                :i/link
      :accessibility-label :view-on-arb
      :label               (i18n/label :t/view-on-arb)
      :right-icon          :i/external}
     {:icon                :i/copy
      :accessibility-label :copy-address
      :label               (i18n/label :t/copy-address)}
     {:icon                :i/qr-code
      :accessibility-label :show-address-qr
      :label               (i18n/label :t/show-address-qr)}
     {:icon                :i/share
      :accessibility-label :share-address
      :label               (i18n/label :t/share-address)}]]])

(defn view
  []
  (let [{:keys [type address]} (rf/sub [:wallet/current-viewing-account])
        networks               (rf/sub [:wallet/network-details])
        watch-only?            (= type :watch)]
    [rn/view {:style style/about-tab}
     [quo/data-item
      {:description     :default
       :icon-right?     true
       :right-icon      :i/options
       :card?           true
       :label           :none
       :status          :default
       :size            :default
       :title           (if watch-only? (i18n/label :t/watched-address) (i18n/label :t/address))
       :custom-subtitle (fn [] [quo/address-text
                                {:networks networks
                                 :address  address
                                 :format   :long}])
       :container-style {:margin-bottom 12}
       :on-press        #(rf/dispatch [:show-bottom-sheet {:content about-options}])}]
     (when (not watch-only?) [quo/account-origin temp/account-origin-state])]))
