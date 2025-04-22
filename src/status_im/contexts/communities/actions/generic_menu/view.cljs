(ns status-im.contexts.communities.actions.generic-menu.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.communities.actions.generic-menu.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [id title]} children]
  (let [{:keys [name images]}                   (rf/sub [:communities/community id])
        [content-height set-content-height]     (rn/use-state 0)
        [sheet-header-height set-header-height] (rn/use-state 0)
        insets                                  safe-area/insets
        {window-height :height}                 (rn/get-window)
        sheet-max-height                        (- window-height (:top insets))
        max-height                              (- sheet-max-height sheet-header-height)]
    [rn/view {:style (style/container max-height)}
     [rn/view
      {:style     {:padding-bottom 12}
       :on-layout (fn [event]
                    (set-header-height (oops/oget
                                        event
                                        "nativeEvent.layout.height")))}
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
     [gesture/scroll-view
      {:scroll-enabled                  (> content-height max-height)
       :scroll-event-throttle           16
       :style                           (style/scroll-view-style max-height)
       :shows-vertical-scroll-indicator false}
      [rn/view
       {:on-layout (fn [event]
                     (set-content-height (oops/oget
                                          event
                                          "nativeEvent.layout.height")))}
       children]]]))
