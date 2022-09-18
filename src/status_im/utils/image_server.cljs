(ns status-im.utils.image-server
  (:require [quo.design-system.colors :as colors]
            [status-im.utils.config :as config]))

(def ^:const image-server-uri-prefix "https://localhost:")
(def ^:const identicons-action "/messages/identicons")
(def ^:const account-images-action "/accountImages")
(def ^:const contact-images-action "/contactImages")

(defn current-theme []
  (case @colors/theme-type
    :light 1
    :dark 2))

(defn- timestamp []
  (.getTime (js/Date.)))

(defn get-identicons-uri [port public-key]
  (let [base (str image-server-uri-prefix port identicons-action "?publicKey=" public-key "&theme=" (current-theme) "&clock=" (timestamp))]
    (cond-> base
      @config/new-ui-enabled? (str "&addRing=1"))))

(defn get-account-image-uri [port public-key image-name key-uid]
  (let [base (str image-server-uri-prefix port account-images-action "?publicKey=" public-key "&keyUid=" key-uid "&imageName=" image-name "&theme=" (current-theme) "&clock=" (timestamp))]
    (cond-> base
      @config/new-ui-enabled? (str "&addRing=1"))))

(defn get-contact-image-uri [port public-key image-name clock]
  (let [base (str image-server-uri-prefix port contact-images-action "?publicKey=" public-key "&imageName=" image-name "&theme=" (current-theme) "&clock=" clock)]
    (cond-> base
      @config/new-ui-enabled? (str "&addRing=1"))))