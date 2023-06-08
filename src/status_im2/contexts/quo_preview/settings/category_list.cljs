(ns status-im2.contexts.quo-preview.settings.category-list
  (:require [quo2.components.settings.category-list.view :as quo]
            [react-native.core :as rn]
            [status-im2.common.resources :as resources]
            [reagent.core :as reagent]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as color]))

(def mockData
  [{:title               "Jazz"
    :accessibility-label :settings-list-item
    :left-icon           :browser-context
    :chevron?            true
    :on-press            (fn [] (js/alert "Link list item pressed"))}
   {:title               "Hip hop"
    :accessibility-label :settings-list-item
    :left-icon           :browser-context
    :chevron?            true
    :on-press            (fn [] (js/alert "Link list item pressed"))}
  {:title               "Folk"
   :accessibility-label :settings-list-item
   :left-icon           :browser-context
   :chevron?            true
   :on-press            (fn [] (js/alert "Link list item pressed"))}])

(defn get-mock-data [data]
  (merge
   data
   {:toggle-props (when (:toggle-props data)
                    {:checked?  true
                     :on-change (fn [new-value] (js/alert new-value))})
    :button-props (when (:button-props data)
                    {:title "Button" :on-press (fn [] (js/alert "Link pressed"))})
    :communities-props
    (when (:communities-props data)
      {:data
       [{:source (resources/mock-images :rarible)}
        {:source (resources/mock-images :decentraland)}
        {:source (resources/mock-images :coinbase)}]})}))

(defn cool-preview [item]
  (let [state (reagent/atom {:title               "Link"
                             :accessibility-label :settings-list-item
                             :left-icon           :browser-context
                             :chevron?            true
                             :on-press            (fn [] (js/alert "Link list item pressed"))})]
    [rn/view
     {:padding-vertical   10
      :padding-horizontal 10
      :border-color       (color/theme-colors color/neutral-20 color/neutral-80)
      :border-width       0.5}
     [quo/category-list (get-mock-data item)]]))

(defn preview-category-list []
  [rn/view {:style {:flex                1
                    :padding-vertical   10
                    :padding-horizontal 20}}
   [text/text
    {:weight           :medium
     :style            {:color         (color/theme-colors color/neutral-50 color/neutral-40)
                        :margin-bottom 15}
     :ellipsize-mode   :tail
     :number-of-lines  1
     :size             :paragraph-1}
    "Music"]
   [rn/flat-list
    {:flex                         1
     :data                         mockData
     :content-container-style      {:border-radius   20
                                    :border-width    1
                                    :overflow          :hidden
                                    :border-color    (color/theme-colors color/neutral-20 color/neutral-80)}
     :keyboard-should-persist-taps :always
     :render-fn                    cool-preview
     :key-fn                       str}]])