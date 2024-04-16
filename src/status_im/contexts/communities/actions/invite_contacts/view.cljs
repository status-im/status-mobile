(ns status-im.contexts.communities.actions.invite-contacts.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.share :as share]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.common.contact-list.view :as contact-list]
    [status-im.contexts.communities.actions.invite-contacts.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- no-contacts-view
  [{:keys [theme id]}]
  (let [customization-color             (rf/sub [:profile/customization-color])
        {:keys [universal-profile-url]} (rf/sub [:profile/profile])
        on-press-share-community        (rn/use-callback
                                         #(rf/dispatch [:communities/share-community-url-with-data
                                                        id]))
        on-press-share-profile          (rn/use-callback #(share/open {:url universal-profile-url})
                                                         [universal-profile-url])]
    [rn/view
     {:style (style/no-contacts)}
     [rn/image {:source (resources/get-themed-image :no-contacts-to-chat theme)}]
     [quo/text
      {:weight :semi-bold
       :size   :paragraph-1
       :style  style/no-contacts-text}
      (i18n/label :t/you-have-no-contacts)]
     [quo/text
      {:weight :regular
       :size   :paragraph-2}
      (i18n/label :t/dont-yell-at-me)]
     [quo/button
      {:customization-color customization-color
       :theme               theme
       :type                :primary
       :size                32
       :container-style     style/no-contacts-button-container
       :on-press            on-press-share-community}
      (i18n/label :t/send-community-link)]
     [quo/button
      {:customization-color customization-color
       :theme               theme
       :type                :grey
       :size                32
       :on-press            on-press-share-profile}
      (i18n/label :t/invite-friends-to-status)]]))

(defn- contact-item
  [{:keys [public-key]
    :as   item}]
  (let [user-selected?         (rf/sub [:is-contact-selected? public-key])
        {:keys [id]}           (rf/sub [:get-screen-params])
        community-members-keys (set (keys (rf/sub [:communities/community-members id])))
        community-member?      (boolean (community-members-keys public-key))
        on-toggle              (fn []
                                 (when-not community-member?
                                   (if user-selected?
                                     (rf/dispatch [:deselect-contact public-key])
                                     (rf/dispatch [:select-contact public-key]))))]
    [contact-list-item/contact-list-item
     {:on-press                on-toggle
      :allow-multiple-presses? true
      :accessory               {:type      :checkbox
                                :disabled? community-member?
                                :checked?  (or community-member? user-selected?)
                                :on-check  on-toggle}
      :disabled?               community-member?}
     item]))

(defn view
  []
  (fn []
    (rn/use-unmount #(rf/dispatch [:group-chat/clear-contacts]))
    (let [theme                   (quo.theme/use-theme)
          customization-color     (rf/sub [:profile/customization-color])
          {:keys [id]}            (rf/sub [:get-screen-params])
          contacts                (rf/sub [:contacts/filtered-active-sections])
          selected                (rf/sub [:group/selected-contacts])
          {:keys [name images]}   (rf/sub [:communities/community id])
          selected-contacts-count (count selected)
          on-press                (fn []
                                    (rf/dispatch [:communities/share-community-confirmation-pressed
                                                  selected id])
                                    (rf/dispatch [:navigate-back])
                                    (rf/dispatch [:toasts/upsert
                                                  {:type  :positive
                                                   :theme theme
                                                   :text  (if (= 1 selected-contacts-count)
                                                            (i18n/label :t/one-user-was-invited)
                                                            (i18n/label
                                                             :t/n-users-were-invited
                                                             {:count selected-contacts-count}))}]))
          {window-height :height} (rn/get-window)]
      [rn/view {:style {:flex 1}}
       [rn/view {:style {:padding-horizontal 20}}
        [quo/button
         {:type       :grey
          :size       32
          :icon-only? true
          :on-press   #(rf/dispatch [:navigate-back])}
         :i/close]
        [rn/view {:style style/contact-selection-heading}
         [quo/text
          {:weight :semi-bold
           :size   :heading-1
           :style  (style/invite-to-community-text theme)}
          (i18n/label :t/invite-to-community)]]
        [quo/context-tag
         {:type            :community
          :size            24
          :community-logo  (:thumbnail images)
          :community-name  name
          :container-style style/context-tag}]]
       (if (empty? contacts)
         [no-contacts-view
          {:theme theme
           :id    id}]
         [:<>
          [gesture/section-list
           {:key-fn                         :public-key
            :sticky-section-headers-enabled true
            :sections                       contacts
            :render-section-header-fn       contact-list/contacts-section-header
            :content-container-style        (style/section-list-container-style theme)
            :render-fn                      contact-item
            :style                          {:height window-height}}]
          (when (pos? selected-contacts-count)
            [quo/button
             {:type                :primary
              :accessibility-label :next-button
              :customization-color customization-color
              :container-style     style/chat-button
              :on-press            on-press}
             (if (= 1 selected-contacts-count)
               (i18n/label :t/invite-1-user)
               (i18n/label :t/invite-n-users {:count selected-contacts-count}))])])])))
