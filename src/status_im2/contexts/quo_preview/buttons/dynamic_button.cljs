(ns status-im2.contexts.quo-preview.buttons.dynamic-button
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.i18n :as i18n]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :jump-to}
              {:key :mention}
              {:key :notification-down}
              {:key :notification-up}
              {:key :search}
              {:key :search-with-label}
              {:key :scroll-to-bottom}]}
   {:key  :count
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:count  "5"
                             :type   :jump-to
                             :labels {:jump-to           (i18n/label :t/jump-to)
                                      :search-with-label (i18n/label :t/back)}})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [quo/dynamic-button @state]])))
