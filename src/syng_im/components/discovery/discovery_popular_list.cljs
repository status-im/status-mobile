(ns syng-im.components.discovery.discovery-popular-list
  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      view
                                      list-view
                                      touchable-highlight
                                      text
                                      image]]
    [reagent.core :as r]
    [syng-im.components.realm :refer [list-view]]
    [syng-im.utils.listview :refer [to-realm-datasource]]
    [syng-im.components.discovery.discovery-popular-list-item :refer [discovery-popular-list-item] ])
  )


(defn render-row [row section-id row-id]
  (let [elem (discovery-popular-list-item row)]
    elem)
)

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style {:borderBottomWidth 1
                               :borderBottomColor "#eff2f3"}
                                  :key rowID}])]
    elem))

(defn discovery-popular-list [tag count navigator]
  (let [discoveries (subscribe [:get-discoveries-by-tag tag 3])]
    (log/debug "Got discoveries for tag (" tag "): " @discoveries)
    [view {:style {:flex 1
                   :backgroundColor "white"
                   :paddingLeft 10
                   :paddingTop 16}}
     [view {:style {:flexDirection "row"
                    :backgroundColor "white"
                    :padding 0}}
      [view {:style {
                     :flexDirection "column"}}
       [touchable-highlight {:onPress (fn [event]
                                        (dispatch [:show-discovery-tag tag navigator :push]))}
        [view {:style {:backgroundColor "#eef2f5"
                     :borderRadius 5
                     :padding 4}}
         [text {:style {:color "#7099e6"
                      :fontFamily "sans-serif-medium"
                      :fontSize   14
                      :paddingRight 5
                      :paddingBottom 2
                      :alignItems "center"
                      :justifyContent "center"}}
        (str " #" (name tag))]]]]
      [view {:style {:flex 0.2
                     :alignItems "flex-end"
                     :paddingTop 10
                     :paddingRight 9}}
       [text {:style {:color "#838c93"
                      :fontFamily "sans-serif"
                      :fontSize   12
                      :paddingRight 5
                      :paddingBottom 2
                      :alignItems "center"
                      :justifyContent "center"}}
        count]]]
     [list-view {:dataSource (to-realm-datasource @discoveries)
                 :enableEmptySections true
                 :renderRow  render-row
                 :renderSeparator render-separator
                 :style      {:backgroundColor "white"
                              :paddingTop 13}}]
     ]))
