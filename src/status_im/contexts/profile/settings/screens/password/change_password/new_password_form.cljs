(ns status-im.contexts.profile.settings.screens.password.change-password.new-password-form
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.constants :as constant]
    [status-im.contexts.profile.settings.screens.password.change-password.events]
    [status-im.contexts.profile.settings.screens.password.change-password.header :as header]
    [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.string :as utils.string]))

(defn- password-with-hint
  [{{:keys [text status shown?]} :hint :as input-props}]
  [:<>
   [quo/input
    (-> input-props
        (dissoc :hint)
        (assoc :type  :password
               :blur? true))]
   [rn/view {:style style/info-message}
    (when shown?
      [quo/info-message
       (cond-> {:status status
                :size   :default}
         (not= :success status) (assoc :icon :i/info)
         (= :success status)    (assoc :icon :i/check-circle)
         (= :default status)    (assoc :color colors/white-70-blur))
       text])]])

(defn- calc-password-strength
  [validations]
  (->> (vals validations)
       (filter true?)
       count))

(defn- help
  [{:keys [validations]}]
  (let [{:keys [lower-case? upper-case? numbers? symbols?]} validations
        password-strength                                   (calc-password-strength validations)]
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

(defn- password-validations
  [password]
  {:lower-case? (utils.string/has-lower-case? password)
   :upper-case? (utils.string/has-upper-case? password)
   :numbers?    (utils.string/has-numbers? password)
   :symbols?    (utils.string/has-symbols? password)})

(defn view
  []
  (let [customization-color                            (rf/sub [:profile/customization-color])
        [password set-password]                        (rn/use-state "")
        [repeat-password set-repeat-password]          (rn/use-state "")
        [disclaimer-accepted? set-disclaimer-accepted] (rn/use-state false)
        [focused? set-focused]                         (rn/use-state false)
        [show-validation? set-show-validation]         (rn/use-state false)

        ;; validations
        not-blank?                                     (complement string/blank?)
        validations                                    (password-validations password)
        long-enough?                                   (utils.string/at-least-n-chars?
                                                        password
                                                        constant/new-password-min-length)
        empty-password?                                (string/blank? password)
        same-passwords?                                (and (not empty-password?)
                                                            (= password repeat-password))
        meet-requirements?                             (and (not empty-password?)
                                                            long-enough?
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
      [password-with-hint
       {:hint           {:text   (i18n/label :t/password-creation-hint)
                         :status (if long-enough? :success :default)
                         :shown? true}
        :placeholder    (i18n/label :t/change-password-new-password-placeholder)
        :label          (i18n/label :t/change-password-new-password-label)
        :on-change-text on-change-password
        :on-focus       on-input-focus
        :auto-focus     true}]
      [rn/view {:style style/space-between-inputs}]
      [password-with-hint
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
