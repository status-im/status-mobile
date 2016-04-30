(ns syng-im.components.discovery.discovery-popular
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      text]]
    [syng-im.components.carousel :refer [carousel]]
    [syng-im.components.discovery.discovery-popular-list :refer [discovery-popular-list]]
    ))

(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(defn discovery-popular []
  (let [popular-tags (subscribe [:get-popular-tags 3])]
    (log/debug "Got popular tags: " @popular-tags)
    (if (> (count @popular-tags) 0)
      [carousel {:pageStyle {:borderRadius  1
                              :shadowColor   "black"
                              :shadowRadius  1
                              :shadowOpacity 0.8
                              :elevation     2
                              :marginBottom  10}
                  :pageWidth (- (page-width) 60)
                  :sneak     20}
       (for [tag @popular-tags]
         (discovery-popular-list (.-name tag)))]
      [text "None"]
      )
    )
  )