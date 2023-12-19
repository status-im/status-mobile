(ns legacy.status-im.ui.screens.communities.members
  (:require
    [legacy.status-im.communities.core :as communities]
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.components.unviewed-indicator :as unviewed-indicator]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide-old])
  (rf/dispatch event))

(defn member-sheet
  [primary-name {:keys [public-key] :as member} community-id can-kick-users? can-manage-users? admin?]
  [:<>
   [list.item/list-item
    {:theme               :accent
     :icon                [chat-icon/contact-icon-contacts-tab member]
     :title               primary-name
     :subtitle            (i18n/label :t/view-profile)
     :accessibility-label :view-chat-details-button
     :chevron             true
     :on-press            #(hide-sheet-and-dispatch [:chat.ui/show-profile public-key])}]
   (when can-kick-users?
     [:<>
      [quo/separator {:style {:margin-vertical 8}}]
      [list.item/list-item
       {:theme    :negative
        :icon     :main-icons/arrow-left
        :title    (i18n/label :t/member-kick)
        :on-press #(rf/dispatch [::communities/member-kick community-id public-key])}]])
   (when can-manage-users?
     [:<>
      [list.item/list-item
       {:theme    :negative
        :icon     :main-icons/cancel
        :title    (i18n/label :t/member-ban)
        :on-press #(rf/dispatch [::communities/member-ban community-id public-key])}]])
   (when admin?
     [:<>
      [list.item/list-item
       {:theme    :accent
        :icon     :main-icons/make-admin
        :title    (i18n/label :t/make-moderator)
        :on-press #(rf/dispatch [:community.member/add-role community-id public-key
                                 constants/community-member-role-moderator])}]])])

(defn render-member
  [public-key _ _
   {:keys [community-id
           my-public-key
           can-manage-users?
           can-kick-users?
           admin?]}]
  (let [member                        (rf/sub [:contacts/contact-by-identity public-key])
        [primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity public-key])]
    [list.item/list-item
     {:title               primary-name
      :subtitle            secondary-name
      :accessibility-label :member-item
      :icon                [chat-icon/profile-photo-plus-dot-view
                            {:public-key public-key
                             :photo-path (profile.utils/photo member)}]
      :accessory           (when (not= public-key my-public-key)
                             [quo/button
                              {:on-press
                               #(rf/dispatch [:bottom-sheet/show-sheet-old
                                              {:content (fn []
                                                          [member-sheet primary-name member community-id
                                                           can-kick-users? can-manage-users? admin?])}])
                               :type :icon
                               :theme :icon
                               :accessibility-label :menu-option}
                              :main-icons/more])}]))

(defn header
  [community-id]
  [:<>
   [list.item/list-item
    {:icon                :main-icons/share
     :title               (i18n/label :t/invite-people)
     :accessibility-label :community-invite-people
     :theme               :accent
     :on-press            #(rf/dispatch [:communities/invite-people-pressed community-id])}]
   [quo/separator {:style {:margin-vertical 8}}]])

(defn requests-to-join
  [community-id]
  (let [requests       (rf/sub [:communities/requests-to-join-for-community community-id])
        requests-count (count requests)]
    [:<>
     [list.item/list-item
      {:chevron true
       :accessory
       [react/view {:flex-direction :row}
        (when (pos? requests-count)
          [unviewed-indicator/unviewed-indicator requests-count])]
       :on-press #(rf/dispatch [:navigate-to :community-requests-to-join {:community-id community-id}])
       :title (i18n/label :t/membership-requests)}]
     [quo/separator {:style {:margin-vertical 8}}]]))

(defn members
  []
  (let [{:keys [community-id]} (rf/sub [:get-screen-params])]
    (fn []
      (let [my-public-key (rf/sub [:multiaccount/public-key])
            {:keys [permissions
                    can-manage-users?
                    admin]}
            (rf/sub [:communities/community community-id])
            sorted-members (rf/sub [:communities/sorted-community-members
                                    community-id])]
        [:<>
         [topbar/topbar
          {:title    (i18n/label :t/community-members-title)
           :subtitle (str (count sorted-members))}]
         [header community-id]
         (when (and can-manage-users? (= constants/community-on-request-access (:access permissions)))
           [requests-to-join community-id])
         [rn/flat-list
          {:data        (keys sorted-members)
           :render-data {:community-id      community-id
                         :my-public-key     my-public-key
                         :can-kick-users?   (and can-manage-users?
                                                 (not= (:access permissions)
                                                       constants/community-no-membership-access))
                         :can-manage-users? can-manage-users?
                         :admin?            admin}
           :key-fn      identity
           :render-fn   render-member}]]))))

(defn legacy-members-container
  []
  (reagent/create-class
   {:display-name        "community-members-view"
    :component-did-mount (fn []
                           (rf/dispatch [:community/fetch-requests-to-join
                                         (get (rf/sub [:get-screen-params])
                                              :community-id)]))
    :reagent-render      members}))
