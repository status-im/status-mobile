(ns quo2.components.navigation.bottom-nav-tab.view
  (:require [quo2.components.counter.counter.view :as counter]
            [quo2.components.icons.icons :as icons]
            [quo2.components.navigation.bottom-nav-tab.styles :as styles]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(defn toggle-background-color
  [background-color press-out? pass-through?]
  (reanimated/set-shared-value
   background-color
   (cond
     press-out?    "transparent"
     pass-through? colors/white-opa-5
     :else         colors/neutral-70)))

(defn- f-bottom-nav-tab
  "[bottom-nav-tab opts]
   opts
   {:icon                   :i/communities
    :new-notifications?     true/false
    :notification-indicator :unread-dot/:counter
    :counter-label          number
    :on-press               bottom-tab on-press function
    :pass-through?          true/false
    :icon-color-anim        reanimated shared value
    :customization-color    Customization color for the notification mark
  "
  [{:keys [icon new-notifications? notification-indicator counter-label
           on-press pass-through? icon-color-anim accessibility-label test-ID
           customization-color on-long-press]
    :or   {customization-color :blue}}]
  (let [icon-animated-style       (reanimated/apply-animations-to-style
                                   {:tint-color icon-color-anim}
                                   {:width       24
                                    :height      24
                                    :margin-left 33
                                    :margin-top  8})
        background-color          (reanimated/use-shared-value "transparent")
        background-animated-style (reanimated/apply-animations-to-style
                                   {:background-color background-color}
                                   {:width         90
                                    :height        40
                                    :border-radius 10})]
    [rn/touchable-without-feedback
     {:test-ID             test-ID
      :on-long-press       on-long-press ;;NOTE - this is temporary while supporting old wallet
      :on-press            on-press
      :on-press-in         #(toggle-background-color background-color false pass-through?)
      :on-press-out        #(toggle-background-color background-color true pass-through?)
      :accessibility-label accessibility-label}
     [reanimated/view {:style background-animated-style}
      ;; In android animations are not working for the animated components which are nested by
      ;; hole-view,
      ;; Interestingly this only happens when hole view and blur view are used together
      ;; Similar behavior is also seen while removing holes, and to fix that we used key and
      ;; force-rendered view
      ;; But we need animations faster for tab clicks, so we can't rely on reagent atoms,
      ;; so for now only using hole view for the ios tab icon notification boundary
      (if platform/ios?
        [hole-view/hole-view
         {:key   new-notifications? ;; Key is required to force removal of holes
          :holes (cond
                   (not new-notifications?) ;; No new notifications, remove holes
                   []

                   (= notification-indicator :unread-dot)
                   [{:x 50 :y 5 :width 10 :height 10 :borderRadius 5}]

                   :else
                   [{:x 47 :y 1 :width 18 :height 18 :borderRadius 7}])}
         [reanimated/image
          {:style  icon-animated-style
           :source (icons/icon-source (keyword (str icon 24)))}]]
        [reanimated/image
         {:style  icon-animated-style
          :source (icons/icon-source (keyword (str icon 24)))}])
      (when new-notifications?
        (if (= notification-indicator :counter)
          [counter/view
           {:customization-color customization-color
            :container-style     styles/notification-counter}
           counter-label]
          [rn/view {:style (styles/notification-dot customization-color)}]))]]))

(defn view
  [opts]
  [:f> f-bottom-nav-tab opts])
