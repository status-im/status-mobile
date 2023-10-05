(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.style :as style]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(defn description
  []
  [quo/text {:size :paragraph-2}
   (map (fn [network]
          ^{:key (str network)}
          [quo/text
           {:size   :paragraph-2
            :weight :medium
            :style  {:color (get colors/networks network)}}
           (str (subs (name network) 0 3) (when (= network :arbitrum) "1") ":")])
        temp/network-names)
   [quo/text
    {:size   :paragraph-2
     :weight :monospace}
    temp/address]])

(defn about-options
  []
  [quo/action-drawer
   [[{:icon                :i/link
      :accessibility-label :view-on-eth
      :label               (i18n/label :t/view-on-eth)}
     {:icon                :i/link
      :accessibility-label :view-on-opt
      :label               (i18n/label :t/view-on-opt)}
     {:icon                :i/link
      :accessibility-label :view-on-arb
      :label               (i18n/label :t/view-on-arb)}
     {:icon                :i/copy
      :accessibility-label :copy-address
      :label               (i18n/label :t/copy-address)}
     {:icon                :i/qr-code
      :accessibility-label :show-address-qr
      :label               (i18n/label :t/show-address-qr)}
     {:icon                :i/share
      :accessibility-label :share-address
      :label               (i18n/label :t/share-address)}]]])

(defn new-account
  []
  [rn/view])

(defn view
  [{:keys [selected-tab]}]
  (case selected-tab
    :assets       [rn/flat-list
                   {:render-fn               quo/token-value
                    :data                    temp/tokens
                    :content-container-style {:padding-horizontal 8}}]
    :collectibles [quo/empty-state
                   {:title           (i18n/label :t/no-collectibles)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    :activity     [quo/empty-state
                   {:title           (i18n/label :t/no-activity)
                    :description     (i18n/label :t/empty-tab-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    :permissions  [quo/empty-state
                   {:title           (i18n/label :t/no-permissions)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    :dapps        [quo/empty-state
                   {:title           (i18n/label :t/no-dapps)
                    :description     (i18n/label :t/no-collectibles-description)
                    :placeholder?    true
                    :container-style style/empty-container-style}]
    [rn/view {:style style/about-tab}
     [quo/data-item
      (merge temp/data-item-state
             {:custom-description description
              :on-press           #(rf/dispatch [:show-bottom-sheet {:content about-options}])})]
     [quo/account-origin temp/account-origin-state]]))
