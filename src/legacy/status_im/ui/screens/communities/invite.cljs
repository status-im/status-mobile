(ns legacy.status-im.ui.screens.communities.invite
  (:require
    [clojure.string :as string]
    [legacy.status-im.communities.core :as communities]
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon.screen]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn header
  [user-pk]
  [:<>
   [rn/view
    {:style {:padding-horizontal 16
             :padding-vertical   8}}
    [quo/text-input
     {:label          (i18n/label :t/enter-user-pk)
      :placeholder    (i18n/label :t/enter-user-pk)
      :on-change-text #(reset! user-pk %)
      :default-value  @user-pk
      :auto-focus     true}]]
   [quo/separator {:style {:margin-vertical 8}}]
   [quo/list-header (i18n/label :t/contacts)]])

(defn contacts-list-item
  [{:keys [public-key active] :as contact} _ _ {:keys [selected]}]
  (let [{:keys [primary-name secondary-name]} contact]
    [list.item/list-item
     {:title     primary-name
      :subtitle  secondary-name
      :icon      [chat-icon.screen/contact-icon-contacts-tab contact]
      :accessory :checkbox
      :active    active
      :on-press  (fn []
                   (if active
                     (swap! selected disj public-key)
                     (swap! selected conj public-key)))}]))

(defn legacy-invite
  []
  (let [user-pk           (reagent/atom "")
        contacts-selected (reagent/atom #{})
        {:keys [invite?]} (rf/sub [:get-screen-params])]
    (fn []
      (let [contacts-data (rf/sub [:contacts/active])
            {:keys [permissions
                    can-manage-users?]}
            (rf/sub [:communities/edited-community])
            selected @contacts-selected
            contacts (map (fn [{:keys [public-key] :as contact}]
                            (assoc contact :active (contains? selected public-key)))
                          contacts-data)
            ;; no-membership communities can only be shared
            can-invite? (and can-manage-users?
                             invite?
                             (not= (:access permissions) constants/community-no-membership-access))]
        [:<>
         [topbar/topbar
          {:title  (i18n/label (if can-invite?
                                 :t/invite-people
                                 :t/community-share-title))
           :modal? true}]
         [rn/flat-list
          {:style                   {:flex 1}
           :content-container-style {:padding-vertical 16}
           ;:header                  [header user-pk]
           :render-data             {:selected contacts-selected}
           :render-fn               contacts-list-item
           :key-fn                  (fn [{:keys [active public-key]}]
                                      (str public-key active))
           :data                    contacts}]
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button
            {:disabled            (and (string/blank? @user-pk)
                                       (zero? (count selected)))
             :accessibility-label :share-community-link
             :type                :secondary
             :on-press            #(debounce/dispatch-and-chill
                                    [(if can-invite?
                                       ::communities/invite-people-confirmation-pressed
                                       ::communities/share-community-confirmation-pressed) @user-pk
                                     selected]
                                    3000)}
            (i18n/label (if can-invite? :t/invite :t/share))]}]]))))
