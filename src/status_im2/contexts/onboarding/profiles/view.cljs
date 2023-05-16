(ns status-im2.contexts.onboarding.profiles.view
  (:require [native-module.core :as native-module]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.confirmation-drawer.view :as confirmation-drawer]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.profiles.style :as style]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [utils.transforms :as types]))

(def show-profiles? (reagent/atom false))

(defn login-multiaccount
  []
  (rf/dispatch [:multiaccounts.login.ui/password-input-submitted]))

(defn new-account-options
  []
  [quo/action-drawer
   [[{:icon                :i/profile
      :label               (i18n/label :t/create-new-profile)
      :on-press            #(rf/dispatch [:navigate-to :new-to-status])
      :accessibility-label :create-new-profile}
     {:icon                :i/multi-profile
      :label               (i18n/label :t/add-existing-status-profile)
      :on-press            #(rf/dispatch [:navigate-to :sign-in])
      :accessibility-label :multi-profile}]]])

(defn show-new-account-options
  []
  (rf/dispatch [:show-bottom-sheet {:content new-account-options}]))

(defn delete-profile-confirmation
  [key-uid context]
  [confirmation-drawer/confirmation-drawer
   {:title               (i18n/label :remove-profile?)
    :description         (i18n/label :remove-profile-confirm-message)
    :accessibility-label :remove-profile-confirm
    :context             context
    :button-text         (i18n/label :t/remove)
    :close-button-text   (i18n/label :t/cancel)
    :on-press            #(do
                            (rf/dispatch [:hide-bottom-sheet])
                            (native-module/delete-multiaccount
                             key-uid
                             (fn [result]
                               (let [{:keys [error]} (types/json->clj result)]
                                 (rf/dispatch [:onboarding-2/on-delete-profile-success key-uid])
                                 (log/info "profile deleted: error" error)))))}])

(defn show-confirmation
  [key-uid context]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [delete-profile-confirmation key-uid context])}]))

(defn profile-options
  [key-uid context]
  [quo/action-drawer
   [[{:icon                :i/delete
      :label               (i18n/label :remove-profile-message)
      :on-press            #(show-confirmation key-uid context)
      :accessibility-label :remove-profile
      :danger?             true}]]])

(defn show-profile-options
  [key-uid context]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [profile-options key-uid context])}]))

(defn profile-card
  [{:keys [name key-uid customization-color keycard-pairing last-index] :as multiaccount} index]
  (let [last-item?      (= last-index index)
        profile-picture (:uri (first (:images multiaccount)))]
    [quo/profile-card
     {:name                 name
      :login-card?          true
      :last-item?           (= last-index index)
      :customization-color  (or customization-color :primary)
      :keycard-account?     keycard-pairing
      :show-options-button? true
      :profile-picture      (when profile-picture {:uri profile-picture})
      :card-style           (style/profiles-profile-card last-item?)
      :on-options-press     #(show-profile-options
                              key-uid
                              {:name            name
                               :color           customization-color
                               :profile-picture profile-picture})
      :on-card-press        #(do
                               (rf/dispatch
                                [:multiaccounts.login.ui/multiaccount-selected key-uid])
                               (when-not keycard-pairing (reset! show-profiles? false)))}]))

