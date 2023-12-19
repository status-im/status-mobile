(ns status-im2.contexts.chat.messages.navigation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [re-frame.db]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im2.common.home.actions.view :as actions]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.constants :as messages.constants]
    [status-im2.contexts.chat.messages.navigation.style :as style]
    [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.chat.messages :as worklets]))

(defn f-header-content-container
  [{:keys [chat distance-from-list-top all-loaded? calculations-complete?]}]
  (let [{:keys [chat-id group-chat chat-type chat-name
                emoji]} chat
        display-name    (cond
                          (= chat-type constants/one-to-one-chat-type)
                          (first (rf/sub
                                  [:contacts/contact-two-names-by-identity
                                   chat-id]))
                          (= chat-type constants/community-chat-type)
                          (str (when emoji (str emoji " ")) "# " chat-name)
                          :else (str emoji chat-name))
        online?         (when-not group-chat (rf/sub [:visibility-status-updates/online? chat-id]))
        photo-path      (when-not group-chat (rf/sub [:chats/photo-path chat-id]))
        header-opacity  (worklets/navigation-header-opacity
                         distance-from-list-top
                         all-loaded?
                         calculations-complete?
                         messages.constants/content-animation-start-position)
        header-position (worklets/navigation-header-position
                         distance-from-list-top
                         all-loaded?
                         messages.constants/top-bar-height
                         messages.constants/content-animation-start-position)]
    [reanimated/view
     {:style (style/header-content-container header-opacity header-position)}
     (when-not group-chat
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
        :style           (style/header-display-name)}
       display-name]
      (when-not group-chat
        [quo/text
         {:number-of-lines 1
          :weight          :medium
          :size            :paragraph-2
          :style           (style/header-status)}
         (i18n/label
          (if online? :t/online :t/offline))])]]))

(defn f-animated-background-and-pinned-banner
  [{:keys [chat-id navigation-view-height distance-from-list-top all-loaded?]}]
  (let [animation-distance messages.constants/header-animation-distance
        props              {:distance-from-list-top distance-from-list-top
                            :all-loaded?            all-loaded?}
        background-opacity (worklets/interpolate-navigation-view-opacity
                            (assoc props
                                   :start-position
                                   messages.constants/header-container-top-margin
                                   :end-position
                                   (+ animation-distance
                                      messages.constants/header-container-top-margin)))
        banner-opacity     (worklets/interpolate-navigation-view-opacity
                            (assoc props
                                   :start-position
                                   (+ navigation-view-height
                                      messages.constants/pinned-banner-animation-start-position)
                                   :end-position
                                   (+ animation-distance
                                      navigation-view-height
                                      messages.constants/pinned-banner-animation-start-position)))]
    [:<>
     [reanimated/view {:style (style/animated-background-view background-opacity navigation-view-height)}
      [blur/view
       {:style         {:flex 1}
        :blur-amount   20
        :blur-type     :transparent
        :overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur)
        :blur-radius   (if platform/ios? 20 10)}]]
     [pin.banner/banner
      {:chat-id        chat-id
       :banner-opacity banner-opacity
       :top-offset     navigation-view-height}]]))

(defn f-view
  [{:keys [distance-from-list-top calculations-complete?]}]
  (let [{:keys [chat-id chat-type] :as chat} (rf/sub [:chats/current-chat-chat-view])
        all-loaded?                          (reanimated/use-shared-value false)
        all-loaded-sub                       (rf/sub [:chats/all-loaded? chat-id])
        top-insets                           (safe-area/get-top)
        top-bar-height                       messages.constants/top-bar-height
        navigation-view-height               (+ top-bar-height top-insets)]
    (reanimated/set-shared-value all-loaded? all-loaded-sub)
    [rn/view
     {:style (style/navigation-view navigation-view-height messages.constants/pinned-banner-height)}
     [:f> f-animated-background-and-pinned-banner
      {:chat-id                chat-id
       :navigation-view-height navigation-view-height
       :distance-from-list-top distance-from-list-top
       :all-loaded?            all-loaded?}]
     [rn/view {:style (style/header-container top-insets top-bar-height)}
      [quo/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :back-button
        :on-press            #(rf/dispatch [:navigate-back])}
       (if (= chat-type constants/community-chat-type) :i/arrow-left :i/close)]
      [:f> f-header-content-container
       {:chat                   chat
        :distance-from-list-top distance-from-list-top
        :all-loaded?            all-loaded?
        :calculations-complete? calculations-complete?}]
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
