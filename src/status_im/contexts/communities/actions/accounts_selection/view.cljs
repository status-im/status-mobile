(ns status-im.contexts.communities.actions.accounts-selection.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.communities.actions.accounts-selection.style :as style]
    [status-im.contexts.communities.actions.addresses-for-permissions.view :as addresses-for-permissions]
    [status-im.contexts.communities.actions.airdrop-addresses.view :as airdrop-addresses]
    [status-im.contexts.communities.actions.community-rules.view :as community-rules]
    [status-im.contexts.communities.actions.permissions-sheet.view :as permissions-sheet]
    [status-im.contexts.communities.utils :as communities.utils]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- bottom-authentication
  [{:keys [id]}]
  (let [theme                     (quo.theme/use-theme)
        can-edit-addresses?       (rf/sub [:communities/can-edit-shared-addresses? id])
        revealed-accounts         (rf/sub [:communities/accounts-to-reveal id])
        {:keys [color]}           (rf/sub [:communities/community id])
        join-requests-for-signing (rf/sub [:communities/join-requests-for-signing-by-id id])
        confirm-choices           (rn/use-callback
                                   (fn [signature-data]
                                     (rf/dispatch [:communities/request-to-join
                                                   {:community-id   id
                                                    :signature-data signature-data}])
                                     (navigate-back))
                                   [can-edit-addresses? id])]

    (rn/use-effect
     #(when (and (not can-edit-addresses?) (seq revealed-accounts))
        (rf/dispatch [:communities/generate-requests-for-signing id]))
     [can-edit-addresses? revealed-accounts])

    (when-not can-edit-addresses?
      [rn/view {:style (style/bottom-actions theme)}
       [standard-auth/slide-sign
        {:sign-payload        join-requests-for-signing
         :disabled?           (not (seq join-requests-for-signing))
         :size                :size-48
         :track-text          (i18n/label :t/slide-to-request-to-join)
         :auth-button-label   (if can-edit-addresses?
                                (i18n/label :t/edit-shared-addresses)
                                (i18n/label :t/request-to-join))
         :customization-color color
         :on-success          confirm-choices}]])))

(defn view
  []
  (let [{id :community-id}                (quo.theme/use-screen-params)
        {:keys [name images joined]}      (rf/sub [:communities/community id])
        has-permissions?                  (rf/sub [:communities/has-permissions? id])
        airdrop-account                   (rf/sub [:communities/airdrop-account id])
        revealed-accounts                 (rf/sub [:communities/accounts-to-reveal id])
        revealed-accounts-count           (count revealed-accounts)
        wallet-accounts-count             (count (rf/sub [:wallet/operable-accounts]))
        addresses-shared-text             (if (= revealed-accounts-count wallet-accounts-count)
                                            (i18n/label :t/all-addresses)
                                            (i18n/label-pluralize
                                             revealed-accounts-count
                                             :t/address-count))
        {:keys [highest-permission-role]} (rf/sub [:community/token-gated-overview id])
        highest-role-text                 (i18n/label (communities.utils/role->translation-key
                                                       highest-permission-role
                                                       :t/member))
        can-edit-addresses?               (rf/sub [:communities/can-edit-shared-addresses? id])
        show-addresses-for-permissions    (rn/use-callback
                                           (fn []
                                             (if can-edit-addresses?
                                               (rf/dispatch [:open-modal :addresses-for-permissions
                                                             {:community-id id}])
                                               (rf/dispatch [:show-bottom-sheet
                                                             {:community-id id
                                                              :content
                                                              addresses-for-permissions/view}])))
                                           [can-edit-addresses?])
        show-airdrop-addresses            (rn/use-callback
                                           (fn []
                                             (if can-edit-addresses?
                                               (rf/dispatch [:open-modal :address-for-airdrop
                                                             {:community-id id}])
                                               (rf/dispatch [:show-bottom-sheet
                                                             {:community-id id
                                                              :content      airdrop-addresses/view}])))
                                           [can-edit-addresses?])
        open-permission-sheet             (rn/use-callback
                                           (fn []
                                             (rf/dispatch
                                              [:show-bottom-sheet
                                               {:content (fn []
                                                           [permissions-sheet/view id])}]))
                                           [id])]
    (rn/use-mount
     (fn []
       (rf/dispatch [:communities/initialize-permission-addresses id])))

    [rn/safe-area-view {:style style/container}
     [quo/page-nav
      (cond-> {:text-align          :left
               :icon-name           :i/close
               :on-press            navigate-back
               :accessibility-label :back-button}
        (and has-permissions? (ff/enabled? ::ff/community.view-token-requirements))
        (assoc :right-side
               [{:icon-left :i/unlocked
                 :on-press  open-permission-sheet
                 :label     (i18n/label :t/permissions)}]))]
     [quo/page-top
      {:title       (if can-edit-addresses?
                      (i18n/label :t/edit-shared-addresses)
                      (i18n/label :t/request-to-join))
       :description :context-tag
       :context-tag {:type           :community
                     :size           24
                     :community-logo (get-in images [:thumbnail :uri])
                     :community-name name}}]
     [gesture/scroll-view
      [:<>
       (when-not can-edit-addresses?
         [quo/text
          {:style               style/section-title
           :accessibility-label :community-rules-title
           :weight              :semi-bold
           :size                :paragraph-1}
          (i18n/label :t/address-to-share)])
       [quo/category
        {:list-type :settings
         :data      [{:title             (if joined
                                           (i18n/label :t/you-are-a-role {:role highest-role-text})
                                           (i18n/label :t/join-as {:role highest-role-text}))
                      :on-press          show-addresses-for-permissions
                      :description       :text
                      :action            :arrow
                      :label             :preview
                      :label-props       {:type :accounts
                                          :data revealed-accounts}
                      :preview-size      :size-32
                      :description-props {:text addresses-shared-text}}
                     {:title             (i18n/label :t/for-airdrops)
                      :on-press          show-airdrop-addresses
                      :description       :text
                      :action            :arrow
                      :label             :preview
                      :label-props       {:type :accounts
                                          :data [airdrop-account]}
                      :preview-size      :size-32
                      :description-props {:text (:name airdrop-account)}}]}]
       (when-not can-edit-addresses?
         [quo/text
          {:style               style/section-title
           :accessibility-label :community-rules-title
           :weight              :semi-bold
           :size                :paragraph-1}
          (i18n/label :t/community-rules)])
       (when-not can-edit-addresses?
         [community-rules/view id])]]
     [bottom-authentication {:id id}]]))
