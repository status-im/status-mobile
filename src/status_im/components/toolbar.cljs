(ns status-im.components.toolbar
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text-input
                                                icon
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.styles :refer [font
                                                 title-font
                                                 color-white
                                                 color-purple
                                                 text1-color
                                                 text2-color
                                                 toolbar-background1
                                                 toolbar-title-container
                                                 toolbar-title-text
                                                 icon-back
                                                 toolbar-height]]
            [status-im.components.realm :refer [list-view]]
            [reagent.core :as r]))

(defn toolbar [{:keys [title nav-action hide-nav? action custom-action
                       background-color custom-content style]}]
  (let [style (merge {:flexDirection   :row
                      :backgroundColor (or background-color toolbar-background1)
                      :height          toolbar-height
                      :elevation       2} style)]
    [view {:style style}
     (when (not hide-nav?)
       (if nav-action
         [touchable-highlight {:on-press (:handler nav-action)}
          [view {:width          56
                 :height         56
                 :alignItems     :center
                 :justifyContent :center}
           [image (:image nav-action)]]]
         [touchable-highlight {:on-press #(dispatch [:navigate-back])}
          [view {:width  56
                 :height 56
                 :alignItems     :center
                 :justifyContent :center}
           [image {:source {:uri :icon_back}
                   :style  icon-back}]]]))
     (or custom-content
         [view {:style toolbar-title-container}
          [text {:style toolbar-title-text}
           title]])
     custom-action
     (when action
       [touchable-highlight {:on-press (:handler action)}
        [view {:width          56
               :height         56

               :alignItems     :center
               :justifyContent :center}
         [image (:image action)]]])]))

