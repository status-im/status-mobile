(ns syng-im.components.toolbar
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              text-input
                                              icon
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-purple
                                               text1-color
                                               text2-color
                                               toolbar-background1]]
            [syng-im.components.realm :refer [list-view]]
            [reagent.core :as r]))

(defn toolbar [{:keys [title nav-action hide-nav? action custom-action
                       background-color custom-content style]}]
  (let [style (merge {:flexDirection   "row"
                      :backgroundColor (or background-color toolbar-background1)
                      :height          56
                      :elevation       2} style)]
    [view {:style style}
     (when (not hide-nav?)
       (if nav-action
         [touchable-highlight {:on-press (:handler nav-action)}
          [view {:width          56
                 :height         56
                 :alignItems     "center"
                 :justifyContent "center"}
           [image (:image nav-action)]]]
         [touchable-highlight {:on-press #(dispatch [:navigate-back])}
          [view {:width  56
                 :height 56}
           [image {:source {:uri "icon_back"}
                   :style  {:marginTop  21
                            :marginLeft 23
                            :width      8
                            :height     14}}]]]))
     (or custom-content
         [view {:style {:flex           1
                        :alignItems     "center"
                        :justifyContent "center"}}
          [text {:style {:marginTop  -2.5
                         :color      text1-color
                         :fontSize   16
                         :fontFamily font}}
           title]])
     custom-action
     (when action
       [touchable-highlight {:on-press (:handler action)}
        [view {:width          56
               :height         56

               :alignItems     "center"
               :justifyContent "center"}
         [image (:image action)]]])]))

