(ns status-im2.contexts.onboarding.welcome.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.onboarding.welcome.style :as style]
    [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  (let [new-account? (rf/sub [:onboarding-2/new-account?])]
    [quo/title
     {:title                        (i18n/label (if new-account?
                                                  :t/welcome-to-web3
                                                  :t/welcome-back))
      :title-accessibility-label    :welcome-title
      :subtitle                     (i18n/label :t/welcome-to-web3-sub-title)
      :subtitle-accessibility-label :welcome-sub-title}]))


(defn navigation-bar
  [root]
  [quo/page-nav
   {:horizontal-description? false
    :one-icon-align-left?    true
    :align-mid?              false
    :page-nav-color          :transparent
    :left-section            {:icon                  :i/arrow-left
                              :icon-background-color colors/white-opa-5
                              :type                  :shell
                              :on-press              #(rf/dispatch [:init-root root])}}])

(defn view
  []
  (let [profile-color (:color (rf/sub [:onboarding-2/profile]))
        insets        (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [background/view true]
     [navigation-bar :enable-notifications]
     [page-title]
     [rn/view {:style style/page-illustration}
      [quo/text
       "Illustration here"]]
     [rn/view {:style (style/buttons insets)}
      [quo/button
       {:on-press                  (fn []
                                     (rf/dispatch [:init-root :shell-stack])
                                     (rf/dispatch [:universal-links/process-stored-event]))
        :type                      :primary
        :accessibility-label       :welcome-button
        :override-background-color (colors/custom-color profile-color 60)}
       (i18n/label :t/start-using-status)]]]))
