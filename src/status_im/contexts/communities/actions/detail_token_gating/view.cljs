(ns status-im.contexts.communities.actions.detail-token-gating.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.detail-token-gating.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn filter-tokens
  [token-permissions permission-types]
  (mapcat (fn [permission-type]
            (get token-permissions permission-type))
   permission-types))

(defn get-token-role
  [token-permissions]
  (->> token-permissions
       (remove (fn [[level _]] (#{5 6} level))) ; Exclude levels 5 and 6, focusing on token roles
       (filter (fn [[_ requests]] (some :sufficient? (first requests))))
       (map first)
       (reduce max)))

(defn view
  [id]
  (let [{:keys [highest-permission-role can-request-access?]}
        (rf/sub [:community/token-gated-overview id])
        token-permissions (rf/sub [:community/token-permissions id])
        tokens (filter-tokens token-permissions [1 2 3])
        collectible (filter-tokens token-permissions [5 6])
        collectible-name (:symbol (first (first collectible)))
        collectible-img (:img-src (first (first collectible)))
        has-collectible (get first (first collectible) :sufficient?)
        role (get-token-role token-permissions)
        highest-role-text (i18n/label
                           (communities.utils/role->translation-key highest-permission-role
                                                                    :t/token-master))
        member-role-text (i18n/label
                          (communities.utils/role->translation-key role :t/member))
        selected-addresses (rf/sub [:communities/selected-permission-addresses id])]

    [rn/view {:style style/container}
     (when (and has-collectible (seq selected-addresses))
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
            :options             (if has-collectible :hold false)
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
