(ns status-im2.contexts.chat.group-details.view
  (:require [utils.i18n :as i18n]
            [quo.components.safe-area :as safe-area]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.chat.group-details.style :as style]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.home.actions.view :as actions]
            [utils.re-frame :as rf]))

(defn back-button
  []
  [quo2/button
   {:type                :grey
    :size                32
    :width               32
    :style               {:margin-left 20}
    :accessibility-label :back-button
    :on-press            #(rf/dispatch [:navigate-back])}
   [quo2/icon :i/arrow-left {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn options-button
  []
  (let [group (rf/sub [:chats/current-chat])]
    [quo2/button
     {:type                :grey
      :size                32
      :width               32
      :style               {:margin-right 20}
      :accessibility-label :options-button
      :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                          {:content (fn [] [actions/group-details-actions group])}])}
     [quo2/icon :i/options {:color (colors/theme-colors colors/neutral-100 colors/white)}]]))

(defn top-buttons
  []
  [rn/view
   {:style {:flex-direction     :row
            :padding-horizontal 20
            :justify-content    :space-between}}
   [back-button] [options-button]])

(defn count-container
  [count accessibility-label]
  [rn/view
   {:style               (style/count-container)
    :accessibility-label accessibility-label}
   [quo2/text
    {:size   :label
     :weight :medium
     :style  {:text-align :center}} count]])

(defn contacts-section-header
  [{:keys [title]}]
  [rn/view
   {:style {:padding-horizontal 20
            :border-top-width   1
            :border-top-color   colors/neutral-20
            :padding-vertical   8
            :margin-top         8}}
   [quo2/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} title]])

(defn add-members-sheet
  [group admin?]
  [:f>
   (fn []
     (let [{window-height :height} (rn/use-window-dimensions)
           safe-area               (safe-area/use-safe-area)
           selected-participants   (rf/sub [:group-chat/selected-participants])
           deselected-members      (rf/sub [:group-chat/deselected-members])]
       [rn/view {:style {:height (- window-height (:top safe-area))}}
        [rn/touchable-opacity
         {:on-press            #(rf/dispatch [:bottom-sheet/hide])
          :accessibility-label :close-manage-members
          :style               (style/close-icon)}
         [quo2/icon :i/close {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
        [quo2/text
         {:size   :heading-1
          :weight :semi-bold
          :style  {:margin-left 20}}
         (i18n/label (if admin? :t/manage-members :t/add-members))]
        [contact-list/contact-list
         {:icon    :check
          :group   group
          :search? true}]
        [rn/view {:style (style/bottom-container safe-area)}
         [quo2/button
          {:style               {:flex 1}
           :accessibility-label :save
           :on-press            (fn []
                                  (rf/dispatch
                                   [:bottom-sheet/hide])
                                  #(do
                                     (js/setTimeout (fn []
                                                      (rf/dispatch
                                                       [:group-chats.ui/remove-members-pressed]))
                                                    500)
                                     (rf/dispatch [:group-chats.ui/add-members-pressed])))
           :disabled            (and (zero? (count selected-participants))
                                     (zero? (count deselected-members)))}
          (i18n/label :t/save)]]]))])

(defn group-details
  []
  (let [{:keys [admins chat-id chat-name color public? muted contacts] :as group} (rf/sub
                                                                                   [:chats/current-chat])
        members (rf/sub [:contacts/group-members-sections])
        pinned-messages (rf/sub [:chats/pinned chat-id])
        current-pk (rf/sub [:multiaccount/public-key])
        admin? (get admins current-pk)]
    [rn/view
     {:style {:flex             1
              :background-color (colors/theme-colors colors/white colors/neutral-95)}}
     [quo2/header
      {:left-component  [back-button]
       :right-component [options-button]
       :background      (colors/theme-colors colors/white colors/neutral-95)}]
     [rn/view
      {:style {:flex-direction     :row
               :margin-top         24
               :padding-horizontal 20}}
      [quo2/group-avatar
       {:color color
        :size  :medium}]
      [quo2/text
       {:weight :semi-bold
        :size   :heading-1
        :style  {:margin-horizontal 8}} chat-name]
      [rn/view {:style {:margin-top 8}}
       [quo2/icon (if public? :i/world :i/privacy)
        {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
     [rn/view {:style (style/actions-view)}
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :pinned-messages
        :on-press            (fn []
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id]))}
       [rn/view
        {:style {:flex-direction  :row
                 :justify-content :space-between}}
        [quo2/icon :i/pin {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count pinned-messages) :pinned-count]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label :t/pinned-messages)]]
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :toggle-mute
        :on-press            #(rf/dispatch [:chat.ui/mute chat-id (not muted)])}
       [quo2/icon (if muted :i/muted :i/activity-center)
        {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label (if muted :unmute-group :mute-group))]]
      [rn/touchable-opacity
       {:style               (style/action-container color)
        :accessibility-label :manage-members
        :on-press            (fn []
                               (rf/dispatch [:group/clear-added-participants])
                               (rf/dispatch [:group/clear-removed-members])
                               (rf/dispatch
                                [:bottom-sheet/show-sheet
                                 {:content (fn [] [add-members-sheet group admin?])}]))}
       [rn/view
        {:style {:flex-direction  :row
                 :justify-content :space-between}}
        [quo2/icon :i/add-user {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count contacts) :members-count]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium}
        (i18n/label (if admin? :t/manage-members :t/add-members))]]]
     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :sections                       members
       :render-section-header-fn       contacts-section-header
       :render-fn                      contact-list-item/contact-list-item
       :render-data                    {:chat-id chat-id
                                        :admin?  admin?
                                        :icon    :options}}]]))
