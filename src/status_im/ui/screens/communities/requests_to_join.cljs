(ns status-im.ui.screens.communities.requests-to-join
  (:require [quo.react-native :as rn]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n.i18n :as i18n]
            [status-im.communities.core :as communities]
            [quo.components.animated.pressable :as animation]))

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn request-actions [community-id request-id]
  [react/view {:flex-direction :row}
   [animation/pressable {:on-press #(re-frame/dispatch [:communities.ui/accept-request-to-join-pressed community-id request-id])}
    [icons/icon :main-icons/checkmark-circle {:width 35
                                              :height 35
                                              :color colors/green}]]
   [animation/pressable {:on-press #(re-frame/dispatch [:communities.ui/decline-request-to-join-pressed community-id request-id])}
    [icons/icon :main-icons/cancel {:width 35
                                    :height 35
                                    :container-style {:margin-left 16}
                                    :color colors/red}]]])

(defn render-request [{:keys [id public-key]} _ _ {:keys [community-id
                                                          can-manage-users?]}]
  (let [member (or (<sub [:contacts/contact-by-identity public-key])
                   {:public-key public-key})]
    [quo/list-item
     {:title               (multiaccounts/displayed-name member)
      :accessibility-label :member-item
      :accessory-style     (when can-manage-users?
                             {:flex-basis 120})
      :accessory           (when can-manage-users?
                             [request-actions community-id id])
      :icon                [chat-icon/contact-icon-contacts-tab
                            (multiaccounts/displayed-photo member)]
      :on-press            #(re-frame/dispatch [:chat.ui/show-profile public-key])}]))

(defn requests-to-join []
  (let [{:keys [community-id]} (<sub [:get-screen-params])]
    (fn []
      (let [requests (<sub [:communities/requests-to-join-for-community community-id])
            {:keys [can-manage-users?]}    (<sub [:communities/community community-id])]
        [:<>
         [topbar/topbar {:title    (i18n/label :t/community-requests-to-join-title)
                         :subtitle (str (count requests))}]
         [rn/flat-list {:data        requests
                        :render-data {:community-id community-id
                                      :can-manage-users? can-manage-users?}
                        :key-fn      :id
                        :render-fn   render-request}]]))))

(defn requests-to-join-container []
  (reagent/create-class
   {:display-name "community-requests-to-join-view"
    :component-did-mount (fn []
                           (communities/fetch-requests-to-join! (get (<sub [:get-screen-params]) :community-id)))
    :reagent-render requests-to-join}))
