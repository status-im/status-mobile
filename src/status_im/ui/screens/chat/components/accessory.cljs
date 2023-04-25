(ns status-im.ui.screens.chat.components.accessory
  (:require [cljs-bean.core :as bean]
            [quo.animated :as animated]
            [quo.design-system.colors :as colors]
            [quo.platform :as platform]
            [quo.react :as react]
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.ui.components.tabbar.core :as tabbar]
            [status-im.ui.screens.chat.components.hooks :refer [use-keyboard-dimension]]
            [react-native.safe-area :as safe-area]))

(def duration 250)

(defn create-pan-responder
  [y pan-active]
  (when-not platform/android?
    (js->clj
     (.-panHandlers
      ^js
      (.create
       ^js rn/pan-responder
       #js
        {:onPanResponderGrant     (fn []
                                    (animated/set-value pan-active 1))
         :onPanResponderMove      (fn [_ ^js state]
                                    (animated/set-value y (.-moveY state)))
         :onPanResponderRelease   (fn []
                                    (animated/set-value pan-active 0)
                                    (js/setTimeout
                                     #(animated/set-value y 0)
                                     100))
         :onPanResponderTerminate (fn []
                                    (animated/set-value pan-active 0)
                                    (js/setTimeout
                                     #(animated/set-value y 0)
                                     100))})))))

(def ios-view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{on-update-inset :onUpdateInset
             y               :y
             pan-state       :panState
             on-close        :onClose
             has-panel       :hasPanel
             children        :children}
            (bean/bean props)
            {keyboard-height       :height
             keyboard-max-height   :max-height
             keyboard-end-position :end-position}
            (use-keyboard-dimension)
            bottom (safe-area/get-bottom)
            {on-layout  :on-layout
             bar-height :height}
            (rn/use-layout)

            visible (or has-panel (pos? keyboard-height))
            anim-visible (animated/use-value visible)
            kb-on-screen (if platform/android? 0 (* -1 (- keyboard-height bottom (tabbar/get-height))))
            panel-on-screen (* -1 (- keyboard-max-height bottom (tabbar/get-height)))
            max-delta (min 0 (if has-panel panel-on-screen kb-on-screen))
            panel-height (* -1 max-delta)
            end-position (- keyboard-end-position (when has-panel keyboard-max-height))
            delay (+ (/ (- keyboard-max-height panel-height)
                        (/ keyboard-max-height duration))
                     16)
            drag-diff (animated/clamp (animated/sub y end-position) 0 keyboard-max-height)
            animated-y (react/use-memo
                        (fn []
                          (animated/mix
                           (animated/with-timing-transition anim-visible
                                                            {:duration (- duration delay)
                                                             :easing   (:keyboard animated/easings)})
                           0
                           panel-on-screen))
                        [panel-on-screen])
            delta-y (animated/clamp (animated/add drag-diff animated-y) max-delta 0)
            on-update (fn []
                        (when on-update-inset
                          (on-update-inset (+ bar-height panel-height))))
            children (react/get-children children)]
        (react/effect! on-update [panel-height bar-height])
        (animated/code!
         (fn []
           (when has-panel
             ;; TODO: Check also velocity
             (animated/cond* (animated/and* (animated/greater-or-eq drag-diff (* 0.6 panel-height))
                                            (animated/not* pan-state))
                             [(animated/call* [] on-close)])))
         [delta-y pan-state has-panel on-close])
        (animated/code!
         (fn []
           (animated/delay
            (animated/set anim-visible (if visible 1 0))
            (if visible delay 0)))
         [visible keyboard-max-height delay])
        (reagent/as-element
         [animated/view
          {:style {:position         :absolute
                   :left             0
                   :right            0
                   :background-color (:ui-background @colors/theme)
                   :bottom           max-delta
                   :transform        [{:translateY delta-y}]}}
          [rn/view {:on-layout on-layout}
           (first children)]
          [rn/view
           {:style {:flex   1
                    :height (when (pos? panel-height) panel-height)}}
           (second children)]]))))))

(def android-view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{on-update-inset :onUpdateInset
             on-close        :onClose
             has-panel       :hasPanel
             children        :children}
            (bean/bean props)
            {keyboard-max-height :max-height} (use-keyboard-dimension)
            bottom (safe-area/get-bottom)
            {on-layout  :on-layout
             bar-height :height}
            (rn/use-layout)

            visible has-panel
            panel-on-screen (* -1 (- keyboard-max-height bottom (tabbar/get-height)))
            max-delta (min 0 (if has-panel panel-on-screen 0))
            panel-height (* -1 max-delta)
            on-update (fn []
                        (when on-update-inset
                          (on-update-inset (+ bar-height panel-height))))
            children (react/get-children children)]
        (react/effect! on-update [panel-height bar-height])
        (rn/use-back-handler
         (fn []
           (when visible
             (on-close))
           visible))
        (reagent/as-element
         [animated/view
          {:style {:position         :absolute
                   :left             0
                   :right            0
                   :bottom           0
                   :background-color (:ui-background @colors/theme)}}
          [rn/view {:on-layout on-layout}
           (first children)]
          [rn/view
           {:style {:flex   1
                    :height (when (pos? panel-height) panel-height)}}
           (second children)]]))))))

(def view (if platform/android? android-view ios-view))
