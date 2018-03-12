(ns status-im.utils.contacts
  (:require
    [status-im.utils.identicon :as identicon]
    [status-im.utils.gfycat.core :as gfycat]))

(defn whisper-id->new-contact [whisper-id]
  {:name             (gfycat/generate-gfy whisper-id)
   :photo-path       (identicon/identicon whisper-id)
   :pending?         true
   :whisper-identity whisper-id})
