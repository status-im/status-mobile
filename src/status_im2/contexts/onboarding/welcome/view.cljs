(ns status-im2.contexts.onboarding.welcome.view
  (:require [quo2.core :as quo]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.constants :as constants]
            [status-im2.contexts.onboarding.welcome.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn page-title
  []
  (let [new-account? (rf/sub [:onboarding-2/new-account?])]
    [quo/text-combinations
     {:container-style                 {:margin-top 12 :margin-horizontal 20}
      :title                           (i18n/label (if new-account?
                                                     :t/welcome-to-web3
                                                     :t/welcome-back))
      :title-accessibility-label       :welcome-title
      :description                     (i18n/label :t/welcome-to-web3-sub-title)
      :description-accessibility-label :welcome-sub-title}]))

(defn dispatch-visibility-status-update
  [status-type]
  (re-frame/dispatch
   [:visibility-status-updates/delayed-visibility-status-update status-type]))

(defn view
  []
  (let [profile-color         (rf/sub [:onboarding-2/customization-color])
        {:keys [status-type]} (rf/sub [:multiaccount/current-user-visibility-status])
        insets                (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     (when (nil? status-type)
       (dispatch-visibility-status-update constants/visibility-status-automatic))
     [quo/page-nav
      {:type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back-within-stack :enable-notifications])}]
     [page-title]
     [rn/view {:style style/page-illustration}
      [quo/text
       "Illustration here"]]
     [rn/view {:style (style/buttons insets)}
      [quo/button
       {:on-press            (fn []
                               (rf/dispatch [:init-root :shell-stack])
                               (rf/dispatch [:universal-links/process-stored-event]))
        :type                :primary
        :accessibility-label :welcome-button
        :customization-color profile-color}
       (i18n/label :t/start-using-status)]]]))
