(ns status-im2.contexts.quo-preview.links.url-preview-list
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]
    utils.number))

(def descriptor
  [{:label "Number of previews"
    :key   :previews-length
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:previews-length "3"})]
    (fn []
      (let [previews-length (min 6 (utils.number/parse-int (:previews-length @state)))]
        [rn/view {:style {:padding-bottom 150}}
         [preview/customizer state descriptor]
         [rn/view
          {:style {:align-items :center
                   :margin-top  50}}
          [quo/url-preview-list
           {:horizontal-spacing 20
            :on-clear           #(js/alert "Clear button pressed")
            :key-fn             :url
            :data               (for [index (range previews-length)
                                      :let  [index (inc index)]]
                                  {:title    (str "Title " index)
                                   :body     (str "status.im." index)
                                   :logo     (resources/get-mock-image :status-logo)
                                   :loading? false
                                   :url      (str "status.im." index)})}]]]))))

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
