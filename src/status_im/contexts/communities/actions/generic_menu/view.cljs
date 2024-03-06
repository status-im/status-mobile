(ns status-im.contexts.communities.actions.generic-menu.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.communities.actions.generic-menu.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [id title]} children]
  (let [{:keys [name images]}                   (rf/sub [:communities/community id])
        [content-scroll-y set-content-scroll-y] (rn/use-state 0)
        {window-height :height}                 (rn/get-window)
        max-bottom-sheet-height                 (- window-height 44)]
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
     [rn/scroll-view
      {:on-layout                       (fn [event]
                                          (set-content-scroll-y (oops/oget event
                                                                           "nativeEvent.layout.height")))
       :scroll-enabled                  (when (> content-scroll-y max-bottom-sheet-height) true)
       :shows-vertical-scroll-indicator false}
      children]]))
