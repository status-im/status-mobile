(ns status-im2.contexts.quo-preview.links.link-preview
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :text :key :title}
   {:type :text :key :description}
   {:type :text :key :link}
   {:type :number :key :width}
   {:type :boolean :key :with-logo?}
   {:type :boolean :key :with-description?}
   {:type :boolean :key :with-thumbnail?}
   {:type :text :key :disabled-text}
   {:type :boolean :key :enabled?}
   {:key     :thumbnail
    :type    :select
    :options (mapv (fn [k] {:key k})
                   (keys resources/mock-images))}
   {:key     :thumbnail-size
    :type    :select
    :options [{:key :normal}
              {:key :large}]}])

(defn view
  []
  (let [state (reagent/atom
               {:description       "Turn your products or services into publicly tradeable items"
                :disabled-text     "Enable Preview"
                :enabled?          true
                :link              "rarible.com"
                :thumbnail         :collectible
                :thumbnail-size    :normal
                :title             "Rarible - NFT Marketplace"
                :width             295
                :with-description? true
                :with-logo?        true
                :with-thumbnail?   true})]
    (fn []
      (let [thumbnail (get resources/mock-images (:thumbnail @state))]
        [preview/preview-container {:state state :descriptor descriptor}
         [quo/link-preview
          {:logo            (when (:with-logo? @state)
                              (resources/get-mock-image :status-logo))
           :title           (:title @state)
           :description     (when (:with-description? @state)
                              (:description @state))
           :enabled?        (:enabled? @state)
           :on-enable       #(js/alert "Button pressed")
           :disabled-text   (:disabled-text @state)
           :link            (:link @state)
           :thumbnail       (when (:with-thumbnail? @state)
                              thumbnail)
           :thumbnail-size  (:thumbnail-size @state)
           :container-style {:width (:width @state)}}]]))))
