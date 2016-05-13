(ns syng-im.discovery.views.popular
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      text]]
    [syng-im.components.carousel.carousel :refer [carousel]]
    [syng-im.discovery.styles :as st]
    [syng-im.discovery.discovery-popular-list :refer [discovery-popular-list]]))

(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(defn popular []
  (let [popular-tags (subscribe [:get-popular-tags 3])]
    (log/debug "Got popular tags: " @popular-tags)
    (if (pos? (count @popular-tags))
      [carousel {:pageStyle st/carousel-page-style
                 :sneak     20}
       (for [{:keys [name count]} @popular-tags]
         [discovery-popular-list name count])]
      [text "None"])))
