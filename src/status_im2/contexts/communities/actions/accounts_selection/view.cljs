(ns status-im2.contexts.communities.actions.accounts-selection.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im2.common.password-authentication.view :as password-authentication]
    [status-im2.contexts.communities.actions.request-to-join.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn join-community-and-navigate-back
  [id]
  (rf/dispatch [:password-authentication/show
                {:content (fn [] [password-authentication/view])}
                {:label    (i18n/label :t/join-open-community)
                 :on-press #(rf/dispatch [:communities/request-to-join
                                          {:community-id id :password %}])}])
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (fn []
    (let [{:keys [_name
                  id
                  _images]} (rf/sub [:get-screen-params])]
      [rn/view {:flex 1}
       [gesture/scroll-view {:style {:flex 1}}
        [rn/view style/page-container
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
           (i18n/label :t/request-to-join)]]]]])))
