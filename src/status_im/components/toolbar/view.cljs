(ns status-im.components.toolbar.view
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.sync-state.gradient :refer [sync-state-gradient-view]]
            [status-im.components.styles :refer [icon-back]]
            [status-im.components.toolbar.styles :as st]
            [status-im.utils.platform :refer [platform-specific]]))

(defn toolbar [{title                :title
                nav-action           :nav-action
                hide-nav?            :hide-nav?
                actions              :actions
                custom-action        :custom-action
                background-color     :background-color
                custom-content       :custom-content
                style                :style}]
  (let [style (merge (st/toolbar-wrapper background-color) style)]
    [view {:style style}
     [view st/toolbar
      [view (st/toolbar-nav-actions-container actions)
       (when (not hide-nav?)
         (if nav-action
           [touchable-highlight {:on-press (:handler nav-action)}
            [view (get-in platform-specific [:component-styles :toolbar-nav-action])
             [image (:image nav-action)]]]
           [touchable-highlight {:on-press            #(dispatch [:navigate-back])
                                 :accessibility-label :navigate-back}
            [view (get-in platform-specific [:component-styles :toolbar-nav-action])
             [image {:source {:uri :icon_back}
                     :style  icon-back}]]]))]
      (or custom-content
          [view {:style st/toolbar-title-container}
           [text {:style st/toolbar-title-text
                  :font  :toolbar-title}
            title]])
      [view (st/toolbar-actions-container (count actions) (or custom-content custom-action))
       (if actions
         (for [{action-image   :image
                action-handler :handler} actions]
           ^{:key (str "action-" action-image)}
           [touchable-highlight {:on-press action-handler}
            [view st/toolbar-action
             [image action-image]]])
         custom-action)]]
     [sync-state-gradient-view]]))

