(ns quo.components.tags.collectible-tag.component-spec
  (:require
    [quo.components.tags.collectible-tag.view :as collectible-tag]
    [test-helpers.component :as h]))

(def collectible-name "Collectible")
(def collectible-id "#123")
(def ^:private theme :light)

(defn get-test-data
  [{:keys [options blur?]}]
  {:collectible-name    collectible-name
   :collectible-id      collectible-id
   :collectible-img-src 1055
   :container-width     139.33
   :options             options
   :blur?               (or blur? false)
   :theme               theme})

(h/describe "Collectible_tag tests"
  (h/test "Renders Default option"
    (let [data (get-test-data {:options false})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders Add option"
    (let [data (get-test-data {:options :add})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders Hold option"
    (let [data (get-test-data {:options :hold})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Blur"
    (let [data (get-test-data {:blur? true})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders without Blur"
    (let [data (get-test-data {:blur? false})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Size 24"
    (let [data (get-test-data {:size :size-24})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with Size 32"
    (let [data (get-test-data {:size :size-32})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "On-layout fires correctly"
    (let [on-layout (h/mock-fn)
          data      (get-test-data {:on-layout on-layout})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      ;; Simulate a layout change
      (on-layout {:nativeEvent {:layout {:x 0 :y 0 :width 100 :height 100}}})
      (h/was-called on-layout)))

  (h/test "Renders with :collectible-img-src as int"
    (let [data (get-test-data {:collectible-img-src 1055})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with :collectible-img-src as string"
    (let [data (get-test-data {:collectible-img-src "1055"})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with :container-width as int"
    (let [data (get-test-data {:container-width 100})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with :container-width as double"
    (let [data (get-test-data {:container-width 100.5})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name))))

  (h/test "Renders with :container-width as string"
    (let [data (get-test-data {:container-width "100"})]
      (h/render-with-theme-provider [collectible-tag/view data] theme)
      (h/is-truthy (h/get-by-text collectible-name)))))
