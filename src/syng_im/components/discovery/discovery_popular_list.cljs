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
    [syng-im.components.discovery.styles :as st]
    [syng-im.utils.listview :refer [to-realm-datasource]]
    [syng-im.components.discovery.discovery-popular-list-item :refer [discovery-popular-list-item] ])
  )


(defn render-row [row section-id row-id]
  (let [elem (discovery-popular-list-item row)]
    elem)
)

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style st/row-separator
                                  :key rowID}])]
    elem))

(defn discovery-popular-list [tag count navigator]
  (let [discoveries (subscribe [:get-discoveries-by-tag tag 3])]
    [view {:style st/popular-list-container}
     [view st/row
      [view st/tag-name-container
       [touchable-highlight {:onPress #(dispatch [:show-discovery-tag tag navigator :push])}
        [text {:style st/tag-name}
         (str " #" (name tag))]]]
      [view {:style st/tag-count-container}
       [text {:style st/tag-count}
        count]]]
     [list-view {:dataSource (to-realm-datasource @discoveries)
                 :enableEmptySections true
                 :renderRow render-row
                 :renderSeparator render-separator
                 :style st/popular-list}]]))
