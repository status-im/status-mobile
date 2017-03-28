(ns status-im.transactions.views.password-form
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.react :as rn]
            [status-im.components.styles :as common-st]
            [status-im.components.text-field.view :as text-field]
            [status-im.transactions.styles.password-form :as st]
            [status-im.i18n :as i18n]))

(defview view [transaction-quantity]
  [wrong-password? [:wrong-password?]]
  (let [error? wrong-password?]
    [rn/view (st/password-container error?)
     [rn/text {:style st/password-title}
      (i18n/label-pluralize transaction-quantity :t/enter-password-transactions)]
     [text-field/text-field
      {:editable               true
       :secure-text-entry      true
       :label-hidden?          true
       :error                  (when error? (i18n/label :t/wrong-password))
       :error-color            common-st/color-light-red2
       :placeholder            (i18n/label :t/password)
       :placeholder-text-color common-st/color-white-transparent
       :line-color             common-st/color-light-blue
       :focus-line-height      2
       :wrapper-style          st/password-input-wrapper
       :input-style            st/password-input
       :auto-focus             true
       :on-change-text         #(rf/dispatch [:set-in [:confirm-transactions :password] %])}]]))
