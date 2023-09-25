(ns utils.image-server
  (:require
    [react-native.fs :as utils.fs]
    [react-native.platform :as platform]
    status-im2.common.pixel-ratio
    [status-im2.constants :as constants]
    [utils.datetime :as datetime]))

(def ^:const image-server-uri-prefix "https://localhost:")
(def ^:const account-images-action "/accountImages")
(def ^:const account-initials-action "/accountInitials")
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
             (fn [file?]
               (if file?
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
  "fn to get the avatar uri when multiaccount has custom image set
  not directly called, check `get-account-image-uri-fn`

  color formats (for all color options):
  #RRGGBB
  #RRGGBBAA
  rgb(255,255,255)
  rgba(255,255,255,0.1) note alpha is 0-1

  non-placeholder-avatar: requires at least one of `public-key` or `key-uid`
  placeholder-avatar: pass image file path as `image-name`

  `indicator-size` is outer indicator radius
  `indicator-size` - `indicator-border` is inner indicator radius"
  [{:keys [port public-key image-name key-uid size theme indicator-size
           indicator-border indicator-center-to-edge indicator-color ring?
           ring-width]}]
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
   (Math/round (* size status-im2.common.pixel-ratio/ratio))
   "&theme="
   (current-theme-index theme)
   "&clock="
   (timestamp)
   "&indicatorColor="
   (js/encodeURIComponent indicator-color)
   "&indicatorSize="
   (* indicator-size status-im2.common.pixel-ratio/ratio)
   "&indicatorBorder="
   (* indicator-border status-im2.common.pixel-ratio/ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge status-im2.common.pixel-ratio/ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width status-im2.common.pixel-ratio/ratio)))

(defn get-account-image-uri-fn
  "pass the result fn to user-avatar component as `:profile-picture`

  use this fn in subs to set multiaccount `:images` as [{:fn ...}]
  pass the image to user-avatar
  user-avatar can fill the rest style related options

  set `override-ring?` to a non-nil value to override `ring?`, mainly used to
  hide ring for account with ens name

  check `get-account-image-uri` for color formats"
  [{:keys [port public-key key-uid image-name theme override-ring?]}]
  (fn [{:keys [size indicator-size indicator-border indicator-center-to-edge
               indicator-color ring? ring-width override-theme]}]
    (get-account-image-uri
     {:port                     port
      :image-name               image-name
      :size                     size
      :public-key               public-key
      :key-uid                  key-uid
      :theme                    (if (nil? override-theme) theme override-theme)
      :indicator-size           indicator-size
      :indicator-border         indicator-border
      :indicator-center-to-edge indicator-center-to-edge
      :indicator-color          indicator-color
      :ring?                    (if (nil? override-ring?) ring? override-ring?)
      :ring-width               ring-width})))

(defn get-initials-avatar-uri
  "fn to get the avatar uri when account/contact/placeholder has no custom pic set
  not directly called, check `get-account-initials-uri-fn`

  multiaccount: at least one of `key-uid`, `public-key` is required to render the ring
  contact: `public-key` is required to render the ring

  check `get-account-image-uri` for color formats
  check `get-font-file-ready` for `font-file`

  `uppercase-ratio` is the uppercase-height/line-height for `font-file`"
  [{:keys [port public-key key-uid theme ring? length size background-color color
           font-size font-file uppercase-ratio indicator-size indicator-border
           indicator-center-to-edge indicator-color full-name ring-width]}]
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
   (Math/round (* size status-im2.common.pixel-ratio/ratio))
   "&bgColor="
   (js/encodeURIComponent background-color)
   "&color="
   (js/encodeURIComponent color)
   "&fontSize="
   (* font-size status-im2.common.pixel-ratio/ratio)
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
   (* indicator-size status-im2.common.pixel-ratio/ratio)
   "&indicatorBorder="
   (* indicator-border status-im2.common.pixel-ratio/ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge status-im2.common.pixel-ratio/ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width status-im2.common.pixel-ratio/ratio)))

(defn get-initials-avatar-uri-fn
  "return a fn that calls `get-account-initials-uri`
  pass the fn to user-avatar component to fill the style related options

  check `get-account-image-uri` for color formats
  check `get-font-file-ready` for `font-file`

  check `get-account-image-uri-fn` for `override-ring?`"
  [{:keys [port public-key key-uid theme override-ring? font-file]}]
  (fn [{:keys [full-name length size background-color font-size color
               indicator-size indicator-border indicator-color indicator-center-to-edge
               ring? ring-width override-theme]}]
    (get-initials-avatar-uri
     {:port                     port
      :public-key               public-key
      :key-uid                  key-uid
      :full-name                full-name
      :length                   length
      :size                     size
      :background-color         background-color
      :theme                    (if (nil? override-theme) theme override-theme)
      :ring?                    (if (nil? override-ring?) ring? override-ring?)
      :ring-width               ring-width
      :font-size                font-size
      :color                    color
      :font-file                font-file
      :uppercase-ratio          (:uppercase-ratio constants/initials-avatar-font-conf)
      :indicator-size           indicator-size
      :indicator-border         indicator-border
      :indicator-center-to-edge indicator-center-to-edge
      :indicator-color          indicator-color})))

(defn get-contact-image-uri
  [{:keys [port public-key image-name clock theme indicator-size indicator-border
           indicator-center-to-edge indicator-color size ring? ring-width]}]
  (str
   image-server-uri-prefix
   port
   contact-images-action
   "?publicKey="
   public-key
   "&imageName="
   image-name
   "&size="
   (Math/round (* size status-im2.common.pixel-ratio/ratio))
   "&theme="
   (current-theme-index theme)
   "&clock="
   clock
   "&indicatorColor="
   (js/encodeURIComponent indicator-color)
   "&indicatorSize="
   (* indicator-size status-im2.common.pixel-ratio/ratio)
   "&indicatorBorder="
   (* indicator-border status-im2.common.pixel-ratio/ratio)
   "&indicatorCenterToEdge="
   (* indicator-center-to-edge status-im2.common.pixel-ratio/ratio)
   "&addRing="
   (if ring? 1 0)
   "&ringWidth="
   (* ring-width status-im2.common.pixel-ratio/ratio)))

(defn get-contact-image-uri-fn
  [{:keys [port public-key image-name theme override-ring? clock]}]
  (fn [{:keys [size indicator-size indicator-border indicator-center-to-edge
               indicator-color ring? ring-width override-theme]}]
    (get-contact-image-uri {:port                     port
                            :image-name               image-name
                            :public-key               public-key
                            :size                     size
                            :theme                    (if (nil? override-theme) theme override-theme)
                            :clock                    clock
                            :indicator-size           indicator-size
                            :indicator-border         indicator-border
                            :indicator-center-to-edge indicator-center-to-edge
                            :indicator-color          indicator-color
                            :ring?                    (if (nil? override-ring?) ring? override-ring?)
                            :ring-width               ring-width})))

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
                                    (* 2 qr-size)
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
