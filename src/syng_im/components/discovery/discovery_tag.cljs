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
    [syng-im.resources :as res]))

(defn render-row [row section-id row-id]
  (log/debug "discovery-tag-row: " row section-id row-id)
  (if row
    (let [elem (discovery-popular-list-item row)]
      elem)
    (r/as-element [text "null"])
  ))

(defn render-separator [sectionID, rowID, adjacentRowHighlighted]
  (let [elem (r/as-element [view {:style {:borderBottomWidth 1
                                          :borderBottomColor "#eff2f3"}
                                  :key   rowID}])]
    elem))

(defn title-content [tag]
  [view {:style {:backgroundColor "#eef2f5"
                 :flexWrap :wrap
                 :borderRadius 5
                 :padding 4}}
   [text {:style {:color "#7099e6"
                  :fontFamily "sans-serif-medium"
                  :fontSize   14
                  :paddingRight 5
                  :paddingBottom 2}}
    (str " #" tag)]])

(defn discovery-tag [{:keys [tag navigator]}]
  (let [tag (subscribe [:get-current-tag])
        discoveries (subscribe [:get-discoveries-by-tag @tag 0])]
    (log/debug "Got discoveries: " @discoveries)
    (fn []
      (let [items @discoveries
            datasource (to-realm-datasource items)]
    [view {:style {:flex            1
                   :backgroundColor "#eef2f5"}}
     [toolbar {:navigator navigator
               :nav-action {:image {:source {:uri "icon_back"}
                                    :style  {:width      8
                                             :height     14}}
                            :handler (fn [] (nav-pop navigator))}
               :title     "Add Participants"
               :content   (title-content @tag)
               :action    {:image {:source {:uri "icon_search"}
                                   :style  {:width  17
                                            :height 17}}
                           :handler (fn []
                                      ())}}]

     [list-view {:dataSource datasource
                 :enableEmptySections true
                 :renderRow  render-row
                 :renderSeparator render-separator
                 :style      {:backgroundColor "white"
                              :paddingLeft 15}}]
     ]))))