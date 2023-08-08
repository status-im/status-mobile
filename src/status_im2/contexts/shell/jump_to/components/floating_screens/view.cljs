(ns status-im2.contexts.shell.jump-to.components.floating-screens.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.view :as chat]
            [status-im2.contexts.shell.jump-to.state :as state]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [status-im2.contexts.shell.jump-to.animation :as animation]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]
            [status-im2.contexts.communities.overview.view :as communities.overview]
            [status-im2.contexts.shell.jump-to.components.floating-screens.style :as style]))

(def screens-map
  {shell.constants/community-screen communities.overview/overview
   shell.constants/chat-screen      chat/chat})

(defn f-screen
  [screen-id {:keys [id animation clock] :as screen-param}]
  ;; First render screen, then animate (smoother animation)
  (rn/use-effect
   (fn []
     (animation/animate-floating-screen screen-id screen-param))
   [animation id clock])
  [reanimated/view
   {:style (style/screen (get @state/shared-values-atom screen-id) screen-id)}
   [rn/view
    {:style               (style/screen-container (utils/dimensions))
     :key                 id}
    [:f> (get screens-map screen-id) id]]])

;; Currently chat screen and events both depends on current-chat-id, once we remove
;; use of current-chat-id from view then we can keep last chat loaded, for fast navigation
(defn lazy-screen
  [screen-id]
  (let [screen-param (rf/sub [:shell/floating-screen screen-id])]
    (when screen-param
      [:f> f-screen screen-id screen-param])))

(defn view
  []
  [:<>
   [lazy-screen shell.constants/community-screen]
   [lazy-screen shell.constants/chat-screen]])
