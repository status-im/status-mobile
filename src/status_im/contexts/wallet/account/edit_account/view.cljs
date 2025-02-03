(ns status-im.contexts.wallet.account.edit-account.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.contexts.wallet.account.edit-account.style :as style]
            [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view
             :as create-or-edit-account]
            [status-im.contexts.wallet.common.utils :as common.utils]
            [status-im.contexts.wallet.sheets.remove-account.view :as remove-account]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- show-save-account-toast
  [updated-key]
  (let [message (case updated-key
                  :name                       :t/edit-wallet-account-name-updated-message
                  :color                      :t/edit-wallet-account-colour-updated-message
                  :emoji                      :t/edit-wallet-account-emoji-updated-message
                  (:prod-preferred-chain-ids
                   :test-preferred-chain-ids) :t/edit-wallet-network-preferences-updated-message
                  nil)]
    (rf/dispatch [:toasts/upsert
                  {:id   :edit-account
                   :type :positive
                   :text (i18n/label message)}])))

(defn- save-account
  [{:keys [account updated-key new-value]}]
  (let [edited-account-data (assoc account updated-key new-value)]
    (rf/dispatch [:wallet/save-account
                  {:account    edited-account-data
                   :on-success #(show-save-account-toast updated-key)}])))

(defn view
  []
  (let [edited-account-name (reagent/atom nil)
        name-error          (reagent/atom nil)
        emoji-color-error   (reagent/atom nil)
        on-change-color     (fn [edited-color {:keys [color] :as account}]
                              (when (not= edited-color color)
                                (save-account {:account     account
                                               :updated-key :color
                                               :new-value   edited-color})))
        on-change-emoji     (fn [edited-emoji {:keys [emoji] :as account}]
                              (when (not= edited-emoji emoji)
                                (save-account {:account     account
                                               :updated-key :emoji
                                               :new-value   edited-emoji})))
        on-confirm-name     (fn [account]
                              (rn/dismiss-keyboard!)
                              (save-account {:account     account
                                             :updated-key :name
                                             :new-value   @edited-account-name}))]
    (fn []
      (let [{:keys [name emoji address color default-account?]
             :as   account}         (rf/sub [:wallet/current-viewing-account])
            other-account-names     (rf/sub [:wallet/accounts-names-without-current-account])
            other-emojis-and-colors (rf/sub [:wallet/accounts-emojis-and-colors-without-current-account])
            account-name            (or @edited-account-name name)
            input-error             (or @emoji-color-error @name-error)
            button-disabled?        (or (string/blank? @edited-account-name)
                                        (= name @edited-account-name)
                                        (some? input-error))]
        [create-or-edit-account/view
         {:page-nav-right-side [(when-not default-account?
                                  {:icon-name :i/delete
                                   :on-press  #(rf/dispatch [:show-bottom-sheet
                                                             {:content
                                                              (fn []
                                                                [remove-account/view])}])})]
          :account-name        account-name
          :placeholder         (i18n/label :t/default-account-placeholder)
          :account-emoji       emoji
          :account-color       color
          :on-change-name      (fn [new-name]
                                 (reset! edited-account-name new-name)
                                 (reset! name-error (common.utils/get-account-name-error
                                                     @edited-account-name
                                                     other-account-names)))
          :on-change-color     (fn [new-color]
                                 (if (other-emojis-and-colors [emoji new-color])
                                   (reset! emoji-color-error :emoji-and-color)
                                   (do
                                     (reset! emoji-color-error nil)
                                     (on-change-color new-color account))))
          :on-change-emoji     (fn [new-emoji]
                                 (if (other-emojis-and-colors [new-emoji color])
                                   (reset! emoji-color-error :emoji-and-color)
                                   (do
                                     (reset! emoji-color-error nil)
                                     (on-change-emoji new-emoji account))))
          :section-label       :t/account-info
          :bottom-action-label :t/confirm
          :bottom-action-props {:customization-color color
                                :disabled?           button-disabled?
                                :on-press            #(on-confirm-name account)}
          :error               input-error}
         [quo/data-item
          {:status          :default
           :size            :default
           :subtitle-type   :default
           :blur?           false
           :card?           true
           :title           (i18n/label :t/address)
           :custom-subtitle (fn [] [quo/address-text
                                    {:address address
                                     :format  :long}])
           :container-style style/data-item}]]))))
