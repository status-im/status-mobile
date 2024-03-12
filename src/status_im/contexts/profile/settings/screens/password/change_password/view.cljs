(ns status-im.contexts.profile.settings.screens.password.change-password.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.safe-area :as safe-area]
    [status-im.constants :as constant]
    [status-im.contexts.profile.settings.screens.password.change-password.style :as style]
    [status-im.contexts.profile.settings.screens.password.events]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.string :as utils.string]))

;; TODO move styles

(defn header
  []
  [rn/view {:style style/heading}
   [quo/text
    {:style  style/heading-title
     :weight :semi-bold
     :size   :heading-1}
    ;; TODO: change text
    (i18n/label :t/change-password)]
   [quo/text
    {:style  style/heading-subtitle
     :weight :regular
     :size   :paragraph-1}
    ;; TODO: change text
    (i18n/label :t/change-password-description)]])

(defn password-with-hint
  [{{:keys [text status shown]} :hint :as input-props}]
  [:<>
   [quo/input
    (-> input-props
        (dissoc :hint)
        (assoc :type  :password
               :blur? true))]
   [rn/view {:style style/info-message}
    (when shown
      [quo/info-message
       {:type       status
        :size       :default
        :icon       (if (= status :success) :i/positive-state :i/info)
        :text-color (when (= status :default)
                      colors/white-70-blur)
        :icon-color (when (= status :default)
                      colors/white-70-blur)}
       text])]])

(def strength-status
  {1 :very-weak
   2 :weak
   3 :okay
   4 :strong
   5 :very-strong})

(defn help
  [{{:keys [lower-case? upper-case? numbers? symbols?]} :validations
    password-strength                                   :password-strength}]
  [rn/view
   [quo/strength-divider {:type (strength-status password-strength :info)}
    (i18n/label :t/password-creation-tips-title)]
   [rn/view {:style style/password-tips}
    [quo/tips {:completed? lower-case?}
     (i18n/label :t/password-creation-tips-1)]
    [quo/tips {:completed? upper-case?}
     (i18n/label :t/password-creation-tips-2)]
    [quo/tips {:completed? numbers?}
     (i18n/label :t/password-creation-tips-3)]
    [quo/tips {:completed? symbols?}
     (i18n/label :t/password-creation-tips-4)]]])

