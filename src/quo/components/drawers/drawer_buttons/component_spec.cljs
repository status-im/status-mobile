(ns quo.components.drawers.drawer-buttons.component-spec
  (:require
    [quo.components.drawers.drawer-buttons.view :as drawer-buttons]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [test-helpers.component :as h]))

(h/describe "drawer-buttons"
  (h/setup-fake-timers)

  (h/test "the top heading and subheading render"
    (h/render [drawer-buttons/view
               {:top-card    {:heading :top-heading}
                :bottom-card {:heading :bottom-heading}}
               :top-sub-heading
               :bottom-sub-heading])
    (-> (js/expect (h/get-by-text "top-heading"))
        (.toBeTruthy))
    (-> (js/expect (h/get-by-text "top-sub-heading"))
        (.toBeTruthy)))

  (h/test "the bottom heading and subheading render"
    (h/render [drawer-buttons/view
               {:top-card    {:heading :top-heading}
                :bottom-card {:heading :bottom-heading}}
               :top-sub-heading
               :bottom-sub-heading])
    (-> (js/expect (h/get-by-text "bottom-heading"))
        (.toBeTruthy))
    (-> (js/expect (h/get-by-text "bottom-sub-heading"))
        (.toBeTruthy)))

  (h/test "it clicks the top card"
    (let [event (h/mock-fn)]
      (with-redefs [safe-area/get-top (fn [] 10)]
        (h/render [drawer-buttons/view
                   {:top-card    {:on-press event
                                  :heading  :top-heading}
                    :bottom-card {:heading :bottom-heading}}
                   :top-sub-heading
                   :bottom-sub-heading])
        (h/fire-event :press (h/get-by-text "top-heading"))
        (-> (js/expect event)
            (.toHaveBeenCalled)))))

  (h/test "it clicks the bottom card"
    (let [event (h/mock-fn)]
      (h/render [drawer-buttons/view
                 {:top-card    {:heading :top-heading}
                  :bottom-card {:on-press event
                                :heading  :bottom-heading}}
                 :top-sub-heading
                 :bottom-sub-heading])
      (h/fire-event :press (h/get-by-text "bottom-heading"))
      (-> (js/expect event)
          (.toHaveBeenCalled))))

  (h/test "the top card child renders with a render prop"
    (h/render [drawer-buttons/view
               {:top-card    {:heading :top-heading}
                :bottom-card {:heading :bottom-heading}}
               [rn/text :top-render-fn]
               :bottom-sub-heading])
    (-> (js/expect (h/get-by-text "top-render-fn"))
        (.toBeTruthy)))

  (h/test "the bottom card child renders with a render prop"
    (h/render [drawer-buttons/view
               {:top-card    {:heading :top-heading}
                :bottom-card {:heading :bottom-heading}}
               :top-sub-heading
               [rn/text :bottom-render-fn]])
    (-> (js/expect (h/get-by-text "bottom-render-fn"))
        (.toBeTruthy))))
