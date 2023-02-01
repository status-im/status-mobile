(ns status-im.utils.image-server
  (:require [quo.design-system.colors :as colors]
            [taoensso.timbre :as log]))

(def ^:const image-server-uri-prefix "https://localhost:")
(def ^:const identicons-action "/messages/identicons")
(def ^:const account-images-action "/accountImages")
(def ^:const contact-images-action "/contactImages")

(defn current-theme
  []
  (case @colors/theme-type
    :light 1
    :dark  2))

(defn- timestamp
  []
  (.getTime (js/Date.)))

(defn get-identicons-uri
  [port public-key]
  (str image-server-uri-prefix
       port
       identicons-action
       "?publicKey="
       public-key
       "&theme="
       (current-theme)
       "&clock="
       (timestamp)
       "&addRing=1"))

(defn get-account-image-uri
  [port public-key image-name key-uid]
  (str image-server-uri-prefix
       port
       account-images-action
       "?publicKey="
       public-key
       "&keyUid="
       key-uid
       "&imageName="
       image-name
       "&theme="
       (current-theme)
       "&clock="
       (timestamp)
       "&addRing=1"))

(defn get-contact-image-uri
  [port public-key image-name clock]
  (str image-server-uri-prefix
       port
       contact-images-action
       "?publicKey="
       public-key
       "&imageName="
       image-name
       "&theme="
       (current-theme)
       "&clock="
       clock
       "&addRing=1"))

(defn get-dummy-qr-uri [port public-key image-name key-uid]
  (str image-server-uri-prefix port account-images-action "?publicKey=" public-key "&keyUid=" key-uid "&imageName=" image-name "&theme=" (current-theme) "&clock=" (timestamp) "&addRing=1"))
