(ns syng-im.components.discovery.discovery-popular-list-item
  (:require
    [syng-im.utils.logging :as log]
    [syng-im.components.react :refer [android?
                                      view
                                      text
                                      image]]
    [syng-im.resources :as res]
    [syng-im.components.discovery.styles :as st]
    [reagent.core :as r])
  )

(defn discovery-popular-list-item [discovery]
  (r/as-element [view {:style st/popular-list-item}
                 [view {:style st/popular-list-item-name-container}
                  [text {:style st/popular-list-item-name} (aget discovery "name")]
                  [text {:style st/popular-list-item-status
                         :numberOfLines 2} (aget discovery "status")]
                  ]
                 [view {:style st/popular-list-item-avatar-container}
                  [image {:style st/popular-list-item-avatar
                          :source res/user-no-photo}]
                  ]
                 ]))