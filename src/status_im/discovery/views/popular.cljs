(ns status-im.discovery.views.popular
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [text]]
    [status-im.components.carousel.carousel :refer [carousel]]
    [status-im.discovery.styles :as st]
    [status-im.discovery.views.popular-list :refer [discovery-popular-list]]
    [status-im.i18n :refer [label]]
    [status-im.components.react :as r]))

(defn page-width []
  (.-width (.get (.. r/react-native -Dimensions) "window")))

(defview discovery-popular [{:keys [contacts]}]
  [popular-tags [:get-popular-tags 10]]
  (if (pos? (count popular-tags))
    [carousel {:pageStyle st/carousel-page-style}
     (for [{:keys [name count]} popular-tags]
       [discovery-popular-list {:tag      name
                                :count    count
                                :contacts contacts}])]
    [text (label :t/none)]))
