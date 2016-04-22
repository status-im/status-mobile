(ns syng-im.components.discovery.discovery-popular-list-item
  (:require
    [syng-im.utils.debug :refer [log]]
    [syng-im.components.react :refer [android?
                                      view
                                      text
                                      image]]
    [syng-im.resources :as res]
    [reagent.core :as r])
  )

(defn discovery-popular-list-item [discovery]
  (let [_ (log discovery)]
    (r/as-element [view {:style {:flexDirection "row"
                               :paddingTop 10
                               :paddingBottom 10}}
                 [view {:style {:flex 0.8
                                :flexDirection "column"}}
                  [text {:style {:color "black"
                                 :fontWeight "bold"
                                 :fontSize 12}} (aget discovery "name")]
                  [text {:style {:color "black"
                                 :fontWeight "normal"
                                 :fontSize 12}
                         :numberOfLines 2} (aget discovery "status")]
                  ]
                 [view {:style {:flex 0.2
                                :flexDirection "column"
                                :alignItems "center"
                                :paddingTop 5}}
                  [image {:style {:resizeMode "contain"
                                  :borderRadius 150
                                  :width 30
                                  :height 30}
                          :source res/user-no-photo}]
                  ]
                 ])
  ))