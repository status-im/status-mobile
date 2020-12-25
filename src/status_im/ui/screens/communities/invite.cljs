(ns status-im.ui.screens.communities.invite
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.communities.core :as communities]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.multiaccounts.core :as multiaccounts]
            [clojure.string :as str]))

(defn header [user-pk]
  [:<>
   [rn/view {:style {:padding-horizontal 16
                     :padding-vertical   8}}
    [quo/text-input
     {:label          (i18n/label :t/enter-user-pk)
      :placeholder    (i18n/label :t/enter-user-pk)
      :on-change-text #(reset! user-pk %)
      :default-value  @user-pk
      :auto-focus     true}]]
   [quo/separator {:style {:margin-vertical 8}}]
   [quo/list-header (i18n/label :t/contacts)]])

(defn contacts-list-item [{:keys [public-key active] :as contact} _ _ {:keys [selected]}]
  (let [[first-name second-name] (multiaccounts/contact-two-names contact true)]
    [quo/list-item
     {:title     first-name
      :subtitle  second-name
      :icon      [chat-icon.screen/contact-icon-contacts-tab
                  (multiaccounts/displayed-photo contact)]
      :accessory :checkbox
      :active    active
      :on-press  (fn []
                   (if active
                     (swap! selected disj public-key)
                     (swap! selected conj public-key)))}]))

(defn invite []
  (let [user-pk           (reagent/atom "")
        contacts-selected (reagent/atom #{})]
    (fn []
      (let [contacts-data (<sub [:contacts/active])
            selected      @contacts-selected
            contacts      (map (fn [{:keys [public-key] :as contact}]
                                 (assoc contact :active (contains? selected public-key)))
                               contacts-data)]
        [:<>
         [topbar/topbar {:title (i18n/label :t/community-invite-title)}]
         [rn/flat-list {:style                   {:flex 1}
                        :content-container-style {:padding-vertical 16}
                        :header                  [header user-pk]
                        :render-data             {:selected contacts-selected}
                        :render-fn               contacts-list-item
                        :key-fn                  (fn [{:keys [active public-key]}]
                                                   (str public-key active))
                        :data                    contacts}]
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button {:disabled (str/blank? @user-pk)
                        :type     :secondary
                        :on-press #(>evt [::communities/invite-people-confirmation-pressed @user-pk selected])}
            (i18n/label :t/invite)]}]]))))
