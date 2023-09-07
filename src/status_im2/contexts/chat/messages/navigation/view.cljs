(ns status-im2.contexts.chat.messages.navigation.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
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

(defn f-view
  [{:keys [scroll-y chat chat-screen-loaded? all-loaded? display-name online? photo-path]}]
  (let [{:keys [group-chat chat-id]} chat
        opacity-animation            (reanimated/interpolate scroll-y
                                                             [style/navigation-bar-height
                                                              (+ style/navigation-bar-height 30)]
                                                             [0 1]
                                                             {:extrapolateLeft  "clamp"
                                                              :extrapolateRight "extend"})
        banner-opacity-animation     (reanimated/interpolate scroll-y
                                                             [(+ style/navigation-bar-height 150)
                                                              (+ style/navigation-bar-height 200)]
                                                             [0 1]
                                                             {:extrapolateLeft  "clamp"
                                                              :extrapolateRight "extend"})
        translate-animation          (reanimated/interpolate scroll-y
                                                             [(+ style/navigation-bar-height 25)
                                                              (+ style/navigation-bar-height 100)]
                                                             [50 0]
                                                             {:extrapolateLeft  "clamp"
                                                              :extrapolateRight "clamp"})
        title-opacity-animation      (reanimated/interpolate scroll-y
                                                             [0 50]
                                                             [0 1]
                                                             {:extrapolateLeft  "clamp"
                                                              :extrapolateRight "clamp"})]
    [rn/view {:style (style/navigation-view chat-screen-loaded?)}
     [reanimated/view
      {:style (style/animated-background-view all-loaded? opacity-animation nil)}]

     [reanimated/view {:style (style/animated-blur-view all-loaded? opacity-animation)}
      [blur/view
       {:blur-amount 20
        :blur-type   (colors/theme-colors :light :dark)
        :blur-radius (if platform/ios? 20 10)
        :style       {:flex 1}}]]

     [rn/view {:style style/header-container}
      [rn/touchable-opacity
       {:active-opacity      1
        :on-press            #(do
                                (when config/shell-navigation-disabled?
                                  (rf/dispatch [:chat/close]))
                                (rf/dispatch [:navigate-back]))
        :accessibility-label :back-button
        :style               (style/button-container {:margin-left 20})}
       [quo/icon :i/arrow-left
        {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
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
      [rn/touchable-opacity
       {:active-opacity      1
        :style               (style/button-container {:margin-right 20})
        :accessibility-label :options-button
        :on-press            (fn []
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:show-bottom-sheet
                                             {:content (fn [] [actions/chat-actions chat true])}]))}
       [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]
     [:f>
      pin.banner/f-banner
      {:chat-id           chat-id
       :opacity-animation banner-opacity-animation
       :all-loaded?       all-loaded?
       :top-offset        style/navigation-bar-height}]]))

(defn- internal-navigation-view
  [params]
  [:f> f-view params])

(def navigation-view (theme/with-theme internal-navigation-view))
