(ns status-im.ui.screens.chat.components.accessory
  (:require [quo.animated :as animated]
            [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.chat.components.hooks :refer [use-keyboard-dimension]]
            [status-im.ui.components.tabbar.styles :as tabs.styles]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.react-native :as rn]
            [quo.components.safe-area :refer [use-safe-area]]))

(def tabbar-height tabs.styles/minimized-tabs-height)

(defn create-pan-responder [y pan-active]
  (js->clj (.-panHandlers
            ^js (.create
                 ^js rn/pan-responder
                 #js {:onPanResponderGrant   (fn []
                                               (animated/set-value pan-active 1))
                      :onPanResponderMove    (fn [_ ^js state]
                                               (animated/set-value y (.-moveY state)))
                      :onPanResponderRelease (fn []
                                               (js/setTimeout
                                                #(animated/set-value y 0)
                                                100))
                      :onPanResponderEnd     (fn []
                                               (animated/set-value pan-active 0))}))))

(def view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{on-update-inset :onUpdateInset
             y               :y
             pan-state       :panState
             on-close        :onClose
             has-panel       :hasPanel
             children        :children}           (bean/bean props)
            {keyboard-height       :height
             keyboard-max-height   :max-height
             duration              :duration
             keyboard-end-position :end-position} (use-keyboard-dimension)
            {:keys [bottom]}                      (use-safe-area)
            {on-layout  :on-layout
             bar-height :height}                  (rn/use-layout)

            visible         (or has-panel (pos? keyboard-height))
            anim-visible    (animated/use-value visible)
            kb-on-screen    (if platform/android? 0 (* -1 (- keyboard-height bottom tabbar-height)))
            panel-on-screen (* -1 (- keyboard-max-height bottom tabbar-height))
            max-delta       (min 0 (if has-panel panel-on-screen kb-on-screen))
            panel-height    (* -1 max-delta)
            end-position    (- keyboard-end-position (when has-panel keyboard-max-height))
            delay           (+ (/ (- keyboard-max-height panel-height)
                                  (/ keyboard-max-height duration))
                               16)
            drag-diff       (animated/clamp (animated/sub y end-position) 0 keyboard-max-height)
            animated-y      (react/use-memo
                             (fn []
                               (animated/mix
                                (animated/with-timing-transition anim-visible
                                  {:duration (- duration delay)
                                   :easing   (:keyboard animated/easings)})
                                0
                                panel-on-screen))
                             [duration panel-on-screen])
            delta-y         (animated/clamp (animated/add drag-diff animated-y) max-delta 0)
            on-update       (react/callback
                             (fn []
                               (when on-update-inset
                                 (on-update-inset (+ bar-height panel-height))))
                             [panel-height bar-height])
            children        (react/get-children children)]
        (react/effect! on-update)
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
         [visible keyboard-max-height duration])
        (rn/use-back-handler
         (fn []
           (when visible
             (on-close))
           visible))
        (reagent/as-element
         [animated/view {:style {:position         :absolute
                                 :left             0
                                 :right            0
                                 :background-color (:ui-background @colors/theme)
                                 :bottom           max-delta
                                 :transform        [{:translateY delta-y}]}}
          [rn/view {:on-layout on-layout}
           (first children)]
          [rn/view {:style {:flex   1
                            :height (when (pos? panel-height) panel-height)}}
           (second children)]]))))))
