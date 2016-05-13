(ns syng-im.discovery.discovery-popular-list
  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      view
                                      list-view
                                      list-item
                                      touchable-highlight
                                      text
                                      image]]
    [reagent.core :as r]
    [syng-im.discovery.styles :as st]
    [syng-im.utils.listview :refer [to-realm-datasource to-datasource2]]
    [syng-im.discovery.discovery-popular-list-item :refer [discovery-popular-list-item] ])
  )


(defn render-row [row _ _]
  (list-item [discovery-popular-list-item row]))

(defn render-separator [sectionID rowID adjacentRowHighlighted]
  (list-item [view {:style st/row-separator
                       :key   rowID}]))

(defn discovery-popular-list [tag count]
  (let [discoveries (subscribe [:get-discoveries-by-tag tag 3])]
    (fn [tag count]
      [view st/popular-list-container
       [view st/row
        [view st/tag-name-container
         [touchable-highlight {:onPress #(dispatch [:show-discovery-tag tag])}
          [text {:style st/tag-name} (str " #" (name tag))]]]
        [view st/tag-count-container
         [text {:style st/tag-count} count]]]
       [list-view {:dataSource          (to-datasource2 @discoveries)
                   :enableEmptySections true
                   :renderRow           render-row
                   :renderSeparator     render-separator
                   :style               st/popular-list}]])))
