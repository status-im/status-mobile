(ns status-im.contexts.communities.actions.generic-menu.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.contexts.communities.actions.generic-menu.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [id title]} children]
  (let [{:keys [name images]}                       (rf/sub [:communities/community id])
        [scroll-view-height set-scroll-view-height] (rn/use-state 0)
        {window-height :height}                     (rn/get-window)
        max-bottom-sheet-height                     (- window-height 44)
        ;; This is the default height of the bottom sheet with zero nested views
        empty-bottom-sheet-height                   62
        total-bottom-sheet-height                   (+ scroll-view-height empty-bottom-sheet-height)]
    [gesture/scroll-view
     {:on-layout                       (fn [event]
                                         (set-scroll-view-height (oops/oget
                                                                  event
                                                                  "nativeEvent.layout.height")))
      :scroll-enabled                  (>= total-bottom-sheet-height max-bottom-sheet-height)
      :scroll-event-throttle           16
      :shows-vertical-scroll-indicator false}
     [rn/view {:style style/container}
      [rn/view {:style {:padding-bottom 12}}
       [rn/view {:style style/inner-container}
        [quo/text
         {:accessibility-label :communities-join-community
          :weight              :semi-bold
          :size                :heading-2}
         title]]
       [rn/view {:style style/community-tag}
        [quo/context-tag
         {:type           :community
          :size           24
          :community-logo (:thumbnail images)
          :community-name name}]]]
      children]]))
