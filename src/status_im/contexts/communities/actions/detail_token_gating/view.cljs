(ns status-im.contexts.communities.actions.detail-token-gating.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.detail-token-gating.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])
        {:keys [highest-permission-role can-request-access?]}
        (rf/sub [:community/token-gated-overview id])
        token-permissions (rf/sub [:community/token-permissions id])
        tokens (get token-permissions 2)
        token-master (get token-permissions 6)
        token-master-name (:symbol (first (first token-master)))
        token-master-img (:img-src (first (first token-master)))
        highest-role-text
        (i18n/label
         (communities.utils/role->translation-key highest-permission-role :t/member))
        member-role-text (communities.utils/role->translation-key (contains? token-permissions 2)
                                                                  :t/member)
        selected-addresses (rf/sub [:communities/selected-permission-addresses id])]

    [rn/view {:style style/container}
     (when (and highest-permission-role (seq selected-addresses))
       [rn/view
        {:style style/highest-role}
        [rn/view {:style {:flex-direction :column}}
         [quo/text {:weight :medium}
          (i18n/label :t/you-eligible-to-join-as {:role highest-role-text})]
         [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
          (if can-request-access?
            (i18n/label :t/you-hodl)
            (i18n/label :t/you-must-hold))]
         [rn/view {:style {:align-items :flex-start}}
          (when token-master
            [quo/collectible-tag
             {:size                :size-24
              :collectible-name    token-master-name
              :options             :hold
              :collectible-img-src token-master-img}])]]])
     [rn/view
      {:style style/highest-role}
      [rn/view {:style {:flex-direction :column :margin-top 12}}
       [quo/text {:weight :medium}
        (i18n/label :t/you-eligible-to-join-as {:role member-role-text})]
       [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hodl)
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list {:tokens tokens}]]]]))
