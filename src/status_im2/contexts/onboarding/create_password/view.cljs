(ns status-im2.contexts.onboarding.create-password.view
  (:require [oops.core :refer [ocall]]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.create-password.style :as style]
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
   [rn/view {:style style/label-container}
    (when shown
      [:<>
       [quo/icon (if (= status :success) :i/check-circle :i/info)
        {:container-style style/label-icon
         :color           (style/label-icon-color status)
         :size            16}]
       [quo/text
        {:style (style/label-color status)
         :size  :paragraph-2}
        text]])]])

(defn password-inputs
  [{:keys [passwords-match? on-change-password on-change-repeat-password on-input-focus
           password-long-enough? empty-password? show-password-validation?
           on-blur-repeat-password]}]
  (let [hint-1-status (if password-long-enough? :success :neutral)
        hint-2-status (if passwords-match? :success :danger)
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

(defn password-form
  []
  (let [password                  (reagent/atom "")
        repeat-password           (reagent/atom "")
        accepts-disclaimer?       (reagent/atom false)
        focused-input             (reagent/atom nil)
        show-password-validation? (reagent/atom false)
        same-password-length?     #(and (seq @password)
                                        (= (count @password) (count @repeat-password)))]
    (fn []
      (let [{user-color :color} (rf/sub [:onboarding-2/profile])
            {:keys [long-enough?]
             :as   validations} (password-validations @password)
            password-strength   (calc-password-strength validations)
            empty-password?     (empty? @password)
            same-passwords?     (and (not empty-password?) (= @password @repeat-password))
            meet-requirements?  (and (not empty-password?)
                                     (utils.string/at-least-n-chars? @password 10)
                                     same-passwords?
                                     @accepts-disclaimer?)]
        [:<>
         [rn/view {:style style/top-part}
          [header]
          [password-inputs
           {:password-long-enough?     long-enough?
            :passwords-match?          same-passwords?
            :empty-password?           empty-password?
            :show-password-validation? @show-password-validation?
            :on-input-focus            #(reset! focused-input :password)
            :on-change-password        (fn [new-value]
                                         (reset! password new-value)
                                         (when (same-password-length?)
                                           (reset! show-password-validation? true)))
            :on-change-repeat-password (fn [new-value]
                                         (reset! repeat-password new-value)
                                         (when (same-password-length?)
                                           (reset! show-password-validation? true)))
            :on-blur-repeat-password   #(if empty-password?
                                          (reset! show-password-validation? false)
                                          (reset! show-password-validation? true))}]]

         [rn/view {:style style/bottom-part}
          (when same-passwords?
            [rn/view {:style style/disclaimer-container}
             [quo/disclaimer
              {:blur?     true
               :on-change #(swap! accepts-disclaimer? not)
               :checked?  @accepts-disclaimer?}
              (i18n/label :t/password-creation-disclaimer)]])
          (when (and (= @focused-input :password) (not same-passwords?))
            [help
             {:validations       validations
              :password-strength password-strength}])

          [rn/view {:style style/button-container}
           [quo/button
            {:disabled?           (not meet-requirements?)
             :customization-color user-color
             :on-press            #(rf/dispatch
                                    [:onboarding-2/password-set
                                     (security/mask-data @password)])}
            (i18n/label :t/password-creation-confirm)]]]]))))

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
  (reagent/with-let [keyboard-shown?      (reagent/atom false)
                     {:keys [top bottom]} (safe-area/get-insets)
                     will-show-listener   (ocall rn/keyboard
                                                 "addListener"
                                                 "keyboardWillShow"
                                                 #(reset! keyboard-shown? true))
                     will-hide-listener   (ocall rn/keyboard
                                                 "addListener"
                                                 "keyboardWillHide"
                                                 #(reset! keyboard-shown? false))
                     on-press-info        (fn []
                                            (rn/dismiss-keyboard!)
                                            (rf/dispatch [:show-bottom-sheet
                                                          {:content create-password-doc
                                                           :shell?  true}]))]
    [:<>
     [rn/touchable-without-feedback
      {:on-press   rn/dismiss-keyboard!
       :accessible false}
      [rn/view {:style style/flex-fill}
       [rn/keyboard-avoiding-view {:style style/flex-fill}
        [quo/page-nav
         {:margin-top top
          :background :blur
          :icon-name  :i/arrow-left
          :on-press   #(rf/dispatch [:navigate-back-within-stack :new-to-status])
          :right-side [{:icon-name :i/info
                        :on-press  on-press-info}]}]
        [password-form]
        [rn/view {:style {:height (if-not @keyboard-shown? bottom 0)}}]]]]]
    (finally
     (ocall will-show-listener "remove")
     (ocall will-hide-listener "remove"))))
