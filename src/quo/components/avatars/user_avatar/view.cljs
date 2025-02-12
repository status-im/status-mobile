(ns quo.components.avatars.user-avatar.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.user-avatar.schema :as component-schema]
    [quo.components.avatars.user-avatar.style :as style]
    [quo.components.common.no-flicker-image :as no-flicker-image]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [schema.core :as schema]
    [utils.image-server :as image-server]
    utils.string))

(defn initials-avatar
  [{:keys [full-name size customization-color]
    :or   {customization-color :blue}}]
  (let [theme           (quo.theme/use-theme)
        font-size       (get-in style/sizes [size :font-size])
        amount-initials (if (#{:xs :xxs :xxxs} size) 1 2)]
    [rn/view
     {:accessibility-label :initials-avatar
      :style               (style/initials-avatar size customization-color theme)}
     [text/text
      {:style  style/initials-avatar-text
       :size   font-size
       :weight :semi-bold}
      (utils.string/get-initials full-name amount-initials)]]))

(defn icon-avatar
  [{:keys [size customization-color]
    :or   {customization-color :blue}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :icon-avatar
      :style               (style/initials-avatar size customization-color theme)}
     [icon/icon (if (= size :big) :i/user :i/friend)
      {:size  (style/default-user-icon-size size)
       :color colors/white-opa-70}]]))

(defn- profile-picture-source
  [{:keys [picture-config profile-picture size full-name status-indicator? online? theme
           outer-styles ring?]}]
  (let [amount-initials (if (#{:xs :xxs :xxxs} size) 1 2)
        font-size       (get-in style/sizes [size :font-size])
        sizes           (get style/sizes size)
        indicator-color (get (style/indicator-color theme) (if online? :online :offline))
        font-size       (:font-size (text/text-style {:size font-size} nil))]
    (cond
      picture-config
      {:uri (image-server/get-image-uri
             picture-config
             {:length                   amount-initials
              :full-name                full-name
              :font-size                font-size
              :indicator-size           (when status-indicator?
                                          (:status-indicator sizes))
              :indicator-border         (when status-indicator?
                                          (:status-indicator-border sizes))
              :indicator-center-to-edge (when status-indicator?
                                          (:status-indicator-center-to-edge sizes))
              :indicator-color          indicator-color
              :theme                    theme
              :color                    (:color style/initials-avatar-text)
              :size                     (:width outer-styles)
              :ring?                    ring?
              :ring-width               (:ring-width sizes)})}

      (:uri profile-picture)
      profile-picture

      (number? profile-picture)
      profile-picture

      :else {:uri profile-picture})))

(defn default-display-name?
  [display-name]
  (and (some? display-name)
       (or (= display-name "")
           (string/includes? display-name "…"))))

(defn user-avatar-internal
  "Render user avatar with `profile-picture`

   WARNING:
   When calling the `profile-picture-fn` and passing the `:ring?` key, be aware that the `profile-picture-fn`
   may have an `:override-ring?` value. If it does then the `:ring?` value will not be used.
   For reference, refer to the `utils.image-server` namespace for these `profile-picture-fn` are generated."
  [{:keys [full-name size profile-picture static? status-indicator? online? ring?]
    :or   {size              :big
           status-indicator? true
           online?           true
           ring?             true}
    :as   props}]
  (let [theme          (quo.theme/use-theme)
        picture-config (:config profile-picture)
        full-name      full-name
        ;; image generated with `profile-picture-fn` is round cropped
        ;; no need to add border-radius for them
        outer-styles   (style/outer size (not picture-config))
        use-icon?      (default-display-name? full-name)
        ;; Once image is loaded, fast image re-renders view with the help of reagent atom,
        ;; But dynamic updates don't work when user-avatar is used inside hole-view
        ;; https://github.com/status-im/status-mobile/issues/15553
        image-view     (if static? no-flicker-image/image fast-image/fast-image)]

    [rn/view {:style outer-styles :accessibility-label :user-avatar}
     (cond
       use-icon?
       [icon-avatar
        {:customization-color (-> picture-config :options :customization-color)
         :size                size}]

       ;; this is for things that's not user-avatar but are currently using user-avatar to render
       ;; the initials e.g. community avatar
       (and full-name (not (or picture-config profile-picture)))
       [initials-avatar props]

       :else
       [image-view
        {:accessibility-label :profile-picture
         :style               outer-styles
         :source              (profile-picture-source {:picture-config    picture-config
                                                       :profile-picture   profile-picture
                                                       :size              size
                                                       :full-name         full-name
                                                       :status-indicator? status-indicator?
                                                       :online?           online?
                                                       :theme             theme
                                                       :outer-styles      outer-styles
                                                       :ring?             ring?})}])]))

(def user-avatar (schema/instrument #'user-avatar-internal component-schema/?schema))
