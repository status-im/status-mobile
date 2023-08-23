(ns status-im2.contexts.quo-preview.messages.gap
  (:require [quo2.components.messages.gap :as gap]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Timestamp Far"
    :key   :timestamp-far
    :type  :text}
   {:label "Timestamp Near"
    :key   :timestamp-near
    :type  :text}])

(defn preview
  []
  (let [state (reagent/atom {:timestamp-far          "Jan 8 · 09:12"
                             :timestamp-near         "Mar 8 · 22:42"
                             :on-info-button-pressed identity
                             :on-press               #(println "fill gaps")
                             :warning-label          (i18n/label :messages-gap-warning)})]
    (fn []
      [rn/view {:margin-bottom 50}
       [rn/view {:padding 16}
        [preview/customizer state descriptor]]
       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [gap/gap @state]]])))

(defn preview-messages-gap
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex   1
     :header [preview]
     :key-fn str}]])
