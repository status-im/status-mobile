(ns status-im.ui.screens.desktop.main.tabs.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.desktop.main.tabs.home.styles :as styles]
            [status-im.ui.components.popup-menu.views :as popup-menu]
            [clojure.string :as string]
            [status-im.ui.screens.home.views.inner-item :as chat-item]
            [taoensso.timbre :as log]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as components]
            [reagent.core :as reagent]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.utils.config :as config]))

(views/defview chat-list-item-inner-view
  [{:keys [chat-id name group-chat
           color public? public-key
           timestamp chat-name
           last-message-content
           last-message-content-type]
    :as chat-item}]
  (views/letsubs [photo-path              [:contacts/chat-photo chat-id]
                  unviewed-messages-count [:chats/unviewed-messages-count chat-id]
                  current-chat-id         [:chats/current-chat-id]]
    (let [last-message {:content      last-message-content
                        :timestamp    timestamp
                        :content-type last-message-content-type}
          name (or chat-name
                   (gfycat/generate-gfy public-key))
          [unviewed-messages-label large?] [(utils/unread-messages-count unviewed-messages-count) true]
          current? (= current-chat-id chat-id)]
      [react/view {:style (styles/chat-list-item current?)}
       [react/view {:style styles/img-container}
        (if public?
          [react/view {:style (styles/topic-image color)}
           [react/text {:style styles/topic-text}
            (string/capitalize (second name))]]
          [react/image {:style styles/chat-icon
                        :source {:uri photo-path}}])]
       [react/view {:style styles/chat-name-last-msg-box}
        [react/view {:style styles/chat-name-box}
         (when (and group-chat (not public?))
           [icons/icon :tiny-icons/tiny-group])
         (when public?
           [icons/icon :tiny-icons/tiny-public])
         [react/text {:ellipsize-mode  :tail
                      :number-of-lines 1
                      :style           (styles/chat-name current?)}
          name]]
        [react/text {:ellipsize-mode  :tail
                     :number-of-lines 1
                     :style           styles/chat-last-message}
         (if (= constants/content-type-command (:content-type last-message))
           [chat-item/command-short-preview last-message]
           (or (:text last-message-content)
               (i18n/label :no-messages-yet)))]]
       [react/view {:style styles/timestamp}
        [chat-item/message-timestamp (:timestamp last-message)]
        (when (pos? unviewed-messages-count)
          [react/view {:style styles/unread-messages-icon}
           [react/text {:style (styles/unread-messages-text large?)} unviewed-messages-label]])]])))

(defn chat-list-item [[chat-id
                       {:keys [group-chat public?] :as chat}]]
  [react/touchable-highlight
   {:on-press (fn [arg]
                (let [right-click? (= "right" (.-button (.-nativeEvent arg)))]
                  (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])
                  (when right-click?
                    (popup-menu/show-desktop-menu
                     (popup-menu/get-chat-menu-items group-chat public? chat-id)))))}
   [chat-list-item-inner-view (assoc chat :chat-id chat-id)]])

(defn tag-view [tag {:keys [on-press]}]
  [react/touchable-highlight {:style {:border-radius 5
                                      :margin 2
                                      :padding 4
                                      :height 23
                                      :background-color (if (= tag "clear filter")
                                                          colors/red
                                                          colors/blue)
                                      :justify-content :center
                                      :align-items :center
                                      :align-content :center}
                              :on-press on-press}
   [react/text {:style {:font-size 9
                        :color colors/white}
                :font :medium} tag]])

(defn search-input [search-filter]
  [react/view {:style {:flex 1
                       :flex-direction :row}}
   [react/text-input {:placeholder "Type here to search chats..."
                      :auto-focus             true
                      :blur-on-submit         true
                      :style {:flex 1
                              :color :black
                              :height            30
                              :margin-left       20
                              :margin-right      22}
                      :default-value search-filter
                      :on-change (fn [e]
                                   (let [native-event (.-nativeEvent e)
                                         text         (.-text native-event)]
                                     (re-frame/dispatch [:search/filter-changed text])))}]])

(defn popup []
  [react/view {:style {:flex-direction :row}}
   [react/view {:style styles/popup-shaddow}
    [react/view {:style styles/popup}
     [action-button/action-button
      {:label               (i18n/label :t/start-new-chat)
       :accessibility-label :start-1-1-chat-button
       :icon                :main-icons/add-contact
       :icon-opts           {:color colors/blue}
       :on-press            #(do
                               (re-frame/dispatch [:set-in [:desktop :popup] nil])
                               (re-frame/dispatch [:navigate-to :desktop/new-one-to-one]))}]
     [action-button/action-button
      {:label               (i18n/label :t/start-group-chat)
       :accessibility-label :start-group-chat-button
       :icon                :main-icons/group-chat
       :icon-opts           {:color colors/blue}
       :on-press            #(do
                               (re-frame/dispatch [:set-in [:desktop :popup] nil])
                               (re-frame/dispatch [:navigate-to :desktop/new-group-chat]))}]
     [action-button/action-button
      {:label               (i18n/label :t/new-public-group-chat)
       :accessibility-label :join-public-chat-button
       :icon                :main-icons/public-chat
       :icon-opts           {:color colors/blue}
       :on-press            #(do
                               (re-frame/dispatch [:set-in [:desktop :popup] nil])
                               (re-frame/dispatch [:navigate-to :desktop/new-public-chat]))}]]]])

(views/defview chat-list-view [loading?]
  (views/letsubs [search-filter       [:search/filter]
                  logging-in?          [:get :accounts/login]
                  filtered-home-items [:search/filtered-home-items]]
    {:component-did-mount
     (fn [this]
       (let [[_ loading?] (.. this -props -argv)]
         (when loading?
           (re-frame/dispatch [:init-rest-of-chats]))))}
    [react/view {:style styles/chat-list-view}
     [react/view {:style styles/chat-list-header}
      [search-input search-filter]
      [react/view
       [react/touchable-highlight {:on-press (when-not logging-in? #(re-frame/dispatch [:set-in [:desktop :popup] popup]))}
        [react/view {:style styles/add-new}
         (if logging-in?
           [components/activity-indicator {:animating true :color :white}]
           [icons/icon :main-icons/add {:style {:tint-color :white}}])]]]]
     [react/scroll-view {:enableArrayScrollingOptimization true}
      [react/view
       (for [[index chat] (map-indexed vector filtered-home-items)]
         ^{:key (first chat)}
         [chat-list-item chat])]]]))

(views/defview chat-list-view-wrapper []
  (views/letsubs [loading? [:get :chats/loading?]]
    [chat-list-view loading?]))
