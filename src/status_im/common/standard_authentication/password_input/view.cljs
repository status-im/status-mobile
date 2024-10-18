(ns status-im.common.standard-authentication.password-input.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.forgot-password-doc.view :as forgot-password-doc]
    [status-im.common.standard-authentication.password-input.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- error-info
  [error-message processing shell?]
  (let [theme    (quo.theme/use-theme)
        on-press (rn/use-callback
                  (fn []
                    (rn/dismiss-keyboard!)
                    (rf/dispatch [:show-bottom-sheet
                                  {:content #(forgot-password-doc/view {:shell? shell?})
                                   :theme   theme
                                   :shell?  shell?}]))
                  [theme])]
    [rn/view {:style style/error-message}
     [quo/info-message
      {:status :error
       :size   :default
       :icon   :i/info}
      error-message]
     [rn/pressable
      {:hit-slop {:top 6 :bottom 20 :left 0 :right 0}
       :disabled processing
       :on-press on-press}
      [rn/text
       {:style                 {:text-decoration-line :underline
                                :color                (colors/resolve-color :danger theme)}
        :size                  :paragraph-2
        :suppress-highlighting true}
       (i18n/label :t/forgot-password)]]]))

(defn view
  [{:keys [shell? on-press-biometrics blur? processing error]}]
  (let [{:keys [error?
                error-message]} error
        default-value           (rn/use-ref-atom "") ;;bug on Android
                                                     ;;https://github.com/status-im/status-mobile/issues/19004
        theme                   (quo.theme/use-theme)
        on-change-password      (rn/use-callback
                                 (fn [entered-password]
                                   (reset! default-value entered-password)
                                   (debounce/debounce-and-dispatch
                                    [:profile/on-password-input-changed
                                     {:password (security/mask-data
                                                 entered-password)
                                      :error    ""}]
                                    100)))]
    [:<>
     [rn/view {:style {:flex-direction :row}}
      [quo/input
       {:container-style {:flex 1}
        :type            :password
        :theme           theme
        :default-value   @default-value
        :blur?           blur?
        :disabled?       processing
        :placeholder     (i18n/label :t/type-your-password)
        :auto-focus      true
        :error?          error?
        :label           (i18n/label :t/profile-password)
        :on-change-text  on-change-password}]
      (when on-press-biometrics
        [quo/button
         {:container-style style/auth-button
          :on-press        on-press-biometrics
          :icon-only?      true
          :background      (when blur? :blur)
          :type            :outline}
         :i/face-id])]
     (when error?
       [error-info error-message processing shell?])]))
