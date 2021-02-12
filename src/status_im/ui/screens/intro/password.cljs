(ns status-im.ui.screens.intro.password
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as const]
            [status-im.utils.security :as security]
            [quo.react-native :as rn]
            [quo.core :as quo]))

(defn validate-password [password]
  (>= (count password) const/min-password-length))

(defn confirm-password [password confirm]
  (= password confirm))

(defn screen []
  (let [password    (reagent/atom nil)
        confirm     (reagent/atom nil)
        show-error  (reagent/atom nil)
        confirm-ref (atom nil)]
    (fn []
      (let [{:keys [forward-action processing?]}
            @(re-frame/subscribe [:intro-wizard/create-code])
            valid-password (validate-password @password)
            valid-form     (confirm-password @password @confirm)
            on-submit      (fn []
                             (when (not processing?)
                               (if (and valid-password valid-form)
                                 (do (reset! show-error false)
                                     (re-frame/dispatch [forward-action {:key-code @password}]))
                                 (reset! show-error true))))]
        [rn/keyboard-avoiding-view {:flex 1}
         [topbar/topbar
          {:border-bottom false
           :navigation
           {:icon                :main-icons/back
            :accessibility-label :back-button
            :on-press            #(re-frame/dispatch [:intro-wizard/navigate-back])}}]
         [rn/view {:style {:flex               1
                           :justify-content    :space-between
                           :padding-vertical   16
                           :padding-horizontal 16}}

          [rn/view
           [quo/text {:weight :bold
                      :align  :center
                      :size   :x-large}
            (i18n/label :intro-wizard-title-alt4)]]
          [rn/view
           [rn/view {:style {:padding 16}}
            [quo/text-input {:secure-text-entry   true
                             :auto-capitalize     :none
                             :auto-focus          true
                             :show-cancel         false
                             :accessibility-label :password-input
                             :placeholder         (i18n/label :t/password-placeholder)
                             :on-change-text      #(reset! password (security/mask-data %))
                             :return-key-type     :next
                             :on-submit-editing   #(when valid-password
                                                     (some-> ^js @confirm-ref .focus))}]]
           [rn/view {:style {:padding 16
                             :opacity (if-not valid-password 0.33 1)}}
            [quo/text-input {:secure-text-entry   true
                             :get-ref             #(reset! confirm-ref %)
                             :auto-capitalize     :none
                             :show-cancel         false
                             :accessibility-label :password-input
                             :editable            valid-password
                             :placeholder         (i18n/label :t/confirm-password-placeholder)
                             :return-key-type     :go
                             :error               (when @show-error (i18n/label :t/password_error1))
                             :blur-on-submit      true
                             :on-focus            #(reset! show-error false)
                             :on-submit-editing   on-submit
                             :on-change-text      #(do
                                                     (reset! confirm (security/mask-data %))
                                                     (cond
                                                       (> (count @password) (count @confirm))
                                                       (reset! show-error false)

                                                       (not (confirm-password @password @confirm))
                                                       (reset! show-error true)

                                                       :else (reset! show-error false)))}]]]
          [rn/view
           [quo/text {:color :secondary
                      :align :center
                      :size  :small}
            (i18n/label :t/password-description)]]]
         [toolbar/toolbar
          (merge {:show-border? true}
                 (if processing?
                   {:center
                    [rn/view {:align-items     :center
                              :justify-content :center
                              :flex-direction  :row}
                     [rn/activity-indicator {:size      :small
                                             :animating true}]
                     [rn/view {:padding-horizontal 8}
                      [quo/text {:color :secondary}
                       (i18n/label :t/processing)]]]}
                   {:right
                    [quo/button
                     {:on-press            on-submit
                      :accessibility-label :onboarding-next-button
                      :disabled            (or (nil? @confirm)
                                               (not valid-password)
                                               (not valid-form)
                                               processing?)
                      :type                :secondary
                      :after               :main-icons/next}
                     (i18n/label :t/next)]}))]]))))
