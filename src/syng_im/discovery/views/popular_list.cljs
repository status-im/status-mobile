(ns syng-im.discovery.views.popular-list
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [syng-im.components.react :refer [view
                                      list-view
                                      list-item
                                      touchable-highlight
                                      text]]
    [syng-im.discovery.styles :as st]
    [syng-im.utils.listview :refer [to-datasource]]
    [syng-im.discovery.views.popular-list-item :refer [popular-list-item]]))

(defn render-row [row _ _]
  (list-item [popular-list-item row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defview discovery-popular-list [tag count]
  [discoveries [:get-discoveries-by-tag tag 3]]
  [view st/popular-list-container
   [view st/row
    [view st/tag-name-container
     [touchable-highlight {:onPress #(dispatch [:show-discovery-tag tag])}
      [text {:style st/tag-name} (str " #" (name tag))]]]
    [view st/tag-count-container
     [text {:style st/tag-count} count]]]
   [list-view {:dataSource          (to-datasource discoveries)
               :enableEmptySections true
               :renderRow           render-row
               :renderSeparator     render-separator
               :style               st/popular-list}]])
