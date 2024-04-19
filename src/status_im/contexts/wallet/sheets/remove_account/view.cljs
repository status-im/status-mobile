(ns status-im.contexts.wallet.sheets.remove-account.view
  (:require
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.sheets.remove-account.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- footer
  [{:keys [address submit-disabled? toast-message]}]
  [quo/bottom-actions
   {:actions          :two-actions
    :button-one-label (i18n/label :t/remove)
    :button-one-props {:on-press
                       (fn []
                         (rf/dispatch [:wallet/remove-account
                                       {:address       address
                                        :toast-message toast-message}]))
                       :type :danger
                       :disabled? submit-disabled?}
    :button-two-label (i18n/label :t/cancel)
    :button-two-props {:on-press #(rf/dispatch [:hide-bottom-sheet])
                       :type     :grey}}])

(defn- recovery-phase-flow
  []
  (let [confirmed? (reagent/atom false)]
    (fn [{:keys [address name emoji path color] :as _account}]
      (let [formatted-path (utils/format-derivation-path path)]
        [:<>
         [quo/drawer-top
          {:title               (i18n/label :t/remove-account-title)
           :type                :context-tag
           :context-tag-type    :account
           :account-name        name
           :emoji               emoji
           :customization-color color}]
         [rn/view {:style style/desc-container}
          [quo/text {:weight :medium}
           (i18n/label :t/remove-account-desc)]]
         [quo/data-item
          {:size            :default
           :status          :default
           :card?           true
           :title           (i18n/label :t/derivation-path)
           :custom-subtitle (fn []
                              [quo/text {:weight :medium}
                               formatted-path])
           :icon-right?     true
           :right-icon      :i/copy
           :on-press        (fn []
                              (rf/dispatch [:toasts/upsert
                                            {:type :positive
                                             :text (i18n/label :t/derivation-path-copied)}])
                              (clipboard/set-string formatted-path))
           :container-style style/copy-container}]
         [rn/pressable
          {:style    style/checkbox-container
           :on-press #(swap! confirmed? not)}
          [quo/selectors
           {:type                :checkbox
            :customization-color color
            :checked?            @confirmed?
            :on-change           #(swap! confirmed? not)}]
          [quo/text (i18n/label :t/remove-account-confirmation)]]
         [footer
          {:submit-disabled? (not @confirmed?)
           :address          address
           :toast-message    (i18n/label :t/account-removed)}]]))))

(defn- watched-address-flow
  [{:keys [address name emoji color] :as _account}]
  [:<>
   [quo/drawer-top
    {:title               (i18n/label :t/remove-watched-address-title)
     :type                :context-tag
     :context-tag-type    :account
     :account-name        name
     :emoji               emoji
     :customization-color color}]
   [rn/view {:style style/desc-container}
    [quo/text {:weight :medium}
     (i18n/label :t/remove-watched-address-desc)]]
   [footer
    {:submit-disabled? false
     :address          address
     :toast-message    (i18n/label :t/watched-account-removed)}]])

(defn view
  []
  (let [{:keys [type] :as account} (rf/sub [:wallet/current-viewing-account])]
    (case type
      :generated [recovery-phase-flow account]
      :watch     [watched-address-flow account]
      nil)))
