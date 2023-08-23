(ns status-im2.contexts.quo-preview.links.url-preview-list
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :number :key :previews-length}])

(defn view
  []
  (let [state   (reagent/atom {:previews-length 3})
        padding 20]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 0}}
       [quo/url-preview-list
        {:horizontal-spacing padding
         :preview-width      (- (:width (rn/get-window))
                                (* 2 padding))
         :on-clear           #(js/alert "Clear button pressed")
         :key-fn             :url
         :data               (for [index (range (:previews-length @state))
                                   :let  [index (inc index)]]
                               {:title    (str "Title " index)
                                :body     (str "status.im." index)
                                :logo     (resources/get-mock-image
                                           :status-logo)
                                :loading? false
                                :url      (str "status.im." index)})}]])))
