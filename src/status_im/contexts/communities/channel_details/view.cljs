(ns status-im.contexts.communities.channel-details.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
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
        chat-id                    (rf/sub [:get-screen-params :group-chat-profile])
        {:keys [admins] :as group} (rf/sub [:chats/chat-by-id chat-id])
        admin?                     (get admins (rf/sub [:multiaccount/public-key]))]
    [rn/view {:flex 1 :margin-top 20}
     [rn/touchable-opacity
      {:on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :close-manage-members
       :style               (style/close-icon)}
      [quo/icon :i/close {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
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
       :content-container-style        {:padding-bottom 20}
       :render-data                    {:group group}
       :render-fn                      add-member-contact-item-render}]
     [rn/view {:style (style/bottom-container 30)}
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

(defn actions
  [chat-id cover-bg-color]
  (let [latest-pin-text                      (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count                           (rf/sub [:chats/pin-messages-count chat-id])
        {:keys [muted muted-till chat-type]} (rf/sub [:chats/chat-by-id chat-id])
        community-channel?                   (= constants/community-chat-type chat-type)
        muted?                               (and muted (some? muted-till))
        mute-chat-label                      (if community-channel? :t/mute-channel :t/mute-chat)
        unmute-chat-label                    (if community-channel?
                                               :t/unmute-channel
                                               :t/unmute-chat)]
    [quo/channel-actions
     {:container-style {:margin-vertical 20 :padding-horizontal 20}
      :actions         [{:accessibility-label :action-button-pinned
                         :big?                true
                         :label               (or latest-pin-text
                                                  (i18n/label :t/no-pinned-messages))
                         :customization-color cover-bg-color
                         :icon                :i/pin
                         :counter-value       pins-count
                         :on-press            (fn []
                                                (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                              chat-id]))}
                        {:accessibility-label :action-button-mute
                         :label               (i18n/label (if muted
                                                            unmute-chat-label
                                                            mute-chat-label))
                         :customization-color cover-bg-color
                         :icon                (if muted? :i/activity-center :i/muted)
                         :on-press            (fn []
                                                (if muted?
                                                  (actions/unmute-chat-action chat-id)
                                                  (actions/mute-chat-action chat-id
                                                                            chat-type
                                                                            muted?)))}]}]))


(defn channel-details
  []
  (let [chat-id         (rf/sub [:get-screen-params :channel-chat-profile])
        {:keys [admins chat-id community-id chat-name emoji color description]
         :as   channel} (rf/sub [:chats/chat-by-id chat-id])
        display-name    (str (when emoji (str emoji " ")) "# " chat-name)
        current-pk      (rf/sub [:multiaccount/public-key])
        admin?          (get admins current-pk)
        contacts        (rf/sub [:communities/current-channel-contacts community-id
                                 (string/replace chat-id community-id "")])]
    [:<>
     [quo/page-nav
      {:type       :no-title
       :background :photo
       :right-side [{:icon-name :i/options
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content (fn [] [actions/chat-actions
                                                                 channel])}])}]
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]

     [quo/page-top
      {:title            display-name
       :description      :text
       :description-text description
       :avatar           {:customization-color color}}]

     [actions chat-id color]

     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :sections                       contacts
       :render-section-header-fn       contacts-section-header
       :render-section-footer-fn       contacts-section-footer
       :render-data                    {:chat-id chat-id
                                        :admin?  admin?}
       :render-fn                      contact-item-render}]
    ]))
