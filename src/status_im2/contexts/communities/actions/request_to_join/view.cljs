(ns status-im2.contexts.communities.actions.request-to-join.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [status-im2.common.password-authentication.view :as password-authentication]
            [status-im2.contexts.communities.actions.community-rules-list.view :as community-rules]
            [status-im2.contexts.communities.actions.request-to-join.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn join-community-and-navigate-back
  [id]
  (rf/dispatch [:password-authentication/show
                {:content (fn [] [password-authentication/view])}
                {:label    (i18n/label :t/join-open-community)
                 :on-press #(rf/dispatch [:communities/request-to-join-with-password id %])}])
  (rf/dispatch [:navigate-back]))

(defn request-to-join
  []
  (fn []
    (let [{:keys [name
                  id
                  images]} (rf/sub [:get-screen-params])]
      [rn/view {:flex 1}
       [gesture/scroll-view {:style {:flex 1}}
        [rn/view style/page-container
         [rn/view {:style style/title-container}
          [quo/text
           {:accessibility-label :communities-join-community
            :weight              :semi-bold
            :size                :heading-2}
           (i18n/label :t/request-to-join)]]
         [rn/view {:style {:margin-right :auto :margin-top 8}}
          [quo/context-tag
           {:type           :community
            :size           24
            :community-logo (:thumbnail images)
            :community-name name}]]
         [quo/text
          {:style               {:margin-top 24}
           :accessibility-label :communities-rules-title
           :weight              :semi-bold
           :size                :paragraph-1}
          (i18n/label :t/community-rules)]
         [community-rules/view community-rules/standard-rules]
         [rn/view {:style (style/bottom-container)}
          [quo/button
           {:accessibility-label :cancel
            :on-press            #(rf/dispatch [:navigate-back])
            :type                :grey
            :container-style     style/cancel-button}
           (i18n/label :t/cancel)]
          [quo/button
           {:accessibility-label :join-community-button
            :on-press            #(join-community-and-navigate-back id)
            :container-style     {:flex 1}}
           (i18n/label :t/request-to-join)]]
         [rn/view {:style style/final-disclaimer-container}
          [quo/text
           {:size  :paragraph-2
            :style style/final-disclaimer-text}
           (i18n/label :t/request-to-join-disclaimer)]]]]])))
