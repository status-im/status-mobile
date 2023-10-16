(ns status-im2.contexts.quo-preview.share.share-qr-code
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.image-server :as image-server]
            [utils.re-frame :as rf]))

(def descriptor
  [{:key :url :type :text}
   {:key :link-title :type :text}])

(defn view
  []
  (let [state (reagent/atom {:info-button? true
                             :link-title   "Link to profile"
                             :url          "status.app/u/zQ34e1zlOdas0pKnvrweeedsasas12adjie8"})]
    (fn []
      (let [qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         (:url @state)
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     300
                                  :error-level :highest})]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :component-container-style {:padding-vertical 20}}
         [quo/share-qr-code
          {:qr-image-uri      qr-media-server-uri
           :link-title        (:link-title @state)
           :url-on-press      #(js/alert "url pressed")
           :url-on-long-press #(js/alert "url long pressed")
           :share-on-press    #(js/alert "share pressed")
           :qr-url            (:url @state)}]]))))
