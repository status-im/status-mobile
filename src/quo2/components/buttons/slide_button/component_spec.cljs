(ns quo2.components.buttons.slide-button.component-spec
  (:require [quo2.components.buttons.slide-button.view :as slide-button]
            [quo2.components.buttons.slide-button.constants :as constants]
            [quo2.components.buttons.slide-button.utils :as utils]
            [test-helpers.component :as h]))

(def ^:private default-props
  {:on-complete identity
   :track-text  :test-track-text
   :track-icon  :face-id})

(h/describe
  "slide-button"
  (h/test "render the correct text"
    (h/render [slide-button/view default-props])
    (h/is-truthy (h/get-by-text :test-track-text)))

  (h/test "render the disabled button"
    (h/render [slide-button/view (assoc default-props :disabled? true)])
    (let [mock (h/get-by-test-id :slide-button-track)]
      (h/has-style mock {:opacity constants/disable-opacity})))

  (h/test "render the small button"
    (h/render [slide-button/view (assoc default-props :size :small)])
    (let [mock         (h/get-by-test-id :slide-button-track)
          small-height (:track-height constants/small-dimensions)]
      (h/has-style mock {:height small-height})))

  (h/test "render with the correct customization-color"
    (h/render [slide-button/view (assoc default-props :customization-color :purple)])
    (let [mock         (h/get-by-test-id :slide-button-track)
          purple-color (utils/slider-color :track :purple)]
      (h/has-style mock {:backgroundColor purple-color}))))
