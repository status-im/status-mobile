(ns status-im.ui.screens.desktop.main.tabs.home.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.desktop.main.tabs.home.styles :as styles]
            [status-im.ui.screens.home.views.inner-item :as chat-item])
  (:require-macros [status-im.utils.views :as views]))

(defn chat-last-message-view [{:keys [content] :as last-message}]
  [react/text {:style styles/chat-last-message}
   (if (= constants/content-type-command (:content-type last-message))
     [chat-item/command-short-preview last-message]
     (or (:text content)
         (i18n/label :no-messages-yet)))])

(defn chat-list-item
  [{:keys [chat-id name group-chat color public? public-key photo-path
           unviewed-messages-label large-unviewed-messages-label? last-message]
    :as chat-item}
   current-chat-id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])}
   [react/view {:style (styles/chat-list-item (= current-chat-id chat-id))}
    [react/view {:style styles/img-container}
     (if public?
       [react/view {:style (styles/topic-image color)}
        [react/text {:style styles/topic-text}
         (string/capitalize (second name))]]
       [react/image {:style styles/chat-icon
                     :source {:uri photo-path}}])
     (when unviewed-messages-label
       [react/view {:style styles/unread-messages-icon}
        [react/text {:style (styles/unread-messages-text large-unviewed-messages-label?)} unviewed-messages-label]])]
    [react/view {:style styles/chat-name-last-msg-box}
     [react/view {:style styles/chat-name-box}
      (when (and group-chat (not public?))
        [react/view [icons/icon :icons/group-chat]])
      (when public?
        [react/view [icons/icon :icons/public-chat]])
      [react/text {:style {:font-size  14
                           :flex 1
                           :font-weight (if (= current-chat-id chat-id)
                                          :bold
                                          :normal)}}
       name]]
     [chat-last-message-view last-message]]
    [react/view {:style styles/timestamp}
     [chat-item/message-timestamp (:timestamp last-message)]]]])

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

(views/defview chat-list-view []
  (views/letsubs [{current-chat-id :chat-id}   [:chat/current]
                  search-filter       [:search/filter]
                  filtered-home-items [:search/filtered-home-items]]
    [react/view {:style styles/chat-list-view}
     [react/view {:style styles/chat-list-header}
      [search-input search-filter]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
       [react/view {:style styles/add-new}
        [icons/icon :icons/add {:style {:tint-color :white}}]]]]
     [react/view {:style styles/chat-list-separator}]
     [list/flat-list {:data filtered-home-items
                      :initialNumToRender 20
                      :key-fn :chat-id
                      :render-fn (fn [item] [chat-list-item item current-chat-id])}]]))
