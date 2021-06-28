(ns status-im.ui.screens.reset-password.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.multiaccounts.reset-password.core :as reset-password]
            [status-im.utils.security :as security]
            [status-im.ui.components.toolbar :as toolbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn input-field
  ([id errors on-submit] (input-field id errors on-submit false))
  ([id errors on-submit focus?]
   [quo/text-input
    {:placeholder         (i18n/label id)
     :default-value       ""
     :auto-focus          focus?
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
                              error))}]))

(defview reset-password []
  (letsubs [{:keys [form-vals errors next-enabled?]}
            [:multiaccount/reset-password-form-vals-and-errors]]
    (let [on-submit #(re-frame/dispatch [::reset-password/reset form-vals])]
      [react/keyboard-avoiding-view {:flex 1}
       [react/view {:style {:flex            1
                            :justify-content :space-between}}
        [react/view {:style {:padding-horizontal 16
                             :padding-vertical   16}}
         [input-field :current-password errors on-submit true]
         [input-field :new-password errors on-submit]
         [input-field :confirm-new-password errors on-submit]]
        [quo/text {:color :secondary :align :center :size :small
                   :style {:padding-horizontal 16}}
         (i18n/label :t/password-description)]
        [toolbar/toolbar
         {:show-border? true
          :right
          [quo/button
           {:on-press            on-submit
            :accessibility-label :next-button
            :disabled            (not next-enabled?)
            :type                :secondary
            :after               :main-icons/next}
           (i18n/label :t/next)]}]]])))

(defview reset-success-popover []
  [react/view {:padding-vertical   24
               :padding-horizontal 48
               :align-items        :center}
   [react/view {:width            32
                :height           32
                :background-color colors/green-transparent-10
                :border-radius    32
                :align-items      :center
                :justify-content  :center}
    [icons/icon :main-icons/check {:color colors/green}]]
   [quo/text {:size   :x-large
              :weight :bold
              :style  {:typography    :title-bold
                       :margin-top    16
                       :margin-bottom 24}}
    (i18n/label :t/password-reset-success)]
   [react/view {:align-items :center}
    [quo/button {:on-press #(re-frame/dispatch [:hide-popover])}
     (i18n/label :t/ok-got-it)]]])
