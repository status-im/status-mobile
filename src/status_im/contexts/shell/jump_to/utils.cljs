(ns status-im.contexts.shell.jump-to.utils
  (:require
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.contexts.shell.jump-to.state :as state]
    [status-im.feature-flags :as ff]
    [utils.re-frame :as rf]))

;;;;  Helper Functions

;;; UI
(defn bottom-tabs-container-height
  []
  (if platform/android?
    shell.constants/bottom-tabs-container-height-android
    shell.constants/bottom-tabs-container-height-ios))

(defn bottom-tabs-extended-container-height
  []
  (if platform/android?
    shell.constants/bottom-tabs-container-extended-height-android
    shell.constants/bottom-tabs-container-extended-height-ios))

(defn status-bar-offset
  []
  (if platform/android? (safe-area/get-top) 0))

;; status bar height is not included in : the dimensions/window for devices with a notch
;; https://github.com/facebook/react-native/issues/23693#issuecomment-662860819
;; More info - https://github.com/status-im/status-mobile/issues/14633
(defn dimensions
  []
  (let [{:keys [width height]} (rn/get-window)]
    {:width  width
     :height (or @state/screen-height
                 (if (> (status-bar-offset) 28)
                   (+ height (status-bar-offset))
                   height))}))

;;;; State

;;; Home Stack
(defn home-stack-open?
  []
  (let [state @state/home-stack-state]
    (or (= state shell.constants/open-with-animation)
        (= state shell.constants/open-without-animation))))

(defn calculate-home-stack-state-value
  [stack-id animate?]
  (if animate?
    (if (some? stack-id)
      shell.constants/open-with-animation
      shell.constants/close-with-animation)
    (if (some? stack-id)
      shell.constants/open-without-animation
      shell.constants/close-without-animation)))

(defn load-stack
  [stack-id]
  (case stack-id
    :communities-stack (reset! state/load-communities-stack? true)
    :chats-stack       (reset! state/load-chats-stack? true)
    :wallet-stack      (reset! state/load-wallet-stack? true)
    :browser-stack     (reset! state/load-browser-stack? true)
    ""))

(defn change-selected-stack-id
  [stack-id store? home-stack-state-value]
  (let [home-stack-state-value (or home-stack-state-value
                                   (calculate-home-stack-state-value stack-id nil))]
    (reset! state/selected-stack-id stack-id)
    (reset! state/home-stack-state home-stack-state-value)
    (when store?
      (async-storage/set-item! :selected-stack-id stack-id))))

(defn reset-bottom-tabs
  []
  (let [selected-stack-id @state/selected-stack-id]
    (reset! state/load-communities-stack? (= selected-stack-id :communities-stack))
    (reset! state/load-chats-stack? (= selected-stack-id :chats-stack))
    (reset! state/load-wallet-stack? (= selected-stack-id :wallet-stack))
    (reset! state/load-browser-stack? (= selected-stack-id :browser-stack))))

(defn reset-floating-screens
  []
  (reset! state/floating-screens-state {})
  (when @state/shared-values-atom
    (doseq [screen-id (seq shell.constants/floating-screens)]
      (reanimated/set-shared-value
       (:screen-state (get @state/shared-values-atom screen-id))
       shell.constants/close-screen-with-shell-animation))))

;;; Floating screen
(defn- screen-state-open?
  [state]
  (#{shell.constants/open-screen-without-animation
     shell.constants/open-screen-with-shell-animation
     shell.constants/open-screen-with-slide-from-right-animation
     shell.constants/open-screen-with-slide-from-bottom-animation}
   state))

(defn floating-screen-open?
  [screen-id]
  (screen-state-open? (get @state/floating-screens-state screen-id)))

(defn open-floating-screens
  []
  (reduce (fn [acc [screen-id state]]
            (let [open? (screen-state-open? state)]
              (if open? (assoc acc screen-id true) acc)))
          {}
          @state/floating-screens-state))

;;; Navigation
(defn shell-navigation?
  [view-id]
  (and (ff/enabled? ::ff/shell.jump-to)
       (some #{view-id} shell.constants/floating-screens)))

(defn calculate-view-id
  []
  (let [screens (open-floating-screens)]
    (cond
      (get screens shell.constants/chat-screen)
      shell.constants/chat-screen
      (get screens shell.constants/community-screen)
      shell.constants/community-screen
      (get screens shell.constants/discover-communities-screen)
      shell.constants/discover-communities-screen
      :else (or @state/selected-stack-id :shell))))

(defn update-view-id
  [view-id]
  (rf/dispatch [:set-view-id view-id]))
