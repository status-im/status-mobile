(ns quo2.components.list-items.dapp.component-spec
  (:require [quo2.components.list-items.dapp.view :as dapp]
            [test-helpers.component :as h]))

(def props
  {:dapp                {:avatar nil
                         :name   "Coingecko"
                         :value  "coingecko.com"}
   :state               :default
   :action              :icon
   :blur?               false
   :customization-color :blue})

(h/describe
  "dApp component test"
  (h/test "default render"
    (h/render [dapp/view props])
    (h/is-truthy (h/get-by-text (:name (:dapp props))))
    (h/is-truthy (h/get-by-text (:value (:dapp props)))))
  (h/test "on-press event"
    (let [mock-fn (h/mock-fn)]
      (h/render [dapp/view (assoc props :on-press-icon mock-fn)])
      (h/fire-event :press (h/get-by-test-id "dapp-component-icon"))
      (h/was-called mock-fn))))
