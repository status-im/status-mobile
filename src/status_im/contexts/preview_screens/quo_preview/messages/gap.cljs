(ns status-im.contexts.preview-screens.quo-preview.messages.gap
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
    [utils.i18n :as i18n]))

(def descriptor
  [{:key :timestamp-far :type :text}
   {:key :timestamp-near :type :text}])

(defn view
  []
  (let [state (reagent/atom {:timestamp-far          "Jan 8 · 09:12"
                             :timestamp-near         "Mar 8 · 22:42"
                             :on-info-button-pressed identity
                             :on-press               #(println "fill gaps")
                             :warning-label          (i18n/label :t/messages-gap-warning)})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [quo/gap @state]])))
