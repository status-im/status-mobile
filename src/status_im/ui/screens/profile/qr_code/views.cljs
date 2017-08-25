(ns status-im.ui.screens.profile.qr-code.views
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [status-im.components.qr-code :refer [qr-code]]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.qr-code.styles :as styles])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview qr-code-view []
  (letsubs [{:keys [photo-path address name] :as contact} [:get-in [:qr-modal :contact]]
            {:keys [qr-source amount? dimensions]} [:get :qr-modal]
            {:keys [amount]} [:get :contacts/click-params]]
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
         [qr-code {:value (if amount?
                            (prn-str {:address (get contact qr-source)
                                      :amount  amount})
                            (str "ethereum:" (get contact qr-source)))
                   :size  (- (min (:width dimensions)
                                  (:height dimensions))
                             80)}]])]
     [react/view styles/footer
      [react/view styles/wallet-info
       [react/text {:style styles/wallet-name-text} (label :t/main-wallet)]
       [react/text {:style styles/wallet-address-text} address]]

      [react/touchable-highlight {:onPress #(dispatch [:navigate-back])}
       [react/view styles/done-button
        [react/text {:style styles/done-button-text} (label :t/done)]]]]]))
