(ns status-im.contexts.profile.edit.name.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.common.validation.profile :as profile-validator]
            [status-im.constants :as constants]
            [status-im.contexts.profile.edit.name.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [insets                   (safe-area/get-insets)
        profile                  (rf/sub [:profile/profile-with-image])
        customization-color      (rf/sub [:profile/customization-color])
        display-name             (profile.utils/displayed-name profile)
        alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
        full-name                (reagent/atom display-name)
        error-msg                (reagent/atom nil)
        typing?                  (reagent/atom false)
        validate-name            (debounce/debounce (fn [name]
                                                      (reset! error-msg
                                                        (profile-validator/validation-name name))
                                                      (reset! typing? false))
                                                    300)
        on-change-text           (fn [s]
                                   (reset! typing? true)
                                   (reset! full-name s)
                                   (validate-name s))]
    (fn []
      [quo/overlay
       {:type            :shell
        :container-style (style/page-wrapper insets)}
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
         [quo/text-combinations {:title (i18n/label :t/name)}]
         [quo/input
          {:blur?           true
           :error?          (not (string/blank? @error-msg))
           :container-style {:margin-bottom -11}
           :default-value   @full-name
           :auto-focus      true
           :char-limit      constants/profile-name-max-length
           :label           (i18n/label :t/profile-name)
           :on-change-text  on-change-text}]
         (when-not (string/blank? @error-msg)
           [quo/info-message
            {:status :error
             :size   :default
             :icon   :i/info}
            @error-msg])]
        [rn/view {:style style/button-wrapper}
         [quo/button
          {:type                :primary
           :customization-color customization-color
           :on-press            (fn []
                                  (rf/dispatch [:profile/edit-name @full-name]))
           :disabled?           (boolean (or @typing?
                                             (string/blank? @full-name)
                                             (not (string/blank? @error-msg))))}
          (i18n/label :t/save-name)]]]])))
