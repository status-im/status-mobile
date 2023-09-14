(ns status-im2.contexts.chat.group-details.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.chat.group-details.style :as style]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.home.actions.view :as actions]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]))

(defn back-button
  []
  [quo/button
   {:type                :grey
    :size                32
    :icon-only?          true
    :container-style     {:margin-left 20}
    :accessibility-label :back-button
    :on-press            #(rf/dispatch [:navigate-back])}
   :i/arrow-left])

(defn options-button
  []
  (let [group (rf/sub [:chats/current-chat])]
    [quo/button
     {:type                :grey
      :size                32
      :icon-only?          true
      :container-style     {:margin-right 20}
      :accessibility-label :options-button
      :on-press            #(rf/dispatch [:show-bottom-sheet
                                          {:content (fn [] [actions/group-details-actions group])}])}
     :i/options]))

(defn top-buttons
  []
  [rn/view
   {:style {:flex-direction     :row
            :padding-horizontal 20
            :justify-content    :space-between}}
   [back-button] [options-button]])

(defn count-container
  [amount accessibility-label]
  [rn/view
   {:style               (style/count-container)
    :accessibility-label accessibility-label}
   [quo/text
    {:size   :label
     :weight :medium
     :style  {:text-align :center}}
    amount]])

(defn contacts-section-header
  [{:keys [title]}]
  [rn/view
   {:style {:padding-horizontal 20
            :border-top-width   1
            :border-top-color   colors/neutral-20
            :padding-vertical   8
            :margin-top         8}}
   [quo/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} title]])

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
           {:on-press  on-toggle
            :accessory {:type     :checkbox
                        :checked? @checked?
                        :on-check on-toggle}})
         item]))))

(defn add-manage-members
  []
  (let [selected-participants      (rf/sub [:group-chat/selected-participants])
        deselected-members         (rf/sub [:group-chat/deselected-members])
        {:keys [admins] :as group} (rf/sub [:chats/current-chat])
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
                                                 [:group-chats.ui/remove-members-pressed]))
                                              500)
                               (rf/dispatch [:group-chats.ui/add-members-pressed]))
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
       {:on-press      #(rf/dispatch [:chat.ui/show-profile public-key])
        :on-long-press show-profile-actions
        :accessory     {:type     :options
                        :on-press show-profile-actions}})
     item]))

(defn group-details
  []
  (let [{:keys [admins chat-id chat-name color public?
                muted contacts]} (rf/sub [:chats/current-chat])
        members                  (rf/sub [:contacts/group-members-sections])
        pinned-messages          (rf/sub [:chats/pinned chat-id])
        current-pk               (rf/sub [:multiaccount/public-key])
        admin?                   (get admins current-pk)]
    [rn/view
     {:style {:flex             1
              :background-color (colors/theme-colors colors/white colors/neutral-95)}}
     [quo/header
      {:left-component  [back-button]
       :right-component [options-button]
       :background      (colors/theme-colors colors/white colors/neutral-95)}]
     [rn/view
      {:style {:flex-direction     :row
               :margin-top         24
               :padding-horizontal 20}}
      [quo/group-avatar
       {:customization-color color
        :size                :size-32}]
      [quo/text
       {:weight :semi-bold
        :size   :heading-1
        :style  {:margin-horizontal 8}} chat-name]
      [rn/view {:style {:margin-top 8}}
       [quo/icon (if public? :i/world :i/privacy)
        {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
     [rn/view {:style (style/actions-view)}
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :pinned-messages
        :on-press            (fn []
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}
       [rn/view
        {:style {:flex-direction  :row
                 :justify-content :space-between}}
        [quo/icon :i/pin {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count pinned-messages) :pinned-count]]
       [quo/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label :t/pinned-messages)]]
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :toggle-mute
        :on-press            #(rf/dispatch [:chat.ui/mute chat-id (not muted)
                                            (when-not muted constants/mute-till-unmuted)])}
       [quo/icon (if muted :i/muted :i/activity-center)
        {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
       [quo/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label (if muted :unmute-group :mute-group))]]
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :manage-members
        :on-press            (fn []
                               (rf/dispatch [:group/clear-added-participants])
                               (rf/dispatch [:group/clear-removed-members])
                               (rf/dispatch [:open-modal :group-add-manage-members]))}
       [rn/view
        {:style {:flex-direction  :row
                 :justify-content :space-between}}
        [quo/icon :i/add-user {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count contacts) :members-count]]
       [quo/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label (if admin? :t/manage-members :t/add-members))]]]
     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :sections                       members
       :render-section-header-fn       contacts-section-header
       :render-data                    {:chat-id chat-id
                                        :admin?  admin?}
       :render-fn                      contact-item-render}]]))
