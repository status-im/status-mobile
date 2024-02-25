(ns status-im.common.standard-authentication.password-input.view
  (:require
    [clojure.string :as string]
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

(defn get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? error "failed to set ")
               (string/starts-with? error "Failed")))
    (i18n/label :t/oops-wrong-password)
    error))

(defn- on-change-password
  [entered-password]
  (debounce/debounce-and-dispatch [:profile/on-password-input-changed
                                   {:password (security/mask-data entered-password)
                                    :error    ""}]
                                  100))

(defn- view-internal
  [{:keys [default-password theme shell? on-press-biometrics blur?]}]
  (let [{:keys [error processing]} (rf/sub [:profile/login])
        error-message              (get-error-message error)
        error?                     (boolean (seq error-message))]
    [:<>
     [rn/view {:style {:flex-direction :row}}
      [quo/input
       {:container-style {:flex 1}
        :type            :password
        :blur?           blur?
        :disabled?       processing
        :placeholder     (i18n/label :t/type-your-password)
        :auto-focus      true
        :error?          error?
        :label           (i18n/label :t/profile-password)
        :on-change-text  on-change-password
        :default-value   (security/safe-unmask-data default-password)}]
      (when on-press-biometrics
        [quo/button
         {:container-style style/auth-button
          :on-press        on-press-biometrics
          :icon-only?      true
          :background      (when blur? :blur)
          :type            :outline}
         :i/face-id])]
     (when error?
       [rn/view {:style style/error-message}
        [quo/info-message
         {:type :error
          :size :default
          :icon :i/info}
         error-message]
        [rn/pressable
         {:hit-slop {:top 6 :bottom 20 :left 0 :right 0}
          :disabled processing
          :on-press (fn []
                      (rn/dismiss-keyboard!)
                      (rf/dispatch [:show-bottom-sheet
                                    {:content #(forgot-password-doc/view {:shell? shell?})
                                     :theme   theme
                                     :shell?  shell?}]))}
         [rn/text
          {:style                 {:text-decoration-line :underline
                                   :color                (colors/resolve-color :danger theme)}
           :size                  :paragraph-2
           :suppress-highlighting true}
          (i18n/label :t/forgot-password)]]])]))

(def view (quo.theme/with-theme view-internal))
