(ns status-im.ui2.screens.chat.group-details.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
<<<<<<< HEAD
<<<<<<< HEAD
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
<<<<<<< HEAD
                :accessibility-label :options-button
                :icon                true}
   :i/options])
=======
                :accessibility-label :options-button}
   [quo2/icon :i/options {:color (colors/theme-colors colors/neutral-100 colors/white)}]])
=======
            [quo2.components.buttons.button :as quo2.button]
=======
>>>>>>> 8c3cd6f91... refactor
            [status-im.ui2.screens.chat.group-details.style :as style]
=======
>>>>>>> 25441811e... feat: group details screen 2
            [quo2.core :as quo2]
            [status-im.ui2.screens.chat.group-details.style :as style]
            [status-im.ui2.screens.chat.messages.message :as message]
            [status-im.chat.models :as chat.models]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [status-im.ui2.screens.common.contact-list.view :as contact-list]
            [status-im.ui2.screens.chat.components.contact-item.view :as contact-item]
            [status-im.ui2.screens.chat.actions :as actions]
            [quo.components.safe-area :as safe-area]
            [reagent.core :as reagent]))

(defn back-button []
  [quo2/button {:type                :grey
                :size                32
                :width               32
                :style               {:margin-left 20}
                :accessibility-label :back-button
                :on-press            #(rf/dispatch [:navigate-back])}
   [quo2/icon :i/arrow-left {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn options-button []
  (let [group (rf/sub [:chats/current-chat])]
    [quo2/button {:type                :grey
                  :size                32
                  :width               32
                  :style               {:margin-right 20}
                  :accessibility-label :options-button
                  :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                                      {:content (fn [] [actions/group-details-actions group])}])}
     [quo2/icon :i/options {:color (colors/theme-colors colors/neutral-100 colors/white)}]]))

<<<<<<< HEAD
(defn top-buttons []
  [rn/view {:style {:flex-direction     :row
                    :padding-horizontal 20
                    :justify-content    :space-between}}
   [back-button] [options-button]])
>>>>>>> 4b20ea02d... feat: group details screen
>>>>>>> 752c7b3f2... feat: group details screen

=======
>>>>>>> c3f9147a9... refactor
(defn count-container [count]
  [rn/view {:style (style/count-container)}
   [quo2/text {:size   :label
               :weight :medium
               :style  {:text-align :center}} count]])

<<<<<<< HEAD
<<<<<<< HEAD
(defn prepare-members [members]
  (let [admins (filter :admin? members)
        online (filter #(and (not (:admin? %)) (:online? %)) members)
        offline (filter #(and (not (:admin? %)) (not (:online? %))) members)]
    (vals (cond-> {}
            (seq admins) (assoc :owner {:title "Owner" :data admins})
            (seq online) (assoc :online {:title "Online" :data online})
            (seq offline) (assoc :offline {:title "Offline" :data offline})))))
=======
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
>>>>>>> 4b20ea02d... feat: group details screen
=======
(defn prepare-members
  [contact-addresses admins]
  (let [contacts (map #(assoc (rf/sub [:contacts/contact-by-address %])
                         :admin? (get admins %))
                      contact-addresses)
        admins   (filter :admin? contacts)
        online   (filter #(and (not (:admin? %)) (:online? %)) contacts)
        offline  (filter #(and (not (:admin? %)) (not (:online? %))) contacts)]
    (vals (cond-> {}
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                  (seq admins)  (assoc :owner {:title :Owner :data admins})
                  (seq online)  (assoc :online {:title :Online :data online})
                  (seq offline) (assoc :offline {:title :offline :data offline})))))
>>>>>>> 8c3cd6f91... refactor
=======
            (seq admins)  (assoc :owner {:title :Owner :data admins})
            (seq online)  (assoc :online {:title :Online :data online})
            (seq offline) (assoc :offline {:title :offline :data offline})))))
>>>>>>> fd94ca82b... refactor
=======
                  (seq admins) (assoc :owner {:title :Owner :data admins})
                  (seq online) (assoc :online {:title :Online :data online})
                  (seq offline) (assoc :offline {:title :offline :data offline})))))
>>>>>>> 5cbb4bc47... fix dark mode
=======
            (seq admins) (assoc :owner {:title :Owner :data admins})
            (seq online) (assoc :online {:title :Online :data online})
            (seq offline) (assoc :offline {:title :offline :data offline})))))
>>>>>>> 459f86ddb... lint
=======
            (seq admins) (assoc :owner {:title "Owner" :data admins})
            (seq online) (assoc :online {:title "Online" :data online})
            (seq offline) (assoc :offline {:title "Offline" :data offline})))))
>>>>>>> 9aedd60b3... refactor
=======
                  (seq admins) (assoc :owner {:title "Owner" :data admins})
                  (seq online) (assoc :online {:title "Online" :data online})
                  (seq offline) (assoc :offline {:title "Offline" :data offline})))))
>>>>>>> 25441811e... feat: group details screen 2

(defn contacts-section-header [{:keys [title]}]
  [rn/view {:style {:padding-horizontal 20 :border-top-width 1 :border-top-color colors/neutral-20 :padding-vertical 8 :margin-top 8}}
   [quo2/text {:size   :paragraph-2
               :weight :medium
               :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} title]])

(def added (reagent/atom ()))

