(ns status-im2.contexts.shell.jump-to.animation
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.shell.jump-to.constants :as shell.constants]
    [status-im2.contexts.shell.jump-to.state :as state]
    [status-im2.contexts.shell.jump-to.utils :as utils]
    [utils.re-frame :as rf]))

;;;; Home stack
(defn open-home-stack
  [stack-id animate?]
  (let [home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (js/setTimeout
     (fn []
       (utils/load-stack stack-id)
       (utils/change-shell-status-bar-style))
     (if animate? shell.constants/shell-animation-time 0))))

(defn change-tab
  [stack-id]
  (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) false)
  (reanimated/set-shared-value (:selected-stack-id @state/shared-values-atom) (name stack-id))
  (utils/load-stack stack-id)
  (utils/change-selected-stack-id stack-id true nil))

(defn bottom-tab-on-press
  [stack-id animate?]
  (when (and @state/shared-values-atom (not= stack-id @state/selected-stack-id))
    (if (utils/home-stack-open?)
      (change-tab stack-id)
      (open-home-stack stack-id animate?))
    (when animate? (utils/update-view-id (or stack-id :shell)))))

(defn close-home-stack
  [animate?]
  (let [stack-id               nil
        home-stack-state-value (utils/calculate-home-stack-state-value stack-id animate?)]
    (reanimated/set-shared-value (:animate-home-stack-left @state/shared-values-atom) true)
    (reanimated/set-shared-value (:home-stack-state @state/shared-values-atom) home-stack-state-value)
    (utils/change-selected-stack-id stack-id true home-stack-state-value)
    (utils/change-shell-status-bar-style)
    (when animate? (utils/update-view-id (or stack-id :shell)))))

;;;; Floating Screen

;; Dispatch Delay - Animation time for the opening of a screen is 200 ms,
;; But starting and completion of animation sometimes takes a little extra time,
;; according to the performance of the device. And if before the animation is
;; complete we start other tasks like rendering messages or opening of the home screen
;; in the background then the animation breaks. So we are adding a small delay for that dispatch.
(defn animate-floating-screen
  [screen-id {:keys [id animation community-id hidden-screen?]}]
  (when (not= animation (get @state/floating-screens-state screen-id))
    ;; Animate Floating Screen
    (reanimated/set-shared-value
     (get-in @state/shared-values-atom [screen-id :screen-state])
     animation)
    (reset! state/floating-screens-state
      (assoc @state/floating-screens-state screen-id animation))
    (let [floating-screen-open? (utils/floating-screen-open? screen-id)
          animation-time        (if (#{shell.constants/open-screen-without-animation
                                       shell.constants/close-screen-without-animation}
                                     animation)
                                  0
                                  shell.constants/shell-animation-time)
          dispatch-delay        (cond
                                  (not floating-screen-open?) 0
                                  js/goog.DEBUG               100
                                  platform/android?           75
                                  :else                       50)
          dispatch-time         (+ animation-time dispatch-delay)]
      (js/setTimeout
       (fn [floating-screen-open?]
         (if floating-screen-open?
           ;; Events realted to opening of a screen
           (rf/dispatch [:shell/floating-screen-opened screen-id
                         id community-id hidden-screen?])
           ;; Events realted to closing of a screen
           (rf/dispatch [:shell/floating-screen-closed screen-id])))
       dispatch-time
       floating-screen-open?))))

(defn set-floating-screen-position
  [left top card-type]
  (let [screen-id (cond
                    (#{shell.constants/one-to-one-chat-card
                       shell.constants/private-group-chat-card
                       shell.constants/community-channel-card}
                     card-type)
                    shell.constants/chat-screen

                    (= card-type shell.constants/community-card)
                    shell.constants/community-screen

                    :else nil)]
    (when screen-id
      (reanimated/set-shared-value
       (get-in @state/shared-values-atom [screen-id :screen-left])
       left)
      (reanimated/set-shared-value
       (get-in @state/shared-values-atom [screen-id :screen-top])
       top))))
