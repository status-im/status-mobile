(ns status-im2.contexts.communities.actions.request-to-join.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [reagent.core :as reagent]
            [status-im2.contexts.communities.actions.community-rules-list.view :as community-rules]
            [status-im2.contexts.communities.actions.request-to-join.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.requests :as requests]))

(defn request-to-join-text
  [open?]
  (if open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join)))

(defn request-to-join
  []
  (let [agreed-to-rules? (reagent/atom false)]
    (fn []
      (let [{:keys [permissions
                    name
                    id
                    images
                    can-join?
                    can-request-access?
                    requested-to-join-at]} (rf/sub [:get-screen-params])
            pending?                       (rf/sub [:communities/my-pending-request-to-join id])
            open?                          (not= 3 (:access permissions))]
        [rn/view {:flex 1}
         [gesture/scroll-view {:style {:flex 1}}
          [rn/view style/page-container
           [rn/view {:style style/title-container}
            [quo/text
             {:accessibility-label :communities-join-community
              :weight              :semi-bold
              :size                :heading-1}
             (request-to-join-text open?)]]
           [quo/context-tag
            {:style
             {:margin-right :auto
              :margin-top   8}}
            (:thumbnail images) name]
           [quo/text
            {:style               {:margin-top 24}
             :accessibility-label :communities-rules-title
             :weight              :semi-bold
             :size                :paragraph-1}
            (i18n/label :t/community-rules)]
           [community-rules/view community-rules/rules]
           [quo/disclaimer
            {:accessibility-label :rules-disclaimer-checkbox
             :container-style     {:margin-top 20}
             :on-change           #(swap! agreed-to-rules? not)
             :checked?            @agreed-to-rules?}
            (i18n/label :t/accept-community-rules)]
           [rn/view {:style (style/bottom-container)}
            [quo/button
             {:accessibility-label :cancel
              :on-press            #(rf/dispatch [:navigate-back])
              :type                :grey
              :container-style     style/cancel-button}
             (i18n/label :t/cancel)]
            [quo/button
             {:accessibility-label :join-community-button
              :on-press            (fn []
                                     (if can-join?
                                       (do
                                         (rf/dispatch [:communities/request-to-join id])
                                         (rf/dispatch [:navigate-back]))
                                       (do (and can-request-access?
                                                (not pending?)
                                                (requests/can-request-access-again?
                                                 requested-to-join-at))
                                           (rf/dispatch [:communities/request-to-join id])
                                           (rf/dispatch [:navigate-back]))))
              :disabled?           (not @agreed-to-rules?)
              :container-style     {:flex 1}}
             (request-to-join-text open?)]]
           [rn/view {:style style/final-disclaimer-container}
            [quo/text
             {:size  :paragraph-2
              :style style/final-disclaimer-text}
             (i18n/label :t/request-to-join-disclaimer)]]]]]))))
