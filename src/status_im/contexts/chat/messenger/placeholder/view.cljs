(ns status-im.contexts.chat.messenger.placeholder.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.placeholder.style :as style]
    [utils.worklets.chat.messenger.placeholder :as worklets]))

(defn- loading-skeleton
  []
  [quo/skeleton-list
   {:content       :messages
    :parent-height (:height (rn/get-window))
    :animated?     false}])

(defn view
  [chat-screen-layout-calculations-complete?]
  (let [theme   (quo.theme/use-theme)
        top     (safe-area/get-top)
        opacity (worklets/placeholder-opacity chat-screen-layout-calculations-complete?)
        z-index (worklets/placeholder-z-index chat-screen-layout-calculations-complete?)]
    [reanimated/view {:style (style/container top opacity z-index theme)}
     [loading-skeleton]]))
