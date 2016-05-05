(ns syng-im.components.discovery.discovery-recent
  (:require-macros
    [natal-shell.data-source :refer [data-source clone-with-rows]]

    )
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.components.react :refer [android?
                                      view]]
    [syng-im.components.realm :refer [list-view]]
    [syng-im.utils.listview :refer [to-realm-datasource]]
    [syng-im.components.discovery.discovery-popular-list-item :refer [discovery-popular-list-item]]
    [reagent.core :as r]))


(defn render-row [row section-id row-id]
  (let [elem (discovery-popular-list-item row)]
    elem)
  )

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style {:borderBottomWidth 1
                                          :borderBottomColor "#eff2f3"}
                                  :key rowID}])]
    elem))

(defn discovery-recent []
  (let [discoveries (subscribe [:get-discoveries])
        datasource (to-realm-datasource @discoveries)]
    [list-view {:dataSource datasource
                :enableEmptySections true
                :renderRow  render-row
                :renderSeparator render-separator
                :style      {:backgroundColor "white"
                             :paddingLeft 15}}]
  ))