(defn password-validations
  [password]
  (let [validations (juxt utils.string/has-lower-case?
                          utils.string/has-upper-case?
                          utils.string/has-numbers?
                          utils.string/has-symbols?
                          #(utils.string/at-least-n-chars? % 10))]
    (->> password
         (validations)
         (zipmap [:lower-case? :upper-case? :numbers? :symbols? :long-enough?]))))

(defn calc-password-strength
  [validations]
  (->> (vals validations)
       (filter true?)
       (count)))

(defn- old-password-form
  [{:keys [customization-color]}]
  (let [error                   (rf/sub [:settings/change-password-error])
        [password set-password] (rn/use-state "")
        on-change-password      (fn [new-value]
                                  (when error
                                    (rf/dispatch [:password-settings/change-password-reset-error]))
                                  (set-password new-value))
        meet-requirements?      (and (seq password)
                                     (utils.string/at-least-n-chars? password
                                                                     constant/min-password-length))
        on-submit               (fn []
                                  (when meet-requirements?
                                    (rf/dispatch
                                     [:password-settings/verify-old-password
                                      (security/mask-data password)])))]
    [:<>
     [rn/view {:style style/top-part}
      [header]
      [quo/input
       {:placeholder    (i18n/label :t/change-password-old-password-placeholder)
        :label          (i18n/label :t/change-password-old-password-label)
        :on-change-text on-change-password
        :auto-focus     true
        :type           :password
        :blur?          true}]
      [rn/view
       ;; TODO move styles
       {:style {:margin-top     8
                :flex-direction :row
                :align-items    :center}}
       (when error
         [quo/info-message
          {:type :error
           :size :default
           :icon :i/info}
          (i18n/label :t/oops-wrong-password)])]]
     [rn/view {:style style/bottom-part}
      [rn/view {:style style/button-container}
       [quo/button
        {:disabled?           (not meet-requirements?)
         :customization-color customization-color
         :on-press            on-submit}
        (i18n/label :t/continue)]]]]))

(defn- new-password-form
  [{:keys [customization-color]}]
  (let [[password set-password]                        (rn/use-state "")
        [repeat-password set-repeat-password]          (rn/use-state "")
        [disclaimer-accepted? set-disclaimer-accepted] (rn/use-state false)
        [focused? set-focused]                         (rn/use-state false)
        [show-validation? set-show-validation]         (rn/use-state false)
        same-password-length?                          #(and (seq password)
                                                             (= (count password)
                                                                (count repeat-password)))
        ;;
        {:keys [long-enough?]
         :as   validations}                            (password-validations password)
        password-strength                              (calc-password-strength validations)
        empty-password?                                (empty? password)
        same-passwords?                                (and (not empty-password?)
                                                            (= password repeat-password))
        meet-requirements?                             (and (not empty-password?)
                                                            (utils.string/at-least-n-chars? password 10)
                                                            same-passwords?
                                                            disclaimer-accepted?)
        on-change-password                             (fn [new-value]
                                                         (set-password new-value)
                                                         (when (same-password-length?)
                                                           (set-show-validation true)))
        on-change-repeat-password                      (fn [new-value]
                                                         (set-repeat-password new-value)
                                                         (when (same-password-length?)
                                                           (set-show-validation true)))
        on-blur-repeat-password                        (fn []
                                                         (if empty-password?
                                                           (set-show-validation false)
                                                           (set-show-validation true)))
        on-input-focus                                 (fn [] (set-focused true))
        on-disclaimer-change                           (fn []
                                                         (set-disclaimer-accepted
                                                          (not disclaimer-accepted?)))
        ;; TODO fix
        on-submit                                      (fn []
                                                         (rf/dispatch
                                                          [:password-settings/confirm-new-password
                                                           (security/mask-data password)]))
        hint-1-status                                  (if long-enough? :success :default)
        hint-2-status                                  (if same-passwords? :success :error)
        hint-2-text                                    (if same-passwords?
                                                         (i18n/label :t/password-creation-match)
                                                         (i18n/label :t/password-creation-dont-match))
        error?                                         (and show-validation?
                                                            (not same-passwords?)
                                                            (not empty-password?))]
    [:<>
     [rn/view {:style style/top-part}
      [header]
      [password-with-hint
       {:hint           {:text   (i18n/label :t/password-creation-hint)
                         :status hint-1-status
                         :shown  true}
        :placeholder    (i18n/label :t/change-password-new-password-placeholder)
        :label          (i18n/label :t/change-password-new-password-label)
        :on-change-text on-change-password
        :on-focus       on-input-focus
        :auto-focus     true}]
      [rn/view {:style style/space-between-inputs}]
      [password-with-hint
       {:hint           {:text   hint-2-text
                         :status hint-2-status
                         :shown  (and (not empty-password?)
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
          {:blur?     true
           :on-change on-disclaimer-change
           :checked?  disclaimer-accepted?}
          (i18n/label :t/password-creation-disclaimer)]])
      (when (and focused? (not same-passwords?))
        [help
         {:validations       validations
          :password-strength password-strength}])
      [rn/view {:style style/button-container}
       [quo/button
        {:disabled?           (not meet-requirements?)
         :customization-color customization-color
         :on-press            on-submit}
        (i18n/label :t/password-creation-confirm)]]]]))

(defn view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)
        {:keys [top bottom]}     (safe-area/get-insets)
        change-password-step     (or (rf/sub
                                      [:settings/change-password-current-step])
                                     :old-password)
        customization-color      (rf/sub [:profile/customization-color])]
    (rn/use-unmount #(rf/dispatch [:password-settings/reset-change-password]))
    [rn/touchable-without-feedback
     {:on-press   rn/dismiss-keyboard!
      :accessible false}
     [quo/overlay {:type :shell}
      [rn/view {:style style/flex-fill}
       [quo/page-nav
        {:margin-top top
         :background :blur
         :icon-name  :i/arrow-left
         :on-press   #(rf/dispatch [:navigate-back])}]
       [rn/keyboard-avoiding-view {:style style/flex-fill}
        (condp = change-password-step
          :old-password [old-password-form
                         {:customization-color customization-color}]
          :new-password [new-password-form
                         {:customization-color customization-color}])
        [rn/view {:style {:height (if-not keyboard-shown bottom 0)}}]]]]]))
