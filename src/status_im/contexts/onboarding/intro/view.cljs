(ns status-im.contexts.onboarding.intro.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.onboarding.common.overlay.view :as overlay]
    [status-im.contexts.onboarding.intro.style :as style]
    [status-im.contexts.onboarding.terms.view :as terms]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  [rn/view {:style style/page-container}
   [background/view false]
   [quo/bottom-actions
    {:container-style  (style/bottom-actions-container (safe-area/get-bottom))
     :actions          :two-vertical-actions
     :description      :bottom
     :description-text [quo/text
                        {:style  style/text-container
                         :size   :paragraph-2
                         :weight :regular}
                        [quo/text {:style style/plain-text}
                         (i18n/label :t/by-continuing-you-accept)]
                        [quo/text
                         {:on-press #(rf/dispatch [:show-bottom-sheet
                                                   {:content (fn [] [terms/terms-of-use])
                                                    :shell?  true}])
                          :style    style/highlighted-text}
                         (i18n/label :t/terms-of-service)]]
     :button-one-label (i18n/label :t/sync-or-recover-profile)
     :button-one-props {:type                :dark-grey
                        :accessibility-label :already-use-status-button
                        :on-press            (fn []
                                               (when-let [blur-show-fn @overlay/blur-show-fn-atom]
                                                 (blur-show-fn))
                                               (rf/dispatch
                                                [:open-modal
                                                 :screen/onboarding.sync-or-recover-profile]))}
     :button-two-label (i18n/label :t/create-profile)
     :button-two-props {:accessibility-label :new-to-status-button
                        :on-press
                        (fn []
                          (when-let [blur-show-fn @overlay/blur-show-fn-atom]
                            (blur-show-fn))
                          (rf/dispatch
                           [:open-modal :screen/onboarding.new-to-status]))}}]
   [overlay/view]])
