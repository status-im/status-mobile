(ns status-im.transactions.views.password-form
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.react :as rn]
            [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.transactions.styles.password-form :as st]
            [status-im.i18n :as i18n]))

(defview view [transaction-quantity]
  [wrong-password? [:wrong-password?]]
  (let [error? wrong-password?]
    [rn/view st/password-container
     [text-input-with-label
      {:label             (i18n/label :t/password)
       :description       (i18n/label-pluralize transaction-quantity :t/enter-password-transactions)
       :on-change-text   #(rf/dispatch [:set-in [:confirm-transactions :password] %])
       :style             {:color :white}
       :auto-focus        true
       :secure-text-entry true
       :error             (when error? (i18n/label :t/wrong-password))}]]))

