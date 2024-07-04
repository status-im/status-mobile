(ns status-im.contexts.onboarding.create-password.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.constants :as constants]
    [status-im.contexts.onboarding.create-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.string :as utils.string]))

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

(defn password-with-hint
  [{{:keys [text status shown]} :hint :as input-props}]
  [rn/view
   [quo/input
    (-> input-props
        (dissoc :hint)
        (assoc :type  :password
               :blur? true))]
   [rn/view {:style style/info-message}
    (when shown
      [quo/info-message
       {:status status
        :size   :default
        :icon   (if (= status :success) :i/positive-state :i/info)
        :color  (when (= status :default)
                  colors/white-70-blur)}
       text])]])

(defn password-inputs
  [{:keys [passwords-match? on-change-password on-change-repeat-password on-input-focus
           password-long-enough? empty-password? show-password-validation?
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
     [password-with-hint
      {:hint           {:text   (i18n/label :t/password-creation-hint)
                        :status hint-1-status
                        :shown  true}
       :placeholder    (i18n/label :t/password-creation-placeholder-1)
       :on-change-text on-change-password
       :on-focus       on-input-focus
       :auto-focus     true}]
     [rn/view {:style style/space-between-inputs}]
     [password-with-hint
      {:hint           {:text   hint-2-text
                        :status hint-2-status
                        :shown  (and (not empty-password?)
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

(defn validate-password
  [password]
  (let [validations (juxt utils.string/has-lower-case?
                          utils.string/has-upper-case?
                          utils.string/has-numbers?
                          utils.string/has-symbols?
                          #(utils.string/at-least-n-chars? % constants/new-password-min-length))]
    (->> password
         validations
         (zipmap (conj constants/password-tips :long-enough?)))))

(defn calc-password-strength
  [validations]
  (->> (vals validations)
       (filter true?)
       count))

(defn- get-height-from-layout
  [laytout-event]
  (oops/oget laytout-event :nativeEvent :layout :height))

(defn- use-password-checks
  [password]
  (rn/use-memo
   (fn []
     (let [{:keys [long-enough?]
            :as   validations} (validate-password password)]
       {:password-long-enough? long-enough?
        :password-validations  validations
        :password-strength     (calc-password-strength validations)
        :empty-password?       (empty? password)}))
   [password]))

(defn- use-repeat-password-checks
  [password repeat-password]
  (rn/use-memo
   (fn []
     {:same-password-length? (and (seq password) (= (count password) (count repeat-password)))
      :same-passwords?       (and (seq password) (= password repeat-password))})
   [password repeat-password]))

(defn password-form
  []
  (let [[password set-password]                         (rn/use-state "")
        [repeat-password set-repeat-password]           (rn/use-state "")
        [accepts-disclaimer? set-accepts-disclaimer?]   (rn/use-state false)
        [focused-input set-focused-input]               (rn/use-state nil)
        [show-password-validation?
         set-show-password-validation?]                 (rn/use-state false)

        {user-color :color}                             (rf/sub [:onboarding/profile])


        {:keys [password-long-enough?
                password-validations password-strength
                empty-password?]}                       (use-password-checks password)

        {:keys [same-password-length? same-passwords?]} (use-repeat-password-checks password
                                                                                    repeat-password)

        meet-requirements?                              (rn/use-memo
                                                         #(and (not empty-password?)
                                                               (utils.string/at-least-n-chars? password
                                                                                               10)
                                                               same-passwords?
                                                               accepts-disclaimer?)
                                                         [password repeat-password
                                                          accepts-disclaimer?])

        [top-part-height set-top-part-height]           (rn/use-state 0)
        [bottom-part-height set-bottom-part-height]     (rn/use-state 0)
        on-top-part-layout                              (comp set-top-part-height
                                                              get-height-from-layout)
        on-bottom-part-layout                           (comp set-bottom-part-height
                                                              get-height-from-layout)]

    [rn/view
     {:style (style/password-form-container (+ top-part-height bottom-part-height))}
     [rn/view {:style style/top-part :on-layout on-top-part-layout}
      [header]
      [password-inputs
       {:password-long-enough?     password-long-enough?
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
                                      (set-show-password-validation? true))}]]

     ;; empty view shrink when keyboard shown
     [rn/view {:style style/middle-part}]

     [rn/view {:style style/bottom-part :on-layout on-bottom-part-layout}
      (when same-passwords?
        [rn/view {:style style/disclaimer-container}
         [quo/disclaimer
          {:blur?     true
           :on-change (partial set-accepts-disclaimer? not)
           :checked?  accepts-disclaimer?}
          (i18n/label :t/password-creation-disclaimer)]])
      (when (and (= focused-input :password) (not same-passwords?))
        [help
         {:validations       password-validations
          :password-strength password-strength}])

      [rn/view {:style style/button-container}
       [quo/button
        {:disabled?           (not meet-requirements?)
         :customization-color user-color
         :on-press            #(rf/dispatch
                                [:onboarding/password-set
                                 (security/mask-data password)])}
        (i18n/label :t/password-creation-confirm)]]]]))

(defn create-password-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/create-profile-password-info-box-title)
    :shell? true}
   [rn/view
    [quo/text {:size :paragraph-2}
     (i18n/label :t/create-profile-password-info-box-description)]]])

(defn create-password
  []
  (let [[keyboard-shown? set-keyboard-shown?] (rn/use-state false)
        {:keys [top bottom]}                  (safe-area/get-insets)
        on-press-info                         (fn []
                                                (rn/dismiss-keyboard!)
                                                (rf/dispatch [:show-bottom-sheet
                                                              {:content create-password-doc
                                                               :shell?  true}]))]

    (rn/use-mount
     (fn []
       (let [will-show-listener (oops/ocall rn/keyboard
                                            "addListener"
                                            "keyboardWillShow"
                                            #(set-keyboard-shown? true))
             will-hide-listener (oops/ocall rn/keyboard
                                            "addListener"
                                            "keyboardWillHide"
                                            #(set-keyboard-shown? false))]
         (fn []
           (oops/ocall will-show-listener "remove")
           (oops/ocall will-hide-listener "remove")))))

    [rn/touchable-without-feedback
     {:on-press   rn/dismiss-keyboard!
      :accessible false}
     [rn/keyboard-avoiding-view {:style style/flex-fill}
      [quo/page-nav
       {:margin-top top
        :background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/info
                      :on-press  on-press-info}]}]
      [rn/scroll-view
       {:content-container-style style/flex-fill}
       [password-form]]
      [rn/view {:style {:height (if-not keyboard-shown? bottom 0)}}]]]))
