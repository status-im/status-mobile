(ns quo.components.profile.collectible-list-item.component-spec
  (:require
    [quo.components.profile.collectible-list-item.view :as collectible-list-item]
    [test-helpers.component :as h]))

(h/describe "Profile/ collectible list item tests"
  (h/describe "type card"
    (h/test "Renders default and on press fires"
      (let [on-press (h/mock-fn)]
        (h/render-with-theme-provider
         [collectible-list-item/view
          {:type     :card
           :on-press on-press}])
        (h/fire-event :press (h/get-by-label-text :collectible-list-item))
        (h/was-called on-press)))

    (h/test "Renders loading and on press fires"
      (let [on-press (h/mock-fn)]
        (h/render-with-theme-provider
         [collectible-list-item/view
          {:type     :card
           :status   :loading
           :on-press on-press}])
        (h/fire-event :press (h/get-by-label-text :collectible-list-item))
        (h/was-not-called on-press)
        (h/is-truthy (h/get-by-label-text :gradient-overlay))))

    (h/test "Renders counter"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type    :card
         :counter "x500"}])
      (h/is-truthy (h/get-by-text "x500")))

    (h/test "Renders counter and collectible name"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type             :card
         :collectible-name "Doodle #6822"
         :counter          "x500"}])
      (h/is-truthy (h/get-by-text "x500"))
      (h/is-truthy (h/get-by-text "Doodle #6822")))

    (h/test "Renders status cant-fetch"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type   :card
         :status :cant-fetch}])
      (h/is-truthy (h/get-by-translation-text :t/cant-fetch-info)))

    (h/test "Renders status unsupported"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type   :card
         :status :unsupported}])
      (h/is-truthy (h/get-by-translation-text :t/unsupported-file)))

    (h/test "Renders status unsupported and counter"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type    :card
         :status  :unsupported
         :counter "x500"}])
      (h/is-truthy (h/get-by-text "x500"))
      (h/is-truthy (h/get-by-translation-text :t/unsupported-file))))

  (h/describe "type image"
    (h/test "Renders default and on press fires"
      (let [on-press (h/mock-fn)]
        (h/render-with-theme-provider
         [collectible-list-item/view
          {:type     :image
           :on-press on-press}])
        (h/fire-event :press (h/get-by-label-text :collectible-list-item))
        (h/was-called on-press)))

    (h/test "Renders loading and on press fires"
      (let [on-press (h/mock-fn)]
        (h/render-with-theme-provider
         [collectible-list-item/view
          {:type     :image
           :status   :loading
           :on-press on-press}])
        (h/fire-event :press (h/get-by-label-text :collectible-list-item))
        (h/was-not-called on-press)
        (h/is-truthy (h/get-by-label-text :gradient-overlay))))

    (h/test "Renders counter"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type    :image
         :counter "x500"}])
      (h/is-truthy (h/get-by-text "x500")))

    (h/test "Renders status cant-fetch"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type   :image
         :status :cant-fetch}])
      (h/is-truthy (h/get-by-translation-text :t/cant-fetch-info)))

    (h/test "Renders status unsupported"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type   :image
         :status :unsupported}])
      (h/is-truthy (h/get-by-translation-text :t/unsupported-file)))

    (h/test "Renders status unsupported and counter"
      (h/render-with-theme-provider
       [collectible-list-item/view
        {:type    :image
         :status  :unsupported
         :counter "x500"}])
      (h/get-by-text "x500")
      (h/is-truthy (h/get-by-translation-text :t/unsupported-file)))))
