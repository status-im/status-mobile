(ns syng-im.discovery.views.popular
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      text]]
    [syng-im.components.carousel.carousel :refer [carousel]]
    [syng-im.discovery.styles :as st]
    [syng-im.discovery.views.popular-list :refer [discovery-popular-list]]))

(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(defview popular []
  [popular-tags [:get-popular-tags 3]]
  (if (pos? (count popular-tags))
    [carousel {:pageStyle st/carousel-page-style
               :sneak     20}
     (for [{:keys [name count]} popular-tags]
       [discovery-popular-list name count])]
    [text "None"]))
