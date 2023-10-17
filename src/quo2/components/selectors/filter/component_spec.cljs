(ns quo2.components.selectors.filter.component-spec
  (:require
    [quo2.components.selectors.filter.view :as quo]
    [test-helpers.component :as h]))

(h/describe "selector filter component"
  (h/test "renders component"
    (h/render [quo/view])
    (-> (js/expect (h/get-by-label-text :selector-filter))
        (.toBeTruthy)))

  (h/test "calls custom event handler when on-press-out is triggered"
    (let [on-press-out-mock (js/jest.fn)]
      (h/render [quo/view {:on-press-out on-press-out-mock}])
      (h/fire-event :press-out (h/get-by-label-text :selector-filter))
      (-> (js/expect on-press-out-mock)
          (.toHaveBeenCalledTimes 1)))))
