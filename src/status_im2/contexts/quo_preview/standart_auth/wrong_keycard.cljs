(ns status-im2.contexts.quo-preview.standart-auth.wrong-keycard
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]
            [status-im2.common.standard-auth.keycard.view :as standard-auth]))

(def descriptor
  [{:key     :theme
    :type    :select
    :options [{:key :dark}
              {:key :light}
              {:key nil :value "System"}]}])

(defn preview
  []
  (let [state (reagent/atom {})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/button
        {:container-style {:margin-horizontal 40}
         :on-press        #(rf/dispatch [:show-bottom-sheet
                                         {:content standard-auth/wrong-keycard-sheet}])}
        "See in bottom sheet"]])))
