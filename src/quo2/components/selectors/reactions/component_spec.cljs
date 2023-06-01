(ns quo2.components.selectors.reactions.component-spec
  (:require [quo2.components.selectors.reactions.view :as view]
            [test-helpers.component :as h]))

(h/describe "Selectors > Reactions"
  (h/test "renders component"
    (h/render [view/view :reaction/sad])
    (h/is-truthy (h/get-by-label-text :reaction)))

  (h/describe "on-press event"
    (h/test "starts with released state"
      (let [on-press (h/mock-fn)]
        (h/render [view/view :reaction/love {:on-press on-press}])
        (h/fire-event :press (h/get-by-label-text :reaction))
        (h/was-called on-press)))

    (h/test "starts with pressed state"
      (let [on-press (h/mock-fn)]
        (h/render [view/view :reaction/love
                   {:on-press       on-press
                    :start-pressed? true}])
        (h/fire-event :press (h/get-by-label-text :reaction))
        (h/was-called on-press)))))
