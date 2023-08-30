(ns status-im2.contexts.quo-preview.share.share-qr-code
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
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

(defn cool-preview
  []
  (let [state (reagent/atom {:info-button? true
                             :link-title   "Link to profile"
                             :url          "status.app/u/zQ34e1zlOdas0pKnvrweeedsasas12adjie8"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}]
        [preview/customizer state descriptor]
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

(defn preview-share-qr-code
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
