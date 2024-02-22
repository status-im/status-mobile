(ns quo.components.tags.collectible-tag.component-spec
  (:require
    [quo.components.tags.collectible-tag.view :as collectible-tag]
    [test-helpers.component :as h]))

(def collectible-name "Collectible")
(def collectible-id "#123")

(defn get-test-data
  [{:keys [options blur? size]}]
  {:collectible-name    collectible-name
   :collectible-id      collectible-id
   :collectible-img-src {:uri "https://example.com/image.jpg"}
   :options             options
   :blur?               (or blur? false)
   :size                (or size :size-24)})

(h/describe "Collectible_tag tests"
  (h/test "Renders Default option"
    (let [data (get-test-data {})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders Add option"
    (let [data (get-test-data {:options :add})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders Hold option"
    (let [data (get-test-data {:options :hold})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Blur"
    (let [data (get-test-data {:blur? true})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders without Blur"
    (let [data (get-test-data {:blur? false})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Size 24"
    (let [data (get-test-data {:size :size-24})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Size 32"
    (let [data (get-test-data {:size :size-32})]
      (h/render-with-theme-provider [collectible-tag/view data])
      (h/is-truthy (h/get-by-text collectible-name)))))