(defn contact-requests-sheet [group]
  [:f>
   (fn []
     (let [{window-height :height} (rn/use-window-dimensions)
           safe-area (safe-area/use-safe-area)]
       [rn/view {:style {:height (- window-height (:top safe-area))}}
        [rn/touchable-opacity
         {:on-press #(rf/dispatch [:bottom-sheet/hide])
          :style
          {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
           :margin-left      20
           :width            32
           :height           32
           :border-radius    10
           :justify-content  :center
           :align-items      :center
           :margin-bottom    24}}
         [quo2/icon :i/close {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
        [quo2/text {:size   :heading-1
                    :weight :semi-bold
                    :style  {:margin-left 20}}
         (i18n/label :t/add-members)]
        [rn/text-input {:placeholder "Search..."
                        :style       {:height             32
                                      :padding-horizontal 20
                                      :margin-vertical    12}}]
        [contact-list/contact-list {:icon  :check
                                    :group group
                                    :added added}]
        [rn/view {:position           :absolute
                  :padding-horizontal 20
                  :padding-vertical   12
                  :padding-bottom     (+ (:bottom safe-area) 33)
                  :width              "100%"
                  :background-color   colors/white
                  :flex-direction     :row
                  :bottom             0}
         [quo2/button {:style    {:flex 1}
                       :on-press #(rf/dispatch [:bottom-sheet/hide])
                       :disabled (= (count @added) 0)}
          (i18n/label :t/save)]]]))])

(defn group-details []
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
  (let [{:keys [admins chat-id chat-name color contacts public?]} (rf/sub [:chats/current-chat])
=======
  (let [{:keys [admins chat-id chat-name color contacts public? muted]} (rf/sub [:chats/current-chat])
>>>>>>> 25441811e... feat: group details screen 2
=======
  (let [{:keys [admins chat-id chat-name color contacts public? muted] :as group} (rf/sub [:chats/current-chat])
>>>>>>> faae21626... updates
        members           (rf/sub [:contacts/current-chat-contacts])
        sectioned-members (prepare-members (seq contacts) admins)
        pinned-messages   (rf/sub [:chats/pinned chat-id])
        current-pk        (rf/sub [:multiaccount/public-key])
        admin?            (get admins current-pk)]
    [rn/view {:style {:flex             1
                      :background-color (colors/theme-colors colors/white colors/neutral-95)}}
     [quo2/header {:left-component  [back-button]
                   :right-component [options-button]
                   :background      (colors/theme-colors colors/white colors/neutral-95)}]
     [rn/view {:style {:flex-direction     :row
<<<<<<< HEAD
                       :margin-top         24
>>>>>>> 4b20ea02d... feat: group details screen
=======
                       :margin-top         12
>>>>>>> e069b53ef... refactor
                       :padding-horizontal 20}}
      [quo2/group-avatar {:color color
                          :size  :medium}]
      [quo2/text {:weight :semi-bold
                  :size   :heading-1
                  :style  {:margin-horizontal 8}} chat-name]
      [rn/view {:style {:margin-top 8}}
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
       [icons/icon (if public? :i/world :i/privacy) {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
=======
       [quo2/icon (if public? :i/world :i/privacy) {:size 20 :color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]
>>>>>>> 8c3cd6f91... refactor
     [rn/view {:style (style/actions-view)}
      [rn/touchable-opacity {:style    (style/action-container color)
                             :on-press (fn []
                                         (rf/dispatch [:bottom-sheet/show-sheet
                                                       {:content #(message/pinned-messages-list chat-id)}]))}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [quo2/icon :i/pin {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count pinned-messages)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label :t/pinned-messages)]]
      [rn/touchable-opacity {:style    (style/action-container color)
                             :on-press #(rf/dispatch [::chat.models/mute-chat-toggled chat-id (not muted)])}
       [quo2/icon (if muted :i/muted :i/activity-center) {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label (if muted :unmute-group :mute-group))]]
      [rn/touchable-opacity {:style    (style/action-container color)
                             :on-press #(rf/dispatch
                                          [:bottom-sheet/show-sheet
                                           {:content (fn [] [contact-requests-sheet group])}])}
       [rn/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
        [quo2/icon :i/add-user {:size 20 :color (colors/theme-colors colors/neutral-100 colors/white)}]
        [count-container (count members)]]
       [quo2/text {:style {:margin-top 16} :size :paragraph-1 :weight :medium} (i18n/label (if admin? :t/manage-members :t/add-members))]]]
<<<<<<< HEAD
     [list/section-list {:key-fn                         :title
                         :sticky-section-headers-enabled false
                         :sections                       sectioned-members
                         :render-section-header-fn       contacts-section-header
                         :render-fn                      contact-item/contact-item}]]))
>>>>>>> 4b20ea02d... feat: group details screen
=======
     [rn/section-list {:key-fn                         :title
                       :sticky-section-headers-enabled false
                       :sections                       sectioned-members
                       :render-section-header-fn       contacts-section-header
<<<<<<< HEAD
                       :render-fn                      contact-item/contact-item}]]))
>>>>>>> ca683e4a1... add SectionList to RN
=======
                       :render-fn                      (fn [item]
                                                         [contact-item/contact-item item {:chat-id chat-id
<<<<<<< HEAD
                                                                                          :admin?  admin?}])}]]))
>>>>>>> 25441811e... feat: group details screen 2
=======
                                                                                          :admin?  admin?
<<<<<<< HEAD
                                                                                          :icon :options}])}]]))
>>>>>>> 6bc845a97... updates
=======
                                                                                          :icon    :options}])}]]))
>>>>>>> faae21626... updates
