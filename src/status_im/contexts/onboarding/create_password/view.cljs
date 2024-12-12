(ns status-im.contexts.onboarding.create-password.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button]
    [status-im.common.password-with-hint.view :as password-with-hint]
    [status-im.common.validation.password :as password]
    [status-im.constants :as constants]
    [status-im.contexts.onboarding.create-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn header
  []
  [rn/view {:style style/heading}
   [quo/text
    {:style  style/heading-title
     :weight :semi-bold
     :size   :heading-1}
    (i18n/label :t/password-creation-title)]
   [quo/text
    {:style  style/heading-subtitle
     :weight :regular
     :size   :paragraph-1}
    (i18n/label :t/password-creation-subtitle)]])

(defn password-inputs
  [{:keys [set-password set-repeat-password same-password-length? same-passwords?
           password-long-enough? password-short-enough? non-empty-password?]}]
  (let [[show-validation?
         set-show-validation?]    (rn/use-state false)
        on-change-password        (rn/use-callback
                                   (fn [new-value]
                                     (set-password new-value)
                                     (when same-password-length?
                                       (set-show-validation? true)))
                                   [same-password-length?])
        on-change-repeat-password (rn/use-callback
                                   (fn [new-value]
                                     (set-repeat-password new-value)
                                     (when same-password-length?
                                       (set-show-validation? true)))
                                   [same-password-length?])
        on-blur-repeat-password   (rn/use-callback
                                   #(set-show-validation? non-empty-password?)
                                   [non-empty-password?])
        hint-1-status             (if password-long-enough? :success :default)
        hint-2-status             (if same-passwords? :success :error)
        hint-2-text               (if same-passwords?
                                    (i18n/label :t/password-creation-match)
                                    (i18n/label :t/password-creation-dont-match))
        error?                    (and show-validation?
                                       (not same-passwords?)
                                       non-empty-password?)]
    [:<>
     [password-with-hint/view
      {:hint           (if (not password-short-enough?)
                         {:text   (i18n/label :t/password-creation-max-length-hint)
                          :status :error
                          :shown? true}
                         {:text   (i18n/label :t/password-creation-hint)
                          :status hint-1-status
                          :shown? true})
       :placeholder    (i18n/label :t/password-creation-placeholder-1)
       :on-change-text on-change-password
       :auto-focus     true}]
     [rn/view {:style style/space-between-inputs}]
     [password-with-hint/view
      {:hint           {:text   hint-2-text
                        :status hint-2-status
                        :shown? (and non-empty-password? show-validation?)}
       :error?         error?
       :placeholder    (i18n/label :t/password-creation-placeholder-2)
       :on-change-text on-change-repeat-password
       :on-blur        on-blur-repeat-password}]]))

(defn help
  [{:keys [lower-case? upper-case? numbers? symbols?] :as validations}]
  (let [password-strength (constants/strength-status (password/strength validations) :info)]
    [rn/view
     [quo/strength-divider {:type password-strength}
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

(defn- use-password-checks
  [password]
  (rn/use-memo
   (fn []
     (-> password
         (password/validate)
         (assoc :non-empty? (seq password))))
   [password]))

(defn- use-repeat-password-checks
  [password repeat-password]
  (rn/use-memo
   (fn []
     {:same-password-length? (and (seq password)
                                  (= (count password) (count repeat-password)))
      :same-passwords?       (and (seq password)
                                  (= password repeat-password))})
   [password repeat-password]))

(defn create-password-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/create-profile-password-info-box-title)
    :shell? true}
   [rn/view
    [quo/text {:size :paragraph-2}
     (i18n/label :t/create-profile-password-info-box-description)]]])

(defn- on-press-info
  []
  (rn/dismiss-keyboard!)
  (rf/dispatch [:show-bottom-sheet
                {:content create-password-doc
                 :shell?  true}]))

(defn- navigate-back [] (rf/dispatch [:navigate-back]))

(defn- page-nav
  [top]
  [quo/page-nav
   {:margin-top top
    :background :blur
    :icon-name  :i/arrow-left
    :on-press   navigate-back
    :right-side [{:icon-name :i/info
                  :on-press  on-press-info}]}])

(defn- help-and-confirm-button
  [{:keys [password-validations same-passwords? on-submit]}]
  (let [{customization-color :color} (rf/sub [:onboarding/profile])
        all-requirements-met?        (and (:non-empty? password-validations)
                                          (:long-enough? password-validations)
                                          (:short-enough? password-validations)
                                          same-passwords?)]
    [rn/view {:style style/footer-container}
     [help password-validations]
     [quo/button
      {:container-style     style/footer-button-container
       :disabled?           (not all-requirements-met?)
       :customization-color customization-color
       :on-press            on-submit}
      (i18n/label :t/password-creation-confirm)]]))

(defn- on-confirm-password
  [password]
  (rf/dispatch [:onboarding/password-set (security/mask-data password)]))

(defn create-password
  []
  (let [[password set-password]      (rn/use-state "")
        [repeat-password
         set-repeat-password]        (rn/use-state "")
        {:keys [long-enough? short-enough? non-empty?]
         :as   password-validations} (use-password-checks password)
        {:keys [same-password-length?
                same-passwords?]}    (use-repeat-password-checks password repeat-password)
        on-submit                    (rn/use-callback
                                      #(on-confirm-password password)
                                      [password])
        top                          (safe-area/get-top)]
    [floating-button/view
     {:header                               [page-nav top]
      :initial-header-height                (+ style/page-nav-height top)
      :keyboard-should-persist-taps         :handled
      :content-avoid-keyboard?              true
      :automatically-adjust-keyboard-insets true
      :blur-options                         style/blur-options
      :footer-container-padding             0
      :footer                               [help-and-confirm-button
                                             {:password-validations password-validations
                                              :same-passwords?      same-passwords?
                                              :on-submit            on-submit}]}
     [rn/view {:style style/form-container}
      [header]
      [password-inputs
       {:password-long-enough?  long-enough?
        :password-short-enough? short-enough?
        :non-empty-password?    non-empty?
        :same-passwords?        same-passwords?
        :same-password-length?  same-password-length?
        :set-password           set-password
        :set-repeat-password    set-repeat-password}]]]))
