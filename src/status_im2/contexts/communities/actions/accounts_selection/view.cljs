(ns status-im2.contexts.communities.actions.accounts-selection.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im2.common.password-authentication.view :as password-authentication]
    [status-im2.contexts.communities.actions.accounts-selection.style :as style]
    [status-im2.contexts.communities.actions.community-rules.view :as community-rules]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- join-community-and-navigate-back
  [id]
  (rf/dispatch [:password-authentication/show
                {:content (fn [] [password-authentication/view])}
                {:label    (i18n/label :t/join-open-community)
                 :on-press #(rf/dispatch [:communities/request-to-join
                                          {:community-id id :password %}])}])
  (rf/dispatch [:navigate-back]))

(defn- page-top
  [{:keys [community-name logo-uri]}]
  [rn/view {:style style/page-top}
   [quo/text
    {:size   :heading-1
     :weight :semi-bold}
    (i18n/label :t/request-to-join)]
   [quo/context-tag
    {:type            :community
     :size            24
     :community-logo  logo-uri
     :community-name  community-name
     :container-style {:margin-top 8}}]])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])]
    [rn/view {:style style/container}
     [quo/page-nav
      {:text-align          :left
       :icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :back-button}]
     [page-top
      {:community-name name
       :logo-uri       (get-in images [:thumbnail :uri])}]
     [gesture/scroll-view
      [rn/view {:style style/content}
       [quo/text
        {:style               {:margin-top 24}
         :accessibility-label :community-rules-title
         :weight              :semi-bold
         :size                :paragraph-1}
        (i18n/label :t/community-rules)]
       [community-rules/view id]]]
     [rn/view {:style (style/bottom-actions)}
      [quo/slide-button
       {:size                :size-48
        :track-text          (i18n/label :t/slide-to-request-to-join)
        :track-icon          :i/face-id
        :customization-color color
        :on-complete         #(join-community-and-navigate-back id)}]]]))
