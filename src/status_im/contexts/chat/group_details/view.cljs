(ns status-im.contexts.chat.group-details.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.common.contact-list.view :as contact-list]
    [status-im.common.home.actions.view :as actions]
    [status-im.constants :as constants]
    [status-im.contexts.chat.group-details.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn group-chat-member-toggle
  [member? selected? public-key]
  (if-not member?
    (if selected?
      (rf/dispatch [:select-participant public-key true])
      (rf/dispatch [:deselect-participant public-key true]))
    (if selected?
      (rf/dispatch [:undo-deselect-member public-key true])
      (rf/dispatch [:deselect-member public-key true]))))

(defn add-member-contact-item-render
  [{:keys [public-key] :as item} _ _ {:keys [group]}]
  (let [current-pk         (rf/sub [:multiaccount/public-key])
        {:keys [contacts]} group
        member?            (contains? contacts public-key)
        checked?           (reagent/atom member?)]
    (fn []
      (let [on-toggle #(group-chat-member-toggle member? (swap! checked? not) public-key)]
        [contact-list-item/contact-list-item
         (when (not= current-pk public-key)
           {:on-press                on-toggle
            :allow-multiple-presses? true
            :accessory               {:type     :checkbox
                                      :checked? @checked?
                                      :on-check on-toggle}})
         item]))))

(defn add-manage-members
  []
  (let [selected-participants      (rf/sub [:group-chat/selected-participants])
        deselected-members         (rf/sub [:group-chat/deselected-members])
        chat-id                    (rf/sub [:get-screen-params :group-details])
        {:keys [admins] :as group} (rf/sub [:chats/chat-by-id chat-id])
        theme                      (quo.theme/use-theme)
        admin?                     (get admins (rf/sub [:multiaccount/public-key]))]
    [rn/view {:flex 1 :margin-top 20}
     [rn/touchable-opacity
      {:on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :close-manage-members
       :style               (style/close-icon theme)}
      [quo/icon :i/close {:color (colors/theme-colors colors/neutral-100 colors/white theme)}]]
     [quo/text
      {:size   :heading-1
       :weight :semi-bold
       :style  {:margin-left 20}}
      (i18n/label (if admin? :t/manage-members :t/add-members))]
     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :sections                       (rf/sub [:contacts/grouped-by-first-letter])
       :render-section-header-fn       contact-list/contacts-section-header
       :render-section-footer-fn       contact-list/contacts-section-footer
       :content-container-style        {:padding-bottom 20}
       :render-data                    {:group group}
       :render-fn                      add-member-contact-item-render}]
     [rn/view {:style (style/bottom-container 30 theme)}
      [quo/button
       {:container-style     {:flex 1}
        :type                :primary
        :accessibility-label :save
        :on-press            (fn []
                               (rf/dispatch [:navigate-back])
                               (js/setTimeout (fn []
                                                (rf/dispatch
                                                 [:group-chats.ui/remove-members-pressed chat-id]))
                                              500)
                               (rf/dispatch [:group-chats.ui/add-members-pressed chat-id]))
        :disabled?           (and (zero? (count selected-participants))
                                  (zero? (count deselected-members)))}
       (i18n/label :t/save)]]]))

(defn contact-item-render
  [{:keys [public-key] :as item} _ _ extra-data]
  (let [current-pk           (rf/sub [:multiaccount/public-key])
        show-profile-actions #(rf/dispatch [:show-bottom-sheet
                                            {:content (fn [] [actions/contact-actions item
                                                              extra-data])}])]
    [contact-list-item/contact-list-item
     (when (not= public-key current-pk)
       {:on-press                #(rf/dispatch [:chat.ui/show-profile public-key])
        :allow-multiple-presses? true
        :on-long-press           show-profile-actions
        :accessory               {:type     :options
                                  :on-press show-profile-actions}})
     item]))

(defn contacts-section-header
  [{:keys [title]}]
  [quo/divider-label {:tight? true} title])

(defn contacts-section-footer
  [_]
  [rn/view {:style {:height 8}}])

(defn group-details
  []
  (let [chat-id         (rf/sub [:get-screen-params :group-details])
        {:keys [admins chat-id chat-name color muted contacts image]
         :as   group}   (rf/sub [:chats/chat-by-id chat-id])
        members         (rf/sub [:contacts/group-members-sections chat-id])
        pinned-messages (rf/sub [:chats/pinned chat-id])
        current-pk      (rf/sub [:multiaccount/public-key])
        profile-color   (rf/sub [:profile/customization-color])
        admin?          (get admins current-pk)]
    [:<>
     [quo/gradient-cover
      {:height              286
       :customization-color color}]
     [quo/page-nav
      {:type       :no-title
       :margin-top (safe-area/get-top)
       :background :photo
       :right-side [{:icon-name :i/options
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content (fn [] [actions/group-details-actions
                                                                 group])}])}]
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]
     [quo/page-top
      {:title  chat-name
       :avatar {:group?              true
                :picture             (when image {:uri image})
                :customization-color color}}]
     [quo/channel-actions
      {:container-style style/actions-view
       :actions         [{:accessibility-label :pinned-messages
                          :label               (i18n/label :t/pinned-messages)
                          :customization-color color
                          :icon                :i/pin
                          :counter-value       (count pinned-messages)
                          :on-press            (fn []
                                                 (rf/dispatch [:dismiss-keyboard])
                                                 (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                               chat-id]))}
                         {:accessibility-label :toggle-mute
                          :customization-color color
                          :icon                (if muted :i/muted :i/activity-center)
                          :label               (i18n/label (if muted :unmute-group :mute-group))
                          :on-press            #(rf/dispatch [:chat.ui/mute chat-id (not muted)
                                                              (when-not muted
                                                                constants/mute-till-unmuted)])}
                         {:accessibility-label :manage-members
                          :customization-color color
                          :icon                :i/add-user
                          :label               (i18n/label (if admin? :t/manage-members :t/add-members))
                          :counter-value       (count contacts)
                          :on-press            (fn []
                                                 (rf/dispatch [:group/clear-added-participants])
                                                 (rf/dispatch [:group/clear-removed-members])
                                                 (rf/dispatch [:open-modal :group-add-manage-members
                                                               chat-id]))}]}]
     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :sections                       members
       :render-section-header-fn       contacts-section-header
       :render-section-footer-fn       contacts-section-footer
       :render-data                    {:chat-id chat-id
                                        :admin?  admin?}
       :render-fn                      contact-item-render
       :separator                      [rn/view {:style {:height 4}}]}]
     [quo/floating-shell-button
      {:jump-to {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
                 :customization-color profile-color
                 :label               (i18n/label :t/jump-to)}}
      style/floating-shell-button]]))
