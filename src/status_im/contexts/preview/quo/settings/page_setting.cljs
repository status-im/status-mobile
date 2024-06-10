(ns status-im.contexts.preview.quo.settings.page-setting
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [(preview/customization-color-option {:feng-shui? true})
   {:key :setting-text :type :text}])

(defn view
  []
  (let [state (reagent/atom {:setting-text        "Sample text"
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/page-setting @state]])))
