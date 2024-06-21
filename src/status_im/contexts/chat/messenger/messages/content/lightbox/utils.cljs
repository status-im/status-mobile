(ns status-im.contexts.chat.messenger.messages.content.lightbox.utils
  (:require
    [utils.datetime :as datetime]
    [utils.re-frame :as rf]))

(defn convert-message-to-lightbox-image
  [{:keys [timestamp image-width image-height message-id from content]}]
  (let [[primary-name _] (rf/sub [:contacts/contact-two-names-by-identity from])]
    {:image        (:image content)
     :image-width  image-width
     :image-height image-height
     :id           message-id
     :header       primary-name
     :description  (when timestamp (datetime/to-short-str timestamp))}))
