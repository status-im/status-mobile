(ns status-im2.contexts.quo-preview.avatars.account-avatar
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :watch-only}]}
   {:key     :size
    :type    :select
    :options [{:key   16
               :value "16"}
              {:key   20
               :value "20"}
              {:key   24
               :value "24"}
              {:key   28
               :value "28"}
              {:key   32
               :value "32"}
              {:key   48
               :value "48"}
              {:key   80
               :value "80"}]}
   {:label "Emoji"
    :key   :emoji
    :type  :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:customization-color :purple
                             :size                80
                             :emoji               "🍑"
                             :type                :default})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/account-avatar @state]])))
