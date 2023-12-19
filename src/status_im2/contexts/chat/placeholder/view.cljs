(ns status-im2.contexts.chat.placeholder.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.chat.placeholder.style :as style]
    [utils.worklets.chat.messages :as worklets]))

(defn- loading-skeleton
  []
  [quo/skeleton-list
   {:content       :messages
    :parent-height (:height (rn/get-window))
    :animated?     false}])

(defn f-view
  [calculations-complete?]
  (let [top     (safe-area/get-top)
        opacity (worklets/placeholder-opacity calculations-complete?)
        z-index (worklets/placeholder-z-index calculations-complete?)]
    [reanimated/view {:style (style/container top opacity z-index)}
     [loading-skeleton]]))
