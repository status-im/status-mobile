(ns status-im2.contexts.quo-preview.profile.link-card
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :link
    :type    :select
    :options [{:key :link}
              {:key :faceook}
              {:key :github}
              {:key :instagram}
              {:key :lens}
              {:key :linkedin}
              {:key :mirror}
              {:key :opensea}
              {:key :pinterest}
              {:key :rarible}
              {:key :snapchat}
              {:key :spotify}
              {:key :superrare}
              {:key :tumblr}
              {:key :twitch}
              {:key :twitter}
              {:key :youtube}]}
   {:key  :address
    :type :text}
   (preview/customization-color-option {:set-type :socials})])

(defn view
  []
  (let [state (reagent/atom {:link     :link
                             :address  "address"
                             :customization-color :link
                             :on-press #(js/alert "pressed")})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-bottom 50}}
       [quo/link-card @state]])))
