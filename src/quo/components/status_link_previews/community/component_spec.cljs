(ns quo.components.status-link-previews.community.component-spec
  (:require
    [quo.components.status-link-previews.community.view :as view]
    [test-helpers.component :as h]))

(def props
  {:title                "Some title"
   :description          "Some description"
   :link                 "status.im"
   :logo                 "data:image/png,logo-x"
   :thumbnail            "data:image/png,whatever"
   :member-count         20
   :active-members-count 20})

(h/describe "Status link previews - Community"
  (h/test "default render"
    (h/render [view/view])
    (h/is-truthy (h/query-by-label-text :link-preview))
    (h/is-null (h/query-by-label-text :button-enable-preview)))

  (h/test "renders with most common props"
    (h/render [view/view props])
    (h/is-truthy (h/query-by-text (:title props)))
    (h/is-truthy (h/query-by-text (:description props)))
    (h/is-truthy (h/query-by-text (:link props)))
    (h/is-truthy (h/query-by-label-text :logo))
    (h/is-truthy (h/query-by-label-text :thumbnail)))

  (h/test "does not render thumbnail if prop is not present"
    (h/render [view/view (dissoc props :thumbnail)])
    (h/is-null (h/query-by-label-text :thumbnail)))

  (h/test "does not render logo if prop is not present"
    (h/render [view/view (dissoc props :logo)])
    (h/is-null (h/query-by-label-text :logo))))
