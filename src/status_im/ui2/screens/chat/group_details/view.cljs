(ns status-im.ui2.screens.chat.group-details.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.buttons.button :as quo2.button]
            [status-im.ui2.screens.chat.group-details.style :as style]
            [quo2.core :as quo2]
            [quo2.components.icon :as icons]
            [status-im.utils.re-frame :as rf]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui2.screens.chat.components.contact-item.view :as contact-item]))

(defn back-button []
  [quo2.button/button {:type                :grey
                       :size                32
                       :width               32
                       :accessibility-label "back-button"
                       :on-press            #(do
                                               (rf/dispatch [:navigate-back]))}
   [icons/icon :i/arrow-left {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn options-button []
  [quo2.button/button {:type                :grey
                       :size                32
                       :width               32
                       :accessibility-label "options-button"}
   [icons/icon :i/options {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn top-buttons []
  [rn/view {:style {:flex-direction     :row
                    :padding-horizontal 20
                    :justify-content    :space-between}}
   [back-button] [options-button]])

(defn count-container [count]
  [rn/view {:style (style/count-container)}
   [quo2/text {:size   :label
               :weight :medium
               :style  {:text-align :center}} count]])

(defn prepare-members [contacts admins]
  (let [data (atom {:owner   {:title :Owner
                              :data  []}
                    :online  {:title :Online
                              :data  []}
                    :offline {:title :Offline
                              :data  []}})]
    (doseq [i (range (count contacts))]
      (let [contact (rf/sub [:contacts/contact-by-address (nth contacts i)])
            admin?  (get admins (nth contacts i))
            online? (:online? contact)]
        (if admin?
          (swap! data #(assoc-in % [:owner :data] (conj (:data (get @data :owner)) contact)))
          (if online?
            (swap! data #(assoc-in % [:online :data] (conj (:data (get @data :online)) contact)))
            (swap! data #(assoc-in % [:offline :data] (conj (:data (get @data :offline)) contact)))))))
    (when (empty? (get-in @data [:online :data]))
      (swap! data #(dissoc % :online)))
    (when (empty? (get-in @data [:offline :data]))
      (swap! data #(dissoc % :offline)))
    (vals @data)))

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:padding-horizontal 20 :border-top-width 1 :border-top-color colors/neutral-20 :padding-vertical 8 :margin-top 8}}
   [quo2/text {:size   :paragraph-2
               :weight :medium
               :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} title]])

(defn group-details []
  (let [{:keys [admins chat-id chat-name color contacts public?]} (rf/sub [:chats/current-chat])
        members           (rf/sub [:contacts/current-chat-contacts])
        sectioned-members (prepare-members (seq contacts) admins)
        pinned-messages   (rf/sub [:chats/pinned chat-id])
        current-pk        (rf/sub [:multiaccount/public-key])
        admin?            (get admins current-pk)]
    [rn/view {:style {:padding-top 50}}
     [top-buttons]
     [rn/view {:style {:flex-direction     :row
                       :margin-top         24
                       :padding-horizontal 20}}
      [quo2/group-avatar {:color color
                          :size  :medium}]
      [quo2/text {:weight :semi-bold
                  :size   :heading-1
                  :style  {:margin-horizontal 8}} chat-name]
      [rn/view {:style {:margin-top 8}}
       [icons/icon (if public? :i/world :i/privacy) {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
     [rn/view {:style (style/actions-view)}
      [rn/view {:style (style/action-container color)}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [icons/icon :i/pin {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count pinned-messages)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label :t/pinned-messages)]]
      [rn/view {:style (style/action-container color)}
       [icons/icon :i/activity-center {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label :t/mute-group)]]
      [rn/view {:style (style/action-container color)}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [icons/icon :i/add-user {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count members)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label (if admin? :t/manage-members :t/add-members))]]]
     [list/section-list {:key-fn                         :title
                         :sticky-section-headers-enabled false
                         :sections                       sectioned-members
                         :render-section-header-fn       contacts-section-header
                         :render-fn                      contact-item/contact-item}]]))
