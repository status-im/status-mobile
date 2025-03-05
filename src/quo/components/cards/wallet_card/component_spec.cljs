(ns quo.components.cards.wallet-card.component-spec
  (:require
    [quo.components.cards.wallet-card.view :as wallet-card]
    [quo.foundations.resources :as resources]
    [test-helpers.component :as h]))

(def ^:private base-props
  {:image    (resources/get-image :keycard-logo)
   :title    "Buy"
   :subtitle "Start investing now"})

(h/describe "cards: wallet card"
  (h/test "Test default render"
    (let [event (h/mock-fn)]
      (h/render-with-theme-provider [wallet-card/view
                                     (assoc base-props
                                            :on-press
                                            event)])
      (h/is-truthy (h/get-by-label-text :wallet-card))
      (h/is-truthy (h/get-by-text "Buy"))
      (h/is-truthy (h/get-by-text "Start investing now"))
      (h/is-truthy (h/get-by-label-text :image))
      (h/fire-event :press (h/get-by-label-text :wallet-card))
      (h/was-called event)))

  (h/test "Test render with dismissible prop"
    (let [event (h/mock-fn)]
      (h/render-with-theme-provider [wallet-card/view
                                     (assoc base-props
                                            :dismissible?   true
                                            :on-press-close event)])
      (h/is-truthy (h/get-by-label-text :close-icon))
      (h/fire-event :press (h/get-by-label-text :icon-container))
      (h/was-called event))))
