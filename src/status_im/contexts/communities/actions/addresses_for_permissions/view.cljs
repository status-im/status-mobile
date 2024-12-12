(ns status-im.contexts.communities.actions.addresses-for-permissions.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.addresses-for-permissions.style :as style]
    [status-im.contexts.communities.actions.permissions-sheet.view :as permissions-sheet]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn- role-keyword
  [role]
  (condp = role
    constants/community-token-permission-become-token-owner  :token-owner
    constants/community-token-permission-become-token-master :token-master
    constants/community-token-permission-become-admin        :admin
    constants/community-token-permission-become-member       :member
    nil))

(defn- balances->components-props
  [balances images-by-symbol]
  (for [{:keys [amount decimals type name] sym :symbol :as balance} balances]
    (cond-> balance
      true
      (assoc :type
             (condp = type
               constants/community-token-type-erc20  :token
               constants/community-token-type-erc721 :collectible
               :token))

      (= type constants/community-token-type-erc721)
      (assoc :collectible-name    name
             :collectible-img-src (images-by-symbol sym))

      (= type constants/community-token-type-erc20)
      (assoc :amount        (str (money/token->unit amount decimals))
             :token         (:symbol balance)
             :token-img-src (images-by-symbol sym)))))

(defn- confirm-discard-drawer
  [community-id]
  (let [{:keys [name logo color]} (rf/sub [:communities/for-context-tag community-id])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :community
       :title               (i18n/label :t/discard-changes?)
       :community-name      name
       :community-logo      logo
       :customization-color color}]
     [rn/view {:style style/drawer-body}
      [quo/text (i18n/label :t/all-changes-will-be-discarded)]]
     [quo/bottom-actions
      {:actions          :two-actions

       :button-one-label (i18n/label :t/discard)
       :button-one-props {:customization-color color
                          :on-press
                          (fn []
                            (rf/dispatch [:dismiss-modal :addresses-for-permissions])
                            (rf/dispatch [:hide-bottom-sheet]))}

       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press #(rf/dispatch [:hide-bottom-sheet])}}]]))

(defn- leave-community-drawer
  [community-id outro-message]
  (let [{:keys [name logo color]} (rf/sub [:communities/for-context-tag community-id])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :community
       :title               (i18n/label :t/leave-community?)
       :community-name      name
       :community-logo      logo
       :customization-color color}]
     [rn/view {:style style/drawer-body}
      [quo/text
       (if (string/blank? outro-message)
         (i18n/label :t/leave-community-farewell)
         outro-message)]]
     [quo/bottom-actions
      {:actions          :two-actions

       :button-one-label (i18n/label :t/confirm-and-leave)
       :button-one-props {:customization-color color
                          :on-press
                          #(rf/dispatch [:communities/addresses-for-permissions-leave community-id])}

       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press #(rf/dispatch [:hide-bottom-sheet])}}]]))

(defn- account-item
  [{:keys [customization-color address name emoji]} _ _
   [addresses-to-reveal set-addresses-to-reveal community-id community-color share-all-addresses?]]
  (let [balances         (rf/sub [:communities/permissioned-balances-by-address community-id address])
        images-by-symbol (rf/sub [:communities/token-images-by-symbol community-id])
        checked?         (contains? addresses-to-reveal address)
        toggle-address   (fn []
                           (let [new-addresses (if checked?
                                                 (disj addresses-to-reveal address)
                                                 (conj addresses-to-reveal address))]
                             (set-addresses-to-reveal new-addresses)))]
    [quo/account-permissions
     {:account             {:name                name
                            :address             address
                            :emoji               emoji
                            :customization-color customization-color}
      :token-details       (when-not share-all-addresses?
                             (balances->components-props balances images-by-symbol))
      :checked?            checked?
      :disabled?           share-all-addresses?
      :on-change           toggle-address
      :container-style     {:margin-bottom 8}
      :customization-color community-color}]))

(defn- page-top
  [{:keys [community-id identical-choices? can-edit-addresses?]}]
  (let [{:keys [name logo color]} (rf/sub [:communities/for-context-tag community-id])
        has-permissions?          (rf/sub [:communities/has-permissions? community-id])
        confirm-discard-changes   (rn/use-callback
                                   (fn []
                                     (if identical-choices?
                                       (rf/dispatch [:dismiss-modal :addresses-for-permissions])
                                       (rf/dispatch [:show-bottom-sheet
                                                     {:content (fn []
                                                                 [confirm-discard-drawer
                                                                  community-id])}])))
                                   [identical-choices? community-id])
        open-permission-sheet     (rn/use-callback
                                   (fn []
                                     (rf/dispatch [:show-bottom-sheet
                                                   {:content (fn []
                                                               [permissions-sheet/view
                                                                community-id])}]))
                                   [community-id])]
    [:<>
     (when can-edit-addresses?
       [quo/page-nav
        {:type      :no-title
         :icon-name :i/arrow-left
         :on-press  confirm-discard-changes}])

     (if can-edit-addresses?
       [quo/page-top
        {:title                     (i18n/label :t/addresses-for-permissions)
         :title-accessibility-label :title-label
         :description               :context-tag
         :context-tag               {:type           :community
                                     :community-logo logo
                                     :community-name name}}]
       [quo/drawer-top
        (cond-> {:type                :context-tag
                 :title               (i18n/label :t/addresses-for-permissions)
                 :context-tag-type    :community
                 :community-name      name
                 :community-logo      logo
                 :customization-color color}
          (and has-permissions? (ff/enabled? ::ff/community.view-token-requirements))
          (assoc :button-icon     :i/info
                 :button-type     :grey
                 :on-button-press open-permission-sheet))])]))

(defn selection-bottom-actions
  [id
   {:keys [flag-share-all-addresses
           addresses-to-reveal
           cancel-selection
           can-edit-addresses?
           identical-choices?]}]
  (let [color (rf/sub [:communities/community-color id])
        outro-message (rf/sub [:communities/community-outro-message id])
        joined (rf/sub [:communities/community-joined id])
        checking? (rf/sub [:communities/permissions-check-for-selection-checking? id])

        leave-community
        (rn/use-callback
         (fn []
           (rf/dispatch [:show-bottom-sheet
                         {:content (fn [] [leave-community-drawer id outro-message])}]))
         [outro-message])

        confirm-changes
        (fn []
          (if can-edit-addresses?
            (do
              (rf/dispatch
               [:standard-auth/authorize
                {:auth-button-label (i18n/label :t/confirm-changes)
                 :on-auth-success   (fn [password]
                                      (rf/dispatch
                                       [:communities/edit-shared-addresses
                                        {:community-id id
                                         :password     password
                                         :addresses    addresses-to-reveal
                                         :on-success   (fn []
                                                         (rf/dispatch [:dismiss-modal
                                                                       :addresses-for-permissions])
                                                         (rf/dispatch [:hide-bottom-sheet]))}]))}])
              (rf/dispatch [:communities/set-share-all-addresses id flag-share-all-addresses]))
            (do
              (rf/dispatch [:communities/set-share-all-addresses id flag-share-all-addresses])
              (rf/dispatch [:communities/set-addresses-to-reveal id addresses-to-reveal]))))
        highest-role (rf/sub [:communities/highest-role-for-selection id])
        [unmodified-role _] (rn/use-state highest-role)]

    [quo/bottom-actions
     (cond-> {:role             (role-keyword highest-role)
              :description      (if highest-role
                                  :top
                                  :top-error)
              :button-one-props {:customization-color color
                                 :on-press            confirm-changes
                                 :disabled?           (or checking?
                                                          (empty? addresses-to-reveal)
                                                          (not highest-role)
                                                          identical-choices?)}}
       can-edit-addresses?
       (->
         (assoc :actions              :one-action
                :button-one-label     (if (and joined (not highest-role))
                                        (i18n/label :t/leave-community)
                                        (i18n/label :t/confirm-changes))
                :description-top-text (cond
                                        (and joined (= highest-role unmodified-role))
                                        (i18n/label :t/you-are-a)

                                        (and joined (not= highest-role unmodified-role))
                                        (i18n/label :t/you-will-be-a))
                :error-message        (when (and joined (not highest-role))
                                        (i18n/label :t/membership-requirements-not-met)))
         (update :button-one-props
                 merge
                 (cond (and joined (not highest-role))
                       {:type      :danger
                        :disabled? false
                        :on-press  leave-community})))

       (not can-edit-addresses?)
       (assoc :actions          :two-actions
              :button-one-label (i18n/label :t/confirm-changes)
              :button-two-label (i18n/label :t/cancel)
              :button-two-props {:type     :grey
                                 :on-press cancel-selection}
              :error-message    (cond
                                  (empty? addresses-to-reveal)
                                  (i18n/label :t/no-addresses-selected)

                                  (not highest-role)
                                  (i18n/label :t/addresses-dont-contain-tokens-needed))))]))

(defn view
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])

        color (rf/sub [:communities/community-color id])

        can-edit-addresses? (rf/sub [:communities/can-edit-shared-addresses? id])

        wallet-accounts (rf/sub [:wallet/operable-accounts])
        joined (rf/sub [:communities/community-joined id])
        unmodified-addresses-to-reveal (rf/sub [:communities/addresses-to-reveal id])
        [addresses-to-reveal set-addresses-to-reveal] (rn/use-state unmodified-addresses-to-reveal)

        unmodified-flag-share-all-addresses (rf/sub [:communities/share-all-addresses? id])
        [flag-share-all-addresses
         set-flag-share-all-addresses] (rn/use-state unmodified-flag-share-all-addresses)

        identical-choices? (and (= unmodified-addresses-to-reveal addresses-to-reveal)
                                (= unmodified-flag-share-all-addresses flag-share-all-addresses))

        cancel-selection
        (fn []
          (rf/dispatch [:communities/check-permissions-to-join-community
                        id
                        unmodified-addresses-to-reveal
                        :based-on-client-selection])
          (rf/dispatch [:hide-bottom-sheet]))

        toggle-flag-share-all-addresses
        (fn []
          (let [new-value (not flag-share-all-addresses)]
            (set-flag-share-all-addresses new-value)
            (when new-value
              (set-addresses-to-reveal (set (map :address wallet-accounts))))))]
    (rn/use-mount
     (fn []
       (when-not flag-share-all-addresses
         (rf/dispatch [:communities/get-permissioned-balances id]))))

    (rn/use-effect
     (fn []
       (rf/dispatch [:communities/check-permissions-to-join-during-selection id addresses-to-reveal]))
     [id addresses-to-reveal])
    [:<>
     [page-top
      {:community-id        id
       :identical-choices?  identical-choices?
       :can-edit-addresses? can-edit-addresses?}]

     [gesture/flat-list
      {:render-fn               account-item
       :render-data             [addresses-to-reveal
                                 set-addresses-to-reveal
                                 id
                                 color
                                 flag-share-all-addresses]
       :header                  [quo/page-setting
                                 {:checked?            flag-share-all-addresses
                                  :disabled?           joined
                                  :customization-color color
                                  :on-change           toggle-flag-share-all-addresses
                                  :setting-text        (i18n/label
                                                        :t/share-all-current-and-future-addresses)
                                  :container-style     {:margin-bottom 16}}]
       :content-container-style {:padding-horizontal 20}
       :key-fn                  :address
       :data                    wallet-accounts}]

     [selection-bottom-actions id
      {:flag-share-all-addresses flag-share-all-addresses
       :addresses-to-reveal      addresses-to-reveal
       :cancel-selection         cancel-selection
       :can-edit-addresses?      can-edit-addresses?
       :identical-choices?       identical-choices?}]]))
