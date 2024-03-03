(ns legacy.status-im.ui.screens.communities.invite
  (:require
    [legacy.status-im.communities.core :as communities]
    [legacy.status-im.ui.screens.communities.style :as style]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.share :as share]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.common.contact-list.view :as contact-list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- no-contacts-view
  [{:keys [theme id]}]
  (let [customization-color             (rf/sub [:profile/customization-color])
        {:keys [universal-profile-url]} (rf/sub [:profile/profile])]
    (fn []
      [rn/view
       {:style (style/no-contacts)}
       [rn/image {:source (resources/get-themed-image :no-contacts-to-chat theme)}]
       [quo/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  {:margin-bottom 2
                  :margin-top    12}}
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
         :container-style     {:margin-top    20
                               :margin-bottom 12}
         :on-press            #(rf/dispatch [:communities/share-community-url-with-data id])}
        (i18n/label :t/send-community-link)]
       [quo/button
        {:customization-color customization-color
         :theme               theme
         :type                :grey
         :size                32
         :on-press            #(do
                                 (share/open {:url universal-profile-url}))}
        (i18n/label :t/invite-friends-to-status)]])))

(defn- contact-item-render
  [_]
  (fn [{:keys [public-key] :as item}]
    (let [user-selected? (rf/sub [:is-contact-selected? public-key])
          on-toggle      #(if user-selected?
                            (rf/dispatch [:deselect-contact public-key])
                            (rf/dispatch [:select-contact public-key]))]
      [contact-list-item/contact-list-item
       {:on-press                on-toggle
        :allow-multiple-presses? true
        :accessory               {:type     :checkbox
                                  :checked? user-selected?
                                  :on-check on-toggle}}
       item])))

(defn view-internal
  [{:keys [theme]}]
  (let [{:keys [id]}          (rf/sub [:get-screen-params])
        {:keys [name images]} (rf/sub [:communities/community id])]
    (fn []
      (rn/use-unmount #(rf/dispatch [:group-chat/clear-contacts]))
      (let [selected                (rf/sub [:group/selected-contacts])
            selected-contacts-count (count selected)
            on-press                (fn []
                                      (rf/dispatch [::communities/share-community-confirmation-pressed
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
            window-height           (:height (rn/get-window))]
        [rn/view {:flex 1}
         [rn/view {:padding-horizontal 20}
          [quo/button
           {:type       :grey
            :size       32
            :icon-only? true
            :on-press   #(rf/dispatch [:navigate-back])} :i/close]
          [rn/view {:style style/contact-selection-heading}
           [quo/text
            {:weight :semi-bold
             :size   :heading-1
             :style  {:color (colors/theme-colors colors/neutral-100 colors/white theme)}}
            (i18n/label :t/invite-to-community)]]
          [quo/context-tag
           {:type            :community
            :size            24
            :community-logo  (:thumbnail images)
            :community-name  name
            :container-style {:align-self    :flex-start
                              :margin-top    -8
                              :margin-bottom 12}}]]
         (if (empty? (rf/sub [:contacts/filtered-active-sections]))
           [no-contacts-view
            {:theme theme
             :id    id}]
           [:<>
            [gesture/section-list
             {:key-fn                         :public-key
              :sticky-section-headers-enabled false
              :sections                       (rf/sub [:contacts/filtered-active-sections])
              :render-section-header-fn       contact-list/contacts-section-header
              :content-container-style        {:padding-bottom   70
                                               :background-color (colors/theme-colors colors/white
                                                                                      colors/neutral-95
                                                                                      theme)}
              :render-fn                      contact-item-render
              :style                          {:height window-height}}]
            (when (pos? selected-contacts-count)
              [rn/view {:style style/chat-button}
               [quo/button
                {:type                :primary
                 :accessibility-label :next-button
                 :on-press            on-press}
                (if (= 1 selected-contacts-count)
                  (i18n/label :t/invite-1-user)
                  (i18n/label :t/invite-n-users {:count selected-contacts-count}))]])])]))))

(def legacy-invite (quo.theme/with-theme view-internal))
