(ns status-im2.contexts.chat.messages.navigation.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [re-frame.db]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [status-im2.config :as config]
            [react-native.reanimated :as reanimated]
            [react-native.platform :as platform]
            [status-im2.contexts.chat.messages.navigation.style :as style]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im2.common.home.actions.view :as actions]))

(defn f-view
  [{:keys [scroll-y]}]
  (let [{:keys [group-chat chat-id chat-name emoji
                chat-type]
         :as   chat}             (rf/sub [:chats/current-chat-chat-view])
        chat-screen-loaded?      (rf/sub [:shell/chat-screen-loaded?])
        all-loaded?              (when chat-screen-loaded?
                                   (rf/sub [:chats/all-loaded? (:chat-id chat)]))
        display-name             (if (= chat-type constants/one-to-one-chat-type)
                                   (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                                   (str emoji " " chat-name))
        online?                  (rf/sub [:visibility-status-updates/online? chat-id])
        contact                  (when-not group-chat
                                   (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path               (when-not (empty? (:images contact))
                                   (rf/sub [:chats/photo-path chat-id]))]
    [rn/view {:style style/navigation-view
              :key "parent"}
     [rn/view {:style style/header-container}
      [rn/touchable-opacity
       {:on-press            #(do
                                (when config/shell-navigation-disabled?
                                  (rf/dispatch [:chat/close]))
                                (rf/dispatch [:navigate-back]))
        :accessible true
        :accessibility-label :back-button
        :style               (style/button-container {:margin-left 20})}
       [quo/icon :i/arrow-left
        {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
      [rn/view
       [rn/view {:style style/header-content-container}
        (when-not group-chat
          [rn/view {:style style/header-avatar-container}
           [quo/user-avatar
            {:full-name       display-name
             :online?         online?
             :profile-picture photo-path
             :size            :small}]])
        [rn/view {:style style/header-text-container}
         [rn/view {:style {:flex-direction :row}}
          [quo/text
           {:weight          :semi-bold
            :size            :paragraph-1
            :number-of-lines 1
            :style           (style/header-display-name)}
           display-name]]
         (when-not group-chat
           [quo/text
            {:number-of-lines 1
             :weight          :medium
             :size            :paragraph-2
             :style           (style/header-status)}
            (i18n/label
             (if online? :t/online :t/offline))])]]]
      [rn/touchable-opacity
       {:style               (style/button-container {:margin-right 20})
        :accessibility-label :options-button
        :accessible true
        :on-press            (fn []
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:show-bottom-sheet
                                             {:content (fn [] [actions/chat-actions chat true])}]))}
       [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]]))

(defn- internal-navigation-view
  [params]
  [:f> f-view params])

(def navigation-view (theme/with-theme internal-navigation-view))
