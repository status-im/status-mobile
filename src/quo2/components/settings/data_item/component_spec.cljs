(ns quo2.components.settings.data-item.component-spec
  (:require [test-helpers.component :as h]
            [quo2.core :as quo]))

(h/describe
  "date item tests"
  (h/test "title is visible"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               true
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/get-by-text "Label")))

  (h/test "data item renders correctly if card? is false"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               false
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth nil}))

  (h/test "data item renders correctly if card? is true and size is small"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               true
                :label               :none
                :status              :default
                :size                :small
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth nil}))

  (h/test "data item renders correctly if card? is true"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               true
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth 1}))

  (h/test "subtitle is visible when status is not loading"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               true
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/get-by-text "Description")))

  (h/test "right icon is not visible when icon-right? is false"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         false
                :card?               true
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-falsy (h/query-by-label-text :icon-right)))

  (h/test "right icon is visible when icon-right? is true"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         true
                :card?               true
                :label               :none
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :icon-right)))

  (h/test "description icon is visible when description is icon"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :icon
                :icon-right?         true
                :card?               true
                :label               :preview
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :description-icon)))

  (h/test "description image is visible when description is network"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :network
                :icon-right?         true
                :card?               true
                :label               :preview
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :description-image)))

  (h/test "description emoji is visible when description is account"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :description         :account
                :icon-right?         true
                :card?               true
                :label               :preview
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Description"
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :account-emoji))))
