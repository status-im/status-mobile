;; Exception here as referrenced status-im.config, the implementation related to the image
;; server and the qr components leave something to be desired. Added an exception to the lint
;; rule as quick fix. Feel free to improve it if you're a brave soul refactor
(ns utils.image-server
  (:require
    [quo.foundations.colors :as colors]
    [react-native.fs :as utils.fs]
    [react-native.platform :as platform]
    [schema.core :as schema]
    [status-im.config :as config]
    [utils.datetime :as datetime]))

(def ^:const image-server-uri-prefix config/STATUS_BACKEND_SERVER_IMAGE_SERVER_URI_PREFIX)
(def ^:const account-images-action "/accountImages")
(def ^:const account-initials-action "/accountInitials")
(def ^:const contact-images-action "/contactImages")
(def ^:const generate-qr-action "/GenerateQRCode")

(defn get-font-file-ready
  "setup font file and get the absolute path to it
  this font file is passed to status-go later to render the initials avatar

  for ios, it's located at main-bundle-path
  for android, it's located in the assets dir which can not be accessed by status-go
               so we copy one to the cache directory"
  [font-file-name callback]
  (if platform/android?
    (let [cache-dir      (utils.fs/cache-dir)
          font-file-name font-file-name
          src            (str "fonts/" font-file-name)
          dest           (str cache-dir "/" font-file-name)
          copy           #(utils.fs/copy-assets src dest)
          cb             #(callback dest)]
      (.then (utils.fs/file-exists? dest)
             (fn [file?]
               (if file?
                 (cb)
                 (.then (copy) cb)))))
    (callback (str (utils.fs/main-bundle-path)
                   "/"
                   font-file-name))))

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
  "fn to get the avatar uri when multiaccount has custom image set.

  color formats (for all color options):
  #RRGGBB
  #RRGGBBAA
  rgb(255,255,255)
  rgba(255,255,255,0.1) note alpha is 0-1

  non-placeholder-avatar: requires at least one of `public-key` or `key-uid`
  placeholder-avatar: pass image file path as `image-name`

  `indicator-size` is outer indicator radius
  `indicator-size` - `indicator-border` is inner indicator radius
  `ring?` shows or hides ring for account with ens name"
  [{:keys [port public-key image-name key-uid size theme indicator-size
           indicator-border indicator-center-to-edge indicator-color ring?
           ring-width ratio]}]
  (str
   image-server-uri-prefix
   port
   account-images-action
   "?publicKey="
   public-key
   "&keyUid="
   key-uid
   "&imageName="
   image-name
   "&size="
   (Math/round (* size ratio))
   "&theme="
   (current-theme-index theme)
   "&clock="
   (timestamp)
   "&indicatorColor="
   (js/encodeURIComponent indicator-color)
   "&indicatorSize="
   (* indicator-size ratio)
   "&indicatorBorder="
   (* indicator-border ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width ratio)))

(defn get-initials-avatar-uri
  "fn to get the avatar uri when account/contact/placeholder has no custom pic set

  multiaccount: at least one of `key-uid`, `public-key` is required to render the ring
  contact: `public-key` is required to render the ring

  check `get-account-image-uri` for color formats
  check `get-font-file-ready` for `font-file`

  `ring?` shows or hides ring for account with ens name
  `uppercase-ratio` is the uppercase-height/line-height for `font-file`"
  [{:keys [port public-key key-uid theme ring? length size customization-color
           color font-size font-file uppercase-ratio indicator-size
           indicator-border indicator-center-to-edge indicator-color full-name
           ring-width ratio]}]
  (str
   image-server-uri-prefix
   port
   account-initials-action
   "?publicKey="
   public-key
   "&keyUid="
   key-uid
   "&length="
   length
   "&size="
   (Math/round (* size ratio))
   "&bgColor="
   (js/encodeURIComponent (colors/resolve-color customization-color theme))
   "&color="
   (js/encodeURIComponent color)
   "&fontSize="
   (* font-size ratio)
   "&fontFile="
   (js/encodeURIComponent font-file)
   "&uppercaseRatio="
   uppercase-ratio
   "&theme="
   (current-theme-index theme)
   "&clock="
   "&name="
   (js/encodeURIComponent full-name)
   (timestamp)
   "&indicatorColor="
   (js/encodeURIComponent indicator-color)
   "&indicatorSize="
   (* indicator-size ratio)
   "&indicatorBorder="
   (* indicator-border ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width ratio)))

(defn get-contact-image-uri
  "check `get-account-image-uri` for color formats
  check `get-font-file-ready` for `font-file`

  `public-key` is required to render the ring
  `ring?` shows or hides ring for account with ens name
  `uppercase-ratio` is the uppercase-height/line-height for `font-file`"
  [{:keys [port public-key image-name clock theme indicator-size indicator-border
           indicator-center-to-edge indicator-color size ring? ring-width ratio]}]
  (str
   image-server-uri-prefix
   port
   contact-images-action
   "?publicKey="
   public-key
   "&imageName="
   image-name
   "&size="
   (Math/round (* size ratio))
   "&theme="
   (current-theme-index theme)
   "&clock="
   clock
   "&indicatorColor="
   (js/encodeURIComponent indicator-color)
   "&indicatorSize="
   (* indicator-size ratio)
   "&indicatorBorder="
   (* indicator-border ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width ratio)))

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

(defn get-image-uri
  [{:keys [type options]}
   profile-picture-options]
  ((case type
     :account  get-account-image-uri
     :contact  get-contact-image-uri
     :initials get-initials-avatar-uri
     str)
   (-> (merge options profile-picture-options)
       (assoc :ring?
              (if (nil? (:override-ring? options))
                (:ring? profile-picture-options)
                (:override-ring? options))))))

(schema/=> get-image-uri
  [:=>
   [:cat
    :schema.quo/image-uri-config
    :schema.quo/profile-picture-options]
   :string])
