(ns status-im.contexts.profile.settings.screens.password.change-password.new-password-form
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.password-with-hint.view :as password-with-hint]
    [status-im.common.validation.password :as password]
    [status-im.constants :as constant]
    [status-im.contexts.profile.settings.screens.password.change-password.events]
    [status-im.contexts.profile.settings.screens.password.change-password.header :as header]
    [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- help
  [{:keys [validations]}]
  (let [{:keys [lower-case? upper-case? numbers? symbols?]} validations
        password-strength                                   (password/strength validations)]
    [rn/view
     [quo/strength-divider {:type (constant/strength-status password-strength :info)}
      (i18n/label :t/password-creation-tips-title)]
     [rn/view {:style style/password-tips}
      [quo/tips {:completed? lower-case?}
       (i18n/label :t/password-creation-tips-1)]
      [quo/tips {:completed? upper-case?}
       (i18n/label :t/password-creation-tips-2)]
      [quo/tips {:completed? numbers?}
       (i18n/label :t/password-creation-tips-3)]
      [quo/tips {:completed? symbols?}
       (i18n/label :t/password-creation-tips-4)]]]))

(def not-blank? (complement string/blank?))

(defn view
  []
  (let [customization-color                            (rf/sub [:profile/customization-color])
        [password set-password]                        (rn/use-state "")
        [repeat-password set-repeat-password]          (rn/use-state "")
        [disclaimer-accepted? set-disclaimer-accepted] (rn/use-state false)
        [focused? set-focused]                         (rn/use-state false)
        [show-validation? set-show-validation]         (rn/use-state false)

        {:keys [long-enough? short-enough?]
         :as   validations}                            (password/validate password)
        empty-password?                                (string/blank? password)
        same-passwords?                                (and (not empty-password?)
                                                            (= password repeat-password))
        meet-requirements?                             (and (not empty-password?)
                                                            long-enough?
                                                            short-enough?
                                                            same-passwords?
                                                            disclaimer-accepted?)
        error?                                         (and show-validation?
                                                            (not same-passwords?)
                                                            (not empty-password?))

        ;; handlers
        on-change-password                             (fn [new-value]
                                                         (set-password new-value)
                                                         (when (and (not-blank? new-value)
                                                                    (= (count new-value)
                                                                       (count repeat-password)))
                                                           (set-show-validation true)))
        on-change-repeat-password                      (fn [new-value]
                                                         (set-repeat-password new-value)
                                                         (when (and (not-blank? new-value)
                                                                    (= (count new-value)
                                                                       (count password)))
                                                           (set-show-validation true)))
        on-blur-repeat-password                        (fn []
                                                         (if empty-password?
                                                           (set-show-validation false)
                                                           (set-show-validation true)))
        on-input-focus                                 (fn [] (set-focused true))
        on-disclaimer-change                           (fn []
                                                         (set-disclaimer-accepted
                                                          (not disclaimer-accepted?)))
        on-submit                                      (fn []
                                                         (rf/dispatch
                                                          [:change-password/confirm-new-password
                                                           (security/mask-data password)]))]
    [:<>
     [rn/scroll-view {:style style/form-container}
      [header/view]
      [password-with-hint/view
       {:hint           (if (not short-enough?)
                          {:text   (i18n/label :t/password-creation-max-length-hint)
                           :status :error
                           :shown? true}
                          {:text   (i18n/label :t/password-creation-hint)
                           :status (if long-enough? :success :default)
                           :shown? true})
        :placeholder    (i18n/label :t/change-password-new-password-placeholder)
        :label          (i18n/label :t/change-password-new-password-label)
        :on-change-text on-change-password
        :on-focus       on-input-focus
        :auto-focus     true}]
      [rn/view {:style style/space-between-inputs}]
      [password-with-hint/view
       {:hint           {:text   (if same-passwords?
                                   (i18n/label :t/password-creation-match)
                                   (i18n/label :t/password-creation-dont-match))
                         :status (if same-passwords? :success :error)
                         :shown? (and (not empty-password?)
                                      show-validation?)}
        :error?         error?
        :placeholder    (i18n/label :t/change-password-repeat-password-placeholder)
        :on-change-text on-change-repeat-password
        :on-focus       on-input-focus
        :on-blur        on-blur-repeat-password}]]
     [rn/view {:style style/bottom-part}
      (when same-passwords?
        [rn/view {:style style/disclaimer-container}
         [quo/disclaimer
          {:blur?               true
           :customization-color customization-color
           :on-change           on-disclaimer-change
           :checked?            disclaimer-accepted?}
          (i18n/label :t/password-creation-disclaimer)]])
      (when (and focused? (not same-passwords?))
        [help
         {:validations validations}])
      [rn/view {:style style/button-container}
       [quo/button
        {:disabled?           (not meet-requirements?)
         :customization-color customization-color
         :on-press            on-submit}
        (i18n/label :t/password-creation-confirm)]]]]))
