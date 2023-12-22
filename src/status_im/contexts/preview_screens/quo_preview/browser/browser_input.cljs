(ns status-im.contexts.preview-screens.quo-preview.browser.browser-input
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :favicon? :type :boolean}
   {:key :locked? :type :boolean}
   {:key :blur? :type :boolean}
   {:key :placeholder :type :text}
   {:key :disabled? :type :boolean}
   (preview/customization-color-option)])

(defn view
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
