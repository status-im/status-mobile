(ns status-im.contexts.settings.privacy-and-security.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.settings.privacy-and-security.profile-picture.view :as profile-picture.view]
    [status-im.contexts.settings.privacy-and-security.style :as style]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.navigation :as navigation]
    [utils.re-frame :as rf]))

(defn- setting-preview-privacy
  [preview-privacy? customization-color on-change]
  {:title        (i18n/label (if platform/android?
                               :t/hide-content-when-switching-apps
                               :t/hide-content-when-switching-apps-ios))
   :blur?        true
   :action       :selector
   :action-props {:on-change           on-change
                  :checked?            preview-privacy?
                  :id                  :preview-privacy
                  :customization-color customization-color}})

(defn view
  []
  (let [insets (safe-area/get-insets)
        centralized-metrics-enabled? (rf/sub [:centralized-metrics/enabled?])
        customization-color (rf/sub [:profile/customization-color])

        preview-privacy? (rf/sub [:profile/preview-privacy?])
        see-profile-pictures-from (rf/sub [:profile/pictures-visibility])
        show-profile-pictures-to (rf/sub [:multiaccount/profile-pictures-show-to])

        toggle-preview-privacy
        (rn/use-callback (fn []
                           (rf/dispatch [:profile.settings/change-preview-privacy
                                         (not preview-privacy?)]))
                         [preview-privacy?])

        open-see-profile-pictures-from-options
        (rn/use-callback (fn []
                           (rf/dispatch
                            [:show-bottom-sheet
                             {:theme   :dark
                              :blur?   true
                              :content (fn []
                                         [profile-picture.view/options-for-see-profile-pictures-from
                                          see-profile-pictures-from])}]))
                         [see-profile-pictures-from])

        open-show-profile-pictures-to-options
        (rn/use-callback (fn []
                           (rf/dispatch
                            [:show-bottom-sheet
                             {:theme   :dark
                              :blur?   true
                              :content (fn []
                                         [profile-picture.view/options-for-show-profile-picture-to
                                          show-profile-pictures-to])}]))
                         [show-profile-pictures-to])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigation/navigate-back}]
     [quo/standard-title
      {:title               (i18n/label :t/privacy-and-security)
       :container-style     style/title-container
       :accessibility-label :privacy-and-security-header
       :customization-color customization-color}]
     [quo/category
      {:key       :category
       :data      [(when (ff/enabled? ::ff/profile-pictures-visibility)
                     (profile-picture.view/setting-see-profile-pictures-from
                      see-profile-pictures-from
                      open-see-profile-pictures-from-options))
                   (when (ff/enabled? ::ff/profile-pictures-visibility)
                     (profile-picture.view/setting-show-your-profile-pictures-to
                      show-profile-pictures-to
                      open-show-profile-pictures-to-options))
                   (setting-preview-privacy preview-privacy? customization-color toggle-preview-privacy)
                   {:title        (i18n/label :t/share-usage-data)
                    :image-props  :i/placeholder
                    :blur?        true
                    :action       :selector
                    :action-props {:on-change #(rf/dispatch
                                                [:centralized-metrics/toggle-centralized-metrics
                                                 (not centralized-metrics-enabled?)])
                                   :customization-color customization-color
                                   :checked? centralized-metrics-enabled?}
                    :on-press     identity}]
       :blur?     true
       :list-type :settings}]]))
