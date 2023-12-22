(ns status-im.contexts.preview-screens.quo-preview.community.community-stat
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :value
    :type :number}
   {:key     :icon
    :type    :select
    :options [{:key :i/members}
              {:key :i/active-members}]}])

(defn view
  []
  (let [state (reagent/atom {:value 5000
                             :icon  :i/members})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/community-stat @state]])))
