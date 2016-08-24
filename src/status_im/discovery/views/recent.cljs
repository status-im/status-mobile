(ns status-im.discovery.views.recent
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view list-view list-item]]
    [status-im.utils.listview :refer [to-datasource]]
    [status-im.discovery.styles :as st]
    [status-im.discovery.views.discovery-list-item :refer [discovery-list-item]]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defview discovery-recent [{platform-specific :platform-specific}]
  [discoveries [:get :discoveries]]
  [view st/recent-list
   (for [{:keys [message-id] :as discovery} discoveries]
     ^{:key (str "message-" message-id)}
     [discovery-list-item discovery platform-specific])])
