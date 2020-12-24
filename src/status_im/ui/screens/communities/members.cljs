(ns status-im.ui.screens.communities.members
  (:require [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n :as i18n]
            [status-im.communities.core :as communities]))

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn member-sheet [{:keys [public-key] :as member} community-id admin]
  [:<>
   [quo/list-item
    {:theme               :accent
     :icon                [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo member)]
     :title               (multiaccounts/displayed-name member)
     :subtitle            (i18n/label :t/view-profile)
     :accessibility-label :view-chat-details-button
     :chevron             true
     :on-press            #(hide-sheet-and-dispatch  [:chat.ui/show-profile public-key])}]
   (when admin
     [:<>
      [quo/separator {:style {:margin-vertical 8}}]
      [quo/list-item {:theme    :negative
                      :icon     :main-icons/arrow-left
                      :title    (i18n/label :t/member-kick)
                      :on-press #(>evt [::communities/member-kick community-id public-key])}]
      [quo/list-item {:theme    :negative
                      :icon     :main-icons/cancel
                      :title    (i18n/label :t/member-ban)
                      :on-press #(>evt [::communities/member-ban community-id public-key])}]])])

(defn render-member [public-key _ _ {:keys [community-id admin]}]
  (let [member (or (<sub [:contacts/contact-by-address public-key])
                   {:public-key public-key})]
    [quo/list-item
     (merge
      {:title               (multiaccounts/displayed-name member)
       :accessibility-label :member-item
       :icon                [chat-icon/contact-icon-contacts-tab
                             (multiaccounts/displayed-photo member)]
       :accessory           [quo/button {:on-press            #(>evt [:bottom-sheet/show-sheet
                                                                      {:content (fn [] [member-sheet member community-id admin])}])
                                         :type                :icon
                                         :theme               :icon
                                         :accessibility-label :menu-option}
                             :main-icons/more]})]))

(defn members [route]
  (let [{:keys [community-id]}     (get-in route [:route :params])
        {{:keys [members]} :description
         admin             :admin} (<sub [:communities/community community-id])]
    [:<>
     [topbar/topbar {:title    (i18n/label :t/community-members-title)
                     :subtitle (str (count members))}]
     [quo/list-item {:icon                :main-icons/share
                     :title               (i18n/label :t/invite-people)
                     :accessibility-label :community-invite-people
                     :theme               :accent
                     :on-press            #(>evt [::communities/invite-people-pressed community-id])}]
     [quo/separator {:style {:margin-vertical 8}}]
     [rn/flat-list {:data        (keys members)
                    :render-data {:community-id community-id
                                  :admin        admin}
                    :key-fn      identity
                    :render-fn   render-member}]]))
