(ns quo2.components.avatars.user-avatar.view
  (:require [quo2.components.avatars.user-avatar.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            utils.string))

(defn user-avatar
  "Render user avatar with `profile-picture`
  `profile-picture` should be one of {:uri profile-picture-uri} or {:fn profile-picture-fn}

  `profile-picture-fn` should return an image URI, there's helper fn to generate
  it in `utils.image-server`

  params for `profile-picture-fn`
  {:length           initials' length
   :full-name        used to generate initials
   :font-size        initials font size
   :indicator-size   status indicator outer radius, set to nil or 0 when no indicator
   :indicator-border `indicator-size`-`indicator-border` is the inner radius
   :indicator-color  color for status indicator
   :override-theme   override theme for ring
   :background-color intials avatar background color
   :color            intials avatar text color
   :size             intials avatar radius
   :ring?            render ident ring around avatar?}

  supported color formats:
  #RRGGBB
  #RRGGBBAA
  rgb(255,255,255)
  rgba(255,255,255,0.1) note alpha is 0-1

  the reason we use the `profile-picture-fn` here is to separate
  logic (pubkey, key-uid... in subs) and style (color, size... in this component)"
  [{:keys [full-name size profile-picture customization-color static?
           status-indicator? online? ring? override-theme muted?]
    :or   {size                :big
           status-indicator?   true
           online?             true
           ring?               true
           customization-color :turquoise}}]
  (let [full-name          (or full-name "Your Name")
        outer-styles       (style/outer size (:uri profile-picture))
        ;; Once image is loaded, fast image re-renders view with the help of reagent atom,
        ;; But dynamic updates don't work when user-avatar is used inside hole-view
        ;; https://github.com/status-im/status-mobile/issues/15553
        image-view         (if static? rn/image fast-image/fast-image)
        font-size          (get-in style/sizes [size :font-size])
        amount-initials    (if (#{:xs :xxs :xxxs} size) 1 2)
        sizes              (get style/sizes size)
        indicator-color    (get style/indicator-color (if online? :online :offline))
        profile-picture-fn (:fn profile-picture)]

    [rn/view {:style outer-styles :accessibility-label :user-avatar}
     [image-view
      {:accessibility-label :profile-picture
       :style               outer-styles
       :source              (if profile-picture-fn
                              {:uri (profile-picture-fn
                                     {:length           amount-initials
                                      :full-name        full-name
                                      :font-size        (:font-size (text/text-style {:size font-size}))
                                      :indicator-size   (when status-indicator?
                                                          (:status-indicator sizes))
                                      :indicator-border (when status-indicator?
                                                          (:status-indicator-border sizes))
                                      :indicator-color  indicator-color
                                      :override-theme   override-theme
                                      :background-color (style/customization-color customization-color
                                                                                   theme)
                                      :color            (:color style/initials-avatar-text)
                                      :size             (:width outer-styles)
                                      :ring?            (if muted? false ring?)})}
                              profile-picture)}]]))
