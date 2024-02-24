(ns status-im.contexts.communities.actions.detail-token-gating.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.detail-token-gating.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

;; [ ] open issue for the slide android
;; [ ] open a follow up to build this component

(defn view
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])
        {:keys [highest-permission-role can-request-access?]}
        (rf/sub [:community/token-gated-overview id])
        token-permissions (rf/sub [:community/token-permissions id])
        tokens (get token-permissions 2)
        collectible (get token-permissions 5)
        collectible-name (:symbol (first (first collectible)))
        collectible-img (:img-src (first (first collectible)))
        collectible-satisfied (get first (first collectible) :sufficient?)
        highest-role-text (i18n/label
                           (communities.utils/role->translation-key highest-permission-role :t/member))
        member-role-text (i18n/label
                          (communities.utils/role->translation-key (contains? token-permissions 2)
                                                                   :t/member))
        selected-addresses (rf/sub [:communities/selected-permission-addresses id])]

    (tap> ["highest-permission-role" (rf/sub [:community/token-gated-overview id])])
    (tap> ["token-permissions" (rf/sub [:community/token-permissions id])])
    (tap> ["tokens" collectible])
    (tap> ["master sufficient" (get (first (first collectible)) :sufficient?)])
    [rn/view {:style style/container}
     (when (and collectible-satisfied (seq selected-addresses))
       [rn/view
        {:style style/role-container}
        [rn/view {:style {:flex-direction :column}}
         [quo/text {:weight :medium}
          (i18n/label :t/you-eligible-to-join-as {:role highest-role-text})]
         [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
          (if can-request-access?
            (i18n/label :t/you-hodl)
            (i18n/label :t/you-must-hold))]
         [rn/view {:style {:align-items :flex-start}}
          [quo/collectible-tag
           {:size                :size-24
            :collectible-name    collectible-name
            :options             (if collectible-satisfied :hold false)
            :collectible-img-src collectible-img}]]]])
     [rn/view
      {:style style/role-container}
      [rn/view {:style style/permission-text}
       [quo/text {:weight :medium}
        (i18n/label :t/you-eligible-to-join-as {:role member-role-text})]
       [quo/text {:style {:padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hodl)
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list {:tokens tokens}]]]]))
