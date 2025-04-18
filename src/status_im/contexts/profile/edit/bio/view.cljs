(ns status-im.contexts.profile.edit.bio.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im.common.validation.profile :as profile-validator]
            [status-im.constants :as constants]
            [status-im.contexts.profile.edit.bio.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile                   (rf/sub [:profile/profile-with-image])
        customization-color       (rf/sub [:profile/customization-color])
        alert-banners-top-margin  (rf/sub [:alert-banners/top-margin])
        current-bio               (:bio profile)
        [bio set-bio]             (rn/use-state current-bio)
        [error-msg set-error-msg] (rn/use-state nil)
        [typing? set-typing]      (rn/use-state false)
        validate-bio              (debounce/debounce (fn [bio]
                                                       (set-error-msg (profile-validator/validation-bio
                                                                       bio))
                                                       (set-typing false))
                                                     300)
        on-change-text            (fn [s]
                                    (set-typing true)
                                    (set-bio s)
                                    (validate-bio s))]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper safe-area/insets)}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]
     [rn/keyboard-avoiding-view
      {:key                      :content
       :keyboard-vertical-offset (if platform/ios? alert-banners-top-margin 0)
       :style                    style/screen-container}
      [rn/view {:style {:gap 22}}
       [quo/text-combinations {:title (i18n/label :t/bio)}]
       [quo/input
        {:blur?           true
         :multiline?      true
         :error?          (not (string/blank? error-msg))
         :container-style {:margin-bottom -11}
         :default-value   bio
         :auto-focus      true
         :max-height      200
         :char-limit      constants/profile-bio-max-length
         :label           (i18n/label :t/profile-bio)
         :placeholder     (i18n/label :t/something-about-you)
         :on-change-text  on-change-text}]
       (when-not (string/blank? error-msg)
         [quo/info-message
          {:status :error
           :size   :default
           :icon   :i/info}
          error-msg])]
      [rn/view {:style style/button-wrapper}
       [quo/button
        {:type                :primary
         :customization-color customization-color
         :on-press            (fn []
                                (rf/dispatch [:profile/edit-bio bio]))
         :disabled?           (boolean (or typing?
                                           (and (string/blank? current-bio)
                                                (string/blank? bio))
                                           (not (string/blank? error-msg))))}
        (i18n/label :t/save-bio)]]]]))
