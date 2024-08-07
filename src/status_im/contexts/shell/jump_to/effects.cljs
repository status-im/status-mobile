(ns status-im.contexts.shell.jump-to.effects
  (:require
    [status-im.contexts.shell.jump-to.animation :as animation]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.contexts.shell.jump-to.state :as state]
    [status-im.contexts.shell.jump-to.utils :as shell.utils]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.shell/change-tab
 (fn [stack-id]
   (when (some #(= stack-id %) shell.constants/stacks-ids)
     (animation/bottom-tab-on-press stack-id false))))

(rf/reg-fx :effects.shell/navigate-to-jump-to
 (fn []
   (animation/close-home-stack false)
   (some-> ^js @state/jump-to-list-ref
           (.scrollToOffset #js {:y 0 :animated false}))))

;; Note - pop-to-root resets currently opened screens to `close-screen-without-animation`.
;; This might take some time. So don't directly merge the effect of `pop-to-root` and
;; `navigate-to` for the floating screen. Because it might close even the currently opened screen.
;; https://github.com/status-im/status-mobile/pull/16438#issuecomment-1623954774
(rf/reg-fx :effects.shell/pop-to-root
 (fn []
   (shell.utils/reset-floating-screens)))

(rf/reg-fx :effects.shell/reset-state
 (fn []
   (reset! state/floating-screens-state {})))
