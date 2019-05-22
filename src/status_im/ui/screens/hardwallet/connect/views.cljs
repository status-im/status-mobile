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
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]))

(defview nfc-enabled []
  (letsubs [card-read-in-progress? [:hardwallet/card-read-in-progress?]]
    [react/view styles/nfc-enabled-container
     [react/view
      [react/image {:source (resources/get-image :hold-card-animation)
                    :style  styles/phone-nfc-on-image}]]
     [react/view styles/turn-nfc-text-container
      [react/text {:style           styles/status-hardwallet-text
                   :number-of-lines 2}
       (i18n/label :t/hold-card)]]
     (when card-read-in-progress?
       [react/view {:margin-top 35}
        [react/activity-indicator {:animating true
                                   :size      :large}]])]))

(defn nfc-disabled []
  [react/view styles/nfc-disabled-container
   [react/view
    [react/image {:source (resources/get-image :phone-nfc-off)
                  :style  styles/phone-nfc-off-image}]]
   [react/view styles/turn-nfc-text-container
    [react/text {:style    styles/status-hardwallet-text
                 :on-press #(re-frame/dispatch [:hardwallet.ui/go-to-settings-button-pressed])}
     (i18n/label :t/turn-nfc-on)]
    [react/text {:style    styles/go-to-settings-text
                 :on-press #(re-frame/dispatch [:hardwallet.ui/go-to-settings-button-pressed])}
     (i18n/label :t/go-to-settings)]]])

(defview hardwallet-connect []
  (letsubs [nfc-enabled? [:hardwallet/nfc-enabled?]
            setup-step [:hardwallet-setup-step]]
    [react/view styles/container
     [status-bar/status-bar]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between}
      [toolbar/toolbar {}
       [toolbar/nav-button (assoc toolbar.actions/default-back
                                  :handler
                                  #(re-frame/dispatch [:hardwallet.ui/hardwallet-connect-navigate-back-button-clicked]))]
       nil]
      [react/view styles/hardwallet-connect
       (if nfc-enabled?
         [nfc-enabled]
         [nfc-disabled])]
      (if (= setup-step :begin)
        [react/view styles/bottom-container
         [react/touchable-highlight {:on-press #(.openURL (react/linking) "https://hardwallet.status.im")}
          [react/view styles/product-info-container
           [react/text {:style styles/product-info-text}
            (i18n/label :t/product-information)]
           [vector-icons/icon :main-icons/link {:color           colors/blue
                                                :container-style styles/external-link-icon}]]]])]]))
