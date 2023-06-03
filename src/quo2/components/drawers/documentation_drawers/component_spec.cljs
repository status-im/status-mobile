(ns quo2.components.drawers.documentation-drawers.component-spec
  (:require [quo2.components.drawers.documentation-drawers.view :as documentation-drawers]
            [test-helpers.component :as h]))

(h/describe "Documentation drawers component"
  (h/test "render component without button"
    (h/render [documentation-drawers/view {:title "Documentation"} "Content"])
    (-> (h/expect (h/get-by-label-text :documentation-drawer-title))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-label-text :documentation-drawer-content))
        (.toBeTruthy)))
  (h/test "render component with button"
    (h/render [documentation-drawers/view
               {:title "Documentation" :show-button? true :button-label "Read more"} "Content"])
    (-> (h/expect (h/get-by-label-text :documentation-drawer-title))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-label-text :documentation-drawer-content))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-label-text :documentation-drawer-button))
        (.toBeTruthy)))
  (h/test "button is pressed"
    (let [event (h/mock-fn)]
      (h/render
       [documentation-drawers/view
        {:title "Documentation" :show-button? true :button-label "Read more" :on-press-button event}
        "Content"])
      (h/fire-event :press (h/get-by-label-text :documentation-drawer-button))
      (-> (h/expect event)
          (.toHaveBeenCalled)))))

