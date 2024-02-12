(ns status-im.contexts.preview.quo.links.internal-link-card
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :message}
              {:key :full-page}]}
   {:type :text
    :key  :title}
   {:type :text
    :key  :subtitle}
   {:type :text
    :key  :description}
   {:type :text
    :key  :emoji-hash}
   {:type :boolean
    :key  :loading?}
   {:type :number
    :key  :members-count}
   {:type :number
    :key  :active-members-count}
   {:key     :type
    :type    :select
    :options [{:key :community}
              {:key :channel}
              {:key :user}]}
   {:key     :banner
    :type    :select
    :options (mapv (fn [k] {:key k})
                   (keys resources/mock-images))}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom
               {:title                "Doodles"
                :description          "Coloring the world with joy • ᴗ •"
                :members-count        24
                :emoji-hash           "🌟🚀🐠🌈🏰🔮🦉🐼🍉🎨🚲🌙🍔🌵"
                :active-members-count 12
                :loading?             false
                :customization-color  :purple
                :banner               :light-blur-background
                :type                 :user
                :subtitle             "Web 3.0 Designer @ethstatus - DJ, Producer - Dad - YouTuber"})]
    (fn []
      (let [banner (get resources/mock-images (:banner @state) :bored-ape)]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [quo/internal-link-card
          (assoc @state
                 :banner   banner
                 :icon     (resources/get-mock-image :status-logo)
                 :on-press #(js/alert "You clicked me!"))]]))))
