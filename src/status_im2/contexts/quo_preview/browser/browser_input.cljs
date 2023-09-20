(ns status-im2.contexts.quo-preview.browser.browser-input
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :favicon? :type :boolean}
   {:key :locked? :type :boolean}
   {:key :blur? :type :boolean}
   {:key :placeholder :type :text}
   {:key :disabled? :type :boolean}
   (preview/customization-color-option)])

(defn preview-browser-input
  []
  (let [state (reagent/atom {:blur?       false
                             :disabled?   false
                             :favicon?    false
                             :placeholder "Search or enter dapp domain"
                             :locked?     false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/browser-input
        (assoc @state
               :favicon
               (when (:favicon? @state) :i/verified))]])))
