(ns quo2.components.links.url-preview.component-spec
  (:require
    [quo2.components.links.url-preview.view :as view]
    [test-helpers.component :as h]))

(h/describe "Links - URL Preview"
  (h/test "default render"
    (h/render [view/view])
    (h/is-truthy (h/query-by-label-text :title))
    (h/is-truthy (h/query-by-label-text :logo))
    (h/is-truthy (h/query-by-label-text :button-clear-preview))
    (h/is-null (h/query-by-label-text :url-preview-loading)))

  (h/test "on-clear event"
    (let [on-clear (h/mock-fn)]
      (h/render [view/view {:on-clear on-clear}])
      (h/fire-event :press (h/get-by-label-text :button-clear-preview))
      (h/was-called on-clear)))

  (h/describe "loading state"
    (h/test "shows a loading container"
      (h/render [view/view {:loading? true :loading-message "Hello"}])
      (h/is-null (h/query-by-label-text :title))
      (h/is-truthy (h/query-by-label-text :url-preview-loading)))

    (h/test "renders if `loading-message` is not passed"
      (h/render [view/view {:loading? true}])
      (h/is-null (h/query-by-label-text :title))
      (h/is-truthy (h/query-by-label-text :url-preview-loading)))))
