(ns status-im.profile.qr-code.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight
                                                get-dimensions]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.styles :refer [icon-close]]
            [status-im.components.qr-code :refer [qr-code]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.profile.qr-code.styles :as st]
            [status-im.i18n :refer [label]]
            [clojure.string :as s]))

(defview qr-code-view []
  [{:keys [photo-path address name] :as contact} [:get-in [:qr-modal :contact]]
   {:keys [qr-source amount? dimensions]} [:get :qr-modal]
   {:keys [amount]} [:get :contacts-click-params]]
  [view st/wallet-qr-code
   [status-bar {:type :modal}]
   [view st/account-toolbar
    [view st/wallet-account-container
     [view st/qr-photo-container
      [view st/account-photo-container
       [image {:source {:uri (if (s/blank? photo-path) :avatar photo-path)}
               :style  st/photo-image}]]]
     [view st/name-container
      [text {:style           st/name-text
             :number-of-lines 1} name]]
     [view st/online-container
      [touchable-highlight {:onPress #(dispatch [:navigate-back])}
       [view st/online-image-container
        [image {:source {:uri :icon_close_white}
                :style  icon-close}]]]]]]
   [view {:style     st/qr-code
          :on-layout #(let [layout (.. % -nativeEvent -layout)]
                        (dispatch [:set-in [:qr-modal :dimensions] {:width  (.-width layout)
                                                                    :height (.-height layout)}]))}
    (when (:width dimensions)
      [view {:style (st/qr-code-container dimensions)}
       [qr-code {:value (if amount?
                          (prn-str {:address (get contact qr-source)
                                    :amount  amount})
                          (str "ethereum:" (get contact qr-source)))
                 :size  (- (min (:width dimensions)
                                (:height dimensions))
                           80)}]])]
   [view st/footer
    [view st/wallet-info
     [text {:style st/wallet-name-text} (label :t/main-wallet)]
     [text {:style st/wallet-address-text} address]]

    [touchable-highlight {:onPress #(dispatch [:navigate-back])}
     [view st/done-button
      [text {:style st/done-button-text} (label :t/done)]]]]])


