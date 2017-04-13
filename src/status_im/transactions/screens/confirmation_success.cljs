(ns status-im.transactions.screens.confirmation-success
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.react :as rn]
            [status-im.components.sticky-button :as sticky-button]
            [status-im.components.status-bar :as status-bar]
            [status-im.transactions.views.list-item :as transactions-list-item]
            [status-im.transactions.styles.screens :as st]
            [status-im.i18n :as i18n]))

(defview confirmation-success []
  [quantity [:get :confirmed-transactions-count]]
  [rn/view {:style st/success-screen}
   [status-bar/status-bar {:type :transparent}]
   [rn/view {:style st/success-screen-content-container}
    [rn/view {:style st/success-icon-container}
     [rn/image {:source {:uri :icon_ok_white}
                :style  st/success-icon}]]
    [rn/view
     [rn/text {:style st/success-text}
      (i18n/label-pluralize quantity :t/transactions-confirmed)]]]
   [sticky-button/sticky-button
    (i18n/label :t/got-it)
    #(do (rf/dispatch [:navigate-back])
         (rf/dispatch [:set :confirmed-transactions-count 0]))]])
