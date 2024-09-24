(ns status-im.contexts.chat.messenger.messages.navigation.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [re-frame.db]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.home.actions.view :as actions]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]
    [status-im.contexts.chat.messenger.messages.navigation.style :as style]
    [status-im.contexts.chat.messenger.messages.pin.banner.view :as pin.banner]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn header-content-container
  [{:keys [chat-id group-chat chat-type chat-name emoji color] :as _chat}]
  (let [theme              (quo.theme/use-theme)
        display-name       (cond
                             (= chat-type constants/one-to-one-chat-type)
                             (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))

                             (= chat-type constants/community-chat-type)
                             (str "# " chat-name)

                             :else
                             (str emoji chat-name))
        online?            (when-not group-chat (rf/sub [:visibility-status-updates/online? chat-id]))
        photo-path         (if group-chat
                             (rf/sub [:chats/group-chat-image chat-id])
                             (rf/sub [:chats/photo-path chat-id]))
        community-channel? (= chat-type constants/community-chat-type)]
    [rn/view {:style style/header-content-container}
     (cond
       community-channel?
       [quo/channel-avatar
        {:size                :size-32
         :full-name           chat-name
         :customization-color color
         :emoji               (when-not (string/blank? emoji)
                                (string/trim emoji))}]
       group-chat
       [quo/group-avatar
        {:customization-color color
         :size                :size-32
         :picture             photo-path
         :chat-name           chat-name}]

       :else
       [quo/user-avatar
        {:full-name       display-name
         :online?         online?
         :profile-picture photo-path
         :size            :small}])
     [rn/view {:style style/header-text-container}
      [quo/text
       {:weight          :semi-bold
        :size            :paragraph-1
        :number-of-lines 1
        :style           (style/header-display-name theme)}
       display-name]
      (when-not group-chat
        [quo/text
         {:number-of-lines 1
          :weight          :medium
          :size            :paragraph-2
          :style           (style/header-status theme)}
         (i18n/label
          (if online? :t/online :t/offline))])]]))

(defn header-background
  [{:keys [chat-id navigation-view-height]}]
  (let [theme (quo.theme/use-theme)]
    [:<>
     [rn/view {:style (style/background navigation-view-height)}
      [quo/blur
       {:style         {:flex 1}
        :blur-amount   20
        :blur-type     :transparent
        :overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)
        :blur-radius   (if platform/ios? 20 10)}]]
     [pin.banner/banner
      {:chat-id    chat-id
       :top-offset navigation-view-height}]]))

(defn view
  []
  (let [{:keys [chat-id chat-type]
         :as   chat}           (rf/sub [:chats/current-chat-chat-view])
        top-insets             (safe-area/get-top)
        navigation-view-height (+ top-insets messages.constants/top-bar-height)]
    [rn/view {:style (style/navigation-view navigation-view-height)}
     [header-background
      {:chat-id                chat-id
       :navigation-view-height navigation-view-height}]
     [rn/view {:style (style/header-container top-insets)}
      [quo/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :back-button
        :on-press            #(rf/dispatch [:navigate-back])}
       (if (= chat-type constants/community-chat-type) :i/arrow-left :i/close)]
      [header-content-container chat]
      [quo/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :options-button
        :on-press            (fn []
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:show-bottom-sheet
                                             {:content (fn [] [actions/chat-actions chat true])}]))}
       :i/options]]]))
