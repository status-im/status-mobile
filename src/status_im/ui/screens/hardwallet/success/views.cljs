(ns status-im.ui.screens.hardwallet.success.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.hardwallet.success.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]))

(defn- activity-indicator [show?]
  [react/view styles/waiting-indicator-container
   (when @show?
     [react/view
      [react/text {:style {:padding-bottom 20}}
       (i18n/label :t/sign-in-to-status)]
      [react/activity-indicator {:animating true
                                 :size      :large}]])])

(defn hardwallet-success []
  (let [processing? (reagent/atom false)]
    [react/view styles/container
     [status-bar/status-bar]
     [react/view components.styles/flex
      [react/view styles/inner-container
       [react/view styles/hardwallet-card-image-container
        [react/view styles/icon-check-container
         [react/view styles/icon-check-inner-container
          [vector-icons/icon :main-icons/check {:color  colors/green
                                                :width  30
                                                :height 30}]]]]
       [react/view styles/complete-text-container
        [activity-indicator processing?]
        [react/text {:style styles/complete-text}
         (i18n/label :t/complete-exclamation)]
        [react/text {:style styles/complete-information-text}
         (i18n/label :t/complete-hardwallet-setup)]
        [react/touchable-highlight
         {:on-press #(do
                       (reset! processing? true)
                       (re-frame/dispatch [:hardwallet.ui/success-button-pressed]))}
         [react/view styles/bottom-action-container
          [react/text {:style styles/bottom-action-text}
           (i18n/label :t/okay)]]]]]]]))
