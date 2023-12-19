(ns legacy.status-im.ui.screens.keycard.pairing.views
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [utils.security.core :as security]))

(defn validate-pairing-code
  [pairing-code]
  (>= (count pairing-code) 1))

(defn confirm-pairing-code
  [pairing-code confirm]
  (= pairing-code confirm))

(defn change-pairing-code
  []
  (let [pairing-code (reagent/atom nil)
        confirm      (reagent/atom nil)
        show-error   (reagent/atom nil)
        confirm-ref  (atom nil)]
    (fn []
      (let [valid-pairing-code (validate-pairing-code @pairing-code)
            valid-form         (confirm-pairing-code @pairing-code @confirm)
            on-submit          (fn []
                                 (if (and valid-pairing-code valid-form)
                                   (do (reset! show-error false)
                                       (re-frame/dispatch [:keycard/change-pairing-code @pairing-code]))
                                   (reset! show-error true)))]
        [rn/keyboard-avoiding-view {:flex 1}
         [rn/scroll-view {:style {:flex 1}}
          [rn/view
           {:style {:flex               1
                    :justify-content    :space-between
                    :padding-vertical   16
                    :padding-horizontal 16}}

           [rn/view
            [quo/text
             {:weight :bold
              :align  :center
              :size   :x-large}
             (i18n/label :t/change-pairing-title)]]
           [rn/view
            [rn/view {:style {:padding 16}}
             [quo/text-input
              {:secure-text-entry   true
               :auto-capitalize     :none
               :auto-focus          true
               :show-cancel         false
               :accessibility-label :password-input
               :placeholder         (i18n/label :t/pairing-code-placeholder)
               :on-change-text      #(reset! pairing-code (security/mask-data %))
               :return-key-type     :next
               :on-submit-editing   #(when valid-pairing-code
                                       (some-> ^js @confirm-ref
                                               .focus))}]]
            [rn/view
             {:style {:padding 16
                      :opacity (if-not valid-pairing-code 0.33 1)}}
             [quo/text-input
              {:secure-text-entry   true
               :get-ref             #(reset! confirm-ref %)
               :auto-capitalize     :none
               :show-cancel         false
               :accessibility-label :password-input
               :editable            valid-pairing-code
               :placeholder         (i18n/label :t/confirm-pairing-code-placeholder)
               :return-key-type     :go
               :error               (when @show-error (i18n/label :t/pairing-code_error1))
               :blur-on-submit      true
               :on-focus            #(reset! show-error false)
               :on-submit-editing   on-submit
               :on-change-text      #(do
                                       (reset! confirm (security/mask-data %))
                                       (cond
                                         (> (count @pairing-code) (count @confirm))
                                         (reset! show-error false)

                                         (not (confirm-pairing-code @pairing-code @confirm))
                                         (reset! show-error true)

                                         :else (reset! show-error false)))}]]]
           [rn/view
            [quo/text
             {:color :secondary
              :align :center
              :size  :small}
             (i18n/label :t/change-pairing-description)]]]]
         [toolbar/toolbar
          {:show-border? true
           :right        [quo/button
                          {:on-press            on-submit
                           :accessibility-label :onboarding-next-button
                           :disabled            (or (nil? @confirm)
                                                    (not valid-pairing-code)
                                                    (not valid-form))
                           :type                :secondary
                           :after               :main-icons/next}
                          (i18n/label :t/change-pairing)]}]]))))
