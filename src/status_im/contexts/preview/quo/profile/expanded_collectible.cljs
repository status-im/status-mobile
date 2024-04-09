(ns status-im.contexts.preview.quo.profile.expanded-collectible
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(defonce vertical-image
  "https://images.unsplash.com/photo-1526512340740-9217d0159da9?q=80&w=1000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8dmVydGljYWx8ZW58MHx8MHx8fDA%3D")
(defonce horizontal-image
  "https://media.istockphoto.com/id/603164912/photo/suburb-asphalt-road-and-sun-flowers.jpg?s=612x612&w=0&k=20&c=qLoQ5QONJduHrQ0kJF3fvoofmGAFcrq6cL84HbzdLQM=")

(def descriptor
  [{:key  :square?
    :type :boolean}
   {:key  :counter
    :type :text}
   {:type    :select
    :key     :image-type
    :options [{:key :vertical}
              {:key :horizontal}]}])

(defn view
  []
  (let [state (reagent/atom {:square?    false
                             :counter    ""
                             :image-type :horizontal})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical  20
                                    :margin-horizontal 35}}
       [quo/expanded-collectible
        (assoc (dissoc @state :image-type)
               :image-src       (if (= :vertical (:image-type @state))
                                  vertical-image
                                  horizontal-image)
               :counter         (when (seq (:counter @state)) (:counter @state))
               :supported-file? true
               :on-press        #(js/alert "Pressed"))]])))
