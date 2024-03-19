(ns quo.components.profile.expanded-collectible.component-spec
  (:require
    [quo.components.profile.expanded-collectible.view :as expanded-collectible]
    [test-helpers.component :as h]))

(h/describe "Profile/ expanded collectible "
  (h/test "renders with counter and has on-press event"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider
       [expanded-collectible/view
        {:image-src
         "https://media.istockphoto.com/id/603164912/photo/suburb-asphalt-road-and-sun-flowers.jpg?s=612x612&w=0&k=20&c=qLoQ5QONJduHrQ0kJF3fvoofmGAFcrq6cL84HbzdLQM="
         :counter "1200"
         :on-press on-press}])
      (h/fire-event :press (h/get-by-label-text :expanded-collectible))
      (h/was-called on-press)
      (h/is-truthy (h/get-by-text "1200"))))

  (h/test "renders with status :cant-fetch and has on-press event"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider
       [expanded-collectible/view
        {:counter  "1200"
         :on-press on-press}])
      (h/fire-event :press (h/get-by-label-text :expanded-collectible))
      (h/was-called on-press)
      (h/is-truthy (h/get-by-translation-text :t/cant-fetch-info))))


  (h/test "renders with status :unsupported and has on-press event"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider
       [expanded-collectible/view
        {:counter  "1200"
         :status   :unsupported
         :on-press on-press}])
      (h/fire-event :press (h/get-by-label-text :expanded-collectible))
      (h/was-called on-press)
      (h/is-truthy (h/get-by-translation-text :t/unsupported-file)))))
