(ns status-im.ui.screens.communities.members
  (:require [quo.react-native :as rn]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.ui.components.react :as react]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.unviewed-indicator :as unviewed-indicator]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n.i18n :as i18n]
            [status-im.communities.core :as communities]))

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn member-sheet [first-name {:keys [public-key] :as member} community-id can-kick-users? can-manage-users?]
  [:<>
   [quo/list-item
    {:theme               :accent
     :icon                [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo member)]
     :title               first-name
     :subtitle            (i18n/label :t/view-profile)
     :accessibility-label :view-chat-details-button
     :chevron             true
     :on-press            #(hide-sheet-and-dispatch  [:chat.ui/show-profile public-key])}]
   (when can-kick-users?
     [:<>
      [quo/separator {:style {:margin-vertical 8}}]
      [quo/list-item {:theme    :negative
                      :icon     :main-icons/arrow-left
                      :title    (i18n/label :t/member-kick)
                      :on-press #(>evt [::communities/member-kick community-id public-key])}]])
   (when can-manage-users?
     [:<>
      [quo/list-item {:theme    :negative
                      :icon     :main-icons/cancel
                      :title    (i18n/label :t/member-ban)
                      :on-press #(>evt [::communities/member-ban community-id public-key])}]])])

(defn render-member [public-key _ _ {:keys [community-id
                                            my-public-key
                                            can-manage-users?
                                            can-kick-users?]}]
  (let [member (<sub [:contacts/contact-by-identity public-key])
        [first-name second-name] (<sub [:contacts/contact-two-names-by-identity public-key])]
    [quo/list-item
     {:title               first-name
      :subtitle            second-name
      :accessibility-label :member-item
      :icon                [chat-icon/contact-icon-contacts-tab
                            (multiaccounts/displayed-photo member)]
      :accessory           (when (not= public-key my-public-key)
                             [quo/button {:on-press
                                          #(>evt [:bottom-sheet/show-sheet
                                                  {:content (fn []
                                                              [member-sheet first-name member community-id can-kick-users? can-manage-users?])}])
                                          :type                :icon
                                          :theme               :icon
                                          :accessibility-label :menu-option}
                              :main-icons/more])}]))

(defn header [community-id]
  [:<>
   [quo/list-item {:icon                :main-icons/share
                   :title               (i18n/label :t/invite-people)
                   :accessibility-label :community-invite-people
                   :theme               :accent
                   :on-press            #(>evt [::communities/invite-people-pressed community-id])}]
   [quo/separator {:style {:margin-vertical 8}}]])

(defn requests-to-join [community-id]
  (let [requests (<sub [:communities/requests-to-join-for-community community-id])
        requests-count (count requests)]
    [:<>
     [quo/list-item {:chevron        true
                     :accessory
                     [react/view {:flex-direction :row}
                      (when (pos? requests-count)
                        [unviewed-indicator/unviewed-indicator requests-count])]
                     :on-press       #(>evt [:navigate-to :community-requests-to-join {:community-id community-id}])
                     :title          (i18n/label :t/membership-requests)}]
     [quo/separator {:style {:margin-vertical 8}}]]))

(defn members []
  (let [{:keys [community-id]}      (<sub [:get-screen-params])
        my-public-key               (<sub [:multiaccount/public-key])
        {:keys [members
                permissions
                can-manage-users?]} (<sub [:communities/community community-id])]
    [:<>
     [topbar/topbar {:title    (i18n/label :t/community-members-title)

                     :subtitle (str (count members))}]
     [header community-id]
     (when (and can-manage-users? (= constants/community-on-request-access (:access permissions)))
       [requests-to-join community-id])
     [rn/flat-list {:data        (keys members)
                    :render-data {:community-id community-id
                                  :my-public-key my-public-key
                                  :can-kick-users? (and can-manage-users?
                                                        (not= (:access permissions)
                                                              constants/community-no-membership-access))
                                  :can-manage-users? can-manage-users?}
                    :key-fn      identity
                    :render-fn   render-member}]]))

(defn members-container []
  (reagent/create-class
   {:display-name "community-members-view"
    :component-did-mount (fn []
                           (communities/fetch-requests-to-join! (get (<sub [:get-screen-params]) :community-id)))
    :reagent-render members}))
