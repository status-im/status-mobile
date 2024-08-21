(ns status-im.contexts.onboarding.intro.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.common.overlay.view :as overlay]
    [status-im.contexts.onboarding.intro.style :as style]
    [status-im.contexts.onboarding.privacy.view :as privacy]
    [status-im.contexts.onboarding.terms.view :as terms]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [[terms-accepted? set-terms-accepted?] (rn/use-state false)]
    [rn/view {:style style/page-container}
     [background/view false]
     [quo/bottom-actions
      {:container-style      (style/bottom-actions-container (safe-area/get-bottom))
       :actions              :two-vertical-actions
       :description          :top
       :description-top-text [rn/view
                              {:style style/terms-privacy-container}
                              [rn/view
                               {:accessibility-label :terms-privacy-checkbox-container}
                               [quo/selectors
                                {:type      :checkbox
                                 :blur?     true
                                 :checked?  terms-accepted?
                                 :on-change #(set-terms-accepted? not)}]]
                              [rn/view {:style style/text-aligner}
                               [rn/view {:style style/text-container}
                                [quo/text
                                 {:style style/plain-text
                                  :size  :paragraph-2}
                                 (str (i18n/label :t/accept-status-tos-prefix) " ")]
                                [quo/text
                                 {:on-press #(rf/dispatch [:show-bottom-sheet
                                                           {:content (fn [] [terms/terms-of-use])
                                                            :shell?  true}])
                                  :style    style/highlighted-text
                                  :size     :paragraph-2
                                  :weight   :medium}
                                 (i18n/label :t/terms-of-service)]
                                [quo/text
                                 {:style style/plain-text
                                  :size  :paragraph-2}
                                 " " (i18n/label :t/and) " "]
                                [quo/text
                                 {:on-press #(rf/dispatch [:show-bottom-sheet
                                                           {:content (fn [] [privacy/privacy-statement])
                                                            :shell?  true}])
                                  :style    style/highlighted-text
                                  :size     :paragraph-2
                                  :weight   :medium}
                                 (i18n/label :t/intro-privacy-statement)]]]]
       :button-one-label     (i18n/label :t/sync-or-recover-profile)
       :button-one-props     {:type                :dark-grey
                              :disabled?           (not terms-accepted?)
                              :accessibility-label :already-use-status-button
                              :on-press            (fn []
                                                     (when-let [blur-show-fn @overlay/blur-show-fn-atom]
                                                       (blur-show-fn))
                                                     (rf/dispatch
                                                      [:open-modal
                                                       :screen/onboarding.sync-or-recover-profile]))}
       :button-two-label     (i18n/label :t/create-profile)
       :button-two-props     {:accessibility-label :new-to-status-button
                              :disabled? (not terms-accepted?)
                              :on-press
                              (fn []
                                (when-let [blur-show-fn @overlay/blur-show-fn-atom]
                                  (blur-show-fn))
                                (rf/dispatch
                                 [:open-modal :screen/onboarding.new-to-status]))}}]
     [overlay/view]]))
