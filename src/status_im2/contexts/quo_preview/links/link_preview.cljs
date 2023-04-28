(ns status-im2.contexts.quo-preview.links.link-preview
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            utils.number))

(def descriptor
  [{:label "Title"
    :key   :title
    :type  :text}
   {:label "Description"
    :key   :description
    :type  :text}
   {:label "Link"
    :key   :link
    :type  :text}
   {:label "Container width"
    :key   :width
    :type  :text}
   {:label "Disabled text"
    :key   :disabled-text
    :type  :text}
   {:label "Enabled?"
    :key   :enabled?
    :type  :boolean}
   {:label   "Thumbnail"
    :key     :thumbnail
    :type    :select
    :options (mapv (fn [k]
                     {:key   k
                      :value (string/capitalize (name k))})
                   (keys resources/mock-images))}
   {:label   "Thumbnail size"
    :key     :thumbnail-size
    :type    :select
    :options [{:key   :normal
               :value :normal}
              {:key   :large
               :value :large}]}])

(defn cool-preview
  []
  (let [state (reagent/atom
               {:title          "Rarible - NFT Marketplace"
                :description    "Turn your products or services into publicly tradeable items"
                :link           "rarible.com"
                :thumbnail      :collectible
                :width          "295"
                :enabled?       true
                :thumbnail-size :normal
                :disabled-text  "Enable Preview"})]
    (fn []
      (let [width     (utils.number/parse-int (:width @state) 295)
            thumbnail (get resources/mock-images (:thumbnail @state))]
        [rn/view {:style {:margin-bottom 20}}
         [preview/customizer state descriptor]
         [rn/view
          {:style {:align-items :center
                   :margin-top  20}}
          [quo/link-preview
           {:logo            (resources/get-mock-image :status-logo)
            :title           (:title @state)
            :description     (:description @state)
            :enabled?        (:enabled? @state)
            :on-enable       #(js/alert "Button pressed")
            :disabled-text   (:disabled-text @state)
            :link            (:link @state)
            :thumbnail       thumbnail
            :thumbnail-size  (:thumbnail-size @state)
            :container-style {:width width}}]]]))))

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
