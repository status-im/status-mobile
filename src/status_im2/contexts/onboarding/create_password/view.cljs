(ns status-im2.contexts.onboarding.create-password.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.onboarding.common.background.view :as background]
    [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
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
       :on-focus       on-input-focus}]
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
  [{:keys [scroll-to-end-fn]}]
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
            same-passwords?     (= @password @repeat-password)
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
            :on-input-focus            (fn []
                                         (scroll-to-end-fn)
                                         (reset! focused-input :password))
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
          [rn/view {:style style/disclaimer-container}
           [quo/disclaimer
            {:on-change #(reset! accepts-disclaimer? %)
             :checked?  @accepts-disclaimer?}
            (i18n/label :t/password-creation-disclaimer)]]

          (when (= @focused-input :password)
            [help
             {:validations       validations
              :password-strength password-strength}])

          [rn/view {:style style/button-container}
           [quo/button
            {:disabled                  (not meet-requirements?)
             :override-background-color (colors/custom-color user-color 60)
             :on-press                  #(rf/dispatch
                                          [:onboarding-2/password-set
                                           (security/mask-data @password)])}
            (i18n/label :t/password-creation-confirm)]]]]))))

(defn create-password
  []
  [:f>
   (let [scroll-view-ref  (atom nil)
         scroll-to-end-fn #(js/setTimeout ^js/Function (.-scrollToEnd @scroll-view-ref) 250)]
     (fn []
       (let [{:keys [top]} (safe-area/use-safe-area)]
         [:<>
          [background/view true]
          [rn/scroll-view
           {:ref                     #(reset! scroll-view-ref %)
            :style                   style/overlay
            :content-container-style style/content-style}
           [navigation-bar/navigation-bar
            {:top                   top
             :right-section-buttons [{:type                :blur-bg
                                      :icon                :i/info
                                      :icon-override-theme :dark
                                      :on-press            #(js/alert "Info pressed")}]}]
           [password-form {:scroll-to-end-fn scroll-to-end-fn}]]])))])
