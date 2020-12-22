(ns status-im.ui.screens.communities.members
  (:require [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n :as i18n]
            [status-im.communities.core :as communities]))

(defn render-member [public-key]
  (let [member (or (<sub [:contacts/contact-by-address public-key])
                   {:public-key public-key})]
    [quo/list-item
     (merge
      {:title               (multiaccounts/displayed-name member)
       :accessibility-label :member-item
       :icon                [chat-icon/contact-icon-contacts-tab
                             (multiaccounts/displayed-photo member)]})]))

(defn members [route]
  (let [{:keys [community-id]}           (get-in route [:route :params])
        {{:keys [members]} :description} (<sub [:communities/community community-id])]
    [:<>
     [topbar/topbar {:title    (i18n/label :t/community-members-title)
                     :subtitle (str (count members))}]
     [quo/list-item {:icon                :main-icons/share
                     :title               (i18n/label :t/invite-people)
                     :accessibility-label :community-invite-people
                     :theme               :accent
                     :on-press            #(>evt [::communities/invite-people-pressed community-id])}]
     [quo/separator {:style {:margin-vertical 8}}]
     [rn/flat-list {:data      (keys members)
                    :key-fn    identity
                    :render-fn render-member}]]))
