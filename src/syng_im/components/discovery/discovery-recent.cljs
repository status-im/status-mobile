(ns syng-im.components.discovery.discovery-recent
  (:require-macros
    [natal-shell.data-source :refer [data-source clone-with-rows]]

    )
  (:require
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      list-view
                                      text
                                      image
                                      navigator
                                      toolbar-android]]
    [syng-im.components.carousel :refer [carousel]]
    [syng-im.components.discovery.discovery-popular-list-item :refer [discovery-popular-list-item]]
    [syng-im.models.discoveries :refer [generate-discoveries]]
    [reagent.core :as r]
    [syng-im.resources :as res]))


(defn render-row [row section-id row-id]
  (let [elem (discovery-popular-list-item row)]
    elem)
  )

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style {:borderBottomWidth 1
                               :borderBottomColor "#eff2f3"}
                                  :key rowID}])]
    elem))

(defn get-data-source [elements]
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= (:discovery-id row1) (:discovery-id row2)))})
                   elements))

(defn discovery-recent [recent-discoveries]
  [list-view {:dataSource (get-data-source recent-discoveries)
              :renderRow  render-row
              :renderSeparator render-separator
              :style      {:backgroundColor "white"
                           :paddingLeft 15}}]
  )