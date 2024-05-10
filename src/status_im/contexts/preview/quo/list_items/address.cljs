(ns status-im.contexts.preview.quo.list-items.address
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :active-state? :type :boolean}
   {:key :show-alert-on-press? :type :boolean}
   {:key :blur? :type :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:address  "0x0ah...78b"
                             :networks [{:network-name :ethereum :short-name "eth"}
                                        {:network-name :optimism :short-name "oeth"}]})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/address
        (merge @state
               (when (:show-alert-on-press? @state)
                 {:on-press #(js/alert "Pressed!")}))]])))
