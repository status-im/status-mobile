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
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- show-terms-of-use
  []
  (rf/dispatch [:show-bottom-sheet {:content terms/terms-of-use :shell? true}]))

(defn- show-privacy-policy
  []
  (rf/dispatch [:show-bottom-sheet {:content privacy/privacy-statement :shell? true}]))

(defn- terms
  [terms-accepted? set-terms-accepted?]
  [rn/view {:style style/terms-privacy-container}
   [rn/view
    {:accessibility-label :terms-privacy-checkbox-container}
    [quo/selectors
     {:type      :checkbox
      :blur?     true
      :checked?  terms-accepted?
      :on-change #(set-terms-accepted? not)}]]
   [rn/view {:style style/text-container}
    [quo/text
     {:style style/plain-text
      :size  :paragraph-2}
     (str (i18n/label :t/accept-status-tos-prefix) " ")]
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

(defn- sync-or-recover-profile
  []
  (when-let [blur-show-fn @overlay/blur-show-fn-atom]
    (blur-show-fn))
  (rf/dispatch [:open-modal :screen/onboarding.sync-or-recover-profile]))

(defn- create-profile
  []
  (when-let [blur-show-fn @overlay/blur-show-fn-atom]
    (blur-show-fn))
  (rf/dispatch [:open-modal :screen/onboarding.new-to-status]))

(defn view
  []
  (let [[terms-accepted? set-terms-accepted?] (rn/use-state false)
        has-profiles-and-unaccepted-terms?    (rf/sub [:profile/has-profiles-and-unaccepted-terms?])]
    [rn/view {:style style/page-container}
     [background/view false]
     [quo/bottom-actions
      (cond->
        {:container-style      (style/bottom-actions-container (safe-area/get-bottom))
         :actions              :two-vertical-actions
         :description          :top
         :description-top-text [terms terms-accepted? set-terms-accepted?]}

        has-profiles-and-unaccepted-terms?
        (assoc
         :actions          :one-action
         :button-one-label (i18n/label :t/explore-the-new-status)
         :button-one-props {:disabled?           (not terms-accepted?)
                            :accessibility-label :explore-new-status
                            :on-press            explore-new-status})

        (not has-profiles-and-unaccepted-terms?)
        (assoc
         :actions          :two-vertical-actions
         :button-one-label (i18n/label :t/sync-or-recover-profile)
         :button-one-props {:type                :dark-grey
                            :disabled?           (not terms-accepted?)
                            :accessibility-label :already-use-status-button
                            :on-press            sync-or-recover-profile}
         :button-two-label (i18n/label :t/create-profile)
         :button-two-props {:accessibility-label :new-to-status-button
                            :disabled?           (not terms-accepted?)
                            :on-press            create-profile}))]
     [overlay/view]]))
