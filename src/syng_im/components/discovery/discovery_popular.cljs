(ns syng-im.components.discovery.discovery-popular
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.utils.debug :refer [log]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      image
                                      navigator
                                      toolbar-android]]
    [syng-im.components.carousel :refer [carousel]]
    [syng-im.components.discovery.discovery-popular-list :refer [discovery-popular-list]]
    [syng-im.models.discoveries :refer [generate-discoveries
                                        generate-discovery
                                        save-discoveries
                                        group-by-tag
                                        get-discovery-recent]]
    [syng-im.resources :as res]))

(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(defn discovery-popular []
  (let [popular-tags (subscribe [:get-popular-tags 3])
        _ (log "Got popular tags: ")
        _ (log @popular-tags)
        popular-lists (mapv #(discovery-popular-list (.-name %)) @popular-tags)]
    (if (> (count popular-lists) 0)
      (apply carousel {:pageStyle {:borderRadius  1
                                   :shadowColor   "black"
                                   :shadowRadius  1
                                   :shadowOpacity 0.8
                                   :elevation     2
                                   :marginBottom  10}
                       :pageWidth (- (page-width) 60)

                       }
             popular-lists
             )
      [text "None"]
      )
    )
  )

(comment
  (set! React (js/require "react-native"))
  (.get (.Dimensions React) "window")
  )