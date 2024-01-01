(ns status-im.contexts.shell.jump-to.components.floating-screens.view
  (:require
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messages.view :as chat]
    [status-im.contexts.communities.discover.view :as communities.discover]
    [status-im.contexts.communities.overview.view :as communities.overview]
    [status-im.contexts.shell.jump-to.animation :as animation]
    [status-im.contexts.shell.jump-to.components.floating-screens.style :as style]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.contexts.shell.jump-to.state :as state]
    [status-im.contexts.shell.jump-to.utils :as utils]
    [utils.re-frame :as rf]))

(def screens-map
  {shell.constants/chat-screen                 chat/chat
   shell.constants/community-screen            communities.overview/overview
   shell.constants/discover-communities-screen communities.discover/view})

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
       :accessible          true}
      [(get screens-map screen-id) id]]]))

(defn lazy-screen
  [screen-id]
  (let [screen-param (rf/sub [:shell/floating-screen screen-id])]
    (when screen-param
      [:f> f-screen (assoc screen-param :screen-id screen-id)])))

(defn view
  []
  [:<>
   [lazy-screen shell.constants/discover-communities-screen]
   [lazy-screen shell.constants/community-screen]
   [lazy-screen shell.constants/chat-screen]])
