(ns legacy.status-im.ui.screens.reset-password.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defview reset-password-popover
  []
  (letsubs [{:keys [resetting?]} [:multiaccount/reset-password-form-vals-and-errors]]
    [react/view
     {:padding-vertical   24
      :padding-horizontal 48
      :align-items        :center}
     [react/view
      {:width            32
       :height           32
       :background-color (if resetting?
                           colors/gray-lighter
                           colors/green-transparent-10)
       :border-radius    32
       :align-items      :center
       :justify-content  :center}
      (if resetting?
        [react/activity-indicator
         {:size      :small
          :animating true}]
        [icons/icon :main-icons/check {:color colors/green}])]
     [quo/text
      {:size   :x-large
       :weight :bold
       :align  :center
       :style  {:typography    :title-bold
                :margin-top    16
                :margin-bottom 24}}
      (i18n/label (if resetting?
                    :t/password-reset-in-progress
                    :t/password-reset-success))]
     (when-not resetting?
       [quo/text
        {:align :center
         :color :secondary
         :style {:margin-bottom 24}}
        (i18n/label :t/password-reset-success-message)])
     [react/view {:align-items :center}
      [quo/button
       {:on-press #(re-frame/dispatch [:profile/logout])
        :disabled resetting?}
       (i18n/label :t/okay)]]]))
