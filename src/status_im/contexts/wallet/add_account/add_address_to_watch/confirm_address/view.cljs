(ns status-im.contexts.wallet.add-account.add-address-to-watch.confirm-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.contexts.wallet.add-account.add-address-to-watch.confirm-address.style :as style]
    [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [status-im.contexts.wallet.common.utils :as common.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [address]} (rf/sub [:get-screen-params])
        placeholder       (i18n/label :t/default-watched-address-placeholder)
        account-name      (reagent/atom "")
        account-color     (reagent/atom (rand-nth colors/account-colors))
        account-emoji     (reagent/atom (emoji-picker.utils/random-emoji))
        name-error        (reagent/atom nil)
        emoji-color-error (reagent/atom nil)]
    (fn []
      (let [accounts-names             (rf/sub [:wallet/accounts-names])
            accounts-emojis-and-colors (rf/sub [:wallet/accounts-emojis-and-colors])
            on-change-name             (fn [new-name]
                                         (reset! account-name new-name)
                                         (reset! name-error (common.utils/get-account-name-error
                                                             @account-name
                                                             accounts-names)))
            on-change-color            (fn [new-color]
                                         (reset! account-color new-color)
                                         (reset! emoji-color-error
                                           (when (accounts-emojis-and-colors
                                                  [@account-emoji @account-color])
                                             :emoji-and-color)))
            on-change-emoji            (fn [new-emoji]
                                         (reset! account-emoji new-emoji)
                                         (reset! emoji-color-error
                                           (when (accounts-emojis-and-colors
                                                  [@account-emoji @account-color])
                                             :emoji-and-color)))
            input-error                (or @emoji-color-error @name-error)]
        [rn/view {:style style/container}
         [create-or-edit-account/view
          {:placeholder         placeholder
           :account-name        @account-name
           :account-emoji       @account-emoji
           :account-color       @account-color
           :on-change-name      on-change-name
           :on-change-color     on-change-color
           :on-change-emoji     on-change-emoji
           :watch-only?         true
           :error               input-error
           :bottom-action-label :t/add-watched-address
           :bottom-action-props {:customization-color @account-color
                                 :disabled?           (or (string/blank? @account-name)
                                                          (some? input-error))
                                 :accessibility-label :confirm-button-label
                                 :on-press            #(rf/dispatch [:wallet/add-account
                                                                     {:type         :watch
                                                                      :account-name @account-name
                                                                      :emoji        @account-emoji
                                                                      :color        @account-color}
                                                                     {:address    address
                                                                      :public-key ""}])}}
          [quo/data-item
           {:card?           true
            :emoji           @account-emoji
            :title           (i18n/label :t/watched-address)
            :subtitle        address
            :status          :default
            :size            :default
            :subtitle-type   :default
            :custom-subtitle (fn [] [quo/text
                                     {:size   :paragraph-2
                                      ;; TODO: monospace font
                                      ;; https://github.com/status-im/status-mobile/issues/17009
                                      :weight :monospace}
                                     address])
            :container-style style/data-item}]]]))))
