(ns status-im2.contexts.quo-preview.avatars.account-avatar
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]
    [utils.re-frame :as rf]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :watch-only}
              {:key :missing-keypair}]}
   {:key     :size
    :type    :select
    :options [{:key :size-16}
              {:key :size-20}
              {:key :size-24}
              {:key :size-28}
              {:key :size-32}
              {:key :size-48}
              {:key :size-64}
              {:key :size-80}]}
   {:key  "Emoji"
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:customization-color :purple
                             :size                :size-80
                             :emoji               "🍑"
                             :type                :default})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items     :center
                                    :justify-content :center}}
       [quo/account-avatar @state]
       [quo/button
        {:type            :grey
         :container-style {:margin-top 30}
         :on-press        #(rf/dispatch [:emoji-picker/open
                                         {:on-select (fn [emoji]
                                                       (swap! state assoc :emoji emoji))}])}
        "Open emoji picker"]])))
