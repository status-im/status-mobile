(ns syng-im.components.discovery.discovery-popular-list
  (:require-macros
    [natal-shell.data-source :refer [data-source clone-with-rows]]
    )
  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.utils.debug :refer [log]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      list-view
                                      text
                                      image
                                      navigator
                                      toolbar-android]]
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

(defn get-data-source [elements]
  (clone-with-rows (data-source {:rowHasChanged (fn [row1 row2]
                                                  (not= (:discovery-id row1) (:discovery-id row2)))})
                   elements))

(defn discovery-popular-list [tag]
  (let [discoveries (subscribe [:get-discoveries-by-tag tag 3])
        _ (log (str "Got discoveries for tag (" tag "): ") @discoveries)
        _ (log @discoveries)]
    (r/as-element [view {:style {:flex 1
                               :backgroundColor "white"
                               :paddingLeft 10
                               :paddingTop 10}}
                 [view {:style {:flexDirection "row"
                                :backgroundColor "white"
                                :padding 0}}
                  [view {:style {:flexDirection "column"
                                 :backgroundColor "#e9f7fe"
                                 :borderRadius 5
                                 :padding 0}}
                  [text {:style {:color "#6092df"
                                :paddingRight 5
                                :paddingBottom 2
                                :alignItems "center"
                                :justifyContent "center"}} (str " #" (name tag))]]]
                 [list-view {:dataSource (to-realm-datasource @discoveries)
                             :renderRow  render-row
                             :renderSeparator render-separator
                             :style      {:backgroundColor "white"}}]
                 ])
  ))

(comment
  list-view {:dataSource elements
             :renderRow  (partial render-row list-element)
             :style      {:backgroundColor "white"}}
  )