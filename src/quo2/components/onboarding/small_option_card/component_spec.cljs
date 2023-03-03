(ns quo2.components.onboarding.small-option-card.component-spec
  (:require [quo2.components.onboarding.small-option-card.view :as small-option-card]
            [status-im.react-native.resources :as resources]
            [test-helpers.component :as h]))

(defn- testing-small-option-card
  [variant
   {:keys [title subtitle image on-press]
    :or   {title "title" subtitle "subtitle"}}]
  [small-option-card/small-option-card
   {:variant  variant
    :title    title
    :subtitle subtitle
    :image    image
    :on-press on-press}])

(h/describe "small-option-card"
  (h/describe "Title & subtitle are rendered"
    (let [title           "A title"
          subtitle        "A subtitle"
          component-props {:title title :subtitle subtitle}]
      (h/test "`:main` variant"
        (h/render (testing-small-option-card :main component-props))
        (-> (h/get-by-text title) h/expect .toBeTruthy)
        (-> (h/get-by-text subtitle) h/expect .toBeTruthy))

      (h/test "`:icon` variant"
        (h/render (testing-small-option-card :icon component-props))
        (-> (h/get-by-text title) h/expect .toBeTruthy)
        (-> (h/get-by-text subtitle) h/expect .toBeTruthy))))

  (h/describe "Card pressed"
    (let [on-press-fn     (h/mock-fn)
          component-props {:on-press on-press-fn}]
      (h/test "`:main` variant"
        (h/render (testing-small-option-card :main component-props))
        (h/fire-event :press (h/get-by-label-text :small-option-card))
        (-> on-press-fn js/expect .toHaveBeenCalled))

      (h/test "`:icon` variant"
        (h/render (testing-small-option-card :icon component-props))
        (h/fire-event :press (h/get-by-label-text :small-option-card))
        (-> on-press-fn js/expect .toHaveBeenCalled))))

  (h/describe "Image rendered"
    (let [image           (resources/get-mock-image :small-opt-card-main)
          component-props {:image image}]
      (h/test "`:main` variant"
        (h/render (testing-small-option-card :main component-props))
        (-> (h/get-by-label-text :small-option-card-main-image) h/expect .-not .toBeNull))

      (h/test "`:icon` variant"
        (h/render (testing-small-option-card :icon component-props))
        (-> (h/get-by-label-text :small-option-card-icon-image) h/expect .-not .toBeNull)))))
