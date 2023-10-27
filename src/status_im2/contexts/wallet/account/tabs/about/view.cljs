(ns status-im2.contexts.wallet.account.tabs.about.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.tabs.about.style :as style]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn description
  [{:keys [address theme]}]
  (let [networks-list (rf/sub [:wallet/network-details])]
    [quo/text {:size :paragraph-2}
     (map (fn [{:keys [chain-id short-name network-name]}]
            ^{:key (str chain-id short-name)}
            [quo/text
             {:size   :paragraph-2
              :weight :medium
              :style  {:color (colors/resolve-color network-name theme)}}
             (str short-name ":")])
          networks-list)
     [quo/text
      {:size   :paragraph-2
       :weight :monospace}
      address]]))

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

(defn- view-internal
  [{:keys [theme]}]
  [rn/view {:style style/about-tab}
   [quo/data-item
    (merge temp/data-item-state
           {:custom-subtitle (fn [] [description
                                     {:theme   theme
                                      :address temp/address}])
            :container-style {:margin-bottom 12}
            :on-press        #(rf/dispatch [:show-bottom-sheet {:content about-options}])})]
   [quo/account-origin temp/account-origin-state]])

(def view (quo.theme/with-theme view-internal))
