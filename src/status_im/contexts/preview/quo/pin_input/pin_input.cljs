(ns status-im.contexts.preview.quo.pin-input.pin-input
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :blur? :type :boolean}
   {:type :number
    :key  :number-of-pins}
   {:type :number
    :key  :number-of-filled-pins}
   {:type :boolean
    :key  :error?}
   {:type :boolean
    :key  :info-error?}
   {:type :text
    :key  :info}])

(defn view
  []
  (let [[state set-state] (rn/use-state {:blur?                 false
                                         :number-of-pins        6
                                         :number-of-filled-pins 0})]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [rn/view {:style {:padding-vertical 40 :align-items :center :justify-content :center}}
      [quo/pin-input state]]]))
