(ns status-im2.contexts.quo-preview.share.share-qr-code
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]
            [reagent.core :as reagent]))


(def descriptor
  [{:label "URL"
    :key   :url
    :type  :text}
   {:label "Link title"
    :key   :link-title
    :type  :text}])

(defn preview-share-qr-code
  []
  (let [state (reagent/atom {:info-button? true
                             :link-title   "Link to profile"
                             :url          "status.app/u/zQ34e1zlOdas0pKnvrweeedsasas12adjie8"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}]
        [rn/view
         {:style {:padding-vertical 60
                  :justify-content  :center}}
         [preview/blur-view
          {:show-blur-background? true
           :height                600
           :blur-view-props       {:padding-top        20
                                   :padding-horizontal 20}}
          [quo/share-qr-code
           {:source            (resources/get-mock-image :qr-code)
            :link-title        (:link-title @state)
            :url-on-press      #(js/alert "url pressed")
            :url-on-long-press #(js/alert "url long pressed")
            :share-on-press    #(js/alert "share pressed")
            :qr-url            (:url @state)}]]]]])))
