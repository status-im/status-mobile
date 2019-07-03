(ns status-im.ui.screens.multiaccounts.create.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.react :as components]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.screens.multiaccounts.create.styles :as styles]))

(def steps
  {:enter-password   {:input-key         :password
                      :input-label       (i18n/label :t/password)
                      :input-placeholder (i18n/label :t/password-placeholder)
                      :input-description (i18n/label :t/password-description)}
   :confirm-password {:input-key         :password-confirm
                      :input-label       (i18n/label :t/confirm)
                      :input-placeholder (i18n/label :t/password-placeholder2)
                      :input-description (i18n/label :t/password-description)}
   :multiaccount-creating nil
   :enter-name       {:input-key         :name
                      :input-label       (i18n/label :t/name)
                      :input-placeholder (i18n/label :t/name-placeholder)
                      :input-description (i18n/label :t/name-description)}})

(defview input [next-enabled? {:keys [step error password password-confirm]}]
  [text-input/text-input-with-label
   (cond-> {:label             (get-in steps [step :input-label])
            :placeholder       (get-in steps [step :input-placeholder])
            :on-change-text    #(re-frame/dispatch [:multiaccounts.create.ui/input-text-changed (get-in steps [step :input-key]) %])
            :secure-text-entry (boolean (#{:enter-password :confirm-password} step))
            :auto-focus        true
            :error             error}
     next-enabled? (assoc :on-submit-editing #(re-frame/dispatch [:multiaccounts.create.ui/next-step-pressed step password password-confirm])))])

(defview create-multiaccount []
  (letsubs [{:keys [step error password password-confirm]} [:multiaccounts/create]
            next-enabled? [:get-multiaccount-creation-next-enabled?]]
    [react/keyboard-avoiding-view {:style styles/create-multiaccount-view}
     [status-bar/status-bar {:flat? true}]
     [toolbar/toolbar nil
      (when (#{:enter-password :confirm-password} step)
        (toolbar/nav-button (actions/back #(re-frame/dispatch [:multiaccounts.create.ui/step-back-pressed step])))) nil]
     (when (= :multiaccount-creating step)
       [react/view styles/multiaccount-creating-view
        [react/view styles/logo-container
         [components.common/logo styles/logo]]
        [react/view {:style styles/multiaccount-creating-indicatior}
         [components/activity-indicator {:animating true}]
         [react/text {:style styles/multiaccount-creating-text}
          (i18n/label :t/creating-your-multiaccount)]]])
     (when (#{:enter-password :confirm-password :enter-name} step)
       [react/scroll-view {:flex 1}
        [react/view {:style styles/logo-container}
         [components.common/logo styles/logo]]
        ^{:key (str "step" step)}
        [react/view components.styles/flex
         [react/view {:style                       styles/input-container
                      :important-for-accessibility :no-hide-descendants}
          [input next-enabled?
           {:step             step
            :error            error
            :password         password
            :password-confirm password-confirm}]
          [react/text {:style styles/input-description}
           (get-in steps [step :input-description])]]]])
     (when (#{:enter-password :confirm-password :enter-name} step)
       [react/view {:style styles/bottom-container}
        [react/view {:style components.styles/flex}]
        [components.common/bottom-button
         {:forward?  true
          :disabled? (not next-enabled?)
          :on-press  #(re-frame/dispatch [:multiaccounts.create.ui/next-step-pressed step password password-confirm])}]])]))
