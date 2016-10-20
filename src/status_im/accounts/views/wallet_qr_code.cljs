(ns status-im.accounts.views.wallet-qr-code
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.styles :refer [icon-close]]
            [status-im.components.qr-code :refer [qr-code]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.accounts.styles :as st]
            [status-im.i18n :refer [label]]
            [clojure.string :as s]))


(defview wallet-qr-code []
  [{:keys [address photo-path name] :as account} [:get-current-account]
   {:keys [amount]} [:get :contacts-click-params]]
  [view st/wallet-qr-code
   [view st/account-toolbar
    [view st/wallet-account-container
     [view st/photo-container
      [view st/account-photo-container
       [image {:source {:uri (if (s/blank? photo-path) :avatar photo-path)}
               :style  st/photo-image}]]]
     [view st/name-container
      [text {:style           st/name-text
             :number-of-lines 1} name]]
     [view st/online-container
      [touchable-highlight {:onPress #(dispatch [:navigate-back])}
       [image {:source {:uri :icon-close-white}
               :style  icon-close}]]]]]
   [view st/qr-code
    [qr-code {:value (prn-str {:address address
                               :amount  amount})
              :size  200}]]
   [view st/footer
    [view st/wallet-info
     [text {:style st/wallet-name-text} (label :t/main-wallet)]
     [text {:style st/wallet-address-text} address]]

    [touchable-highlight {:onPress #(dispatch [:navigate-back])}
     [view st/done-button
      [text {:style st/done-button-text} (label :t/done)]]]]])

