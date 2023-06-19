(ns utils.image-server
  (:require
    [react-native.fs :as utils.fs]
    [react-native.platform :as platform]
    [status-im2.constants :as constants]
    [utils.datetime :as datetime]))

(def ^:const image-server-uri-prefix "https://localhost:")
(def ^:const account-images-action "/accountImages")
(def ^:const contact-images-action "/contactImages")
(def ^:const generate-qr-action "/GenerateQRCode")
(def ^:const status-profile-base-url "https://join.status.im/u/")
(def ^:const status-profile-base-url-without-https "join.status.im/u/")

(defn get-font-file-ready
  "setup font file and get the absolute path to it
  this font file is passed to status-go later to render the initials avatar

  for ios, it's located at main-bundle-path
  for android, it's located in the assets dir which can not be accessed by status-go
               so we copy one to the cache directory"
  [callback]
  (if platform/android?
    (let [cache-dir      (utils.fs/cache-dir)
          font-file-name (:android constants/initials-avatar-font-conf)
          src            (str "fonts/" font-file-name)
          dest           (str cache-dir "/" font-file-name)
          copy           #(utils.fs/copy-assets src dest)
          cb             #(callback dest)]
      (.then (utils.fs/file-exists? dest)
             (fn [exists?]
               (if exists?
                 (cb)
                 (.then (copy) cb)))))
    (callback (str (utils.fs/main-bundle-path)
                   "/"
                   (:ios constants/initials-avatar-font-conf)))))

(defn timestamp [] (datetime/timestamp))

(defn current-theme-index
  [theme]
  (case theme
    :light 1
    :dark  2))

(defn correction-level->index
  [level]
  (case (keyword level)
    :low     1
    :medium  2
    :quart   3
    :highest 4
    4))

(defn get-account-image-uri
  [{:keys [port public-key image-name key-uid theme ring?]}]
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
       "&addRing="
       (if ring? 1 0)))

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

(defn get-account-qr-image-uri
  [{:keys [key-uid public-key port qr-size]}]
  (let [profile-qr-url         (str status-profile-base-url public-key)
        base-64-qr-url         (js/btoa profile-qr-url)
        profile-image-type     "large"
        error-correction-level (correction-level->index :highest)
        superimpose-profile?   true
        media-server-url       (str image-server-uri-prefix
                                    port
                                    generate-qr-action
                                    "?level="
                                    error-correction-level
                                    "&url="
                                    base-64-qr-url
                                    "&keyUid="
                                    key-uid
                                    "&allowProfileImage="
                                    superimpose-profile?
                                    "&size="
                                    qr-size
                                    "&imageName="
                                    profile-image-type)]
    media-server-url))

(defn get-qr-image-uri-for-any-url
  [{:keys [url port qr-size error-level]}]
  (let [qr-url-base64          (js/btoa url)
        error-correction-level (correction-level->index error-level)
        superimpose-profile?   false
        media-server-url       (str image-server-uri-prefix
                                    port
                                    generate-qr-action
                                    "?level="
                                    error-correction-level
                                    "&url="
                                    qr-url-base64
                                    "&allowProfileImage="
                                    superimpose-profile?
                                    "&size="
                                    qr-size)]
    media-server-url))
