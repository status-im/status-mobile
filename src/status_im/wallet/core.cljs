(ns status-im.wallet.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn show-password-input [{:keys [db]}]
  {:db (assoc-in db [:wallet :send-transaction :show-password-input?] true)})

(defn show-setup-confirmation [modal? cofx]
  {:ui/show-confirmation
   {:title               (i18n/label :t/wallet-setup-confirm-title)
    :content             (i18n/label :t/wallet-setup-confirm-description)
    :confirm-button-text (i18n/label :t/got-it)
    :cancel-button-text  (i18n/label :t/see-it-again)
    :on-accept           #(re-frame/dispatch [:accounts.ui/wallet-setup-confirmed modal?])
    :options             {:ios-confirm-style "default"}}})
