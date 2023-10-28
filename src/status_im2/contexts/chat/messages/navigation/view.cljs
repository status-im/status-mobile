(ns status-im2.contexts.chat.messages.navigation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [re-frame.db]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.common.home.actions.view :as actions]
    [status-im2.config :as config]
    [status-im2.contexts.chat.messages.navigation.style :as style]
    [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defonce ^:const title-opacity-interpolation-start 50)
(defonce ^:const title-opacity-interpolation-end 80)

(defn f-view
  [{:keys [theme scroll-y chat chat-screen-loaded? all-loaded? display-name online? photo-path
           back-icon animate-topbar-name? keyboard-shown? big-name-visible? animate-topbar-opacity?]}]
  (let [{:keys [group-chat chat-id]} chat
        opacity-animation            (reanimated/use-shared-value 0)
        banner-opacity-animation     (reanimated/interpolate scroll-y
                                                             [(+ style/navigation-bar-height 150)
                                                              (+ style/navigation-bar-height 200)]
                                                             [0 1]
                                                             {:extrapolateLeft  "clamp"
                                                              :extrapolateRight "clamp"})
        translate-animation          (reanimated/use-shared-value title-opacity-interpolation-start)
        title-opacity-animation      (reanimated/use-shared-value 0)
        messages                     (rf/sub [:chats/raw-chat-messages-stream (:chat-id chat)])
        more-than-two-messages?      (<= 2 (count messages))]
    (rn/use-effect
     (fn []
       (if @animate-topbar-opacity?
         (reanimated/animate opacity-animation 1)
         (reanimated/animate opacity-animation 0))
       (if (or (and more-than-two-messages?
                    (< title-opacity-interpolation-start (reanimated/get-shared-value scroll-y))
                    keyboard-shown?)
               @animate-topbar-name?
               (not @big-name-visible?)
               (and (neg? (reanimated/get-shared-value scroll-y))
                    more-than-two-messages?
                    keyboard-shown?))
         (do
           (reanimated/animate title-opacity-animation 1)
           (reanimated/animate translate-animation 0))
         (do
           (reanimated/animate title-opacity-animation 0)
           (reanimated/animate translate-animation title-opacity-interpolation-start))))
     [@animate-topbar-name? @big-name-visible? keyboard-shown? @animate-topbar-opacity?])
    [rn/view {:style (style/navigation-view chat-screen-loaded?)}
     [reanimated/view
      {:style (style/animated-background-view all-loaded? opacity-animation nil)}]

     [reanimated/view {:style (style/animated-blur-view all-loaded? opacity-animation)}
      [blur/view
       {:blur-amount   20
        :blur-type     :transparent
        :overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur theme)
        :blur-radius   (if platform/ios? 20 10)
        :style         {:flex 1}}]]

     [rn/view {:style style/header-container}
      [quo/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :back-button
        :on-press            #(do
                                (when config/shell-navigation-disabled?
                                  (rf/dispatch [:chat/close]))
                                (rf/dispatch [:navigate-back]))}
       back-icon]
      [reanimated/view
       {:style (style/animated-header all-loaded? translate-animation title-opacity-animation)}
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
       :i/options]]
     [:f>
      pin.banner/f-banner
      {:chat-id           chat-id
       :opacity-animation banner-opacity-animation
       :all-loaded?       all-loaded?
       :top-offset        style/navigation-bar-height}]]))

(defn- internal-navigation-view
  [params]
  [:f> f-view params])

(def navigation-view (quo.theme/with-theme internal-navigation-view))
