(ns quo.components.settings.data-item.component-spec
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [test-helpers.component :as h]))

(h/describe
  "date item tests"
  (h/test "title is visible"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/get-by-text "Label")))

  (h/test "data item renders correctly if card? is false"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               false
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth nil}))

  (h/test "data item renders correctly if card? is true and size is small"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :small
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth nil}))

  (h/test "data item renders correctly if card? is true"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/has-style (h/query-by-label-text :data-item)
                 {:borderWidth 1}))

  (h/test "subtitle is visible when status is not loading"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/get-by-text "Subtitle")))

  (h/test "right icon is not visible when icon-right? is false"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-falsy (h/query-by-label-text :icon-right)))

  (h/test "right icon is visible when icon-right? is true"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :right-icon          :i/chevron-right
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :icon-right)))

  (h/test "icon is visible when subtitle type is icon"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :icon
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :subtitle-type-icon)))

  (h/test "image is visible when subtitle type is network"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :network
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow
                :network-image       (quo.resources/get-network :ethereum)}])
    (h/is-truthy (h/query-by-label-text :subtitle-type-image)))

  (h/test "emoji is visible when subtitle type is account"
    (h/render [quo/data-item
               {:on-press            (h/mock-fn)
                :blur?               false
                :card?               true
                :status              :default
                :size                :default
                :title               "Label"
                :subtitle            "Subtitle"
                :subtitle-type       :account
                :icon                :i/placeholder
                :emoji               "🎮"
                :customization-color :yellow}])
    (h/is-truthy (h/query-by-label-text :account-emoji))))
