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

(h/describe "Links - Link Preview"
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
    (h/is-truthy (h/query-by-label-text :thumbnail))
    (h/is-truthy (h/query-by-label-text :thumbnail)))

  (h/test "does not render thumbnail if prop is not present"
    (h/render [view/view (dissoc props :thumbnail)])
    (h/is-null (h/query-by-label-text :thumbnail)))

  (h/test "does not render logo if prop is not present"
    (h/render [view/view (dissoc props :logo)])
    (h/is-null (h/query-by-label-text :logo)))

  (h/test "shows button to enable preview when preview is disabled"
    (h/render [view/view
               (assoc props
                      :enabled?      false
                      :disabled-text "I'm disabled")])
    (h/is-truthy (h/query-by-label-text :button-enable-preview))
    (h/is-truthy (h/query-by-text "I'm disabled")))

  (h/test "on-enable event"
    (let [on-enable (h/mock-fn)]
      (h/render [view/view
                 (assoc props
                        :enabled?  false
                        :on-enable on-enable)])
      (h/fire-event :press (h/get-by-label-text :button-enable-preview))
      (h/was-called on-enable))))
