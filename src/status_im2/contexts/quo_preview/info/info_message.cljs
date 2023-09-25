(ns status-im2.contexts.quo-preview.info.info-message
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :success}
              {:key :error}]}
   {:key     :size
    :type    :select
    :options [{:key :default}
              {:key :tiny}]}
   {:key  :message
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:type    :default
                             :size    :default
                             :icon    :i/placeholder
                             :message "This is a message"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/info-message @state (:message @state)]])))
