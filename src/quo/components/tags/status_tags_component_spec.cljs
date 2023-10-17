(ns quo.components.tags.status-tags-component-spec
  (:require
    [quo.components.tags.status-tags :as quo]
    [test-helpers.component :as h]))

(defn render-status-tag
  [opts]
  (h/render [quo/status-tag opts]))

(h/describe "status tag component"
  (h/test "renders status tag with positive type"
    (render-status-tag {:status {:type :positive}
                        :label  "Positive"
                        :size   :small})
    (-> (h/expect (h/get-all-by-label-text :status-tag-positive))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Positive"))
        (.toBeTruthy)))
  (h/test "renders status tag with negative type"
    (render-status-tag {:status {:type :negative}
                        :label  "Negative"
                        :size   :small})
    (-> (h/expect (h/get-all-by-label-text :status-tag-negative))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Negative"))
        (.toBeTruthy)))
  (h/test "renders status tag with pending type"
    (render-status-tag {:status {:type :pending}
                        :label  "Pending"
                        :size   :small})
    (-> (h/expect (h/get-all-by-label-text :status-tag-pending))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-text "Pending"))
        (.toBeTruthy))))
