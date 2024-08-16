(ns quo.components.list-items.approval-info.component-spec
  (:require [quo.components.list-items.approval-info.view :as approval-info]
            [test-helpers.component :as h]))

(h/describe "List Items: Approval Info"
  (h/test "should render correctly with basic props"
    (h/render-with-theme-provider
     [approval-info/view
      {:type         :spending-cap
       :label        "Spending Cap"
       :avatar-props {:image "image"}}])
    (h/is-truthy (h/get-by-label-text :approval-info)))

  (h/test "should render correctly with label & description"
    (h/render-with-theme-provider
     [approval-info/view
      {:type         :spending-cap
       :label        "Spending Cap"
       :description  "Description"
       :avatar-props {:image "image"}}])
    (h/is-truthy (h/get-by-text "Spending Cap"))
    (h/is-truthy (h/get-by-text "Description")))

  (h/test "on-button-press event is called when button is pressed"
    (let [on-button-press (h/mock-fn)]
      (h/render-with-theme-provider
       [approval-info/view
        {:type            :spending-cap
         :label           "Spending Cap"
         :button-label    "Edit"
         :button-icon     :i/options
         :on-button-press on-button-press
         :avatar-props    {:image "image"}}])
      (h/fire-event :press (h/get-by-text "Edit"))
      (h/was-called on-button-press)))

  (h/test "on-option-press event is called when option icon is pressed"
    (let [on-option-press (h/mock-fn)]
      (h/render-with-theme-provider
       [approval-info/view
        {:type            :spending-cap
         :label           "Spending Cap"
         :option-icon     :i/options
         :on-option-press on-option-press
         :avatar-props    {:image "image"}}])
      (h/fire-event :press (h/get-by-label-text :icon))
      (h/was-called on-option-press)))

  (h/test "on-avatar-press event is called when avatar is pressed"
    (let [on-avatar-press (h/mock-fn)]
      (h/render-with-theme-provider
       [approval-info/view
        {:type            :spending-cap
         :label           "Spending Cap"
         :on-avatar-press on-avatar-press
         :avatar-props    {:image "image"}}])
      (h/fire-event :press (h/get-by-label-text :token-avatar))
      (h/was-called on-avatar-press))))
