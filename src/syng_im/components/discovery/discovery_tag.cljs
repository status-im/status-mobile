(ns syng-im.components.discovery.discovery-tag
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.utils.logging :as log]
    [syng-im.utils.listview :refer [to-realm-datasource
                                    to-datasource]]
    [syng-im.navigation :refer [nav-pop]]
    [syng-im.components.react :refer [android?
                                      view
                                      text]]
    [syng-im.components.realm :refer [list-view]]
    [syng-im.components.toolbar :refer [toolbar]]
    [reagent.core :as r]
    [syng-im.components.discovery.discovery-popular-list-item :refer [discovery-popular-list-item]]
    [syng-im.components.discovery.styles :as st]))

(defn render-row [row section-id row-id]
  (log/debug "discovery-tag-row: " row section-id row-id)
  (if row
    (let [elem (discovery-popular-list-item row)]
      elem)
    (r/as-element [text "null"])))

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style st/row-separator
                                  :key   rowID}])]
    elem))

(defn title-content [tag]
  [view {:style st/tag-title-container}
   [view {:style st/tag-container}
    [text {:style st/tag-title}
     (str " #" tag)]]])

(defn discovery-tag [{:keys [tag navigator]}]
  (let [tag (subscribe [:get-current-tag])
        discoveries (subscribe [:get-discoveries-by-tag @tag 0])]
    (log/debug "Got discoveries: " @discoveries)
    (fn []
      (let [items @discoveries
            datasource (to-realm-datasource items)]
        [view {:style st/discovery-tag-container}
         [toolbar {:navigator      navigator
                   :custom-content [title-content @tag]
                   :action         {:image   {:source {:uri "icon_search"}
                                              :style  st/icon-search}
                                    :handler (fn []
                                               ())}}]

         [list-view {:dataSource          datasource
                     :enableEmptySections true
                     :renderRow           render-row
                     :renderSeparator     render-separator
                     :style               st/recent-list}]
         ]))))