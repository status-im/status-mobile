(ns legacy.status-im.ui.screens.reset-password.views
  (:require
    [legacy.status-im.multiaccounts.reset-password.core :as reset-password]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]
    [utils.security.core :as security])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defn input-field
  [{:keys [id errors on-submit disabled? focus?]
    :or   {disabled? false focus? false}}]
  [react/view {:style {:opacity (if disabled? 0.33 1)}}
   [quo/text-input
    {:placeholder         (i18n/label id)
     :default-value       ""
     :auto-focus          focus?
     :editable            (not disabled?)
     :accessibility-label id
     :show-cancel         false
     :style               {:margin-bottom 32}
     :on-submit-editing   on-submit
     :on-change-text      #(re-frame/dispatch [::reset-password/handle-input-change
                                               id
                                               (security/mask-data %)])
     :secure-text-entry   true
     :error               (when-let [error (get errors id)]
                            (if (keyword? error)
                              (i18n/label error)
                              error))}]])

(defview reset-password
  []
  (letsubs [{:keys [form-vals errors next-enabled? resetting?]}
            [:multiaccount/reset-password-form-vals-and-errors]]
    (let [{:keys [current-password
                  new-password]}
          form-vals
          on-submit #(re-frame/dispatch [::reset-password/reset form-vals])]
      [react/keyboard-avoiding-view {:flex 1}
       [react/view
        {:style {:flex            1
                 :justify-content :space-between}}
        [react/view
         {:style {:margin-top         32
                  :padding-horizontal 16
                  :padding-vertical   16}}
         [input-field
          {:id        :current-password
           :errors    errors
           :on-sumbit on-submit
           :disabled? false
           :focus?    true}]
         [input-field
          {:id        :new-password
           :errors    errors
           :on-sumbit on-submit
           :disabled? (zero? (count current-password))}]
         [input-field
          {:id        :confirm-new-password
           :errors    errors
           :on-sumbit on-submit
           :disabled? (zero? (count new-password))}]]
        [quo/text
         {:color :secondary
          :align :center
          :size  :small
          :style {:padding-horizontal 16}}
         (i18n/label :t/password-description)]
        [toolbar/toolbar
         {:show-border? true
          :right
          [quo/button
           {:on-press            on-submit
            :accessibility-label :next-button
            :disabled            (or (not next-enabled?)
                                     ;; disable on resetting? so the user cannot press the next button
                                     ;; recklessly
                                     ;; https://github.com/status-im/status-mobile/pull/12245#issuecomment-874827573
                                     resetting?)
            :type                :secondary
            :after               :main-icons/next}
           (i18n/label :t/next)]}]]])))

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
       {:on-press #(re-frame/dispatch [:logout])
        :disabled resetting?}
       (i18n/label :t/okay)]]]))
