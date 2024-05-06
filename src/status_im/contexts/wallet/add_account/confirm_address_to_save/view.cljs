(ns status-im.contexts.wallet.add-account.confirm-address-to-save.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.wallet.add-account.confirm-address-to-save.style :as style]
    [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:const sheet-closing-delay 750)

(defn- on-success-confirm-address
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

(defn- on-press-confirm-address
  [{:keys [account-name account-emoji account-color address ens? theme]}]
  (rf/dispatch
   [:wallet/save-address
    {:address             address
     :name                account-name
     :customization-color account-color
     :account-emoji       account-emoji
     :ens                 (when ens? address)
     :on-success          #(on-success-confirm-address theme)}]))

(defn to-be-implemented
  []
  not-implemented/alert)

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
        [account-emoji on-change-emoji]                (rn/use-state (emoji-picker.utils/random-emoji))]
    [:<>
     [rn/view {:style style/save-address-drawer-bar-container}
      [quo/drawer-bar]]
     [rn/view {:style style/container}
      [create-or-edit-account/view
       {:placeholder         (i18n/label placeholder)
        :account-name        account-name
        :account-emoji       account-emoji
        :account-color       account-color
        :on-change-name      on-change-name
        :on-change-color     on-change-color
        :on-change-emoji     on-change-emoji
        :watch-only?         true
        :top-left-icon       :i/arrow-left
        :bottom-action-label button-label
        :bottom-action-props {:customization-color account-color
                              :disabled?           (string/blank? account-name)
                              :accessibility-label :confirm-button-label
                              :on-press            #(on-press-confirm-address
                                                     {:ens?                   ens?
                                                      :adding-address-purpose adding-address-purpose
                                                      :account-name           account-name
                                                      :account-emoji          account-emoji
                                                      :account-color          account-color
                                                      :address                address
                                                      :theme                  theme})}}
       [quo/data-item
        {:card?           true
         :right-icon      :i/advanced
         :icon-right?     true
         :emoji           account-emoji
         :title           (i18n/label address-type)
         :subtitle        address
         :status          :default
         :size            :default
         :subtitle-type   :default
         :custom-subtitle (fn []
                            [quo/text
                             {:size   :paragraph-2
                              ;; TODO: monospace font
                              ;; https://github.com/status-im/status-mobile/issues/17009
                              :weight :monospace}
                             address])
         :container-style style/data-item
         :on-press        to-be-implemented}]]]]))
