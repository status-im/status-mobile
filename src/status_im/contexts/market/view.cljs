(ns status-im.contexts.market.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.view :as common.top-nav]
    [status-im.common.refreshable-flat-list.view :as refreshable-flat-list]
    [status-im.constants :as constants]
    [status-im.contexts.market.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn new-account
  []
  (let [watched-accounts             (rf/sub [:wallet/watch-only-accounts])
        reached-max-watched-account? (>= (count watched-accounts)
                                         constants/max-allowed-watched-accounts)
        on-add-address-press         (rn/use-callback
                                      (fn []
                                        (if reached-max-watched-account?
                                          (rf/dispatch [:toasts/upsert
                                                        {:type :negative
                                                         :theme :dark
                                                         :text
                                                         (i18n/label
                                                          :t/saved-addresses-limit-reached-toast)}])
                                          (rf/dispatch [:navigate-to
                                                        :screen/wallet.add-address-to-watch])))
                                      [reached-max-watched-account?])]
    [quo/action-drawer
     [[{:icon                :i/add
        :accessibility-label :start-a-new-chat
        :label               (i18n/label :t/add-account)
        :sub-label           (i18n/label :t/add-account-description)
        :on-press            #(rf/dispatch [:navigate-to :screen/wallet.create-account])}
       {:icon                :i/reveal
        :accessibility-label :add-a-contact
        :label               (i18n/label :t/add-address-to-watch)
        :sub-label           (i18n/label :t/add-address-to-watch-description)
        :on-press            on-add-address-press
        :add-divider?        true}]]]))

(defn- new-account-card-data
  []
  {:customization-color (rf/sub [:profile/customization-color])
   :on-press            #(rf/dispatch [:show-bottom-sheet {:content new-account}])
   :type                :add-account})

(defn swap-header
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/swap-header-container theme)}

     [quo/text
      {:weight :semi-bold
       :size   :heading-1
       :style  (style/market-header-text theme)}
      (i18n/label :t/market)]

     [quo/button
      {:size      32
       ;; :type       :primary
       :icon-left :i/swap
       ;; :customization-color profile-color :icon-only? true
       :on-press  #()}
      (i18n/label :t/swap)]]))

(defn sort-header
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/sort-header-container theme)}
     [quo/text
      {:size  :paragraph-2
       :style (style/sort-text theme)}
      (i18n/label :t/market-cap)]
     [quo/icon :i/arrow-down
      {:size            12
       :color           colors/neutral-50
       :container-style {:margin-left 4}}]]))

(defn token
  [token-data]
  [quo/market-token token-data])

(defn view
  []
  (let [account-list-ref               (rn/use-ref-atom nil)
        selected-tab                   (rf/sub [:wallet/home-tab])
        tokens-loading?                (rf/sub [:wallet/home-tokens-loading?])
        tokens                         (rf/sub [:market/tokens])

        account-cards-data             (rf/sub [:wallet/account-cards-data])
        [init-loaded? set-init-loaded] (rn/use-state false)]
    (rn/use-effect
     #(when (and (boolean? tokens-loading?) (not tokens-loading?) (not init-loaded?))
        (set-init-loaded true))
     [tokens-loading?])
    [rn/view {:style (style/home-container)}
     [common.top-nav/view]
     [refreshable-flat-list/view
      {:refresh-control         [rn/refresh-control
                                 {:refreshing (and tokens-loading? init-loaded?)
                                  :colors     [colors/neutral-40]
                                  :tint-color colors/neutral-40
                                  :on-refresh #(rf/dispatch [:wallet/get-accounts])}]
       :header                  [rn/view
                                 [swap-header]
                                 [sort-header]]
       :content-container-style style/list-container
       :sticky-header-indices   [0]
       :data                    tokens
       :render-fn               token}]]))


(comment
  (rf/dispatch [:navigate-back]))
