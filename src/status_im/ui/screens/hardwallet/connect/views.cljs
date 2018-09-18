(ns status-im.ui.screens.hardwallet.connect.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.hardwallet.connect.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defview hardwallet-connect []
  (letsubs [nfc-enabled? [:hardwallet/nfc-enabled?]]
    [react/view styles/container
     [status-bar/status-bar]
     [react/view components.styles/flex
      [toolbar/toolbar {}
       toolbar/default-nav-back
       nil
       [toolbar/actions [{:icon      :icons/info
                          :icon-opts {:color               :black
                                      :accessibility-label :hardwallet-connect-info-button}
                          :handler   #(re-frame/dispatch [:open-url-in-browser "https://hardwallet.status.im"])}]]]
      [react/view styles/hardwallet-connect
       [react/view styles/hardwallet-card-image-container
        [react/image {:source (:hardwallet-card resources/ui)
                      :style  styles/hardwallet-card-image}]]
       [react/view styles/status-hardwallet-text-container
        [react/text {:style styles/status-hardwallet-text}
         (i18n/label :t/status-hardwallet)]
        [react/text {:style styles/status-hardwallet-text}
         (i18n/label :t/secure-your-assets)]
        [react/text {:style           styles/link-card-text
                     :number-of-lines 2}
         (i18n/label :t/link-card)]]
       [react/view styles/bottom-action-container
        (if nfc-enabled?
          [react/view styles/nfc-enabled-container
           [react/image {:source (:phone-nfc resources/ui)
                         :style  styles/phone-nfc-image}]
           [react/image {:source (:hardwallet-card resources/ui)
                         :style  styles/hardwallet-card-image-small}]
           [react/text {:style           styles/hold-card-text
                        :number-of-lines 2
                        :uppercase?      true}
            (i18n/label :t/hold-card)]]
          [react/view styles/nfc-disabled-container
           [vector-icons/icon :icons/nfc {:color           colors/gray
                                          :container-style styles/nfc-icon}]
           [react/view styles/nfc-disabled-actions-container
            [react/text {:style      styles/turn-nfc-text
                         :font       :medium
                         :on-press   #(re-frame/dispatch [:hardwallet.ui/go-to-settings-button-pressed])
                         :uppercase? true}
             (i18n/label :t/turn-nfc-on)]
            [react/text {:style    styles/go-to-settings-text
                         :on-press #(re-frame/dispatch [:hardwallet.ui/go-to-settings-button-pressed])}
             (i18n/label :t/go-to-settings)]]])]]]]))