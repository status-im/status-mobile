(ns status-im2.contexts.wallet.common.sheets.account-options.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.blur :as blur]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.common.sheets.account-options.style :as style]
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
  (let [{:keys [name color emoji address watch-only?]} (rf/sub [:wallet/current-viewing-account])
        network-preference-details                     (rf/sub [:wallet/network-preference-details])]
    [rn/view
     {:on-layout #(reset! options-height (oops/oget % "nativeEvent.layout.height"))
      :style     (when show-account-selector? style/options-container)}
     (when show-account-selector?
       [blur/view
        {:style         (style/blur-container @options-height)
         :blur-radius   (if platform/android? 20 10)
         :blur-amount   (if platform/ios? 20 10)
         :blur-type     (quo.theme/theme-value (if platform/ios? :light :xlight) :dark theme)
         :overlay-color (quo.theme/theme-value colors/white-70-blur
                                               colors/neutral-95-opa-70-blur
                                               theme)}])
     [rn/view {:style style/gradient-container}
      [quo/gradient-cover
       {:customization-color color
        :opacity             0.4}]]
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
         :on-press            #(rf/dispatch [:navigate-to :wallet-edit-account])}
        {:icon                :i/copy
         :accessibility-label :copy-address
         :label               (i18n/label :t/copy-address)
         :on-press            (fn []
                                (rf/dispatch [:toasts/upsert
                                              {:icon       :i/correct
                                               :icon-color (colors/resolve-color :success theme)
                                               :text       (i18n/label :t/address-copied)}])
                                (clipboard/set-string address))}
        {:icon                :i/share
         :accessibility-label :share-account
         :label               (i18n/label :t/share-account)}
        {:add-divider?        (not show-account-selector?)
         :icon                :i/delete
         :accessibility-label :remove-account
         :label               (i18n/label :t/remove-account)
         :danger?             true}]]]
     (when show-account-selector?
       [:<>
        [quo/divider-line {:container-style style/divider-label}]
        [quo/section-label
         {:section         (i18n/label :t/select-another-account)
          :container-style style/drawer-section-label}]])]))

(defn- view-internal
  []
  (let [options-height (reagent/atom 0)]
    (fn [{:keys [theme]}]
      (let [accounts               (rf/sub [:wallet/accounts-without-current-viewing-account])
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

(def view (quo.theme/with-theme view-internal))
