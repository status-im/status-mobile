(ns utils.image-server
  (:require [utils.datetime :as datetime]))

(def ^:const image-server-uri-prefix "https://localhost:")
(def ^:const identicons-action "/messages/identicons")
(def ^:const account-images-action "/accountImages")
(def ^:const contact-images-action "/contactImages")

(defn timestamp [] (datetime/timestamp))

(defn current-theme-index
  [theme]
  (case theme
    :light 1
    :dark  2))

(defn get-identicons-uri
  [port public-key theme]
  (str image-server-uri-prefix
       port
       identicons-action
       "?publicKey="
       public-key
       "&theme="
       (current-theme-index theme)
       "&clock="
       (timestamp)
       "&addRing=1"))

(defn get-account-image-uri
  [port public-key image-name key-uid theme]
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
       (current-theme-index theme)
       "&clock="
       (timestamp)
       "&addRing=1"))

(defn get-contact-image-uri
  [port public-key image-name clock theme]
  (str image-server-uri-prefix
       port
       contact-images-action
       "?publicKey="
       public-key
       "&imageName="
       image-name
       "&theme="
       (current-theme-index theme)
       "&clock="
       clock
       "&addRing=1"))
