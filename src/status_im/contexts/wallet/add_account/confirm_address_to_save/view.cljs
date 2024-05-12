(ns status-im.contexts.wallet.add-account.confirm-address-to-save.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.confirm-address-to-save.style :as style]
    [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:const sheet-closing-delay 750)

(defn- on-success-confirm-address-to-save
  [theme]
  (rf/dispatch [:navigate-back])
  (debounce/debounce-and-dispatch
   [:navigate-back]
   sheet-closing-delay)
  (debounce/debounce-and-dispatch
   [:toasts/upsert
    {:type  :positive
     :theme theme
     :text  (i18n/label
             :t/address-saved)}]
   sheet-closing-delay))

(defn- on-press-confirm-address-to-save
  [{:keys [account-name account-color address ens? theme]}]
  (rf/dispatch
   [:wallet/save-address
    {:address             address
     :name                account-name
     :customization-color account-color
     :ens                 (when ens? address)
     :on-success          #(on-success-confirm-address-to-save theme)}]))

(defn view
  []
  (let [theme                                          (quo.theme/use-theme)
        {:keys                  [adding-address-purpose
                                 ens? address]
         {:keys [placeholder
                 address-type
                 button-label]} :confirm-screen-props} (rf/sub [:wallet/currently-added-address])
        [account-name on-change-name]                  (rn/use-state "")
        [account-color on-change-color]                (rn/use-state (rand-nth colors/account-colors))
        [selected-networks set-selected-networks]      (rn/use-state (set
                                                                      constants/default-network-names))]
    [:<>
     [rn/view {:style style/save-address-drawer-bar-container}
      [quo/drawer-bar]]
     [rn/view {:style style/container}
      [create-or-edit-account/view
       {:placeholder         (i18n/label placeholder)
        :account-name        account-name
        :account-color       account-color
        :on-change-name      on-change-name
        :on-change-color     on-change-color
        :watch-only?         true
        :top-left-icon       :i/arrow-left
        :bottom-action-label button-label
        :bottom-action-props {:customization-color account-color
                              :disabled?           (string/blank? account-name)
                              :accessibility-label :confirm-button-label
                              :on-press            #(on-press-confirm-address-to-save
                                                     {:ens?                   ens?
                                                      :adding-address-purpose adding-address-purpose
                                                      :account-name           account-name
                                                      :account-color          account-color
                                                      :address                address
                                                      :theme                  theme})}}
       [quo/data-item
        {:card?           true
         :right-icon      :i/advanced
         :icon-right?     true
         :title           (i18n/label address-type)
         :subtitle        address
         :status          :default
         :size            :default
         :subtitle-type   :default
         :custom-subtitle (fn []
                            [quo/text
                             {:size   :paragraph-2
                              :weight :monospace}
                             address])
         :container-style style/data-item
         :on-press        (rn/use-callback
                           (fn []
                             (let [on-save
                                   (fn [chain-ids]
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (set-selected-networks (set (map #(get network-utils/id->network %)
                                                                      chain-ids)))
                                     (rf/dispatch [:wallet/update-current-address-to-save chain-ids]))]
                               (rf/dispatch [:show-bottom-sheet
                                             {:content
                                              (fn []
                                                [network-preferences/view
                                                 {:button-label (i18n/label :t/add-preferences)
                                                  :sheet-title (i18n/label :t/add-network-preferences)
                                                  :sheet-description
                                                  (i18n/label
                                                   :t/add-saved-address-network-preferences-description)
                                                  :selected-networks selected-networks
                                                  :account {:address address
                                                            :color   account-color}
                                                  :on-save on-save
                                                  :watch-only? true}])}])))
                           [selected-networks])}]]]]))
