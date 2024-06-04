(ns quo.components.text-combinations.standard-title.component-spec
  (:require [quo.components.markdown.text :as text]
            [quo.components.text-combinations.standard-title.view :as standard-title]
            [test-helpers.component :as h]))

(h/describe "Text combinations - Standard title"
  (h/test "Default render"
    (h/render [standard-title/view {:title "This is a title"}])
    (h/is-truthy (h/get-by-text "This is a title")))

  (h/test "Counter variant"
    (h/render [standard-title/view
               {:title         "This is a title"
                :right         :counter
                :counter-left  50
                :counter-right 100}])
    (h/is-truthy (h/get-by-text "50/100")))

  (h/describe "Action variant"
    (h/test "Default render"
      (let [on-press-fn (h/mock-fn)]
        (h/render [standard-title/view
                   {:title    "This is a title"
                    :right    :action
                    :on-press on-press-fn}])
        (h/is-truthy (h/get-by-text "This is a title"))))

    (h/test "Action fired"
      (let [on-press-fn (h/mock-fn)]
        (h/render [standard-title/view
                   {:title    "This is a title"
                    :right    :action
                    :on-press on-press-fn}])

        (h/fire-event :on-press (h/get-by-label-text :standard-title-action))
        (h/was-called-times on-press-fn 1))))

  (h/describe "Tag variant"
    (h/test "Default render"
      (h/render [standard-title/view
                 {:title "This is a title"
                  :right :tag}])
      (h/is-truthy (h/get-by-text "This is a title")))

    (h/test "Tag callback fired"
      (let [on-press-fn (h/mock-fn)]
        (h/render [standard-title/view
                   {:title    "This is a title"
                    :right    :tag
                    :on-press on-press-fn}])
        (h/fire-event :on-press (h/get-by-label-text :standard-title-tag))
        (h/was-called-times on-press-fn 1))))

  (h/describe "Custom content variant"
    (h/test "Default render"
      (h/render [standard-title/view
                 {:title "This is a title"
                  :right [text/text "Right"]}])
      (h/is-truthy (h/get-by-text "This is a title"))
      (h/is-truthy (h/get-by-text "Right")))))
