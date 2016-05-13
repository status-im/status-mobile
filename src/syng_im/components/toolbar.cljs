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
                                               toolbar-background1]]))

(defn toolbar [{:keys [title nav-action action background-color content style]}]
  (let [style (merge {:flexDirection   :row
                      :backgroundColor (or background-color toolbar-background1)
                      :height          56
                      :elevation       2} style)]
    [view {:style style}
     (if nav-action
       [touchable-highlight {:on-press (:handler nav-action)}
        [view {:width          56
               :height         56
               :alignItems     :center
               :justifyContent :center}
         [image (:image nav-action)]]]
       [touchable-highlight {:on-press #(dispatch [:navigate-back])}
        [view {:width  56
               :height 56}
         [icon :back {:marginTop  21
                       :marginLeft 23
                       :width      8
                       :height     14}]]])
     (if content
       [view {:flex           1
              :alignItems     :center
              :justifyContent :center}
        content]
       [view {:flex           1
              :alignItems     :center
              :justifyContent :center}
        [text {:marginTop  -2.5
               :color      text1-color
               :fontSize   16
               :fontFamily font}
         title]])
     [touchable-highlight {:on-press (:handler action)}
      [view {:width          56
             :height         56
             :alignItems     :center
             :justifyContent :center}
       [image (:image action)]]]]))

