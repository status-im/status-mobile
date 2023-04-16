(ns quo2.components.links.url-preview-list.component-spec
  (:require
    [oops.core :as oops]
    [quo2.components.links.url-preview-list.view :as view]
    [test-helpers.component :as h]))

(def previews
  (->> (range 3)
       (map inc)
       (mapv (fn [index]
               {:title    (str "Title " index)
                :body     (str "status.im." index)
                :loading? false
                :url      (str "status.im." index)}))))

(h/describe "Links - URL Preview List"
  (h/test "default render"
    (h/render [view/view
               {:data               previews
                :key-fn             :url
                :horizontal-spacing 10}])
    (-> (count (h/query-all-by-label-text :url-preview))
        (h/expect)
        (.toEqual 3))

    (-> (map #(oops/oget % "props.children")
             (h/query-all-by-label-text :title))
        (clj->js)
        (h/expect)
        (.toStrictEqual #js ["Title 1" "Title 2" "Title 3"])))

  (h/test "on-clear event is individually handled by each preview"
    (let [on-clear (h/mock-fn)]
      (h/render [view/view
                 {:data     previews
                  :key-fn   :url
                  :on-clear on-clear}])
      (h/fire-event :press (first (h/get-all-by-label-text :button-clear-preview)))
      (h/fire-event :press (second (h/get-all-by-label-text :button-clear-preview)))
      (h/was-called-times on-clear 2)))

  (h/test "previews have separate loading states"
    (h/render [view/view
               {:data   (assoc-in previews [1 :loading?] true)
                :key-fn :url}])
    (h/is-truthy (h/get-by-label-text :url-preview-loading))
    (-> (count (h/query-all-by-label-text :url-preview))
        (h/expect)
        (.toEqual 2))))
