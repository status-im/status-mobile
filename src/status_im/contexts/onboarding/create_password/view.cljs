(ns status-im.contexts.onboarding.create-password.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.platform :as platform]
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
  [{:keys [passwords-match? on-change-password on-change-repeat-password on-input-focus
           password-long-enough? password-short-enough? empty-password? show-password-validation?
           on-blur-repeat-password]}]
  (let [hint-1-status (if password-long-enough? :success :default)
        hint-2-status (if passwords-match? :success :error)
        hint-2-text   (if passwords-match?
                        (i18n/label :t/password-creation-match)
                        (i18n/label :t/password-creation-dont-match))
        error?        (and show-password-validation?
                           (not passwords-match?)
                           (not empty-password?))]
    [:<>
     [password-with-hint/view
      {:hint           (if (not password-short-enough?)
                         {:text   (i18n/label
                                   :t/password-creation-max-length-hint)
                          :status :error
                          :shown? true}
                         {:text   (i18n/label :t/password-creation-hint)
                          :status hint-1-status
                          :shown? true})
       :placeholder    (i18n/label :t/password-creation-placeholder-1)
       :on-change-text on-change-password
       :on-focus       on-input-focus
       :auto-focus     true}]
     [rn/view {:style style/space-between-inputs}]
     [password-with-hint/view
      {:hint           {:text   hint-2-text
                        :status hint-2-status
                        :shown? (and (not empty-password?)
                                     show-password-validation?)}
       :error?         error?
       :placeholder    (i18n/label :t/password-creation-placeholder-2)
       :on-change-text on-change-repeat-password
       :on-focus       on-input-focus
       :on-blur        on-blur-repeat-password}]]))

(defn help
  [{{:keys [lower-case? upper-case? numbers? symbols?]} :validations
    password-strength                                   :password-strength}]
  [rn/view
   [quo/strength-divider {:type (constants/strength-status password-strength :info)}
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

(defn- use-password-checks
  [password]
  (rn/use-memo
   (fn []
     (let [{:keys [long-enough? short-enough?]
            :as   validations} (password/validate password)]
       {:password-long-enough?  long-enough?
        :password-short-enough? short-enough?
        :password-validations   validations
        :password-strength      (password/strength validations)
        :empty-password?        (empty? password)}))
   [password]))

(defn- use-repeat-password-checks
  [password repeat-password]
  (rn/use-memo
   (fn []
     {:same-password-length? (and (seq password) (= (count password) (count repeat-password)))
      :same-passwords?       (and (seq password) (= password repeat-password))})
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

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- page-nav
  []
  (let [{:keys [top]} (safe-area/get-insets)]
    [quo/page-nav
     {:margin-top top
      :background :blur
      :icon-name  :i/arrow-left
      :on-press   navigate-back
      :right-side [{:icon-name :i/info
                    :on-press  on-press-info}]}]))

(defn create-password
  []
  (let [[password set-password]                         (rn/use-state "")
        [repeat-password set-repeat-password]           (rn/use-state "")
        [accepts-disclaimer? set-accepts-disclaimer?]   (rn/use-state false)
        [focused-input set-focused-input]               (rn/use-state nil)
        [show-password-validation?
         set-show-password-validation?]                 (rn/use-state false)
        {user-color :color}                             (rf/sub [:onboarding/profile])


        {:keys [password-long-enough?
                password-short-enough?
                password-validations password-strength
                empty-password?]}                       (use-password-checks password)

        {:keys [same-password-length? same-passwords?]} (use-repeat-password-checks password
                                                                                    repeat-password)

        meet-requirements?                              (and (not empty-password?)
                                                             password-long-enough?
                                                             password-short-enough?
                                                             same-passwords?
                                                             accepts-disclaimer?)]

    [floating-button/view
     {:header [page-nav]
      :keyboard-should-persist-taps :handled
      :content-avoid-keyboard? true
      :automatically-adjust-keyboard-insets true
      :blur-options
      {:blur-amount        34
       :blur-radius        20
       :blur-type          :transparent
       :overlay-color      :transparent
       :background-color   (if platform/android? colors/neutral-100 colors/neutral-80-opa-1-blur)
       :padding-vertical   0
       :padding-horizontal 0}
      :footer-container-padding 0
      :footer
      [rn/view
       {:style style/footer-container}
       (when same-passwords?
         [rn/view {:style style/disclaimer-container}
          [quo/disclaimer
           {:blur?               true
            :on-change           (partial set-accepts-disclaimer? not)
            :checked?            accepts-disclaimer?
            :customization-color user-color}
           (i18n/label :t/password-creation-disclaimer)]])
       (when (and (= focused-input :password) (not same-passwords?))
         [help
          {:validations       password-validations
           :password-strength password-strength}])
       [quo/button
        {:container-style     style/footer-button-container
         :disabled?           (not meet-requirements?)
         :customization-color user-color
         :on-press            #(rf/dispatch
                                [:onboarding/password-set
                                 (security/mask-data password)])}
        (i18n/label :t/password-creation-confirm)]]}

     [rn/view {:style style/form-container}
      [header]
      [password-inputs
       {:password-long-enough?     password-long-enough?
        :password-short-enough?    password-short-enough?
        :passwords-match?          same-passwords?
        :empty-password?           empty-password?
        :show-password-validation? show-password-validation?
        :on-input-focus            #(set-focused-input :password)
        :on-change-password        (fn [new-value]
                                     (set-password new-value)
                                     (when same-password-length?
                                       (set-show-password-validation? true)))

        :on-change-repeat-password (fn [new-value]
                                     (set-repeat-password new-value)
                                     (when same-password-length?
                                       (set-show-password-validation? true)))
        :on-blur-repeat-password   #(if empty-password?
                                      (set-show-password-validation? false)
                                      (set-show-password-validation? true))}]]]))