(defn profiles-section
  []
  (let [multiaccounts (vals (rf/sub [:multiaccounts/multiaccounts]))
        multiaccounts (map #(assoc % :last-index (- (count multiaccounts) 1)) multiaccounts)]
    [rn/view
     {:style style/profiles-container}
     [rn/view
      {:style style/profiles-header}
      [quo/text
       {:size   :heading-1
        :weight :semi-bold
        :style  style/profiles-header-text}
       (i18n/label :t/profiles-on-device)]
      [quo/button
       {:type                :primary
        :size                32
        :icon                true
        :on-press            show-new-account-options
        :accessibility-label :show-new-account-options
        :override-theme      :dark}
       :main-icons/add]]
     [rn/flat-list
      {:data                    (sort-by :timestamp > multiaccounts)
       :key-fn                  :key-uid
       :content-container-style {:padding-bottom 20}
       :render-fn               profile-card}]]))

(defn forget-password-doc
  []
  [quo/documentation-drawers
   {:title  (i18n/label :t/forgot-your-password-info-title)
    :shell? true}
   [rn/view
    {:style style/forget-password-doc-container}
    [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-description)]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true :override-theme :dark} 1]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-remove-app)]
      [quo/text {:size :paragraph-2} (i18n/label :t/forgot-your-password-info-remove-app-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true :override-theme :dark} 2]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-reinstall-app)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-reinstall-app-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true :override-theme :dark} 3]
     [rn/view
      {:style style/forget-password-step-content}
      [rn/view
       {:style style/forget-password-step-title}
       [quo/text {:size :paragraph-2} (str (i18n/label :t/sign-up) " ")]
       [quo/text {:size :paragraph-2 :weight :semi-bold}
        (i18n/label :t/forgot-your-password-info-signup-with-key)]]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-signup-with-key-description)]]]

    [rn/view {:style style/forget-password-step-container}
     [quo/step {:in-blur-view? true :override-theme :dark} 4]
     [rn/view
      {:style style/forget-password-step-content}
      [quo/text {:size :paragraph-2 :weight :semi-bold}
       (i18n/label :t/forgot-your-password-info-create-new-password)]
      [quo/text {:size :paragraph-2}
       (i18n/label :t/forgot-your-password-info-create-new-password-description)]]]]])

(defn login-section
  []
  (let [{:keys [name customization-color error processing]
         :as   multiaccount} (rf/sub [:multiaccounts/login])
        sign-in-enabled?     (rf/sub [:sign-in-enabled?])
        profile-picture      (:uri (first (:images multiaccount)))]
    [rn/keyboard-avoiding-view
     {:style style/login-container}
     [quo/button
      {:size                32
       :type                :blur-bg
       :icon                true
       :on-press            #(reset! show-profiles? true)
       :override-theme      :dark
       :width               32
       :accessibility-label :show-profiles
       :style               style/multi-profile-button}
      :i/multi-profile]
     [rn/scroll-view
      {:keyboard-should-persist-taps :always
       :style                        {:flex 1}}
      [quo/profile-card
       {:name                name
        :customization-color (or customization-color :primary)
        :profile-picture     (when profile-picture {:uri profile-picture})
        :card-style          style/login-profile-card}]
      [quo/input
       {:type              :password
        :blur?             true
        :override-theme    :dark
        :placeholder       (i18n/label :t/type-your-password)
        :auto-focus        true
        :error?            (when (not-empty error) error)
        :label             (i18n/label :t/profile-password)
        :secure-text-entry true
        :on-change-text    (fn [password]
                             (rf/dispatch [:set-in [:multiaccounts/login :password]
                                           (security/mask-data password)])
                             (rf/dispatch [:set-in [:multiaccounts/login :error] ""]))
        :on-submit-editing (when sign-in-enabled? login-multiaccount)}]
      (when (not-empty error)
        [quo/info-message
         {:type  :error
          :size  :default
          :icon  :i/info
          :style style/info-message}
         error])]
     [quo/button
      {:size                40
       :type                :ghost
       :before              :i/info
       :accessibility-label :forget-password-button
       :override-theme      :dark
       :style               style/forget-password-button
       :on-press            #(rf/dispatch [:show-bottom-sheet
                                           {:content forget-password-doc :shell? true}])}
      (i18n/label :t/forgot-password)]
     [quo/button
      {:size                40
       :type                :primary
       :customization-color (or customization-color :primary)
       :accessibility-label :login-button
       :override-theme      :dark
       :before              :i/unlocked
       :disabled            (or (not sign-in-enabled?) processing)
       :on-press            login-multiaccount
       :style               (style/login-button)}
      (i18n/label :t/log-in)]]))

(defn views
  []
  (reset! show-profiles? false)
  (fn []
    [:<>
     [background/view true]
     (if @show-profiles?
       [profiles-section]
       [login-section])]))
