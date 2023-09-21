(ns status-im2.contexts.shell.jump-to.gesture
  (:require [utils.re-frame :as rf]
            [react-native.gesture :as gesture]
            [utils.worklets.shell :as worklets.shell]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [status-im2.contexts.shell.jump-to.state :as state]
            [status-im2.contexts.shell.jump-to.constants :as constants]))

(defn on-screen-closed
  [animation-time]
  (js/setTimeout
   #(rf/dispatch [:shell/navigate-back constants/close-screen-without-animation])
   (or animation-time constants/shell-animation-time)))

;; Make sure issue is fixed before enabling gesture for floating screens
;; Issue: https://github.com/status-im/status-mobile/pull/16438#issuecomment-1621397789
;; More Info: https://github.com/status-im/status-mobile/pull/16438#issuecomment-1622589147
(defn floating-screen-gesture
  [screen-id]
  (let [{:keys [screen-left screen-state]} (get @state/shared-values-atom screen-id)
        {:keys [width]}                    (utils/dimensions)]
    (-> (gesture/gesture-pan)
        (gesture/min-distance 0)
        (gesture/max-pointers 1)
        (gesture/fail-offset-x -1)
        (gesture/hit-slop (clj->js {:left 0 :width constants/gesture-width}))
        (gesture/on-update (worklets.shell/floating-screen-gesture-on-update screen-left))
        (gesture/on-end
         (worklets.shell/floating-screen-gesture-on-end
          {:screen-left            screen-left
           :screen-state           screen-state
           :screen-width           width
           :left-velocity          constants/gesture-fling-left-velocity
           :right-velocity         constants/gesture-fling-right-velocity
           :screen-closed-callback on-screen-closed})))))

