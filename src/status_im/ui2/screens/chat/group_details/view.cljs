(ns status-im.ui2.screens.chat.group-details.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui2.screens.chat.group-details.style :as style]
            [quo2.core :as quo2]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [status-im2.common.contact-list-item.view :as contact-item]))

(defn back-button []
  [quo2/button {:type                :grey
                :size                32
                :style               {:margin-left 20}
                :accessibility-label :back-button
                :on-press            #(rf/dispatch [:navigate-back])
                :icon                true}
   :i/arrow-left])

(defn options-button []
  [quo2/button {:type                :grey
                :size                32
                :style               {:margin-right 20}
                :accessibility-label :options-button
                :icon                true}
   :i/options])

(defn count-container [count]
  [rn/view {:style (style/count-container)}
   [quo2/text {:size   :label
               :weight :medium
               :style  {:text-align :center}} count]])

(defn prepare-members [members]
  (let [admins (filter :admin? members)
        online (filter #(and (not (:admin? %)) (:online? %)) members)
        offline (filter #(and (not (:admin? %)) (not (:online? %))) members)]
    (vals (cond-> {}
            (seq admins) (assoc :owner {:title "Owner" :data admins})
            (seq online) (assoc :online {:title "Online" :data online})
            (seq offline) (assoc :offline {:title "Offline" :data offline})))))

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:padding-horizontal 20 :border-top-width 1 :border-top-color colors/neutral-20 :padding-vertical 8 :margin-top 8}}
   [quo2/text {:size   :paragraph-2
               :weight :medium
               :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} title]])

(defn group-details []
  (let [{:keys [admins chat-id chat-name color public?]} (rf/sub [:chats/current-chat])
        members (rf/sub [:contacts/current-chat-contacts])
        sectioned-members (prepare-members members)
        pinned-messages (rf/sub [:chats/pinned chat-id])
        current-pk (rf/sub [:multiaccount/public-key])
        admin? (get admins current-pk)]
    [rn/view {:style {:flex             1
                      :background-color (colors/theme-colors colors/white colors/neutral-95)}}
     [quo2/header {:left-component  [back-button]
                   :right-component [options-button]
                   :background      (colors/theme-colors colors/white colors/neutral-95)}]
     [rn/view {:style {:flex-direction     :row
                       :margin-top         12
                       :padding-horizontal 20}}
      [quo2/group-avatar {:color color
                          :size  :medium}]
      [quo2/text {:weight :semi-bold
                  :size   :heading-1
                  :style  {:margin-horizontal 8}} chat-name]
      [rn/view {:style {:margin-top 8}}
       [quo2/icon (if public? :i/world :i/privacy) {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
     [rn/view {:style (style/actions-view)}
      [rn/touchable-opacity {:style (style/action-container color)}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [quo2/icon :i/pin {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count pinned-messages)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label :t/pinned-messages-2)]]
      [rn/touchable-opacity {:style (style/action-container color)}
       [quo2/icon :i/activity-center {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label :t/mute-group)]]
      [rn/touchable-opacity {:style (style/action-container color)}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [quo2/icon :i/add-user {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count members)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label (if admin? :t/manage-members :t/add-members))]]]
     [rn/section-list {:key-fn                         :title
                       :sticky-section-headers-enabled false
                       :sections                       sectioned-members
                       :render-section-header-fn       contacts-section-header
                       :render-fn                      contact-item/contact-item}]]))
