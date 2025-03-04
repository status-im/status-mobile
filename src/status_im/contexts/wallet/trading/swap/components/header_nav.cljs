(ns status-im.contexts.wallet.trading.swap.components.header-nav
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.profile.utils :as profile.utils]
            [status-im.contexts.wallet.sheets.select-account.view :as select-account]
            [status-im.contexts.wallet.trading.swap.components.network-selector :as network-selector]
            [utils.re-frame :as rf]))

(defn profile-button
  []
  (let [{:keys [public-key] :as profile} (rf/sub [:profile/profile-with-image])
        online?                          (rf/sub [:visibility-status-updates/online?
                                                  public-key])
        customization-color              (rf/sub [:profile/customization-color])]
    [rn/pressable
     {:on-press            #(rf/dispatch [:open-modal :settings])
      :accessibility-label :open-profile}
     [quo/user-avatar
      {:status-indicator?   true
       :ring?               true
       :static?             true
       :customization-color customization-color
       :size                :small
       :online?             online?
       :full-name           (profile.utils/displayed-name profile)
       :profile-picture     (profile.utils/photo profile)}]]))

(defn- show-account-selector
  [address token-symbol network]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn []
                            [select-account/view
                             {:show-account-balances true
                              :address               address
                              :on-press-account      (fn [address]
                                                       (rf/dispatch [:trading.swap/set-account-address
                                                                     address]))
                              :asset-symbol          token-symbol
                              :network               network}])}]))

(defn- show-network-selector
  [token-symbol address]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn []
                            [network-selector/view
                             {:token-symbol      token-symbol
                              :address           address
                              :on-select-network (fn [{:keys [chain-id]}]
                                                   (rf/dispatch [:hide-bottom-sheet])
                                                   (rf/dispatch
                                                    [:trading.swap/set-chain-id chain-id]))}])}]))

(defn view
  []
  (let [{:keys [color emoji address]} (rf/sub [:trading.swap/account])
        input-focused?                (rf/sub [:trading.swap/focused?])
        network                       (rf/sub [:trading.swap/network])
        pay-token-symbol              (rf/sub [:trading.swap/pay-token-symbol])]
    [quo/page-nav
     (cond-> {}
       input-focused?       (assoc :icon-name :i/close
                                   :on-press  (fn []
                                                #_(rf/dispatch [:hide-bottom-sheet])
                                                (rf/dispatch [:trading.swap/blur-input])))
       (not input-focused?) (assoc :left-component profile-button)
       :always              (assoc :type                :dropdown
                                   :left-component      profile-button
                                   :dropdown-props      {:dropdown-text (:full-name network)
                                                         :on-press      #(show-network-selector
                                                                          pay-token-symbol
                                                                          address)
                                                         :type          :outline
                                                         :image?        true
                                                         :image-source  (:source network)}
                                   :margin-top          (safe-area/get-top)
                                   :background          :blur
                                   :accessibility-label :top-bar
                                   :align-center?       true
                                   :right-side          [{:content-type        :account-switcher
                                                          :customization-color color
                                                          :on-press            #(show-account-selector
                                                                                 address
                                                                                 pay-token-symbol
                                                                                 network)
                                                          :emoji               emoji}]))]))
