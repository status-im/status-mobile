(ns status-im.contexts.preview-screens.quo-preview.community.data
  (:require
    [status-im.common.resources :as resources]
    [utils.i18n :as i18n]))

(def thumbnail
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII")

(def community
  {:id "0xsomeid"
   :name "Status"
   :description
   "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
   :customization-color :blue
   :community-icon thumbnail
   :tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]
   :tags [{:id 1 :tag-label (i18n/label :t/music) :resource (resources/get-image :music)}
          {:id        2
           :tag-label (i18n/label :t/lifestyle)
           :resource  (resources/get-image :lifestyle)}
          {:id        3
           :tag-label (i18n/label :t/podcasts)
           :resource  (resources/get-image :podcasts)}]})
