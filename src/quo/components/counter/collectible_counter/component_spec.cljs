(ns quo.components.counter.collectible-counter.component-spec
  (:require
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [test-helpers.component :as h]))

(defn render
  [component]
  (h/render-with-theme-provider component :dark))

(h/describe "collectible counter component"
  (h/test "default render of component"
    (render [collectible-counter/view {}])
    (-> (h/expect (h/get-by-label-text :collectible-counter))
        (h/is-truthy)))

  (h/test "render with a string value"
    (render [collectible-counter/view {:value "x500"}])
    (-> (h/expect (h/get-by-text "x500"))
        (h/is-truthy)))

  (h/test "render with an integer value and default status"
    (render [collectible-counter/view
             {:value  100
              :status :default}])
    (-> (h/expect (h/get-by-text "100"))
        (h/is-truthy)))

  (h/test "render with all availiable props"
    (render [collectible-counter/view
             {:value  "x100"
              :status :error
              :size   :size-24}])
    (-> (h/expect (h/get-by-text "x100"))
        (h/is-truthy))))
