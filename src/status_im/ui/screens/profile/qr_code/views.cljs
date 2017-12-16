(ns status-im.ui.screens.profile.qr-code.views
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [status-im.ui.components.qr-code :refer [qr-code]]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.qr-code.styles :as styles]
            [status-im.utils.money :as money])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview qr-code-view []
  (letsubs [{:keys [photo-path address name]} [:get-in [:qr-modal :contact]]
            {:keys [qr-source qr-value dimensions]} [:get :qr-modal]
            chain-id [:get-network-id]]
    [react/view styles/wallet-qr-code
     [status-bar {:type :modal}]
     [react/view styles/account-toolbar
      [react/view styles/wallet-account-container
       [react/view styles/qr-photo-container
        [react/view styles/account-photo-container
         [react/image {:source {:uri (if (string/blank? photo-path) :avatar photo-path)}
                       :style  styles/photo-image}]]]
       [react/view styles/name-container
        [react/text {:style           styles/name-text
                     :number-of-lines 1} name]]
       [react/view styles/online-container
        [react/touchable-highlight {:onPress #(dispatch [:navigate-back])}
         [react/view styles/online-image-container
          [vi/icon :icons/close {:color :white}]]]]]]
     [react/view {:style     styles/qr-code
                  :on-layout #(let [layout (.. % -nativeEvent -layout)]
                                (dispatch [:set-in [:qr-modal :dimensions] {:width  (.-width layout)
                                                                            :height (.-height layout)}]))}
      (when (:width dimensions)
        [react/view {:style (styles/qr-code-container dimensions)}
         [qr-code {:value qr-value
                   :size  (- (min (:width dimensions)
                                  (:height dimensions))
                             80)}]])]
     [react/view styles/footer
      (if (= :address qr-source)
        [react/view styles/wallet-info
         [react/text {:style styles/wallet-name-text} (label :t/main-wallet)]
         [react/text {:style styles/wallet-address-text} address]]
        [react/view styles/wallet-info
         [react/text {:style styles/wallet-name-text} (label :t/public-key)]])
      [react/touchable-highlight {:onPress #(dispatch [:navigate-back])}
       [react/view styles/done-button
        [react/text {:style styles/done-button-text} (label :t/done)]]]]]))
