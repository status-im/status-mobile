(ns quo.components.avatars.user-avatar.view
  (:require
    [quo.components.avatars.user-avatar.schema :as component-schema]
    [quo.components.avatars.user-avatar.style :as style]
    [quo.components.common.no-flicker-image :as no-flicker-image]
    [quo.components.markdown.text :as text]
    [quo.context]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [schema.core :as schema]
    [utils.image-server :as image-server]
    utils.string))

(defn initials-avatar
  [{:keys [full-name size customization-color]
    :or   {customization-color :blue}}]
  (let [theme           (quo.context/use-theme)
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

(defn user-avatar-internal
  "Render user avatar with `profile-picture`."
  [{:keys [full-name size profile-picture static? status-indicator? online?]
    :or   {size              :big
           status-indicator? true
           online?           true}
    :as   props}]
  (let [theme           (quo.context/use-theme)
        picture-config  (:config profile-picture)
        full-name       (or full-name "Your Name")
        ;; image generated with `profile-picture-fn` is round cropped
        ;; no need to add border-radius for them
        outer-styles    (style/outer size (not picture-config))
        ;; Once image is loaded, fast image re-renders view with the help of reagent atom,
        ;; But dynamic updates don't work when user-avatar is used inside hole-view
        ;; https://github.com/status-im/status-mobile/issues/15553
        image-view      (if static? no-flicker-image/image fast-image/fast-image)
        font-size       (get-in style/sizes [size :font-size])
        amount-initials (if (#{:xs :xxs :xxxs} size) 1 2)
        sizes           (get style/sizes size)
        indicator-color (get (style/indicator-color theme) (if online? :online :offline))]

    [rn/view {:style outer-styles :accessibility-label :user-avatar}
     (if (and full-name (not (or picture-config profile-picture)))
       ;; this is for things that's not user-avatar but are currently using user-avatar to render
       ;; the initials e.g. community avatar
       [initials-avatar props]
       [image-view
        {:accessibility-label :profile-picture
         :style outer-styles
         :source
         (cond picture-config
               {:uri (image-server/get-image-uri
                      picture-config
                      {:length                   amount-initials
                       :full-name                full-name
                       :font-size                (:font-size (text/text-style {:size
                                                                               font-size}
                                                                              nil))
                       :indicator-size           (when status-indicator?
                                                   (:status-indicator sizes))
                       :indicator-border         (when status-indicator?
                                                   (:status-indicator-border sizes))
                       :indicator-center-to-edge (when status-indicator?
                                                   (:status-indicator-center-to-edge sizes))
                       :indicator-color          indicator-color
                       :theme                    theme
                       :color                    (:color style/initials-avatar-text)
                       :size                     (:width outer-styles)})}
               (:uri profile-picture)
               profile-picture

               (number? profile-picture)
               profile-picture

               :else {:uri profile-picture})}])]))

(def user-avatar (schema/instrument #'user-avatar-internal component-schema/?schema))
