(ns status-im.components.status-view.view
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.components.styles :refer [color-blue]]))

(defn tag-view [tag]
  [text {:style {:color color-blue}
         :font :medium}
   (str tag " ")])

(defn status-view [{:keys [style
                           message-id
                           status
                           on-press
                           number-of-lines]
                    :or {message-id "msg"}}]
  [text {:style           style
         :on-press        on-press
         :number-of-lines number-of-lines
         :font            :default}
   (for [[i status] (map-indexed vector (str/split status #" "))]
     (if (.startsWith status "#")
       ^{:key (str "item-" message-id "-" i)}
       [tag-view status]
       ^{:key (str "item-" message-id "-" i)}
       (str status " ")))])
