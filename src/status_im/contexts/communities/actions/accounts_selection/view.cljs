(ns status-im.contexts.communities.actions.accounts-selection.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.password-authentication.view :as password-authentication]
    [status-im.contexts.communities.actions.accounts-selection.style :as style]
    [status-im.contexts.communities.actions.addresses-for-permissions.view :as addresses-for-permissions]
    [status-im.contexts.communities.actions.airdrop-addresses.view :as airdrop-addresses]
    [status-im.contexts.communities.actions.community-rules.view :as community-rules]
    [status-im.contexts.communities.actions.detail-token-gating.view :as detail-token-gating]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [{id :community-id}             (rf/sub [:get-screen-params])
        show-addresses-for-permissions (fn []
                                         (rf/dispatch [:show-bottom-sheet
                                                       {:community-id id
                                                        :content      addresses-for-permissions/view}]))
        show-airdrop-addresses         (fn []
                                         (rf/dispatch [:show-bottom-sheet
                                                       {:community-id id
                                                        :content      airdrop-addresses/view}]))
        navigate-back                  #(rf/dispatch [:navigate-back])
        join-and-go-back               (fn []
                                         (rf/dispatch [:password-authentication/show
                                                       {:content (fn [] [password-authentication/view])}
                                                       {:label (i18n/label :t/join-open-community)
                                                        :on-press
                                                        #(rf/dispatch
                                                          [:communities/request-to-join-with-addresses
                                                           {:community-id id :password %}])}])
                                         (navigate-back))]
    (fn []
      (let [{:keys [name color images
                    membership-permissions?]} (rf/sub [:communities/community id])
            airdrop-account                   (rf/sub [:communities/airdrop-account id])
            selected-accounts                 (rf/sub [:communities/selected-permission-accounts id])
            {:keys [highest-permission-role
                    no-member-permission?]}   (rf/sub [:community/token-gated-overview id])
            highest-role-text                 (i18n/label (communities.utils/role->translation-key
                                                           highest-permission-role
                                                           :t/member))]
        [rn/safe-area-view {:style style/container}
         [quo/page-nav
          (cond-> {:text-align          :left
                   :icon-name           :i/close
                   :on-press            navigate-back
                   :accessibility-label :back-button}
            (or membership-permissions? (and highest-permission-role (not no-member-permission?)))
            (assoc :right-side
                   [{:icon-left :i/unlocked
                     :on-press  #(rf/dispatch
                                  [:show-bottom-sheet
                                   {:content (fn [] [detail-token-gating/view
                                                     id])}])
                     :label     (i18n/label :t/permissions)}]))]
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
             :data      [{:title             (i18n/label :t/join-as-a {:role highest-role-text})
                          :on-press          show-addresses-for-permissions
                          :description       :text
                          :action            :arrow
                          :label             :preview
                          :label-props       {:type :accounts
                                              :data selected-accounts}
                          :description-props {:text (i18n/label :t/all-addresses)}}
                         {:title             (i18n/label :t/for-airdrops)
                          :on-press          show-airdrop-addresses
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
            :on-complete         join-and-go-back}]]]))))
