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
            [status-im2.contexts.shell.jump-to.components.floating-screens.style :as style]
            [quo2.theme :as quo.theme]))

(def screens-map
  {shell.constants/community-screen communities.overview/overview
   shell.constants/chat-screen      chat/chat})

(defn f-screen
  [{:keys [screen-id id animation clock] :as screen-param}]
  (let [theme (quo.theme/use-theme-value)]
    ;; First render screen, then animate (smoother animation)
    (rn/use-effect
     (fn []
       (animation/animate-floating-screen screen-id screen-param))
     [animation id clock])
    [reanimated/view
     {:style (style/screen (get @state/shared-values-atom screen-id)
                           {:theme     theme
                            :screen-id screen-id})}
     [rn/view
      {:style               (style/screen-container (utils/dimensions))
       :accessibility-label (str screen-id "-floating-screen")
       :accessible          true
       :key                 id}
      [(get screens-map screen-id) id]]]))

;; Currently chat screen and events both depends on current-chat-id, once we remove
;; use of current-chat-id from view then we can keep last chat loaded, for fast navigation
(defn lazy-screen
  [screen-id]
  (let [screen-param (rf/sub [:shell/floating-screen screen-id])]
    (when screen-param
      [:f> f-screen (assoc screen-param :screen-id screen-id)])))

(defn view
  []
  [:<>
   [lazy-screen shell.constants/community-screen]
   [lazy-screen shell.constants/chat-screen]])
