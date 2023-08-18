(ns status-im2.common.standard-authentication.standard-auth.view
  (:require
    [quo2.core :as quo]
    [quo2.theme :as quo.theme]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]
    [react-native.touch-id :as biometric]
    [utils.i18n :as i18n]
    [taoensso.timbre :as log]
    [status-im2.common.standard-authentication.enter-password.view :as enter-password]
    [react-native.core :as rn]))

(defn reset-password
  []
  (rf/dispatch [:set-in [:profile/login :password] nil])
  (rf/dispatch [:set-in [:profile/login :error] ""]))

(defn authorize
  [{:keys [on-enter-password biometric-auth? on-auth-success on-auth-fail
           fallback-button-label theme on-close]}]
  (biometric/get-supported-type
   (fn [biometric-type]
     (if (and biometric-auth? biometric-type)
       (biometric/authenticate
        {:reason     (i18n/label :t/biometric-auth-confirm-message)
         :on-success (fn [response]
                       (when on-auth-success (on-auth-success response))
                       (log/info "response" response))
         :on-fail    (fn [error]
                       (log/error "Authentication Failed. Error:" error)
                       (when on-auth-fail (on-auth-fail error))
                       (rf/dispatch [:show-bottom-sheet
                                     {:theme   theme
                                      :shell?  true
                                      :content (fn []
                                                 [enter-password/view
                                                  {:on-enter-password on-enter-password}])}]))})
       (do
         (reset-password)
         (rf/dispatch [:show-bottom-sheet
                       {:on-close on-close
                        :theme    theme
                        :content  (fn []
                                    [enter-password/view
                                     {:on-enter-password on-enter-password
                                      :button-label      fallback-button-label}])}]))))))

(defn- view-internal
  [_]
  (let [reset-slider? (reagent/atom false)
        on-close      #(reset! reset-slider? true)]
    (fn [{:keys [biometric-auth?
                 track-text
                 customization-color
                 fallback-button-label
                 on-enter-password
                 on-auth-success
                 on-auth-fail
                 size
                 theme]}]
      [rn/view {:style {:flex 1}}
       [quo/slide-button
        {:size                size
         :customization-color customization-color
         :on-reset            (when @reset-slider? #(reset! reset-slider? false))
         :on-complete         #(authorize {:on-close              on-close
                                           :theme                 theme
                                           :on-enter-password     on-enter-password
                                           :biometric-auth?       biometric-auth?
                                           :on-auth-success       on-auth-success
                                           :on-auth-fail          on-auth-fail
                                           :fallback-button-label fallback-button-label})
         :track-icon          :i/face-id
         :track-text          track-text}]])))

(def view (quo.theme/with-theme view-internal))
