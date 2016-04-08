(ns syng-im.components.discovery.discovery-popular

  (:require
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      image
                                      navigator
                                      toolbar-android]]
    [syng-im.components.carousel :refer [carousel]]
    [syng-im.components.discovery.discovery-popular-list :refer [discovery-popular-list]]
    [syng-im.models.discoveries :refer [generate-discoveries]]
    [syng-im.resources :as res]))

(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))


(defn discovery-popular [popular-discoveries]
  (let [popular-lists (mapv #(discovery-popular-list % (get popular-discoveries %)) (keys popular-discoveries))]
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