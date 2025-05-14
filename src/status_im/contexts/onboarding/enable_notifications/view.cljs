(ns status-im.contexts.onboarding.enable-notifications.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.enable-notifications.style :as style]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn page-title
  []
  [quo/text-combinations
   {:container-style                 style/page-title
    :title                           (i18n/label :t/intro-wizard-title6)
    :title-accessibility-label       :notifications-title
    :description                     (i18n/label :t/enable-notifications-sub-title)
    :description-accessibility-label :notifications-sub-title}])

(defn on-notifications-setup-start
  [params]
  (rf/dispatch [:onboarding/notifications-setup-start params]))

(defn notifications-info-view
  [{:keys [blur?]}]
  [quo/documentation-drawers
   {:title        (i18n/label :t/enable-notifications)
    :show-button? true
    :shell?       blur?
    :button-label (i18n/label :t/read-more)
    :button-icon  :i/info}
   [quo/text (i18n/label :t/enable-notifications-info-description)]])

(defn on-open-info
  [{:keys [blur? theme]
    :or   {blur? true}}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn []
                            [notifications-info-view {:blur? blur?}])
                 :theme   theme
                 :shell?  blur?}]))

(defn enable-notification-form
  [{:keys [insets params]}]
  (let [profile-color             (rf/sub [:onboarding/customization-color
                                           {:onboarding? (:onboarding? params)}])
        [third-party-checked?
         set-third-party-checked] (rn/use-state
                                   (boolean? (ff/enabled? ::ff/settings.news-notifications)))
        on-enable-notifications   (rn/use-callback
                                   (fn []
                                     (on-notifications-setup-start
                                      (assoc params
                                             :enable-notifications?      true
                                             :enable-news-notifications? third-party-checked?)))
                                   [params third-party-checked?])
        on-skip-notifications     (rn/use-callback
                                   (fn []
                                     (on-notifications-setup-start
                                      (assoc params
                                             :enable-notifications?      false
                                             :enable-news-notifications? false)))
                                   [params])]
    [rn/view
     (when (and platform/android?
                (ff/enabled? ::ff/settings.news-notifications))
       [rn/view
        {:style style/news-notifications-checkbox-container}
        [quo/selectors
         {:type                :checkbox
          :blur?               true
          :customization-color profile-color
          :checked?            third-party-checked?
          :on-change           set-third-party-checked}]
        [quo/text
         {:size  :paragraph-2
          :style style/news-notifications-checkbox-text}
         (i18n/label :t/enable-news-notifications-third-party)]])
     [rn/view {:style (style/buttons insets)}
      [quo/button
       {:on-press            on-enable-notifications
        :type                :primary
        :icon-left           :i/notifications
        :accessibility-label :enable-notifications-button
        :customization-color profile-color}
       (i18n/label :t/intro-wizard-title6)]
      [quo/button
       {:on-press            on-skip-notifications
        :accessibility-label :enable-notifications-later-button
        :type                :grey
        :background          :blur
        :container-style     {:margin-top 12}}
       (i18n/label :t/maybe-later)]]]))

(defn enable-notifications-illustration
  []
  (let [width (:width (rn/get-window))]
    [rn/image
     {:resize-mode :contain
      :style       (style/page-illustration width)
      :source      (resources/get-image :notifications)}]))

(defn background-image
  []
  [rn/view {:style rn/stylesheet-absolute-fill}
   [background/view true]])

(defn view
  []
  (let [insets safe-area/insets
        params (quo.context/use-screen-params)]
    [:<>
     (when-not (:onboarding? params)
       [background-image])
     [rn/view {:style (style/page-container insets)}
      [rn/view {:style style/page-heading}
       [quo/page-nav
        {:type       :no-title
         :background :blur
         :right-side [{:icon-name           :i/info
                       :on-press            on-open-info
                       :accessibility-label :notifications-info-button}]}]
       [page-title]]
      [enable-notifications-illustration]
      [enable-notification-form
       {:insets insets
        :params params}]]]))
