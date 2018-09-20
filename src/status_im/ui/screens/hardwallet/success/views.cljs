(ns status-im.ui.screens.hardwallet.success.views
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.hardwallet.success.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn hardwallet-success []
  [react/view styles/container
   [status-bar/status-bar]
   [react/view components.styles/flex
    [react/view styles/inner-container
     [react/view styles/hardwallet-card-image-container
      [react/image {:source (:hardwallet-card resources/ui)
                    :style  styles/hardwallet-card-image}]
      [react/view styles/icon-check-container
       [vector-icons/icon :icons/check {:color  :white
                                        :width  30
                                        :height 30}]]]
     [react/view styles/complete-text-container
      [react/text {:style styles/complete-text}
       (i18n/label :t/complete-exclamation)]
      [react/text {:style           styles/complete-information-text
                   :number-of-lines 3}
       (i18n/label :t/complete-hardwallet-setup)]]
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:hardwallet.ui/success-button-pressed])}
      [react/view styles/bottom-action-container
       [react/text {:style      styles/bottom-action-text
                    :font       :medium
                    :uppercase? true}
        (i18n/label :t/okay)]]]]]])