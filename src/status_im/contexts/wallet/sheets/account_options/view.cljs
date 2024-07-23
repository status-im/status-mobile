(ns status-im.contexts.wallet.sheets.account-options.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.sheets.account-options.style :as style]
            [status-im.contexts.wallet.sheets.remove-account.view :as remove-account]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- render-account-item
  [{:keys [name emoji color address watch-only?]}]
  [quo/account-item
   {:account-props (cond-> {:name                name
                            :emoji               emoji
                            :customization-color color
                            :address             address}

                     watch-only?
                     (assoc :type :watch-only))
    :title-icon    (when watch-only? :i/reveal)
    :on-press      (fn []
                     (rf/dispatch [:wallet/switch-current-viewing-account address])
                     (rf/dispatch [:hide-bottom-sheet]))}])

(defn- options
  [{:keys [theme show-account-selector? options-height]}]
  (let [{:keys [name color emoji address watch-only?
                default-account?]} (rf/sub [:wallet/current-viewing-account])
        network-preference-details (rf/sub [:wallet/network-preference-details])
        multichain-address         (utils/get-multichain-address
                                    network-preference-details
                                    address)
        share-title                (i18n/label :t/share-address-title {:address name})]
    [rn/view
     {:on-layout #(reset! options-height (oops/oget % "nativeEvent.layout.height"))
      :style     (when show-account-selector? style/options-container)}
     (when show-account-selector?
       [quo/blur
        {:style         (style/blur-container @options-height)
         :blur-radius   (if platform/android? 20 10)
         :blur-amount   (if platform/ios? 20 10)
         :blur-type     (if (= theme :light) (if platform/ios? :light :xlight) :dark)
         :overlay-color (if (= theme :light) colors/white-70-blur colors/neutral-95-opa-70-blur)}])
     [quo/gradient-cover
      {:customization-color color
       :opacity             0.4}]
     [quo/drawer-bar]
     [quo/drawer-top
      (cond-> {:title                name
               :type                 :account
               :networks             network-preference-details
               :description          address
               :account-avatar-emoji emoji
               :customization-color  color}

        watch-only?
        (assoc :title-icon          :i/reveal
               :account-avatar-type :watch-only))]
     [quo/action-drawer
      [[{:icon                :i/edit
         :accessibility-label :edit
         :label               (i18n/label :t/edit-account)
         :on-press            #(rf/dispatch [:navigate-to :screen/wallet.edit-account])}
        {:icon                :i/copy
         :accessibility-label :copy-address
         :label               (i18n/label :t/copy-address)
         :on-press            (fn []
                                (rf/dispatch [:toasts/upsert
                                              {:type :positive
                                               :text (i18n/label :t/address-copied)}])
                                (clipboard/set-string multichain-address))}
        {:icon                :i/qr-code
         :accessibility-label :show-address-qr
         :label               (i18n/label :t/show-address-qr)
         :on-press            #(rf/dispatch [:open-modal :screen/wallet.share-address {:status :share}])}
        {:icon                :i/share
         :accessibility-label :share-account
         :label               (i18n/label :t/share-address)
         :on-press            (fn []
                                (rf/dispatch [:hide-bottom-sheet])
                                (js/setTimeout
                                 #(rf/dispatch [:wallet/share-account
                                                {:title share-title :content multichain-address}])
                                 600))}
        (when-not default-account?
          {:add-divider?        (not show-account-selector?)
           :icon                :i/delete
           :accessibility-label :remove-account
           :label               (i18n/label :t/remove-account)
           :danger?             true
           :on-press            #(rf/dispatch [:show-bottom-sheet
                                               {:content remove-account/view}])})]]]
     (when show-account-selector?
       [:<>
        [quo/divider-line {:container-style style/divider-label}]
        [quo/section-label
         {:section         (i18n/label :t/select-another-account)
          :container-style style/drawer-section-label}]])]))

(defn view
  []
  (let [options-height (reagent/atom 0)]
    (fn []
      (let [theme                  (quo.theme/use-theme)
            accounts               (rf/sub [:wallet/operable-accounts-without-current-viewing-account])
            show-account-selector? (pos? (count accounts))]
        [:<>
         (when show-account-selector?
           [gesture/flat-list
            {:data                            accounts
             :render-fn                       render-account-item
             :content-container-style         (style/list-container @options-height)
             :shows-vertical-scroll-indicator false}])
         [options
          {:show-account-selector? show-account-selector?
           :theme                  theme
           :options-height         options-height}]]))))
