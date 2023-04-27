(ns status-im2.contexts.communities.menus.request-to-join.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.communities.menus.community-rules-list.view :as community-rules]
            [reagent.core :as reagent]
            [status-im2.contexts.communities.menus.request-to-join.style :as style]
            [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.requests :as requests]
            [react-native.gesture :as gesture]))

(defn request-to-join-text
  [is-open?]
  (if is-open?
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
            is-open?                       (not= 3 (:access permissions))]
        [rn/view {:flex 1 :margin-top 40}
         [gesture/scroll-view {:style {:flex 1}}
          [rn/view style/page-container
           [rn/view
            {:style style/title-container}
            [quo/text
             {:accessibility-label :communities-join-community
              :weight              :semi-bold
              :size                :heading-1}
             (request-to-join-text is-open?)]
            [rn/view
             {:style style/request-icon}
             [quo/icon :i/info]]]
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
             :on-change           #(swap! agreed-to-rules? not)}
            (i18n/label :t/accept-community-rules)]
           [rn/view {:style (style/bottom-container)}
            [quo/button
             {:accessibility-label :cancel
              :on-press            #(rf/dispatch [:navigate-back])
              :type                :grey
              :style               style/cancel-button} (i18n/label :t/cancel)]
            [quo/button
             {:accessibility-label :join-community-button
              :on-press            (fn []
                                     (if can-join?
                                       (do
                                         (rf/dispatch [:communities/join id])
                                         (rf/dispatch [:navigate-back]))
                                       (do (and can-request-access?
                                                (not pending?)
                                                (requests/can-request-access-again?
                                                 requested-to-join-at))
                                           (rf/dispatch [:communities/request-to-join id])
                                           (rf/dispatch [:navigate-back]))))
              :disabled            (not @agreed-to-rules?)
              :style               {:flex 1}} (request-to-join-text is-open?)]]]]]))))
