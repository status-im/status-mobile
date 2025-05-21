(ns status-im.contexts.onboarding.intro.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.privacy.view :as privacy]
    [status-im.common.terms.view :as terms]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.common.overlay.view :as overlay]
    [status-im.contexts.onboarding.intro.style :as style]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- show-terms-of-use
  []
  (rf/dispatch [:show-bottom-sheet {:content terms/terms-of-use :shell? true}]))

(defn- show-privacy-policy
  []
  (rf/dispatch [:show-bottom-sheet {:content privacy/privacy-statement :shell? true}]))

(defn- show-privacy-mode-sheet
  []
  (rf/dispatch [:privacy-mode/show-bottom-sheet {:theme :dark :shell? true}]))

(defn- third-party-services-text
  [privacy-mode-enabled?]
  [rn/view {:style {:flex-direction :row :margin-bottom 8}}
   [quo/text
    {:on-press (when privacy-mode-enabled? show-privacy-mode-sheet)
     :style    (if privacy-mode-enabled? style/danger-text style/plain-text)
     :size     :paragraph-2
     :weight   :medium}
    (i18n/label :t/third-party-services)]
   [quo/text
    {:on-press show-privacy-mode-sheet
     :style    (if privacy-mode-enabled? style/danger-text style/highlighted-text)
     :size     :paragraph-2
     :weight   :medium}
    (i18n/label (if privacy-mode-enabled? :t/disabled-2 :t/enabled))]])

(defn- terms
  [privacy-mode-enabled?]
  [rn/view {:style style/terms-privacy-container}
   (when (ff/enabled? ::ff/privacy-mode-ui)
     [third-party-services-text privacy-mode-enabled?])
   [quo/text
    {:style style/plain-text
     :size  :paragraph-2}
    (str (i18n/label :t/accept-status-tos-prefix))]
   [rn/view {:style {:flex-direction :row}}
    [quo/text
     {:on-press show-terms-of-use
      :style    style/highlighted-text
      :size     :paragraph-2
      :weight   :medium}
     (i18n/label :t/terms-of-service)]
    [quo/text
     {:style style/plain-text
      :size  :paragraph-2}
     " " (i18n/label :t/and) " "]
    [quo/text
     {:on-press show-privacy-policy
      :style    style/highlighted-text
      :size     :paragraph-2
      :weight   :medium}
     (i18n/label :t/intro-privacy-policy)]]])

(defn- explore-new-status
  []
  (rf/dispatch [:profile/explore-new-status]))

(defn- open-next-screen
  [privacy-mode-enabled? use-temporary-display-name? next-screen]
  (when-let [blur-show-fn @overlay/blur-show-fn-atom]
    (blur-show-fn))
  (rf/dispatch [:onboarding/use-temporary-display-name use-temporary-display-name?])
  (if privacy-mode-enabled?
    (rf/dispatch [:open-modal next-screen])
    (rf/dispatch [:open-modal :screen/onboarding.share-usage {:next-screen next-screen}])))

(defn view
  []
  (let [has-profiles-and-unaccepted-terms? (rf/sub [:profile/has-profiles-and-unaccepted-terms?])
        privacy-mode-enabled?              (rf/sub [:privacy-mode/privacy-mode-enabled?])
        sync-or-recover-profile            (rn/use-callback
                                            #(open-next-screen privacy-mode-enabled?
                                                               false
                                                               :screen/onboarding.log-in)
                                            [privacy-mode-enabled?])
        create-profile                     (rn/use-callback
                                            #(open-next-screen privacy-mode-enabled?
                                                               true
                                                               :screen/onboarding.create-profile)
                                            [privacy-mode-enabled?])]
    [rn/view {:style style/page-container}
     [background/view false]
     [quo/bottom-actions
      (cond->
        {:container-style  (style/bottom-actions-container safe-area/bottom)
         :description      :bottom
         :description-text [terms privacy-mode-enabled?]}

        has-profiles-and-unaccepted-terms?
        (assoc
         :actions          :one-action
         :button-one-label (i18n/label :t/explore-the-new-status)
         :button-one-props {:accessibility-label :explore-new-status
                            :on-press            explore-new-status})

        (not has-profiles-and-unaccepted-terms?)
        (assoc
         :actions          :two-vertical-actions
         :button-one-label (i18n/label :t/log-in)
         :button-one-props {:type                :dark-grey
                            :accessibility-label :log-in
                            :on-press            sync-or-recover-profile}
         :button-two-label (i18n/label :t/create-profile)
         :button-two-props {:accessibility-label :new-to-status-button
                            :on-press            create-profile}))]
     [overlay/view]]))
