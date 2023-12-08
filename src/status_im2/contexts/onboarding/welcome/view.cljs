(ns status-im2.contexts.onboarding.welcome.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.safe-area :as safe-area]
    [status-im2.common.resources :as resources]
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
        window                (rf/sub [:dimensions/window])
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
     [rn/image
      {:style  (style/page-illustration (:width window))
       :source (resources/get-image :welcome-illustration)}]
     [rn/view {:style (style/buttons insets)}
      (when rn/small-screen?
        [linear-gradient/linear-gradient
         {:style  style/bottom-shadow
          :colors [colors/neutral-100-opa-0 colors/neutral-100-opa-80]}])
      [quo/button
       {:on-press            (fn []
                               (rf/dispatch [:init-root :shell-stack])
                               (rf/dispatch [:universal-links/process-stored-event]))
        :type                :primary
        :accessibility-label :welcome-button
        :customization-color profile-color}
       (i18n/label :t/start-using-status)]]]))
