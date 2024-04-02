(ns quo.components.profile.collectible-list-item.component-spec
  (:require
    [quo.components.profile.collectible-list-item.view :as collectible-list-item]
    [test-helpers.component :as h]))

(h/describe "Profile/ collectible list item tests"
  (h/describe "type card"
    ;; (h/test "Renders default and on press fires"
    ;;   (let [on-press (h/mock-fn)]
    ;;     (h/render-with-theme-provider
    ;;      [collectible-list-item/view
    ;;       {:type     :card
    ;;        :image-src "https://media.istockphoto.com/id/603164912/photo/suburb-asphalt-road-and-sun-flowers.jpg?s=612x612&w=0&k=20&c=qLoQ5QONJduHrQ0kJF3fvoofmGAFcrq6cL84HbzdLQM="
    ;;        :on-press on-press
    ;;        :collectible-mime "image/png"}])
    ;;     (h/fire-event :press (h/get-by-label-text :collectible-list-item))
    ;;     (h/was-called on-press)))

    (h/test "Renders counter"
            (h/render-with-theme-provider
             [collectible-list-item/view
              {:type    :card
               :image-src "https://upload.wikimedia.org/wikipedia/commons/3/38/JPEG_example_JPG_RIP_001.jpg"
               :counter "x500"
               :collectible-mime "image/png"}])
            (-> (h/wait-for #(h/get-by-label-text :collectible-counter))
                (.then #(h/is-truthy (h/get-by-text "x500")))))

    ;; (h/test "Renders counter and collectible name"
    ;;   (h/render-with-theme-provider
    ;;    [collectible-list-item/view
    ;;     {:type             :card
    ;;      :collectible-name "Doodle #6822"
    ;;      :image-src "https://media.istockphoto.com/id/603164912/photo/suburb-asphalt-road-and-sun-flowers.jpg?s=612x612&w=0&k=20&c=qLoQ5QONJduHrQ0kJF3fvoofmGAFcrq6cL84HbzdLQM="
    ;;      :counter          "x500"
    ;;      :collectible-mime "image/png"}])
    ;;   (h/wait-for 
    ;;    (h/is-truthy (h/get-by-text "x500"))
    ;;    (h/is-truthy (h/get-by-text "Doodle #6822"))))
    )

  )
