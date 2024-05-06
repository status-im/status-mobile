(ns status-im.contexts.wallet.add-account.confirm-address-to-watch.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.contexts.wallet.add-account.confirm-address-to-watch.style :as style]
    [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-press-confirm-address-to-watch
  [{:keys [account-name account-emoji account-color address]}]
  (rf/dispatch [:wallet/add-account
                {:sha3-pwd     nil
                 :account-name account-name
                 :type         :watch
                 :emoji        account-emoji
                 :color        account-color}
                {:address    address
                 :public-key ""}]))

(defn to-be-implemented
  []
  not-implemented/alert)

(defn view
  []
  (let [theme                                          (quo.theme/use-theme)
        {:keys                  [ens? address]
         {:keys [placeholder
                 address-type
                 button-label]} :confirm-screen-props} (rf/sub [:wallet/currently-added-address])
        [account-name on-change-name]                  (rn/use-state "")
        [account-color on-change-color]                (rn/use-state (rand-nth colors/account-colors))
        [account-emoji on-change-emoji]                (rn/use-state (emoji-picker.utils/random-emoji))]
    [:<>
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
                              :on-press            #(on-press-confirm-address-to-watch
                                                     {:ens?          ens?
                                                      :account-name  account-name
                                                      :account-emoji account-emoji
                                                      :account-color account-color
                                                      :address       address
                                                      :theme         theme})}}
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
