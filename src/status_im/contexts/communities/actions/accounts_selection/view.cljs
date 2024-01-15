(ns status-im.contexts.communities.actions.accounts-selection.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.password-authentication.view :as password-authentication]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.accounts-selection.style :as style]
    [status-im.contexts.communities.actions.community-rules.view :as community-rules]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- join-community-and-navigate-back
  [id]
  (rf/dispatch [:password-authentication/show {:content (fn [] [password-authentication/view])}
                {:label    (i18n/label :t/join-open-community)
                 :on-press #(rf/dispatch [:communities/request-to-join-with-addresses
                                          {:community-id id :password %}])}])
  (rf/dispatch [:navigate-back]))

(defn f-view-internal
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])
        airdrop-account (rf/sub [:communities/airdrop-account id])
        selected-accounts (rf/sub [:communities/selected-permission-accounts id])
        {:keys [highest-permission-role]} (rf/sub [:community/token-gated-overview id])
        highest-permission-role-text
        (i18n/label
         (condp = highest-permission-role
           constants/community-token-permission-become-token-owner  :t/token-owner
           constants/community-token-permission-become-token-master :t/token-master
           constants/community-token-permission-become-admin        :t/admin
           :t/member))]
    [rn/view {:style style/container}
     [quo/page-nav
      {:text-align          :left
       :icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :back-button}]
     [quo/page-top
      {:title       (i18n/label :t/request-to-join)
       :description :context-tag
       :context-tag {:type           :community
                     :size           24
                     :community-logo (get-in images [:thumbnail :uri])
                     :community-name name}}]
     [gesture/scroll-view
      [:<>
       [quo/text
        {:style               style/section-title
         :accessibility-label :community-rules-title
         :weight              :semi-bold
         :size                :paragraph-1}
        (i18n/label :t/address-to-share)]
       [quo/category
        {:list-type :settings
         :data      [{:title             (i18n/label :t/join-as-a {:role highest-permission-role-text})
                      :on-press          #(rf/dispatch [:open-modal :addresses-for-permissions
                                                        {:community-id id}])
                      :description       :text
                      :action            :arrow
                      :label             :preview
                      :label-props       {:type :accounts
                                          :data selected-accounts}
                      :description-props {:text (i18n/label :t/all-addresses)}}
                     {:title             (i18n/label :t/for-airdrops)
                      :on-press          #(rf/dispatch [:open-modal :airdrop-addresses
                                                        {:community-id id}])
                      :description       :text
                      :action            :arrow
                      :label             :preview
                      :label-props       {:type :accounts
                                          :data [airdrop-account]}
                      :description-props {:text (:name airdrop-account)}}]}]
       [quo/text
        {:style               style/section-title
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

(defn view
  []
  [:f> f-view-internal])
