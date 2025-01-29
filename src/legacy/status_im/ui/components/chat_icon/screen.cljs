(ns legacy.status-im.ui.components.chat-icon.screen
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.chat-icon.styles :as styles]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.screens.chat.photos :as photos]
    [legacy.status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
    [quo.components.avatars.user-avatar.style :as user-avatar.style]
    [quo.core :as quo]
    [quo.theme]
    [re-frame.core :as re-frame.core]
    [react-native.core :as rn]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.ens.core :as utils.ens]))

;;TODO REWORK THIS NAMESPACE

(def get-name-first-char
  (memoize
   (fn [name]
     ;; TODO: for now we check if the first letter is a #
     ;; which means it is most likely a public chat and
     ;; use the second letter if that is the case
     ;; a broader refactoring should clean up upstream params
     ;; for default-chat-icon
     (string/capitalize (if (and (= "#" (first name))
                                 (< 1 (count name)))
                          (second name)
                          (first name))))))

(defn default-chat-icon
  [name styles]
  (when-not (string/blank? name)
    [rn/view (:default-chat-icon styles)
     [rn/text {:style (:default-chat-icon-text styles)}
      (get-name-first-char name)]]))

(defn profile-photo-plus-dot-view
  [{:keys [public-key full-name customization-color photo-container photo-path community?]}]
  (let [theme                   @(re-frame.core/subscribe [:theme])
        photo-container         (if (nil? photo-container)
                                  styles/container-chat-list
                                  photo-container)
        size                    (:width photo-container)
        dot-styles              (visibility-status-utils/icon-visibility-status-dot
                                 public-key
                                 size)
        dot-accessibility-label (:accessibility-label dot-styles)
        text-style              (styles/default-chat-icon-text size)]
    [rn/view
     {:style               photo-container
      :accessibility-label :profile-photo}
     (if (:fn photo-path)
       ;; temp support new media server avatar for old component
       [photos/photo
        {:uri ((:fn photo-path)
               {:size             size
                :full-name        full-name
                :font-size        (get text-style :font-size)
                :background-color (user-avatar.style/customization-color customization-color theme)
                :indicator-size   0
                :indicator-border 0
                :indicator-color  "#000000"
                :color            (get text-style :color)
                :length           2
                :ring?            (not (utils.ens/is-valid-eth-name? full-name))
                :ring-width       2})}
        {:size size}]
       [photos/photo photo-path {:size size}])
     (when-not community?
       [rn/view
        {:style               dot-styles
         :accessibility-label dot-accessibility-label}])]))

(defn custom-icon-view-list
  [name color & [size]]
  [rn/view (styles/container-list-size (or size 40))
   [default-chat-icon name
    {:default-chat-icon      (styles/default-chat-icon-profile color (or size 40))
     :default-chat-icon-text (styles/default-chat-icon-text (or size 40))}]])

(defn contact-icon-view
  [contact {:keys [container] :as styles}]
  [rn/view container
   [photos/photo (profile.utils/photo contact) styles]])

(defn contact-icon-contacts-tab
  [profile]
  [rn/view styles/container-chat-list
   [quo/user-avatar
    {:full-name         (profile.utils/displayed-name profile)
     :profile-picture   (profile.utils/photo profile)
     :size              :small
     :status-indicator? false}]])

(defn dapp-icon-permission
  [contact size]
  [contact-icon-view contact
   {:container              {:width size :height size}
    :size                   size
    :chat-icon              (styles/custom-size-icon size)
    :default-chat-icon      (styles/default-chat-icon-profile colors/default-chat-color size)
    :default-chat-icon-text (styles/default-chat-icon-text size)}])


