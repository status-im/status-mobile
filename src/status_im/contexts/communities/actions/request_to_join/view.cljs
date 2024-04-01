(ns status-im.contexts.communities.actions.request-to-join.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.contexts.communities.actions.community-rules-list.view :as community-rules]
    [status-im.contexts.communities.actions.request-to-join.style :as style]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn join-community-and-navigate-back
  [id]
  (rf/dispatch
   [:standard-auth/authorize
    {:auth-button-label (i18n/label :t/confirm-changes)
     :on-auth-success   (fn [password]
                          (rf/dispatch
                           [:communities/request-to-join
                            {:community-id id :password (security/safe-unmask-data password)}]))
     :on-auth-fail      (fn [err]
                          (log/info "Biometric authentication failed" err))}])

  (rf/dispatch [:navigate-back]))

(defn- view-internal
  [{:keys [theme]}]
  (fn []
    (let [{:keys [id]}                (rf/sub [:get-screen-params])
          {:keys [color name images]} (rf/sub [:communities/community id])]
      [rn/safe-area-view {:flex 1}
       [gesture/scroll-view {:style style/container}
        [rn/view style/page-container
         [rn/view {:style style/title-container}
          [quo/text
           {:accessibility-label :communities-join-community
            :weight              :semi-bold
            :size                :heading-2}
           (i18n/label :t/request-to-join)]]
         [rn/view {:style style/community-icon}
          [quo/context-tag
           {:type           :community
            :size           24
            :community-logo (:thumbnail images)
            :community-name name}]]
         [quo/text
          {:style               style/rules-text
           :accessibility-label :communities-rules-title
           :weight              :semi-bold
           :size                :paragraph-1}
          (i18n/label :t/community-rules)]
         [community-rules/view community-rules/standard-rules false]]]
       [rn/view {:style style/bottom-container}
        [quo/button
         {:accessibility-label :cancel
          :on-press            #(rf/dispatch [:navigate-back])
          :type                :grey
          :container-style     style/cancel-button}
         (i18n/label :t/cancel)]
        [quo/button
         {:accessibility-label :join-community-button
          :on-press            #(join-community-and-navigate-back id)
          :container-style     {:flex 1}
          :inner-style         {:background-color (colors/resolve-color color theme)}}
         (i18n/label :t/request-to-join)]]
       [rn/view {:style style/final-disclaimer-container}
        [quo/text
         {:size  :paragraph-2
          :style style/final-disclaimer-text}
         (i18n/label :t/request-to-join-disclaimer)]]])))

(def view (quo.theme/with-theme view-internal))
